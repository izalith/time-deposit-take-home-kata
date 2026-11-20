package org.ikigaidigital.application.port.outbound

import org.ikigaidigital.TimeDepositDetails

fun interface TimeDepositDetailsRepository {
    fun findAllWithWithdrawals(): List<TimeDepositDetails>
}
