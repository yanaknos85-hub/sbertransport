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
 * Допустимые категории грузов.
 */
@RequiredArgsConstructor
@Getter
@Schema(title = "Категория груза", description = "Доступные категории груза")
public enum CargoCategoryEnum {

    /**
     * Прочее.
     */
    OTHER(UUID.fromString("7f18ce71-1111-47b5-b285-335059c6715c"), "другое", "мм3", 75),

    /**
     * Обычные.
     */
    REGULAR(UUID.fromString("10dcfa8f-b24a-4460-9d6d-72ba3196530c"), "Обычные", "мм3", 75),

    /**
     * Наливные.
     */
    LIQUID(UUID.fromString("8a01beb2-3ded-4465-8fa8-a28efc5d2a42"), "Наливные", "м3", 100),

    /**
     * Насыпные.
     */
    BULK(UUID.fromString("506bc52d-85e2-4a1a-825c-779a617ced86"), "Насыпные", "м3", 75),

    /**
     * Корреспонденция.
     */
    CORRESPONDENCE(UUID.fromString("0e76aded-09c7-4b0e-829f-2c0a8fe751a0"), "Корреспонденция", "мм3", 75);
    
    private final UUID id;
    private final String name;
    private final String unit;

    /**
     * Процент загрузки по объему.
     */
    private final double availableVolume;

    /**
     * Получение категории по идентификатору.
     *
     * @param id идентификатор.
     *
     * @return категория.
     */
    public static Optional<CargoCategoryEnum> fromId(UUID id) {
        for (CargoCategoryEnum value : CargoCategoryEnum.values()) {
            if (value.getId().equals(id)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    /**
     * Получение категории по названию.
     *
     * @param name название категории.
     *
     * @return категория.
     */
    public static Optional<CargoCategoryEnum> getByName(String name) {
        for (CargoCategoryEnum value : CargoCategoryEnum.values()) {
            if (value.getName().equalsIgnoreCase(name)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    /**
     * Получение названий категорий.
     *
     * @return список категорий.
     */
    public static List<String> getNames() {
        return Arrays.stream(CargoCategoryEnum.values()).map(CargoCategoryEnum::getName).collect(Collectors.toList());
    }
}
