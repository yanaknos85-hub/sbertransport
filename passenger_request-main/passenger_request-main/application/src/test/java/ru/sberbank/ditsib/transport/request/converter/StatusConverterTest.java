package ru.sberbank.ditsib.transport.request.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class StatusConverterTest {

    @ParameterizedTest
    @EnumSource(value = TripRequestStatus.class)
    void tripRequestStatusToMetricsStatus(TripRequestStatus status) {
        Supplier<String> expected = () -> {
            if (StatusConverter.CANCELED_STATUSES.contains(status)) {
                return "CANCELED";
            }
            if (StatusConverter.DONE_STATUSES.contains(status)) {
                return "DONE";
            }
            return "IN_PROGRESS";
        };
        assertThat(StatusConverter.tripRequestStatusToMetricsStatus(status))
                .isEqualTo(expected.get());
    }
}