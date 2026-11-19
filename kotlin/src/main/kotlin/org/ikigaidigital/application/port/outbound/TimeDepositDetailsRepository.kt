package org.ikigaidigital.application.port.outbound

import org.ikigaidigital.TimeDepositDetails

/** Read-side persistence port: deposits together with their withdrawals. */
fun interface TimeDepositDetailsRepository {
    fun findAllWithWithdrawals(): List<TimeDepositDetails>
}
