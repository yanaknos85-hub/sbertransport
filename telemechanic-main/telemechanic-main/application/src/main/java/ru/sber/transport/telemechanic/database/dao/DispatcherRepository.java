package ru.sber.transport.telemechanic.database.dao;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.Dispatcher;
import ru.sber.transport.telemechanic.database.model.Dispatcher_;
import ru.sber.transport.telemechanic.dto.dispatcher.SearchDispatcherRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий диспетчеров
 */
@Repository
public interface DispatcherRepository extends JpaRepository<Dispatcher, UUID> {
    
    @NotNull
    @Override
    @EntityGraph(attributePaths = { Dispatcher_.EMPLOYEE, Dispatcher_.ORGANIZATION, Dispatcher_.DEPARTMENT, Dispatcher_.ATTORNEY })
    Optional<Dispatcher> findById(@NotNull UUID id);
    
    @NotNull
    @Override
    @EntityGraph(attributePaths = { Dispatcher_.ATTORNEY })
    List<Dispatcher> findAll();
    
    @EntityGraph(attributePaths = { Dispatcher_.EMPLOYEE, Dispatcher_.ORGANIZATION, Dispatcher_.DEPARTMENT, Dispatcher_.ATTORNEY })
    @Query("""
            SELECT d
            FROM Dispatcher d
            WHERE ((d.organization.id = :#{#filter.organizationId}) OR (:#{#filter.organizationId} IS NULL))
            AND ((d.department.id = :#{#filter.departmentId}) OR (:#{#filter.departmentId} IS NULL))
            AND ((d.active = :#{#filter.active}) OR (:#{#filter.active} IS NULL))
            AND ((d.employee.personnelNumber LIKE %:#{#filter.personnelNumber}%) OR (:#{#filter.personnelNumber} IS NULL))
           """)
    Page<Dispatcher> findAllBySearchFilters(
            SearchDispatcherRequest filter,
            PageRequest pageable
                                           );
    @EntityGraph(attributePaths = { Dispatcher_.EMPLOYEE, Dispatcher_.ORGANIZATION, Dispatcher_.DEPARTMENT, Dispatcher_.ATTORNEY })
    @SuppressWarnings("java:S100")
    boolean existsByEmployee_IdAndOrganization_IdAndActiveIsTrue(UUID employeeId, UUID organizationId);

    @EntityGraph(attributePaths = { Dispatcher_.EMPLOYEE, Dispatcher_.ORGANIZATION, Dispatcher_.DEPARTMENT, Dispatcher_.ATTORNEY })
    @SuppressWarnings("java:S100")
    boolean existsByAttorney_NumberAndOrganization_IdAndActiveIsTrue(UUID attorneyNumber, UUID organizationId);
    
    @Modifying
    @Query("""
           UPDATE Dispatcher d
           SET d.active = FALSE
           WHERE d.attorney.expiryDate < CURRENT_DATE
           """)
    void setActiveFalseWhereExpiryDateBeforeNow();
    
    @EntityGraph(attributePaths = { Dispatcher_.EMPLOYEE, Dispatcher_.ORGANIZATION, Dispatcher_.DEPARTMENT, Dispatcher_.ATTORNEY })
    List<Dispatcher> findByEmployeeUserIdAndActiveIsTrue(UUID userId);
    
    Optional<Dispatcher> findByEmployeeIdAndActiveIsTrue(UUID employeeId);
    
    Optional<Dispatcher> findByAttorneyNumber(UUID attorneyNumber);
}
