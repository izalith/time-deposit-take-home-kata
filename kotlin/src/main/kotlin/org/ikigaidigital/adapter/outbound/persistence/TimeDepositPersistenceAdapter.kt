package org.ikigaidigital.adapter.outbound.persistence

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.TimeDepositPage
import org.ikigaidigital.application.port.outbound.TimeDepositRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.sql.Date
import java.time.YearMonth

@Component
class TimeDepositPersistenceAdapter(
    private val timeDeposits: TimeDepositJpaRepository,
    private val withdrawals: WithdrawalJpaRepository,
    private val jdbc: JdbcTemplate,
) : TimeDepositRepository {

    override fun findPage(page: Int, size: Int): TimeDepositPage {
        val deposits = timeDeposits.findAll(PageRequest.of(page, size, Sort.by("id")))
        val withdrawalsByDepositId =
            if (deposits.isEmpty) emptyMap()
            else withdrawals.findByTimeDepositIdInOrderByDateAscIdAsc(deposits.map { it.id }.toList())
                .groupBy { it.timeDepositId }
        return TimeDepositPage(
            items = deposits.map { TimeDepositPersistenceMapper.toDetails(it, withdrawalsByDepositId[it.id].orEmpty()) }.toList(),
            page = page,
            size = size,
            totalElements = deposits.totalElements,
        )
    }

    override fun findNotCreditedFor(month: YearMonth, afterId: Int, batchSize: Int): List<TimeDeposit> =
        timeDeposits.findNotCreditedFor(month.atDay(1), afterId, Pageable.ofSize(batchSize))
            .map(TimeDepositPersistenceMapper::toDomain)

    @Transactional
    override fun creditInterest(month: YearMonth, interestByDepositId: Map<Int, BigDecimal>) {
        val monthStart = Date.valueOf(month.atDay(1))
        jdbc.batchUpdate(CREDIT_INTEREST_SQL, interestByDepositId.entries.toList(), interestByDepositId.size) { statement, entry ->
            statement.setBigDecimal(1, entry.value)
            statement.setDate(2, monthStart)
            statement.setInt(3, entry.key)
            statement.setDate(4, monthStart)
        }
    }

    private companion object {
        val CREDIT_INTEREST_SQL = """
            UPDATE "timeDeposits" SET "balance" = "balance" + ?, "lastInterestMonth" = ?
            WHERE "id" = ? AND ("lastInterestMonth" IS NULL OR "lastInterestMonth" < ?)
            """.trimIndent()
    }
}
