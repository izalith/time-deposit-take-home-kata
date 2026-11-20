package org.ikigaidigital.application.port.outbound

import org.ikigaidigital.TimeDeposit

interface TimeDepositRepository {
    fun findAll(): List<TimeDeposit>

    fun saveBalances(deposits: List<TimeDeposit>)
}
