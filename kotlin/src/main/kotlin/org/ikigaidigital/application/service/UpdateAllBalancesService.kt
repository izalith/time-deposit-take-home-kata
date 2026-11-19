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

    // All deposits are updated in a single transaction: either every balance gets its interest or none.
    // Loading everything at once is fine for the kata; a large portfolio would need batching.
    @Transactional
    override fun updateAllBalances() {
        val deposits = repository.findAll()
        calculator.updateBalance(deposits)
        repository.saveBalances(deposits)
    }
}
