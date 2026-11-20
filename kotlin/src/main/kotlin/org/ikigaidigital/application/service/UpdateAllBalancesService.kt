package org.ikigaidigital.application.service

import org.ikigaidigital.TimeDepositCalculator
import org.ikigaidigital.application.port.inbound.UpdateAllBalancesUseCase
import org.ikigaidigital.application.port.outbound.TimeDepositRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.YearMonth

@Service
class UpdateAllBalancesService(
    private val calculator: TimeDepositCalculator,
    private val repository: TimeDepositRepository,
    private val clock: Clock,
    @Value("\${time-deposits.balance-update.batch-size}") private val batchSize: Int,
) : UpdateAllBalancesUseCase {

    // Assumptions: one month of interest (same rules as updateBalance) is applied at most once per deposit per
    // calendar month, and `days` is not advanced, which the original code does not do either.
    // Each batch commits on its own; repeated, parallel or resumed runs skip deposits already credited this month.
    override fun updateAllBalances() {
        val month = YearMonth.now(clock)
        var lastId = Int.MIN_VALUE
        while (true) {
            val batch = repository.findNotCreditedFor(month, lastId, batchSize)
            if (batch.isEmpty()) return
            repository.creditInterest(month, batch.associate { it.id to calculator.monthlyInterest(it) })
            if (batch.size < batchSize) return
            lastId = batch.last().id
        }
    }
}
