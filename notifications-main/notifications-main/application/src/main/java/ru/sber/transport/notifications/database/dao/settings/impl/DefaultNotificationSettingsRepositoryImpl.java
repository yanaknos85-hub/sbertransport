package ru.sber.transport.notifications.database.dao.settings.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.sber.transport.notifications.database.dao.settings.DefaultNotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.dto.notification.*;
import ru.sber.transport.notifications.dto.timing.EventType;
import ru.sber.transport.notifications.dto.timing.TimingDto;
import ru.sber.transport.notifications.mapper.settings.NotificationSettingsMapper;
import ru.sber.transport.exceptions.EntityNotFoundException;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DefaultNotificationSettingsRepositoryImpl implements DefaultNotificationSettingsRepository {

    private static final String REQUEST_CREATION_TIME_FIELD_NAME = "request.creationTime";
    private static final String CREATION_TIME_FIELD_NAME = "creationTime";
    private static final String DESIRED_DATE_FIELD_NAME = "desiredDate";
    private static final String RATING_MESSAGE = "Поездка завершена. Не забудьте оценить качество сервиса.";
    private static final String DRIVER_DECLINED_TRIP = "Отказ от поездки водителем";
    private static final String PASSENGER_REQUESTED_DRIVER = "Пассажиру нужна связь с водителем";
    private static final String DRIVER_NOT_ASSIGNED = "Водитель не назначен";
    private static final String PASSENGER_CANCELLED_TRIP = "Отмена поездки пассажиром";
    private static final String LOW_RATED_TRIP = "Поездка с низкой оценкой";
    private static final String REQUEST_STARTED_WITH_INDICATOR = "Создана заявка с индикацией";

    private final List<NotificationSettings> notificationSettings;
    private final NotificationSettingsMapper mapper;

    @Value("${default.push.channel.enabled:true}")
    private Boolean defaultPushChannelEnabled;

    @Value("${default.email.channel.enabled:true}")
    private Boolean defaultEmailChannelEnabled;

    @Value("${default.sms.channel.enabled:false}")
    private Boolean defaultSmsChannelEnabled;

    @Value("${default.internal:true}")
    private boolean internal;

    @PostConstruct
    private void init() {
        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_TAXI, NotificationTypeDto.APPROVE,
                "Согласование заявки на такси", "notice_101",
                "Согласование поездки на такси {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.",
                internal ?
                        "Согласование поездки на такси {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока.\n" +
                                "\n" +
                                "Сигма - {sigmaLink} (ПРОМ)\n" +
                                "\n" +
                                "Альфа - {alphaLink} (ПРОМ)"
                        :
                        "Согласование поездки на такси {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока - {dzoLink}",
                EventType.AT_EVENT, REQUEST_CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_TAXI, NotificationTypeDto.APPROVE_STATUS,
                "Статус согласования заявки на такси", "notice_102",
                "По вашей поездке {humanReadableId} завершен этап \"Согласование\" со статусом \"{approveStatusDescription}\".",
                null,EventType.AT_EVENT, "request.approvalDate", Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_TAXI, NotificationTypeDto.WAITING_FOR_DRIVER,
                "Ожидание назначение водителя", "notice_103", "Ожидайте " +
                        "назначения водителя. Мы ищем для Вас лучшее " +
                        "решение.",
                null,EventType.DEADLINE, "desiredDate", Duration.ofMinutes(10)));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_TAXI, NotificationTypeDto.DRIVER_ASSIGNED,
                "Назначение водителя",
                "notice_104",
                "Заказ выполнит {driver.lastName} {driver.firstName} {driver.patronymic}, автомобиль {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.stateNumber}.",
                null,EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_TAXI, NotificationTypeDto.DRIVER_ASSIGNED_DISPATCHER,
                "Назначение водителя (диспетчер)",
                "notice_104_disp",
                "Заказ выполнит {driver.lastName} {driver.firstName} {driver.patronymic}, автомобиль {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.stateNumber}.",
                null,EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_TAXI, NotificationTypeDto.DRIVER_ASSIGNED_XML,
                "Назначение водителя (XML)",
                "notice_104_xml",
                "Заказ будет выполнен. {resolution}",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_TAXI, NotificationTypeDto.DRIVER_ARRIVED,
                "Водитель ожидает в пункте назначения", "notice_105",
                "Водитель {driver.lastName} {driver.firstName} {driver.patronymic} ожидает в пункте назначения, автомобиль {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.stateNumber}.",
                null,EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_TAXI, NotificationTypeDto.FREE_TIME_EXPIRED,
                "Время бесплатного ожидания истекло", "notice_106",
                "Время бесплатного ожидания истекло, каждая последующая минута ожидания " +
                        "составит {tariff.waitCostPerMin} руб.",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_TAXI, NotificationTypeDto.TRIP_STARTED,
                "Поездка на такси началась", "notice_107", "Поездка началась, " +
                        "время в " +
                        "пути составит {taxiTrip.tripFactDuration}",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_TAXI, NotificationTypeDto.WAYPOINT_ARRIVED,
                "Прибытие на такси в промежуточный пункт", "notice_108", "Вы " +
                        "прибыли в " +
                        "промежуточную точку. Помните, что платная " +
                        "стоимость ожидания составляет {tariff.waitCostPerMinIntermediate} руб./мин.",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_TAXI, NotificationTypeDto.WAYPOINT_WAITING_EXPIRED,
                "Превышение времени ожидания такси в промежуточном пункте", "notice_109",
                "Вы превысили нормативное время ожидания в {Превышение времени ожидания в промежуточном " +
                        "пункте. Время} минут.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_TAXI, NotificationTypeDto.TRIP_FINISHED,
                "Поездка на такси завершена", "notice_110", "Поездка завершена.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_TAXI, NotificationTypeDto.COOP_TRIP_ATTACHMENT,
                "Присоединение к совместной поездке на такси", "notice_111", "К" +
                        " вашей поездке присоединился {passenger" +
                        ".lastName}" +
                        " {passenger.firstName} {passenger.patronymic}",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_TAXI, NotificationTypeDto.COOP_TRIP_CHANGES,
                "Изменение в совместной поездке на такси", "notice_112",
                "Пассажир {passenger.lastName} {passenger" +
                        ".firstName} " +
                        "{passenger.patronymic} отказался от совместной поездки с вами № {magentaSharedRequest.magentaId}.",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PUBLIC, NotificationTypeDto.APPROVE,
                "Согласование заявки на общественный транспорт", "notice_201",
                "Согласование поездки на общественном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.",
                internal ?
                        "Согласование поездки на общественном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока.\n" +
                                "\n" +
                                "Сигма - {sigmaLink} (ПРОМ)\n" +
                                "\n" +
                                "Альфа - {alphaLink} (ПРОМ)"
                        :
                        "Согласование поездки на общественном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока - {dzoLink}",
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PUBLIC, NotificationTypeDto.APPROVE_STATUS,
                "Статус согласования заявки на общественный транспорт", "notice_202",
                "По вашей поездке {humanReadableId} завершен этап \"Согласование\" со статусом \"{approveStatusDescription}\".",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PUBLIC, NotificationTypeDto.TRIP_CONFIRM,
                "Подтверждение поездки на общественном транспорте", "notice_203",
                "Необходимо подтвердить расходы на поездку по заявке {humanReadableId}.",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PUBLIC, NotificationTypeDto.TRIP_AFFIRM,
                "Утверждение поездки на общественном транспорте", "notice_204",
                "Утверждение поездки на общественном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PUBLIC, NotificationTypeDto.TRIP_AFFIRM,
                "Статус утверждения поездки на общественном транспорте", "notice_205",
                "По вашей поездке {humanReadableId} завершен этап \"Утверждение\" со статусом \"{requestStatusDescription}\".",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PUBLIC, NotificationTypeDto.PAYMENT_STATUS,
                "Ожидание выплаты", "notice_206",
                "По вашей поездке {humanReadableId} завершен этап \"Ожидание выплаты\" со статусом \"{requestStatusDescription}\".",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        initPersonalTransport();

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CAR_SHARING, NotificationTypeDto.COOP_TRIP_ATTACHMENT_APPROVE,
                "Обработка заявки на присоединение к корп. каршерингу", "notice_401",
                "Поступила новая заявка на подключение к корп. каршерингу {request.humanReadableId}. Обработайте заявку до истечения контрольного срока.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CAR_SHARING, NotificationTypeDto.STATUS,
                "Статус обработки заявки на каршеринг", "notice_402",
                "По вашей поездке {humanReadableId} получено решение. Пожалуйста, ознакомьтесь!",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CAR_SHARING, NotificationTypeDto.APPROVE,
                "Согласование заявки на каршеринг", "notice_403",
                "Согласование поездки на каршеринге {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.",
                internal ?
                        "Согласование поездки на каршеринге {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока.\n" +
                                "\n" +
                                "Сигма - {sigmaLink} (ПРОМ)\n" +
                                "\n" +
                                "Альфа - {alphaLink} (ПРОМ)"
                        :
                        "Согласование поездки на каршеринге {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока - {dzoLink}",
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CAR_SHARING, NotificationTypeDto.APPROVE_STATUS,
                "Статус согласования заявки на каршеринг", "notice_404",
                "По вашей поездке {humanReadableId} завершен этап \"Согласование\" со статусом \"{approveStatusDescription}\".",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CAR_SHARING, NotificationTypeDto.TRANSPORT_BOOKING,
                "Бронирование автомобиля", "notice_405",
                "Вы забронировали автомобиль. У Вас есть <какой-то промежуток времени> минут для того," +
                        " чтобы добраться до него.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CAR_SHARING, NotificationTypeDto.TRANSPORT_OVERVIEW,
                "Осмотр автомобиля", "notice_406",
                "Осмотрите авто на наличие дефектов. Сделайте фото с разных ракурсов и загрузите в приложение.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CAR_SHARING, NotificationTypeDto.TRIP_STARTED,
                "Старт аренды. Поездка  на каршеринге началась", "notice_407",
                "Будьте вдвойне внимательны. Не забывайте, что Вы едете на арендованном автомобиле. Поехали…",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CAR_SHARING, NotificationTypeDto.WAYPOINT_ARRIVED,
                "Прибытие в промежуточный пункт на каршеринге", "notice_408",
                "Вы прибыли в промежуточную точку. Помните, что платная стоимость ожидания составляет" +
                        " <Стоимость за минуту ожидания каршеринга> руб./мин",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CAR_SHARING, NotificationTypeDto.TRANSPORT_BOOKING_FINISHED,
                "Завершение аренды", "notice_409",
                "Припаркуйтесь в разрешенном месте. Сфотографируйте автомобиль со всех сторон. " +
                        "Завершите аренду в приложении.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CAR_SHARING, NotificationTypeDto.TRIP_FINISHED,
                "Поездка на каршеринге завершена", "notice_410",
                RATING_MESSAGE,null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CAR_SHARING, NotificationTypeDto.COOP_TRIP_ATTACHMENT,
                "Присоединение к совместной поездке на каршеринге", "notice_411",
                "К вашей поезде присоединился {passenger.lastName} {passenger.firstName} " +
                        "{passenger.patronymic}.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CAR_SHARING, NotificationTypeDto.COOP_TRIP_ATTACHMENT,
                "Изменения в совместной поездке на каршеринге", "notice_412",
                "Пассажир {passenger.lastName} {passenger.firstName} {passenger.patronymic} отказался " +
                        "от совместной поездки с вами № {sharedRide}.", null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.APPROVE,
                "Согласование заявки на велосипед", "notice_501",
                "Согласование поездки на велосипеде {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.",
                internal ?
                        "Согласование поездки на велосипеде {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока.\n" +
                                "\n" +
                                "Сигма - {sigmaLink} (ПРОМ)\n" +
                                "\n" +
                                "Альфа - {alphaLink} (ПРОМ)"
                        :
                        "Согласование поездки на велосипеде {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока - {dzoLink}",
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.APPROVE_STATUS,
                "Статус согласования заявки на велосипед", "notice_502",
                "По вашей поездке {humanReadableId} завершен этап \"Согласование\" со статусом {approveStatusDescription}.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.TRANSPORT_BOOKING,
                "Бронирование велосипеда", "notice_503",
                "Вы забронировали велосипед. У Вас есть <какой-то промежуток времени> минут для того, " +
                        "чтобы добраться до него.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.TRANSPORT_OVERVIEW,
                "Осмотр велосипеда", "notice_504",
                "Осмотрите велосипед на наличие дефектов. Сделайте фото с разных ракурсов и загрузите" +
                        " в приложение.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.TRIP_STARTED,
                "Старт аренды. Поездка на велосипеде началась", "notice_505",
                "Будьте вдвойне внимательны. Не забывайте, что Вы едете на арендованном велосипеде. " +
                        "Поехали…",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.WAYPOINT_ARRIVED,
                "Прибытие в промежуточный пункт на велосипеде", "notice_506",
                "Вы прибыли в промежуточную точку. Помните, что платная стоимость ожидания " +
                        "составляет <Стоимость за минуту ожидания велосипеда> руб./мин.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.TRANSPORT_BOOKING_FINISHED,
                "Завершение аренды велосипеда", "notice_507",
                "Припаркуйтесь в разрешенном месте. Сфотографируйте велосипед всех сторон. Завершите аренду в приложении.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.TRIP_FINISHED,
                "Поездка на велосипеде завершена", "notice_508",
                RATING_MESSAGE, null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.APPROVE,
                "Согласование заявки на самокат", "notice_601",
                "Согласование поездки на самокате {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.",
                internal ?
                        "Согласование поездки на самокате {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока.\n" +
                                "\n" +
                                "Сигма - {sigmaLink} (ПРОМ)\n" +
                                "\n" +
                                "Альфа - {alphaLink} (ПРОМ)"
                        :
                        "Согласование поездки на самокате {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока - {dzoLink}",
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.APPROVE_STATUS,
                "Статус согласования заявки на самокат", "notice_602",
                "По вашей поездке {humanReadableId} завершен этап \"Согласование\" со статусом \"{approveStatusDescription}\".",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.TRANSPORT_BOOKING,
                "Бронирование самоката", "notice_603",
                "Вы забронировали самокат. У Вас есть <какой-то промежуток времени> минут для того, чтобы " +
                        "добраться до него.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.TRANSPORT_OVERVIEW,
                "Осмотр самоката", "notice_604",
                "Осмотрите самокат на наличие дефектов. Сделайте фото с разных ракурсов и " +
                        "загрузите в приложение.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.TRIP_STARTED,
                "Старт аренды. Поездка на самокате началась", "notice_605",
                "Будьте вдвойне внимательны. Не забывайте, что Вы едете на арендованном самокате. " +
                        "Поехали…",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.WAYPOINT_ARRIVED,
                "Прибытие в промежуточный пункт на самокате", "notice_606",
                "Вы прибыли в промежуточную точку. Помните, что платная стоимость ожидания составляет " +
                        "<Стоимость за минуту ожидания самоката> руб./мин.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.TRANSPORT_BOOKING_FINISHED,
                "Завершение аренды самоката", "notice_607",
                "Припаркуйтесь в разрешенном месте. Сфотографируйте самокат всех сторон. " +
                        "Завершите аренду в приложении.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_BICYCLE, NotificationTypeDto.TRIP_FINISHED,
                "Поездка на самокате завершена", "notice_608",
                RATING_MESSAGE,null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_LIMIT_PERSON, NotificationTypeDto.APPROVE,
                "Согласование заявки на личный лимит", "notice_701",
                "Согласование заявки <ФИО инициатора заявки> на пополнение личного лимита на " +
                        "{transportType}. Согласуйте заявку до {deadlineDateTime}",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_LIMIT_PERSON, NotificationTypeDto.APPROVE_STATUS,
                "Статус согласования на личный лимит", "notice_702",
                "По вашей заявке {humanReadableId} завершен этап \"Согласование\" " +
                        "со статусом \"{approveStatusDescription}\".",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_LIMIT_PERSON, NotificationTypeDto.REQUEST_EXECUTION,
                "Исполнение заявки на личный лимит", "notice_703",
                "Личный лимит был пополнен.", null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_LIMIT_DEPARTMENT, NotificationTypeDto.APPROVE,
                "Согласование заявки на лимит подразделения", "notice_801",
                "Согласование лимита подразделения. Согласуйте заявку до истечения контрольного срока.",
                internal ?
                        "Согласование лимита подразделения.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока.\n" +
                                "\n" +
                                "Сигма - {sigmaLink} (ПРОМ)\n" +
                                "\n" +
                                "Альфа - {alphaLink} (ПРОМ)"
                        :
                        "Согласование лимита подразделения.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока - {dzoLink}",
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_LIMIT_DEPARTMENT, NotificationTypeDto.APPROVE_STATUS,
                "Статус согласования заявки на лимит подразделения", "notice_802",
                "По вашей заявке {humanReadableId} завершен этап \"Согласование\" " +
                        "со статусом \"{approveStatusDescription}\".",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_LIMIT_DEPARTMENT, NotificationTypeDto.REQUEST_EXECUTION,
                "Исполнение заявки на лимит подразделения", "notice_803",
                "Лимит подразделения был пополнен.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.LIMIT_DEPARTMENT, NotificationTypeDto.ALLOCATION,
                "Выделение лимита подразделения", "notice_901",
                "Вашему подразделению назначен лимит {limitId} на период <какой-то период>",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.LIMIT_DEPARTMENT, NotificationTypeDto.CHANGE,
                "Изменение лимита подразделения", "notice_902",
                "По решению <ФИО инициатора изменений> ваш лимит подразделения на транспортное " +
                        "обеспечение изменился.  Снизился/увеличился на <сумма изменения> руб.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.LIMIT_DEPARTMENT, NotificationTypeDto.STATUS,
                "Статус лимита подразделения", "notice_903",
                "Статус лимита изменен на <статус лимита подразделения>",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.LIMIT_DEPARTMENT, NotificationTypeDto.LOW_REMAINS,
                "Уведомление о низком остатке лимита", "notice_904",
                "Лимит подразделения израсходован на <процент остатка> %. " +
                        "Обратите внимание на статистику поездок ваших сотрудников. ",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.LIMIT_DEPARTMENT, NotificationTypeDto.LOW_REMAINS_FROM_EMPLOYEE,
                "Уведомление о низком остатке лимита подразделения", "notice_905",
                "Лимит подразделения израсходован на <процент остатка> %. ",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.LIMIT_PERSON, NotificationTypeDto.ALLOCATION,
                "Выделение личного лимита", "notice_1001",
                "Вам распределен личный лимит на транспортное обеспечение " +
                        "<Общая сумма выделенного лимита на все виды транспорта> руб. на период <какой-то период>.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.LIMIT_PERSON, NotificationTypeDto.CHANGE,
                "Изменение личного лимита", "notice_1002",
                "Ваш личный лимит на <вид транспорта>  <сумма на вид транспорта> руб. на период " +
                        "<какой-то период> изменился на <сумма изменения> руб. на период <какой-то период>.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.LIMIT_PERSON, NotificationTypeDto.STATUS,
                "Статус личного лимита", "notice_1003",
                "Статус лимита изменен на <статус личного лимита>",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.LIMIT_PERSON, NotificationTypeDto.LOW_REMAINS,
                "Уведомление о низком остатке личного лимита", "notice_1004",
                "Ваш личный лимит на <вид транспорта> израсходована на <процент остатка> %. " +
                        "Обратите внимание на статистику поездок ваших сотрудников.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.USER_DELEGATE, NotificationTypeDto.ASSIGNMENT,
                "Назначение делегатом", "notice_1101",
                "Вам делегированы полномочия на согласование обращений по " +
                        "{transportType} на период с {startDate} по {endDate}",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.USER_DELEGATE, NotificationTypeDto.RESTRICTIONS_EDITED_DATE,
                "Изменение полномочий - Срок полномочий", "notice_1102",
                "Период действия ваших полномочий по согласованию заявок на <вид транспорта> изменен с " +
                        "<прошлый период делегирования> по <новый период делегирования>. " +
                        "По истечении периода право согласования будет отозвано.",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.USER_DELEGATE, NotificationTypeDto.RESTRICTIONS_EDITED_TYPE,
                "Изменение полномочий - Вид транспорта", "notice_1103",
                "Право согласования для <вид транспорта> на период <период делегирования> " +
                        "было отозвано <ФИО инициатора изменений>",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.USER_OWNER_LIMIT, NotificationTypeDto.ASSIGNMENT,
                "Назначение владельцем лимита подразделения", "notice_1201",
                "Вы назначены владельцем лимита на транспортное обеспечение.",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.USER_OWNER_LIMIT, NotificationTypeDto.RESTRICTIONS_EDITED,
                "Изменение полномочий", "notice_1202",
                "Период действия полномочий Владельца лимита изменен с <прошлый период делегирования> " +
                        "по <новый период делегирования>. После истечения периода полномочия будут отозваны.",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.USER_ASSIGNMENT, NotificationTypeDto.ASSIGNMENT,
                "Назначение руководителем подразделения", "notice_1301",
                "Поздравляем! Вы назначены руководителем подразделения!",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.DISPATCHER_NOTIFICATION, NotificationTypeDto.PUSH_DECLINED_BY_DRIVER,
                DRIVER_DECLINED_TRIP, "notice_1401",
                "Водитель отказался от поездки <ID поездки>  по причине: <причина отказа>",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.DISPATCHER_NOTIFICATION, NotificationTypeDto.PUSH_PASSENGER_COMMUNICATION_REQUEST,
                PASSENGER_REQUESTED_DRIVER, "notice_1402",
                "<ID поездки> Пассажиру необходимо связаться с водителем",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.DISPATCHER_NOTIFICATION, NotificationTypeDto.PUSH_DRIVER_NOT_ASSIGNED,
                DRIVER_NOT_ASSIGNED, "notice_1403",
                "Максимальное время назначения водителя на поездку истекло. <ID поездки>",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.DISPATCHER_NOTIFICATION, NotificationTypeDto.PUSH_DECLINED_BY_PASSENGER,
                PASSENGER_CANCELLED_TRIP, "notice_1404",
                "Пассажир отменил поездку <ID поездки> по причине <причина отмены>",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.DISPATCHER_NOTIFICATION, NotificationTypeDto.PUSH_LOW_GRADE_TRIP,
                LOW_RATED_TRIP, "notice_1405",
                "Пассажир низко оценил поездку <ID поездки>",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.DISPATCHER_NOTIFICATION, NotificationTypeDto.PUSH_INDICATION_TRIP_REQUEST,
                REQUEST_STARTED_WITH_INDICATOR, "notice_1406",
                "Пожалуйста, назначьте водителя на поездку <ID поездки>",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.DISPATCHER_NOTIFICATION, NotificationTypeDto.DECLINED_BY_DRIVER,
                DRIVER_DECLINED_TRIP, "notice_1501",
                """
                        Водитель отказался от поездки  <ID поездки> по причине: <причина отказа>
                                                        
                        Пожалуйста, назначьте другого водителя на данную поездку.""",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.DISPATCHER_NOTIFICATION, NotificationTypeDto.PASSENGER_COMMUNICATION_REQUEST,
                PASSENGER_REQUESTED_DRIVER, "notice_1502",
                """
                        <ID поездки> Пассажиру необходимо связаться с водителем.

                        Пожалуйста, свяжитесь с пассажиром для уточнения информации""",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.DISPATCHER_NOTIFICATION, NotificationTypeDto.DRIVER_NOT_ASSIGNED,
                DRIVER_NOT_ASSIGNED, "notice_1503",
                """
                        Максимальное время назначения водителя на поездку истекло.
                                                
                        Пожалуйста, назначьте другого водителя на поездку + <ID поездки>""",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.DISPATCHER_NOTIFICATION, NotificationTypeDto.DECLINED_BY_PASSENGER,
                PASSENGER_CANCELLED_TRIP, "notice_1504",
                """
                        Пассажир отменил поездку <ID поездки> по причине  <причина отмены>
                                                
                        Пожалуйста, назначьте водителя на другую поездку""",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.DISPATCHER_NOTIFICATION, NotificationTypeDto.LOW_GRADE_TRIP,
                LOW_RATED_TRIP, "notice_1505",
                """
                        Пассажир низко оценил поездку <ID поездки> комментарий пассажира:  <комментарий>
                                                
                        Водитель: <ФИО водителя>
                                                
                        Номер ТС <Регистрационный знак ТС>""",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.DISPATCHER_NOTIFICATION, NotificationTypeDto.INDICATION_TRIP_REQUEST,
                REQUEST_STARTED_WITH_INDICATOR, "notice_1506",
                "Пожалуйста, назначьте водителя на поездку <ID поездки>",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));
        notificationSettings.add(generateTimingConfig(NotificationClassDto.CONTRACTOR, NotificationTypeDto.DRIVER_REJECTED_REQUEST,
                DRIVER_DECLINED_TRIP, "notice_2000",
                "Водитель отказался от поездки {requestId} по причине \"{reason}\"",null,
                EventType.AT_EVENT, REQUEST_CREATION_TIME_FIELD_NAME, Duration.ZERO));
        notificationSettings.add(generateTimingConfig(NotificationClassDto.CONTRACTOR, NotificationTypeDto.PASSENGER_LOOKING_FOR_DRIVER,
                PASSENGER_REQUESTED_DRIVER, "notice_2001",
                "{requestId} Пассажиру необходимо связаться с водителем",null,
                EventType.AT_EVENT, REQUEST_CREATION_TIME_FIELD_NAME, Duration.ZERO));
        notificationSettings.add(generateTimingConfig(NotificationClassDto.CONTRACTOR, NotificationTypeDto.DRIVER_NOT_ASSIGNED,
                DRIVER_NOT_ASSIGNED, "notice_2002",
                "Максимальное время назначения водителя на поездку истекло" +
                        ".\n{requestId}",null,
                EventType.AT_EVENT, REQUEST_CREATION_TIME_FIELD_NAME, Duration.ZERO));
        notificationSettings.add(generateTimingConfig(NotificationClassDto.CONTRACTOR, NotificationTypeDto.PASSENGER_REJECTED_REQUEST,
                PASSENGER_CANCELLED_TRIP, "notice_2003",
                "Пассажир отменил поездку {requestId} по причине " +
                        "\"{reason}\"",null,
                EventType.AT_EVENT, REQUEST_CREATION_TIME_FIELD_NAME, Duration.ZERO));
        notificationSettings.add(generateTimingConfig(NotificationClassDto.CONTRACTOR, NotificationTypeDto.TRIP_WITH_LOW_RATE,
                LOW_RATED_TRIP, "notice_2004",
                "Пассажир низко оценил поездку {requestId}",null,
                EventType.AT_EVENT, REQUEST_CREATION_TIME_FIELD_NAME, Duration.ZERO));
        notificationSettings.add(generateTimingConfig(NotificationClassDto.CONTRACTOR, NotificationTypeDto.REQUEST_WITH_INDICATION_CREATED,
                REQUEST_STARTED_WITH_INDICATOR, "notice_2005",
                "Пожалуйста, назначьте водителя на поездку {requestId}",null,
                EventType.AT_EVENT, REQUEST_CREATION_TIME_FIELD_NAME, Duration.ZERO));
        notificationSettings.add(generateTimingConfig(NotificationClassDto.CONTRACTOR, NotificationTypeDto.DRIVER_ASSIGNED,
                "Водитель назначен", "notice_2006",
                "У Вас новый заказ. Откройте приложение Сбертранспорт.Водитель",
                null,
                EventType.AT_EVENT, REQUEST_CREATION_TIME_FIELD_NAME, Duration.ZERO));

        cargoInit();
    }

    private void initPersonalTransport() {
        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PERSONAL, NotificationTypeDto.APPROVE,
                "Согласование заявки на личный транспорт", "notice_301",
                "Согласование поездки на личном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.",
                internal ?
                        "Согласование поездки на личном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока.\n" +
                                "\n" +
                                "Сигма - {sigmaLink} (ПРОМ)\n" +
                                "\n" +
                                "Альфа - {alphaLink} (ПРОМ)"
                        :
                        "Согласование поездки на личном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока - {dzoLink}",
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PERSONAL, NotificationTypeDto.APPROVE_STATUS,
                "Статус согласования заявки на личный транспорт", "notice_302",
                "По вашей поездке {humanReadableId} завершен этап \"Согласование\" со статусом \"{approveStatusDescription}\".",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PERSONAL, NotificationTypeDto.TRIP_START_REMIND,
                "Напоминание о начале поездки", "notice_305",
                "Ваша поездка скоро начнется. Не забудьте отметить локации в приложении.",null,
                List.of(TimingDto.builder()
                        .eventType(EventType.DEADLINE)
                        .deadlineFieldName(DESIRED_DATE_FIELD_NAME)
                        .timeBefore(Duration.ofSeconds(900)).build())));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PERSONAL, NotificationTypeDto.WAYPOINT_ARRIVED,
                "Прибытие в промежуточный пункт на личном транспорте",
                "notice_306", "Вы прибыли в " +
                        "промежуточный пункт, не забудьте отметить" +
                        " чек-бокс геопозиции в вашей заявке для ускорения выплаты компенсации.",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PERSONAL, NotificationTypeDto.TRIP_FINISHED,
                "Поездка на личном транспорте завершена", "notice_307",
                "Поездка успешно завершена",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PERSONAL, NotificationTypeDto.TRIP_AFFIRM,
                "Утверждение финального маршрута на личном транспорте", "notice_308",
                "Утверждение поездки на личном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PERSONAL, NotificationTypeDto.TRIP_AFFIRM_STATUS,
                "Статус утверждения финального маршрута на личном транспорте", "notice_309",
                "По вашей поездке {humanReadableId} завершен этап \"Утверждение маршрута\" со статусом \"{requestStatusDescription}\".",null,
                EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PERSONAL, NotificationTypeDto.PAYMENT,
                "Ожидание выплаты за поездку на личном транспорте", "notice_310",
                "Ваша заявка {humanReadableId} была отправлена на выплату.",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PERSONAL, NotificationTypeDto.PAYMENT_STATUS,
                "Статус ожидания выплаты за поездку на личном транспорте",
                "notice_311",
                "По вашей поездке {humanReadableId} завершен этап \"Ожидание выплаты\" со статусом \"{requestStatusDescription}\".",null,
                EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PERSONAL,
                NotificationTypeDto.COOP_TRIP_ATTACHMENT, "Присоединение к " +
                        "совместной поездке " +
                        "на личном транспорте",
                "notice_312",
                "К вашей поездке присоединился {passenger.lastName} {passenger.firstName} {passenger" +
                        ".patronymic}",null, EventType.AT_EVENT, CREATION_TIME_FIELD_NAME, Duration.ZERO));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_PERSONAL, NotificationTypeDto.COOP_TRIP_CHANGES,
                "Изменение в совместной поездке на личном транспорте", "notice_313",
                "Пассажир {passenger.lastName} {passenger.firstName} {passenger.patronymic} отказался от " +
                        "совместной поездки с вами № {magentaSharedRequest.magentaId}.",null, EventType.AT_EVENT,
                CREATION_TIME_FIELD_NAME, Duration.ZERO
        ));
    }
    private NotificationSettings generateTimingConfig(
            NotificationClassDto notificationClass,
            NotificationTypeDto notificationType, String name, String description, String text, String customEmailText,
            List<TimingDto> timings) {
        var nnsd = new NewNotificationSettingsDto();
        nnsd.setNotificationClass(notificationClass);
        nnsd.setNotificationType(notificationType);
        nnsd.setName(name);
        nnsd.setDescription(description);
        nnsd.setChannels(List.of(ChannelSettingsDto.builder()
                        .channel(Channel.PUSH)
                        .enabled(defaultPushChannelEnabled)
                        .text(text).build(),
                ChannelSettingsDto.builder()
                        .channel(Channel.EMAIL)
                        .enabled(defaultEmailChannelEnabled)
                        .text(customEmailText != null ? customEmailText : text).build(),
                ChannelSettingsDto.builder()
                        .channel(Channel.SMS)
                        .enabled(defaultSmsChannelEnabled)
                        .text(text).build()));
        if (timings!= null) {
            nnsd.setTimings(timings);
        }
        return mapper.toModel(nnsd);
    }

    @SuppressWarnings("java:S107")
    private NotificationSettings generateTimingConfig(
            NotificationClassDto notificationClass,
            NotificationTypeDto notificationType, String name, String description, String text, String customEmailText,
            EventType eventType, String timeFieldName, Duration timeBefore) {
        var nnsd = new NewNotificationSettingsDto();
        nnsd.setNotificationClass(notificationClass);
        nnsd.setNotificationType(notificationType);
        nnsd.setName(name);
        nnsd.setDescription(description);
        nnsd.setChannels(List.of(ChannelSettingsDto.builder()
                        .channel(Channel.PUSH)
                        .enabled(defaultPushChannelEnabled)
                        .text(text).build(),
                ChannelSettingsDto.builder()
                        .channel(Channel.EMAIL)
                        .enabled(defaultEmailChannelEnabled)
                        .text(customEmailText != null ? customEmailText : text).build(),
                ChannelSettingsDto.builder()
                        .channel(Channel.SMS)
                        .enabled(defaultSmsChannelEnabled)
                        .text(text).build()));
        nnsd.setTimings(List.of(TimingDto.builder()
                .eventType(eventType)
                .timeFieldName(timeFieldName)
                .timeBefore(timeBefore).build()));
        return mapper.toModel(nnsd);
    }

    @SuppressWarnings("java:S107")
    private NotificationSettings generateTimingConfigWithEmptyText(
            NotificationClassDto notificationClass,
            NotificationTypeDto notificationType, String name, String description, String text,
            EventType eventType, String timeFieldName, Duration timeBefore) {
        var nnsd = new NewNotificationSettingsDto();
        nnsd.setNotificationClass(notificationClass);
        nnsd.setNotificationType(notificationType);
        nnsd.setName(name);
        nnsd.setDescription(description);
        nnsd.setChannels(List.of(ChannelSettingsDto.builder()
                        .channel(Channel.PUSH)
                        .enabled(false)
                        .text(text).build(),
                ChannelSettingsDto.builder()
                        .channel(Channel.EMAIL)
                        .enabled(false)
                        .text(text).build(),
                ChannelSettingsDto.builder()
                        .channel(Channel.SMS)
                        .enabled(false)
                        .text(text).build()));
        nnsd.setTimings(List.of(TimingDto.builder()
                .eventType(eventType)
                .timeFieldName(timeFieldName)
                .timeBefore(timeBefore).build()));
        return mapper.toModel(nnsd);
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<NotificationSettings> findAllDefaultSettings() {
        return notificationSettings.stream().sorted(Comparator.comparing(NotificationSettings::getName)).toList();
    }

    @Override
    public NotificationSettings findSettingByClassAndType(
            NotificationClass notificationClass, NotificationType notificationType
    ) {
        return notificationSettings.stream().filter(ns -> ns.getNotificationClass().equals(notificationClass) && ns.getType().equals(notificationType))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(NotificationSettings.class,
                        notificationClass + " " + notificationType));
    }


    private void cargoInit() {
        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CARGO, NotificationTypeDto.APPROVE,
                "Согласование заявки", "notice_801_cargo",
                "На согласование поступила заявка на грузоперевозку {humanReadableId} от {authorFio}. Согласуйте заявку до истечения контрольного срока.",
                internal ?
                        "На согласование поступила заявка на грузоперевозку {humanReadableId} от {authorFio}.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока.\n" +
                                "\n" +
                                "Сигма - {sigmaLink} (ПРОМ)\n" +
                                "\n" +
                                "Альфа - {alphaLink} (ПРОМ)"
                        :
                        "На согласование поступила заявка на грузоперевозку {humanReadableId} от {authorFio}.\n" +
                                "\n" +
                                "Согласуйте заявку до истечения контрольного срока - {dzoLink}",
                EventType.AT_EVENT, null, Duration.ZERO
        ));
        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CARGO, NotificationTypeDto.APPROVE_STATUS,
                "Статус согласования", "notice_802_cargo",
                "Завершено согласование по заявке на грузоперевозку {humanReadableId} со статусом {status}", null,
                EventType.AT_EVENT, null, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CARGO, NotificationTypeDto.CARGO_DRIVER_AWAITING_DATA,
                "Назначение водителя на заявку", "notice_806_cargo",
                "На заявку {humanReadableId} назначен водитель {authorFio} и автомобиль {carInfo}",null,
                EventType.AT_EVENT, null, Duration.ZERO
        ));
        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CARGO, NotificationTypeDto.CARGO_DRIVER_AWAITING_DATA,
                "Назначение курьера на заявку", "notice_817_cargo",
                "На заявку {humanReadableId} назначен курьер",null,
                EventType.AT_EVENT, null, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CARGO, NotificationTypeDto.CARGO_DRIVER_AWAITING_DATA,
                "Заявка исполнена - оценка", "notice_811_cargo",
                "Заявка {humanReadableId} на грузоперевозку исполнена. Оцените качество предоставленных услуг",null,
                EventType.AT_EVENT, null, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CARGO, NotificationTypeDto.CARGO_CANCELED_BY_ENGINEER,
                "Заявка отменена Инженером", "notice_814",
                "Заявка {humanReadableId} отменена Инженером",null,
                EventType.AT_EVENT, null, Duration.ZERO
        ));
        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CARGO, NotificationTypeDto.CARGO_CANCELED_BY_CONTRACTOR,
                "Заявка отменена контрагентом", "notice_815",
                "Заявка {humanReadableId} отменена контрагентом",null,
                EventType.AT_EVENT, null, Duration.ZERO
        ));

        notificationSettings.add(generateTimingConfig(NotificationClassDto.REQUEST_CARGO, NotificationTypeDto.CARGO_CHANGE_DESIRED_DATE,
                "Изменен плановый срок исполнения заявки", "notice_818",
                "По заявке {humanReadableId} изменен плановый срок исполнения на {desiredDate}",null,
                EventType.AT_EVENT, null, Duration.ZERO
        ));
    }
}
