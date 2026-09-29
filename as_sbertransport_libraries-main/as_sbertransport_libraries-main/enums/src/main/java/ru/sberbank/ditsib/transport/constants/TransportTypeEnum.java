package ru.sberbank.ditsib.transport.constants;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.transport.constants.TransportServiceType.*;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.Constants.*;

/**
 * Доступные виды транспорта.
 */
@RequiredArgsConstructor
@Getter
@Schema(title = "Вид транспорта", description = "Доступные виды транспорта")
public enum TransportTypeEnum {

    /**
     *  Такси.
     */
    TAXI(UUID.fromString("7f18ce71-99a7-47b5-b285-335058c6715c"), TAXI_STRING, "Такси",
         EMPLOYEE_TRANSPORTATION),

    /**
     * Личный транспорт.
     */
    PERSONAL(UUID.fromString("1a33601d-4db4-4720-8d09-95f015770fe0"), PERSONAL_STRING, "Личный транспорт",
             EMPLOYEE_TRANSPORTATION),

    /**
     * Общественный транспорт.
     */
    PUBLIC(UUID.fromString("fa96da51-068d-4cf5-bfd2-4cd28d717e13"), PUBLIC_STRING, "Общественный транспорт",
           EMPLOYEE_TRANSPORTATION),

    /**
     * Каршеринг.
     */
    CARSHARING(UUID.fromString("7f18ce71-65a7-47b5-b285-335058c6515c"), CARSHARING_STRING, "Каршеринг",
               EMPLOYEE_TRANSPORTATION),
    /**
     * Групповой трансфер
     */
    GROUP_TRANSFER(UUID.fromString("ab6efcb9-dce2-4545-82f7-f75e51e670c3"), GROUP_TRANSFER_STRING, "Трансфер",
               EMPLOYEE_TRANSPORTATION),

    /**
     * Велосипед.
     */
    BICYCLE(UUID.fromString("6f18ce82-99a7-34b5-b285-335058c6715c"), BICYCLE_STRING, "Велосипед",
            EMPLOYEE_TRANSPORTATION),

    /**
     * Пешком.
     */
    WALK(UUID.fromString("1f13ce82-99a7-34b5-b285-225058b6715c"), WALK_STRING, "Пешком",
         EMPLOYEE_TRANSPORTATION),

    /**
     * Самокат.
     */
    SCOOTER(UUID.fromString("3c13ce82-24a7-11b5-b195-225058b6715c"), SCOOTER_STRING, "Самокат",
            EMPLOYEE_TRANSPORTATION),

    /**
     * Доставка сборного груза.
     */
    DEDICATED(UUID.fromString("c6ab83a2-0637-11ec-9a03-0242ac130003"), DEDICATED_STRING, "Доставка сборного груза",
              CARGO_TRANSPORTATION),

    /**
     * Курьерская доставка.
     */
    COURIER(UUID.fromString("296c1c60-0aa3-4e06-85a0-f5efc021d8c9"), COURIER_STRING, "Курьерская доставка",
            CARGO_TRANSPORTATION),

    /**
     * Внутренний курьер.
     */
    DOMESTIC_COURIER(UUID.fromString("d090237f-f1f8-4282-b318-6064f5891354"), DOMESTIC_COURIER_STRING,
            "Внутренний курьер", CARGO_TRANSPORTATION),

    /**
     * Межрегиональная доставка.
     */
    INTERREGIONAL(UUID.fromString("e2722f95-1043-41d0-8a57-09182c6410c6"), INTERREGIONAL_STRING,
                  "Межрегиональная доставка", CARGO_TRANSPORTATION),

    /**
     * Доставка выделенным транспортом.
     */
    INDIVIDUAL(UUID.fromString("e2722f95-1043-41d0-8a57-09182c6410c7"), INDIVIDUAL_STRING,
            "Доставка выделенным транспортом", CARGO_TRANSPORTATION),

    /**
     * Служебный.
     */
    OFFICIAL(UUID.fromString("a760c70f-fcf6-0d11-20fe-0818fdbb15ff"), OFFICIAL_STRING,
             "Служебный", REPAIR),

    /**
     * Ремонт.
     */
    PRIVATE(UUID.fromString("9a46760e-09cf-c8c2-149e-6177118083dd"), PRIVATE_STRING,
            "Личный", REPAIR),

    /**
     * Специальный
     */
    SPECIAL(UUID.fromString("8bf3e674-ad6b-a86c-eb72-9cd9b55e9924"), SPECIAL_STRING,
            "Специальный", REPAIR);
    private final UUID id;

    private final String name;

    private final String rusName;

    private final TransportServiceType serviceType;

    /**
     * Получение типа транспорта по идентификатору.
     *
     * @param id идентификатор.
     *
     * @return тип транспорта.
     */
    public static Optional<TransportTypeEnum> fromId(UUID id) {
        for (TransportTypeEnum value : TransportTypeEnum.values()) {
            if (value.getId().equals(id)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    /**
     * Получение типа транспорта по идентификатору.
     *
     * @param name название.
     *
     * @return тип транспорта.
     */
    public static Optional<TransportTypeEnum> getByName(String name) {
        for (TransportTypeEnum value : TransportTypeEnum.values()) {
            if (value.getName().equalsIgnoreCase(name)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    /**
     * Получение типа транспорта по идентификатору.
     *
     * @param name название.
     *
     * @return тип транспорта.
     */
    public static Optional<TransportTypeEnum> getByRusName(String name) {
        for (TransportTypeEnum value : TransportTypeEnum.values()) {
            if (value.getRusName().equalsIgnoreCase(name)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    /**
     * Получение типов транспорта по виду услуги.
     *
     * @param serviceType вид услуги.
     *
     * @return типы транспорта.
     */
    @SuppressWarnings("java:S3958")
    public static List<TransportTypeEnum> getByServiceType(TransportServiceType serviceType) {
        if (serviceType == null) {
            return Collections.emptyList();
        }
        
        return Arrays.stream(TransportTypeEnum.values())
                     .filter(v -> serviceType == v.getServiceType())
                     .toList();
    }

    /**
     * Получение имен типов транспорта.
     *
     * @return имена типов транспорта.
     */
    public static List<String> getNames() {
        return Arrays.stream(TransportTypeEnum.values()).map(TransportTypeEnum::getName).collect(Collectors.toList());
    }

    /**
     * Константы.
     */
    @UtilityClass
    public static class Constants {

        /**
         * Такси.
         */
        public final String TAXI_STRING = "TAXI";

        /**
         * ЛТ.
         */
        public final String PERSONAL_STRING = "PERSONAL";

        /**
         * ОТ.
         */
        public final String PUBLIC_STRING = "PUBLIC";

        /**
         * Каршеринг.
         */
        public final String CARSHARING_STRING = "CARSHARING";

        /**
         * Групповой трансфер
         */
        public final String GROUP_TRANSFER_STRING = "GROUP_TRANSFER";

        /**
         * Вело.
         */
        public final String BICYCLE_STRING = "BICYCLE";

        /**
         * Пешком.
         */
        public final String WALK_STRING = "WALK";

        /**
         * Самокат.
         */
        public final String SCOOTER_STRING = "SCOOTER";

        /**
         * Доставка сборного груза.
         */
        public final String DEDICATED_STRING = "DEDICATED";

        /**
         * Курьерская доставка.
         */
        public final String COURIER_STRING = "COURIER";

        /**
         * Внутренний курьер.
         */
        public final String DOMESTIC_COURIER_STRING = "DOMESTIC_COURIER";

        /**
         * Межрегиональная доставка.
         */
        public final String INTERREGIONAL_STRING = "INTERREGIONAL";

        /**
         * Доставка выделенным транспортом.
         */
        public final String INDIVIDUAL_STRING = "INDIVIDUAL";

        /**
         * Служебный.
         */
        public final String OFFICIAL_STRING = "OFFICIAL";

        /**
         * Специальный.
         */
        public final String SPECIAL_STRING = "SPECIAL";

        /**
         * Личный.
         */
        public final String PRIVATE_STRING = "PRIVATE";
    }
}
