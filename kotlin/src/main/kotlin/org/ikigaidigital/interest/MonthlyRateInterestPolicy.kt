package org.ikigaidigital.interest

import org.ikigaidigital.TimeDeposit

abstract class MonthlyRateInterestPolicy(
    final override val planType: String,
    private val annualRate: Double,
) : InterestPolicy {

    final override fun monthlyInterest(deposit: TimeDeposit): Double =
        if (deposit.days > GRACE_PERIOD_DAYS && isEligible(deposit)) {
            deposit.balance * annualRate / MONTHS_PER_YEAR
        } else {
            0.0
        }

    protected open fun isEligible(deposit: TimeDeposit): Boolean = true

    companion object {
        const val GRACE_PERIOD_DAYS = 30
        private const val MONTHS_PER_YEAR = 12
    }
}
