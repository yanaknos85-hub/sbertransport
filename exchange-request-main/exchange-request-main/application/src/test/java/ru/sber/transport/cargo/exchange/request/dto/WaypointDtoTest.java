package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.cargo.exchange.request.enums.WaypointType.LOAD;
import static ru.sber.transport.cargo.exchange.request.enums.WaypointType.UNLOAD;

@DisplayName("WaypointDto should")
public class WaypointDtoTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("be created via constructor with all fields")
    void createViaConstructor() {
        // Arrange
        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 2, 2);
        LocalTime from = LocalTime.of(8, 0);
        LocalTime to = LocalTime.of(10, 0);
        AddressInfoDto address = AddressInfoDto.builder()
                .city("Москва")
                .street("Ленинская, 10")
                .latitude(55.7558)
                .longitude(37.6173)
                .build();
        WaypointContactDto contact = WaypointContactDto.builder()
                .contactPerson("Иван Иванов")
                .contactPhone("+7 999 123-45-67")
                .contactEmail("ivan@example.com")
                .build();

        // Act
        WaypointDto dto = new WaypointDto(
                id,
                0,
                500,
                LOAD,
                address,
                date,
                from,
                to,
                contact
        );

        // Assert
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getOrderingIndex()).isEqualTo(0);
        assertThat(dto.getRadius()).isEqualTo(500);
        assertThat(dto.getType()).isEqualTo(LOAD);
        assertThat(dto.getAddressInfo().getCity()).isEqualTo("Москва");
        assertThat(dto.getDate()).isEqualTo(date);
        assertThat(dto.getFrom()).isEqualTo(from);
        assertThat(dto.getTo()).isEqualTo(to);
        assertThat(dto.getContact().getContactPerson()).isEqualTo("Иван Иванов");
    }

    @Test
    @DisplayName("be created via builder")
    void createViaBuilder() {
        // Arrange
        LocalDate date = LocalDate.parse("2025-04-05");
        LocalTime from = LocalTime.parse("10:00");
        LocalTime to = LocalTime.parse("18:00");

        // Act
        WaypointDto dto = WaypointDto.builder()
                .id(UUID.randomUUID())
                .orderingIndex(1)
                .radius(250)
                .type(UNLOAD)
                .addressInfo(AddressInfoDto.builder()
                        .city("Санкт-Петербург")
                        .street("Невский проспект, 50")
                        .latitude(59.9343)
                        .longitude(30.3351)
                        .build())
                .date(date)
                .from(from)
                .to(to)
                .contact(WaypointContactDto.builder()
                        .contactPerson("Анна Петрова")
                        .contactPhone("+7 987 654-32-10")
                        .build())
                .build();

        // Assert
        assertThat(dto.getOrderingIndex()).isEqualTo(1);
        assertThat(dto.getRadius()).isEqualTo(250);
        assertThat(dto.getType()).isEqualTo(UNLOAD);
        assertThat(dto.getAddressInfo().getCity()).isEqualTo("Санкт-Петербург");
        assertThat(dto.getDate()).isEqualTo(date);
        assertThat(dto.getFrom()).isEqualTo(from);
        assertThat(dto.getTo()).isEqualTo(to);
        assertThat(dto.getContact().getContactPerson()).isEqualTo("Анна Петрова");
        assertThat(dto.getContact().getContactEmail()).isNull();
    }

    @Test
    @DisplayName("respect required fields: orderingIndex, radius, type, date, from, to must not be null")
    void validateRequiredFields() {
        // Этот тест проверяет, что обязательные поля действительно устанавливаются
        LocalDate date = LocalDate.now();
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(18, 0);

        WaypointDto dto = WaypointDto.builder()
                .orderingIndex(0)
                .radius(100)
                .type(LOAD)
                .date(date)
                .from(from)
                .to(to)
                .build();

        assertThat(dto.getOrderingIndex()).isNotNull();
        assertThat(dto.getRadius()).isNotNull();
        assertThat(dto.getType()).isNotNull();
        assertThat(dto.getDate()).isNotNull();
        assertThat(dto.getFrom()).isNotNull();
        assertThat(dto.getTo()).isNotNull();
    }

    @Test
    @DisplayName("serialize and deserialize correctly using Jackson")
    void serializeDeserialize() throws JsonProcessingException {
        // Arrange
        String json = """
            {
              "orderingIndex": 0,
              "radius": 500,
              "type": "LOAD",
              "addressInfo": {
                "city": "Москва",
                "street": "Ленинская, 10"
              },
              "date": "2026-02-02",
              "from": "08:00",
              "to": "10:00",
              "contact": {
                "contactPerson": "Иван Иванов",
                "contactPhone": "+7 999 123-45-67"
              }
            }
            """;

        // Act
        WaypointDto dto = objectMapper.readValue(json, WaypointDto.class);

        String serialized = objectMapper.writeValueAsString(dto);

        // Assert
        assertThat(dto.getOrderingIndex()).isEqualTo(0);
        assertThat(dto.getRadius()).isEqualTo(500);
        assertThat(dto.getType()).isEqualTo(LOAD);
        assertThat(dto.getAddressInfo().getCity()).isEqualTo("Москва");
        assertThat(dto.getDate()).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(dto.getFrom()).isEqualTo(LocalTime.of(8, 0));
        assertThat(dto.getTo()).isEqualTo(LocalTime.of(10, 0));
        assertThat(dto.getContact().getContactPerson()).isEqualTo("Иван Иванов");

        // Проверим, что сериализация обратно в JSON корректна
        assertThat(serialized).contains("2026-02-02");
        assertThat(serialized).contains("08:00");
        assertThat(serialized).contains("10:00");
    }
}