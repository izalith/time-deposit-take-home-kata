package org.ikigaidigital.interest

class BasicInterestPolicy : MonthlyRateInterestPolicy(planType = PLAN_TYPE, annualRate = 0.01) {
    companion object {
        const val PLAN_TYPE = "basic"
    }
}
