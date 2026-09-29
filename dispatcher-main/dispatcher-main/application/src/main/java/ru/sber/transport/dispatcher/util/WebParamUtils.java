package ru.sber.transport.dispatcher.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * Маппер для file resolvers
 */
@UtilityClass
public class WebParamUtils {

    private static final String CONVERT_ERROR = "Cannot convert %s to %s";

    /**
     * Преобразует объект в экземпляр {@link Integer}.
     *
     * @param value объект для преобразования
     * @return преобразованный {@code Integer} или {@code null}, если был передан {@code null}
     */
    public static Integer toInteger(Object value) {
        return switch (value) {
            case null -> null;
            case Integer integer -> integer;
            case String valueStr -> Integer.parseInt(valueStr);
            default -> throw new IllegalArgumentException(CONVERT_ERROR.formatted(value.getClass(), Integer.class));
        };
    }

    /**
     * Преобразует объект в экземпляр {@link Boolean}.
     *
     * @param value объект для преобразования
     * @return преобразованный {@code Boolean} или {@code null}, если был передан {@code null}
     */
    public static Boolean toBoolean(Object value) {
        return switch (value) {
            case null -> null;
            case Boolean boolVal -> boolVal;
            case String valueStr -> Boolean.parseBoolean(valueStr.toLowerCase().trim());
            default -> throw new IllegalArgumentException(CONVERT_ERROR.formatted(value.getClass(), Boolean.class));
        };
    }

    /**
     * Преобразует объект в экземпляр {@link UUID}.
     *
     * @param value объект для преобразования
     * @return преобразованный {@code UUID} или {@code null}, если был передан {@code null}
     */
    public static UUID toUUID(Object value) {
        return switch (value) {
            case null -> null;
            case UUID uuid -> uuid;
            case String valueStr -> UUID.fromString(valueStr);
            default -> throw new IllegalArgumentException(CONVERT_ERROR.formatted(value.getClass(), UUID.class));
        };
    }
}