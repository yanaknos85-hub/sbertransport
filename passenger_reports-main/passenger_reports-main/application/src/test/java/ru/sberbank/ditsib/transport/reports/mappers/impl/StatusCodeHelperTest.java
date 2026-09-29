package ru.sberbank.ditsib.transport.reports.mappers.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.sberbank.ditsib.transport.reports.enums.GroupTransferStatusCode;
import ru.sberbank.ditsib.transport.reports.enums.PersonalStatusCode;
import ru.sberbank.ditsib.transport.reports.enums.TaxiStatusCode;

import static org.assertj.core.api.Assertions.assertThat;

class StatusCodeHelperTest {

    @ParameterizedTest
    @EnumSource(TaxiStatusCode.class)
    void getStatusCodeNameForTaxi(TaxiStatusCode statusCode) {
        assertThat(StatusCodeHelper.getStatusCodeNameForTaxi(statusCode.getCode())).isEqualTo(statusCode.getName());
    }

    @ParameterizedTest
    @EnumSource(GroupTransferStatusCode.class)
    void getStatusCodeNameForGroupTransfer(GroupTransferStatusCode statusCode) {
        assertThat(StatusCodeHelper.getStatusCodeNameForGroupTransfer(statusCode.getCode())).isEqualTo(statusCode.getName());
    }

    @ParameterizedTest
    @EnumSource(PersonalStatusCode.class)
    void getStatusCodeNameForPersonal(PersonalStatusCode statusCode) {
        assertThat(StatusCodeHelper.getStatusCodeNameForPersonal(statusCode.getCode())).isEqualTo(statusCode.getName());
    }

    @Test
    void getStatusCodeNameForTaxiOtherCases() {
        assertThat(StatusCodeHelper.getStatusCodeNameForTaxi(null)).isNull();
    }

    @Test
    void getStatusCodeNameForGroupTransferOtherCases() {
        assertThat(StatusCodeHelper.getStatusCodeNameForGroupTransfer(null)).isNull();
    }

    @Test
    void getStatusCodeNameForPersonalOtherCases() {
        assertThat(StatusCodeHelper.getStatusCodeNameForPersonal(null)).isNull();
    }
}