package ru.sber.transport.cargo.exchange.request.enums;

import lombok.Getter;

import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

/**
 * Статусы заявки на перевозку.
 * <p>
 * Каждый статус имеет:
 * - код (техническое имя)
 * - displayName — название для отображения пользователю (грузовладельцу)
 * - carrierMessage — сообщение, отображаемое перевозчику
 */
@Getter
public enum RequestStatus {

    DRAFT("BS_01", "Черновик", null),
    PUBLISHED("BS_02", "Опубликована", "Доступна для отклика"),
    CARRIER_SELECTED("BS_03", "Перевозчик выбран", "Вас выбрали"),
    CONFIRMED("BS_05", "Перевозка подтверждена", "Перевозка подтверждена"),
    DEPARTING_TO_LOADING("BS_06", "В пути к отправителю", "Выехал к отправителю"),
    ARRIVED_AT_LOADING("BS_07", "Прибыл на погрузку", "Вы на месте погрузки"),
    LOADING_IN_PROGRESS("BS_08", "Идёт погрузка", "Погрузка начата"),
    CARGO_ACCEPTED_BY_CARRIER("BS_10", "Груз принят перевозчиком", "Вы приняли груз"),
    DEPARTING_TO_UNLOADING("BS_11", "В пути к получателю", "Выехал к получателю"),
    ARRIVED_AT_UNLOADING("BS_12", "Прибыл на выгрузку", "Вы на месте выгрузки"),
    UNLOADING_IN_PROGRESS("BS_13", "Идёт выгрузка", "Выгрузка начата"),
    CARGO_DELIVERED("BS_14", "Груз доставлен", "Доставка завершена"),
    DOCUMENT_PENDING_CARRIER("BS_15", "Документы: требуется подпись", "Требуется подпись документов"),
    AWAITING_CUSTOMER_CONFIRMATION("BS_16", "Ожидает вашего подтверждения", "Ожидаем подтверждение заказчика"),
    AWAITING_PAYMENT("BS_17", "Ожидает оплаты", "Ожидание оплаты"),
    PAID("BS_18", "Оплачено", "Оплачено"),
    RATING_PENDING("BS_19", "Оставьте оценку", "Оцените перевозку"),
    COMPLETED("BS_20", "Завершена", "Завершена"),

    // Статусы отмены
    CANCELLED_BY_CUSTOMER("BS_90", "Отменена вами", "Отменена заказчиком");

    private final String code;
    private final String displayName;
    private final String carrierMessage;


    // Статическое поле с правилами переходов для грузовладельца
    private static final Map<RequestStatus, Set<RequestStatus>> ALLOWED_TRANSITIONS_SHIPPER = Map.ofEntries(
            entry(DRAFT, Set.of(PUBLISHED)),
            entry(PUBLISHED, Set.of(CARRIER_SELECTED, CANCELLED_BY_CUSTOMER)),
            entry(CARRIER_SELECTED, Set.of(PUBLISHED, CANCELLED_BY_CUSTOMER)),
            entry(CONFIRMED, Set.of(CANCELLED_BY_CUSTOMER)),
            entry(DEPARTING_TO_LOADING, Set.of(CANCELLED_BY_CUSTOMER)),
            entry(ARRIVED_AT_LOADING, Set.of(CANCELLED_BY_CUSTOMER)),
            entry(LOADING_IN_PROGRESS, Set.of()),
            entry(CARGO_ACCEPTED_BY_CARRIER, Set.of()),
            entry(DEPARTING_TO_UNLOADING, Set.of()),
            entry(ARRIVED_AT_UNLOADING, Set.of()),
            entry(UNLOADING_IN_PROGRESS, Set.of()),
            entry(CARGO_DELIVERED, Set.of()),
            entry(DOCUMENT_PENDING_CARRIER, Set.of()),
            entry(AWAITING_CUSTOMER_CONFIRMATION, Set.of()),
            entry(AWAITING_PAYMENT, Set.of(PAID)),
            entry(PAID, Set.of(RATING_PENDING)),
            entry(RATING_PENDING, Set.of(COMPLETED)),
            entry(COMPLETED, Set.of()),
            entry(CANCELLED_BY_CUSTOMER, Set.of())
    );

    // Статическое поле с правилами переходов для перевозчика
    private static final Map<RequestStatus, Set<RequestStatus>> ALLOWED_TRANSITIONS_CARRIER = Map.ofEntries(
            entry(DRAFT, Set.of()),
            entry(PUBLISHED, Set.of()),
            entry(CARRIER_SELECTED, Set.of(PUBLISHED, CONFIRMED)),
            entry(CONFIRMED, Set.of(PUBLISHED, DEPARTING_TO_LOADING)),
            entry(DEPARTING_TO_LOADING, Set.of(PUBLISHED, ARRIVED_AT_LOADING)),
            entry(ARRIVED_AT_LOADING, Set.of(PUBLISHED, LOADING_IN_PROGRESS)),
            entry(LOADING_IN_PROGRESS, Set.of(CARGO_ACCEPTED_BY_CARRIER)),
            entry(CARGO_ACCEPTED_BY_CARRIER, Set.of(DEPARTING_TO_UNLOADING)),
            entry(DEPARTING_TO_UNLOADING, Set.of(ARRIVED_AT_UNLOADING)),
            entry(ARRIVED_AT_UNLOADING, Set.of(UNLOADING_IN_PROGRESS)),
            entry(UNLOADING_IN_PROGRESS, Set.of(CARGO_DELIVERED)),
            entry(CARGO_DELIVERED, Set.of(DOCUMENT_PENDING_CARRIER, AWAITING_PAYMENT, COMPLETED /*TODO: удалить после внедрения полноценного флоу оплаты*/)),
            entry(DOCUMENT_PENDING_CARRIER, Set.of(AWAITING_CUSTOMER_CONFIRMATION)),
            entry(AWAITING_CUSTOMER_CONFIRMATION, Set.of(AWAITING_PAYMENT)),
            entry(AWAITING_PAYMENT, Set.of()),
            entry(PAID, Set.of(RATING_PENDING)),
            entry(RATING_PENDING, Set.of()),
            entry(COMPLETED, Set.of()),
            entry(CANCELLED_BY_CUSTOMER, Set.of())
    );

    RequestStatus(String code, String displayName, String carrierMessage) {
        this.code = code;
        this.displayName = displayName;
        this.carrierMessage = carrierMessage;
    }

    /**
     * Проверяет, можно ли перейти из текущего статуса в указанный.
     */
    public boolean canTransitionTo(RequestStatus target, Role role) {
        if (target == null) {
            return false;
        }

        var allowed = switch(role) {
            case SHIPPER -> ALLOWED_TRANSITIONS_SHIPPER.get(this);
            case CARRIER -> ALLOWED_TRANSITIONS_CARRIER.get(this);
        };

        return allowed != null && allowed.contains(target);
    }
}