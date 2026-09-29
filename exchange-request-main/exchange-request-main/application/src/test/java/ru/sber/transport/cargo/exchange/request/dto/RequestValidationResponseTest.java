package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RequestValidationResponse should")
public class RequestValidationResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("create instance via constructor")
    void createViaConstructor() {
        UUID id = UUID.randomUUID();
        // Arrange & Act
        RequestShortDto requestShortDto = new RequestShortDto();
        requestShortDto.setId(id);

        RequestValidationResponse.ValidationInfo.ValidationError error =
                RequestValidationResponse.ValidationInfo.ValidationError.builder()
                        .field("waypoints")
                        .message("Минимум 2 точки маршрута должны быть указаны")
                        .build();

        RequestValidationResponse.ValidationInfo validationInfo = new RequestValidationResponse.ValidationInfo(
                true,
                List.of(error)
        );

        RequestValidationResponse.ErrorDetails errorDetails = new RequestValidationResponse.ErrorDetails(
                "VALIDATION_ERROR",
                "Ошибка валидации заявки"
        );

        RequestValidationResponse response = new RequestValidationResponse(
                requestShortDto,
                validationInfo,
                true,
                errorDetails
        );

        // Assert
        assertThat(response.getRequest().getId()).isEqualTo(id);
        assertThat(response.getValidation().getIsValidForPublication()).isTrue();
        assertThat(response.getValidation().getErrors()).hasSize(1);
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getError().getType()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("create instance via builder")
    void createViaBuilder() {
        // Arrange & Act
        UUID id = UUID.randomUUID();
        RequestValidationResponse response = RequestValidationResponse.builder()
                .request(RequestShortDto.builder().id(id).build())
                .validation(RequestValidationResponse.ValidationInfo.builder()
                        .isValidForPublication(false)
                        .errors(List.of(
                                RequestValidationResponse.ValidationInfo.ValidationError.builder()
                                        .field("weight")
                                        .message("Превышен максимальный вес груза")
                                        .build()
                        ))
                        .build())
                .isSuccess(false)
                .error(RequestValidationResponse.ErrorDetails.builder()
                        .type("BUSINESS_ERROR")
                        .message("Ошибка бизнес-логики")
                        .build())
                .build();

        // Assert
        assertThat(response.getRequest().getId()).isEqualTo(id);
        assertThat(response.getValidation().getIsValidForPublication()).isFalse();
        assertThat(response.getValidation().getErrors()).extracting("field")
                .containsExactly("weight");
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getMessage()).contains("бизнес");
    }

    @Test
    @DisplayName("serialize and deserialize correctly with Jackson")
    void serializeAndDeserializeWithJackson() throws JsonProcessingException {
        // Arrange
        RequestValidationResponse response = RequestValidationResponse.builder()
                .request(new RequestShortDto())
                .validation(RequestValidationResponse.ValidationInfo.builder()
                        .isValidForPublication(true)
                        .errors(List.of(
                                RequestValidationResponse.ValidationInfo.ValidationError.builder()
                                        .field("price")
                                        .message("Цена не указана")
                                        .build()
                        ))
                        .build())
                .isSuccess(true)
                .error(RequestValidationResponse.ErrorDetails.builder()
                        .type("MISSING_DATA")
                        .message("Не хватает данных")
                        .build())
                .build();

        // Act
        String json = objectMapper.writeValueAsString(response);
        RequestValidationResponse deserialized = objectMapper.readValue(json, RequestValidationResponse.class);

        // Assert
        assertThat(deserialized.isSuccess()).isEqualTo(response.isSuccess());
        assertThat(deserialized.getValidation().getIsValidForPublication())
                .isEqualTo(response.getValidation().getIsValidForPublication());
        assertThat(deserialized.getValidation().getErrors()).hasSize(1);
        assertThat(deserialized.getError().getType()).isEqualTo("MISSING_DATA");

        // Проверим, что @JsonProperty("isValidForPublication") работает при десериализации
        assertThat(json).contains("isValidForPublication");
    }
}
