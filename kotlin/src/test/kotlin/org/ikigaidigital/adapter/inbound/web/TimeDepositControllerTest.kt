package org.ikigaidigital.adapter.inbound.web

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.TimeDepositDetails
import org.ikigaidigital.Withdrawal
import org.ikigaidigital.application.port.inbound.GetAllTimeDepositsQuery
import org.ikigaidigital.application.port.inbound.UpdateAllBalancesUseCase
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.LocalDate

@WebMvcTest(TimeDepositController::class)
class TimeDepositControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockBean
    private lateinit var getAllTimeDeposits: GetAllTimeDepositsQuery

    @MockBean
    private lateinit var updateAllBalances: UpdateAllBalancesUseCase

    @Test
    fun `GET returns all time deposits with the contract schema`() {
        `when`(getAllTimeDeposits.getAllTimeDeposits()).thenReturn(
            listOf(
                TimeDepositDetails(
                    TimeDeposit(1, "basic", 1000.8299999999999, 31),
                    listOf(Withdrawal(7, BigDecimal("100.50"), LocalDate.of(2026, 1, 15))),
                ),
                TimeDepositDetails(TimeDeposit(2, "premium", 50.0, 10), emptyList()),
            )
        )

        mockMvc.get("/time-deposits").andExpect {
            status { isOk() }
            content {
                json(
                    """
                    [
                      {"id":1,"planType":"basic","balance":1000.83,"days":31,
                       "withdrawals":[{"id":7,"amount":100.50,"date":"2026-01-15"}]},
                      {"id":2,"planType":"premium","balance":50.00,"days":10,"withdrawals":[]}
                    ]
                    """,
                    strict = true,
                )
            }
        }
    }

    @Test
    fun `POST balance-updates triggers the balance update`() {
        mockMvc.post("/time-deposits/balance-updates").andExpect {
            status { isNoContent() }
        }

        verify(updateAllBalances).updateAllBalances()
    }
}
