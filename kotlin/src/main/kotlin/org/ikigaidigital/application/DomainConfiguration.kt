package org.ikigaidigital.application

import org.ikigaidigital.TimeDepositCalculator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class DomainConfiguration {

    @Bean
    fun timeDepositCalculator(): TimeDepositCalculator = TimeDepositCalculator()
}
