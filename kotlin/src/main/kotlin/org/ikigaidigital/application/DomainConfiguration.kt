package org.ikigaidigital.application

import org.ikigaidigital.TimeDepositCalculator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** Exposes the framework-free domain services as Spring beans. */
@Configuration(proxyBeanMethods = false)
class DomainConfiguration {

    @Bean
    fun timeDepositCalculator(): TimeDepositCalculator = TimeDepositCalculator()
}
