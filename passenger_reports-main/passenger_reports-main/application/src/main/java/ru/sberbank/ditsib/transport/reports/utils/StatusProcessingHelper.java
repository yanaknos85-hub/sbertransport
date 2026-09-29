package ru.sberbank.ditsib.transport.reports.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.enums.*;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@UtilityClass
@Slf4j
public class StatusProcessingHelper {

    private static final Map<String, Function<String, Enum<?>>> STATUS_ENUM_MAP = Map.of(
            TransportTypeEnum.TAXI.name(), RequestTaxiStatus::valueOf,
            TransportTypeEnum.PUBLIC.name(), RequestPublicStatus::valueOf,
            TransportTypeEnum.GROUP_TRANSFER.name(), RequestGroupTransferStatus::valueOf,
            TransportTypeEnum.CARSHARING.name(), RequestCarsharingStatus::valueOf,
            TransportTypeEnum.PERSONAL.name(), RequestPersonalStatus::valueOf
    );

    public static boolean stopProcessingMessage(String status, UUID messageId, String messageTransportType, String messageStatus) {
        try {
            if (status == null || messageStatus == null || !STATUS_ENUM_MAP.containsKey(messageTransportType)) {
                return false;
            }
            var enumStatus = getEnumStatus(messageTransportType, status);
            var enumNewStatus = getEnumStatus(messageTransportType, messageStatus);
            if (enumStatus.equals(enumNewStatus)) {
                return false;
            } else {
                return enumStatus.ordinal() > enumNewStatus.ordinal();
            }
        } catch (IllegalArgumentException e) {
            log.error("Неподдерживаемый статус {} в сообщении с id {}", messageStatus, messageId);
            return true;
        }
    }

    private static Enum<?> getEnumStatus(String transportType, String status) throws IllegalArgumentException {
        return STATUS_ENUM_MAP.get(transportType).apply(status);
    }
}
