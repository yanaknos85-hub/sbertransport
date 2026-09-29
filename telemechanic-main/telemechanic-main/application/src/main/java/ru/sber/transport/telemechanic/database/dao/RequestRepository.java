package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Request;
import ru.sber.transport.telemechanic.database.model.Request_;
import ru.sber.transport.telemechanic.dto.RegistryExcelDto;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID>,
        JpaSpecificationExecutor<Request> {
    List<Request> findAllByAuthorAndStatusIn(Employee employee, List<RequestStatus> statuses);
    
    @Query("select new ru.sber.transport.telemechanic.dto.RegistryExcelDto(" +
           "r.humanReadableId, " +
           "r.author.organization.officialName, " +
           "r.creationTime, " +
           "r.checksStartedTime, " +
           "r.checksFinishedTime, " +
           "r.inspectionTime, " +
           "case " +
           "   when r.status = ru.sber.transport.telemechanic.enumerate.RequestStatus.ON_THE_LINE then 'Пройден' " +
           "   when r.status = ru.sber.transport.telemechanic.enumerate.RequestStatus.FINISHED then 'Пройден' " +
           "   when r.status = ru.sber.transport.telemechanic.enumerate.RequestStatus.DECLINED then 'Не пройден' " +
           "   else '' end, " +
           "r.transport.stateNumber, " +
           "r.transport.brand, " +
           "r.transport.model, " +
           "r.author.personnelNumber, " +
           "r.author.lastName " +
           "    || ' ' " +
           "    || r.author.firstName " +
           "    || case when r.author.patronymic is null then '' when r.author.patronymic = '' then '' else (' ' || r.author.patronymic) end, " +
           "(select rr.inspector.personnelNumber from Request rr where rr.id = r.id), " +
           "(select (rr.inspector.lastName " +
           "    || ' ' " +
           "    || rr.inspector.firstName " +
           "    || case when rr.inspector.patronymic is null then '' when rr.inspector.patronymic = '' then '' else (' ' || rr.inspector.patronymic) end) " +
           "   from Request rr " +
           "   where rr.id = r.id)," +
           "r.comment," +
           "r.author.department.id," +
           "'') " +
           "from Request r " +
           "where (r.author.department.organization.id = :organizationId or cast(:organizationId as string) is null) " +
           "and (r.humanReadableId like :humanReadableId or cast(:humanReadableId as string) is null) " +
           "and (r.author.personnelNumber = :personnelNumber or cast(:personnelNumber as string) is null) " +
           "and ((r.creationTime between :startCreationTime and :endCreationTime)" +
           " or (cast(:startCreationTime as string) is null and r.creationTime < :endCreationTime) " +
           " or (cast(:endCreationTime as string) is null and r.creationTime > :startCreationTime) " +
           " or (cast(:startCreationTime as string) is null and cast(:endCreationTime as string) is null)) " +
           "and (r.author.department.id in (:departmentIds) or :hasDepartments = false)" +
           "order by r.humanReadableId")
    List<RegistryExcelDto> findAllRegistryByOrganizationId(
            UUID organizationId, String humanReadableId, String personnelNumber,
            LocalDateTime startCreationTime, LocalDateTime endCreationTime,
            Set<UUID> departmentIds, boolean hasDepartments
                                                          );

    @Override
    @EntityGraph("search-request")
    Page<Request> findAll(Specification<Request> spec, Pageable pageable);
    
    @Override
    @EntityGraph(attributePaths = { Request_.AUTHOR, Request_.TRANSPORT, Request_.CHECKS })
    Optional<Request> findById(UUID id);
    
    List<Request> findAllByStatusInAndCreationTimeBetween(Set<RequestStatus> statuses, LocalDateTime start, LocalDateTime end);
}
