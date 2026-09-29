package ru.sber.transport.request.messaging;

import ru.sber.transport.messaging.Message;
import ru.sberbank.ditsib.transport.constants.GroupTransferClass;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.external.taxi.OutboundRequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Данные заявки на исполнение для контрагентов
 *
 * @param tripId              Идентификатор в системе Банка
 * @param humanId             Человекочитаемый идентификатор
 * @param status              Статус в системе заказчика
 * @param timezone            Временная зона
 * @param workGroup           Рабочая группа
 * @param organization        Организация
 * @param integrationType     Используемый для отправки сообщения тип интеграции
 * @param integrationParams   Параметры для интеграции
 * @param inn                 ИНН перевозчика
 * @param commentForDriver    Комментарий
 * @param taxiId              Идентификатор такси
 * @param coop                Флаг совместной поездки
 * @param taxiClass           Класс такси
 * @param groupTransferClass  Класс трансфера
 * @param information         Информация о поездке - точки маршрута, пассажиры
 * @param contractorId        Идентификатор контрагента
 * @param contractorTariffId  Идентификатор используемого тарифа в системе контрагента
 * @param source              Адрес отправления
 * @param destination         Адрес назначения
 * @param waypoints           Точки остановок
 * @param transportType       Тип транспорта
 * @param transportId         Идентификатор транспорта
 * @param time                Предполагаемая длительность поездки, в секундах
 * @param distance            Предполагаемая дистанция поездки, в км
 * @param transferInformation Дополнительная информация по трансферу
 */
public record OutContractorTaxiTripMessage(
        String tripId,
        String humanId,
        OutboundRequestStatus status,
        String timezone,
        String workGroup,
        String organization,
        TaxiExternalIntegrationType integrationType,
        IntegrationParamsDTO integrationParams,
        String inn,
        String commentForDriver,
        String taxiId,
        boolean coop,
        TaxiClass taxiClass,
        GroupTransferClass groupTransferClass,
        RequestInformationMessage information,
        String contractorId,
        String contractorTariffId,
        Source source,
        Destination destination,
        List<Waypoint> waypoints,
        TransportTypeEnum transportType,
        UUID transportId,
        Long time,
        Double distance,
        GroupTransferRequestInformation transferInformation
) implements Message<String> {

    @Override
    public String getId() {
        return tripId();
    }

    /**
     * @param childSeat            Детское кресло
     * @param childSeatDetails     Детское кресло
     * @param bugs                 Количество багажа
     * @param bugsComment          Количество багажа, комментарий
     * @param bugsOversized        Негабаритный багаж
     * @param bugsOversizedComment Негабаритный багаж, комментарий
     * @param animal               Животные
     * @param animalComment        Животные, комментарий
     * @param addContactFIO        Дополнительное контактное лицо фио
     * @param addContactPhone      Дополнительное контактное лицо телефон
     * @param numberFlight         Номер рейса/поезда:  текст
     * @param dateFlight           Дата и время рейса/поездка
     * @param phoneHotel           Телефон принимающей гостиницы
     * @param typeVehicle          Желаемый тип Транспортного средства
     * @param transportId          Ид машины
     */
    public record GroupTransferRequestInformation(
            boolean childSeat,
            ChildSeatDetails childSeatDetails,
            boolean bugs,
            String bugsComment,
            boolean bugsOversized,
            String bugsOversizedComment,
            boolean animal,
            String animalComment,
            String addContactFIO,
            String addContactPhone,
            String numberFlight,
            LocalDateTime dateFlight,
            String phoneHotel,
            String typeVehicle,
            UUID transportId
    ) {
    }
}
