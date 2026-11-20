package org.ikigaidigital.application.port.outbound

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.TimeDepositPage
import java.math.BigDecimal
import java.time.YearMonth

interface TimeDepositRepository {
    fun findPage(page: Int, size: Int): TimeDepositPage

    fun findNotCreditedFor(month: YearMonth, afterId: Int, batchSize: Int): List<TimeDeposit>

    fun creditInterest(month: YearMonth, interestByDepositId: Map<Int, BigDecimal>)
}
