package ru.sber.transport.trip.web.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.jooq.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.trip.business.dto.PassengerActionType;
import ru.sber.transport.trip.business.model.*;
import ru.sber.transport.trip.business.providers.TripProvider;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.*;
import ru.sber.transport.trip.messaging.providers.ContractorProvider;
import ru.sber.transport.trip.messaging.providers.DispatcherProvider;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.messaging.providers.VehicleProvider;
import ru.sber.transport.trip.messaging.senders.ReportSender;
import ru.sber.transport.trip.providers.checkin.CheckinProvider;
import ru.sber.transport.trip.providers.trips.mapping.TripMapper;
import ru.sber.transport.trip.web.dto.PassengersTripDTO;
import ru.sber.transport.trip_reports.message.TripReportMessage;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@DisplayName("Проверка формирования данных для отчета")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
class ReportServiceImplTest {
    private Trip trip;
    private Waypoint w1;
    private Waypoint w2;

    private TripProvider tripProvider = mock(TripProvider.class);
    private DispatcherProvider dispatcherProvider = mock(DispatcherProvider.class);
    private DriverProvider driverProvider = mock(DriverProvider.class);
    private VehicleProvider vehicleProvider = mock(VehicleProvider.class);
    private ContractorProvider contractorProvider = mock(ContractorProvider.class);
    private CheckinProvider checkinProvider = mock(CheckinProvider.class);
    private ObjectMapper objectMapper = mock(ObjectMapper.class);
    private ReportSender reportSender = mock(ReportSender.class);
    private TripMapper tripMapper = mock(TripMapper.class);
    private PassengerInfoExtractorServiceImpl passengerInfoExtractorService = mock(PassengerInfoExtractorServiceImpl.class);
    private ReportServiceImpl service = new ReportServiceImpl(tripProvider,dispatcherProvider, vehicleProvider, driverProvider, contractorProvider,
            checkinProvider, objectMapper, reportSender, tripMapper, passengerInfoExtractorService);

    private ReportServiceImpl serviceMock = mock(ReportServiceImpl.class);

    @BeforeEach
    void createData() throws JsonProcessingException {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Waypoint.Contact contact = new Waypoint.Contact("8912345678", "Иван Иванович");
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
        trip.setPassengerCount(1);
        trip.setTaxiClass(TaxiClass.COMFORT.name());
        trip.setDriverWaitingTime(null);
        trip.setDispatcherId(UUID.randomUUID());
        trip.setReportCreated(false);
    }

    @Test
    public void processReportWhenRequestsIsEmptyAndNoPassengersTest() throws Exception {
        // Arrange
        UUID tripId = UUID.randomUUID();
        TripsRecord record = new TripsRecord();
        record.setId(tripId);

        List<TripsRecord> trips = Collections.singletonList(record);
        when(tripProvider.findAllByReportCreatedFalseAndTerminalStatusesAndLimit(anyInt())).thenReturn(trips);

        when(tripMapper.toModel(any(TripsRecord.class))).thenReturn(trip);

        PassengersTripDTO  passengersInfoPrepare = new PassengersTripDTO(
                "UTC", "Петров", "NONE", "112233", "Comment", 0);
        doReturn(passengersInfoPrepare).when(passengerInfoExtractorService).extractPassengersInfo(null, trip);

        List<Checkin> checkins = Collections.emptyList();
        when(checkinProvider.findAllByTripIds(anyList())).thenReturn(checkins);

        // Act
        service.processReport();

        // Assert
        verify(reportSender, times(1)).send(any(TripReportMessage.class));
        verify(tripProvider, times(1)).setReportCreatedIsTrueByIds(Collections.singletonList(trip.getId()));
    }
}