package ru.sberbank.ditsib.transport.constants;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Категории водительских прав.
 */
@Schema(title = "Категории транспортных средств", description = "Категории транспортных средств")
public enum DriverLicenseClass {

    /**
     * Мото (А)
     */
    A,

    /**
     * Мото (А1)
     */
    A1,

    /**
     * Легковые авто (В)
     */
    B,

    /**
     * Легковые авто (В1)
     */
    B1,

    /**
     * Грузовые авто (С)
     */
    C,

    /**
     * Грузовые авто (С1)
     */
    C1,

    /**
     * Пассажирские ТС (D)
     */
    D,

    /**
     * Пассажирские ТС (D1)
     */
    D1,

    /**
     * Легковые с прицепом >750кг
     */
    BE,

    /**
     * Грузовые с прицепом >750кг (CE)
     */
    CE,

    /**
     * Грузовые с прицепом >750кг (C1E)
     */
    C1E,

    /**
     * Пассажирские с прицепом >750кг (DE)
     */
    DE,

    /**
     * Пассажирские с прицепом >750кг (D1E)
     */
    D1E,

    /**
     * Малый транспорт
     */
    M,

    /**
     * Трамвай
     */
    TM;

    /**
     * Получение списка типов разрешенных ТС по строке.
     *
     * @param str исходная строка.
     *
     * @return типы разрешенных ТС.
     */
    public static Set<DriverLicenseClass> valuesOf(String str) {
        if (str != null && !str.isBlank()) {
            return Arrays.stream(str.split(",")).map(DriverLicenseClass::valueOf).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    /**
     * Получение типа разрешенных ТС по строке.
     *
     * @param name исходная строка.
     *
     * @return тип разрешенных ТС.
     */
    public static Optional<DriverLicenseClass> getByName(String name) {
        if (name == null || "".equals(name)) {
            return Optional.empty();
        }

        for (DriverLicenseClass value : DriverLicenseClass.values()) {
            if (value.name().equals(name)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
