package ru.sberbank.ditsib.transport.vehicle.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sberbank.ditsib.transport.vehicle.BaseIntegrationTest;
import ru.sberbank.ditsib.transport.vehicle.constants.Role;
import ru.sberbank.ditsib.transport.vehicle.constants.TransportStatus;
import ru.sberbank.ditsib.transport.vehicle.database.dao.BrandRepository;
import ru.sberbank.ditsib.transport.vehicle.database.dao.ModelRepository;
import ru.sberbank.ditsib.transport.vehicle.database.dao.TransportRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Transport;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.LocationRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.OrganizationRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSearchingRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create.DocumentsCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create.TransportCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create.VehicleCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.update.DocumentsUpdateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.update.TransportUpdateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.update.VehicleUpdateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.AccessiblePositionDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.OrganizationResponseDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportResponseDto;
import ru.sberbank.ditsib.transport.vehicle.messaging.sender.TransportSender;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/scripts/vehicle_integration_test.sql")
@Sql("/scripts/transport_integration_test.sql")
class TransportTest extends BaseIntegrationTest {
    private static final String ROOT_PATH = "/transport";

    private static final UUID EMPLOYEE_1 = fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8");
    private static final UUID EMPLOYEE_2 = fromString("01fab58c-ac99-45ef-a83e-f05b5a39d88d");
    private static final UUID EMPLOYEE_3 = fromString("3cd35c19-fd39-413c-99a0-30f35bd642a7");
    private static final UUID ORGANIZATION_1 = fromString("fc73b25b-9564-4560-98b5-abc0f16af9b2");
    private static final UUID ORGANIZATION_2 = fromString("3cbe0b6b-fe04-4a28-83e8-184f0f331bff");
    private static final UUID DEPARTMENT_1 = fromString("003a33fb-faa6-49a7-be37-106fdbef2324");
    private static final UUID DEPARTMENT_2 = fromString("000098ba-5c12-423f-ba6a-de9c76db31a5");
    private static final UUID DEPARTMENT_3 = fromString("d728e86f-d624-fbf7-3fe6-fa07f3ead218");
    private static final UUID VEHICLE_CHANGAN = fromString("400466cb-df0e-40be-8b34-9ff42444637a");
    private static final UUID VEHICLE_VAZ = fromString("748ba8ab-572a-4178-9e05-fae61aefb376");
    private static final UUID VEHICLE_VAZ2 = fromString("748ba8ab-572a-4178-9e05-fae61aefb371");
    private static final TransportCreateDto TRANSPORT_CREATE_DTO_1 = prepareCreateRequestOrg1();
    private static final TransportCreateDto TRANSPORT_CREATE_DTO_2 = prepareCreateRequestOrg2();
    private static final TransportCreateDto TRANSPORT_CREATE_DTO_3 = prepareCreateRequestOrg3();
    private static final TransportCreateDto TRANSPORT_CREATE_DTO_4 = prepareCreateRequestOrg4();
    private static final TransportCreateDto TRANSPORT_CREATE_DTO_5 = prepareCreateRequestOrg5();
    private static final TransportCreateDto TRANSPORT_CREATE_DTO_6 = prepareCreateRequestOrg6();
    private static final TransportCreateDto TRANSPORT_CREATE_DTO_7 = prepareCreateRequestOrg7();
    private static final TransportCreateDto TRANSPORT_CREATE_DTO_8 = prepareCreateRequestOrg8();
    private static final TransportCreateDto TRANSPORT_CREATE_DTO_9 = prepareCreateRequestOrg9();
    private static final TransportUpdateDto TRANSPORT_UPDATE_DTO_1 = prepareUpdateRequestOrg1();
    private static final TransportUpdateDto TRANSPORT_UPDATE_DTO_2 = prepareUpdateRequestOrg2();

    @MockitoBean
    private TransportSender transportSender;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private TransportRepository transportRepository;
    
    private final ArgumentCaptor<Transport> capturedTransport = ArgumentCaptor.forClass(Transport.class);


    @Test
    @DisplayName("Сохранение и получение сущности")
    @SneakyThrows
    @Transactional
    void saveAndGetTransport() {
        doNothing().when(transportSender).send(capturedTransport.capture(), anyBoolean());
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        var transportId = transportRepository
                .findAll()
                .stream()
                .filter(ts -> ts.getOrganizations().stream()
                                .anyMatch(org -> TRANSPORT_CREATE_DTO_1.organizations().get(0).organizationId().equals(org.getId())))
                .findFirst()
                .get()
                .getId();

        var contentAsStringGot = mockMvc.perform(get(ROOT_PATH + "/" + transportId.toString())
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        var transportResponseDto = objectMapper.readValue(contentAsStringGot, TransportResponseDto.class);

        assertTransport1(transportResponseDto, transportId);
        
        var transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new JUnitException("Transport not found"));
        assertThat(capturedTransport.getValue().getSubtype().getTitle())
                .isEqualTo(transport.getSubtype().getTitle());
        assertThat(capturedTransport.getValue().getSubtype().getType().getTitle())
                .isEqualTo(transport.getSubtype().getType().getTitle());
    }

    @ParameterizedTest
    @MethodSource
    @SneakyThrows
    void createTransport(UUID employeeId,
                         Set<Role> roles,
                         TransportCreateDto createDto,
                         ResultMatcher resultMatcher) {
        
        if (!roles.contains(Role.ROLE_PARKING_ADMIN_ORGANIZATION)) {
            Mockito.<AuthorizationManager<?>>reset(manager);
            roles.stream().findFirst()
                 .ifPresent(role -> AuthorizeUtils.authorize(manager, role.name()));
        }
        
        var preparedRoles = roles.stream()
                                 .map(role -> new SimpleGrantedAuthority(role.name()))
                                 .map(GrantedAuthority.class::cast)
                                 .toList();
        doNothing().when(transportSender).send(capturedTransport.capture(), anyBoolean());
        var response = mockMvc.perform(post(ROOT_PATH)
                                               .with(jwt().jwt(builder -> builder.jti(employeeId.toString()))
                                                          .authorities(preparedRoles))
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content(objectMapper.writeValueAsString(createDto)))
                              .andExpect(resultMatcher)
                              .andReturn();
        
        if (resultMatcher == status().isOk()) {
            var responseAsString = response.getResponse().getContentAsString(StandardCharsets.UTF_8);
            var transportResponseDto = objectMapper.readValue(responseAsString, TransportResponseDto.class);
            var transport = transportRepository.findById(transportResponseDto.id())
                                               .orElseThrow(() -> new JUnitException("Transport not found"));
            assertThat(capturedTransport.getValue().getSubtype().getTitle())
                    .isEqualTo(transport.getSubtype().getTitle());
            assertThat(capturedTransport.getValue().getSubtype().getType().getTitle())
                    .isEqualTo(transport.getSubtype().getType().getTitle());
            assertThat(capturedTransport.getValue().getLocationAddress())
                    .isEqualTo(transport.getLocationAddress());
            assertThat(capturedTransport.getValue().getParkingAddress())
                    .isEqualTo(transport.getParkingAddress());
            assertThat(capturedTransport.getValue().getComment())
                    .isEqualTo(transport.getComment());
        }
    }

    private static Stream<Arguments> createTransport() {
        return Stream.of(
                Arguments.of(
                        EMPLOYEE_1,
                        Set.of(Role.ROLE_ADMIN_DATA_MASTER, Role.ROLE_ADMIN_CORP_CLIENT),
                        TRANSPORT_CREATE_DTO_1,
                        status().isOk()),
                Arguments.of(
                        EMPLOYEE_1,
                        Set.of(Role.ROLE_ADMIN_DATA_MASTER, Role.ROLE_ADMIN_CORP_CLIENT),
                        TRANSPORT_CREATE_DTO_2,
                        status().isOk()),
                Arguments.of(
                        EMPLOYEE_1,
                        Set.of(Role.ROLE_ADMIN_CORP_CLIENT, Role.ROLE_ADMIN_DATA_MASTER),
                        TRANSPORT_CREATE_DTO_1,
                        status().isOk()),
                Arguments.of(
                        EMPLOYEE_1,
                        Set.of(Role.ROLE_ADMIN_CORP_CLIENT, Role.ROLE_ADMIN_DATA_MASTER),
                        TRANSPORT_CREATE_DTO_2,
                        status().isOk()),
                Arguments.of(
                        EMPLOYEE_1,
                        Set.of(Role.ROLE_ADMIN_CORP_CLIENT),
                        TRANSPORT_CREATE_DTO_1,
                        status().isOk()),
                Arguments.of(
                        EMPLOYEE_1,
                        Set.of(Role.ROLE_ADMIN_CORP_CLIENT),
                        TRANSPORT_CREATE_DTO_1.withVehicle(TRANSPORT_CREATE_DTO_1.vehicle().withCurrentMileage(0)),
                        status().isOk()),
                Arguments.of(
                        EMPLOYEE_1,
                        Set.of(Role.ROLE_PARKING_ADMIN_ORGANIZATION),
                        TRANSPORT_CREATE_DTO_2,
                        status().isForbidden()),
                Arguments.of(
                        EMPLOYEE_2,
                        Set.of(Role.ROLE_PARKING_ADMIN_ORGANIZATION),
                        TRANSPORT_CREATE_DTO_1,
                        status().isForbidden()),
                Arguments.of(
                        EMPLOYEE_1,
                        Set.of(Role.ROLE_ADMIN_CORP_CLIENT),
                        TRANSPORT_CREATE_DTO_4,
                        status().isBadRequest()),
                Arguments.of(
                        EMPLOYEE_1,
                        Set.of(Role.ROLE_DISPATCHER_CONTRACTOR),
                        TRANSPORT_CREATE_DTO_6,
                        status().isOk()
                ),
                Arguments.of(
                        EMPLOYEE_1,
                        Set.of(Role.ROLE_DISPATCHER_CONTRACTOR),
                        TRANSPORT_CREATE_DTO_7,
                        status().isConflict()
                ),
                Arguments.of(
                        EMPLOYEE_1,
                        Set.of(Role.ROLE_DISPATCHER_CONTRACTOR),
                        TRANSPORT_CREATE_DTO_8,
                        status().isConflict()
                )
        );
    }

    @Test
    @DisplayName("Проверка обновления сущности")
    @SneakyThrows
    @Transactional
    void updateTransport() {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        
        doNothing().when(transportSender).send(Mockito.any(Transport.class), anyBoolean());
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_1)))
                .andExpect(status().isOk());

        var transport = transportRepository
                .findAll()
                .stream()
                .filter(ts -> ts.getOrganizations().stream()
                                .anyMatch(org -> TRANSPORT_CREATE_DTO_1.organizations().get(0).organizationId().equals(org.getId())))
                .findFirst()
                .get();

        assertThat(TRANSPORT_CREATE_DTO_1.location().locationAddress())
                .isEqualTo(transport.getLocationAddress());
        assertThat(TRANSPORT_CREATE_DTO_1.location().parkingAddress())
                .isEqualTo(transport.getParkingAddress());
        assertThat(TRANSPORT_CREATE_DTO_1.comment())
                .isEqualTo(transport.getComment());
        assertThat(TRANSPORT_CREATE_DTO_1.vehicle().currentMileage())
                .isEqualTo(transport.getCurrentMileage());

        var transportId1 = transport.getId();

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_2.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_2)))
                .andExpect(status().isOk());

        var transportId2 = transportRepository
                .findAll()
                .stream()
                .filter(ts -> ts.getOrganizations().stream()
                                .anyMatch(org -> TRANSPORT_CREATE_DTO_2.organizations().get(0).organizationId().equals(org.getId())))
                .findFirst()
                .get()
                .getId();

        var update1 = TRANSPORT_UPDATE_DTO_1.withVehicle(TRANSPORT_UPDATE_DTO_1.vehicle().withId(VEHICLE_CHANGAN))
                                            .withLocation(TRANSPORT_UPDATE_DTO_1.location()
                                                                                .withLocationAddress("Новый адрес")
                                                                                .withParkingAddress("Новый адрес2"))
                                            .withComment("Новый комментарий");
        mockMvc.perform(patch(ROOT_PATH + "/" + transportId1.toString())
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Новый комментарий"))
                .andExpect(jsonPath("$.location.locationAddress").value("Новый адрес"))
                .andExpect(jsonPath("$.location.parkingAddress").value("Новый адрес2"));

        mockMvc.perform(patch(ROOT_PATH + "/" + transportId2.toString())
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(
                        "Транспортное средство с VIN номером " + TRANSPORT_UPDATE_DTO_1.vehicle().vinCode() +
                                ", номером основного средства " + TRANSPORT_UPDATE_DTO_1.vehicle().assetNumber() +
                                ", гос. номером " + TRANSPORT_UPDATE_DTO_1.vehicle().stateNumber() + " уже существует"));

        var updateWithVinCodeCollision = TRANSPORT_UPDATE_DTO_1.withVehicle(
                TRANSPORT_UPDATE_DTO_1.vehicle()
                        .withVinCode(TRANSPORT_CREATE_DTO_2.vehicle().vinCode()));
        mockMvc.perform(patch(ROOT_PATH + "/" + transportId1)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWithVinCodeCollision)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(
                        "Транспортное средство с VIN номером " + TRANSPORT_CREATE_DTO_2.vehicle().vinCode() + " уже существует"));
    }

    @Test
    @DisplayName("Дата ввода в эксплуатацию не должна быть позже даты вывода из эксплуатации при обновлении.")
    @SneakyThrows
    @Transactional
    void updateTransportFailure() {
        doNothing().when(transportSender).send(Mockito.any(Transport.class), anyBoolean());
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        var transportId1 = transportRepository
                .findAll()
                .stream()
                .filter(ts -> ts.getOrganizations().stream()
                                .anyMatch(org -> TRANSPORT_CREATE_DTO_1.organizations().get(0).organizationId().equals(org.getId())))
                .findFirst()
                .get()
                .getId();

        mockMvc.perform(patch(ROOT_PATH + "/deactivate/" + transportId1.toString())
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "exploitationEnd": 1675987200000
                                }
                                """))
                .andExpect(status().isOk());

        var update1 = TRANSPORT_UPDATE_DTO_1
                .withLocation(TRANSPORT_UPDATE_DTO_1.location()
                                                    .withExploitationStart(LocalDate.of(2023, 2, 11).atStartOfDay()));
        mockMvc.perform(patch(ROOT_PATH + "/" + transportId1)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Дата не может быть меньше даты начала эксплуатации"));
        var update2 = TRANSPORT_UPDATE_DTO_1
                .withVehicle(new VehicleUpdateDto(VEHICLE_CHANGAN, "А133АА99", "WBA47110007817985",
                        "2423234234", "542353454332", "4534523123412",
                        "453453532453", fromString("60b9b87e-3a4c-4500-acc4-60dbea5eec46"),
                        2001, null));
        mockMvc.perform(patch(ROOT_PATH + "/" + transportId1)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update2)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> result.getResponse().getContentAsString().equals("{\"vehicle.subtypeId\":\"Идентификатор подвида ТС должен быть задан\"}"));
        ;
    }
    
    @Test
    @DisplayName("Организация должна быть связана с переданным департаментом")
    @SneakyThrows
    @Transactional
    void updateTransportRelationsFailure() {
        doNothing().when(transportSender).send(Mockito.any(Transport.class), anyBoolean());
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        
        mockMvc.perform(post(ROOT_PATH)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_1)))
               .andExpect(status().isOk())
               .andReturn()
               .getResponse()
               .getContentAsString(StandardCharsets.UTF_8);
        var transportId1 = transportRepository
                .findAll()
                .stream()
                .filter(ts -> ts.getOrganizations().stream()
                                .anyMatch(org -> TRANSPORT_CREATE_DTO_1.organizations().get(0).organizationId().equals(org.getId())))
                .findFirst()
                .get()
                .getId();
        
        var update1 = TRANSPORT_UPDATE_DTO_2
                .withLocation(TRANSPORT_UPDATE_DTO_2.location()
                                                    .withExploitationStart(LocalDate.of(2023, 2, 11).atStartOfDay()));
        mockMvc.perform(patch(ROOT_PATH + "/" + transportId1)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update1)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.detail")
                                  .value(String.format("Подразделение %s не принадлежит организации %s", DEPARTMENT_3, ORGANIZATION_2)));
    }
    
    @Test
    @DisplayName("Текущий пробег не может быть меньше предыдущего")
    @SneakyThrows
    @Transactional
    void updateTransportMileageFailure() {
        doNothing().when(transportSender).send(Mockito.any(Transport.class), anyBoolean());
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        
        mockMvc.perform(post(ROOT_PATH)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_1)))
               .andExpect(status().isOk())
               .andReturn()
               .getResponse()
               .getContentAsString(StandardCharsets.UTF_8);
        var transportId1 = transportRepository
                .findAll()
                .stream()
                .filter(ts -> ts.getOrganizations().stream()
                                .anyMatch(org -> TRANSPORT_CREATE_DTO_1.organizations().get(0).organizationId().equals(org.getId())))
                .findFirst()
                .get()
                .getId();
        
        var update1 = TRANSPORT_UPDATE_DTO_2
                .withVehicle(TRANSPORT_UPDATE_DTO_2.vehicle().withCurrentMileage(1000));
        mockMvc.perform(patch(ROOT_PATH + "/" + transportId1)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update1)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.detail")
                                  .value("Текущий пробег не может быть меньше предыдущего"));
    }

    @Test
    @DisplayName("Автомобиль должен быть деактивирован")
    @SneakyThrows
    @Transactional
    void deactivation() {
        doNothing().when(transportSender).send(Mockito.any(Transport.class), anyBoolean());
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        var transportId1 = transportRepository
                .findAll()
                .stream()
                .filter(ts -> ts.getOrganizations().stream()
                                .anyMatch(org -> TRANSPORT_CREATE_DTO_1.organizations().get(0).organizationId().equals(org.getId())))
                .findFirst()
                .get()
                .getId();

        mockMvc.perform(patch(ROOT_PATH + "/deactivate/" + transportId1.toString())
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "exploitationEnd": 1675987200000
                                }
                                """))
                .andExpect(status().isOk());

        var foundTransport = transportRepository.findById(transportId1);
        assertThat(foundTransport)
                .isPresent()
                .get()
                .extracting(Transport::getExploitationEnd)
                .isEqualTo(LocalDate.of(2023, 2, 10));
    }


    @Test
    @DisplayName("Дата не может быть больше текущей даты и дата не может быть меньше даты начала эксплуатации")
    @SneakyThrows
    @Transactional
    void deactivationFailure() {
        doNothing().when(transportSender).send(Mockito.any(Transport.class), anyBoolean());
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        var transportId1 = transportRepository
                .findAll()
                .stream()
                .filter(ts -> ts.getOrganizations().stream()
                                .anyMatch(org -> TRANSPORT_CREATE_DTO_1.organizations().get(0).organizationId().equals(org.getId())))
                .findFirst()
                .get()
                .getId();

        mockMvc.perform(patch(ROOT_PATH + "/deactivate/" + transportId1.toString())
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "exploitationEnd": 1672617600000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Дата не может быть меньше даты начала эксплуатации"));
        
        mockMvc.perform(patch(ROOT_PATH + "/deactivate/" + transportId1.toString())
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "exploitationEnd": 1698883200000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Дата не может быть больше текущей даты"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    @DisplayName("Поиск транспортных средств по всем организациям")
    @SneakyThrows
    void searchAllOrganizations(String testCase, Role role, String requestBody, List<ResultMatcher> resultMatchers) {
        testSearching(testCase, role, requestBody, resultMatchers, "/all-organizations");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    @DisplayName("Поиск транспортных средств по организации пользователя")
    @SneakyThrows
    void searchSelfOrganizations(String testCase, Role role, String requestBody, List<ResultMatcher> resultMatchers) {
        testSearching(testCase, role, requestBody, resultMatchers, "/self-organizations");
    }

    @Test
    @DisplayName("Поиск транспортных средств по организации пользователя - не пользовательское подразделение и не дочернее")
    @SneakyThrows
    void searchSelfOrganizationsWithNotEmployeeDepartmentException() {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_3.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_5)))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_3.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_2)))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_3.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_6)))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_3.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_9)))
                .andExpect(status().isOk());

        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());

        mockMvc.perform(post(ROOT_PATH + "/self-organizations")
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_3.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 1
                                        },
                                        "departmentId": "%s"
                                    }
                                """, DEPARTMENT_2)
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", equalTo(
                        "Подразделение 000098ba-5c12-423f-ba6a-de9c76db31a5 не принадлежит организации fc73b25b-9564-4560-98b5-abc0f16af9b2")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    @SneakyThrows
    void searching(String testCase, Role role, String requestBody, List<ResultMatcher> resultMatchers) {
        testSearching(testCase, role, requestBody, resultMatchers, "/search");
    }

    @SneakyThrows
    void testSearching(String testCase, Role role, String requestBody, List<ResultMatcher> resultMatchers, String mapping) {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        
        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_5)))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_2)))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_6)))
                .andExpect(status().isOk());

        mockMvc.perform(post(ROOT_PATH)
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_9)))
                .andExpect(status().isOk());
        
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, role.name());

        if (testCase.equals("Поиск авто по бренду")) {
            var searchRequest = objectMapper.readValue(requestBody, TransportSearchingRequestDto.class);
            requestBody = objectMapper.writeValueAsString(TransportSearchingRequestDto.builder()
                    .brand(UUID.fromString("c896bb1a-593e-4355-bb19-d8f64f84f226"))
                    .page(searchRequest.page())
                    .build()
            );
        } else if (testCase.equals("Поиск авто по модели")) {
            var searchRequest = objectMapper.readValue(requestBody, TransportSearchingRequestDto.class);
            requestBody = objectMapper.writeValueAsString(TransportSearchingRequestDto.builder()
                    .model(UUID.fromString("c8489954-548b-4a44-a621-aabce28da502"))
                    .page(searchRequest.page())
                    .build()
            );
        } else if (testCase.equals("Поиск по идентификатору контрагента и филиала")) {
            var searchRequest = objectMapper.readValue(requestBody, TransportSearchingRequestDto.class);
            requestBody = objectMapper.writeValueAsString(TransportSearchingRequestDto.builder()
                    .page(searchRequest.page())
                    .contractorIds(List.of(TRANSPORT_CREATE_DTO_6.contractorId()))
                    .autoparkId(TRANSPORT_CREATE_DTO_6.autoparkId())
                    .build());
        }
        var resultAction = mockMvc.perform(post(ROOT_PATH + mapping)
                                                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                                                   .authorities(new SimpleGrantedAuthority(role.name())))
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(requestBody));
        for (var resultMatcher : resultMatchers) {
            resultAction.andExpect(resultMatcher);
        }
    }

    static Stream<Arguments> searchAllOrganizations() {
        return searchingMethodSource(Role.ROLE_DISPATCHER_SUPPORT_SERVICE, true, Collections.emptyList());
    }

    static Stream<Arguments> searchSelfOrganizations() {
        return Stream.concat(searchingMethodSource(Role.ROLE_ADMIN_CORP_CLIENT, false, Collections.emptyList()),
                searchingMethodSource(Role.ROLE_ENGINEER_CORP_CLIENT, false, Collections.emptyList()));
    }

    static Stream<Arguments> searchOrganizationsWithoutOrganizationsRole() {
        return searchingMethodSource(Role.ROLE_ADMIN_DATA_MASTER, true, Collections.emptyList()).limit(1);
    }

    static Stream<Arguments> searching() {
        return searchingMethodSource(Role.ROLE_ENGINEER_CORP_CLIENT, true, List.of(Role.ROLE_ADMIN_CORP_CLIENT, Role.ROLE_DISPATCHER_CONTRACTOR));
    }

    private static Stream<Arguments> searchingMethodSource(Role role, boolean searchAllOrganizations, List<Role> additionalRoles) {
        return Stream.of(
                Arguments.of(
                        "Поиск с правами инженера, без текста поиска",
                        role,
                        """
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 10
                                        }
                                    }
                                """,
                        getResultMatchers(searchAllOrganizations,
                                List.of("А103АА99", "А133АА100", "А133АА99", "А139АА91"), List.of("А133АА99", "А139АА91"))
                ),
                Arguments.of(
                        "Поиск первого авто по части номера",
                        role,
                        """
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 10
                                        },
                                        "searchText": "А99"
                                    }
                                """,
                        getResultMatchers(searchAllOrganizations, List.of("А103АА99", "А133АА99"), List.of("А133АА99"))
                ),
                Arguments.of(
                        "Поиск второго авто по части номера",
                        role,
                        """
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 10
                                        },
                                        "searchText": "А100"
                                    }
                                """,
                        getResultMatchers(searchAllOrganizations, Collections.emptyList(), Collections.emptyList())
                ),
                Arguments.of(
                        "Поиск второго авто по части номера в поле stateNumber",
                        role,
                        """
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 10
                                        },
                                        "stateNumber": "А100"
                                    }
                                """,
                        getResultMatchers(searchAllOrganizations, Collections.emptyList(), Collections.emptyList())
                ),
                Arguments.of(
                        "Поиск первого авто по части VIN кода",
                        role,
                        """
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 10
                                        },
                                        "searchText": "BA4711000781798"
                                    }
                                """,
                        getResultMatchers(searchAllOrganizations, List.of("А133АА99"), List.of("А133АА99"))
                ),
                Arguments.of(
                        "Поиск второго авто по части VIN кода",
                        role,
                        """
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 10
                                        },
                                        "searchText": "7910"
                                    }
                                """,
                        getResultMatchers(searchAllOrganizations, Collections.emptyList(), Collections.emptyList())
                ),
                Arguments.of(
                        "Поиск обоих авто по части гос номера ",
                        role,
                        """
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 10
                                        },
                                        "searchText": "А133А"
                                    }
                                """,
                        getResultMatchers(searchAllOrganizations, List.of("А133АА100","А133АА99"), List.of("А133АА99"))
                ),
                Arguments.of(
                        "Поиск с правами клиента",
                        additionalRoles.isEmpty() ? role : additionalRoles.getFirst(),
                        """
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 10
                                        },
                                        "searchText": "А133А"
                                    }
                                """,
                        getResultMatchers(searchAllOrganizations, List.of("А133АА100","А133АА99"), List.of("А133АА99"))
                ),
                Arguments.of(
                        "Проверка результата пагинации, при наличии разбиения на несколько страниц",
                        role,
                        """
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 1
                                        }
                                    }
                                """,

                        List.of(status().isOk(),
                                jsonPath("$.content", hasSize(1)),
                                jsonPath("$.totalPages", is(searchAllOrganizations ? 4 : 2)))
                ),
                Arguments.of(
                        "Проверка первого авто по ид организации",
                        role,
                        String.format("""
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 1
                                        },
                                        "organizationId": "%s"
                                    }
                                """, ORGANIZATION_1),

                        List.of(status().isOk(),
                                jsonPath("$.content", hasSize(1)),
                                jsonPath("$.content[*].stateNumber", is(equalTo(List.of("А133АА99")))))
                ),
                Arguments.of(
                        "Поиск первого авто по ид департамента",
                        role,
                        String.format("""
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 1
                                        },
                                        "departmentId": "%s"
                                    }
                                """, DEPARTMENT_1),

                        List.of(status().isOk(),
                                jsonPath("$.content", hasSize(1)),
                                jsonPath("$.content[*].stateNumber", is(equalTo(List.of("А133АА99")))))
                ),
                Arguments.of(
                        "Поиск первого авто по ид родительского департамента",
                        role,
                        String.format("""
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 1
                                        },
                                        "departmentId": "%s"
                                    }
                                """, DEPARTMENT_3),

                        List.of(status().isOk(),
                                jsonPath("$.content", hasSize(1)),
                                jsonPath("$.content[*].stateNumber", is(equalTo(List.of("А133АА99")))))
                            ),
                Arguments.of(
                        "Поиск авто по статусу IN_USE",
                        role,
                        String.format("""
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 1
                                        },
                                        "status": "%s"
                                    }
                                """, TransportStatus.IN_USE),
                        
                        List.of(status().isOk(),
                                jsonPath("$.content", hasSize(1))
                            )),
                Arguments.of(
                        "Поиск авто по модели",
                        role,
                                """
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 10
                                        }
                                    }
                                """,
                        
                        List.of(status().isOk(),
                                jsonPath("$.content", hasSize(1)),
                                jsonPath("$.content[*].stateNumber", is(equalTo(List.of("А139АА91"))))
                               )),
                Arguments.of(
                        "Поиск авто по бренду",
                        role,
                        """
                            {
                                "page": {
                                    "page": 0,
                                    "size": 10
                                }
                            }
                        """,

                        List.of(status().isOk(),
                                jsonPath("$.content", hasSize(searchAllOrganizations ? 3 : 2))
                        )),
                Arguments.of(
                        "Поиск обоих авто по году",
                        role,
                        String.format("""
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 1
                                        },
                                        "year": "%s"
                                    }
                                """, 2023),
                        
                        List.of(status().isOk(),
                                jsonPath("$.content", hasSize(1))
                               )),
                Arguments.of(
                        "Поиск по идентификатору контрагента и филиала",
                        additionalRoles.isEmpty() ? role : additionalRoles.get(1),
                        String.format("""
                                    {
                                        "page": {
                                            "page": 0,
                                            "size": 1
                                        },
                                        "contractorId": "%s",
                                        "autoparkId": "%s"
                                    }
                                """, UUID.randomUUID(), UUID.randomUUID()),

                        getResultMatchers(searchAllOrganizations, Collections.emptyList(), Collections.emptyList())
                )
        );
    }

    @NotNull
    private static List<ResultMatcher> getResultMatchers(boolean searchAllOrganizations,
                                                         List<String> allOrganizationsStateNumbers,
                                                         List<String> selfOrganizationsStateNumbers) {
        var resultMatchers = new ArrayList<ResultMatcher>();
        resultMatchers.add(status().isOk());
        var stateNumbers = searchAllOrganizations ? allOrganizationsStateNumbers : selfOrganizationsStateNumbers;
        if (stateNumbers.isEmpty()) {
            if (searchAllOrganizations) {
                resultMatchers.add(jsonPath("$.content", hasSize(1)));
            } else {
                resultMatchers.add(jsonPath("$.content", hasSize(0)));
            }
        } else {
            resultMatchers.addAll(List.of(
                    jsonPath("$.content", hasSize(stateNumbers.size())),
                    jsonPath("$.content[*].stateNumber", is(equalTo(stateNumbers)))));
        }
        return resultMatchers;
    }

    @ParameterizedTest(name = "{0}")
    @SneakyThrows
    @MethodSource
    void searchByStateNumber(String testName, UUID employeeId, Role role, String body, List<ResultMatcher> matchers) {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        
        mockMvc.perform(post(ROOT_PATH)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_1)))
               .andExpect(status().isOk());
    
        mockMvc.perform(post(ROOT_PATH)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_2.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_2)))
               .andExpect(status().isOk());
        
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, role.name());
    
        var resultAction = mockMvc.perform(post(ROOT_PATH + "/statenumber")
                                                   .with(jwt().jwt(builder -> builder.jti(employeeId.toString()))
                                                              .authorities(new SimpleGrantedAuthority(role.name())))
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content(body));
        for (var resultMatcher : matchers) {
            resultAction.andExpect(resultMatcher);
        }
    }
    
    @Test
    @SneakyThrows
    void searchWithStructure() {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        
        mockMvc.perform(post(ROOT_PATH)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_3.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_1)))
               .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_3.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_2)))
               .andExpect(status().isOk());
        mockMvc.perform(post(ROOT_PATH)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_3.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(TRANSPORT_CREATE_DTO_3)))
               .andExpect(status().isOk());
        var searchDto = """
                            {
                                "page": {
                                    "page": 0,
                                    "size": 10
                                },
                                "searchText": "А99"
                            }
                        """;
        mockMvc.perform(post(ROOT_PATH + "/search/structure")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_3.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(searchDto))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content", hasSize(2)))
               .andExpect(jsonPath("$.content[*].stateNumber", contains("А133АА99", "А993АА77")));
    }

    @SneakyThrows
    @Test
    void getAccessiblePositions() {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_CORP_CLIENT.name());
        var result =  mockMvc.perform(get(ROOT_PATH + "/accessible-positions")
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk())
                .andReturn();
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<AccessiblePositionDto>>(){});
        assertEquals(15, response.size());
        assertEquals("Заместитель председателя ТБ", response.stream().sorted(Comparator.comparing(AccessiblePositionDto::title)).toList().get(0).title());

    }
    
    static Stream<Arguments> searchByStateNumber() {
        return Stream.of(
                Arguments.of(
                        "Проверка поиска по гос.номеру",
                        EMPLOYEE_1,
                        Role.ROLE_ADMIN_CORP_CLIENT,
                        """
                        {
                        	"stateNumber": "АА9",
                        	"page": {
                        		"page": 0,
                        		"size": 10
                        	}
                        }
                        """,
                        List.of(status().isOk(),
                                jsonPath("$.content", hasSize(1)),
                                jsonPath("$.content[*].stateNumber", contains("А133АА99")))
                            ),
                Arguments.of(
                        "Проверка поиска по гос.номеру с ролью ROLE_ADMIN_CORP_CLIENT",
                        EMPLOYEE_1,
                        Role.ROLE_ADMIN_CORP_CLIENT,
                        """
                        {
                        	"stateNumber": "3АА",
                        	"page": {
                        		"page": 0,
                        		"size": 10
                        	}
                        }
                        """,
                        List.of(status().isOk(),
                                jsonPath("$.content", hasSize(2)),
                                jsonPath("$.content[*].stateNumber", containsInAnyOrder("А133АА99", "А133АА100")))
                            ),
                Arguments.of(
                        "Проверка поиска по гос.номеру с ролью ROLE_DISPATCHER_SUPPORT_SERVICE",
                        EMPLOYEE_2,
                        Role.ROLE_DISPATCHER_SUPPORT_SERVICE,
                        """
                        {
                        	"stateNumber": "3АА",
                        	"page": {
                        		"page": 0,
                        		"size": 10
                        	}
                        }
                        """,
                        List.of(status().isOk(),
                                jsonPath("$.content", hasSize(2)),
                                jsonPath("$.content[*].stateNumber", containsInAnyOrder("А133АА99", "А133АА100")))
                            )
                        );
    }
    
    
    private static TransportCreateDto prepareCreateRequestOrg1() {
        return new TransportCreateDto(
                Collections.singletonList(new OrganizationRequestDto(ORGANIZATION_1, DEPARTMENT_1)),
                new LocationRequestDto(LocalDateTime.parse("2023-02-09T00:00:00"),
                                           "г. Москва, ул. Московская, д. 1", "г. Москва, ул. Московская, д. 1"),
                "Комментарий",
                UUID.fromString("4e2a5e47-4a03-46c8-9491-59369f241475"),
                new VehicleCreateDto(VEHICLE_VAZ, "А133АА99", 2023, 2000,
                        "WBA47110007817985", "2423234234", "542353454332",
                        "4534523123412", "453453532453",
                        fromString("c5964d64-7b98-4978-bf93-3c48d38c3d7c"),
                        fromString("60b9b87e-3a4c-4500-acc4-60dbea5eec46"), "White"),
                new DocumentsCreateDto("12345", LocalDateTime.parse("2023-01-09T00:00:00"),
                        "Тойота", "Ленд круизер", "1312312313",
                        LocalDateTime.parse("2023-03-09T00:00:00"), "Легковой"),
                null,
                null,
                "0077",
                "0078",
                "0079"
        );
    }

    private static TransportCreateDto prepareCreateRequestOrg2() {
        return new TransportCreateDto(
                Collections.singletonList(new OrganizationRequestDto(ORGANIZATION_2, DEPARTMENT_2)),
                new LocationRequestDto(LocalDateTime.parse("2023-01-09T00:00:00"),
                                           "г. Москва, ул. Московская, д. 1", "г. Москва, ул. Московская, д. 1"),
                null,
                UUID.fromString("4e2a5e47-4a03-46c8-9491-59369f241475"),
                new VehicleCreateDto(VEHICLE_CHANGAN, "А133АА100", 2023, 2000,
                        "WBA47110007817910", "2423234210", "542353454310",
                        "4534523123410", "453453532410",
                        fromString("c5964d64-7b98-4978-bf93-3c48d38c3d7c"), null, "White"),
                new DocumentsCreateDto("12345", LocalDateTime.parse("2023-01-09T00:00:00"),
                        "Не Тойота", "Не Ленд круизер", "1312312310",
                        LocalDateTime.parse("2023-01-09T00:00:00"), "Легковой"),
                null,
                null,
                "0077",
                "0078",
                "0079"
        );
    }
    
    private static TransportCreateDto prepareCreateRequestOrg3() {
        return new TransportCreateDto(
                Collections.singletonList(new OrganizationRequestDto(ORGANIZATION_1, DEPARTMENT_3)),
                new LocationRequestDto(LocalDateTime.parse("2023-01-09T00:00:00"),
                                           "г. Москва, ул. Московская, д. 1", "г. Москва, ул. Московская, д. 1"),
                null,
                UUID.fromString("4e2a5e47-4a03-46c8-9491-59369f241475"),
                new VehicleCreateDto(VEHICLE_CHANGAN, "А993АА77", 2023, 2000,
                                     "WBA47110007817922", "2423234431", "542353455362",
                                     "4534541223410", "453457427410",
                                     fromString("c5964d64-7b98-4978-bf93-3c48d38c3d7c"), null, "White"),
                new DocumentsCreateDto("73654", LocalDateTime.parse("2023-01-09T00:00:00"),
                                       "Не Тойота", "Не Ленд круизер", "4722312310",
                                       LocalDateTime.parse("2023-01-09T00:00:00"), "Легковой"),
                null,
                null,
                "0077",
                "0078",
                "0079"
        );
    }
    
    private static TransportCreateDto prepareCreateRequestOrg4() {
        return new TransportCreateDto(
                Collections.singletonList(new OrganizationRequestDto(ORGANIZATION_2, DEPARTMENT_3)),
                new LocationRequestDto(LocalDateTime.parse("2023-01-09T00:00:00"),
                                       "г. Москва, ул. Московская, д. 1", "г. Москва, ул. Московская, д. 1"),
                null,
                UUID.fromString("4e2a5e47-4a03-46c8-9491-59369f241475"),
                new VehicleCreateDto(VEHICLE_CHANGAN, "А993АА77", 2023, 2000,
                                     "WBA47110007817922", "2423234431", "542353455362",
                                     "4534541223410", "453457427410",
                                     fromString("c5964d64-7b98-4978-bf93-3c48d38c3d7c"), null, "White"),
                new DocumentsCreateDto("73654", LocalDateTime.parse("2023-01-09T00:00:00"),
                                       "Не Тойота", "Не Ленд круизер", "4722312310",
                                       LocalDateTime.parse("2023-01-09T00:00:00"), "Легковой"),
                null,
                null,
                "0077",
                "0078",
                "0079"
        );
    }

    private static TransportCreateDto prepareCreateRequestOrg5() {
        return new TransportCreateDto(
                Collections.singletonList(new OrganizationRequestDto(ORGANIZATION_1, DEPARTMENT_1)),
                new LocationRequestDto(LocalDateTime.parse("2023-02-09T00:00:00"),
                        "г. Москва, ул. Московская, д. 1", "г. Москва, ул. Московская, д. 1"),
                "Комментарий",
                UUID.fromString("4e2a5e47-4a03-46c8-9491-59369f241475"),
                new VehicleCreateDto(VEHICLE_VAZ, "А133АА99", 2023, 2000,
                        "WBA47110007817985", "2423234234", "542353454332",
                        "4534523123412", "453453532453",
                        fromString("c5964d64-7b98-4978-bf93-3c48d38c3d7c"),
                        fromString("60b9b87e-3a4c-4500-acc4-60dbea5eec46"), "White"),
                new DocumentsCreateDto("12345", LocalDateTime.parse("2023-01-09T00:00:00"),
                        "Лада", "2114", "1312312313",
                        LocalDateTime.parse("2023-03-09T00:00:00"), "Легковой"),
                null,
                null,
                "0077",
                "0078",
                "0079"
        );
    }

    private static TransportCreateDto prepareCreateRequestOrg6() {
        return new TransportCreateDto(
                Collections.emptyList(),
                new LocationRequestDto(LocalDateTime.parse("2023-02-09T00:00:00"),
                        "г. Москва, ул. Московская, д. 1", "г. Москва, ул. Московская, д. 1"),
                "Комментарий",
                UUID.fromString("4e2a5e47-4a03-46c8-9491-59369f241475"),
                new VehicleCreateDto(VEHICLE_VAZ, "А103АА99", 2023, 2000,
                        "WBA47110107817985", "2423204234", "542353404332",
                        "4534520123412", "453453502453",
                        fromString("c5964d64-7b98-4978-bf93-3c48d38c3d7c"),
                        fromString("60b9b87e-3a4c-4500-acc4-60dbea5eec46"), "White"),
                new DocumentsCreateDto("12305", LocalDateTime.parse("2023-01-09T00:00:00"),
                        "Тойота", "Ленд круизер", "1312302313",
                        LocalDateTime.parse("2023-03-09T00:00:00"), "Легковой"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "0077",
                "0078",
                "0079"
        );
    }

    private static TransportCreateDto prepareCreateRequestOrg7() {
        return new TransportCreateDto(
                Collections.emptyList(),
                new LocationRequestDto(LocalDateTime.parse("2023-02-09T00:00:00"),
                        "г. Москва, ул. Московская, д. 1", "г. Москва, ул. Московская, д. 1"),
                "Комментарий",
                UUID.fromString("4e2a5e47-4a03-46c8-9491-59369f241475"),
                new VehicleCreateDto(VEHICLE_VAZ, "А103АА99", 2023, 2000,
                        "WBA47110107817985", "2423204234", "542353404332",
                        "4534520123412", "453453502453",
                        fromString("c5964d64-7b98-4978-bf93-3c48d38c3d7c"),
                        fromString("60b9b87e-3a4c-4500-acc4-60dbea5eec46"), "White"),
                new DocumentsCreateDto("12305", LocalDateTime.parse("2023-01-09T00:00:00"),
                        "Тойота", "Ленд круизер", "1312302313",
                        LocalDateTime.parse("2023-03-09T00:00:00"), "Легковой"),
                null,
               null,
                "0077",
                "0078",
                "0079"
        );
    }

    private static TransportCreateDto prepareCreateRequestOrg8() {
        return new TransportCreateDto(
                Collections.singletonList(new OrganizationRequestDto(ORGANIZATION_1, DEPARTMENT_1)),
                new LocationRequestDto(LocalDateTime.parse("2023-02-09T00:00:00"),
                        "г. Москва, ул. Московская, д. 1", "г. Москва, ул. Московская, д. 1"),
                "Комментарий",
                UUID.fromString("4e2a5e47-4a03-46c8-9491-59369f241475"),
                new VehicleCreateDto(VEHICLE_VAZ, "А103АА99", 2023, 2000,
                        "WBA47110107817985", "2423204234", "542353404332",
                        "4534520123412", "453453502453",
                        fromString("c5964d64-7b98-4978-bf93-3c48d38c3d7c"),
                        fromString("60b9b87e-3a4c-4500-acc4-60dbea5eec46"), "White"),
                new DocumentsCreateDto("12305", LocalDateTime.parse("2023-01-09T00:00:00"),
                        "Тойота", "Ленд круизер", "1312302313",
                        LocalDateTime.parse("2023-03-09T00:00:00"), "Легковой"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "0077",
                "0078",
                "0079"
        );
    }

    private static TransportCreateDto prepareCreateRequestOrg9() {
        return new TransportCreateDto(
                Collections.singletonList(new OrganizationRequestDto(ORGANIZATION_1, DEPARTMENT_1)),
                new LocationRequestDto(LocalDateTime.parse("2023-02-09T00:00:00"),
                        "г. Москва, ул. Московская, д. 1", "г. Москва, ул. Московская, д. 1"),
                "Комментарий",
                UUID.fromString("4e2a5e47-4a03-46c8-9491-59369f241475"),
                new VehicleCreateDto(VEHICLE_VAZ2, "А139АА91", 2023, 2000,
                        "WBA97110107817985", "24232042349", "542353404332",
                        "4534520123412", "453453502453",
                        fromString("c5964d64-7b98-4978-bf93-3c48d38c3d7c"),
                        fromString("60b9b87e-3a4c-4500-acc4-60dbea5eec46"), "White"),
                new DocumentsCreateDto("12305", LocalDateTime.parse("2023-01-09T00:00:00"),
                        "Тойота", "Ленд круизер", "1312302313",
                        LocalDateTime.parse("2023-03-09T00:00:00"), "Легковой"),
                null,
                null,
                "0077",
                "0078",
                "0079"
        );
    }

    private static TransportUpdateDto prepareUpdateRequestOrg1() {
        return new TransportUpdateDto(
                Collections.singletonList(new OrganizationRequestDto(ORGANIZATION_1, DEPARTMENT_1)),
                new LocationRequestDto(LocalDateTime.parse("2023-01-09T00:00:00"),
                "г. Москва, ул. Московская, д. 1", "г. Москва, ул. Московская, д. 1"),
                "Комментарий",
                UUID.fromString("4e2a5e47-4a03-46c8-9491-59369f241475"),
                new VehicleUpdateDto(VEHICLE_CHANGAN, "А133АА99", "WBA47110007817985",
                        "2423234234", "542353454332", "4534523123412",
                        "453453532453", fromString("60b9b87e-3a4c-4500-acc4-60dbea5eec46"),
                        2001, UUID.fromString("c5964d64-7b98-4978-bf93-3c48d38c3d7c")),
                new DocumentsUpdateDto("1312312313", LocalDateTime.parse("2023-02-09T00:00:00"), "Легковой"),
                null,
                null,
                "0077",
                "0078",
                "0079"
        );
    }
    
    private static TransportUpdateDto prepareUpdateRequestOrg2() {
        return new TransportUpdateDto(
                Collections.singletonList(new OrganizationRequestDto(ORGANIZATION_2, DEPARTMENT_3)),
                new LocationRequestDto(LocalDateTime.parse("2023-01-09T00:00:00"),
                                       "г. Москва, ул. Московская, д. 1", "г. Москва, ул. Московская, д. 1"),
                "Комментарий",
                UUID.fromString("4e2a5e47-4a03-46c8-9491-59369f241475"),
                new VehicleUpdateDto(VEHICLE_CHANGAN, "А133АА99", "WBA47110007817985",
                                     "2423234234", "542353454332", "4534523123412",
                                     "453453532453", fromString("60b9b87e-3a4c-4500-acc4-60dbea5eec46"),
                        2001, UUID.fromString("c5964d64-7b98-4978-bf93-3c48d38c3d7c")),
                new DocumentsUpdateDto("1312312313", LocalDateTime.parse("2023-02-09T00:00:00"), "Легковой"),
                null,
                null,
                "0077",
                "0078",
                "0079"
        );
    }

    private static TransportUpdateDto prepareUpdateRequestOrg3() {
        return new TransportUpdateDto(
                Collections.emptyList(),
                new LocationRequestDto(LocalDateTime.parse("2023-02-09T00:00:00"),
                        "г. Москва, ул. Московская, д. 1", "г. Москва, ул. Московская, д. 1"),
                "Комментарий",
                UUID.fromString("4e2a5e47-4a03-46c8-9491-59369f241475"),
                new VehicleUpdateDto(VEHICLE_VAZ, "А103АА99",
                        "WBA47110107817985", "2423204234", "542353404332",
                        "4534520123412", "453453502453",
                        fromString("c5964d64-7b98-4978-bf93-3c48d38c3d7c"),
                        2001, UUID.fromString("c5964d64-7b98-4978-bf93-3c48d38c3d7c")),
                new DocumentsUpdateDto("12305", LocalDateTime.parse("2023-01-09T00:00:00"),
                        "Легковой"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "0077",
                "0078",
                "0079"
        );
    }

    private static void assertTransport1(TransportResponseDto transportResponseDto, UUID transportId) {
        SoftAssertions.assertSoftly(as -> {
            as.assertThat(transportResponseDto.id()).isEqualTo(transportId);
            as.assertThat(transportResponseDto.stateNumber()).isEqualTo("А133АА99");
            as.assertThat(transportResponseDto.brand()).isEqualTo("Лада");
            as.assertThat(transportResponseDto.model()).isEqualTo("2114");
            as.assertThat(transportResponseDto.year()).isEqualTo("2023");
            as.assertThat(transportResponseDto.currentMileage()).isEqualTo(2000);
            as.assertThat(transportResponseDto.comment()).isEqualTo("Комментарий");
            as.assertThat(transportResponseDto.balanceUnitNumber()).isEqualTo("0077");
            as.assertThat(transportResponseDto.facility()).isEqualTo("0078");
            as.assertThat(transportResponseDto.equipmentUnitSystemNumber()).isEqualTo("0079");
            as.assertThat(transportResponseDto.vehicle().id()).isEqualTo(VEHICLE_VAZ);
            as.assertThat(transportResponseDto.vehicle().status()).isEqualTo(TransportStatus.IN_USE);
            as.assertThat(transportResponseDto.vehicle().manufacturer()).isEqualTo("ВАЗ");
            as.assertThat(transportResponseDto.vehicle().vinCode()).isEqualTo("WBA47110007817985");
            as.assertThat(transportResponseDto.vehicle().assetNumber()).isEqualTo("2423234234");
            as.assertThat(transportResponseDto.vehicle().inventoryNumber()).isEqualTo("542353454332");
            as.assertThat(transportResponseDto.vehicle().bodyNumber()).isEqualTo("4534523123412");
            as.assertThat(transportResponseDto.vehicle().chassisNumber()).isEqualTo("453453532453");
            as.assertThat(transportResponseDto.vehicle().type()).isEqualTo("Запасной");
            as.assertThat(transportResponseDto.vehicle().subtype()).isEqualTo("На всякий");
            as.assertThat(transportResponseDto.vehicle().drive()).isEqualTo("Передний");
            as.assertThat(transportResponseDto.vehicle().ecologicalClass()).isEqualTo("4");
            as.assertThat(transportResponseDto.vehicle().bodyType()).isEqualTo("Купе");
            as.assertThat(transportResponseDto.vehicle().transmissionType()).isEqualTo("МКПП 7");
            as.assertThat(transportResponseDto.vehicle().manufacturePeriod()).isEqualTo("1980 - н.в.");
            as.assertThat(transportResponseDto.organizations()).containsExactlyInAnyOrder(
                    new OrganizationResponseDto(
                            ORGANIZATION_1,
                            "Дальневосточный банк (ДВБ)",
                            DEPARTMENT_1,
                            "Отдел трансп обеспечения УРМ г. Магадан"));
            as.assertThat(transportResponseDto.location().exploitationStart()).isEqualTo("2023-02-09T00:00:00");
            as.assertThat(transportResponseDto.location().exploitationEnd()).isNull();
            as.assertThat(transportResponseDto.location().locationAddress()).isEqualTo("г. Москва, ул. Московская, д. 1");
            as.assertThat(transportResponseDto.location().parkingAddress()).isEqualTo("г. Москва, ул. Московская, д. 1");
            as.assertThat(transportResponseDto.documents().passportNumber()).isEqualTo("12345");
            as.assertThat(transportResponseDto.documents().passportIssuedDate()).isEqualTo("2023-01-09T00:00:00");
            as.assertThat(transportResponseDto.documents().brandByPassport()).isEqualTo("Тойота");
            as.assertThat(transportResponseDto.documents().modelByPassport()).isEqualTo("Ленд круизер");
            as.assertThat(transportResponseDto.documents().certificateNumber()).isEqualTo("1312312313");
            as.assertThat(transportResponseDto.documents().certificateIssuedDate()).isEqualTo("2023-03-09T00:00:00");
            as.assertThat(transportResponseDto.engine().engineType()).isEqualTo("БЕНЗИН");
            as.assertThat(transportResponseDto.engine().engineCapacity()).isEqualTo(1400);
            as.assertThat(transportResponseDto.engine().enginePower()).isEqualTo(BigDecimal.valueOf(240.33));
            as.assertThat(transportResponseDto.engine().fuelType()).isEqualTo("АИ-98, ЭЛЕКТРИЧЕСТВО");
            as.assertThat(transportResponseDto.general().height()).isEqualTo(1400);
            as.assertThat(transportResponseDto.general().width()).isEqualTo(1650);
            as.assertThat(transportResponseDto.general().length()).isEqualTo(4122);
            as.assertThat(transportResponseDto.general().weight()).isEqualTo(700);
            as.assertThat(transportResponseDto.general().maxWeight()).isEqualTo(900);
            as.assertThat(transportResponseDto.general().bodyColor()).isEqualTo("White");
            as.assertThat(transportResponseDto.general().telematics()).isEqualTo("Santel");
            as.assertThat(transportResponseDto.general().category()).isEqualTo("В");
            as.assertThat(transportResponseDto.general().categoryName()).isEqualTo("Легковые автомобили, небольшие грузовики (до 3,5 тонн)");
            as.assertThat(transportResponseDto.general().fuelTankVolume()).isEqualTo(50);
            as.assertThat(transportResponseDto.general().spareWheelHolderInstalled()).isEqualTo(false);
            as.assertThat(transportResponseDto.general().mudguardInstalled()).isEqualTo(true);
            as.assertThat(transportResponseDto.general().mudguardInstalled()).isEqualTo(true);
            as.assertThat(transportResponseDto.general().frontWheelSize()).isEqualTo("185/55R15 81V");
            as.assertThat(transportResponseDto.general().rearWheelSize()).isEqualTo("185/55R15 81V");
            as.assertThat(transportResponseDto.service().serviceIntervalMileage()).isEqualTo(180);
            as.assertThat(transportResponseDto.service().serviceIntervalDays()).isEqualTo(10000);
            as.assertThat(transportResponseDto.service().serviceAuthorizationMileage()).isEqualTo(2000);
            as.assertThat(transportResponseDto.service().serviceAuthorizationDays()).isEqualTo(10);
            as.assertThat(transportResponseDto.documents().vehicleType()).isEqualTo("Легковой");
        });
    }
}
