package ru.sber.transport.etrn.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SigningOperationStatusTest {

    @Test
    @DisplayName("SigningOperationStatus — все значения имеют описание")
    void allStatuses_haveDescription() {
        for (SigningOperationStatus status : SigningOperationStatus.values()) {
            assertThat(status.getDescription())
                    .as("Description for " + status.name())
                    .isNotNull()
                    .isNotBlank();
        }
    }

    @Test
    @DisplayName("SigningOperationStatus — проверять ACCEPTED_BY_KORUS")
    void acceptedByKorus_hasCorrectDescription() {
        assertThat(SigningOperationStatus.ACCEPTED_BY_KORUS.getDescription())
                .isEqualTo("Подтверждено КОРУС");
    }

    @Test
    @DisplayName("SigningOperationStatus — проверять OUTCOME_UNKNOWN")
    void outcomeUnknown_hasCorrectDescription() {
        assertThat(SigningOperationStatus.OUTCOME_UNKNOWN.getDescription())
                .isEqualTo("Исход неизвестен");
    }
}
