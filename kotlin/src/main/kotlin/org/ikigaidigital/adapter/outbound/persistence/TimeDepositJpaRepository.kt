package org.ikigaidigital.adapter.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TimeDepositJpaRepository : JpaRepository<TimeDepositEntity, Int> {

    // Single query instead of N+1 lazy loads.
    @Query("select distinct t from TimeDepositEntity t left join fetch t.withdrawals order by t.id")
    fun findAllWithWithdrawals(): List<TimeDepositEntity>
}
