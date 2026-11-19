package org.ikigaidigital.application.port.outbound

import org.ikigaidigital.TimeDeposit

/** Write-side persistence port used by the balance update. */
interface TimeDepositRepository {
    fun findAll(): List<TimeDeposit>

    /** Persists the current balances of the given (already existing) deposits. */
    fun saveBalances(deposits: List<TimeDeposit>)
}
