package ru.sberbank.ditsib.transport.request.service;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.exceptions.IllegalCallerResponseException;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.client.PersonalCarDataResolver;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.CancelDTO;
import ru.sberbank.ditsib.transport.request.dto.GetRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.RequestDTO;
import ru.sberbank.ditsib.transport.request.dto.RequestRatingDTO;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapperImpl;
import ru.sberbank.ditsib.transport.request.evaluators.Evaluator;
import ru.sberbank.ditsib.transport.request.evaluators.impl.EvaluatorDummyImpl;
import ru.sberbank.ditsib.transport.request.evaluators.impl.EvaluatorPersonalImpl;
import ru.sberbank.ditsib.transport.request.evaluators.impl.EvaluatorPublicImpl;
import ru.sberbank.ditsib.transport.request.evaluators.impl.EvaluatorTaxiImpl;
import ru.sberbank.ditsib.transport.request.exceptions.UserNotFoundException;
import ru.sberbank.ditsib.transport.request.mappers.EmployeeMapperImpl;
import ru.sberbank.ditsib.transport.request.mappers.FraudMapperImpl;
import ru.sberbank.ditsib.transport.request.mappers.PositionMapperImpl;
import ru.sberbank.ditsib.transport.request.mappers.VehicleMapperImpl;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestRatingSender;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestSender;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.PositionService;
import ru.sberbank.ditsib.transport.request.service.impl.RequestControllerServiceImpl;
import ru.sberbank.ditsib.transport.request.service.search.RequestSearchService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.sberbank.ditsib.transport.request.service.impl.RequestValidationServiceImpl.*;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@SpringBootTest(classes = RequestApplication.class)
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка сервиса контроллера заявок")
class RequestControllerServiceTest extends KafkaTest {

    private final RequestService requestService = mock(RequestService.class);

    private final RequestValidationService requestValidationService = mock(RequestValidationService.class);

    private final RequestSearchService requestSearchService = mock(RequestSearchService.class);

    private final EntityDTOMapper mapper = new EntityDTOMapperImpl(new EmployeeMapperImpl(), new VehicleMapperImpl(), new FraudMapperImpl());

    private final EmployeeService employeeService = mock(EmployeeService.class);

    private final PositionService positionService = mock(PositionService.class);

    private final DepartmentService departmentService = mock(DepartmentService.class);

    private final TripPurposeRepository tripPurposeRepository = mock(TripPurposeRepository.class);

    private final PersonalCarDataResolver personalCarDataResolver = mock(PersonalCarDataResolver.class);

    private final RequestSender<Request> delayedSendService = mock(RequestSender.class);

    private final RequestRepository requestRepository = mock(RequestRepository.class);

    private final RequestRatingSender requestRatingSender = mock(RequestRatingSender.class);

    private final Map<TransportTypeEnum, Evaluator> evaluators = getEvaluators(requestRepository, requestRatingSender);

    private final RequestForTaxiRepository requestForTaxiRepository = mock(RequestForTaxiRepository.class);

    private final RequestForPublicRepository requestForPublicRepository = mock(RequestForPublicRepository.class);

    private final RequestControllerService requestControllerService =
            new RequestControllerServiceImpl(requestService,
                    requestValidationService,
                    requestSearchService,
                    mapper,
                    new EmployeeMapperImpl(),
                    employeeService,
                    positionService,
                    new PositionMapperImpl(),
                    departmentService,
                    personalCarDataResolver,
                    new ArrayList<>(),
                    Map.of(TransportTypeEnum.TAXI, delayedSendService, TransportTypeEnum.PERSONAL, delayedSendService, TransportTypeEnum.PUBLIC, delayedSendService, TransportTypeEnum.CARSHARING, delayedSendService, TransportTypeEnum.GROUP_TRANSFER, delayedSendService),
                    requestForTaxiRepository,
                    requestForPublicRepository,
                    evaluators,
                    mock(FraudRepository.class),
                    new FraudMapperImpl());

    private Map<TransportTypeEnum, Evaluator> getEvaluators(RequestRepository requestRepository, RequestRatingSender requestRatingSender) {
        var dummyEvaluator = new EvaluatorDummyImpl();
        Map<TransportTypeEnum, Evaluator> evaluators = new HashMap<>();
        for (var transportType : TransportTypeEnum.values()) {
            switch (transportType) {
                case TAXI:
                    evaluators.put(transportType, new EvaluatorTaxiImpl(requestRepository, requestRatingSender));
                    break;
                case PERSONAL:
                    evaluators.put(transportType, new EvaluatorPersonalImpl(requestRepository, requestRatingSender));
                    break;
                case PUBLIC:
                    evaluators.put(transportType, new EvaluatorPublicImpl(requestRepository, requestRatingSender));
                    break;
                default:
                    evaluators.put(transportType, dummyEvaluator);
                    break;
            }
        }
        return evaluators;
    }

    @Test
    @DisplayName("Редактирование. Несуществующая заявка")
    void editRequest_NotFound_Throws() {
        var requestId = UUID.randomUUID();
        var employee = Employee.builder().id(UUID.randomUUID()).build();
        var requestDTO = RequestDTO.builder().build();
        doThrow(new EntityNotFoundException(Request.class, requestId)).when(requestValidationService).validateAndGetRequest(any());
        assertThatThrownBy(() -> requestControllerService.edit(
                requestId, requestDTO, employee, "token"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("entityName", "Request")
                .hasFieldOrPropertyWithValue("entityId", requestId);
    }

    @Test
    @DisplayName("Редактирование. Пользователь не автор и не пассажир")
    void editRequest_userNotAllowed_Throws() {
        when(departmentService.findOrCreateById(any())).thenReturn(Department.builder()
                .id(UUID.randomUUID())
                .build());
        var department = departmentService.findOrCreateById(UUID.randomUUID());
        var requestId = UUID.randomUUID();
        var request =
                RequestForPersonal.builder().transportType(TransportTypeEnum.PERSONAL)
                        .author(Employee.builder().id(UUID.randomUUID()).department(department).build())
                        .passenger(Employee.builder().id(UUID.randomUUID()).department(department).build()).build();
        var employee = Employee.builder().id(UUID.randomUUID()).build();
        var requestDTO = mapper.requestToDTO(request);
        doReturn(request).when(requestValidationService).validateAndGetRequest(any());
        doThrow(new IllegalCallerResponseException()).when(requestValidationService)
                .checkUserEditPermission(any(), any());
        assertThrows(IllegalCallerResponseException.class, () -> requestControllerService.edit(
                requestId, requestDTO, employee,
                "token"));
    }

    @Test
    @DisplayName("Редактирование. Пользователь не автор и не пассажир, но среди согласующих")
    void editRequest_userApprover() {
        when(departmentService.findOrCreateById(any())).thenReturn(Department.builder()
                .id(UUID.randomUUID())
                .build());
        var department = departmentService.findOrCreateById(UUID.randomUUID());
        var requestId = UUID.randomUUID();
        var passenger = Employee.builder().id(UUID.randomUUID()).department(department).build();
        var callerApproverWoType = Employee.builder().id(UUID.randomUUID()).build();
        var callerApproverTaxi = Employee.builder().id(UUID.randomUUID()).build();
        var callerApproverPersonal = Employee.builder().id(UUID.randomUUID()).build();
        var callerNoApprover = Employee.builder().id(UUID.randomUUID()).build();

        var approverWoType = Approver.builder().employeeId(callerApproverWoType.getId()).build();
        var approverTaxi = Approver.builder()
                .employeeId(callerApproverTaxi.getId())
                .transportType(TransportTypeEnum.TAXI)
                .build();
        var approverPersonal = Approver.builder()
                .employeeId(callerApproverPersonal.getId())
                .transportType(TransportTypeEnum.PERSONAL)
                .build();

        var request = RequestForPersonal.builder().transportType(TransportTypeEnum.PERSONAL)
                .status(TripRequestStatus.PERSONAL_AWAITING_APPROVAL)
                .author(Employee.builder().id(UUID.randomUUID()).build())
                .passenger(passenger).build();
        var requestDTO = mapper.requestToDTO(request);
        when(requestService.update(any(), any(), any())).thenReturn(request);
        doReturn(request).when(requestValidationService).validateAndGetRequest(any());
        doThrow(new IllegalCallerResponseException()).when(requestValidationService)
                .checkUserEditPermission(request, callerNoApprover);
        doThrow(new IllegalCallerResponseException()).when(requestValidationService)
                .checkUserEditPermission(request, callerApproverTaxi);
        doReturn(Arrays.asList(approverWoType, approverTaxi, approverPersonal))
                .when(departmentService).getApprovals(passenger.getDepartment().getId());

        // Caller not approver
        assertThrows(IllegalCallerResponseException.class, () -> requestControllerService.edit(
                requestId,
                requestDTO,
                callerNoApprover,
                "token"));


        // Caller is taxi approver
        assertThrows(IllegalCallerResponseException.class, () -> requestControllerService.edit(
                requestId,
                requestDTO,
                callerApproverTaxi,
                "token"));

        // Caller is personal approver
        requestControllerService.edit(requestId,
                requestDTO,
                callerApproverPersonal,
                "token");

        // Caller is approver ia approver without transport type restriction
        requestControllerService.edit(requestId,
                requestDTO,
                callerApproverWoType,
                "token");
    }

    @Test
    @DisplayName("Редактирование. Заявка в не редактируемом статусе")
    void editRequest_notEditable_Throws() {
        var requestId = UUID.randomUUID();
        var author = Employee.builder().id(UUID.randomUUID()).build();
        var passengerId = UUID.randomUUID();
        var passenger = Employee.builder().id(passengerId).build();
        var request = RequestForPublic.builder().author(author).transportType(TransportTypeEnum.PUBLIC)
                .passenger(passenger)
                .expected(ExpectedData.builder().cost(100d).build())
                .purpose(TripPurpose.builder().id(UUID.randomUUID()).build())
                .status(TripRequestStatus.PUBLIC_CANCELLED).build();
        var requestDTO = mapper.requestToDTO(request);
        when(employeeService.get(passengerId)).thenReturn(Optional.of(passenger));
        when(tripPurposeRepository.findById(UUID.randomUUID()))
                .then(invocation -> Optional.of(TripPurpose.builder().id(invocation.getArgument(0)).build()));
        doReturn(request).when(requestValidationService).validateAndGetRequest(any());
        doThrow(new IllegalStateResponseException(String.format(EDIT_REQUEST_ILLEGAL_STATUS_FORMAT, request.getStatus())))
                .when(requestValidationService)
                .checkRequestStatusByAllowedStatuses(any(), any());
        assertThrows(IllegalStateResponseException.class, () -> requestControllerService.edit(
                requestId, requestDTO, author,
                "token"));
    }

    @Test
    @DisplayName("Отмена. Несуществующая заявка")
    void test_requestNotFound_Throws() {
        var requestId = UUID.randomUUID();
        var employeeId = UUID.randomUUID();
        var optionalCancelDTO = Optional.of(new CancelDTO());
        doThrow(new EntityNotFoundException(Request.class, requestId)).when(requestValidationService).validateAndGetRequest(any());
        assertThatThrownBy(() -> requestControllerService.cancel(
                requestId, employeeId, optionalCancelDTO, "token"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("entityName", "Request")
                .hasFieldOrPropertyWithValue("entityId", requestId);
    }

    @Test
    @DisplayName("Отмена. Пользователь не найден")
    void test_userNotAllowed_Throws() {
        var employeeId = UUID.randomUUID();
        var optionalCancelDTO = Optional.of(new CancelDTO());
        var request =
                RequestForTaxi.builder().transportType(TransportTypeEnum.TAXI)
                        .approvedBy(Employee.builder().id(employeeId).build()).build();
        doReturn(Optional.of(request)).when(requestService).get(any());
        assertThrows(UserNotFoundException.class, () -> requestControllerService.cancel(
                UUID.randomUUID(), employeeId, optionalCancelDTO, "token"));
    }

    @Test
    @DisplayName("Отмена. Заявка в не редактируемом статусе")
    void test_requestNotCancelable_Throws() throws NoSuchFieldException, IllegalAccessException {
        var requestId = UUID.randomUUID();
        var author = Employee.builder().id(UUID.randomUUID()).build();
        var passengerId = UUID.randomUUID();
        var passenger = Employee.builder().id(passengerId).build();
        var optionalCancelDTO = Optional.of(new CancelDTO());
        var request = RequestForTaxi.builder().author(author).transportType(TransportTypeEnum.TAXI)
                .passenger(passenger)
                .expected(ExpectedData.builder().cost(100d).build())
                .purpose(TripPurpose.builder().id(UUID.randomUUID()).build())
                .status(TripRequestStatus.TAXI_DRIVER_SEARCH).build();
        when(employeeService.getByUserId(passengerId)).thenReturn(Optional.of(passenger));
        when(tripPurposeRepository.findById(UUID.randomUUID()))
                .then(invocation -> Optional.of(TripPurpose.builder().id(invocation.getArgument(0)).build()));
        doReturn(request).when(requestValidationService).validateAndGetRequest(any());
        doThrow(new IllegalStateResponseException(String.format(CANCEL_REQUEST_ILLEGAL_STATUS_FORMAT, request.getStatus())))
                .when(requestValidationService).validateStatusAndGetCancelDto(any(), any(),
                        any());
        assertThrows(IllegalStateResponseException.class, () -> requestControllerService.cancel(
                requestId, passengerId, optionalCancelDTO, "token"));
    }

    @Test
    @DisplayName("Оценка. Несуществующая заявка")
    void rate_requestNotFound_Throws() {
        var requestId = UUID.randomUUID();
        var employee = Employee.builder().id(UUID.randomUUID()).build();
        var requestRatingDTO = new RequestRatingDTO();
        doThrow(new EntityNotFoundException(Request.class, requestId)).when(requestValidationService).validateAndGetRequest(any());
        assertThatThrownBy(() -> requestControllerService.rate(
                requestId, requestRatingDTO, employee))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("entityName", "Request")
                .hasFieldOrPropertyWithValue("entityId", requestId);
    }

    @Test
    @DisplayName("Оценка. Пользователь не пассажир")
    void rate_employeeNotPassenger_Throws() {
        var requestId = UUID.randomUUID();
        var employee = Employee.builder().id(UUID.randomUUID()).build();
        var passenger = Employee.builder().id(UUID.randomUUID()).build();
        var requestRatingDTO = new RequestRatingDTO();
        var request = RequestForTaxi.builder().transportType(TransportTypeEnum.TAXI).passenger(passenger).build();
        doReturn(request).when(requestValidationService).validateAndGetRequest(any());
        assertThrows(IllegalCallerResponseException.class, () -> requestControllerService.rate(
                requestId, requestRatingDTO, employee));
    }

    @Test
    @DisplayName("Оценка. Заявка уже оценена")
    void rate_requestAlreadyRated_Throws() {
        RequestRatingDTO newRating = new RequestRatingDTO();
        newRating.setRating(5);
        var employee = Employee.builder().id(UUID.randomUUID()).build();
        RequestRating oldRating = new RequestRating();
        oldRating.setRating(0);
        var request =
                RequestForTaxi.builder().transportType(TransportTypeEnum.TAXI)
                        .passenger(employee).status(TripRequestStatus.TAXI_TRIP_FINISHED).requestRating(oldRating)
                        .taxiTrip(TaxiTrip.builder().organizationId(UUID.randomUUID()).assignedCar(CarInfo.builder().build()).build())
                        .id(UUID.randomUUID())
                        .build();
        doReturn(request).when(requestValidationService).validateAndGetRequest(any());
        doReturn(Optional.of(request)).when(requestForTaxiRepository).findById(any());
        GetRequestDTO requestDTO = requestControllerService.rate(UUID.randomUUID(), newRating, employee);
        assertNotNull(requestDTO.getRequestRating());
        assertNotEquals(requestDTO.getRequestRating().getRating(), newRating.getRating());
        assertEquals(requestDTO.getRequestRating().getRating(), oldRating.getRating());
    }

    @Test
    @DisplayName("Изменение статуса. Несуществующая заявка")
    void changeState_requestNotFound_Throws() {
        var requestId = UUID.randomUUID();
        var employee = Employee.builder().id(UUID.randomUUID()).build();
        doThrow(new EntityNotFoundException(Request.class, requestId)).when(requestValidationService).validateAndGetRequest(any());
        assertThatThrownBy(() -> requestControllerService.changeStatus(
                requestId, TripRequestStatus.TAXI_TRIP_FINISHED, employee, null, null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("entityName", "Request")
                .hasFieldOrPropertyWithValue("entityId", requestId);

    }

    @Test
    @DisplayName("Изменение статуса. Недопустимый статус")
    void changeState_illegalStatus_Throws() {
        var requestId = UUID.randomUUID();
        var employee = Employee.builder().id(UUID.randomUUID()).build();
        var request = RequestForTaxi.builder().transportType(TransportTypeEnum.TAXI)
                .status(TripRequestStatus.TAXI_AWAITING_APPROVAL).build();
        doReturn(request).when(requestValidationService).validateAndGetRequest(any());
        doThrow(new IllegalStateResponseException(String.format(CHANGE_REQUEST_ILLEGAL_STATUS_FORMAT, request.getStatus())))
                .when(requestValidationService).validateCurrentRequestStatus(any());
        assertThrows(IllegalStateResponseException.class, () -> requestControllerService.changeStatus(
                requestId, TripRequestStatus.TAXI_TRIP_FINISHED, employee, null, null));
    }


    @Test
    @DisplayName("Изменение статуса. Уменьшение")
    void changeState_decreaseStatus_Throws() {
        var requestId = UUID.randomUUID();
        var employee = Employee.builder().id(UUID.randomUUID()).build();
        var request = RequestForTaxi.builder().transportType(TransportTypeEnum.TAXI)
                .status(TripRequestStatus.TAXI_DRIVER_FOUND).build();
        doReturn(request).when(requestValidationService).validateAndGetRequest(any());
        doThrow(new IllegalStateResponseException(String.format(DECREASE_REQUEST_ILLEGAL_STATUS_FORMAT, request.getStatus(),
                TripRequestStatus.TAXI_APPROVED)))
                .when(requestValidationService).validateCurrentRequestStatus(any());
        assertThrows(IllegalStateResponseException.class, () -> requestControllerService.changeStatus(
                requestId, TripRequestStatus.TAXI_APPROVED, employee, null, null));
    }

    @Test
    @DisplayName("Получение отмененной заявки с типом TAXI по ID возвращает нужное описание")
    void getCancelledTaxiRequestShouldReturnCorrectDescription() {
        var request = Instancio.of(RequestForTaxi.class)
                .set(Select.field(Request.class, "transportType"), TransportTypeEnum.TAXI)
                .set(Select.field(Request.class, "status"), TripRequestStatus.TAXI_CANCELLED)
                .set(Select.field(Request.class, "statusCode"), 207)
                .create();

        var department = Instancio.of(Department.class).create();

        when(requestValidationService.validateAndGetRequest(any())).thenReturn(request);
        when(requestForTaxiRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(departmentService.getWithApproversByIds(any())).thenReturn(Collections.singleton(department));

        var result = requestControllerService.get(request.getId());
        assertEquals("Отменено исполнителем", result.getStatusCodeDescription());
    }

    @Test
    @DisplayName("Получение отмененной заявки с указанием типа TAXI по ID возвращает нужное описание")
    void getCancelledTaxiRequestShouldReturnCorrectDescription2() {
        var request = Instancio.of(RequestForTaxi.class)
                .set(Select.field(Request.class, "transportType"), TransportTypeEnum.TAXI)
                .set(Select.field(Request.class, "status"), TripRequestStatus.TAXI_CANCELLED)
                .set(Select.field(Request.class, "statusCode"), 207)
                .create();

        var department = Instancio.of(Department.class).create();

        when(requestValidationService.validateAndGetRequest(request.getId(), TransportTypeEnum.TAXI)).thenReturn(request);
        when(requestForTaxiRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(departmentService.getWithApproversByIds(any())).thenReturn(Collections.singleton(department));

        var result = requestControllerService.get(request.getId(), TransportTypeEnum.TAXI);
        assertEquals("Отменено исполнителем", result.getStatusCodeDescription());
    }
}
