package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WaypointContactDto should")
public class WaypointContactDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("be created via constructor with all fields")
    void createViaConstructor() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID waypointId = UUID.randomUUID();
        String organizationId = "12341243";

        WaypointContactDto dto = new WaypointContactDto(
                id,
                waypointId,
                organizationId,
                "Иван Петров",
                "+7 (999) 123-45-67",
                "ivan.petrov@company.com"
        );

        // Assert
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getWaypointId()).isEqualTo(waypointId);
        assertThat(dto.getOrganizationInn()).isEqualTo(organizationId);
        assertThat(dto.getContactPerson()).isEqualTo("Иван Петров");
        assertThat(dto.getContactPhone()).isEqualTo("+7 (999) 123-45-67");
        assertThat(dto.getContactEmail()).isEqualTo("ivan.petrov@company.com");
    }

    @Test
    @DisplayName("be created via builder")
    void createViaBuilder() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID waypointId = UUID.randomUUID();
        String organizationId = "4241241241";

        WaypointContactDto dto = WaypointContactDto.builder()
                .id(id)
                .waypointId(waypointId)
                .organizationInn(organizationId)
                .contactPerson("Анна Смирнова")
                .contactPhone("+7 987 654-32-10")
                .contactEmail("a.smirnova@logistics.ru")
                .build();

        // Assert
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getWaypointId()).isEqualTo(waypointId);
        assertThat(dto.getOrganizationInn()).isEqualTo(organizationId);
        assertThat(dto.getContactPerson()).isEqualTo("Анна Смирнова");
        assertThat(dto.getContactPhone()).isEqualTo("+7 987 654-32-10");
        assertThat(dto.getContactEmail()).isEqualTo("a.smirnova@logistics.ru");
    }

    @Test
    @DisplayName("serialize and deserialize correctly with Jackson")
    void serializeAndDeserializeWithJackson() throws JsonProcessingException {
        // Arrange
        WaypointContactDto dto = WaypointContactDto.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .waypointId(UUID.fromString("223e4567-e89b-12d3-a456-426614174001"))
                .organizationInn("426614174002")
                .contactPerson("Дмитрий Козлов")
                .contactPhone("+7 900 100-20-30")
                .contactEmail("d.kozlov@partner.org")
                .build();

        // Act
        String json = objectMapper.writeValueAsString(dto);
        WaypointContactDto deserialized = objectMapper.readValue(json, WaypointContactDto.class);

        // Assert
        assertThat(deserialized.getId()).isEqualTo(dto.getId());
        assertThat(deserialized.getWaypointId()).isEqualTo(dto.getWaypointId());
        assertThat(deserialized.getOrganizationInn()).isEqualTo(dto.getOrganizationInn());
        assertThat(deserialized.getContactPerson()).isEqualTo("Дмитрий Козлов");
        assertThat(deserialized.getContactPhone()).isEqualTo("+7 900 100-20-30");
        assertThat(deserialized.getContactEmail()).isEqualTo("d.kozlov@partner.org");

        // Проверим, что JSON содержит ключевые поля
        assertThat(json).contains("id").contains("contactPerson").contains("contactEmail");
    }

    @Test
    @DisplayName("allow valid max lengths for string fields")
    void validateStringMaxLengths() {
        // maxLength: contactPerson — 200, contactPhone — 20, contactEmail — 100
        String longName = "A".repeat(200);
        String longPhone = "8".repeat(20);
        String longEmail = "a".repeat(100 - "@example.com".length()) + "@example.com";

        WaypointContactDto dto = WaypointContactDto.builder()
                .contactPerson(longName)
                .contactPhone(longPhone)
                .contactEmail(longEmail)
                .build();

        assertThat(dto.getContactPerson()).hasSize(200);
        assertThat(dto.getContactPhone()).hasSize(20);
        assertThat(dto.getContactEmail()).hasSize(100);
    }
}
