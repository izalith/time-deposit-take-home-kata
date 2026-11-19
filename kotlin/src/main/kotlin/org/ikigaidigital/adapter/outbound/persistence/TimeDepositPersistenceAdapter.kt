package org.ikigaidigital.adapter.outbound.persistence

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.TimeDepositDetails
import org.ikigaidigital.application.port.outbound.TimeDepositDetailsRepository
import org.ikigaidigital.application.port.outbound.TimeDepositRepository
import org.springframework.stereotype.Component

@Component
class TimeDepositPersistenceAdapter(
    private val jpaRepository: TimeDepositJpaRepository,
) : TimeDepositRepository, TimeDepositDetailsRepository {

    override fun findAll(): List<TimeDeposit> =
        jpaRepository.findAll().map(TimeDepositPersistenceMapper::toDomain)

    override fun saveBalances(deposits: List<TimeDeposit>) {
        val balancesById = deposits.associate { it.id to TimeDepositPersistenceMapper.toStoredBalance(it.balance) }
        val entities = jpaRepository.findAllById(balancesById.keys)
        entities.forEach { entity -> entity.balance = balancesById.getValue(entity.id) }
        jpaRepository.saveAll(entities)
    }

    override fun findAllWithWithdrawals(): List<TimeDepositDetails> =
        jpaRepository.findAllWithWithdrawals().map(TimeDepositPersistenceMapper::toDetails)
}
