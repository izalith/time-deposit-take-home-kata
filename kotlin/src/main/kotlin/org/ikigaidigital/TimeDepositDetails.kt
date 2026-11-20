package org.ikigaidigital

// Assumption: withdrawals are read-only data for the API and do not affect interest (no rule requires it),
// so they live next to TimeDeposit instead of inside it, leaving the shared class unchanged.
data class TimeDepositDetails(
    val deposit: TimeDeposit,
    val withdrawals: List<Withdrawal>,
)
