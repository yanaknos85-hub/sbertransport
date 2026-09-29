package ru.sberbank.ditsib.transport.reports.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.sberbank.ditsib.transport.reports.enums.*;

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.*;
import static ru.sberbank.ditsib.transport.reports.enums.RequestTaxiStatus.TAXI_AWAITING_APPROVAL;
import static ru.sberbank.ditsib.transport.reports.enums.RequestTaxiStatus.values;

class StatusProcessingHelperTest {

    @Test
    void stopProcessingMessageCommon() {
        assertThat(StatusProcessingHelper.stopProcessingMessage(null,
                UUID.randomUUID(),
                TAXI.name(),
                TAXI_AWAITING_APPROVAL.name())).isFalse();
        assertThat(StatusProcessingHelper.stopProcessingMessage(TAXI_AWAITING_APPROVAL.name(),
                UUID.randomUUID(),
                "BOAT",
                TAXI_AWAITING_APPROVAL.name())).isFalse();
        assertThat(StatusProcessingHelper.stopProcessingMessage(TAXI_AWAITING_APPROVAL.name(),
                UUID.randomUUID(),
                TAXI.name(),
                null)).isFalse();
        assertThat(StatusProcessingHelper.stopProcessingMessage(TAXI_AWAITING_APPROVAL.name(),
                UUID.randomUUID(),
                TAXI.name(),
                "NEW_GOOD_STATUS")).isTrue();
        assertThat(StatusProcessingHelper.stopProcessingMessage("NEW_GOOD_STATUS",
                UUID.randomUUID(),
                TAXI.name(),
                TAXI_AWAITING_APPROVAL.name())).isTrue();
    }

    @ParameterizedTest
    @EnumSource(RequestTaxiStatus.class)
    void stopProcessingMessageRequestTaxiStatus(RequestTaxiStatus newStatus) {
        Arrays.stream(values()).forEach(
                status -> {
                    var result = Boolean.FALSE;
                    if (!status.equals(newStatus) && newStatus.ordinal() <= status.ordinal()) {
                        result = Boolean.TRUE;
                    }
                    assertThat(StatusProcessingHelper.stopProcessingMessage(status.name(),
                            UUID.randomUUID(),
                            TAXI.name(),
                            newStatus.name())).isEqualTo(result);
                }
        );
    }

    @ParameterizedTest
    @EnumSource(RequestPublicStatus.class)
    void stopProcessingMessageRequestPublicStatus(RequestPublicStatus newStatus) {
        Arrays.stream(RequestPublicStatus.values()).forEach(
                status -> {
                    var result = Boolean.FALSE;
                    if (!status.equals(newStatus) && newStatus.ordinal() <= status.ordinal()) {
                        result = Boolean.TRUE;
                    }
                    assertThat(StatusProcessingHelper.stopProcessingMessage(status.name(),
                            UUID.randomUUID(),
                            PUBLIC.name(),
                            newStatus.name())).isEqualTo(result);
                }
        );
    }

    @ParameterizedTest
    @EnumSource(RequestGroupTransferStatus.class)
    void stopProcessingMessageRequestGroupTransferStatus(RequestGroupTransferStatus newStatus) {
        Arrays.stream(RequestGroupTransferStatus.values()).forEach(
                status -> {
                    var result = Boolean.FALSE;
                    if (!status.equals(newStatus) && newStatus.ordinal() <= status.ordinal()) {
                        result = Boolean.TRUE;
                    }
                    assertThat(StatusProcessingHelper.stopProcessingMessage(status.name(),
                            UUID.randomUUID(),
                            GROUP_TRANSFER.name(),
                            newStatus.name())).isEqualTo(result);
                }
        );
    }

    @ParameterizedTest
    @EnumSource(RequestCarsharingStatus.class)
    void stopProcessingMessageRequestCarsharingStatus(RequestCarsharingStatus newStatus) {
        Arrays.stream(RequestCarsharingStatus.values()).forEach(
                status -> {
                    var result = Boolean.FALSE;
                    if (!status.equals(newStatus) && newStatus.ordinal() <= status.ordinal()) {
                        result = Boolean.TRUE;
                    }
                    assertThat(StatusProcessingHelper.stopProcessingMessage(status.name(),
                            UUID.randomUUID(),
                            CARSHARING.name(),
                            newStatus.name())).isEqualTo(result);
                }
        );
    }

    @ParameterizedTest
    @EnumSource(RequestPersonalStatus.class)
    void stopProcessingMessageRequestPersonalStatus(RequestPersonalStatus newStatus) {
        Arrays.stream(RequestPersonalStatus.values()).forEach(
                status -> {
                    var result = Boolean.FALSE;
                    if (!status.equals(newStatus) && newStatus.ordinal() <= status.ordinal()) {
                        result = Boolean.TRUE;
                    }
                    assertThat(StatusProcessingHelper.stopProcessingMessage(status.name(),
                            UUID.randomUUID(),
                            PERSONAL.name(),
                            newStatus.name())).isEqualTo(result);
                }
        );
    }
}