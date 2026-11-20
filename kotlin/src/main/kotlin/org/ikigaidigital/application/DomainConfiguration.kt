package org.ikigaidigital.application

import org.ikigaidigital.TimeDepositCalculator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration(proxyBeanMethods = false)
class DomainConfiguration {

    @Bean
    fun timeDepositCalculator(): TimeDepositCalculator = TimeDepositCalculator()

    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
