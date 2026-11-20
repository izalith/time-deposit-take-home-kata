package org.ikigaidigital.adapter.outbound.persistence

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.TimeDepositDetails
import org.ikigaidigital.Withdrawal
import java.math.BigDecimal
import java.math.RoundingMode

internal object TimeDepositPersistenceMapper {

    fun toDomain(entity: TimeDepositEntity): TimeDeposit =
        TimeDeposit(entity.id, entity.planType, entity.balance.toDouble(), entity.days)

    fun toDetails(entity: TimeDepositEntity): TimeDepositDetails =
        TimeDepositDetails(
            deposit = toDomain(entity),
            withdrawals = entity.withdrawals.map { Withdrawal(it.id, it.amount, it.date) },
        )

    // Assumption: balances are stored as DECIMAL(19,2) while the shared TimeDeposit keeps Double.
    // Interest is already rounded to cents, so rounding here only removes binary noise (1000.8299999999999 -> 1000.83).
    fun toStoredBalance(balance: Double): BigDecimal =
        BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP)
}
