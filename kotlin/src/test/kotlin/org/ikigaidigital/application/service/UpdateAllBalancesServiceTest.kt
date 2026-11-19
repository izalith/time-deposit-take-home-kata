package org.ikigaidigital.application.service

import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.TimeDepositCalculator
import org.ikigaidigital.application.port.outbound.TimeDepositRepository
import org.junit.jupiter.api.Test

class UpdateAllBalancesServiceTest {

    private class InMemoryTimeDepositRepository(deposits: List<TimeDeposit>) : TimeDepositRepository {
        private val stored = deposits.map { it.copy() }
        val balances = deposits.associate { it.id to it.balance }.toMutableMap()

        override fun findAll(): List<TimeDeposit> = stored.map { it.copy(balance = balances.getValue(it.id)) }

        override fun saveBalances(deposits: List<TimeDeposit>) {
            deposits.forEach { balances[it.id] = it.balance }
        }
    }

    @Test
    fun `applies monthly interest to all deposits and saves the new balances`() {
        val repository = InMemoryTimeDepositRepository(
            listOf(
                TimeDeposit(1, "basic", 1000.0, 31),
                TimeDeposit(2, "premium", 1000.0, 10),
            )
        )

        UpdateAllBalancesService(TimeDepositCalculator(), repository).updateAllBalances()

        assertThat(repository.balances).containsEntry(1, 1000.83).containsEntry(2, 1000.0)
    }
}
