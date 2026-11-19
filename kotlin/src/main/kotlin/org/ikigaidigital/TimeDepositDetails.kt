package org.ikigaidigital

/**
 * Read model combining a deposit with its withdrawals.
 *
 * Withdrawals are kept out of [TimeDeposit] on purpose: the shared class must not change,
 * and the current interest rules do not depend on withdrawals.
 */
data class TimeDepositDetails(
    val deposit: TimeDeposit,
    val withdrawals: List<Withdrawal>,
)
