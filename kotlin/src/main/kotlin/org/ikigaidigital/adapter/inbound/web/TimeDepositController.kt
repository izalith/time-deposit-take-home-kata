package org.ikigaidigital.adapter.inbound.web

import org.ikigaidigital.adapter.inbound.web.api.TimeDepositsApi
import org.ikigaidigital.adapter.inbound.web.model.TimeDeposit
import org.ikigaidigital.application.port.inbound.GetAllTimeDepositsQuery
import org.ikigaidigital.application.port.inbound.UpdateAllBalancesUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class TimeDepositController(
    private val getAllTimeDeposits: GetAllTimeDepositsQuery,
    private val updateAllBalances: UpdateAllBalancesUseCase,
) : TimeDepositsApi {

    override fun getTimeDeposits(): ResponseEntity<List<TimeDeposit>> =
        ResponseEntity.ok(getAllTimeDeposits.getAllTimeDeposits().map(TimeDepositWebMapper::toDto))

    override fun updateBalances(): ResponseEntity<Unit> {
        updateAllBalances.updateAllBalances()
        return ResponseEntity.noContent().build()
    }
}
