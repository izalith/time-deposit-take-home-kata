package org.ikigaidigital

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class TimeDepositApiIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val jdbc: JdbcTemplate,
) {

    @BeforeEach
    fun setUpData() {
        jdbc.execute("DELETE FROM \"withdrawals\"")
        jdbc.execute("DELETE FROM \"timeDeposits\"")
        jdbc.update(
            """
            INSERT INTO "timeDeposits" ("id", "planType", "days", "balance") VALUES
              (1, 'basic', 45, 1234567.00),
              (2, 'student', 366, 1000.00),
              (3, 'premium', 46, 1000.00),
              (4, 'basic', 30, 1000.00)
            """
        )
        jdbc.update(
            """
            INSERT INTO "withdrawals" ("id", "timeDepositId", "amount", "date") VALUES
              (1, 1, 100.50, '2026-01-15'),
              (2, 1, 20.00, '2026-02-01')
            """
        )
    }

    @Test
    fun `GET returns all time deposits with their withdrawals`() {
        mockMvc.get("/time-deposits").andExpect {
            status { isOk() }
            content {
                json(
                    """
                    [
                      {"id":1,"planType":"basic","balance":1234567.00,"days":45,"withdrawals":[
                        {"id":1,"amount":100.50,"date":"2026-01-15"},
                        {"id":2,"amount":20.00,"date":"2026-02-01"}]},
                      {"id":2,"planType":"student","balance":1000.00,"days":366,"withdrawals":[]},
                      {"id":3,"planType":"premium","balance":1000.00,"days":46,"withdrawals":[]},
                      {"id":4,"planType":"basic","balance":1000.00,"days":30,"withdrawals":[]}
                    ]
                    """,
                    strict = true,
                )
            }
        }
    }

    @Test
    fun `POST balance-updates applies one month of interest and persists it`() {
        mockMvc.post("/time-deposits/balance-updates").andExpect { status { isNoContent() } }

        assertThat(storedBalances()).containsExactly(
            BigDecimal("1235595.81"),
            BigDecimal("1000.00"),
            BigDecimal("1004.17"),
            BigDecimal("1000.00"),
        )
    }

    @Test
    fun `each balance update adds another month of interest`() {
        repeat(2) { mockMvc.post("/time-deposits/balance-updates").andExpect { status { isNoContent() } } }

        assertThat(storedBalances()[2]).isEqualByComparingTo("1008.35")
    }

    private fun storedBalances(): List<BigDecimal> =
        jdbc.queryForList("SELECT \"balance\" FROM \"timeDeposits\" ORDER BY \"id\"", BigDecimal::class.java)
}
