package org.ikigaidigital.application.port.inbound

import org.ikigaidigital.TimeDepositDetails

fun interface GetAllTimeDepositsQuery {
    fun getAllTimeDeposits(): List<TimeDepositDetails>
}
