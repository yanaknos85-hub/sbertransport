package ru.sberbank.ditsib.transport.constants.cargo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Возможные типы грузов.
 */
@RequiredArgsConstructor
@Getter
@Schema(title = "Вид груза", description = "Доступные виды груза")
public enum CargoTypeEnum {

    /**
     * Техника.
     */
    @Schema(description = "Техника")
    TECHNIQUE(UUID.fromString("7f18ce71-99a7-47b5-b285-335050c6715c"), "Техника"),

    /**
     * Документы.
     */
    @Schema(description = "Документы")
    DOCUMENT(UUID.fromString("7f18ce71-99a7-47b5-b285-335051c6715c"), "Документы"),

    /**
     * Мебель.
     */
    @Schema(description = "Мебель")
    FURNITURE(UUID.fromString("7f18ce71-99a7-47b5-b285-335052c6715c"), "Мебель"),

    /**
     * Посуда.
     */
    @Schema(description = "Посуда")
    TABLEWARE(UUID.fromString("7f18ce71-99a7-47b5-b285-335053c6715c"), "Посуда"),

    /**
     * Строительные материалы.
     */
    @Schema(description = "Строительные материалы")
    MATERIALS(UUID.fromString("7f18ce71-99a7-47b5-b285-335054c6715c"), "Строительные материалы"),

    /**
     * Хозяйственные товары.
     */
    @Schema(description = "Хозяйственные товары")
    HOUSEHOLD_GOODS(UUID.fromString("7f18ce71-99a7-47b5-b285-335055c6715c"), "Хозяйственные товары"),

    /**
     * Продовольственные товары.
     */
    @Schema(description = "Продовольственные товары")
    FOOD_PRODUCTS(UUID.fromString("7f18ce71-99a7-47b5-b285-335056c6715c"), "Продовольственные товары"),

    /**
     * Прочие грузы.
     */
    @Schema(description = "Прочие типы грузов")
    OTHER(UUID.fromString("7f18ce71-99a7-47b5-b285-335057c6715c"), "другое"),

    /**
     * Документы ЦАРС.
     */
    @Schema(description = "Документы ЦАРС")
    DOCUMENT_CARS(UUID.fromString("9f236293-3edf-40c0-9f8f-c4225940190f"), "Документы ЦАРС");
    
    private final UUID id;

    private final String name;

    /**
     * Получение типа груза по идентификатору.
     *
     * @param id идентификатор.
     *
     * @return тип груза.
     */
    public static Optional<CargoTypeEnum> fromId(UUID id) {
        for (CargoTypeEnum value : CargoTypeEnum.values()) {
            if (value.getId().equals(id)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    /**
     * Получение типа груза по названию.
     *
     * @param name название типа.
     *
     * @return тип груза.
     */
    public static Optional<CargoTypeEnum> getByName(String name) {
        for (CargoTypeEnum value : CargoTypeEnum.values()) {
            if (value.getName().equalsIgnoreCase(name)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    /**
     * Получение списка.
     *
     * @return список имен типов транспорта.
     */
    public static List<String> getNames() {
        return Arrays.stream(CargoTypeEnum.values()).map(CargoTypeEnum::getName).collect(Collectors.toList());
    }
}
