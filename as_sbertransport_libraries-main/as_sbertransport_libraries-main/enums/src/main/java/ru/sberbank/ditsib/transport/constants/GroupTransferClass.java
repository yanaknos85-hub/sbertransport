package ru.sberbank.ditsib.transport.constants;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * Доступные классы группового трансфера
 */
@RequiredArgsConstructor
@Getter
@Schema(title = "Класс группового трансфера", description = "Доступные классы группового трансфера")
public enum GroupTransferClass {

    TRANSFER("Economy", "Эконом"),

    TRANSFER_COMFORT("Comfort", "Комфорт"),

    TRANSFER_COMFORT_PLUS("Comfort+", "Комфорт+"),

    TRANSFER_BUSINESS("Business", "Бизнес"),

    TRANSFER_VIP("VIP", "VIP"),

    TRANSFER_CAR_CHOICE("Car choice", "Выбор автомобиля");

    
    /**
     * Поиск класса такси по русскому названию.
     *
     * @param name название.
     * @return класс такси.
     */
    public static Optional<GroupTransferClass> getByRusName(String name) {
        return Arrays.stream(GroupTransferClass.values()).filter(value -> value.getRusName().equalsIgnoreCase(name)).findFirst();
    }

    /**
     * Поиск класса такси по названию.
     *
     * @param name название.
     * @return класс такси.
     */
    public static Optional<GroupTransferClass> getByName(String name) {
        return Optional.ofNullable(name)
                       .map(String::toUpperCase)
                       .map(GroupTransferClass::valueOf);
    }
    /**
     * Альтернативное название.
     */
    private final String value;
    
    /**
     * Русское название.
     */
    private final String rusName;
}
