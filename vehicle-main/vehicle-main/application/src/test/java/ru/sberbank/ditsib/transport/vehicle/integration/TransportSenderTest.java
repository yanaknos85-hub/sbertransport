package ru.sberbank.ditsib.transport.vehicle.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.vehicle.constants.Role;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.LocationRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.OrganizationRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create.DocumentsCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create.TransportCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create.VehicleCreateDto;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.TransportMessage;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
class TransportSenderTest extends KafkaTest {

    private static final String CONTROLLER_URL = "/transport";
    private static final UUID EMPLOYEE_1 = fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8");
    private static final UUID ORGANIZATION_1 = fromString("fc73b25b-9564-4560-98b5-abc0f16af9b2");
    private static final UUID ENGINE_TYPE_1 = fromString("773b5013-ac57-45e9-9d0b-75221c3ce333");
    private static final UUID FUEL_TYPE_1 = fromString("67a89599-fa56-4329-b671-981f78883319");
    private static final UUID FUEL_TYPE_2 = fromString("e705ffd8-f158-4455-a005-5ea644614223");
    private static final UUID DEPARTMENT_1 = fromString("003a33fb-faa6-49a7-be37-106fdbef2324");
    private static final UUID VEHICLE_VAZ = fromString("748ba8ab-572a-4178-9e05-fae61aefb376");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Test
    @Sql({ "/scripts/vehicle_integration_test.sql", "/scripts/transport_integration_test.sql" })
    @SneakyThrows
    void addTransport() {
        mockMvc.perform(post(CONTROLLER_URL)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1.toString()))
                                           .authorities(new SimpleGrantedAuthority(Role.ROLE_ADMIN_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(prepareCreateRequestOrg1())))
               .andExpect(status().isOk());
        var message = consumeMessage("service.transport", TransportMessage.class);
        assertThat(message)
                .isNotNull()
                .extracting(TransportMessage::stateNumber,
                            TransportMessage::brand,
                            TransportMessage::model,
                            TransportMessage::transportType,
                            TransportMessage::year,
                            TransportMessage::vin,
                            TransportMessage::currentMileage,
                            TransportMessage::type,
                            TransportMessage::subtype,
                            TransportMessage::organizationIds,
                            TransportMessage::fuelTypeIds,
                            TransportMessage::engineTypeId,
                            TransportMessage::deleted)
                .containsExactly("А133АА99",
                                 "Тойота",
                                 "Ленд круизер",
                                 "Легковой",
                                 2023,
                                 "WBA47110007817985",
                                 2000,
                                 "Запасной",
                                 "На всякий",
                                 Set.of(ORGANIZATION_1),
                                 Set.of(FUEL_TYPE_1, FUEL_TYPE_2),
                                 ENGINE_TYPE_1,
                                 false
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
                "0079");
    }
}