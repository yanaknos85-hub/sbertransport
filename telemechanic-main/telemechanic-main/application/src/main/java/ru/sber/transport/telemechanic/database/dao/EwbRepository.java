package ru.sber.transport.telemechanic.database.dao;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface EwbRepository extends JpaRepository<Ewb, UUID>, JpaSpecificationExecutor<Ewb> {
    
    @EntityGraph(attributePaths = { Ewb_.REQUEST })
    Optional<Ewb> findEwbByRequestId(UUID requestId);
    
    @EntityGraph(attributePaths = { Ewb_.REQUEST,
                                    Ewb_.TRANSPORT,
                                    Ewb_.REQUEST + "." + Request_.CHECKS,
                                    Ewb_.REQUEST + "." + Request_.CHECKS + "." + Check_.PHOTOS })
    Optional<Ewb> findEwbWithTransportByRequestId(UUID requestId);
    
    @EntityGraph(attributePaths = { Ewb_.MEDIC_REQUEST, Ewb_.MEDIC_CONTRACTOR })
    Optional<Ewb> findByEwbUuid(UUID ewbUuid);
    
    @Query("""
           select ewb
           from Ewb ewb
           where ewb.driver.employee.id = :driverId
            and :requestDate between ewb.startDate and ewb.finishDate
            and ewb.status not in :finishStatuses
           """)
    @EntityGraph("on-the-line")
    Optional<Ewb> findEwbForRequestOnTheLine(UUID driverId, LocalDate requestDate, List<EwbStatus> finishStatuses);
    
    @NotNull
    @Override
    @EntityGraph(attributePaths = {
            Ewb_.AUTHOR,
            Ewb_.AUTHOR + "." + Employee_.ORGANIZATION,
            Ewb_.AUTHOR + "." + Employee_.DEPARTMENT,
            Ewb_.AUTHOR + "." + Employee_.POSITION,
            Ewb_.MEDIC,
            Ewb_.MEDIC + "." + Employee_.ORGANIZATION,
            Ewb_.MEDIC + "." + Employee_.POSITION,
            Ewb_.MEDIC_CONTRACTOR,
            Ewb_.TRANSPORT,
            Ewb_.ORGANIZATION,
            Ewb_.ORGANIZATION + "." + Organization_.CONTACTS,
            Ewb_.ORGANIZATION + "." + Organization_.ADDRESS,
            Ewb_.ORGANIZATION + "." + Organization_.ADDRESS + "." + OrganizationAddress_.REGION,
            Ewb_.REQUEST,
            Ewb_.REQUEST + "." + Request_.CHECKS,
            Ewb_.REQUEST + "." + Request_.AUTHOR,
            Ewb_.MEDIC_REQUEST,
            Ewb_.DRIVER,
            Ewb_.DRIVER + "." + Driver_.EMPLOYEE,
            Ewb_.DRIVER + "." + Driver_.EMPLOYEE + "." + Employee_.ORGANIZATION,
            Ewb_.DRIVER + "." + Driver_.EMPLOYEE + "." + Employee_.DEPARTMENT,
            Ewb_.DRIVER + "." + Driver_.EMPLOYEE + "." + Employee_.POSITION,
            Ewb_.DRIVER + "." + Driver_.DRIVING_LICENSE,
            Ewb_.TELEMECH_OUT,
            Ewb_.TELEMECH_OUT + "." + Employee_.POSITION,
            Ewb_.TELEMECH_IN,
            Ewb_.TELEMECH_IN + "." + Employee_.POSITION,
            Ewb_.ATTORNEY_OUT_ID
    })
    Optional<Ewb> findById(UUID id);
    
    @NotNull
    @Override
    @EntityGraph("search-ewb")
    List<Ewb> findAll();
    
    @NotNull
    @Override
    @EntityGraph("search-ewb")
    Page<Ewb> findAll(Specification<Ewb> spec, Pageable pageable);
    
    @Query("""
           select ewb
           from Ewb ewb
           join Driver d on d.employee.id = :employeeId
           where ewb.driver.id = d.id
            and :requestDate = ewb.startDate
            and (ewb.status in :accessStatuses
            or ewb.status in :declinedStatuses)
           """)
    @EntityGraph("search-ewb")
    List<Ewb> findByDriverIdAndStatusIn(UUID employeeId, LocalDate requestDate, Set<EwbStatus> accessStatuses, Set<EwbStatus> declinedStatuses);
    
    @EntityGraph("search-ewb")
    Optional<Ewb> findByMedicRequestId(UUID medicRequestId);
    
    boolean existsByTransportIdAndStartDateAndStatusIn(UUID transportId, LocalDate startDate, Set<EwbStatus> finishStatuses);
    
    boolean existsByTransportIdAndStatusIn(UUID transportId, Set<EwbStatus> finishStatus);
    
    boolean existsByDriverIdAndStartDateAndStatusIn(UUID driverId, LocalDate startDate, Set<EwbStatus> finishStatuses);
    
    @EntityGraph(attributePaths = { Ewb_.MEDIC_REQUEST,
                                    Ewb_.TRANSPORT,
                                    Ewb_.REQUEST,
                                    Ewb_.REQUEST + "." + Request_.CHECKS,
                                    Ewb_.REQUEST + "." + Request_.AUTHOR
    })
    Optional<Ewb> findByRequestId(UUID requestId);
    
    @Query("""
           select case
                      when (count(e) > 0) then true
                      else false
                      end
           from Ewb e
           where e.startDate >= :checkStartDate
             and e.status in :statuses
             and e.tariffDepartmentId in (:departmentIds)
           """)
    boolean ewbExistsByTariffDepartmentsAndStatuses(List<UUID> departmentIds, List<EwbStatus> statuses, LocalDate checkStartDate);
    
    @Query("""
           select ewb
           from Ewb ewb
           where ewb.transport.id = :transportId
            and (
                    :ewbStartDate between ewb.startDate and ewb.finishDate or
                    :ewbEndDate between ewb.startDate and ewb.finishDate
                )
            and ewb.status in (:activeStatuses)
           """)
    Optional<Ewb> findByTransportIdAndNewEwbDatesBetweenEwbDatesAndActiveStatuses(
            UUID transportId,
            LocalDate ewbStartDate,
            LocalDate ewbEndDate,
            Set<EwbStatus> activeStatuses
                                                                                 );
    
    @Query("""
           select ewb
           from Ewb ewb
           where ewb.driver.id = :driverId
            and (
                    :ewbStartDate between ewb.startDate and ewb.finishDate or
                    :ewbEndDate between ewb.startDate and ewb.finishDate
                )
            and ewb.status in (:activeStatuses)
           """)
    Optional<Ewb> findByDriverIdAndNewEwbDatesBetweenEwbDatesAndActiveStatuses(
            UUID driverId,
            LocalDate ewbStartDate,
            LocalDate ewbEndDate,
            Set<EwbStatus> activeStatuses
                                                                              );
    
    @Query("""
           select ewb
           from Ewb ewb
            join fetch ewb.author
            join fetch ewb.organization
            left join fetch ewb.organization.contacts
            join fetch ewb.transport
            join fetch ewb.medicRequest
            left join fetch ewb.medic
            left join fetch ewb.medicContractor
            join fetch ewb.telemechOut
           where ewb.driver.id = :driverId
            and ewb.status = 'ON_THE_LINE'
            and :requestDate between ewb.startDate AND ewb.finishDate
           """)
    Optional<Ewb> findCurrentOnTheLineByDriverId(UUID driverId, LocalDate requestDate);
    
    List<Ewb> findAllByStatusInAndFinishDate(Set<EwbStatus> status, LocalDate startDate);
    
    @Modifying
    @Query(value = """
                   UPDATE telemechanic.ewb
                   SET status = 'EXPIRED',
                       ewb_uuid = gen_random_uuid()
                   WHERE id in (:ids)
                   """, nativeQuery = true)
    void setEwbListExpired(List<UUID> ids);
}
