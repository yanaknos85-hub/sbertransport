package ru.sberbank.ditsib.transport.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.exceptions.WrongTransportTypeException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Возможные статусы заявок. Порядок важен, так как используется при сортировке.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum TripRequestStatus {

    // Такси
    /**
     * На согласовании
     */
    TAXI_AWAITING_APPROVAL(Constants.AWAITING_APPROVAL, true, true, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Отменено
     */
    TAXI_CANCELLED(Constants.CANCELED, false, false, false, true, Color.DARK_GRAY),

    /**
     * Согласована
     */
    TAXI_APPROVED(Constants.APPROVED, true, false, true, false, Color.LIME_GREEN),

    /**
     * Ожидайте назначения водителя
     */
    TAXI_AWAITING_SEARCH(Constants.DRIVER_AWAITING, true, false, true, false, Color.SOFT_BLUE),

    /**
     * Поиск водителя
     */
    TAXI_DRIVER_SEARCH(Constants.DRIVER_SEARCHING, false, false, true, false, Color.SOFT_BLUE),

    /**
     * Водитель назначен
     */
    TAXI_DRIVER_FOUND(Constants.DRIVER_FOUND, false, false, true, false, Color.SOFT_BLUE),

    /**
     * Водитель в пути
     */
    TAXI_DRIVER_ON_THE_WAY(Constants.DRIVER_ON_THE_WAY, false, false, true, false, Color.SOFT_BLUE),

    /**
     * Водитель ожидает в точке отправления
     */
    TAXI_DRIVER_ARRIVED(Constants.DRIVER_WAITING, false, false, true, false, Color.SOFT_BLUE),

    /**
     * Время бесплатного ожидания истекло
     */
    TAXI_FREE_TIME_EXPIRED("Время бесплатного ожидания истекло", false, false, true, false, Color.SOFT_BLUE),

    /**
     * Прибытие в промежуточный пункт на такси
     */
    TAXI_WAYPOINT_ARRIVED("Прибытие в промежуточный пункт на такси", false, false, true, false, Color.SOFT_BLUE),

    /**
     * Поездка началась
     */
    TAXI_TRIP_IN_PROGRESS(Constants.TRIP_STARTED, false, false, true, false, Color.SOFT_BLUE),

    /**
     * Поездка завершена
     */
    TAXI_TRIP_FINISHED(Constants.TRIP_FINISHED, false, false, false, true, Color.LIME_GREEN),

    // Личный транспорт
    /**
     * На согласовании
     */
    PERSONAL_AWAITING_APPROVAL(Constants.AWAITING_APPROVAL, true, true, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Согласована
     */
    PERSONAL_APPROVED(Constants.APPROVED, true, false, true, false, Color.LIME_GREEN),

    /**
     * Согласование присоединения к СП
     */
    PERSONAL_AWAITING_SHARED_RIDE_APPROVAL("Согласование присоединения к СП", false, false, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Присоединение не согласовано
     */
    PERSONAL_SHARED_RIDE_DECLINED("Присоединение не согласовано", true, false, false, true, Color.BRIGHT_RED),

    /**
     * Присоединение согласовано
     */
    PERSONAL_SHARED_RIDE_APPROVED("Присоединение согласовано", true, false, true, false, Color.LIME_GREEN),

    /**
     * Согласование доп. точек в маршруте на личном транспорте
     */
    PERSONAL_ADDITIONAL_WAYPOINTS_APPROVAL("Согласование доп. точек в маршруте на личном транспорте", true, false, true, false, Color.SOFT_BLUE),

    /**
     * Добавление дополнительных точек в маршрут было согласовано
     */
    PERSONAL_ADDITIONAL_WAYPOINTS_APPROVED("Добавление дополнительных точек в маршрут было согласовано", true, false, true, false, Color.LIME_GREEN),

    /**
     * Добавление дополнительных точек в маршрут было отклонено
     */
    PERSONAL_ADDITIONAL_WAYPOINTS_DECLINED("Добавление дополнительных точек в маршрут было отклонено", true, false, true, true, Color.BRIGHT_RED),

    /**
     * Напоминание о начале поездки
     */
    PERSONAL_TRIP_START_REMIND("Напоминание о начале поездки", false, false, true, false, Color.SOFT_BLUE),

    /**
     * Поездка
     */
    PERSONAL_TRIP_IN_PROGRESS("Поездка", false, false, true, false, Color.SOFT_BLUE),

    /**
     * Прибытие в промежуточный пункт на личном транспорте
     */
    PERSONAL_WAYPOINT_ARRIVED("Прибытие в промежуточный пункт на личном транспорте", false, false, true, false, Color.SOFT_BLUE),

    /**
     * Утверждение маршрута
     */
    PERSONAL_AWAITING_TRIP_APPROVAL("Утверждение маршрута", true, false, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Утверждено
     */
    PERSONAL_AWAITING_TRIP_APPROVED("Утверждено", true, false, true, false, Color.LIME_GREEN),

    /**
     * Не утверждено
     */
    PERSONAL_AWAITING_TRIP_DECLINED("Не утверждено", true, false, true, true, Color.BRIGHT_RED),

    /**
     * Проверка GenAI
     */
    GENAI_CHECK(Constants.AWAITING_GENAI_CHECK, false, false, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Формирование приказа на выплату
     */
    PERSONAL_ORDER_PAYMENT_FORMATION("Формирование приказа на выплату", false, false, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Ожидание выплаты
     */
    PERSONAL_PAYMENT_AWAITING("Ожидание выплаты", false, false, false, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Выплата произведена
     */
    PERSONAL_PAYMENT_DONE("Выплата произведена", false, false, false, true, Color.LIME_GREEN),

    /**
     * Выплата не произведена
     */
    PERSONAL_PAYMENT_DECLINED("Выплата не произведена", false, false, false, true, Color.BRIGHT_RED),

    /**
     * Отменено
     */
    PERSONAL_CANCELLED(Constants.CANCELED, false, false, false, true, Color.DARK_GRAY),

    /**
     * Поездка завершена
     */
    PERSONAL_TRIP_FINISHED(Constants.TRIP_FINISHED, false, false, false, true, Color.LIME_GREEN),

    // Статусы заявок на общественный транспорт
    /**
     * На согласовании
     */
    PUBLIC_AWAITING_APPROVAL(Constants.AWAITING_APPROVAL, true, true, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Согласована
     */
    PUBLIC_APPROVED("Согласована", true, false, true, false, Color.LIME_GREEN),

    /**
     * Подтверждение поездки
     */
    PUBLIC_TRIP_CONFIRMATION("Подтверждение поездки", true, false, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Поездка подтверждена
     */
    PUBLIC_TRIP_CONFIRMED("Поездка подтверждена", true, false, true, false, Color.LIME_GREEN),

    /**
     * Утверждение
     */
    PUBLIC_AWAITING_AFFIRMATIVE("Утверждение", true, true, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Утверждена
     */
    PUBLIC_AFFIRMED("Утверждена", true, false, true, false, Color.LIME_GREEN),

    /**
     * Формирование приказа на выплату
     */
    PUBLIC_ORDER_PAYMENT_FORMATION("Формирование приказа на выплату", false, false, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Ожидание выплаты
     */
    PUBLIC_PAYMENT_AWAITING("Ожидание выплаты", false, false, false, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Выплата произведена
     */
    PUBLIC_PAYMENT_DONE("Выплата произведена", false, false, false, true, Color.LIME_GREEN),

    /**
     * Выплата не произведена
     */
    PUBLIC_PAYMENT_NOT_DONE("Выплата не произведена", false, false, false, true, Color.BRIGHT_RED),

    /**
     * Отменено
     */
    PUBLIC_CANCELLED(Constants.CANCELED, false, false, false, true, Color.DARK_GRAY),


    // Статусы заявок на каршеринге
    /**
     * На согласовании
     */
    CARSHARING_AWAITING_APPROVAL(Constants.AWAITING_APPROVAL, true, true, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Согласована
     */
    CARSHARING_APPROVED(Constants.APPROVED, true, false, true, false, Color.LIME_GREEN),

    /**
     * Не согласована
     */
    CARSHARING_DECLINED("Не согласована", true, false, true, false, Color.BRIGHT_RED),

    /**
     * Поездка запланирована
     */
    CARSHARING_AWAITING_SEARCH("Поездка запланирована", true, false, true, false, Color.SOFT_BLUE),

    /**
     * Поездка началась
     */
    CARSHARING_TRIP_IN_PROGRESS(Constants.TRIP_STARTED, false, false, false, false, Color.SOFT_BLUE),

    /**
     * Утверждение маршрута
     */
    CARSHARING_AWAITING_TRIP_APPROVAL("Утверждение маршрута", true, false, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Поездка завершена
     */
    CARSHARING_TRIP_FINISHED(Constants.TRIP_FINISHED, false, false, false, true, Color.LIME_GREEN),

    /**
     * Отменено
     */
    CARSHARING_CANCELLED(Constants.CANCELED, false, false, false, true, Color.DARK_GRAY),

    // Статусы заявок по грузоперевозкам
    /**
     * Принята
     */
    CARGO_ACCEPTED("Принята", true, false, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Планирование
     */
    CARGO_PLANNING("Планирование", true, true, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Планирование завершено
     */
    CARGO_PLANNING_FINISHED("Планирование завершено", false, true, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * На согласовании
     */
    CARGO_AWAITING_APPROVAL(Constants.AWAITING_APPROVAL, true, true, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Согласовано
     */
    CARGO_APPROVED("Согласовано", true, false, true, false, Color.LIME_GREEN),

    /**
     * Отправлено контрагенту
     */
    CARGO_AWAITING_DATA("Отправлено контрагенту", false, false, false, false, Color.SOFT_BLUE),

    /**
     * На сборе
     */
    CARGO_AWAITING_TRANSFER("На сборе", false, false, false, false, Color.SOFT_BLUE),

    /**
     * Доставка
     */
    CARGO_TRANSFER_FINISHED("Доставка", false, false, false, false, Color.SOFT_BLUE),

    /**
     * Доставлено
     */
    CARGO_SHIPMENT_FINISHED("Доставлено", false, false, false, false, Color.LIME_GREEN),

    /**
     * Завершено
     */
    CARGO_DELIVERY_CONFIRMATION_FINISHED("Завершено", false, false, false, true, Color.DARK_GRAY),

    /**
     * Отменено
     */
    CARGO_CANCELED(Constants.CANCELED, false, false, false, true, Color.BRIGHT_RED),

    //Статусы заявок по ремонту
    /**
     * На согласовании
     */
    REPAIR_AWAITING_APPROVAL(Constants.AWAITING_APPROVAL, true, true, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Согласовано
     */
    REPAIR_APPROVED("Согласовано", true, false, true, false, Color.LIME_GREEN),

    /**
     * Поиск эвакуатора
     */
    REPAIR_SEARCH_FOR_A_TOW_TRUCK("Поиск эвакуатора", false, false, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Эвакуатор едет к вам
     */
    REPAIR_TOW_TRUCK_IS_COMING ("Эвакуатор едет к вам", false, false, false, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Машина передана на эвакуатор
     */
    REPAIR_CAR_WAS_TRANSFERRED_TO_A_TOW_TRUCK("Машина передана на эвакуатор", false, false, false, false, Color.LIME_GREEN),

    /**
     * Запись на СТО
     */
    REPAIR_REGISTRATION_AT_THE_SERVICE_STATION("Запись на СТО", false, false, true, false, Color.LIME_GREEN),

    /**
     * Вас ожидают на СТО
     */
    REPAIR_EXPECTED_AT_THE_SERVICE_STATION("Вас ожидают на СТО", false, false, false, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Машина принята на СТО
     */
    REPAIR_CAR_WAS_ACCEPTED_AT_THE_SERVICE_STATION("Машина принята на СТО", false, false, false, false, Color.LIME_GREEN),

    /**
     * Диагностика автомобиля
     */
    REPAIR_DIAGNOSTICS("Диагностика автомобиля", false, false, false, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Согласование Заказ-наряда
     */
    REPAIR_WORK_ORDER_APPROVAL("Согласование Заказ-наряда", false, false, false, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Ремонтные работы
     */
    REPAIR_WORK("Ремонтные работы", false, false, false, false, Color.LIME_GREEN),

    /**
     * Выдача автомобиля
     */
    REPAIR_ISSUING_A_CAR("Выдача автомобиля", false, false, false, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Завершено
     */
    REPAIR_FINISHED("Завершено", false, false, false, true, Color.DARK_GRAY),

    /**
     * Отменено
     */
    REPAIR_CANCELED(Constants.CANCELED, false, false, false, true, Color.BRIGHT_RED),

    // Групповой трансфер
    /**
     * На согласовании
     */
    GROUP_TRANSFER_AWAITING_APPROVAL(Constants.AWAITING_APPROVAL, true, true, true, false, Color.VERY_LIGHT_ORANGE),

    /**
     * Отменено
     */
    GROUP_TRANSFER_CANCELLED(Constants.CANCELED, false, false, false, true, Color.DARK_GRAY),

    /**
     * Согласована
     */
    GROUP_TRANSFER_APPROVED(Constants.APPROVED, true, false, true, false, Color.LIME_GREEN),

    /**
     * Ожидайте назначения водителя
     */
    GROUP_TRANSFER_AWAITING_SEARCH(Constants.DRIVER_AWAITING, true, false, true, false, Color.SOFT_BLUE),

    /**
     * Поиск водителя
     */
    GROUP_TRANSFER_DRIVER_SEARCH(Constants.DRIVER_SEARCHING, false, false, true, false, Color.SOFT_BLUE),

    /**
     * Водитель назначен
     */
    GROUP_TRANSFER_DRIVER_FOUND(Constants.DRIVER_FOUND, false, false, true, false, Color.SOFT_BLUE),

    /**
     * Водитель в пути
     */
    GROUP_TRANSFER_DRIVER_ON_THE_WAY(Constants.DRIVER_ON_THE_WAY, false, false, true, false, Color.SOFT_BLUE),

    /**
     * Водитель ожидает в точке отправления
     */
    GROUP_TRANSFER_DRIVER_ARRIVED(Constants.DRIVER_WAITING, false, false, true, false, Color.SOFT_BLUE),

    /**
     * Поездка началась
     */
    GROUP_TRANSFER_TRIP_IN_PROGRESS(Constants.TRIP_STARTED, false, false, true, false, Color.SOFT_BLUE),

    /**
     * Поездка завершена
     */
    GROUP_TRANSFER_TRIP_FINISHED(Constants.TRIP_FINISHED, false, false, false, true, Color.LIME_GREEN);

    private final String description;
    private final boolean editable;
    private final boolean approvable;
    private final boolean cancelable;
    private final boolean terminal;
    private final String color;

    /**
     * Список статусов заявок на такси
     */
    public static final List<TripRequestStatus> TAXI_STATUSES =
            List.of(TAXI_AWAITING_APPROVAL, TAXI_APPROVED, TAXI_AWAITING_SEARCH, TAXI_DRIVER_SEARCH, TAXI_DRIVER_FOUND,
                    TAXI_DRIVER_ON_THE_WAY, TAXI_DRIVER_ARRIVED, TAXI_TRIP_IN_PROGRESS, TAXI_TRIP_FINISHED,
                    TAXI_CANCELLED, GENAI_CHECK);

    /**
     * Список статусов заявок на личный транспорт
     */
    public static final List<TripRequestStatus> PERSONAL_STATUSES =
            List.of(PERSONAL_AWAITING_APPROVAL, PERSONAL_APPROVED, PERSONAL_AWAITING_SHARED_RIDE_APPROVAL,
                    PERSONAL_SHARED_RIDE_DECLINED, PERSONAL_TRIP_IN_PROGRESS, PERSONAL_AWAITING_TRIP_APPROVAL,
                    PERSONAL_ORDER_PAYMENT_FORMATION, PERSONAL_PAYMENT_AWAITING, PERSONAL_PAYMENT_DONE, PERSONAL_TRIP_FINISHED,
                    PERSONAL_PAYMENT_DECLINED, PERSONAL_CANCELLED, GENAI_CHECK);

    /**
     * Список статусов заявок на общественный транспорт
     */
    public static final List<TripRequestStatus> PUBLIC_STATUSES =
            List.of(PUBLIC_AWAITING_APPROVAL, PUBLIC_TRIP_CONFIRMATION, PUBLIC_AWAITING_AFFIRMATIVE,
                    PUBLIC_ORDER_PAYMENT_FORMATION, PUBLIC_PAYMENT_AWAITING, PUBLIC_PAYMENT_DONE,
                    PUBLIC_PAYMENT_NOT_DONE, PUBLIC_CANCELLED, GENAI_CHECK);

    /**
     * Список статусов заявок на каршеринг
     */
    public static final List<TripRequestStatus> CARSHARING_STATUSES =
            List.of(CARSHARING_AWAITING_APPROVAL, CARSHARING_APPROVED, CARSHARING_DECLINED, CARSHARING_AWAITING_SEARCH,
                    CARSHARING_TRIP_IN_PROGRESS, CARSHARING_AWAITING_TRIP_APPROVAL, CARSHARING_TRIP_FINISHED,
                    CARSHARING_CANCELLED);

    /**
     * Список статусов заявок на грузоперевозки (для поиска заявок в журналах)
     */
    public static final List<TripRequestStatus> CARGO_STATUSES =
            List.of(CARGO_ACCEPTED, CARGO_AWAITING_APPROVAL, CARGO_APPROVED,
                    CARGO_AWAITING_DATA, CARGO_AWAITING_TRANSFER, CARGO_TRANSFER_FINISHED, CARGO_SHIPMENT_FINISHED,
                    CARGO_DELIVERY_CONFIRMATION_FINISHED, CARGO_CANCELED);

    /**
     * Список статусов заявок на ремонт
     */
    public static final List<TripRequestStatus> REPAIR_STATUSES = List.of(REPAIR_AWAITING_APPROVAL, REPAIR_APPROVED, REPAIR_SEARCH_FOR_A_TOW_TRUCK,
                                                                          REPAIR_TOW_TRUCK_IS_COMING, REPAIR_CAR_WAS_TRANSFERRED_TO_A_TOW_TRUCK,
                                                                          REPAIR_REGISTRATION_AT_THE_SERVICE_STATION,
                                                                          REPAIR_EXPECTED_AT_THE_SERVICE_STATION,
                                                                          REPAIR_CAR_WAS_ACCEPTED_AT_THE_SERVICE_STATION, REPAIR_DIAGNOSTICS,
                                                                          REPAIR_WORK_ORDER_APPROVAL, REPAIR_WORK, REPAIR_ISSUING_A_CAR,
                                                                          REPAIR_FINISHED, REPAIR_CANCELED);

    /**
     * Список статусов заявок на групповой трансфер
     */
    public static final List<TripRequestStatus> GROUP_TRANSFER_STATUSES =
            List.of(GROUP_TRANSFER_AWAITING_APPROVAL, GROUP_TRANSFER_APPROVED, GROUP_TRANSFER_AWAITING_SEARCH, GROUP_TRANSFER_DRIVER_SEARCH, GROUP_TRANSFER_DRIVER_FOUND,
                    GROUP_TRANSFER_DRIVER_ON_THE_WAY, GROUP_TRANSFER_DRIVER_ARRIVED, GROUP_TRANSFER_TRIP_IN_PROGRESS, GROUP_TRANSFER_TRIP_FINISHED,
                    GROUP_TRANSFER_CANCELLED);

    /**
     * @return Получение списка статусов заявок, ожидающих подтверждения
     */
    public static Set<TripRequestStatus> getAwaitingApprovalStatuses() {
        return Set.of(TAXI_AWAITING_APPROVAL, PERSONAL_AWAITING_APPROVAL, PUBLIC_AWAITING_APPROVAL,
                      CARSHARING_AWAITING_APPROVAL, CARGO_AWAITING_APPROVAL, REPAIR_AWAITING_APPROVAL, GROUP_TRANSFER_AWAITING_APPROVAL);
    }

    /**
     * @return Получение списка статусов подтвержденных заявок
     */
    public static Set<TripRequestStatus> getApprovedStatuses() {
        return Set.of(TAXI_APPROVED, PERSONAL_APPROVED, CARSHARING_APPROVED, CARGO_APPROVED, REPAIR_APPROVED, GROUP_TRANSFER_APPROVED);
    }

    /**
     * @return Получение списка статусов завершенной поездки
     */
    public static Set<TripRequestStatus> getTripFinishStatuses() {
        return Set.of(TAXI_TRIP_FINISHED, CARSHARING_TRIP_FINISHED, GROUP_TRANSFER_TRIP_FINISHED);
    }

    /**
     * @return Получение списка статусов заявок, ожидающих подтверждение маршрута
     */
    public static Set<TripRequestStatus> getAwaitingTripApprovalStatuses() {
        return Set.of(PERSONAL_AWAITING_TRIP_APPROVAL, PUBLIC_AWAITING_AFFIRMATIVE);
    }

    /**
     * Получение статуса успешно завершенной заявки по типу транспорта
     *
     * @param transportType тип транспорта.
     * @return статус заявки.
     */
    public static TripRequestStatus getSuccessfullyCompletedServiceStatusByTransportType(TransportTypeEnum transportType){
        return switch (transportType){
            case TAXI -> TAXI_TRIP_FINISHED;
            case GROUP_TRANSFER -> GROUP_TRANSFER_TRIP_FINISHED;
            case PERSONAL -> PERSONAL_PAYMENT_DONE;
            case PUBLIC -> PUBLIC_PAYMENT_DONE;
            case DEDICATED, INDIVIDUAL, COURIER, DOMESTIC_COURIER, INTERREGIONAL -> CARGO_DELIVERY_CONFIRMATION_FINISHED;
            default -> throw new WrongTransportTypeException(transportType, Set.of(TransportTypeEnum.TAXI,
                    TransportTypeEnum.PERSONAL,
                    TransportTypeEnum.CARSHARING,
                    TransportTypeEnum.PUBLIC,
                    TransportTypeEnum.DEDICATED,
                    TransportTypeEnum.COURIER,
                    TransportTypeEnum.DOMESTIC_COURIER,
                    TransportTypeEnum.INDIVIDUAL,
                    TransportTypeEnum.INTERREGIONAL));
        };
    }

    /**
     * Получение статуса согласованной заявки по типу транспорта
     *
     * @param transportType тип транспорта.
     * @return статус заявки.
     */
    public static TripRequestStatus getApprovedStatusByTransportType(TransportTypeEnum transportType) {
        return switch (transportType) {
            case TAXI -> TAXI_APPROVED;
            case GROUP_TRANSFER -> GROUP_TRANSFER_APPROVED;
            case PERSONAL -> PERSONAL_APPROVED;
            case CARSHARING -> CARSHARING_APPROVED;
            case PUBLIC -> PUBLIC_TRIP_CONFIRMATION;
            case DEDICATED, INDIVIDUAL, COURIER, DOMESTIC_COURIER, INTERREGIONAL -> CARGO_APPROVED;
            case OFFICIAL, PRIVATE, SPECIAL -> REPAIR_APPROVED;
            default -> throw new WrongTransportTypeException(transportType, Set.of(TransportTypeEnum.TAXI,
                                                                                   TransportTypeEnum.PERSONAL,
                                                                                   TransportTypeEnum.CARSHARING,
                                                                                   TransportTypeEnum.PUBLIC,
                                                                                   TransportTypeEnum.DEDICATED,
                                                                                   TransportTypeEnum.INDIVIDUAL,
                                                                                   TransportTypeEnum.COURIER,
                                                                                   TransportTypeEnum.DOMESTIC_COURIER,
                                                                                   TransportTypeEnum.INTERREGIONAL,
                                                                                   TransportTypeEnum.OFFICIAL,
                                                                                   TransportTypeEnum.PRIVATE,
                                                                                   TransportTypeEnum.SPECIAL));
        };
    }

    /**
     * Получение статуса завершенной заявки по типу транспорта
     *
     * @param transportType тип транспорта.
     * @return статус заявки.
     */
    public static TripRequestStatus getCompletedStatusByTransportType(TransportTypeEnum transportType) {
        return switch (transportType) {
            case TAXI -> TAXI_TRIP_FINISHED;
            case GROUP_TRANSFER -> GROUP_TRANSFER_TRIP_FINISHED;
            case PERSONAL -> PERSONAL_PAYMENT_DECLINED;
            case CARSHARING -> CARSHARING_TRIP_FINISHED;
            case PUBLIC -> PUBLIC_PAYMENT_NOT_DONE;
            case DEDICATED, INDIVIDUAL, COURIER, DOMESTIC_COURIER, INTERREGIONAL -> CARGO_DELIVERY_CONFIRMATION_FINISHED;
            case OFFICIAL, PRIVATE, SPECIAL -> REPAIR_FINISHED;
            default -> throw new WrongTransportTypeException(transportType, Set.of(TransportTypeEnum.TAXI,
                                                                                   TransportTypeEnum.PERSONAL,
                                                                                   TransportTypeEnum.CARSHARING,
                                                                                   TransportTypeEnum.PUBLIC,
                                                                                   TransportTypeEnum.DEDICATED,
                                                                                   TransportTypeEnum.INDIVIDUAL,
                                                                                   TransportTypeEnum.COURIER,
                                                                                   TransportTypeEnum.DOMESTIC_COURIER,
                                                                                   TransportTypeEnum.INTERREGIONAL,
                                                                                   TransportTypeEnum.OFFICIAL,
                                                                                   TransportTypeEnum.PRIVATE,
                                                                                   TransportTypeEnum.SPECIAL));
        };
    }

    /**
     * Получение списка статусов отмененных заявок
     *
     * @return список статусов отмененных заявок.
     */
    public static Set<TripRequestStatus> getCanceledStatuses() {
        return Set.of(TAXI_CANCELLED, PERSONAL_CANCELLED, PUBLIC_CANCELLED, CARSHARING_CANCELLED, CARGO_CANCELED, REPAIR_CANCELED, GROUP_TRANSFER_CANCELLED);
    }

    /**
     * Получение статуса несогласованной заявки по типу транспорта.
     *
     * @param transportType тип транспорта.
     *
     * @return код статуса.
     */
    public static Integer getNotApprovedStatusesByTransportType(TransportTypeEnum transportType) {
        return switch (transportType) {
            case PERSONAL -> PersonalStatusCode.PERSONAL_NOT_APPROVED.getCode();
            case PUBLIC -> PublicStatusCode.PUBLIC_NOT_APPROVED.getCode();
            case DEDICATED, INDIVIDUAL, COURIER, DOMESTIC_COURIER, INTERREGIONAL -> CargoStatusCode.CARGO_NOT_APPROVED.getCode();
            case OFFICIAL, PRIVATE, SPECIAL -> RepairStatusCode.REPAIR_NOT_APPROVED.getCode();
            default -> throw new WrongTransportTypeException(transportType, Set.of(TransportTypeEnum.PERSONAL,
                                                                                   TransportTypeEnum.PUBLIC,
                                                                                   TransportTypeEnum.DEDICATED,
                                                                                   TransportTypeEnum.INDIVIDUAL,
                                                                                   TransportTypeEnum.COURIER,
                                                                                   TransportTypeEnum.DOMESTIC_COURIER,
                                                                                   TransportTypeEnum.INTERREGIONAL,
                                                                                   TransportTypeEnum.OFFICIAL,
                                                                                   TransportTypeEnum.PRIVATE,
                                                                                   TransportTypeEnum.SPECIAL));
        };
    }

    /**
     * Получение кода отклоненной заявки по типу транспорта.
     *
     * @param transportType тип транспорта.
     * @return код статуса.
     */
    public static Integer getDeclinedByTransportType(TransportTypeEnum transportType) {
        return switch (transportType) {
            case TAXI -> TaxiStatusCode.TAXI_DECLINED.getCode();
            case GROUP_TRANSFER -> GroupTransferStatusCode.GROUP_TRANSFER_DECLINED.getCode();
            case PERSONAL -> PersonalStatusCode.PERSONAL_DECLINED.getCode();
            case CARSHARING -> CarsharingStatusCode.CARSHARING_DECLINED.getCode();
            case PUBLIC -> PublicStatusCode.PUBLIC_NOT_APPROVED.getCode();
            case DEDICATED, INDIVIDUAL, COURIER, DOMESTIC_COURIER, INTERREGIONAL -> CargoStatusCode.CARGO_DECLINED.getCode();
            case OFFICIAL, PRIVATE, SPECIAL -> RepairStatusCode.REPAIR_DECLINED.getCode();
            default -> throw new WrongTransportTypeException(transportType, Set.of(TransportTypeEnum.TAXI,
                                                                                   TransportTypeEnum.PERSONAL,
                                                                                   TransportTypeEnum.CARSHARING,
                                                                                   TransportTypeEnum.PUBLIC,
                                                                                   TransportTypeEnum.DEDICATED,
                                                                                   TransportTypeEnum.INDIVIDUAL,
                                                                                   TransportTypeEnum.COURIER,
                                                                                   TransportTypeEnum.DOMESTIC_COURIER,
                                                                                   TransportTypeEnum.INTERREGIONAL,
                                                                                   TransportTypeEnum.OFFICIAL,
                                                                                   TransportTypeEnum.PRIVATE,
                                                                                   TransportTypeEnum.SPECIAL));
        };
    }

    /**
     * Получение списка кодов отклоненных заявок по типу транспорта.
     *
     * @param transportType тип транспорта.
     * @return список кодов статуса.
     */
    public static List<Integer> getDeclinedCodesByTransportType(TransportTypeEnum transportType) {
        return switch (transportType) {
            case TAXI -> List.of(TaxiStatusCode.TAXI_DECLINED.getCode());
            case GROUP_TRANSFER -> List.of(GroupTransferStatusCode.GROUP_TRANSFER_DECLINED.getCode());
            case PERSONAL -> List.of(PersonalStatusCode.PERSONAL_DECLINED.getCode(),
                                     PersonalStatusCode.PERSONAL_DECLINED_BY_EXPIRATION_TIME.getCode());
            case CARSHARING -> List.of(CarsharingStatusCode.CARSHARING_DECLINED.getCode(),
                                       CarsharingStatusCode.CARSHARING_DECLINED_BY_EXPIRATION_TIME.getCode());
            case PUBLIC -> List.of(PublicStatusCode.PUBLIC_DECLINED.getCode(),
                                   PublicStatusCode.PUBLIC_DECLINED_AT_EXPIRATION.getCode());
            case DEDICATED, INDIVIDUAL, COURIER, DOMESTIC_COURIER, INTERREGIONAL -> List.of(CargoStatusCode.CARGO_DECLINED.getCode(),
                                                              CargoStatusCode.CARGO_DECLINED_BY_EXPIRATION_TIME.getCode());
            case OFFICIAL, PRIVATE, SPECIAL -> List.of(RepairStatusCode.REPAIR_DECLINED.getCode(),
                                                       RepairStatusCode.REPAIR_BY_EXPIRATION_TIME.getCode());
            default -> throw new WrongTransportTypeException(transportType, Set.of(TransportTypeEnum.TAXI,
                                                                                   TransportTypeEnum.PERSONAL,
                                                                                   TransportTypeEnum.CARSHARING,
                                                                                   TransportTypeEnum.PUBLIC,
                                                                                   TransportTypeEnum.DEDICATED,
                                                                                   TransportTypeEnum.INDIVIDUAL,
                                                                                   TransportTypeEnum.COURIER,
                                                                                   TransportTypeEnum.DOMESTIC_COURIER,
                                                                                   TransportTypeEnum.INTERREGIONAL,
                                                                                   TransportTypeEnum.OFFICIAL,
                                                                                   TransportTypeEnum.PRIVATE,
                                                                                   TransportTypeEnum.SPECIAL));
        };
    }

    /**
     * Получение кодов отмененных сотрудником заявок.
     *
     * @param transportType тип транспорта.
     * @return код отмененной заявки.
     */
    public static Object getCanceledByEmployee(TransportTypeEnum transportType) {
        return switch (transportType) {
            case TAXI -> TaxiStatusCode.TAXI_CANCELLED_BY_EMPLOYEE;
            case GROUP_TRANSFER -> GroupTransferStatusCode.GROUP_TRANSFER_CANCELLED_BY_EMPLOYEE;
            case PERSONAL -> PersonalStatusCode.PERSONAL_CANCELLED_BY_EMPLOYEE;
            case CARSHARING -> CarsharingStatusCode.CARSHARING_CANCELLED_BY_EMPLOYEE;
            case PUBLIC -> PublicStatusCode.PUBLIC_CANCELLED_BY_EMPLOYEE;
            case DEDICATED, INDIVIDUAL, COURIER, DOMESTIC_COURIER, INTERREGIONAL -> CargoStatusCode.CARGO_CANCELLED_BY_EMPLOYEE;
            case OFFICIAL, PRIVATE, SPECIAL -> RepairStatusCode.REPAIR_CANCELLED_BY_EMPLOYEE;
            default -> throw new WrongTransportTypeException(transportType, Set.of(TransportTypeEnum.TAXI,
                                                                                   TransportTypeEnum.PERSONAL,
                                                                                   TransportTypeEnum.CARSHARING,
                                                                                   TransportTypeEnum.PUBLIC,
                                                                                   TransportTypeEnum.DEDICATED,
                                                                                   TransportTypeEnum.INDIVIDUAL,
                                                                                   TransportTypeEnum.COURIER,
                                                                                   TransportTypeEnum.DOMESTIC_COURIER,
                                                                                   TransportTypeEnum.INTERREGIONAL,
                                                                                   TransportTypeEnum.OFFICIAL,
                                                                                   TransportTypeEnum.PRIVATE,
                                                                                   TransportTypeEnum.SPECIAL));
        };
    }

    /**
     * Получение кодов отмененных инженером заявок.
     *
     * @param transportType тип транспорта.
     * @return код отмененной заявки.
     */
    public static Object getCanceledByEngineer(TransportTypeEnum transportType) {
        return switch (transportType) {
            case DEDICATED, INDIVIDUAL, COURIER, DOMESTIC_COURIER, INTERREGIONAL -> CargoStatusCode.CARGO_CANCELLED_BY_ENGINEER;
            default -> throw new WrongTransportTypeException(transportType, Set.of(TransportTypeEnum.TAXI,
                                                                                   TransportTypeEnum.PERSONAL,
                                                                                   TransportTypeEnum.CARSHARING,
                                                                                   TransportTypeEnum.PUBLIC,
                                                                                   TransportTypeEnum.DEDICATED,
                                                                                   TransportTypeEnum.INDIVIDUAL,
                                                                                   TransportTypeEnum.COURIER,
                                                                                   TransportTypeEnum.DOMESTIC_COURIER,
                                                                                   TransportTypeEnum.INTERREGIONAL,
                                                                                   TransportTypeEnum.OFFICIAL,
                                                                                   TransportTypeEnum.PRIVATE,
                                                                                   TransportTypeEnum.SPECIAL));
        };
    }

    /**
     * Получение статуса по названию.
     *
     * @param status статус.
     *
     * @return статус.
     */
    public static Optional<TripRequestStatus> getByName(String status) {
        if (status != null) {
            return Optional.of(TripRequestStatus.valueOf(status));
        }
        return Optional.empty();
    }

    /**
     * Определение, является ли текущий статус между указанными.
     *
     * @param start начальный статус для проверки (включительно).
     * @param end конченый статус для проверки (исключительно).
     *
     * @return <code>true</code> если текущий статус между указанными.
     */
    public boolean between(TripRequestStatus start, TripRequestStatus end) {
        return (ordinal() >= start.ordinal()) && (ordinal() < end.ordinal());
    }

    /**
     * Получения списка следующих статусов.
     *
     * @param toCompare статус для выборки.
     * @return набор следующих статусов.
     */
    @SuppressWarnings("java:S3958")
    public static Set<TripRequestStatus> getSetOfGreaterOrEqualToStatus(TripRequestStatus toCompare) {
        return EnumSet.copyOf(Arrays.stream(TripRequestStatus.values())
                                    .filter(elt -> elt.ordinal() >= toCompare.ordinal()).toList());
    }

    /**
     * Получения списка предыдущих статусов.
     *
     * @param toCompare статус для выборки.
     * @return набор предыдущих статусов.
     */
    @SuppressWarnings("java:S3958")
    public static Set<TripRequestStatus> getSetOfLesserWithoutDraft(TripRequestStatus toCompare) {
        var collect = Arrays.stream(TripRequestStatus.values())
                                                .filter(elt -> elt.ordinal() < toCompare.ordinal())
                                                .toList();
        return EnumSet.copyOf(collect);
    }

    /**
     * Получение статусов по кодам.
     *
     * @param collection коды.
     * @return статусы.
     */
    @SuppressWarnings("java:S3958")
    public static Set<TripRequestStatus> getSetOfIntegerCollection(Collection<Integer> collection) {
        var collect = collection.stream().filter(ord -> ord < TripRequestStatus.values().length)
                                                    .map(ord -> TripRequestStatus.values()[ord])
                                                    .toList();
        return EnumSet.copyOf(collect);
    }

    /**
     * Получение конечных статусов.
     *
     * @param isTerminate признак конечного статуса. Если <code>false</code>, будут возвращены промежуточные статусы.
     * @return набор статусов.
     */
    public static Set<TripRequestStatus> getTerminalStatus(boolean isTerminate) {
        return EnumSet.copyOf(Arrays.stream(TripRequestStatus.values())
                                    .filter(elt -> elt.terminal == isTerminate).collect(
                        Collectors.toSet()));
    }

    /**
     * Получение статуса по строке.
     *
     * @param status исходная строка.
     * @return статус.
     */
    public static Optional<TripRequestStatus> getFromString(String status) {
        for (TripRequestStatus value : TripRequestStatus.values()) {
            if (value.toString().equals(status)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    /**
     * Список кодов статусов.
     */
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum TaxiStatusCode implements StatusCode {

        /**
         * Поездка завешена с оценкой.
         */
        TAXI_TRIP_FINISHED_RATED(101, Constants.FINISH_RATED, TAXI_TRIP_FINISHED),

        /**
         * Поездка завешена без оценки.
         */
        TAXI_TRIP_FINISHED_NOT_RATED(102, Constants.FINISH_NOT_RATED, TAXI_TRIP_FINISHED),

        /**
         * Поездка завершена системой.
         */
        TAXI_TRIP_CLOSED_BY_SYSTEM(103, Constants.CLOSED_BY_SYSTEM, TAXI_TRIP_FINISHED),

        /**
         * Поездка отменена пользователем.
         */
        TAXI_CANCELLED_BY_EMPLOYEE(201, Constants.CLIENT_CANCELED, TAXI_CANCELLED),

        /**
         * Поездка не согласована.
         */
        TAXI_DECLINED(202, Constants.NOT_APPROVED, TAXI_CANCELLED),

        /**
         * Поездка не согласована - вышел КС.
         */
        TAXI_CANCELLED_BY_EXPIRATION_TIME(203, Constants.NOT_APPROVED_EXPIRED, TAXI_CANCELLED),

        /**
         * Поездка отменена водителем.
         */
        TAXI_CANCELLED_BY_DRIVER(207, Constants.DRIVER_CANCELED, TAXI_CANCELLED);
        
        private final int code;

        private final String description;

        private final TripRequestStatus tripRequestStatus;

        /**
         * Получение статуса по коду.
         *
         * @param code код.
         * @return статус.
         */
        public static Optional<TaxiStatusCode> findByCode(Integer code) {
            return Optional.ofNullable(code)
                           .flatMap(search -> Arrays.stream(values()).filter(item -> search.equals(item.getCode())).findFirst());
        }
    }

    /**
     * Статусы ЛТ.
     */
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum PersonalStatusCode implements StatusCode {

        /**
         * Отмена пользователем
         */
        PERSONAL_CANCELLED_BY_EMPLOYEE(201, Constants.CLIENT_CANCELED, PERSONAL_CANCELLED),

        /**
         * Не согласовано
         */
        PERSONAL_DECLINED(202, Constants.NOT_APPROVED, PERSONAL_CANCELLED),

        /**
         * Не согласовано - КС
         */
        PERSONAL_DECLINED_BY_EXPIRATION_TIME(203, Constants.NOT_APPROVED_EXPIRED, PERSONAL_CANCELLED),

        /**
         *
         */
        PERSONAL_NOT_APPROVED(204, Constants.NOT_CONFIRMED, PERSONAL_CANCELLED),

        /**
         * Не утверждено - КС
         */
        PERSONAL_NOT_APPROVED_BY_EXPIRATION_TIME(205, Constants.NOT_CONFIRMED_EXPIRED, PERSONAL_CANCELLED),

        /**
         * КС
         */
        PERSONAL_BY_EXPIRATION_TIME(206, Constants.EXPIRED, PERSONAL_CANCELLED),

        /**
         * Отмена водителем
         */
        PERSONAL_CANCELLED_BY_DRIVER(207, Constants.DRIVER_CANCELED, PERSONAL_CANCELLED);
        
        private final int code;
        private final String description;
        private final TripRequestStatus tripRequestStatus;

        /**
         * Получение статуса по коду.
         *
         * @param code код.
         *
         * @return статус.
         */
        public static Optional<PersonalStatusCode> findByCode(Integer code) {
            return Optional.ofNullable(code)
                           .flatMap(search -> Arrays.stream(values()).filter(item -> search.equals(item.getCode())).findFirst());
        }
    }

    /**
     * Статусы каршеринга.
     */
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum CarsharingStatusCode implements StatusCode {

        /**
         * Завершено - с оценкой.
         */
        CARSHARING_TRIP_FINISHED_RATED(101, Constants.FINISH_RATED, CARSHARING_TRIP_FINISHED),

        /**
         * Завершено - без оценки.
         */
        CARSHARING_TRIP_FINISHED_NOT_RATED(102, Constants.FINISH_NOT_RATED, CARSHARING_TRIP_FINISHED),

        /**
         * Отмена пользователем
         */
        CARSHARING_CANCELLED_BY_EMPLOYEE(201, Constants.CLIENT_CANCELED, CARSHARING_CANCELLED),

        /**
         * Не согласовано
         */
        CARSHARING_DECLINED(202, Constants.NOT_APPROVED, CARSHARING_CANCELLED),

        /**
         * Не согласовано - КС
         */
        CARSHARING_DECLINED_BY_EXPIRATION_TIME(203, Constants.NOT_APPROVED_EXPIRED, CARSHARING_CANCELLED),

        /**
         * КС
         */
        CARSHARING_BY_EXPIRATION_TIME(206, Constants.EXPIRED, CARSHARING_CANCELLED);
        
        private final int code;

        private final String description;

        private final TripRequestStatus tripRequestStatus;

        /**
         * Получение статуса по коду.
         *
         * @param code код.
         *
         * @return статус.
         */
        public static Optional<CarsharingStatusCode> findByCode(Integer code) {
            return Optional.ofNullable(code)
                           .flatMap(search -> Arrays.stream(values()).filter(item -> search.equals(item.getCode())).findFirst());
        }
    }

    /**
     * Статусы ОТ.
     */
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum PublicStatusCode implements StatusCode {

        /**
         * Отмена пользователем
         */
        PUBLIC_CANCELLED_BY_EMPLOYEE(201, Constants.CLIENT_CANCELED, PUBLIC_CANCELLED),

        /**
         * Не согласовано
         */
        PUBLIC_DECLINED(202, Constants.NOT_APPROVED, PUBLIC_CANCELLED),

        /**
         * Не согласовано - КС
         */
        PUBLIC_DECLINED_AT_EXPIRATION(203, Constants.NOT_APPROVED_EXPIRED, PUBLIC_CANCELLED),

        /**
         * Не утверждено
         */
        PUBLIC_NOT_APPROVED(204, Constants.NOT_CONFIRMED, PUBLIC_CANCELLED),

        /**
         * Не утверждено - КС
         */
        PUBLIC_NOT_APPROVED_BY_EXPIRATION_TIME(205, Constants.NOT_CONFIRMED_EXPIRED, PUBLIC_CANCELLED),

        /**
         * КС
         */
        PUBLIC_BY_EXPIRATION_TIME(206, Constants.EXPIRED, PUBLIC_CANCELLED);
        
        @Getter(value = AccessLevel.PRIVATE)
        private static final Map<Integer, PublicStatusCode> codeMap =
                Arrays.stream(PublicStatusCode.values())
                      .collect(Collectors.toMap(PublicStatusCode::getCode,
                                                elt -> elt));
        private final int code;
        private final String description;
        private final TripRequestStatus tripRequestStatus;

        /**
         * Получение статуса по коду.
         *
         * @param code код.
         *
         * @return статус.
         */
        public static Optional<PublicStatusCode> findByCode(Integer code) {
            return Optional.ofNullable(code)
                           .flatMap(search -> Arrays.stream(values()).filter(item -> search.equals(item.getCode())).findFirst());
        }
        
    }

    /**
     * Интерфейс статус-кодов.
     */
    public interface StatusCode {

        /**
         * @return название.
         */
        String name();

        /**
         * @return код.
         */
        int getCode();

        /**
         * @return описание.
         */
        String getDescription();

        /**
         * @return статус поездки.
         */
        TripRequestStatus getTripRequestStatus();
    }

    /**
     * Статусы грузовых перевозок.
     */
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum CargoStatusCode {

        /**
         * Отмена пользователем
         */
        CARGO_CANCELLED_BY_EMPLOYEE(800, Constants.CLIENT_CANCELED, CARGO_CANCELED),

        /**
         * Отмена инженером
         */
        CARGO_CANCELLED_BY_ENGINEER (801, Constants.ENGINEER_CANCELED, CARGO_CANCELED),

        /**
         * Отмена после отмены всех заявок
         */
        CARGO_CANCELLED_BY_ALL_REQUESTS (802, Constants.ALL_REQUESTS_CANCELED, CARGO_CANCELED),

        /**
         * Отмена контрагентом
         */
        CARGO_CANCELLED_BY_CONTRACTOR (803, Constants.CONTRACTOR_CANCELED, CARGO_CANCELED),

        /**
         * Не принято контрагентом
         */
        CARGO_CANCELLED_BY_CONTRACTOR_REJECT (807, Constants.CONTRACTOR_REJECT_CANCELED, CARGO_CANCELED),

        /**
         * Не согласовано
         */
        CARGO_DECLINED(202, Constants.NOT_APPROVED, CARGO_CANCELED),

        /**
         * Не согласовано - КС
         */
        CARGO_DECLINED_BY_EXPIRATION_TIME(203, Constants.NOT_APPROVED_EXPIRED, CARGO_CANCELED),

        /**
         * Не утверждено
         */
        CARGO_NOT_APPROVED(204, Constants.NOT_CONFIRMED, CARGO_CANCELED),

        /**
         * Не утверждено - КС
         */
        CARGO_NOT_APPROVED_BY_EXPIRATION_TIME(205, Constants.NOT_CONFIRMED_EXPIRED, CARGO_CANCELED),

        /**
         * КС
         */
        CARGO_BY_EXPIRATION_TIME(206, Constants.EXPIRED, CARGO_CANCELED);
        
        private final int code;
        private final String description;
        private final TripRequestStatus tripRequestStatus;
    }

    /**
     * Статусы ремонта.
     */
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum RepairStatusCode {

        /**
         * Отмена пользователем
         */
        REPAIR_CANCELLED_BY_EMPLOYEE(201, Constants.CLIENT_CANCELED, REPAIR_CANCELED),

        /**
         * Не согласовано
         */
        REPAIR_DECLINED(202, Constants.NOT_APPROVED, REPAIR_CANCELED),

        /**
         * Не согласовано - КС
         */
        REPAIR_DECLINED_BY_EXPIRATION_TIME(203, Constants.NOT_APPROVED_EXPIRED, REPAIR_CANCELED),

        /**
         * Не утверждено
         */
        REPAIR_NOT_APPROVED(204, Constants.NOT_CONFIRMED, REPAIR_CANCELED),

        /**
         * Не утверждено - КС
         */
        REPAIR_NOT_APPROVED_BY_EXPIRATION_TIME(205, Constants.NOT_CONFIRMED_EXPIRED, REPAIR_CANCELED),

        /**
         * КС
         */
        REPAIR_BY_EXPIRATION_TIME(206, Constants.EXPIRED, REPAIR_CANCELED);
        
        private final int code;
        private final String description;
        private final TripRequestStatus tripRequestStatus;
    }

    /**
     * Список кодов статусов.
     */
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum GroupTransferStatusCode implements StatusCode {

        /**
         * Поездка завершена системой.
         */
        GROUP_TRANSFER_TRIP_CLOSED_BY_SYSTEM(103, Constants.CLOSED_BY_SYSTEM, GROUP_TRANSFER_TRIP_FINISHED),

        /**
         * Поездка отменена пользователем.
         */
        GROUP_TRANSFER_CANCELLED_BY_EMPLOYEE(201, Constants.CLIENT_CANCELED, GROUP_TRANSFER_CANCELLED),

        /**
         * Поездка не согласована.
         */
        GROUP_TRANSFER_DECLINED(202, Constants.NOT_APPROVED, GROUP_TRANSFER_CANCELLED),

        /**
         * Поездка не согласована - вышел КС.
         */
        GROUP_TRANSFER_CANCELLED_BY_EXPIRATION_TIME(203, Constants.NOT_APPROVED_EXPIRED, GROUP_TRANSFER_CANCELLED),

        /**
         * Поездка отменена водителем.
         */
        GROUP_TRANSFER_CANCELLED_BY_DRIVER(207, Constants.DRIVER_CANCELED, GROUP_TRANSFER_CANCELLED);

        private final int code;

        private final String description;

        private final TripRequestStatus tripRequestStatus;

        /**
         * Получение статуса по коду.
         *
         * @param code код.
         * @return статус.
         */
        public static Optional<GroupTransferStatusCode> findByCode(Integer code) {
            return Optional.ofNullable(code)
                    .flatMap(search -> Arrays.stream(values()).filter(item -> search.equals(item.getCode())).findFirst());
        }
    }

    /**
     * Получение статуса заявки на поездку из внешнего статуса.
     *
     * @param inboundTaxiTripStatus внешний статус.
     *
     * @return статус заявки на поездку.
     */
    public static TripRequestStatus fromContractorStatus(InboundTaxiTripStatus inboundTaxiTripStatus) {
        return switch (inboundTaxiTripStatus) {
            case SENT_TO_CONTRACTOR, WAITING_FOR_ASSIGNMENT -> TAXI_AWAITING_SEARCH;
            case DRIVER_ASSIGNED, DRIVER_APPROVED -> TAXI_DRIVER_FOUND;
            case DRIVER_ON_THE_WAY -> TAXI_DRIVER_ON_THE_WAY;
            case DRIVER_ARRIVED -> TAXI_DRIVER_ARRIVED;
            case TRIP_IN_PROGRESS -> TAXI_TRIP_IN_PROGRESS;
            case ORDER_FINISHED -> TAXI_TRIP_FINISHED;
            default -> TAXI_CANCELLED;
        };
    }

    /**
     * Получение статуса заявки на перевозку из внешнего статуса.
     *
     * @param inboundTaxiTripStatus внешний статус.
     *
     * @return статус заявки на перевозку.
     */
    public static TripRequestStatus cargoStatusFromContractorStatus(InboundTaxiTripStatus inboundTaxiTripStatus) {
        return switch (inboundTaxiTripStatus) {
            case SENT_TO_CONTRACTOR, WAITING_FOR_ASSIGNMENT -> CARGO_AWAITING_DATA;
            case DRIVER_ASSIGNED, DRIVER_APPROVED, DRIVER_ON_THE_WAY -> CARGO_AWAITING_TRANSFER;
            case DRIVER_ARRIVED, TRIP_IN_PROGRESS -> CARGO_TRANSFER_FINISHED;
            case ORDER_FINISHED -> CARGO_SHIPMENT_FINISHED;
            default -> CARGO_CANCELED;
        };
    }

    /**
     * Цвет.
     */
    private static class Color {

        /**
         * Светло-оранжевый.
         */
        public static final String VERY_LIGHT_ORANGE = "#FFB467";

        /**
         * Темно-серый.
         */
        public static final String DARK_GRAY = "#757575";

        /**
         * Зеленый.
         */
        public static final String LIME_GREEN = "#17D35B";

        /**
         * Голубой.
         */
        public static final String SOFT_BLUE = "#6979F7";

        /**
         * Красный.
         */
        public static final String BRIGHT_RED = "#FE5B3B";
    }

    @UtilityClass
    private static class Constants {

        private final String AWAITING_APPROVAL = "На согласовании";

        private final String CANCELED = "Отменено";

        private final String APPROVED = "Согласована";

        private final String DRIVER_AWAITING = "Ожидайте назначения водителя";

        private final String DRIVER_SEARCHING = "Поиск водителя";

        private final String DRIVER_FOUND = "Водитель назначен";

        private final String DRIVER_ON_THE_WAY = "Водитель в пути";

        private final String DRIVER_WAITING = "Водитель ожидает в точке отправления";

        private final String TRIP_STARTED = "Поездка началась";

        private final String TRIP_FINISHED = "Поездка завершена";
        
        private final String CLIENT_CANCELED = "Отмена пользователем";

        private final String  ENGINEER_CANCELED = "Отмена инженером";

        private final String  ALL_REQUESTS_CANCELED = "Отмена после отмены всех заявок";

        private final String  CONTRACTOR_CANCELED = "Отмена контрагентом";

        private final String  CONTRACTOR_REJECT_CANCELED = "Не принято контрагентом";

        private final String AWAITING_GENAI_CHECK = "Проверка GenAI";
        
        private final String NOT_APPROVED = "Не согласовано";
        
        private final String FINISH_RATED = "С оценкой";
        
        private final String FINISH_NOT_RATED = "Без оценки";
        
        private final String DRIVER_CANCELED = "Отменено водителем";

        private final String NOT_APPROVED_EXPIRED = "Не согласовано по истечению срока";

        private final String NOT_CONFIRMED = "Не утверждено";

        private final String NOT_CONFIRMED_EXPIRED = "Не утверждено по истечению срока";

        private final String EXPIRED = "По истечению срока";

        private final String CLOSED_BY_SYSTEM = "Закрыто системой";
    }
}
