package org.ikigaidigital.adapter.outbound.persistence

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface TimeDepositJpaRepository : JpaRepository<TimeDepositEntity, Int> {

    @Query(
        """
        select t from TimeDepositEntity t
        where t.id > :afterId and (t.lastInterestMonth is null or t.lastInterestMonth < :month)
        order by t.id
        """
    )
    fun findNotCreditedFor(month: LocalDate, afterId: Int, page: Pageable): List<TimeDepositEntity>
}
