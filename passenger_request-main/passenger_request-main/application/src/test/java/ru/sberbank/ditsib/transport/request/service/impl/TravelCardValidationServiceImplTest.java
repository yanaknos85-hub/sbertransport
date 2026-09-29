package ru.sberbank.ditsib.transport.request.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import ru.sberbank.ditsib.transport.constants.PublicTransportType;
import ru.sberbank.ditsib.transport.request.database.dao.TransportCompensationRepository;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation;
import ru.sberbank.ditsib.transport.request.exceptions.ActiveTravelPassExistsException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.sberbank.ditsib.transport.constants.PublicCompensationType.TRAVEL_CARD_COMPENSATION;
import static ru.sberbank.ditsib.transport.constants.PublicTransportType.*;

@ExtendWith(MockitoExtension.class)
class TravelCardValidationServiceImplTest {

    @Mock
    private TransportCompensationRepository transportCompensationRepository;

    @InjectMocks
    private TravelCardValidationServiceImpl travelCardValidationService;

    private static Stream<Arguments> transportTypeAndExpectedCardsProvider() {
        return Stream.of(
                Arguments.of(
                        CITY_BUS,
                        List.of(TRAVEL_CARD_BUS.name(), TRAVEL_CARD_ALL_CITY_TRANSPORT.name())
                ),
                Arguments.of(
                        CITY_TROLLEYBUS,
                        List.of(TRAVEL_CARD_TROLLEYBUS.name(), TRAVEL_CARD_ALL_CITY_TRANSPORT.name())
                ),
                Arguments.of(
                        CITY_TRAM,
                        List.of(TRAVEL_CARD_TRAM.name(), TRAVEL_CARD_ALL_CITY_TRANSPORT.name())
                ),
                Arguments.of(
                        CITY_METRO,
                        List.of(TRAVEL_CARD_METRO.name(), TRAVEL_CARD_ALL_CITY_TRANSPORT.name())
                )
        );
    }

    @ParameterizedTest
    @MethodSource("transportTypeAndExpectedCardsProvider")
    @DisplayName("""
            Проверка существования активного проездного билета у пользователя на выбранную дату поездки:
            ровно один проездной и нет активного
            """)
    void checkOneTimeTrip_noActive_success(PublicTransportType transportType,
                                           List<String> expectedCardTypes) {
        var requestId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var passenger = mock(Employee.class);
        when(passenger.getId()).thenReturn(passengerId);
        var compensation = mock(TransportCompensation.class);
        when(compensation.getTransportType()).thenReturn(transportType);
        var compensations = List.of(compensation);
        var desiredDate = LocalDateTime.now();
        final var request = mock(RequestForPublic.class);
        when(request.getId()).thenReturn(requestId);
        when(request.getTransportCompensation()).thenReturn(compensations);
        when(request.getPassenger()).thenReturn(passenger);
        when(request.getDesiredDate()).thenReturn(desiredDate);
        when(transportCompensationRepository.existsActiveTravelCard(requestId, passengerId, desiredDate.toLocalDate(), expectedCardTypes))
                .thenReturn(false);

        assertDoesNotThrow(() -> travelCardValidationService.checkOneTimeTrip(request));

        verify(transportCompensationRepository).existsActiveTravelCard(
                requestId, passengerId, desiredDate.toLocalDate(), expectedCardTypes
        );
    }

    @ParameterizedTest
    @MethodSource("transportTypeAndExpectedCardsProvider")
    @DisplayName("""
            Проверка существования активного проездного билета у пользователя на выбранную дату поездки:
            существует активный проездной для одной компенсации
            """)
    void checkOneTimeTrip_activeExists_throwsException(PublicTransportType transportType,
                                                       List<String> expectedCardTypes) {
        var requestId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var passenger = mock(Employee.class);
        when(passenger.getId()).thenReturn(passengerId);
        var compensation = mock(TransportCompensation.class);
        when(compensation.getTransportType()).thenReturn(transportType);
        var desiredDate = LocalDateTime.now();
        final var request = mock(RequestForPublic.class);
        when(request.getId()).thenReturn(requestId);
        when(request.getTransportCompensation()).thenReturn(List.of(compensation));
        when(request.getPassenger()).thenReturn(passenger);
        when(request.getDesiredDate()).thenReturn(desiredDate);
        when(transportCompensationRepository.existsActiveTravelCard(requestId, passengerId, desiredDate.toLocalDate(), expectedCardTypes))
                .thenReturn(true);

        assertThrows(
                ActiveTravelPassExistsException.class,
                () -> travelCardValidationService.checkOneTimeTrip(request)
        );

        verify(transportCompensationRepository).existsActiveTravelCard(
                requestId, passengerId, desiredDate.toLocalDate(), expectedCardTypes
        );
    }

    @Test
    @DisplayName("""
            Проверка существования активного проездного билета у пользователя на выбранную дату поездки:
            подача заявки с множеством компенсацией общественного транспорта
            """)
    void checkOneTimeTrip_moreThanOneCard_throwsException() {
        var busCompensation = mock(TransportCompensation.class);
        when(busCompensation.getTransportType()).thenReturn(CITY_BUS);
        var tramCompensation = mock(TransportCompensation.class);
        when(tramCompensation.getTransportType()).thenReturn(CITY_TRAM);
        final var request = mock(RequestForPublic.class);
        when(request.getTransportCompensation()).thenReturn(List.of(busCompensation, tramCompensation));

        assertThrows(
                ActiveTravelPassExistsException.class,
                () -> travelCardValidationService.checkOneTimeTrip(request)
        );

        verify(transportCompensationRepository, never()).existsActiveTravelCard(
                any(UUID.class), any(UUID.class), any(LocalDate.class), anyList()
        );
    }

    @ParameterizedTest
    @MethodSource("transportTypeAndExpectedCardsProvider")
    @DisplayName("""
            Проверка существования активного проездного билета у пользователя на выбранную дату поездки:
            ошибка слоя данных
            """)
    void checkOneTimeTrip_dataAccessException_throwsException(PublicTransportType transportType,
                                                              List<String> expectedCardTypes) {
        var requestId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var passenger = mock(Employee.class);
        when(passenger.getId()).thenReturn(passengerId);
        var compensation = mock(TransportCompensation.class);
        when(compensation.getTransportType()).thenReturn(transportType);
        var desiredDate = LocalDateTime.now();
        final var request = mock(RequestForPublic.class);
        when(request.getId()).thenReturn(requestId);
        when(request.getTransportCompensation()).thenReturn(List.of(compensation));
        when(request.getPassenger()).thenReturn(passenger);
        when(request.getDesiredDate()).thenReturn(desiredDate);
        var messageEx = UUID.randomUUID().toString();
        when(transportCompensationRepository.existsActiveTravelCard(requestId, passengerId, desiredDate.toLocalDate(), expectedCardTypes))
                .thenThrow(new DataAccessException(messageEx) {});

        assertThrows(
                ActiveTravelPassExistsException.class,
                () -> travelCardValidationService.checkOneTimeTrip(request)
        );

        verify(transportCompensationRepository).existsActiveTravelCard(
                requestId, passengerId, desiredDate.toLocalDate(), expectedCardTypes
        );
    }

    @Test
    @DisplayName("""
            Проверка пересечения активных проездных по периоду действия с проездными в заявке для пассажира:
            ровно один проездной и нет пересечений с существующим
            """)
    void checkOverlap_noOverlap_success() {
        var requestId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var passenger = mock(Employee.class);
        when(passenger.getId()).thenReturn(passengerId);
        var startDate = LocalDate.now();
        var endDate = startDate.plusMonths(1);
        var compensation = mock(TransportCompensation.class);
        when(compensation.getCompensationType()).thenReturn(TRAVEL_CARD_COMPENSATION);
        when(compensation.getTicketsExpirationStart()).thenReturn(startDate);
        when(compensation.getTicketsExpirationEnd()).thenReturn(endDate);
        final var request = mock(RequestForPublic.class);
        when(request.getId()).thenReturn(requestId);
        when(request.getTransportCompensation()).thenReturn(List.of(compensation));
        when(request.getPassenger()).thenReturn(passenger);
        when(transportCompensationRepository.existsOverlappingTravelCard(requestId, passengerId, startDate, endDate))
                .thenReturn(false);

        assertDoesNotThrow(() -> travelCardValidationService.checkOverlap(request));

        verify(transportCompensationRepository).existsOverlappingTravelCard(
                requestId, passengerId, startDate, endDate
        );
    }

    @Test
    @DisplayName("""
            Проверка пересечения активных проездных по периоду действия с проездными в заявке для пассажира:
            отсутствует идентификатор пассажира
            """)
    void checkOverlap_nullPassengerId_throwsException() {
        var requestId = UUID.randomUUID();
        var passenger = mock(Employee.class);
        when(passenger.getId()).thenReturn(null);
        var compensation = mock(TransportCompensation.class);
        when(compensation.getCompensationType()).thenReturn(TRAVEL_CARD_COMPENSATION);
        final var request = mock(RequestForPublic.class);
        when(request.getId()).thenReturn(requestId);
        when(request.getTransportCompensation()).thenReturn(List.of(compensation));
        when(request.getPassenger()).thenReturn(passenger);

        assertThrows(ActiveTravelPassExistsException.class, () -> travelCardValidationService.checkOverlap(request));

        verify(transportCompensationRepository, never()).existsOverlappingTravelCard(
                any(UUID.class), any(UUID.class), any(LocalDate.class), any(LocalDate.class)
        );
    }

    @Test
    @DisplayName("""
            Проверка пересечения активных проездных по периоду действия с проездными в заявке для пассажира:
            отсутствует дата начала проездного
            """)
    void checkOverlap_nullStartDate_throwsException() {
        var requestId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var passenger = mock(Employee.class);
        when(passenger.getId()).thenReturn(passengerId);
        var compensation = mock(TransportCompensation.class);
        when(compensation.getCompensationType()).thenReturn(TRAVEL_CARD_COMPENSATION);
        when(compensation.getTicketsExpirationStart()).thenReturn(null);
        final var request = mock(RequestForPublic.class);
        when(request.getId()).thenReturn(requestId);
        when(request.getTransportCompensation()).thenReturn(List.of(compensation));
        when(request.getPassenger()).thenReturn(passenger);

        assertThrows(ActiveTravelPassExistsException.class, () -> travelCardValidationService.checkOverlap(request));

        verify(transportCompensationRepository, never()).existsOverlappingTravelCard(
                any(UUID.class), any(UUID.class), any(LocalDate.class), any(LocalDate.class)
        );
    }

    @Test
    @DisplayName("""
            Проверка пересечения активных проездных по периоду действия с проездными в заявке для пассажира:
            отсутствует дата окончания проездного
            """)
    void checkOverlap_nullEndDate_throwsException() {
        var requestId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var passenger = mock(Employee.class);
        when(passenger.getId()).thenReturn(passengerId);
        var startDate = LocalDate.now();
        var compensation = mock(TransportCompensation.class);
        when(compensation.getCompensationType()).thenReturn(TRAVEL_CARD_COMPENSATION);
        when(compensation.getTicketsExpirationStart()).thenReturn(startDate);
        when(compensation.getTicketsExpirationEnd()).thenReturn(null);
        final var request = mock(RequestForPublic.class);
        when(request.getId()).thenReturn(requestId);
        when(request.getTransportCompensation()).thenReturn(List.of(compensation));
        when(request.getPassenger()).thenReturn(passenger);

        assertThrows(ActiveTravelPassExistsException.class, () -> travelCardValidationService.checkOverlap(request));

        verify(transportCompensationRepository, never()).existsOverlappingTravelCard(
                any(UUID.class), any(UUID.class), any(LocalDate.class), any(LocalDate.class)
        );
    }

    @Test
    @DisplayName("""
            Проверка пересечения активных проездных по периоду действия с проездными в заявке для пассажира:
            существует пересечение с активным проездным
            """)
    void checkOverlap_overlapExists_throwsException() {
        var requestId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var passenger = mock(Employee.class);
        when(passenger.getId()).thenReturn(passengerId);
        var startDate = LocalDate.now();
        var endDate = startDate.plusMonths(1);
        var compensation = mock(TransportCompensation.class);
        when(compensation.getCompensationType()).thenReturn(TRAVEL_CARD_COMPENSATION);
        when(compensation.getTicketsExpirationStart()).thenReturn(startDate);
        when(compensation.getTicketsExpirationEnd()).thenReturn(endDate);
        final var request = mock(RequestForPublic.class);
        when(request.getId()).thenReturn(requestId);
        when(request.getTransportCompensation()).thenReturn(List.of(compensation));
        when(request.getPassenger()).thenReturn(passenger);
        when(transportCompensationRepository.existsOverlappingTravelCard(requestId, passengerId, startDate, endDate))
                .thenReturn(true);

        assertThrows(
                ActiveTravelPassExistsException.class,
                () -> travelCardValidationService.checkOverlap(request)
        );

        verify(transportCompensationRepository).existsOverlappingTravelCard(requestId, passengerId, startDate, endDate);
    }

    @Test
    @DisplayName("""
            Проверка пересечения активных проездных по периоду действия с проездными в заявке для пассажира:
            больше одного проездного в заявке, исключение без вызова репозитория
            """)
    void checkOverlap_multipleCards_throwsException() {
        var compensation1 = mock(TransportCompensation.class);
        when(compensation1.getCompensationType()).thenReturn(TRAVEL_CARD_COMPENSATION);
        var compensation2 = mock(TransportCompensation.class);
        when(compensation2.getCompensationType()).thenReturn(TRAVEL_CARD_COMPENSATION);
        final var request = mock(RequestForPublic.class);
        when(request.getTransportCompensation()).thenReturn(List.of(compensation1, compensation2));

        assertThrows(
                ActiveTravelPassExistsException.class,
                () -> travelCardValidationService.checkOverlap(request)
        );

        verify(transportCompensationRepository, never()).existsOverlappingTravelCard(
                any(UUID.class), any(UUID.class), any(LocalDate.class), any(LocalDate.class)
        );
    }

    @Test
    @DisplayName("""
            Проверка пересечения активных проездных по периоду действия с проездными в заявке для пассажира : ошибка слоя данных
            """)
    void checkOverlap_dataAccessException_throwsException() {
        var requestId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var passenger = mock(Employee.class);
        when(passenger.getId()).thenReturn(passengerId);
        var startDate = LocalDate.now();
        var endDate = startDate.plusMonths(1);
        var compensation = mock(TransportCompensation.class);
        when(compensation.getCompensationType()).thenReturn(TRAVEL_CARD_COMPENSATION);
        when(compensation.getTicketsExpirationStart()).thenReturn(startDate);
        when(compensation.getTicketsExpirationEnd()).thenReturn(endDate);
        final var request = mock(RequestForPublic.class);
        when(request.getId()).thenReturn(requestId);
        when(request.getTransportCompensation()).thenReturn(List.of(compensation));
        when(request.getPassenger()).thenReturn(passenger);
        var messageEx = UUID.randomUUID().toString();
        when(transportCompensationRepository.existsOverlappingTravelCard(requestId, passengerId, startDate, endDate))
                .thenThrow(new DataAccessException(messageEx) {});

        assertThrows(
                ActiveTravelPassExistsException.class,
                () -> travelCardValidationService.checkOverlap(request)
        );

        verify(transportCompensationRepository).existsOverlappingTravelCard(requestId, passengerId, startDate, endDate);
    }
}