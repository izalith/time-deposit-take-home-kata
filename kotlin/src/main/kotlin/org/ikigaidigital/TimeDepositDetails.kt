package org.ikigaidigital

import java.math.BigDecimal

// Assumption: withdrawals are read-only data for the API and do not affect interest (no rule requires it),
// so they live in this read model instead of TimeDeposit, leaving the shared class unchanged.
data class TimeDepositDetails(
    val id: Int,
    val planType: String,
    val balance: BigDecimal,
    val days: Int,
    val withdrawals: List<Withdrawal>,
)
