package org.ikigaidigital.application.port.inbound

fun interface UpdateAllBalancesUseCase {
    /** Applies one month of interest to every stored time deposit. */
    fun updateAllBalances()
}
