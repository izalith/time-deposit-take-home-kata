package org.ikigaidigital.adapter.inbound.web

import org.ikigaidigital.TimeDepositDetails
import org.ikigaidigital.Withdrawal
import org.ikigaidigital.adapter.inbound.web.model.TimeDeposit as TimeDepositDto
import org.ikigaidigital.adapter.inbound.web.model.Withdrawal as WithdrawalDto
import java.math.BigDecimal
import java.math.RoundingMode

internal object TimeDepositWebMapper {

    fun toDto(details: TimeDepositDetails): TimeDepositDto =
        TimeDepositDto(
            id = details.deposit.id,
            planType = details.deposit.planType,
            // Assumption: money is exposed as a decimal with cents rather than as a raw Double.
            balance = BigDecimal.valueOf(details.deposit.balance).setScale(2, RoundingMode.HALF_UP),
            days = details.deposit.days,
            withdrawals = details.withdrawals.map(::toDto),
        )

    private fun toDto(withdrawal: Withdrawal): WithdrawalDto =
        WithdrawalDto(id = withdrawal.id, amount = withdrawal.amount, date = withdrawal.date)
}
