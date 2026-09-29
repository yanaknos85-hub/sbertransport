package ru.sberbank.ditsib.transport.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Типы лимитов.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum LimitType {

    /**
     * На подразделение.
     */
    DEPARTMENT(Values.DEPARTMENT),

    /**
     * На сотрудников.
     */
    EMPLOYEE(Values.EMPLOYEE);
    
    private final String name;

    /**
     * Получение типа лимита по названию.
     *
     * @param name название.
     *
     * @return тип лимита.
     */
    public static Optional<LimitType> getByName(String name) {
        for (LimitType value : LimitType.values()) {
            if (value.getName().equals(name)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    /**
     * Получение названий типов лимита.
     *
     * @return список названий.
     */
    @SuppressWarnings("java:S3958")
    public static List<String> getNames() {
        return Arrays.stream(TransportTypeEnum.values()).map(TransportTypeEnum::getName).toList();
    }

    /**
     * Значения.
     */
    @UtilityClass
    public static class Values {

        /**
         * Подразделение.
         */
        public final String DEPARTMENT = "DEPARTMENT";

        /**
         * Сотрудник.
         */
        public final String EMPLOYEE = "EMPLOYEE";
    }
}
