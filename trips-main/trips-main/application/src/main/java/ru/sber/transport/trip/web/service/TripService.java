package ru.sber.transport.trip.web.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.trip.business.dto.v2.CheckinResponseDtoV2;
import ru.sber.transport.trip.business.dto.RequestSearchDto;
import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.business.model.Shift;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.exceptions.FieldsException;
import ru.sber.transport.trip.business.dto.TripAssignStatisticDto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с поездками.
 */
public interface TripService {

    /**
     * Получение поездки по идентификатору.
     *
     * @param tripId идентификатор поездки.
     * @return поездка.
     */
    Optional<Trip> get(UUID tripId);

    /**
     * Обновление данных по поездке.
     *
     * @param contractorId идентификатор контрагента.
     * @param tripId       идентификатор поездки.
     * @param patchData    данные для обновления.
     * @param authentication       пользователь.
     */
    void update(UUID contractorId, UUID tripId, Map<String, Serializable> patchData, JwtAuthenticationToken authentication) throws FieldsException;

    /**
     * Получение статистики по назначению трипов.
     *
     * @param contractorId идентификатор контрагента.
     * @param autoparkId   идентификатор филиала.
     * @return статистика по назначению трипов.
     */
    TripAssignStatisticDto getAssignStatistic(UUID contractorId, UUID autoparkId);

    /**
     * Получение всех поездок.
     *
     * @param contractorId        идентификатор контрагента.
     * @param searchData          данные для фильтрации.
     * @param authenticatedUserId идентификатор аутентифицированного пользователя.
     * @return список поездок.
     */
    Iterable<Trip> getAll(UUID contractorId, RequestSearchDto searchData, List<TripStatus> statuses, UUID authenticatedUserId);

    /**
     * Получение чек-инов по поездке
     *
     * @param contractorId ID контрагента
     * @param tripId ID поездки
     * @return информация о чек-инах поездки
     */
    CheckinResponseDtoV2 getTripCheckins(UUID contractorId, UUID tripId);

    /**
     * Обработка запланированных поездок
     *
     * @param shift смена
     */
    void processPlannedTrips(Shift shift);
}
