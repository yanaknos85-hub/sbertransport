package ru.sberbank.ditsib.transport.request.messaging.message;

/**
 * @param group1 Кресло от 9 мес. до 4 лет
 * @param group2 Кресло 3-7 лет
 * @param booster бустер 6-12 лет
 * @param newborn люлька до 1 года
 */
public record ChildSeatDetails(
        int group1,
        int group2,
        int booster,
        int newborn
) {
}
