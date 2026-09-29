package ru.sber.transport.request_checks.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Проверка состава ExcludedRequestStatuses")
class ExcludedRequestStatusesTest {

    @Test
    @DisplayName("EXCLUDED_STATUSES содержит все необходимые статусы для исключения из антифрод проверок")
    void excludedStatusesContainsAllRequiredValues() {
        assertThat(ExcludedRequestStatuses.EXCLUDED_STATUSES)
                .containsExactlyInAnyOrder(
                        "PERSONAL_PAYMENT_DECLINED",
                        "PERSONAL_CANCELLED",
                        "PUBLIC_CANCELLED",
                        "PUBLIC_PAYMENT_NOT_DONE",
                        "CARSHARING_CANCELLED",
                        "TAXI_CANCELLED",
                        "GROUP_TRANSFER_CANCELLED",
                        "BUS_CANCELLED",
                        "CANCELLED",
                        "PAYMENT_NOT_DONE",
                        "DECLINED"
                );
    }

    @Test
    @DisplayName("EXCLUDED_STATUSES — immutable set")
    void excludedStatusesIsImmutable() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> ExcludedRequestStatuses.EXCLUDED_STATUSES.add("NEW_STATUS")
        );
    }
}