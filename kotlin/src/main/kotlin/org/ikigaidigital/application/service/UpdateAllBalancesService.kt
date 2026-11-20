package org.ikigaidigital.application.service

import org.ikigaidigital.TimeDepositCalculator
import org.ikigaidigital.application.port.inbound.UpdateAllBalancesUseCase
import org.ikigaidigital.application.port.outbound.TimeDepositRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateAllBalancesService(
    private val calculator: TimeDepositCalculator,
    private val repository: TimeDepositRepository,
) : UpdateAllBalancesUseCase {

    // Assumptions: one call applies one month of interest (same as updateBalance) and does not advance `days`,
    // which the original code does not do either. All balances are updated in one transaction: all or nothing.
    @Transactional
    override fun updateAllBalances() {
        val deposits = repository.findAll()
        calculator.updateBalance(deposits)
        repository.saveBalances(deposits)
    }
}
