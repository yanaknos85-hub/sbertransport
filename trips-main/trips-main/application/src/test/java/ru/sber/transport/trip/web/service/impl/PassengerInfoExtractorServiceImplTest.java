package ru.sber.transport.trip.web.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.trip.business.dto.PassengerActionType;
import ru.sber.transport.trip.business.dto.RequestDto;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.database.trips.tables.records.TripsRecord;
import ru.sber.transport.trip.web.dto.PassengersTripDTO;
import ru.sber.transport.trip_reports.message.TripReportMessage;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static ru.sber.transport.trip.web.service.impl.PassengerInfoExtractorServiceImpl.GROUP_PASSENGER;
import static ru.sber.transport.trip.web.service.impl.PassengerInfoExtractorServiceImpl.SINGLE_PASSENGER;

@DisplayName("Проверка сервиса для получения информации о пассажирах")
@UnitTest
@Feature("app_platform_trips")
class PassengerInfoExtractorServiceImplTest {
    private Trip trip;
    private Waypoint w1;
    private Waypoint w2;
    private Waypoint w3;
    Waypoint.Contact contact;

    private PassengerInfoExtractorServiceImpl passengerInfoExtractorService = new PassengerInfoExtractorServiceImpl();

    @BeforeEach
    void setUp() {
        contact = new Waypoint.Contact("8912345678", "Сергей Серегеевич");
        Waypoint.Passenger p1 = new Waypoint.Passenger(PassengerActionType.BOARDING,"Иван", "Иванович", "79001234567");
        Waypoint.Passenger p2 = new Waypoint.Passenger(PassengerActionType.BOARDING,null, "Петрович", "79001234568");
        Waypoint.Passenger p3 = new Waypoint.Passenger(PassengerActionType.BOARDING,"Анна", null, "79001234569");

        w1 = new Waypoint(
                UUID.randomUUID(),
                0.0,
                0.0,
                0,
                null, null, null, null, null,null,null, null,
                contact,
                List.of(p1, p2)
        );

        w2 = new Waypoint(
                UUID.randomUUID(),
                0.0,
                0.0,
                0,
                null, null, null, null, null,null,null, null,
                null,
                List.of(p2, p3)
        );

        w3 = new Waypoint(
                UUID.randomUUID(),
                0.0,
                0.0,
                0,
                null, null, null, null, null,null,null, null,
                contact,
                null
        );

        trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setFactStartTime(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(28));
        trip.setFactEndTime(trip.getFactStartTime().plusHours(1));
        trip.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(28));
        trip.setExpectedEndTime(trip.getExpectedStartTime().plusHours(1));
        trip.setStatus(TripStatus.ORDER_FINISHED);
        trip.setContractorId(UUID.randomUUID());
        trip.setWaypoints(List.of(w1, w2));
        trip.setRequests(new HashSet<Request>());
        trip.setDigitId(1L);
        trip.setFactDistance(10.02);
        trip.setPassengerCount(3);
        trip.setTaxiClass(TaxiClass.COMFORT.name());
        trip.setDriverWaitingTime(null);
        trip.setDispatcherId(UUID.randomUUID());
        trip.setReportCreated(false);
        trip.setTimeZone("UTC");
        trip.setExternalHumanReadableId("HRID");
        trip.setComment("Комментарий");
    }

    @DisplayName("Корректно получение данных о пассажирах из поля passenger.")
    @Test
    void getPassengerInfoFromWaypointTest() {
        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(w1);
        waypoints.add(w2);

        HashMap<String, String> result = passengerInfoExtractorService.getPassengerInfoFromWaypoint(waypoints);

        assertEquals(3, result.size());
        assertEquals("Иван Иванович", result.get("79001234567"));
        assertEquals("Анна", result.get("79001234569"));
        assertEquals("Петрович", result.get("79001234568"));
    }

    @DisplayName("Корректно получение данных о пассажирах из поля contact.")
    @Test
    public void getPassengerInfoFromWaypointFromContactWithValidDataTest() {
        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(w3);

        HashMap<String, String> result = passengerInfoExtractorService.getPassengerInfoFromWaypoint(waypoints);

        assertEquals(1, result.size());
        assertEquals("Сергей Серегеевич", result.get("8912345678"));
    }

    @DisplayName("Проверка игнорировоания дупликов в данных из поля passenger.")
    @Test
    public void getPassengerInfoFromWaypointIgnoreDuplicatesTest() {
        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(w1);
        waypoints.add(w2);

        HashMap<String, String> result = passengerInfoExtractorService.getPassengerInfoFromWaypoint(waypoints);

        assertEquals(3, result.size());
    }

    @DisplayName("Обработка пустого списка waypoints.")
    @Test
    public void getPassengerInfoFromWaypointEmptyListTest() {
        List<Waypoint> emptyList = Collections.emptyList();

        HashMap<String, String> result = passengerInfoExtractorService.getPassengerInfoFromWaypoint(emptyList);

        assertTrue(result.isEmpty());
    }

    @DisplayName("Обработка null списка waypoints.")
    @Test
    public void getPassengerInfoFromWaypointNullListTest() {
        List<Waypoint> nullList = null;

        HashMap<String, String> result = passengerInfoExtractorService.getPassengerInfoFromWaypoint(nullList);

        assertTrue(result.isEmpty());
    }

    @DisplayName("Проверка метода extractPassengersInfo для случая, когда requests равен null.")
    @Test
    public void extractPassengersInfoWhenRequestsIsNullTest() {
        List<Request> requests = null;

        PassengersTripDTO dto = passengerInfoExtractorService.extractPassengersInfo(requests, trip);

        assertEquals("UTC", dto.timeZone());
        assertEquals("Анна; Иван Иванович; Петрович", dto.passengers());
        assertEquals(GROUP_PASSENGER, dto.type());
        assertEquals("HRID", dto.requestHumanReadableIds());
        assertEquals("Комментарий", dto.comments());
        assertEquals(3, dto.count());
    }

    @DisplayName("Проверка метода extractPassengersInfo для случая, когда requests пустой.")
    @Test
    public void extractPassengersInfoWhenRequestsIsEmptyNullTest() {
        List<Request> requests = new ArrayList<>();

        PassengersTripDTO dto = passengerInfoExtractorService.extractPassengersInfo(requests, trip);

        assertEquals("UTC", dto.timeZone());
        assertEquals("Анна; Иван Иванович; Петрович", dto.passengers());
        assertEquals(GROUP_PASSENGER, dto.type());
        assertEquals("HRID", dto.requestHumanReadableIds());
        assertEquals("Комментарий", dto.comments());
        assertEquals(3, dto.count());
    }

    @DisplayName("Проверка метода extractPassengersInfo для случая, когда requests не пустой.")
    @Test
    public void extractPassengersInfoWhenRequestsNotEmptyNullTest() {
        Employee passenger = new Employee();
        passenger.setId(UUID.randomUUID());
        passenger.setFirstName("Иван");
        passenger.setLastName("Иванов");
        passenger.setMobilePhone("79001234567");

        Request request = new Request();
        request.setId(UUID.randomUUID());
        request.setPassenger(passenger);
        request.setTimeZone("UTC");
        request.setCoopTrip(false);
        request.setPassengerCount(1);
        request.setHumanReadableId("HRID");
        request.setComment("Комментарий");

        List<Request> requests = List.of(request);


        PassengersTripDTO dto = passengerInfoExtractorService.extractPassengersInfo(requests, trip);

        assertEquals("UTC", dto.timeZone());
        assertEquals("Иван И.", dto.passengers());
        assertEquals(SINGLE_PASSENGER, dto.type());
        assertEquals("HRID", dto.requestHumanReadableIds());
        assertEquals("Комментарий", dto.comments());
        assertEquals(1, dto.count());
    }
}