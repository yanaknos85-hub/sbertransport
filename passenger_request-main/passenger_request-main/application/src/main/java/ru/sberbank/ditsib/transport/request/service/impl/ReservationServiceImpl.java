package ru.sberbank.ditsib.transport.request.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.sber.transport.limits.grpc.LimitReservationServiceGrpc;
import ru.sber.transport.limits.grpc.OldReserve;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.dto.LimitReservationResultDto;
import ru.sberbank.ditsib.transport.request.dto.LimitReservationStatus;
import ru.sberbank.ditsib.transport.request.service.ReservationService;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    
    private final DepartmentService departmentService;
    
    @Setter
    @GrpcClient("limits")
    private LimitReservationServiceGrpc.LimitReservationServiceBlockingStub stub;
    
    @Override
    public LimitReservationResultDto makeReservation(
            Request request,
            Employee employee,
            double sum,
            Long bonusSum
                                                    ) {
        return makeReservation(request, request.getTransportType(), employee, sum, bonusSum);
    }
    
    @Override
    public LimitReservationResultDto makeReservation(
            Request request,
            TransportTypeEnum transportType,
            Employee employee,
            double sum,
            Long bonusSum
                                                    ) {
        return limitReservation(request, transportType, employee, (long) sum, OldReserve.Action.RESERVE);
    }
    
    @Override
    public void spend(Request request, Integer sumSpent, boolean isCoop, boolean isDriver, TransportTypeEnum transportType, Integer moneySaved) {
        var action = OldReserve.Action.SPEND;
        limitReservation(request, transportType, request.getAuthor(), sumSpent, action);
    }
    
    @Override
    public void cancel(Request request) {
        var action = OldReserve.Action.CANCEL;
        var result = limitReservation(request, request.getTransportType(), request.getAuthor(), 0, action);
        if (result.getLimitReservationStatus() == LimitReservationStatus.LIMIT_NOT_FOUND) {
            log.info("ReservationService: cancel: Limit for request {} not found", request.getId());
            return;
        }
        log.info(String.format("ReservationService: cancel: Cancel result of limit reservation for request with id %s was: %s",
                               request.getId(), result.getLimitReservationStatus().name()));
        if (result.getLimitReservationStatus() != LimitReservationStatus.LIMIT_CANCELLED) {
            log.error("PROBLEM CANCELLING LIMIT for request {}", request.getId());
        }
        log.info("ReservationService: cancel: successfully cancelled limit for request {}", request.getId());
    }
    
    private LimitReservationResultDto limitReservation(
            Request request, TransportTypeEnum transportType, Employee employee, long sum, OldReserve.Action action
                                                      ) {
        log.info("Limit reservation for request with id {} for sum {} and action {}", request.getId(), sum, action);
        var resultDefault = new LimitReservationResultDto();
        resultDefault.setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_DEPARTMENT);
        try {
            var departmentId = employee.getDepartment().getId();
            var organizationId = departmentService.get(departmentId).map(Department::getOrganization)
                                                  .map(Organization::getId)
                                                  .orElseThrow(() -> new EntityNotFoundException(
                                                          "Organization not found for departmentId " + departmentId));
            
            var limitReservationRequest = OldReserve.LimitReservationRequest
                    .newBuilder()
                    .setOrganizationId(organizationId.toString())
                    .setDepartmentId(departmentId.toString())
                    .setEmployeeId(employee.getId().toString())
                    .setTransportType(transportType.getName())
                    .setSum(sum)
                    .setPlannedDate(convertLocalDateTimeToGoogleTimestamp(request.getDesiredDate()))
                    .setCheckLimit(false)
                    .setRequestId(request.getId().toString())
                    .setAction(action)
                    .build();
            
            LimitReservationResultDto result = null;
            
            var response = stub.limitReservation(limitReservationRequest);
            
            // При тестировании response == null
            if (response != null) {
                result = LimitReservationResultDto.builder()
                                                  .tripRequestId(UUID.fromString(response.getTripRequestId()))
                                                  .message(response.getMessage().getData())
                                                  .limitId(convertStringToUUID(response.getLimitId().getData()))
                                                  .limitReservationStatus(Enum.valueOf(LimitReservationStatus.class,
                                                                                       response.getLimitReservationStatus()))
                                                  .build();
            }
            
            if (result != null) {
                if (result.getLimitReservationStatus() == LimitReservationStatus.LIMIT_NOT_FOUND) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "LIMIT_NOT_FOUND: " + result.getMessage());
                }
                if (result.getLimitReservationStatus() == LimitReservationStatus.LIMIT_NOT_SUFFICIENT) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                                                      "LIMIT_NOT_SUFFICIENT: " + result.getMessage());
                }
                log.debug(String.format("Reservation result of limit reserve for request  id %s was: %s",
                                        request.getId().toString(), result));
                return result;
            }
            
        } catch (TimeoutException e) {
            log.error(e.getMessage());
            throw e;
        }
        return resultDefault;
    }
    
    private UUID convertStringToUUID(String str) {
        UUID result = null;
        try {
            result = UUID.fromString(str);
        } catch (IllegalArgumentException ignored) {
        }
        return result;
    }
    
    private com.google.protobuf.Timestamp convertLocalDateTimeToGoogleTimestamp(LocalDateTime localDateTime) {
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
        
        return com.google.protobuf.Timestamp.newBuilder()
                                            .setSeconds(instant.getEpochSecond())
                                            .setNanos(instant.getNano())
                                            .build();
    }
}

