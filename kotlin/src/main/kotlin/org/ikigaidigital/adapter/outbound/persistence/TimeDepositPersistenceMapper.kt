package org.ikigaidigital.adapter.outbound.persistence

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.TimeDepositDetails
import org.ikigaidigital.Withdrawal

internal object TimeDepositPersistenceMapper {

    // Assumption: the shared TimeDeposit keeps Double, so this is the only BigDecimal -> Double conversion.
    // It only feeds the interest calculation; stored and returned balances never go through Double.
    fun toDomain(entity: TimeDepositEntity): TimeDeposit =
        TimeDeposit(entity.id, entity.planType, entity.balance.toDouble(), entity.days)

    fun toDetails(entity: TimeDepositEntity, withdrawals: List<WithdrawalEntity>): TimeDepositDetails =
        TimeDepositDetails(
            id = entity.id,
            planType = entity.planType,
            balance = entity.balance,
            days = entity.days,
            withdrawals = withdrawals.map { Withdrawal(it.id, it.amount, it.date) },
        )
}
