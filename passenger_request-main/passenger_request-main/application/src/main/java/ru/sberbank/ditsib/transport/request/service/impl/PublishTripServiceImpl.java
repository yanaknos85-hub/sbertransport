package ru.sberbank.ditsib.transport.request.service.impl;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.TripType;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;
import ru.sberbank.ditsib.transport.request.mappers.OutContractorTaxiTripMessageMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.ContractorMessageSender;
import ru.sberbank.ditsib.transport.request.service.ContractorService;
import ru.sberbank.ditsib.transport.request.service.PublishTripService;
import ru.sberbank.ditsib.transport.request.service.SrmGrpcClient;
import ru.sberbank.ditsib.transport.request.service.TaxiTariffService;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;

import java.util.*;

import static ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus.ORDER_CANCELLED_BY_CLIENT;
import static ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus.SENT_TO_CONTRACTOR;

/**
 * Имплементация сервиса публикации поездок
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PublishTripServiceImpl implements PublishTripService {
    private static final String TAXI_TRIP_PREFIX = "TT-";
    public static final String NO_ACTIVE_REQUESTS = "transformToContractorMessage: No active requests found for trip {} with rideId {}";
    private final CoopTaxiTripRepository coopTaxiTripRepository;
    private final SingleTaxiTripRepository singleTaxiTripRepository;
    private final RequestForTaxiRepository requestForTaxiRepository;
    private final DepartmentService departmentService;
    private final ContractorMessageSender contractorMessageSender;
    private final TaxiTariffService taxiTariffService;
    private final GroupTransferTariffRepository groupTransferTariffRepository;
    private final RequestForGroupTransferRepository requestForGroupTransferRepository;
    private final GroupTransferTripRepository groupTransferTripRepository;
    private final ContractorService contractorService;
    private final OrganizationService organizationService;
    private final EmployeeService employeeService;
    private final SrmGrpcClient srmGrpcClient;
    private final TransactionTemplate transactionTemplate;
    private final OutContractorTaxiTripMessageMapper outContractorTaxiTripMessageMapper;

    @Override
    public SingleTaxiTrip publishNewSingleTrip(RequestForTaxi request) {
        var hri = TAXI_TRIP_PREFIX + request.getHumanReadableId();
        var singleTaxiTrip = singleTaxiTripRepository.findByHumanReadableId(hri).orElseGet(() -> createTaxiTrip(hri, request));
        var contractor = contractorService.get(request.getContractorId());
        var organizationName = organizationService.get(request.getOrganizationId())
                .map(Organization::getOfficialName).orElse(null);
        var joinedPassengers = employeeService.getByEmployeeIds(request.getJoinedPassengerIds());
        contractorMessageSender.send(outContractorTaxiTripMessageMapper.transformSingleTaxiTripToContractorMessage(
                singleTaxiTrip,
                request,
                taxiTariffService.getOptionalById(request.getOutcomeTariffId()).orElse(null),
                contractor,
                organizationName,
                joinedPassengers));
        return singleTaxiTrip;
    }

    @Override
    public GroupTransferTrip publishNewGroupTransferTrip(RequestForGroupTransfer request) {
        var hri = TAXI_TRIP_PREFIX + request.getHumanReadableId();
        var trip = groupTransferTripRepository.findFirstByHumanReadableId(hri)
                .orElseGet(() -> createTransferTrip(hri, request));
        var contractor = contractorService.get(request.getContractorId());
        var organizationName = organizationService.get(request.getOrganizationId())
                .map(Organization::getOfficialName).orElse(null);
        var joinedPassengers = employeeService.getByEmployeeIds(request.getJoinedPassengerIds());
        contractorMessageSender.send(outContractorTaxiTripMessageMapper.transformGroupTransferTripToContractorMessage(
                trip,
                request,
                groupTransferTariffRepository.findById(request.getOutcomeTariffId()).orElse(null),
                contractor,
                organizationName,
                joinedPassengers));
        return groupTransferTripRepository.getReferenceById(trip.getId());
    }

    @Override
    public CoopTaxiTrip publishNewCoopTrip(UUID rideId, UUID tariffId, UUID outcomeTariffId, @NotEmpty List<RequestForTaxi> activeRequests) {
        var firstActiveRequest = !activeRequests.isEmpty() ? activeRequests.getFirst() : null;
        if (firstActiveRequest == null) {
            log.info(NO_ACTIVE_REQUESTS, null, rideId);
            return null;
        } else {
            var coopTaxiTrip = coopTaxiTripRepository.findByRideId(rideId).orElseGet(() -> createCoopTrip(
                    rideId,
                    tariffId,
                    outcomeTariffId,
                    activeRequests,
                    firstActiveRequest));
            activeRequests.forEach(requestForTaxi -> {
                requestForTaxi.setTaxiTrip(coopTaxiTrip);
                transactionTemplate.executeWithoutResult(status -> requestForTaxiRepository.save(requestForTaxi));
            });
            log.info("publishNewCoopTrip: going to send to contractor: rideId = {}, coopTaxiTrip = {}", rideId, coopTaxiTrip.getId());
            var tariff = taxiTariffService.getOptionalById(coopTaxiTrip.getOutcomeTariffId()).orElse(null);
            var contractor = contractorService.get(firstActiveRequest.getContractorId());
            var organizationName = organizationService.get(firstActiveRequest.getOrganizationId())
                    .map(Organization::getOfficialName).orElse(null);
            var joinedPassengers = employeeService.getByEmployeeIds(firstActiveRequest.getJoinedPassengerIds());
            if (ORDER_CANCELLED_BY_CLIENT.equals(coopTaxiTrip.getStatus()) || activeRequests.stream()
                    .filter(requestForTaxi -> requestForTaxi.getPassenger() != null)
                    .findAny()
                    .isEmpty()) {
                sendCoopTripRejected(activeRequests, coopTaxiTrip, firstActiveRequest, tariff, contractor, organizationName, joinedPassengers);
            } else {
                sendCoopTrip(activeRequests, coopTaxiTrip, firstActiveRequest, tariff, contractor, organizationName, joinedPassengers);
            }
            return coopTaxiTrip;
        }
    }

    @Override
    public void publishSingleTrip(SingleTaxiTrip singleTaxiTrip, RequestForTaxi request, TaxiTariff usedTariff) {
        if (singleTaxiTrip.getRequests() != null && !singleTaxiTrip.getRequests().isEmpty()) {
            if (singleTaxiTrip.getHumanReadableId() == null) {
                singleTaxiTrip.setHumanReadableId(TAXI_TRIP_PREFIX + singleTaxiTrip.getRequests().getFirst().getHumanReadableId());
                transactionTemplate.executeWithoutResult(status -> singleTaxiTripRepository.save(singleTaxiTrip));
            }
            var contractor = contractorService.get(request.getContractorId());
            var organizationName = organizationService.get(request.getOrganizationId())
                    .map(Organization::getOfficialName).orElse(null);
            var joinedPassengers = employeeService.getByEmployeeIds(request.getJoinedPassengerIds());
            contractorMessageSender.send(outContractorTaxiTripMessageMapper.transformSingleTaxiTripToContractorMessage(
                    singleTaxiTrip,
                    request,
                    usedTariff,
                    contractor,
                    organizationName,
                    joinedPassengers));
        }
    }

    @Override
    public void publishGroupTransferTrip(GroupTransferTrip trip) {
        if (trip.getHumanReadableId() == null) {
            trip.setHumanReadableId(TAXI_TRIP_PREFIX + trip.getRequest().getHumanReadableId());
            transactionTemplate.executeWithoutResult(status -> groupTransferTripRepository.save(trip));
        }
        var tariff = groupTransferTariffRepository.findById(trip.getRequest().getOutcomeTariffId())
                .orElseThrow(() -> new EntityNotFoundException(GroupTransferTariff.class, trip.getRequest().getTariffId()));
        var contractor = contractorService.get(trip.getRequest().getContractorId());
        var organizationName = organizationService.get(trip.getRequest().getOrganizationId())
                .map(Organization::getOfficialName).orElse(null);
        var joinedPassengers = employeeService.getByEmployeeIds(trip.getRequest().getJoinedPassengerIds());
        contractorMessageSender.send(outContractorTaxiTripMessageMapper.transformGroupTransferTripToContractorMessage(
                trip,
                trip.getRequest(),
                tariff,
                contractor,
                organizationName,
                joinedPassengers));

    }

    @Override
    public void publishCoopTrip(CoopTaxiTrip coopTaxiTrip, List<RequestForTaxi> activeRequests, TaxiTariff tariff) {
        var firstActiveRequest = !activeRequests.isEmpty() ? activeRequests.getFirst() : null;
        if (firstActiveRequest == null) {
            log.info(NO_ACTIVE_REQUESTS, coopTaxiTrip.getId(), coopTaxiTrip.getRideId());
        } else {
            var contractor = contractorService.get(firstActiveRequest.getContractorId());
            var organizationName = organizationService.get(firstActiveRequest.getOrganizationId())
                    .map(Organization::getOfficialName).orElse(null);
            var joinedPassengers = employeeService.getByEmployeeIds(firstActiveRequest.getJoinedPassengerIds());
            if (ORDER_CANCELLED_BY_CLIENT.equals(coopTaxiTrip.getStatus()) || activeRequests.stream()
                    .filter(requestForTaxi -> requestForTaxi.getPassenger() != null)
                    .findAny()
                    .isEmpty()) {
                sendCoopTripRejected(activeRequests, coopTaxiTrip, firstActiveRequest, tariff, contractor, organizationName, joinedPassengers);
            } else {
                sendCoopTrip(activeRequests, coopTaxiTrip, firstActiveRequest, tariff, contractor, organizationName, joinedPassengers);
            }
        }
    }

    private void sendCoopTrip(List<RequestForTaxi> activeRequests,
                              CoopTaxiTrip coopTaxiTrip,
                              RequestForTaxi firstActiveRequest,
                              TaxiTariff tariff,
                              Contractor contractor,
                              String organizationName,
                              Map<UUID, Employee> joinedPassengers) {
        var sharedRideDTO = Optional.ofNullable(coopTaxiTrip.getRideId())
                .map(srmGrpcClient::getSharedRideGrpc)
                .orElse(null);
        if (sharedRideDTO == null) {
            log.info("transformToContractorMessage: no shared ride found for trip {} with rideId {}",
                    coopTaxiTrip.getId(),
                    coopTaxiTrip.getRideId());
        } else if (sharedRideDTO.getWaypoints().isEmpty()) {
            log.info("transformToContractorMessage: no waypoints found in shared ride for trip {} with rideId {}",
                    coopTaxiTrip.getId(),
                    coopTaxiTrip.getRideId());
        } else if (sharedRideDTO.getRequestKpiList().isEmpty()) {
            log.info("transformToContractorMessage: no requestKpi's found in shared ride for trip {} with rideId {}",
                    coopTaxiTrip.getId(),
                    coopTaxiTrip.getRideId());
        } else if (activeRequests.stream()
                .filter(e -> e.getId().equals(sharedRideDTO.getRequestKpiList().getFirst().getId()))
                .findAny().isEmpty()) {
            log.info("transformToContractorMessage: No initiator request found for trip {} with rideId {}",
                    coopTaxiTrip.getId(),
                    coopTaxiTrip.getRideId());
        } else {
            final var message = outContractorTaxiTripMessageMapper.transformCoopTaxiTripToContractorMessage(
                    coopTaxiTrip,
                    firstActiveRequest,
                    activeRequests,
                    tariff,
                    contractor,
                    organizationName,
                    joinedPassengers,
                    sharedRideDTO);
            contractorMessageSender.send(message);
        }
    }

    private void sendCoopTripRejected(List<RequestForTaxi> activeRequests,
                                      CoopTaxiTrip coopTaxiTrip,
                                      RequestForTaxi firstActiveRequest,
                                      TaxiTariff tariff,
                                      Contractor contractor,
                                      String organizationName,
                                      Map<UUID, Employee> joinedPassengers) {
        final var message = outContractorTaxiTripMessageMapper.transformRejectedCoopTaxiTripToContractorMessage(
                coopTaxiTrip,
                firstActiveRequest,
                activeRequests,
                tariff,
                contractor,
                organizationName,
                joinedPassengers);
        contractorMessageSender.send(message);
    }

    private SingleTaxiTrip createTaxiTrip(String hri, RequestForTaxi request) {
        var singleTaxiTrip = transactionTemplate.execute(status -> singleTaxiTripRepository.save(SingleTaxiTrip.builder()
                .tariffId(request.getTariffId())
                .outcomeTariffId(request.getOutcomeTariffId())
                .humanReadableId(hri)
                .tripType(TripType.SINGLE)
                .organizationId(request.getOrganizationId())
                .status(SENT_TO_CONTRACTOR)
                .requests(new ArrayList<>(Collections.singletonList(request)))
                .build()));
        transactionTemplate.executeWithoutResult(status -> {
            request.setTaxiTrip(singleTaxiTrip);
            requestForTaxiRepository.save(request);
        });
        return singleTaxiTrip;
    }

    private GroupTransferTrip createTransferTrip(String hri, RequestForGroupTransfer request) {
        var groupTransferTrip = transactionTemplate.execute(status -> groupTransferTripRepository.save(GroupTransferTrip.builder()
                .tariffId(request.getTariffId())
                .outcomeTariffId(request.getOutcomeTariffId())
                .humanReadableId(hri)
                .organizationId(request.getOrganizationId())
                .status(SENT_TO_CONTRACTOR)
                .request(request)
                .build()));
        transactionTemplate.executeWithoutResult(status -> {
            request.setTrip(groupTransferTrip);
            requestForGroupTransferRepository.save(request);
        });
        return groupTransferTrip;
    }

    private CoopTaxiTrip createCoopTrip(UUID rideId,
                                        UUID tariffId,
                                        UUID outcomeTariffId,
                                        List<RequestForTaxi> activeRequests,
                                        RequestForTaxi request) {
        var department = departmentService.get(request.getPassenger().getDepartment().getId())
                .orElseThrow(() -> new EntityNotFoundException(Department.class, request.getPassenger().getDepartment().getId()));
        return transactionTemplate.execute(status -> coopTaxiTripRepository.save(
                CoopTaxiTrip.builder()
                        .tripType(TripType.COOP)
                        .organizationId(department.getOrganization().getId())
                        .status(SENT_TO_CONTRACTOR)
                        .humanReadableId(TAXI_TRIP_PREFIX + activeRequests.getFirst().getHumanReadableId())
                        .rideId(rideId)
                        .tariffId(tariffId)
                        .outcomeTariffId(outcomeTariffId)
                        .requests(activeRequests)
                        .build()));
    }
}

