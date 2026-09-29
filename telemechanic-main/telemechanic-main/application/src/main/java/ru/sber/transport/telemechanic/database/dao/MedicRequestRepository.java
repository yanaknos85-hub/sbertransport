package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.MedicRequest;
import ru.sber.transport.telemechanic.database.projection.TelemedicineSearchProjection;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface MedicRequestRepository extends JpaRepository<MedicRequest, UUID> {
    
    @Query(value = """
                    select mr1_0.id                                                                                                        as id,
                           mr1_0.human_readable_id                                                                                         as humanReadableId,
                           e1_0.id                                                                                                         as ewbId,
                           e1_0.human_readable_id                                                                                          as ewbHumanReadableId,
                           o1_0.official_name                                                                                              as organizationName,
                           mr1_0.status                                                                                                    as status,
                           mr1_0.creation_time                                                                                             as creationTime,
                           (((emp1_0.last_name || ' ') || emp1_0.first_name) ||
                            case when emp1_0.patronymic is null then '' when emp1_0.patronymic = '' then '' else (' ' || emp1_0.patronymic) end) as driverFullName
                    from telemechanic.ewb e1_0
                             join telemechanic.medic_request mr1_0 on mr1_0.id = e1_0.medic_request_id
                             join telemechanic.organization o1_0 on e1_0.organization_id = o1_0.id
                             join telemechanic.driver dr1_0 ON e1_0.driver_id = dr1_0.id
                                      join telemechanic.employee emp1_0 on dr1_0.employee_id = emp1_0.id
                        where mr1_0.organization_id = (select e2_0.organization_id from telemechanic.employee e2_0 where e2_0.user_id = :userId)
                          and (upper(mr1_0.human_readable_id) like upper('%' || trim(:searchText) || '%')
                               or upper(emp1_0.full_name_index) like upper('%' || trim(:searchText) || '%')
                               or cast(:searchText as text) is null)
                          and (e1_0.organization_id = :organizationId or (cast(:organizationId as uuid) is null))
                          and (mr1_0.status in (:requestStatusSet) or (coalesce(:requestStatusSet) is null))
                          and ((mr1_0.creation_time between :creationTimeStart and :creationTimeEnd)
                            or (cast(:creationTimeStart as text) is null and mr1_0.creation_time < :creationTimeEnd)
                            or (cast(:creationTimeEnd as text) is null and mr1_0.creation_time > :creationTimeStart)
                            or (cast(:creationTimeStart as text) is null and cast(:creationTimeEnd as text) is null))
                          and ((e1_0.medic_decision_time between :medicDecisionTimeStart and :medicDecisionTimeEnd)
                            or (cast(:medicDecisionTimeStart as text) is null and e1_0.medic_decision_time < :medicDecisionTimeEnd)
                            or (cast(:medicDecisionTimeEnd as text) is null and e1_0.medic_decision_time > :medicDecisionTimeStart)
                            or (cast(:medicDecisionTimeStart as text) is null and cast(:medicDecisionTimeEnd as text) is null))
                   """,
           countQuery = """
                        select count(*)
                        from telemechanic.ewb e1_0
                             join telemechanic.medic_request mr1_0 on mr1_0.id = e1_0.medic_request_id
                             join telemechanic.organization o1_0 on e1_0.organization_id = o1_0.id
                             join telemechanic.driver dr1_0 ON e1_0.driver_id = dr1_0.id
                                      join telemechanic.employee emp1_0 on dr1_0.employee_id = emp1_0.id
                        where mr1_0.organization_id = (select e2_0.organization_id from telemechanic.employee e2_0 where e2_0.user_id = :userId)
                          and (upper(mr1_0.human_readable_id) like upper('%' || trim(:searchText) || '%')
                               or upper(emp1_0.full_name_index) like upper('%' || trim(:searchText) || '%')
                               or cast(:searchText as text) is null)
                          and (e1_0.organization_id = :organizationId or (cast(:organizationId as uuid) is null))
                          and (mr1_0.status in (:requestStatusSet) or (coalesce(:requestStatusSet) is null))
                          and ((mr1_0.creation_time between :creationTimeStart and :creationTimeEnd)
                            or (cast(:creationTimeStart as text) is null and mr1_0.creation_time < :creationTimeEnd)
                            or (cast(:creationTimeEnd as text) is null and mr1_0.creation_time > :creationTimeStart)
                            or (cast(:creationTimeStart as text) is null and cast(:creationTimeEnd as text) is null))
                          and ((e1_0.medic_decision_time between :medicDecisionTimeStart and :medicDecisionTimeEnd)
                            or (cast(:medicDecisionTimeStart as text) is null and e1_0.medic_decision_time < :medicDecisionTimeEnd)
                            or (cast(:medicDecisionTimeEnd as text) is null and e1_0.medic_decision_time > :medicDecisionTimeStart)
                            or (cast(:medicDecisionTimeStart as text) is null and cast(:medicDecisionTimeEnd as text) is null))
                        """,
           nativeQuery = true)
    Page<TelemedicineSearchProjection> searchMedicRequests(
            UUID userId,
            String searchText,
            UUID organizationId,
            Set<String> requestStatusSet,
            LocalDateTime creationTimeStart,
            LocalDateTime creationTimeEnd,
            LocalDateTime medicDecisionTimeStart,
            LocalDateTime medicDecisionTimeEnd,
            PageRequest pageRequest
                                                          );
    
    List<MedicRequest> findAllByStatusAndCreationTimeBetween(TelemedicineStatus status, LocalDateTime creationTimeStart,
                                                             LocalDateTime creationTimeEnd);
    
    @Modifying
    @Query(value = """
                   UPDATE telemechanic.medic_request
                   SET status = 'EXPIRED'
                   WHERE id in (:ids)
                   """, nativeQuery = true)
    void updateMedicRequestsStatus(List<UUID> ids);
}
