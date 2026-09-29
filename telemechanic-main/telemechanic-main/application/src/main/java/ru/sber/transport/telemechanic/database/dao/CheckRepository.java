package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface CheckRepository extends JpaRepository<Check, UUID> {
    
    Optional<Check> findByRequestIdAndCheckType(UUID requestId, CheckType checkType);
    
    List<Check> findAllByRequestId(UUID requestId);

    boolean existsByRequestIdAndCheckTypeAndCheckStatus(UUID requestId, CheckType checkType, CheckStatus checkStatus);
    
    @Query("select c from Check c where c.request.id = :requestId and c.checkType in :checkTypePrefix")
    List<Check> findAllByRequestIdAndCheckTypeContains(UUID requestId, Set<CheckType> checks);
}
