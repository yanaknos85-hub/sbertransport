package ru.sberbank.ditsib.transport.reports.service.impl.mapping;

import lombok.experimental.UtilityClass;

@UtilityClass
class AllColumnNames {
    public final String SERVICE_NAME = "Услуга";
    public final String REQUEST_ID = "ID поездки";
    public final String SHARED_RIDE_ID = "ID Совместной поездки";
    public final String REQUEST_STATUS = "Статус поездки";
    public final String RATING = "Оценка поездки пользователем";
    public final String RATING_COMMENT = "Комментарий пользователя к оценке";
    public final String ADDITIONAL_OPTIONS = "Доп.опции";
    public final String COMMENT_FOR_DRIVER = "Комментарий для водителя";
    
    public final String EXPENSE_ITEM = "Статья расходов";
    public final String MVZ = "МВЗ";
    public final String LIMIT_ID = "ID лимита";
    public final String LIMIT_PARENT_DEPARTMENT = "Распределяющее лимит подразделение";
    
    public final String HIERARCHY_OF_THE_DEPARTMENT = "Структурная иерархия подразделения";
    
    public final String PASSENGER_PERSONNEL_NUMBER = "Табельный номер ВК";
    public final String PASSENGER_FIO = "ФИО пассажира";
    public final String PASSENGER_DEPARTMENT_1 = "Подразделение 1 уровня";
    public final String PASSENGER_DEPARTMENT_2 = "Подразделение 2 уровня";
    public final String PASSENGER_DEPARTMENT_3 = "Подразделение 3 уровня";
    public final String PASSENGER_DEPARTMENT_4 = "Подразделение 4 уровня";
    public final String PASSENGER_DEPARTMENT_5 = "Подразделение 5 уровня";
    public final String PASSENGER_DEPARTMENT_6 = "Подразделение 6 уровня";
    public final String PASSENGER_ACTIVITY = "Разъездной характер деятельности";
    public final String PASSENGER_POSITION = "Должность пассажира";
    public final String CUSTOMER_PERSONNEL_NUMBER = "Табельный номер Заявителя";
    public final String CUSTOMER_FIO = "Заявитель ФИО";
    public final String APPROVER_PERSONNEL_NUMBER = "Табельный номер Согласующего";
    public final String APPROVER_FIO = "Согласующий ФИО";
    
    public final String TARIFF_ID = "ID тарифа";
    public final String TARIFF_BY_KM = "Тариф за км";
    public final String TARIFF_BY_MIN = "Тариф за мин";
    public final String TARIFF_BY_WAITING_MIN = "Тариф за минуту ожидания";
    public final String TARIFF_CAR_SERVICE_COST = "Тариф. Стоимость подачи такси, руб";
    public final String TARIFF_MIN_RIDE_COST = "Тариф. Мин. стоимость поездки, руб";
    
    public final String TRIP_PURPOSE = "Цель поездки";
    public final String DEPARTURE_ADDRESS = "Адрес отправления";
    public final String INTERMEDIATE_ADDRESSES = "Промежуточные адреса";
    public final String DESTINATION_ADDRESS = "Адрес назначения";
    public final String CREATION_DATETIME = "Дата и время создания поездки";
    public final String EXECUTION_DATE_OF_REQUEST = "Дата выполнения обращения";
    public final String CLOSE_DATE_OF_REQUEST = "Дата закрытия обращения";
    public final String CONTROL_DATE = "Контрольный срок";
    public final String DESIRED_DATE = "Желаемая дата отправления";
    public final String DESIRED_TIME = "Желаемое время отправления";
    public final String PASSENGER_MOBILE_PHONE = "Мобильный телефон ВК-пассажира";
    public final String REQUEST_DESCRIPTION = "Описание заявки";
    public final String VSP_GOSB_TB_ADDRESS = "Адрес отправления/назначения является адресом ВСП/ГОСБ/ТБ";
    public final String REQUEST_DECISION = "Решение по заявке";
    public final String CONTRACTOR_NAME = "Рабочая группа (контрагент)";
    public final String REQUEST_EXPECTED_DISTANCE = "Предварительный километраж (обращ), км";
    public final String TRIP_FACT_DISTANCE = "Фактический километраж (обращ)";
    public final String REQUEST_EXPECTED_COST = "Условная стоимость (обращ)";
    public final String TRIP_FACT_PRICE = "Фактическая стоимость (обращ)";
    public final String REQUEST_EXPECTED_TIME = "Предварительное время поездки (обращ)";
    public final String TRIP_FACT_DURATION = "Фактическое время поездки (обращ), мин.";
    public final String WAYPOINT_WAIT_TIME = "Время ожидания, указанное ВК (обращ), мин.";
    public final String ACTUAL_WAITING_TIME = "Фактическое время ожидания (обращ), мин.";
    public final String INTERMEDIATE_WAYPOINT_WAIT_TIME = "Время ожидания в промежуточной точке, мин";
    public final String INTERMEDIATE_WAYPOINT_PRICE = "Стоимость ожидания в промежуточной точке, руб.";
    public final String DEPARTURE_WAIT_TIME = "Время ожидания при подаче, мин.";
    public final String DEPARTURE_WAIT_PRICE = "Стоимость ожидания при подаче, руб.";
    public final String TRIP_TYPE = "Тип поездки";
    public final String ACTUAL_REQUEST_PARAMS_UPDATE_DATE = "Фактическая дата внесения параметров поездки";
    public final String ACTUAL_DEPARTURE_DATETIME = "Фактическая дата и время выезда";
    public final String SHARED_REQUEST_ID = "№ заявки совместной поездки";
    public final String COST_SHARE_PART = "Доля в общей стоимости поездки для участника";
    public final String REQUEST_EXPECTED_COST_2 = "Примерная (расчетная) стоимость общая";
    public final String REQUEST_SAVING = "Предварительная экономия для обращения";
    public final String ACTUAL_REQUEST_SAVING = "Фактическая экономия для обращения";
    public final String TRIP_FACT_PRICE_2 = "Фактическая стоимость (общая)";
    public final String SHARED_PASSENGERS_COUNT = "Кол-во пассажиров в СП";
    public final String TARIFF_ACCURACY = "Точность тарифа";
    public final String ORDER_DISTANCE_KM = "Примерный (расчетный) километраж общий";
    public final String TRIP_FACT_DISTANCE_2 = "Фактический километраж общий";
    public final String CONTRAGENT_WAITING_TIME = "Реестр контрагента. Ожидание (простой) по поездке, мин.";
    public final String CONTRAGENT_DISTANCE = "Реестр контрагента. Протяженность маршрута поездки, км.";
    public final String CONTRAGENT_COST = "Реестр контрагента. Итого без НДС по поездке, руб.";
    public final String CHECK_0 = "НАЛИЧИЕ Проверка 0";
    public final String CHECK_1 = "СТАТУС Проверка 1";
    public final String CHECK_2 = "ДАТА Проверка 2";
    public final String CHECK_3 = "ОТЗЫВ Проверка 3";
    public final String CHECK_4 = "ПРОТЯЖЕННОСТЬ Проверка 4";
    public final String CHECK_4A = "ПРОТЯЖЕННОСТЬ Проверка 4а";
    public final String CHECK_5 = "ТАРИФ Проверка 5";
    public final String CHECK_6 = "СТОИМ ПРЕДВ Проверка 6";
    public final String CHECK_7 = "СТОИМ ФАКТ Проверка 7";
    public final String CHECK_8 = "ОЖИДАНИЕ Проверка 8";
    public final String TRANSPORT_TYPE = "Выбранный вид транспорта";
    public final String COMPENSATION_TYPE = "Выбранный вид компенсации";
    public final String REQUEST_EXPECTED_COST_3 = "Стоимость поездки, руб";
    public final String APPROVE_DATE = "Дата согласования поездки";
    public final String KK_PERSONAL_NUMBER = "Табельный номер польз. К.К.";
    public final String KK_FIO = "ФИО польз. К.К.";
    public final String ROUTE = "Маршрут";
    public final String HAS_ATTACHMENT = "Есть вложение (в заявку вложен билет/картинка)";
    public final String WAYPOINTS_COUNT = "Кол-во пунктов маршрута общее";
    public final String WAYPOINTS_COUNT_WITH_CHECK_IN = "Кол-во пунктов маршрута поездки с совпадением координат \"Отметиться\"";
    public final String WAYPOINTS_COUNT_WITHOUT_CHECK_IN = "Кол-во пунктов поездки без совпадения координат \"Отметиться\"";
    public final String PAYMENT_PERIOD = "Период выплаты";
    public final String OWNERSHIP_OF_CAR = "Право собственности на автомобиль";
    public final String MARRIAGE_CERTIFICATE_NUMBER = "Номер свидетельства о браке";
    public final String CAR_REGISTRATION_NUMBER = "Регистрационный номер автомобиля";
    public final String CAR_BRAND_NAME = "Марка автомобиля";
    public final String CAR_ENGINE_VOLUME = "Объем двигателя автомобиля";
    public final String OSAGO_NUMBER = "Номер полиса ОСАГО";
    public final String REQUEST_EXPECTED_DISTANCE_2 = "Расстояние, км";
    public final String ACTUAL_DURATION = "Фактическая длительность, мин";
    public final String CARRIER = "Перевозчик";
    public final String FREE_WAITING_TIME = "Бесплатное время ожидания при бронировании, мин.";
    
    public final String CUSTOMER_STRUCTURE_UNIT = "Заказчик (структурное подразделение)";
    public final String CODE_ORG_STRUCTURE_PASSENGER = "Kод (ID) оргструктуры пассажира";
    
    public final String ORDER_PAYMENT_FORMATION_FINISHING_DATE = "Дата и время окончания формирования приказа на выплату";
    public final String ORDER_PAYMENT_FORMATION_START_DATE = "Дата утверждения поездки";
}