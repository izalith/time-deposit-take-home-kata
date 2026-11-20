package org.ikigaidigital.application.service

import org.ikigaidigital.TimeDepositCalculator
import org.ikigaidigital.application.port.inbound.UpdateAllBalancesUseCase
import org.ikigaidigital.application.port.outbound.InterestPlanRepository
import org.ikigaidigital.application.port.outbound.TimeDepositRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionOperations
import java.time.Clock
import java.time.YearMonth

@Service
class UpdateAllBalancesService(
    private val deposits: TimeDepositRepository,
    private val plans: InterestPlanRepository,
    private val transaction: TransactionOperations,
    private val clock: Clock,
    @Value("\${time-deposits.balance-update.batch-size}") private val batchSize: Int,
) : UpdateAllBalancesUseCase {

    // Assumptions: interest is monthly, so it is applied at most once per deposit per calendar month (UTC);
    // `days` is not advanced, as in the original code. Each batch is one transaction that locks its rows
    // (FOR UPDATE SKIP LOCKED): concurrent requests split the deposits between them and never update one twice.
    // An external scheduler calls this once a month; a missed month is not caught up, only the current one is credited.
    override fun updateAllBalances() {
        // Assumption: plans are read on every run, so a rate change also applies to deposits opened earlier.
        // That may contradict their contract terms; accepted as the intended behaviour.
        val calculator = TimeDepositCalculator(plans.findAll())
        val month = YearMonth.now(clock)
        do {
            val updatedCount = transaction.execute { updateNextBatch(calculator, month) }!!
        } while (updatedCount == batchSize)
    }

    private fun updateNextBatch(calculator: TimeDepositCalculator, month: YearMonth): Int {
        val batch = deposits.findBatchToUpdate(month, batchSize)
        batch.forEach { deposit -> deposits.save(deposit.withMonthlyInterest(calculator, month)) }
        return batch.size
    }
}
