package ru.sber.transport.cargo.exchange.request.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseDtoTest {

    private static final String ERROR_TYPE = "VALIDATION_ERROR";
    private static final String MESSAGE = "Некорректные данные в запросе";
    private static final String ORDER_ID = "ST-20260201-001";

    @Test
    void defaultConstructor_shouldSetAllFields() {
        // When
        ErrorResponseDto response = new ErrorResponseDto(false, ORDER_ID, new ErrorResponseDto.ErrorDetails(ERROR_TYPE, MESSAGE));

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.orderSbertransportId()).isEqualTo(ORDER_ID);
        assertThat(response.error()).isNotNull();
        assertThat(response.error().type()).isEqualTo(ERROR_TYPE);
        assertThat(response.error().message()).isEqualTo(MESSAGE);
    }

    @Test
    void constructor_withTypeAndOrderIdAndMessage_shouldInitializeErrorCorrectly() {
        // When
        ErrorResponseDto response = new ErrorResponseDto(ERROR_TYPE, ORDER_ID, MESSAGE);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.orderSbertransportId()).isEqualTo(ORDER_ID);
        assertThat(response.error()).isNotNull();
        assertThat(response.error().type()).isEqualTo(ERROR_TYPE);
        assertThat(response.error().message()).isEqualTo(MESSAGE);
    }

    @Test
    void constructor_withTypeAndMessage_shouldInitializeWithErrorAndNullOrderId() {
        // When
        ErrorResponseDto response = new ErrorResponseDto(ERROR_TYPE, MESSAGE);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.orderSbertransportId()).isNull();
        assertThat(response.error()).isNotNull();
        assertThat(response.error().type()).isEqualTo(ERROR_TYPE);
        assertThat(response.error().message()).isEqualTo(MESSAGE);
    }

    @Test
    void errorDetailsRecord_shouldCreateAndExposeFields() {
        // When
        ErrorResponseDto.ErrorDetails error = new ErrorResponseDto.ErrorDetails(ERROR_TYPE, MESSAGE);

        // Then
        assertThat(error.type()).isEqualTo(ERROR_TYPE);
        assertThat(error.message()).isEqualTo(MESSAGE);
    }

    @Test
    void toString_shouldContainErrorTypeAndMessage() {
        // When
        ErrorResponseDto response = new ErrorResponseDto(ERROR_TYPE, ORDER_ID, MESSAGE);

        // Then
        String toString = response.toString();
        assertThat(toString).contains(ERROR_TYPE);
        assertThat(toString).contains(MESSAGE);
        assertThat(toString).contains(ORDER_ID);
        assertThat(toString).contains("isSuccess=false");
    }

    @Test
    void equalsAndHashCode_shouldBeBasedOnFields() {
        ErrorResponseDto response1 = new ErrorResponseDto(ERROR_TYPE, ORDER_ID, MESSAGE);
        ErrorResponseDto response2 = new ErrorResponseDto(ERROR_TYPE, ORDER_ID, MESSAGE);
        ErrorResponseDto response3 = new ErrorResponseDto("OTHER_ERROR", ORDER_ID, MESSAGE);

        // Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());

        assertThat(response1).isNotEqualTo(response3);
        assertThat(response1.hashCode()).isNotEqualTo(response3.hashCode());
    }
}



