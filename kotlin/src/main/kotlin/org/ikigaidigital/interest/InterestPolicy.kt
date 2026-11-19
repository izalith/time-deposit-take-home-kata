package org.ikigaidigital.interest

import org.ikigaidigital.TimeDeposit

/**
 * Strategy that computes the (unrounded) monthly interest for the deposits of one plan type.
 *
 * New plans or more complex rules (tiered rates, withdrawal-dependent interest, ...) are added
 * by providing a new implementation, without touching [org.ikigaidigital.TimeDepositCalculator].
 */
interface InterestPolicy {
    val planType: String

    fun monthlyInterest(deposit: TimeDeposit): Double
}
