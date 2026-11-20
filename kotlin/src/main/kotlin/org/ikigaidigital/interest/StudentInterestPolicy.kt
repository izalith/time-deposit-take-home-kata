package org.ikigaidigital.interest

import org.ikigaidigital.TimeDeposit

class StudentInterestPolicy : MonthlyRateInterestPolicy(planType = PLAN_TYPE, annualRate = 0.03) {

    // Assumption: "no interest after 1 year" means from day 366 on (a year = 365 days), as in the original code.
    override fun isEligible(deposit: TimeDeposit): Boolean = deposit.days <= DAYS_IN_YEAR

    companion object {
        const val PLAN_TYPE = "student"
        private const val DAYS_IN_YEAR = 365
    }
}
