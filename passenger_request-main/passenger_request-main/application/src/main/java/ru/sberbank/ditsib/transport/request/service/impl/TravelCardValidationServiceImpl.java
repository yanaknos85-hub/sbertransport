package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.PublicTransportType;
import ru.sberbank.ditsib.transport.request.database.dao.TransportCompensationRepository;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation;
import ru.sberbank.ditsib.transport.request.exceptions.ActiveTravelPassExistsException;
import ru.sberbank.ditsib.transport.request.service.TravelCardValidationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static java.lang.String.format;
import static ru.sberbank.ditsib.transport.constants.PublicCompensationType.TRAVEL_CARD_COMPENSATION;
import static ru.sberbank.ditsib.transport.constants.PublicTransportType.*;

/**
 * Реализация сервиса валидации проездных билетов
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TravelCardValidationServiceImpl implements TravelCardValidationService {

    private static final int MAX_TRAVEL_CARDS_PER_REQUEST = 1;
    private static final Set<PublicTransportType> ONE_TIME_TRIP_TYPES = Set.of(CITY_BUS, CITY_TROLLEYBUS, CITY_TRAM, CITY_METRO);
    private static final Map<PublicTransportType, List<String>> TRAVEL_CARD_TYPES = Map.of(
            CITY_BUS, List.of(TRAVEL_CARD_BUS.name(), TRAVEL_CARD_ALL_CITY_TRANSPORT.name()),
            CITY_TROLLEYBUS, List.of(TRAVEL_CARD_TROLLEYBUS.name(), TRAVEL_CARD_ALL_CITY_TRANSPORT.name()),
            CITY_TRAM, List.of(TRAVEL_CARD_TRAM.name(), TRAVEL_CARD_ALL_CITY_TRANSPORT.name()),
            CITY_METRO, List.of(TRAVEL_CARD_METRO.name(), TRAVEL_CARD_ALL_CITY_TRANSPORT.name())
    );

    private final TransportCompensationRepository transportCompensationRepository;

    @Transactional(readOnly = true)
    @Override
    public void checkOneTimeTrip(@NonNull RequestForPublic request) throws ActiveTravelPassExistsException {
        try {
            var oneTimeTrips = getOneTimeTrips(request);
            var tripsSize = oneTimeTrips.size();
            if (tripsSize == MAX_TRAVEL_CARDS_PER_REQUEST) {
                var requestId = request.getId();
                var passengerId = getPassengerId(request);
                var desiredDate = getDesiredDate(request);
                var oneTimeTrip = oneTimeTrips.getFirst();
                throwIfActiveTravelCardExists(requestId, passengerId, desiredDate, oneTimeTrip);
            }
            if (tripsSize > MAX_TRAVEL_CARDS_PER_REQUEST) {
                throw new ActiveTravelPassExistsException("Нельзя заводить больше одной разовой поездки на городском транспорте в одной заявке");
            }
        } catch (DataAccessException ex) {
            log.warn("Ошибка слоя данных", ex);
            throw new ActiveTravelPassExistsException("Что-то пошло не так", ex);
        }
    }

    private List<TransportCompensation> getOneTimeTrips(RequestForPublic request) {
        return Optional.ofNullable(request.getTransportCompensation())
                .orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .filter(compensation -> ONE_TIME_TRIP_TYPES.contains(compensation.getTransportType()))
                .toList();
    }

    private LocalDate getDesiredDate(RequestForPublic request) {
        return Optional.ofNullable(request.getDesiredDate())
                .map(LocalDateTime::toLocalDate)
                .orElseThrow(() -> new ActiveTravelPassExistsException("В заявке не указана дата поездки"));
    }

    private void throwIfActiveTravelCardExists(UUID requestId,
                                               UUID passengerId,
                                               LocalDate desiredDate,
                                               TransportCompensation compensation) {
        var transportType = compensation.getTransportType();
        var passesTypes = TRAVEL_CARD_TYPES.get(transportType);
        if (transportCompensationRepository.existsActiveTravelCard(requestId, passengerId, desiredDate, passesTypes)) {
            throw new ActiveTravelPassExistsException(format("У пассажира уже есть активный проездной на дату [%s]", desiredDate));
        }
    }

    @Transactional(readOnly = true)
    @Override
    public void checkOverlap(@NonNull RequestForPublic request) throws ActiveTravelPassExistsException {
        try {
            var travelCards = getTravelCards(request);
            var cardsSize = travelCards.size();
            if (cardsSize == MAX_TRAVEL_CARDS_PER_REQUEST) {
                var requestId = request.getId();
                var passengerId = getPassengerId(request);
                var travelCard = travelCards.getFirst();
                var start = getStartDate(travelCard);
                var end = getEndDate(travelCard);
                throwIfOverlapExists(requestId, passengerId, start, end);
            }
            if (cardsSize > MAX_TRAVEL_CARDS_PER_REQUEST) {
                throw new ActiveTravelPassExistsException("Нельзя заводить больше одного проездного на период для одного человека");
            }
        } catch (DataAccessException ex) {
            log.warn("Ошибка слоя данных", ex);
            throw new ActiveTravelPassExistsException("Что-то пошло не так", ex);
        }
    }

    private List<TransportCompensation> getTravelCards(RequestForPublic request) {
        return Optional.ofNullable(request.getTransportCompensation())
                .orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .filter(compensation -> TRAVEL_CARD_COMPENSATION.equals(compensation.getCompensationType()))
                .toList();
    }

    private UUID getPassengerId(RequestForPublic request) {
        return Optional.ofNullable(request.getPassenger())
                .map(Employee::getId)
                .orElseThrow(() -> new ActiveTravelPassExistsException("В компенсации не указан идентификатор пользователя"));
    }

    private LocalDate getStartDate(TransportCompensation compensation) {
        return Optional.ofNullable(compensation)
                .map(TransportCompensation::getTicketsExpirationStart)
                .orElseThrow(() -> new ActiveTravelPassExistsException("В компенсации не указана дата начала действия проездного"));
    }

    private LocalDate getEndDate(TransportCompensation compensation) {
        return Optional.ofNullable(compensation)
                .map(TransportCompensation::getTicketsExpirationEnd)
                .orElseThrow(() -> new ActiveTravelPassExistsException("В компенсации не указана дата окончания действия проездного"));
    }

    private void throwIfOverlapExists(UUID requestId, UUID passengerId, LocalDate start, LocalDate end) {
        if (transportCompensationRepository.existsOverlappingTravelCard(requestId, passengerId, start, end)) {
            throw new ActiveTravelPassExistsException(
                    format("У пассажира уже есть активный проездной, пересекающийся с периодом [%s – %s]", start, end)
            );
        }
    }
}
