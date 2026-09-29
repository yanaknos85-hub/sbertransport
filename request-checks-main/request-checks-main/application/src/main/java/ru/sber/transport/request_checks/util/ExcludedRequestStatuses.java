package ru.sber.transport.request_checks.util;

import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * реализованы статусы согласно таблице https://confluence.sberbank.ru/pages/viewpage.action?pageId=24390503133
 */
@UtilityClass
public final class ExcludedRequestStatuses {
    public static final Set<String> EXCLUDED_STATUSES = Set.of(
            "PERSONAL_PAYMENT_DECLINED",
            "PERSONAL_CANCELLED",
            "PUBLIC_CANCELLED",
            "PUBLIC_PAYMENT_NOT_DONE",
            "TAXI_CANCELLED",
            "CARSHARING_CANCELLED",
            "BUS_CANCELLED",
            "GROUP_TRANSFER_CANCELLED",
            "CANCELLED",
            "PAYMENT_NOT_DONE",
            "DECLINED"
    );
}
