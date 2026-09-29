package ru.sberbank.ditsib.transport.reports.mappers.impl;

import lombok.experimental.UtilityClass;
import ru.sberbank.ditsib.transport.reports.enums.GroupTransferStatusCode;
import ru.sberbank.ditsib.transport.reports.enums.PersonalStatusCode;
import ru.sberbank.ditsib.transport.reports.enums.TaxiStatusCode;

import java.util.Arrays;

@UtilityClass
public class StatusCodeHelper {

    public static String getStatusCodeNameForTaxi(Integer code) {
        return Arrays.stream(TaxiStatusCode.values())
                .filter(statusCode -> code != null && statusCode.getCode() == code)
                .findAny()
                .map(TaxiStatusCode::getName)
                .orElse(null);
    }

    public static String getStatusCodeNameForGroupTransfer(Integer code) {
        return Arrays.stream(GroupTransferStatusCode.values())
                .filter(statusCode -> code != null && statusCode.getCode() == code)
                .findAny()
                .map(GroupTransferStatusCode::getName)
                .orElse(null);
    }

    public static String getStatusCodeNameForPersonal(Integer code) {
        return Arrays.stream(PersonalStatusCode.values())
                .filter(statusCode -> code != null && statusCode.getCode() == code)
                .findAny()
                .map(PersonalStatusCode::getName)
                .orElse(null);
    }
}
