package org.ikigaidigital

import org.ikigaidigital.interest.BasicInterestPolicy
import org.ikigaidigital.interest.InterestPolicy
import org.ikigaidigital.interest.PremiumInterestPolicy
import org.ikigaidigital.interest.StudentInterestPolicy
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Applies one month of interest to time deposits.
 *
 * The interest rules live in [InterestPolicy] strategies; the calculator only selects the policy
 * by plan type and applies the rounding. The no-arg constructor keeps the original API intact.
 */
class TimeDepositCalculator(policies: List<InterestPolicy> = DEFAULT_POLICIES) {

    private val policiesByPlanType: Map<String, InterestPolicy> = policies.associateBy { it.planType }

    fun updateBalance(xs: List<TimeDeposit>) {
        xs.forEach { deposit -> deposit.balance += roundToCents(monthlyInterest(deposit)) }
    }

    // Unknown plan types earn no interest, as in the original implementation.
    private fun monthlyInterest(deposit: TimeDeposit): Double =
        policiesByPlanType[deposit.planType]?.monthlyInterest(deposit) ?: 0.0

    private fun roundToCents(amount: Double): Double =
        BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).toDouble()

    companion object {
        val DEFAULT_POLICIES: List<InterestPolicy> = listOf(
            BasicInterestPolicy(),
            StudentInterestPolicy(),
            PremiumInterestPolicy(),
        )
    }
}
