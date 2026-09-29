package ru.sberbank.ditsib.transport.constants;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Типы компенсаций за ОТ.
 */
@RequiredArgsConstructor
@Getter
@Schema(title = "Типы компенсации за поездки на общественном транспорте")
public enum PublicCompensationType {

    /**
     * Городская поездка.
     */
    CITY_TRIP_COMPENSATION("Разовая поездка", false, false),

    /**
     * Междугородняя поездка.
     */
    SUBURB_TRIP_COMPENSATION("Междугородняя поездка", true, false),

    /**
     * Проездной.
     */
    TRAVEL_CARD_COMPENSATION("Проездной документ", true, true),

    /**
     * Платные сервисы (парковки, платные дороги, переправы)
     */
    PAID_SERVICES_COMPENSATION("Платные сервисы (парковка/дорога/переправа)", true, false);

    private final String rusName;
    private final boolean attachmentDocumentRequired;
    private final boolean expirationDatesRequired;
    
    /**
     * Получение типа компенсации по названию.
     *
     * @param name название.
     * @return тип компенсации.
     */
    public static Optional<PublicCompensationType> getByName(String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }

        for (PublicCompensationType value : PublicCompensationType.values()) {
            if (value.name().equals(name)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Получение типа компенсации по русскому названию.
     *
     * @param name название.
     * @return тип компенсации.
     */
    public static Optional<PublicCompensationType> getByRusName(String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }

        for (PublicCompensationType value : PublicCompensationType.values()) {
            if (value.getRusName().equals(name)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
