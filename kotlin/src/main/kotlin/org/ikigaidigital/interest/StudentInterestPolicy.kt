package org.ikigaidigital.interest

import org.ikigaidigital.TimeDeposit

/** Student plan: 3% yearly interest, no interest after one year. */
class StudentInterestPolicy : MonthlyRateInterestPolicy(planType = PLAN_TYPE, annualRate = 0.03) {

    // "After 1 year" is interpreted as day 366 onwards (a year is assumed to be 365 days),
    // matching the original implementation.
    override fun isEligible(deposit: TimeDeposit): Boolean = deposit.days <= DAYS_IN_YEAR

    companion object {
        const val PLAN_TYPE = "student"
        private const val DAYS_IN_YEAR = 365
    }
}
