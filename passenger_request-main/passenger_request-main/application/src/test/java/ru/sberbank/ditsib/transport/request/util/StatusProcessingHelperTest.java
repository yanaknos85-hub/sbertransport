package ru.sberbank.ditsib.transport.request.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.*;
import static ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus.*;

class StatusProcessingHelperTest {

    @Test
    void cantChangeTaxiTripStatus() {
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(SENT_TO_CONTRACTOR, SENT_TO_CONTRACTOR)).isFalse();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(SENT_TO_CONTRACTOR, null)).isFalse();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(SENT_TO_CONTRACTOR, UNDEFINED)).isFalse();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(WAITING_FOR_ASSIGNMENT, SENT_TO_CONTRACTOR)).isFalse();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(DRIVER_ASSIGNED, WAITING_FOR_ASSIGNMENT)).isFalse();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(DRIVER_APPROVED, DRIVER_ASSIGNED)).isFalse();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(DRIVER_ON_THE_WAY, DRIVER_APPROVED)).isFalse();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(DRIVER_ARRIVED, DRIVER_ON_THE_WAY)).isFalse();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(TRIP_IN_PROGRESS, DRIVER_ARRIVED)).isFalse();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(ORDER_FINISHED, TRIP_IN_PROGRESS)).isFalse();

        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(SENT_TO_CONTRACTOR, WAITING_FOR_ASSIGNMENT)).isTrue();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(WAITING_FOR_ASSIGNMENT, DRIVER_ASSIGNED)).isTrue();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(DRIVER_ASSIGNED, DRIVER_APPROVED)).isTrue();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(DRIVER_APPROVED, DRIVER_ON_THE_WAY)).isTrue();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(DRIVER_ON_THE_WAY, DRIVER_ARRIVED)).isTrue();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(DRIVER_ARRIVED, TRIP_IN_PROGRESS)).isTrue();
        assertThat(StatusProcessingHelper.cantChangeTaxiTripStatus(TRIP_IN_PROGRESS, ORDER_FINISHED)).isTrue();
    }

    @Test
    void inboundTaxiTripStatusToGroupTransferStatus() {
        assertThat(StatusProcessingHelper.inboundTaxiTripStatusToGroupTransferStatus(SENT_TO_CONTRACTOR)).isEqualTo(GROUP_TRANSFER_AWAITING_SEARCH);
        assertThat(StatusProcessingHelper.inboundTaxiTripStatusToGroupTransferStatus(WAITING_FOR_ASSIGNMENT)).isEqualTo(GROUP_TRANSFER_AWAITING_SEARCH);
        assertThat(StatusProcessingHelper.inboundTaxiTripStatusToGroupTransferStatus(DRIVER_ASSIGNED)).isEqualTo(GROUP_TRANSFER_DRIVER_FOUND);
        assertThat(StatusProcessingHelper.inboundTaxiTripStatusToGroupTransferStatus(DRIVER_APPROVED)).isEqualTo(GROUP_TRANSFER_DRIVER_FOUND);
        assertThat(StatusProcessingHelper.inboundTaxiTripStatusToGroupTransferStatus(DRIVER_ON_THE_WAY)).isEqualTo(GROUP_TRANSFER_DRIVER_ON_THE_WAY);
        assertThat(StatusProcessingHelper.inboundTaxiTripStatusToGroupTransferStatus(DRIVER_ARRIVED)).isEqualTo(GROUP_TRANSFER_DRIVER_ARRIVED);
        assertThat(StatusProcessingHelper.inboundTaxiTripStatusToGroupTransferStatus(TRIP_IN_PROGRESS)).isEqualTo(GROUP_TRANSFER_TRIP_IN_PROGRESS);
        assertThat(StatusProcessingHelper.inboundTaxiTripStatusToGroupTransferStatus(ORDER_FINISHED)).isEqualTo(GROUP_TRANSFER_TRIP_FINISHED);
        assertThat(StatusProcessingHelper.inboundTaxiTripStatusToGroupTransferStatus(ORDER_CANCELLED_BY_CLIENT)).isEqualTo(GROUP_TRANSFER_CANCELLED);
    }

    @Test
    void canChangeToGenaiCheckShouldReturnTrueForAllowedStatuses() {
        assertThat(StatusProcessingHelper.canChangeToGenaiCheck(PERSONAL_AWAITING_TRIP_APPROVAL)).isTrue();
        assertThat(StatusProcessingHelper.canChangeToGenaiCheck(PERSONAL_TRIP_FINISHED)).isTrue();
        assertThat(StatusProcessingHelper.canChangeToGenaiCheck(PUBLIC_AWAITING_AFFIRMATIVE)).isTrue();
        assertThat(StatusProcessingHelper.canChangeToGenaiCheck(PUBLIC_TRIP_CONFIRMATION)).isTrue();
    }

    @Test
    void canChangeToGenaiCheckShouldReturnFalseForDisallowedStatuses() {
        assertThat(StatusProcessingHelper.canChangeToGenaiCheck(TAXI_AWAITING_APPROVAL)).isFalse();
        assertThat(StatusProcessingHelper.canChangeToGenaiCheck(PERSONAL_APPROVED)).isFalse();
        assertThat(StatusProcessingHelper.canChangeToGenaiCheck(PUBLIC_AWAITING_APPROVAL)).isFalse();
        assertThat(StatusProcessingHelper.canChangeToGenaiCheck(null)).isFalse();
    }
}