package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.Transport;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransportRepository extends JpaRepository<Transport, UUID>, JpaSpecificationExecutor<Transport> {
    
    Optional<Transport> findByStateNumber(String stateNumber);
    
    @Query(value = """
                    select t
                    from Transport t
                    where (upper(t.stateNumber) like upper('%' || ?1 || '%') or ?1 is null)
                        and (?2 is null or exists ( select 1 from t.organizations o where o.id = ?2 ))
                        and t.status = 'IN_USE'
                    order by t.stateNumber
                   """,
           countQuery = """
                        select count(t)
                        from Transport t
                        where (upper(t.stateNumber) like upper('%' || ?1 || '%') or ?1 is null)
                            and (?2 is null or exists ( select 1 from t.organizations o where o.id = ?2 ))
                            and t.status = 'IN_USE'
                        """)
    Page<Transport> findAllByStateNumber(
            String stateNumber,
            UUID organizationId,
            Pageable pageable
                                        );
    
    @EntityGraph(attributePaths = {"organizations"})
    @Query("SELECT t FROM Transport t WHERE t.id = :id")
    Optional<Transport> findByIdWithOrganizations(@Param("id") UUID id);

    
}
