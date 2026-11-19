package org.ikigaidigital.application.service

import org.ikigaidigital.TimeDepositDetails
import org.ikigaidigital.application.port.inbound.GetAllTimeDepositsQuery
import org.ikigaidigital.application.port.outbound.TimeDepositDetailsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetAllTimeDepositsService(
    private val repository: TimeDepositDetailsRepository,
) : GetAllTimeDepositsQuery {

    @Transactional(readOnly = true)
    override fun getAllTimeDeposits(): List<TimeDepositDetails> = repository.findAllWithWithdrawals()
}
