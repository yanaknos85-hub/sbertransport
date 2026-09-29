package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingJoinRequest;

import java.util.List;
import java.util.UUID;

public interface CarsharingJoinRequestRepository extends JpaRepository<CarsharingJoinRequest, UUID> {
    
    List<CarsharingJoinRequest> findByEmployeeIdAndRequestStatus(UUID employeeId, CarsharingJoinRequestStatus status);
    List<CarsharingJoinRequest> findByRequestStatus(CarsharingJoinRequestStatus status);
    List<CarsharingJoinRequest> findByEmployeeDepartmentOrganizationIdAndRequestStatus(UUID organizationId,
                                                                                CarsharingJoinRequestStatus status);
}
