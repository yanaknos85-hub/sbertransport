package ru.sberbank.transport.oto.cargo.controller;

import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.enums.RequestTypeEnum;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.messaging.TemplateForCargoMessage;
import ru.sberbank.transport.oto.cargo.SharedTestData;
import ru.sberbank.transport.oto.cargo.database.dao.*;
import ru.sberbank.transport.oto.cargo.database.model.*;
import ru.sberbank.transport.oto.cargo.database.model.template.TemplateForCargo;
import ru.sberbank.transport.oto.cargo.dto.cargo.CargoRequestDto;
import ru.sberbank.transport.oto.cargo.service.OrganizationService;
import ru.sberbank.transport.oto.cargo.util.CheckOrganizationAccessUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.CARGO_APPROVED;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@UnitTest
@Isolated
@Feature("app_passenger_oto")
@DisplayName("Проверка контроллера по грузоперевозкам для инженера ОТО")
@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(properties = {"logger.level.root=debug", "spring.main.cloud-platform=none"})
@EmbeddedPostgres
@MockitoBean(types = {CheckOrganizationAccessUtils.class, JwtDecoder.class})
@Transactional
@ActiveProfiles("test")
class OtoEngineerControllerImplTest extends SharedTestData {

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private TemplateForCargoRepository templateForCargoRepository;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @Autowired
    private OtoEngineerController otoEngineerController;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private WaypointRepository waypointRepository;

    UUID org1 = UUID.fromString("0c33d791-f48c-44ee-b59b-831cc6f386fd");
    Request savedRequest;
    Request savedRequest2;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(roleCheckService);
        var org = organizationRepository.save(Organization.builder().officialName("name").id(org1).build());
        org1 = org.getId();

        var position = createPosition("Position");
        position = positionRepository.save(position);

        var department = Department.builder()
                .id(UUID.randomUUID())
                .departmentName("Department")
                .organizationId(org1)
                .build();
        department = departmentRepository.save(department);
        var contractor = createContractor("Contractor ");
        contractor = contractorRepository.save(contractor);

        var passenger = createPassenger(UUID.fromString(USER1_ID), "HRE", "LastName",
                "FirstName",
                "Patronymic",
                "Phone",
                position,
                department);
        passenger = employeeRepository.save(passenger);
        var address = Address.builder().id(UUID.randomUUID()).city("Москва")
                .region("Москва")
                .street("Пушкина")
                .addressString("Москва,ул. Пушкина").build();
        var addressSaved = addressRepository.saveAndFlush(address);
        var addres2 = Address.builder().id(UUID.randomUUID()).city("Сергиев Посад")
                .region("Московская область")
                .street("Лермонтова").addressString("Московская область, город Люберцы, ул. Лермонтова").build();
        addressRepository.saveAndFlush(addres2);
        var waypoint1 = Waypoint.builder().id(UUID.randomUUID()).address(addressSaved).orderingIndex(0).build();
        var waypoint2 = Waypoint.builder().id(UUID.randomUUID()).address(addres2).orderingIndex(1).build();
        var savedWaypoint = waypointRepository.saveAndFlush(waypoint1);
        waypointRepository.saveAndFlush(waypoint2);
        List<Waypoint> waypointList = new ArrayList<>();
        waypointList.add(savedWaypoint);
        waypointList.add(waypoint2);
        var request = createRequest(UUID.randomUUID(), "HR"
                , CARGO_APPROVED
                , TransportTypeEnum.INDIVIDUAL
                , contractor, passenger, ExpectedData.builder().cost(10d).distance(100d).build()
                , waypointList
                , LocalDateTime.now(), LocalDateTime.now().plusDays(4), LocalDateTime.now().plusDays(10));
        request.setTemplate(true);
        request.setOrganization(org);
        request.setStartWaypoint(savedWaypoint);
        request.setSenderName("Иванов Сергей Валентинович");
        request.setRecipientName("Прутковский Кузьма Петрович");
        request.setLoaders(5);
        savedRequest = requestRepository.saveAndFlush(request);

        var request2 = createRequest(UUID.randomUUID(), "HR2"
                , CARGO_APPROVED
                , TransportTypeEnum.INDIVIDUAL
                , contractor, passenger, ExpectedData.builder().cost(10d).distance(100d).build()
                , new ArrayList<>()
                , LocalDateTime.now(), LocalDateTime.now().plusDays(4), LocalDateTime.now().plusDays(10));
        request2.setTemplate(false);
        request2.setOrganization(org);
        request2.setLoaders(5);
        savedRequest2 = requestRepository.saveAndFlush(request2);
    }

    @Test
    @DisplayName("Получение")
    @WithMockUser(roles = "GUEST")
    void getCargoRequests() {
        var auth = new FakeAuthentication();
        when(employeeRepository.findByUserId(any())).thenReturn(Optional.of(Employee.builder().department(Department.builder().build()).build()));
        when(organizationService.findById(any())).thenReturn(Optional.of(Organization.builder().build()));

        var result =
                otoEngineerController.getCargoRequests(Instancio.ofBlank(CargoRequestDto.class)
                        .set(Select.field(CargoRequestDto::organizationId), org1)
                        .set(Select.field(CargoRequestDto::pageSize), 1)
                        .set(Select.field(CargoRequestDto::senderName), "Сергей Иванов ")
                        .set(Select.field(CargoRequestDto::requestTypes), List.of(RequestTypeEnum.REGULAR))
                        .create(), auth);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getHumanReadableId()).isEqualTo(savedRequest.getHumanReadableId());
        assertThat(result.getContent().getFirst().getSenderAddress()).isEqualTo("Москва,ул. Пушкина");
        assertThat(result.getContent().getFirst().getRecipientAddress()).isEqualTo("Московская область, город Люберцы, ул. Лермонтова");
        assertThat(result.getContent().getFirst().getSender()).isEqualTo("Иванов Сергей Валентинович");

        var result2 =
                otoEngineerController.getCargoRequests(Instancio.ofBlank(CargoRequestDto.class)
                                .set(Select.field(CargoRequestDto::organizationId), org1)
                                .set(Select.field(CargoRequestDto::pageSize), 1)
                                .set(Select.field(CargoRequestDto::requestTypes), List.of(RequestTypeEnum.SINGLE))
                                .create(), auth);
        assertThat(result2.getContent().size()).isEqualTo(1);
        assertThat(result2.getContent().getFirst().getHumanReadableId()).isEqualTo(savedRequest2.getHumanReadableId());
        assertThat(result2.getContent().getFirst().getLoaders()).isEqualTo(5);
    }

    @Test
    @DisplayName("Получение с фильтром по контрольной дате")
    @WithMockUser(roles = "GUEST")
    void getCargoRequests3() {
        var auth = new FakeAuthentication();
        when(employeeRepository.findByUserId(any())).thenReturn(Optional.of(Employee.builder().department(Department.builder().build()).build()));
        when(organizationService.findById(any())).thenReturn(Optional.of(Organization.builder().build()));

        var result = otoEngineerController.getCargoRequests(Instancio.ofBlank(CargoRequestDto.class)
                .set(Select.field(CargoRequestDto::organizationId), org1)
                .set(Select.field(CargoRequestDto::pageSize), 1)
                .set(Select.field(CargoRequestDto::senderName), "Сергей Иванов ")
                .set(Select.field(CargoRequestDto::controlTimeFrom), ZonedDateTime.now().plusDays(10))
                .set(Select.field(CargoRequestDto::controlTimeTo), ZonedDateTime.now().plusDays(15))
                .set(Select.field(CargoRequestDto::requestTypes), List.of(RequestTypeEnum.REGULAR))
                .create(), auth);
        assertThat(result.getContent()).hasSize(1);

        var result2 = otoEngineerController.getCargoRequests(Instancio.ofBlank(CargoRequestDto.class)
                .set(Select.field(CargoRequestDto::organizationId), org1)
                .set(Select.field(CargoRequestDto::pageSize), 1)
                .set(Select.field(CargoRequestDto::controlTimeTo), ZonedDateTime.now().plusDays(15))
                .set(Select.field(CargoRequestDto::transferTimeFrom), ZonedDateTime.now().plusDays(16))
                .set(Select.field(CargoRequestDto::requestTypes), List.of(RequestTypeEnum.SINGLE))
                .create(), auth);
        assertThat(result2.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Получение заявок без точек загрузки и разгрузки")
    @WithMockUser(roles = "GUEST")
    void getCargoRequests2() {
        var auth = new FakeAuthentication();
        when(employeeRepository.findByUserId(any())).thenReturn(Optional.of(Employee.builder().department(Department.builder().build()).build()));
        when(organizationService.findById(any())).thenReturn(Optional.of(Organization.builder().build()));

        var result =
                otoEngineerController.getCargoRequests(Instancio.ofBlank(CargoRequestDto.class)
                        .set(Select.field(CargoRequestDto::organizationId), org1)
                        .set(Select.field(CargoRequestDto::pageSize), 1)
                        .set(Select.field(CargoRequestDto::requestTypes), List.of(RequestTypeEnum.SINGLE))
                        .create(), auth);
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getHumanReadableId()).isEqualTo(savedRequest2.getHumanReadableId());
        assertThat(result.getContent().getFirst().getSenderAddress()).isNull();
        assertThat(result.getContent().getFirst().getRecipientAddress()).isNull();
    }

    @Test
    @DisplayName("Получение расписаний")
    void getTemplates() {
        setupTemplates();
        TripRequestStatus[] statuses = {CARGO_APPROVED};
        var result =
                otoEngineerController.getCargoTemplatesForOto(org1, 10, 0, null, null
                        , "HUM1", statuses, ZonedDateTime.now().minusHours(1)
                        , ZonedDateTime.now().plusDays(1), "FIO1", "Строгинский 2 "
                        , "FIO2", "Шаболовка"
                        , null);
        assertThat(result.getContent().size()).isEqualTo(1);

        result =
                otoEngineerController.getCargoTemplatesForOto(org1, 10, 0, null, null
                        , "HUM2", null, null
                        , null, null, null
                        , null, null
                        , null);
        assertThat(result.getContent().size()).isEqualTo(0);

        result =
                otoEngineerController.getCargoTemplatesForOto(org1, 10, 0, null, null
                        , null, null, null
                        , null, null, null
                        , null, null
                        , null);
        assertThat(result.getContent().size()).isEqualTo(1);
    }

    private static class FakeAuthentication extends JwtAuthenticationToken {

        public FakeAuthentication() {
            super(Jwt.withTokenValue("token")
                            .header("alg", "none")
                            .jti(UUID.randomUUID().toString())
                            .claim("roles", List.of("ROLE_ADMIN_DATA_MASTER"))
                            .build(),
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN_DATA_MASTER"))
            );
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return null;
        }

        @Override
        public boolean isAuthenticated() {
            return false;
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

        }

        @Override
        public String getName() {
            return UUID.randomUUID().toString();
        }
    }

    private void setupTemplates() {
        var templateForCargo = TemplateForCargo.builder()
                .id(UUID.randomUUID())
                .status(CARGO_APPROVED)
                .humanReadableId("HUM1")
                .organizationId(org1)
                .requestsDateDelivery(List.of(LocalDateTime.now()))
                .cronExpression("0 0 0 * * 5")
                .creationTime(LocalDateTime.now())
                .senderName("FIO1")
                .recipientName("FIO2")
                .senderAddress("Строгинский бульвар 2 корп 1")
                .recipientAddress("Шаболовка 31Г")
                .template(
                        TemplateForCargoMessage
                                .TemplateInfo.builder()
                                .waypoints(List.of(TemplateForCargoMessage
                                                .Waypoint.builder()
                                                .orderingIndex(1)
                                                .addressStringRepresentation("Address1")
                                                .contact(TemplateForCargoMessage.Contact
                                                        .builder()
                                                        .fio("FIO1")
                                                        .build()).build(),
                                        TemplateForCargoMessage
                                                .Waypoint.builder()
                                                .orderingIndex(0)
                                                .addressStringRepresentation("Address2")
                                                .contact(TemplateForCargoMessage.Contact
                                                        .builder()
                                                        .fio("FIO2")
                                                        .build()).build())).build())
                .build();
        templateForCargoRepository.saveAndFlush(templateForCargo);
    }
}