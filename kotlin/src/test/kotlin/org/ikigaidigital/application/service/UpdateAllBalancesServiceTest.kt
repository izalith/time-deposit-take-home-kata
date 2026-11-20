package org.ikigaidigital.application.service

import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.TimeDepositCalculator
import org.ikigaidigital.TimeDepositPage
import org.ikigaidigital.application.port.outbound.TimeDepositRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset

class UpdateAllBalancesServiceTest {

    private class InMemoryTimeDepositRepository(private val deposits: List<TimeDeposit>) : TimeDepositRepository {
        val balances = deposits.associate { it.id to BigDecimal.valueOf(it.balance).setScale(2) }.toMutableMap()
        private val lastInterestMonths = mutableMapOf<Int, YearMonth>()
        val batchSizes = mutableListOf<Int>()

        override fun findPage(page: Int, size: Int): TimeDepositPage = throw UnsupportedOperationException()

        override fun findNotCreditedFor(month: YearMonth, afterId: Int, batchSize: Int): List<TimeDeposit> =
            deposits.filter { it.id > afterId && (lastInterestMonths[it.id]?.let { last -> last < month } ?: true) }
                .take(batchSize)
                .map { it.copy(balance = balances.getValue(it.id).toDouble()) }

        override fun creditInterest(month: YearMonth, interestByDepositId: Map<Int, BigDecimal>) {
            batchSizes += interestByDepositId.size
            interestByDepositId.forEach { (id, interest) ->
                balances[id] = balances.getValue(id) + interest
                lastInterestMonths[id] = month
            }
        }
    }

    private val deposits = listOf(
        TimeDeposit(1, "basic", 1000.0, 31),
        TimeDeposit(2, "premium", 1000.0, 10),
        TimeDeposit(3, "student", 1000.0, 31),
    )

    private fun service(repository: TimeDepositRepository, now: String) =
        UpdateAllBalancesService(TimeDepositCalculator(), repository, Clock.fixed(Instant.parse(now), ZoneOffset.UTC), batchSize = 2)

    @Test
    fun `credits monthly interest to all deposits batch by batch`() {
        val repository = InMemoryTimeDepositRepository(deposits)

        service(repository, "2026-11-20T10:00:00Z").updateAllBalances()

        assertThat(repository.batchSizes).containsExactly(2, 1)
        assertThat(repository.balances.values.map(BigDecimal::toPlainString)).containsExactly("1000.83", "1000.00", "1002.50")
    }

    @Test
    fun `credits interest only once per month`() {
        val repository = InMemoryTimeDepositRepository(deposits)

        repeat(2) { service(repository, "2026-11-20T10:00:00Z").updateAllBalances() }
        service(repository, "2026-12-01T00:00:00Z").updateAllBalances()

        assertThat(repository.balances.values.map(BigDecimal::toPlainString)).containsExactly("1001.66", "1000.00", "1005.01")
    }

    @Test
    fun `stops after an exactly full last batch`() {
        val repository = InMemoryTimeDepositRepository(deposits.take(2))

        service(repository, "2026-11-20T10:00:00Z").updateAllBalances()

        assertThat(repository.batchSizes).containsExactly(2)
    }
}
