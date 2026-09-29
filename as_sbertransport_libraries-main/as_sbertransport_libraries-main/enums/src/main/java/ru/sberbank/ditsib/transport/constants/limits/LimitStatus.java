package ru.sberbank.ditsib.transport.constants.limits;

/**
 * Статусы лимитов.
 */
public enum LimitStatus {

    /**
     * Планируется.
     */
    PLANNING,

    /**
     * Распределен.
     */
    SHARED,

    /**
     * Закрыт.
     */
    CLOSED;

    /**
     * Получение статуса по названию.
     *
     * @param text название.
     *
     * @return статус.
     */
    public static LimitStatus fromString(String text) {
        for (LimitStatus b : LimitStatus.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
