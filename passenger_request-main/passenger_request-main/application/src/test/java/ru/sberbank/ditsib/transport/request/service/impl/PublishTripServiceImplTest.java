package ru.sberbank.ditsib.transport.request.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;
import ru.sber.transport.srm.model.SrmRequestKpiDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
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
import ru.sberbank.ditsib.transport.request.service.SrmGrpcClient;
import ru.sberbank.ditsib.transport.request.service.TaxiTariffService;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка передачи контактов при интеграции")
@ExtendWith(MockitoExtension.class)
class PublishTripServiceImplTest {
    @InjectMocks
    private PublishTripServiceImpl publishTripService;
    @Mock
    private CoopTaxiTripRepository coopTaxiTripRepository;
    @Mock
    private SingleTaxiTripRepository singleTaxiTripRepository;
    @Mock
    private RequestForTaxiRepository requestForTaxiRepository;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private ContractorMessageSender contractorMessageSender;
    @Mock
    private TaxiTariffService taxiTariffService;
    @Mock
    private GroupTransferTariffRepository groupTransferTariffRepository;
    @Mock
    private RequestForGroupTransferRepository requestForGroupTransferRepository;
    @Mock
    private GroupTransferTripRepository groupTransferTripRepository;
    @Mock
    private ContractorService contractorService;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private SrmGrpcClient srmGrpcClient;
    @Mock
    private TransactionTemplate transactionTemplate;
    @Mock
    private OutContractorTaxiTripMessageMapper outContractorTaxiTripMessageMapper;

    
    @Test
    void publishNewSingleTrip() {
        var organizationId = UUID.randomUUID();
        var employee = Instancio.create(Employee.class);
        var waypoints = Instancio.ofList(Waypoint.class)
                .size(2)
                .create();
        var requestForTaxi = RequestForTaxi.builder()
                                           .organizationId(organizationId)
                                           .passenger(employee)
                                           .waypoints(waypoints)
                                           .expected(ExpectedData.builder()
                                                   .cost(10000.0)
                                                   .distance(1000.0)
                                                   .time(Duration.ofSeconds(1000000))
                                                   .build())
                                           .build();
        var singleTaxiTrip = SingleTaxiTrip.builder()
                                                      .id(UUID.randomUUID())
                                                      .organizationId(organizationId)
                                                      .build();
        doReturn(Optional.empty()).when(singleTaxiTripRepository).findByHumanReadableId(any());
        doReturn(singleTaxiTrip).when(singleTaxiTripRepository).save(any());

        when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0).doInTransaction(null));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());

        var taxiTrip = publishTripService.publishNewSingleTrip(requestForTaxi);
        
        assertEquals(taxiTrip.getId(), singleTaxiTrip.getId());
    }

    @Test
    void publishNewGroupTransferTrip() {
        var request = Instancio.create(RequestForGroupTransfer.class);
        var trip = Instancio.create(GroupTransferTrip.class);
        var contractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getId), request.getContractorId())
                .create();
        var organization = Instancio.of(Organization.class)
                .set(field(Organization::getId), request.getOrganizationId())
                .create();
        var joinedPassengers = Instancio.createMap(UUID.class, Employee.class);
        var message = Instancio.create(OutContractorTaxiTripMessage.class);
        var tariff = Instancio.create(GroupTransferTariff.class);
        doReturn(Optional.of(trip)).when(groupTransferTripRepository).findFirstByHumanReadableId("TT-" + request.getHumanReadableId());
        doReturn(contractor).when(contractorService).get(request.getContractorId());
        doReturn(Optional.of(organization)).when(organizationService).get(request.getOrganizationId());
        doReturn(joinedPassengers).when(employeeService).getByEmployeeIds(request.getJoinedPassengerIds());
        doReturn(Optional.of(tariff)).when(groupTransferTariffRepository).findById(request.getOutcomeTariffId());
        doReturn(message).when(outContractorTaxiTripMessageMapper).transformGroupTransferTripToContractorMessage(
                trip,
                request,
                tariff,
                contractor,
                organization.getOfficialName(),
                joinedPassengers
        );
        doNothing().when(contractorMessageSender).send(message);
        doReturn(trip).when(groupTransferTripRepository).getReferenceById(trip.getId());
        assertThat(publishTripService.publishNewGroupTransferTrip(request))
                .usingRecursiveComparison()
                .isEqualTo(trip);
        verify(groupTransferTripRepository).findFirstByHumanReadableId(anyString());
        verify(contractorService).get(any(UUID.class));
        verify(organizationService).get(any(UUID.class));
        verify(employeeService).getByEmployeeIds(anySet());
        verify(groupTransferTariffRepository).findById(any(UUID.class));
        verify(outContractorTaxiTripMessageMapper).transformGroupTransferTripToContractorMessage(
                any(GroupTransferTrip.class),
                any(RequestForGroupTransfer.class),
                any(GroupTransferTariff.class),
                any(Contractor.class),
                anyString(),
                anyMap()
        );
        verify(contractorMessageSender).send(any(OutContractorTaxiTripMessage.class));
        verify(groupTransferTripRepository).getReferenceById(any(UUID.class));
    }

    @Test
    void publishNewGroupTransferTripNewTrip() {
        var request = Instancio.create(RequestForGroupTransfer.class);
        var trip = Instancio.create(GroupTransferTrip.class);
        var contractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getId), request.getContractorId())
                .create();
        var organization = Instancio.of(Organization.class)
                .set(field(Organization::getId), request.getOrganizationId())
                .create();
        var joinedPassengers = Instancio.createMap(UUID.class, Employee.class);
        var message = Instancio.create(OutContractorTaxiTripMessage.class);
        var tariff = Instancio.create(GroupTransferTariff.class);
        doReturn(Optional.empty()).when(groupTransferTripRepository).findFirstByHumanReadableId("TT-" + request.getHumanReadableId());
        doReturn(trip).when(groupTransferTripRepository).save(any(GroupTransferTrip.class));
        doReturn(request).when(requestForGroupTransferRepository).save(any(RequestForGroupTransfer.class));
        doReturn(contractor).when(contractorService).get(request.getContractorId());
        doReturn(Optional.of(organization)).when(organizationService).get(request.getOrganizationId());
        doReturn(joinedPassengers).when(employeeService).getByEmployeeIds(request.getJoinedPassengerIds());
        doReturn(Optional.of(tariff)).when(groupTransferTariffRepository).findById(request.getOutcomeTariffId());
        doReturn(message).when(outContractorTaxiTripMessageMapper).transformGroupTransferTripToContractorMessage(
                trip,
                request,
                tariff,
                contractor,
                organization.getOfficialName(),
                joinedPassengers
        );
        doNothing().when(contractorMessageSender).send(message);
        doReturn(trip).when(groupTransferTripRepository).getReferenceById(trip.getId());
        when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0).doInTransaction(null));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());
        assertThat(publishTripService.publishNewGroupTransferTrip(request))
                .usingRecursiveComparison()
                .isEqualTo(trip);
        verify(groupTransferTripRepository).findFirstByHumanReadableId(anyString());
        verify(groupTransferTripRepository).save(any(GroupTransferTrip.class));
        verify(transactionTemplate).executeWithoutResult(any());
        verify(requestForGroupTransferRepository).save(any(RequestForGroupTransfer.class));
        verify(contractorService).get(any(UUID.class));
        verify(organizationService).get(any(UUID.class));
        verify(employeeService).getByEmployeeIds(anySet());
        verify(groupTransferTariffRepository).findById(any(UUID.class));
        verify(outContractorTaxiTripMessageMapper).transformGroupTransferTripToContractorMessage(
                any(GroupTransferTrip.class),
                any(RequestForGroupTransfer.class),
                any(GroupTransferTariff.class),
                any(Contractor.class),
                anyString(),
                anyMap()
        );
        verify(contractorMessageSender).send(any(OutContractorTaxiTripMessage.class));
        verify(groupTransferTripRepository).getReferenceById(any(UUID.class));
    }

    @Test
    void publishGroupTransferTrip() {
        var trip = Instancio.of(GroupTransferTrip.class)
                .set(field(GroupTransferTrip::getHumanReadableId), null)
                .create();
        var contractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getId), trip.getRequest().getContractorId())
                .create();
        var organization = Instancio.of(Organization.class)
                .set(field(Organization::getId), trip.getRequest().getOrganizationId())
                .create();
        var joinedPassengers = Instancio.createMap(UUID.class, Employee.class);
        var message = Instancio.create(OutContractorTaxiTripMessage.class);
        var tariff = Instancio.create(GroupTransferTariff.class);
        doReturn(contractor).when(contractorService).get(trip.getRequest().getContractorId());
        doReturn(Optional.of(organization)).when(organizationService).get(trip.getRequest().getOrganizationId());
        doReturn(joinedPassengers).when(employeeService).getByEmployeeIds(trip.getRequest().getJoinedPassengerIds());
        doReturn(Optional.of(tariff)).when(groupTransferTariffRepository).findById(trip.getRequest().getOutcomeTariffId());
        doReturn(message).when(outContractorTaxiTripMessageMapper).transformGroupTransferTripToContractorMessage(
                trip,
                trip.getRequest(),
                tariff,
                contractor,
                organization.getOfficialName(),
                joinedPassengers
        );
        doNothing().when(contractorMessageSender).send(message);
        publishTripService.publishGroupTransferTrip(trip);
        verify(contractorService).get(any(UUID.class));
        verify(organizationService).get(any(UUID.class));
        verify(employeeService).getByEmployeeIds(anySet());
        verify(groupTransferTariffRepository).findById(any(UUID.class));
        verify(outContractorTaxiTripMessageMapper).transformGroupTransferTripToContractorMessage(
                any(GroupTransferTrip.class),
                any(RequestForGroupTransfer.class),
                any(GroupTransferTariff.class),
                any(Contractor.class),
                anyString(),
                anyMap()
        );
        verify(contractorMessageSender).send(any(OutContractorTaxiTripMessage.class));
    }

    @Test
    void publishNewCoopTrip() {
        var rideId = UUID.randomUUID();
        var tariffId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var organization = Instancio.of(Organization.class)
                .set(field(Organization::getId), organizationId)
                .create();
        var department = Instancio.of(Department.class)
                .set(field(Department::getOrganization), organization)
                .create();
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), department)
                .create();
        var waypoints = Instancio.ofList(Waypoint.class)
                .size(2)
                .create();
        var trip = Instancio.of(CoopTaxiTrip.class)
                .set(field(CoopTaxiTrip::getOrganizationId), organizationId)
                .set(field(CoopTaxiTrip::getRideId), rideId)
                .set(field(CoopTaxiTrip::getStatus), InboundTaxiTripStatus.WAITING_FOR_ASSIGNMENT)
                .create();
        var contractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getId), organizationId)
                .create();
        var tariff = Instancio.of(TaxiTariff.class)
                .set(field(TaxiTariff::getId), trip.getOutcomeTariffId())
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getOrganizationId), organizationId)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getPassenger), employee)
                .set(field(RequestForTaxi::getTaxiTrip), trip)
                .set(field(RequestForTaxi::getContractorId), contractor.getId())
                .set(field(RequestForTaxi::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getCost), 10000.0)
                        .set(field(ExpectedData::getDistance), 1000.0)
                        .set(field(ExpectedData::getTime), Duration.ofSeconds(1000000))
                        .create())
                .create();
        var activeRequests = Collections.singletonList(request);
        var joinedPassengers = Instancio.createMap(UUID.class, Employee.class);
        var message = Instancio.create(OutContractorTaxiTripMessage.class);
        var srmSharedRideDTO = Instancio.of(SrmSharedRideDTO.class)
                .set(field(SrmSharedRideDTO::getRequestKpiList), Instancio.ofList(SrmRequestKpiDTO.class)
                        .size(1)
                        .set(field(SrmRequestKpiDTO::getId), request.getId())
                        .create())
                .create();
        doReturn(Optional.of(trip)).when(coopTaxiTripRepository).findByRideId(rideId);
        doReturn(request).when(requestForTaxiRepository).save(any(RequestForTaxi.class));
        doReturn(Optional.of(tariff)).when(taxiTariffService).getOptionalById(trip.getOutcomeTariffId());
        doReturn(contractor).when(contractorService).get(contractor.getId());
        doReturn(Optional.of(organization)).when(organizationService).get(organizationId);
        doReturn(joinedPassengers).when(employeeService).getByEmployeeIds(request.getJoinedPassengerIds());
        doReturn(srmSharedRideDTO).when(srmGrpcClient).getSharedRideGrpc(rideId);
        when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0).doInTransaction(null));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());
        doReturn(message).when(outContractorTaxiTripMessageMapper).transformCoopTaxiTripToContractorMessage(
                trip,
                request,
                activeRequests,
                tariff,
                contractor,
                organization.getOfficialName(),
                joinedPassengers,
                srmSharedRideDTO
        );
        doNothing().when(contractorMessageSender).send(message);
        var actual = publishTripService.publishNewCoopTrip(rideId, tariffId, UUID.randomUUID(), activeRequests);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(trip);
        assertThat(publishTripService.publishNewCoopTrip(rideId, tariffId, UUID.randomUUID(), Collections.emptyList()))
                .isNull();
        verify(coopTaxiTripRepository).findByRideId(any(UUID.class));
        verify(departmentService, never()).get(any(UUID.class));
        verify(transactionTemplate).executeWithoutResult(any());
        verify(taxiTariffService).getOptionalById(any(UUID.class));
        verify(contractorService).get(any(UUID.class));
        verify(organizationService).get(any(UUID.class));
        verify(employeeService).getByEmployeeIds(anySet());
        verify(outContractorTaxiTripMessageMapper).transformCoopTaxiTripToContractorMessage(
                any(CoopTaxiTrip.class),
                any(RequestForTaxi.class),
                anyList(),
                any(TaxiTariff.class),
                any(Contractor.class),
                anyString(),
                anyMap(),
                any(SrmSharedRideDTO.class)
        );
        verify(srmGrpcClient).getSharedRideGrpc(any(UUID.class));
        verify(contractorMessageSender).send(any(OutContractorTaxiTripMessage.class));
    }


    @Test
    void publishNewCoopTripNew() {
        var rideId = UUID.randomUUID();
        var tariffId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var organization = Instancio.of(Organization.class)
                .set(field(Organization::getId), organizationId)
                .create();
        var department = Instancio.of(Department.class)
                .set(field(Department::getOrganization), organization)
                .create();
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), department)
                .create();
        var waypoints = Instancio.ofList(Waypoint.class)
                .size(2)
                .create();
        var trip = Instancio.of(CoopTaxiTrip.class)
                .set(field(CoopTaxiTrip::getOrganizationId), organizationId)
                .set(field(CoopTaxiTrip::getRideId), rideId)
                .set(field(CoopTaxiTrip::getStatus), InboundTaxiTripStatus.WAITING_FOR_ASSIGNMENT)
                .create();
        var contractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getId), organizationId)
                .create();
        var tariff = Instancio.of(TaxiTariff.class)
                .set(field(TaxiTariff::getId), trip.getOutcomeTariffId())
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getOrganizationId), organizationId)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getPassenger), employee)
                .set(field(RequestForTaxi::getTaxiTrip), trip)
                .set(field(RequestForTaxi::getContractorId), contractor.getId())
                .set(field(RequestForTaxi::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getCost), 10000.0)
                        .set(field(ExpectedData::getDistance), 1000.0)
                        .set(field(ExpectedData::getTime), Duration.ofSeconds(1000000))
                        .create())
                .create();
        var activeRequests = Collections.singletonList(request);
        var joinedPassengers = Instancio.createMap(UUID.class, Employee.class);
        var message = Instancio.create(OutContractorTaxiTripMessage.class);
        var srmSharedRideDTO = Instancio.of(SrmSharedRideDTO.class)
                .set(field(SrmSharedRideDTO::getRequestKpiList), Instancio.ofList(SrmRequestKpiDTO.class)
                        .size(1)
                        .set(field(SrmRequestKpiDTO::getId), request.getId())
                        .create())
                .create();
        doReturn(Optional.empty()).when(coopTaxiTripRepository).findByRideId(rideId);
        doReturn(Optional.of(department)).when(departmentService).get(request.getPassenger().getDepartment().getId());
        doReturn(trip).when(coopTaxiTripRepository).save(any(CoopTaxiTrip.class));
        doReturn(request).when(requestForTaxiRepository).save(any(RequestForTaxi.class));
        doReturn(Optional.of(tariff)).when(taxiTariffService).getOptionalById(trip.getOutcomeTariffId());
        doReturn(contractor).when(contractorService).get(contractor.getId());
        doReturn(Optional.of(organization)).when(organizationService).get(organizationId);
        doReturn(joinedPassengers).when(employeeService).getByEmployeeIds(request.getJoinedPassengerIds());
        doReturn(srmSharedRideDTO).when(srmGrpcClient).getSharedRideGrpc(rideId);
        when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0).doInTransaction(null));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());
        doReturn(message).when(outContractorTaxiTripMessageMapper).transformCoopTaxiTripToContractorMessage(
                trip,
                request,
                activeRequests,
                tariff,
                contractor,
                organization.getOfficialName(),
                joinedPassengers,
                srmSharedRideDTO
        );
        doNothing().when(contractorMessageSender).send(message);
        var actual = publishTripService.publishNewCoopTrip(rideId, tariffId, UUID.randomUUID(), activeRequests);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(trip);
        assertThat(publishTripService.publishNewCoopTrip(rideId, tariffId, UUID.randomUUID(), Collections.emptyList()))
                .isNull();
        verify(coopTaxiTripRepository).findByRideId(any(UUID.class));
        verify(coopTaxiTripRepository).save(any(CoopTaxiTrip.class));
        verify(departmentService).get(any(UUID.class));
        verify(transactionTemplate).executeWithoutResult(any());
        verify(taxiTariffService).getOptionalById(any(UUID.class));
        verify(contractorService).get(any(UUID.class));
        verify(organizationService).get(any(UUID.class));
        verify(employeeService).getByEmployeeIds(anySet());
        verify(outContractorTaxiTripMessageMapper).transformCoopTaxiTripToContractorMessage(
                any(CoopTaxiTrip.class),
                any(RequestForTaxi.class),
                anyList(),
                any(TaxiTariff.class),
                any(Contractor.class),
                anyString(),
                anyMap(),
                any(SrmSharedRideDTO.class)
        );
        verify(srmGrpcClient).getSharedRideGrpc(any(UUID.class));
        verify(contractorMessageSender).send(any(OutContractorTaxiTripMessage.class));
    }

    @Test
    void publishNewCoopTripRejected() {
        var rideId = UUID.randomUUID();
        var tariffId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var organization = Instancio.of(Organization.class)
                .set(field(Organization::getId), organizationId)
                .create();
        var department = Instancio.of(Department.class)
                .set(field(Department::getOrganization), organization)
                .create();
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), department)
                .create();
        var waypoints = Instancio.ofList(Waypoint.class)
                .size(2)
                .create();
        var trip = Instancio.of(CoopTaxiTrip.class)
                .set(field(CoopTaxiTrip::getOrganizationId), organizationId)
                .set(field(CoopTaxiTrip::getRideId), rideId)
                .set(field(CoopTaxiTrip::getStatus), InboundTaxiTripStatus.ORDER_CANCELLED_BY_CLIENT)
                .create();
        var contractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getId), organizationId)
                .create();
        var tariff = Instancio.of(TaxiTariff.class)
                .set(field(TaxiTariff::getId), trip.getOutcomeTariffId())
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getOrganizationId), organizationId)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getPassenger), employee)
                .set(field(RequestForTaxi::getTaxiTrip), trip)
                .set(field(RequestForTaxi::getContractorId), contractor.getId())
                .set(field(RequestForTaxi::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getCost), 10000.0)
                        .set(field(ExpectedData::getDistance), 1000.0)
                        .set(field(ExpectedData::getTime), Duration.ofSeconds(1000000))
                        .create())
                .create();
        var activeRequests = Collections.singletonList(request);
        var joinedPassengers = Instancio.createMap(UUID.class, Employee.class);
        var message = Instancio.create(OutContractorTaxiTripMessage.class);
        doReturn(Optional.of(trip)).when(coopTaxiTripRepository).findByRideId(rideId);
        doReturn(request).when(requestForTaxiRepository).save(any(RequestForTaxi.class));
        doReturn(Optional.of(tariff)).when(taxiTariffService).getOptionalById(trip.getOutcomeTariffId());
        doReturn(contractor).when(contractorService).get(contractor.getId());
        doReturn(Optional.of(organization)).when(organizationService).get(organizationId);
        doReturn(joinedPassengers).when(employeeService).getByEmployeeIds(request.getJoinedPassengerIds());
        when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0).doInTransaction(null));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());
        doReturn(message).when(outContractorTaxiTripMessageMapper).transformRejectedCoopTaxiTripToContractorMessage(
                trip,
                request,
                activeRequests,
                tariff,
                contractor,
                organization.getOfficialName(),
                joinedPassengers
        );
        doNothing().when(contractorMessageSender).send(message);
        var actual = publishTripService.publishNewCoopTrip(rideId, tariffId, UUID.randomUUID(), activeRequests);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(trip);
        assertThat(publishTripService.publishNewCoopTrip(rideId, tariffId, UUID.randomUUID(), Collections.emptyList()))
                .isNull();
        verify(coopTaxiTripRepository).findByRideId(any(UUID.class));
        verify(departmentService, never()).get(any(UUID.class));
        verify(transactionTemplate).executeWithoutResult(any());
        verify(taxiTariffService).getOptionalById(any(UUID.class));
        verify(contractorService).get(any(UUID.class));
        verify(organizationService).get(any(UUID.class));
        verify(employeeService).getByEmployeeIds(anySet());
        verify(outContractorTaxiTripMessageMapper).transformRejectedCoopTaxiTripToContractorMessage(
                any(CoopTaxiTrip.class),
                any(RequestForTaxi.class),
                anyList(),
                any(TaxiTariff.class),
                any(Contractor.class),
                anyString(),
                anyMap()
        );
        verifyNoInteractions(srmGrpcClient);
        verify(contractorMessageSender).send(any(OutContractorTaxiTripMessage.class));
    }

    @Test
    void publishNewCoopTripRejectedNoPassengers() {
        var rideId = UUID.randomUUID();
        var tariffId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var organization = Instancio.of(Organization.class)
                .set(field(Organization::getId), organizationId)
                .create();
        var waypoints = Instancio.ofList(Waypoint.class)
                .size(2)
                .create();
        var trip = Instancio.of(CoopTaxiTrip.class)
                .set(field(CoopTaxiTrip::getOrganizationId), organizationId)
                .set(field(CoopTaxiTrip::getRideId), rideId)
                .set(field(CoopTaxiTrip::getStatus), InboundTaxiTripStatus.WAITING_FOR_ASSIGNMENT)
                .create();
        var contractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getId), organizationId)
                .create();
        var tariff = Instancio.of(TaxiTariff.class)
                .set(field(TaxiTariff::getId), trip.getOutcomeTariffId())
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getOrganizationId), organizationId)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getPassenger), null)
                .set(field(RequestForTaxi::getTaxiTrip), trip)
                .set(field(RequestForTaxi::getContractorId), contractor.getId())
                .set(field(RequestForTaxi::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getCost), 10000.0)
                        .set(field(ExpectedData::getDistance), 1000.0)
                        .set(field(ExpectedData::getTime), Duration.ofSeconds(1000000))
                        .create())
                .create();
        var activeRequests = Collections.singletonList(request);
        var joinedPassengers = Instancio.createMap(UUID.class, Employee.class);
        var message = Instancio.create(OutContractorTaxiTripMessage.class);
        doReturn(Optional.of(trip)).when(coopTaxiTripRepository).findByRideId(rideId);
        doReturn(request).when(requestForTaxiRepository).save(any(RequestForTaxi.class));
        doReturn(Optional.of(tariff)).when(taxiTariffService).getOptionalById(trip.getOutcomeTariffId());
        doReturn(contractor).when(contractorService).get(contractor.getId());
        doReturn(Optional.of(organization)).when(organizationService).get(organizationId);
        doReturn(joinedPassengers).when(employeeService).getByEmployeeIds(request.getJoinedPassengerIds());
        when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0).doInTransaction(null));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());
        doReturn(message).when(outContractorTaxiTripMessageMapper).transformRejectedCoopTaxiTripToContractorMessage(
                trip,
                request,
                activeRequests,
                tariff,
                contractor,
                organization.getOfficialName(),
                joinedPassengers
        );
        doNothing().when(contractorMessageSender).send(message);
        var actual = publishTripService.publishNewCoopTrip(rideId, tariffId, UUID.randomUUID(), activeRequests);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(trip);
        assertThat(publishTripService.publishNewCoopTrip(rideId, tariffId, UUID.randomUUID(), Collections.emptyList()))
                .isNull();
        verify(coopTaxiTripRepository).findByRideId(any(UUID.class));
        verify(departmentService, never()).get(any(UUID.class));
        verify(transactionTemplate).executeWithoutResult(any());
        verify(taxiTariffService).getOptionalById(any(UUID.class));
        verify(contractorService).get(any(UUID.class));
        verify(organizationService).get(any(UUID.class));
        verify(employeeService).getByEmployeeIds(anySet());
        verify(outContractorTaxiTripMessageMapper).transformRejectedCoopTaxiTripToContractorMessage(
                any(CoopTaxiTrip.class),
                any(RequestForTaxi.class),
                anyList(),
                any(TaxiTariff.class),
                any(Contractor.class),
                anyString(),
                anyMap()
        );
        verifyNoInteractions(srmGrpcClient);
        verify(contractorMessageSender).send(any(OutContractorTaxiTripMessage.class));
    }
    
    @Test
    void publishSingleTrip() {
        var organizationId = UUID.randomUUID();
        var employee = Instancio.create(Employee.class);
        var contractor = Instancio.create(Contractor.class);
        var waypoints = Instancio.ofList(Waypoint.class)
                .size(2)
                .create();
        var requestForTaxi = RequestForTaxi.builder()
                                           .organizationId(organizationId)
                                           .passenger(employee)
                                           .waypoints(waypoints)
                                           .contractorId(contractor.getId())
                                           .expected(ExpectedData.builder()
                                                   .cost(10000.0)
                                                   .distance(1000.0)
                                                   .time(Duration.ofSeconds(1000000))
                                                   .build())
                                           .build();
        
        var requests = Collections.singletonList(requestForTaxi);
        var singleTaxiTrip = SingleTaxiTrip.builder()
                                                      .id(UUID.randomUUID())
                                                      .organizationId(organizationId)
                                                      .requests(requests)
                                                      .build();
        
        var taxiTariff = Instancio.create(TaxiTariff.class);

        doReturn(contractor).when(contractorService).get(any(UUID.class));
        doReturn(singleTaxiTrip).when(singleTaxiTripRepository).save(any());
        when(transactionTemplate.execute(any())).thenAnswer(
                invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0).doInTransaction(null));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());
        
        assertAll(() -> publishTripService.publishSingleTrip(singleTaxiTrip, requestForTaxi, taxiTariff));
    }
    
    @Test
    void publishCoopTrip() {
        var rideId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var organization = Instancio.of(Organization.class)
                .set(field(Organization::getId), organizationId)
                .create();
        var department = Instancio.of(Department.class)
                .set(field(Department::getOrganization), organization)
                .create();
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), department)
                .create();
        var waypoints = Instancio.ofList(Waypoint.class)
                .size(2)
                .create();
        var trip = Instancio.of(CoopTaxiTrip.class)
                .set(field(CoopTaxiTrip::getOrganizationId), organizationId)
                .set(field(CoopTaxiTrip::getRideId), rideId)
                .set(field(CoopTaxiTrip::getStatus), InboundTaxiTripStatus.WAITING_FOR_ASSIGNMENT)
                .create();
        var contractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getId), organizationId)
                .create();
        var tariff = Instancio.of(TaxiTariff.class)
                .set(field(TaxiTariff::getId), trip.getOutcomeTariffId())
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getOrganizationId), organizationId)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getPassenger), employee)
                .set(field(RequestForTaxi::getTaxiTrip), trip)
                .set(field(RequestForTaxi::getContractorId), contractor.getId())
                .set(field(RequestForTaxi::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getCost), 10000.0)
                        .set(field(ExpectedData::getDistance), 1000.0)
                        .set(field(ExpectedData::getTime), Duration.ofSeconds(1000000))
                        .create())
                .create();
        var activeRequests = Collections.singletonList(request);
        var joinedPassengers = Instancio.createMap(UUID.class, Employee.class);
        var message = Instancio.create(OutContractorTaxiTripMessage.class);
        var srmSharedRideDTO = Instancio.of(SrmSharedRideDTO.class)
                .set(field(SrmSharedRideDTO::getRequestKpiList), Instancio.ofList(SrmRequestKpiDTO.class)
                        .size(1)
                        .set(field(SrmRequestKpiDTO::getId), request.getId())
                        .create())
                .create();
        doReturn(contractor).when(contractorService).get(contractor.getId());
        doReturn(Optional.of(organization)).when(organizationService).get(organizationId);
        doReturn(joinedPassengers).when(employeeService).getByEmployeeIds(request.getJoinedPassengerIds());
        doReturn(srmSharedRideDTO).when(srmGrpcClient).getSharedRideGrpc(rideId);
        doReturn(message).when(outContractorTaxiTripMessageMapper).transformCoopTaxiTripToContractorMessage(
                trip,
                request,
                activeRequests,
                tariff,
                contractor,
                organization.getOfficialName(),
                joinedPassengers,
                srmSharedRideDTO
        );
        doNothing().when(contractorMessageSender).send(message);
        publishTripService.publishCoopTrip(trip, activeRequests, tariff);
        verify(coopTaxiTripRepository,never()).findByRideId(any(UUID.class));
        verify(departmentService, never()).get(any(UUID.class));
        verify(transactionTemplate, never()).executeWithoutResult(any());
        verify(taxiTariffService, never()).getOptionalById(any(UUID.class));
        verify(contractorService).get(any(UUID.class));
        verify(organizationService).get(any(UUID.class));
        verify(employeeService).getByEmployeeIds(anySet());
        verify(outContractorTaxiTripMessageMapper).transformCoopTaxiTripToContractorMessage(
                any(CoopTaxiTrip.class),
                any(RequestForTaxi.class),
                anyList(),
                any(TaxiTariff.class),
                any(Contractor.class),
                anyString(),
                anyMap(),
                any(SrmSharedRideDTO.class)
        );
        verify(srmGrpcClient).getSharedRideGrpc(any(UUID.class));
        verify(contractorMessageSender).send(any(OutContractorTaxiTripMessage.class));
    }

    @Test
    void publishCoopTripRejected() {
        var rideId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var organization = Instancio.of(Organization.class)
                .set(field(Organization::getId), organizationId)
                .create();
        var department = Instancio.of(Department.class)
                .set(field(Department::getOrganization), organization)
                .create();
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), department)
                .create();
        var waypoints = Instancio.ofList(Waypoint.class)
                .size(2)
                .create();
        var trip = Instancio.of(CoopTaxiTrip.class)
                .set(field(CoopTaxiTrip::getOrganizationId), organizationId)
                .set(field(CoopTaxiTrip::getRideId), rideId)
                .set(field(CoopTaxiTrip::getStatus), InboundTaxiTripStatus.ORDER_CANCELLED_BY_CLIENT)
                .create();
        var contractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getId), organizationId)
                .create();
        var tariff = Instancio.of(TaxiTariff.class)
                .set(field(TaxiTariff::getId), trip.getOutcomeTariffId())
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getOrganizationId), organizationId)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getPassenger), employee)
                .set(field(RequestForTaxi::getTaxiTrip), trip)
                .set(field(RequestForTaxi::getContractorId), contractor.getId())
                .set(field(RequestForTaxi::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getCost), 10000.0)
                        .set(field(ExpectedData::getDistance), 1000.0)
                        .set(field(ExpectedData::getTime), Duration.ofSeconds(1000000))
                        .create())
                .create();
        var activeRequests = Collections.singletonList(request);
        var joinedPassengers = Instancio.createMap(UUID.class, Employee.class);
        var message = Instancio.create(OutContractorTaxiTripMessage.class);
        doReturn(contractor).when(contractorService).get(contractor.getId());
        doReturn(Optional.of(organization)).when(organizationService).get(organizationId);
        doReturn(joinedPassengers).when(employeeService).getByEmployeeIds(request.getJoinedPassengerIds());
        doReturn(message).when(outContractorTaxiTripMessageMapper).transformRejectedCoopTaxiTripToContractorMessage(
                trip,
                request,
                activeRequests,
                tariff,
                contractor,
                organization.getOfficialName(),
                joinedPassengers
        );
        doNothing().when(contractorMessageSender).send(message);
        publishTripService.publishCoopTrip(trip, activeRequests, tariff);
        verify(coopTaxiTripRepository,never()).findByRideId(any(UUID.class));
        verify(departmentService, never()).get(any(UUID.class));
        verify(transactionTemplate, never()).executeWithoutResult(any());
        verify(taxiTariffService, never()).getOptionalById(any(UUID.class));
        verify(contractorService).get(any(UUID.class));
        verify(organizationService).get(any(UUID.class));
        verify(employeeService).getByEmployeeIds(anySet());
        verify(outContractorTaxiTripMessageMapper).transformRejectedCoopTaxiTripToContractorMessage(
                any(CoopTaxiTrip.class),
                any(RequestForTaxi.class),
                anyList(),
                any(TaxiTariff.class),
                any(Contractor.class),
                anyString(),
                anyMap()
        );
        verify(srmGrpcClient, never()).getSharedRideGrpc(any(UUID.class));
        verify(contractorMessageSender).send(any(OutContractorTaxiTripMessage.class));
    }

    @Test
    void publishCoopTripRejectedNoPassengers() {
        var rideId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var organization = Instancio.of(Organization.class)
                .set(field(Organization::getId), organizationId)
                .create();
        var waypoints = Instancio.ofList(Waypoint.class)
                .size(2)
                .create();
        var trip = Instancio.of(CoopTaxiTrip.class)
                .set(field(CoopTaxiTrip::getOrganizationId), organizationId)
                .set(field(CoopTaxiTrip::getRideId), rideId)
                .set(field(CoopTaxiTrip::getStatus), InboundTaxiTripStatus.WAITING_FOR_ASSIGNMENT)
                .create();
        var contractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getId), organizationId)
                .create();
        var tariff = Instancio.of(TaxiTariff.class)
                .set(field(TaxiTariff::getId), trip.getOutcomeTariffId())
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getOrganizationId), organizationId)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getPassenger), null)
                .set(field(RequestForTaxi::getTaxiTrip), trip)
                .set(field(RequestForTaxi::getContractorId), contractor.getId())
                .set(field(RequestForTaxi::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getCost), 10000.0)
                        .set(field(ExpectedData::getDistance), 1000.0)
                        .set(field(ExpectedData::getTime), Duration.ofSeconds(1000000))
                        .create())
                .create();
        var activeRequests = Collections.singletonList(request);
        var joinedPassengers = Instancio.createMap(UUID.class, Employee.class);
        var message = Instancio.create(OutContractorTaxiTripMessage.class);
        doReturn(contractor).when(contractorService).get(contractor.getId());
        doReturn(Optional.of(organization)).when(organizationService).get(organizationId);
        doReturn(joinedPassengers).when(employeeService).getByEmployeeIds(request.getJoinedPassengerIds());
        doReturn(message).when(outContractorTaxiTripMessageMapper).transformRejectedCoopTaxiTripToContractorMessage(
                trip,
                request,
                activeRequests,
                tariff,
                contractor,
                organization.getOfficialName(),
                joinedPassengers
        );
        doNothing().when(contractorMessageSender).send(message);
        publishTripService.publishCoopTrip(trip, activeRequests, tariff);
        verify(coopTaxiTripRepository,never()).findByRideId(any(UUID.class));
        verify(departmentService, never()).get(any(UUID.class));
        verify(transactionTemplate, never()).executeWithoutResult(any());
        verify(taxiTariffService, never()).getOptionalById(any(UUID.class));
        verify(contractorService).get(any(UUID.class));
        verify(organizationService).get(any(UUID.class));
        verify(employeeService).getByEmployeeIds(anySet());
        verify(outContractorTaxiTripMessageMapper).transformRejectedCoopTaxiTripToContractorMessage(
                any(CoopTaxiTrip.class),
                any(RequestForTaxi.class),
                anyList(),
                any(TaxiTariff.class),
                any(Contractor.class),
                anyString(),
                anyMap()
        );
        verify(srmGrpcClient, never()).getSharedRideGrpc(any(UUID.class));
        verify(contractorMessageSender).send(any(OutContractorTaxiTripMessage.class));
    }

    @Test
    void publishCoopTripNoRequests() {
        var trip = Instancio.create(CoopTaxiTrip.class);
        var tariff = Instancio.create(TaxiTariff.class);
        publishTripService.publishCoopTrip(trip, Collections.emptyList(), tariff);
        verify(coopTaxiTripRepository,never()).findByRideId(any(UUID.class));
        verify(departmentService, never()).get(any(UUID.class));
        verify(transactionTemplate, never()).executeWithoutResult(any());
        verify(taxiTariffService, never()).getOptionalById(any(UUID.class));
        verify(contractorService, never()).get(any(UUID.class));
        verify(organizationService, never()).get(any(UUID.class));
        verify(employeeService, never()).getByEmployeeIds(anySet());
        verify(outContractorTaxiTripMessageMapper, never()).transformCoopTaxiTripToContractorMessage(
                any(CoopTaxiTrip.class),
                any(RequestForTaxi.class),
                anyList(),
                any(TaxiTariff.class),
                any(Contractor.class),
                anyString(),
                anyMap(),
                any(SrmSharedRideDTO.class)
        );
        verify(srmGrpcClient, never()).getSharedRideGrpc(any(UUID.class));
        verify(contractorMessageSender, never()).send(any(OutContractorTaxiTripMessage.class));
    }


    @Test
    void publishCoopTripSharedRideDtoIsNull() {
        publishCoopTripNotSent(null);
    }

    @Test
    void publishCoopTripSharedRideDtoWaypointsEmpty() {
        publishCoopTripNotSent(Instancio.of(SrmSharedRideDTO.class)
                .set(field(SrmSharedRideDTO::getWaypoints), Collections.emptyList())
                .create());
    }

    @Test
    void publishCoopTripSharedRideDtoRequestKpiListEmpty() {
        publishCoopTripNotSent(Instancio.of(SrmSharedRideDTO.class)
                .set(field(SrmSharedRideDTO::getRequestKpiList), Collections.emptyList())
                .create());
    }

    @Test
    void publishCoopTripSharedRideDtoNoRequests() {
        publishCoopTripNotSent(Instancio.create(SrmSharedRideDTO.class));
    }

    private void publishCoopTripNotSent(SrmSharedRideDTO srmSharedRideDTO) {
        var rideId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var organization = Instancio.of(Organization.class)
                .set(field(Organization::getId), organizationId)
                .create();
        var department = Instancio.of(Department.class)
                .set(field(Department::getOrganization), organization)
                .create();
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getDepartment), department)
                .create();
        var waypoints = Instancio.ofList(Waypoint.class)
                .size(2)
                .create();
        var trip = Instancio.of(CoopTaxiTrip.class)
                .set(field(CoopTaxiTrip::getOrganizationId), organizationId)
                .set(field(CoopTaxiTrip::getRideId), rideId)
                .set(field(CoopTaxiTrip::getStatus), InboundTaxiTripStatus.WAITING_FOR_ASSIGNMENT)
                .create();
        var contractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getId), organizationId)
                .create();
        var tariff = Instancio.of(TaxiTariff.class)
                .set(field(TaxiTariff::getId), trip.getOutcomeTariffId())
                .create();
        var request = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::getOrganizationId), organizationId)
                .set(field(RequestForTaxi::getWaypoints), waypoints)
                .set(field(RequestForTaxi::getPassenger), employee)
                .set(field(RequestForTaxi::getTaxiTrip), trip)
                .set(field(RequestForTaxi::getContractorId), contractor.getId())
                .set(field(RequestForTaxi::getExpected), Instancio.of(ExpectedData.class)
                        .set(field(ExpectedData::getCost), 10000.0)
                        .set(field(ExpectedData::getDistance), 1000.0)
                        .set(field(ExpectedData::getTime), Duration.ofSeconds(1000000))
                        .create())
                .create();
        var activeRequests = Collections.singletonList(request);
        var joinedPassengers = Instancio.createMap(UUID.class, Employee.class);
        doReturn(contractor).when(contractorService).get(contractor.getId());
        doReturn(Optional.of(organization)).when(organizationService).get(organizationId);
        doReturn(joinedPassengers).when(employeeService).getByEmployeeIds(request.getJoinedPassengerIds());
        doReturn(srmSharedRideDTO).when(srmGrpcClient).getSharedRideGrpc(rideId);
        publishTripService.publishCoopTrip(trip, activeRequests, tariff);
        verify(coopTaxiTripRepository,never()).findByRideId(any(UUID.class));
        verify(departmentService, never()).get(any(UUID.class));
        verify(transactionTemplate, never()).executeWithoutResult(any());
        verify(taxiTariffService, never()).getOptionalById(any(UUID.class));
        verify(contractorService).get(any(UUID.class));
        verify(organizationService).get(any(UUID.class));
        verify(employeeService).getByEmployeeIds(anySet());
        verify(outContractorTaxiTripMessageMapper, never()).transformCoopTaxiTripToContractorMessage(
                any(CoopTaxiTrip.class),
                any(RequestForTaxi.class),
                anyList(),
                any(TaxiTariff.class),
                any(Contractor.class),
                anyString(),
                anyMap(),
                any(SrmSharedRideDTO.class)
        );
        verify(srmGrpcClient).getSharedRideGrpc(any(UUID.class));
        verify(contractorMessageSender, never()).send(any(OutContractorTaxiTripMessage.class));
    }
}