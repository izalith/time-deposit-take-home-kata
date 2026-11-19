package org.ikigaidigital.interest

import org.ikigaidigital.TimeDeposit

/** Premium plan: 5% yearly interest, interest starts after 45 days. */
class PremiumInterestPolicy : MonthlyRateInterestPolicy(planType = PLAN_TYPE, annualRate = 0.05) {

    override fun isEligible(deposit: TimeDeposit): Boolean = deposit.days > INTEREST_START_DAYS

    companion object {
        const val PLAN_TYPE = "premium"
        private const val INTEREST_START_DAYS = 45
    }
}
