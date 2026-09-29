package ru.sberbank.ditsib.transport.srm.service.impl;

import ch.qos.logback.classic.Level;
import org.instancio.Instancio;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.constants.EventType;
import ru.sber.transport.constants.PointMatchingType;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sber.transport.srm.model.SrmWaypointPostDTO;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.srm.LoggingExtension;
import ru.sberbank.ditsib.transport.srm.dao.BaseTariffRepository;
import ru.sberbank.ditsib.transport.srm.dao.SrmRequestKpiRepository;
import ru.sberbank.ditsib.transport.srm.dao.SrmSharedRideRepository;
import ru.sberbank.ditsib.transport.srm.dto.DistanceMatrixResponseDTO;
import ru.sberbank.ditsib.transport.srm.dto.internal.SrmMultipleRequestDTO;
import ru.sberbank.ditsib.transport.srm.dto.internal.SrmSingleRequestDTO;
import ru.sberbank.ditsib.transport.srm.exception.SrmLogicException;
import ru.sberbank.ditsib.transport.srm.mapper.EntityCopyConverter;
import ru.sberbank.ditsib.transport.srm.mapper.EntityDtoConverter;
import ru.sberbank.ditsib.transport.srm.model.SrmRequestKpi;
import ru.sberbank.ditsib.transport.srm.model.SrmSharedRide;
import ru.sberbank.ditsib.transport.srm.model.SrmWaypoint;
import ru.sberbank.ditsib.transport.srm.model.tariff.BaseTariff;
import ru.sberbank.ditsib.transport.srm.model.tariff.CoopTariffParams;
import ru.sberbank.ditsib.transport.srm.model.tariff.TaxiTariff;
import ru.sberbank.ditsib.transport.srm.service.GeoService;
import ru.sberbank.ditsib.transport.srm.service.SrmSettingService;
import ru.sberbank.ditsib.transport.srm.service.TariffService;
import ru.sberbank.ditsib.transport.srm.service.TaxiTariffService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SrmServiceImplTest {

    @InjectMocks
    private SrmServiceImpl srmService;
    @Mock
    private SrmSharedRideRepository sharedRideRepository;
    @Mock
    private SrmRequestKpiRepository requestKpiRepository;
    @Mock
    private BaseTariffRepository baseTariffRepository;
    @Mock
    private Map<TransportTypeEnum, TariffService<? extends BaseTariff>> tariffServices;
    @Mock
    private GeoService geoService;
    @Mock
    private SrmSettingService settingService;
    @Mock
    private EntityDtoConverter entityDtoConverter;
    @Mock
    private EntityCopyConverter entityCopyConverter;
    @Mock
    private TaxiTariffService taxiTariffService;
    @Captor
    private ArgumentCaptor<SrmSharedRide> srmSharedRideArgumentCaptor;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(SrmServiceImpl.class);


    @Test
    void addNew_ShouldReturnSharedRideDTO_WhenValidRequestAndDoSaveTrue() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 15);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 4);
        var waypoints = Instancio.ofList(SrmWaypointPostDTO.class)
                .size(2)
                .create();
        var requestId = UUID.randomUUID();
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), Collections.singletonList(singleRequestDTO))
                .create();
        var distanceMatrixResponseDTO = Instancio.of(DistanceMatrixResponseDTO.class)
                .set(field(DistanceMatrixResponseDTO::getOrigins), Collections.emptyList())
                .create();
        var bunchId = UUID.randomUUID();
        var rideId = UUID.randomUUID();
        var sharedRide = Instancio.of(SrmSharedRide.class)
                .set(field(SrmSharedRide::getId), rideId)
                .set(field(SrmSharedRide::isActive), true)
                .set(field(SrmSharedRide::getTariffId), tariffId)
                .set(field(SrmSharedRide::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmSharedRide::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmSharedRide::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmSharedRide::getBunchId), bunchId)
                .set(field(SrmSharedRide::getCreationTime), LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .set(field(SrmSharedRide::getBunchNumber), 0)
                .set(field(SrmSharedRide::getCoopTariffParams), coopTariffParams)
                .set(field(SrmSharedRide::getWaypoints), Collections.emptyList())
                .set(field(SrmSharedRide::getRequestKpiList), Collections.emptyList())
                .create();
        var expected = Instancio.create(SrmSharedRideDTO.class);
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);
        doReturn(distanceMatrixResponseDTO).when(geoService).getDistanceMatrix(anyList(), any());
        doReturn(Optional.empty()).when(requestKpiRepository).findByOldId(anyInt());
        doReturn(sharedRide).when(sharedRideRepository).saveAndFlush(any());
        doReturn(expected).when(entityDtoConverter).sharedRideToSharedRideDto(any());

        var result = srmService.addNew(multipleRequestDTO, bunchId, true);

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        verify(sharedRideRepository).saveAndFlush(any());
        verify(entityDtoConverter).sharedRideToSharedRideDto(any());
    }

    @Test
    void addNew_ShouldReturnSharedRideDTO_WhenValidRequestAndDoSaveFalse() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 15);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 4);
        var waypoints = Instancio.ofList(SrmWaypointPostDTO.class)
                .size(2)
                .create();
        var requestId = UUID.randomUUID();
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), Collections.singletonList(singleRequestDTO))
                .create();
        var distanceMatrixResponseDTO = Instancio.of(DistanceMatrixResponseDTO.class)
                .set(field(DistanceMatrixResponseDTO::getOrigins), Collections.emptyList())
                .create();
        var bunchId = UUID.randomUUID();
        var expected = Instancio.create(SrmSharedRideDTO.class);
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);
        doReturn(distanceMatrixResponseDTO).when(geoService).getDistanceMatrix(anyList(), any());
        doReturn(Optional.empty()).when(requestKpiRepository).findByOldId(anyInt());
        doReturn(expected).when(entityDtoConverter).sharedRideToSharedRideDto(any());

        var result = srmService.addNew(multipleRequestDTO, bunchId, false);

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        verify(sharedRideRepository, never()).saveAndFlush(any());
        verify(entityDtoConverter).sharedRideToSharedRideDto(any());
    }

    @Test
    void addNew_ShouldThrowSrmBadRequestException_WhenTransportTypeNotSupported() {
        var tariffId = UUID.randomUUID();
        var waypoints = Instancio.ofList(SrmWaypointPostDTO.class)
                .size(2)
                .create();
        var requestId = UUID.randomUUID();
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.CARSHARING)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), Collections.singletonList(singleRequestDTO))
                .create();
        var bunchId = UUID.randomUUID();
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        var result = srmService.addNew(multipleRequestDTO, bunchId, true);
        assertThat(result).isNull();
        checkInfoLog(1, "SRM: addNew: вид транспорта не поддерживается: %s".formatted(TransportTypeEnum.CARSHARING.getName()));
    }

    @Test
    void addNew_ShouldThrowSrmBadRequestException_WhenPassengersZero() {
        var tariffId = UUID.randomUUID();
        var waypoints = Instancio.ofList(SrmWaypointPostDTO.class)
                .size(2)
                .create();
        var requestId = UUID.randomUUID();
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 0, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), Collections.singletonList(singleRequestDTO))
                .create();
        var bunchId = UUID.randomUUID();
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        var result = srmService.addNew(multipleRequestDTO, bunchId, true);
        assertThat(result).isNull();
        checkInfoLog(1, "SRM: addNew: Количество пассажиров равно нулю. requestId = %s".formatted(requestId));
    }

    @Test
    void addNew_ShouldThrowSrmBadRequestException_WhenWaypointsLessThanTwo() {
        var tariffId = UUID.randomUUID();
        var waypoints = Instancio.ofList(SrmWaypointPostDTO.class)
                .size(1)
                .create();
        var requestId = UUID.randomUUID();
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), Collections.singletonList(singleRequestDTO))
                .create();
        var bunchId = UUID.randomUUID();
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        var result = srmService.addNew(multipleRequestDTO, bunchId, true);
        assertThat(result).isNull();
        checkInfoLog(1, "SRM: addNew: Количество точек меньше 2. requestId = %s".formatted(requestId));
    }

    @Test
    void addNew_ShouldThrowSrmBadRequestException_WhenPickupTimeInPast() {
        var tariffId = UUID.randomUUID();
        var waypoints = Instancio.ofList(SrmWaypointPostDTO.class)
                .size(2)
                .create();
        var requestId = UUID.randomUUID();
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), Collections.singletonList(singleRequestDTO))
                .create();
        var bunchId = UUID.randomUUID();
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        var result = srmService.addNew(multipleRequestDTO, bunchId, true);
        assertThat(result).isNull();
        checkInfoLog(1, "SRM: addNew: Время начало поездки в прошлом. requestId = %s".formatted(requestId));
    }

    @Test
    void addNew_ShouldThrowSrmBadRequestException_WhenPointMatchingFalseAndMinimalDistanceNotReserved() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 15);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 4);
        var waypoints = new ArrayList<>(List.of(Instancio.of(SrmWaypointPostDTO.class)
                        .set(field(SrmWaypointPostDTO::getId), UUID.fromString("31aa8ed5-5679-3695-a8c5-52c5e05cfea8"))
                        .set(field(SrmWaypointPostDTO::getLatitude), 44.93847545175141)
                        .set(field(SrmWaypointPostDTO::getLongitude), 34.07262756028693)
                        .create(),
                Instancio.of(SrmWaypointPostDTO.class)
                        .set(field(SrmWaypointPostDTO::getId), UUID.fromString("72116028-face-3586-a3b3-6c3c7f7ff771"))
                        .set(field(SrmWaypointPostDTO::getLatitude), 44.93847545175141)
                        .set(field(SrmWaypointPostDTO::getLongitude), 34.07262756028693)
                        .create()));

        var requestId = UUID.randomUUID();
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_POINTS_FALSE)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), Collections.singletonList(singleRequestDTO))
                .create();

        var bunchId = UUID.randomUUID();
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);

        var result = srmService.addNew(multipleRequestDTO, bunchId, true);
        assertThat(result).isNull();
        checkInfoLog(1, "SRM: addNew: Растояние между двумя соседними точками меньше минимального: 2.0");
    }

    @Test
    void addNew_ShouldThrowSrmLogicException_WhenFilterByCapacityFails() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 15);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 2);
        var waypoints = Instancio.ofList(SrmWaypointPostDTO.class)
                .size(2)
                .create();
        var requestId = UUID.randomUUID();
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 3, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), Collections.singletonList(singleRequestDTO))
                .create();
        var distanceMatrixResponseDTO = Instancio.of(DistanceMatrixResponseDTO.class)
                .set(field(DistanceMatrixResponseDTO::getOrigins), Collections.emptyList())
                .create();
        var bunchId = UUID.randomUUID();
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);
        doReturn(distanceMatrixResponseDTO).when(geoService).getDistanceMatrix(anyList(), any());
        doReturn(Optional.empty()).when(requestKpiRepository).findByOldId(anyInt());

        var result = srmService.addNew(multipleRequestDTO, bunchId, true);
        assertThat(result).isNull();
        checkInfoLog(8, "SRM: addNew: Заявка не подходит по метрическим критериям");
    }

    @Test
    void addNew_ShouldThrowSrmLogicException_DebugEnabled() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 15);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 2);
        var waypoints = Instancio.ofList(SrmWaypointPostDTO.class)
                .size(2)
                .create();
        var requestId = UUID.randomUUID();
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 3, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), Collections.singletonList(singleRequestDTO))
                .create();
        var distanceMatrixResponseDTO = Instancio.of(DistanceMatrixResponseDTO.class)
                .set(field(DistanceMatrixResponseDTO::getOrigins), Collections.emptyList())
                .create();
        var bunchId = UUID.randomUUID();
        LOGGING_EXTENSION.setLogLevel(Level.DEBUG);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);
        doReturn(distanceMatrixResponseDTO).when(geoService).getDistanceMatrix(anyList(), any());
        doReturn(Optional.empty()).when(requestKpiRepository).findByOldId(anyInt());

        var result = srmService.addNew(multipleRequestDTO, bunchId, true);
        assertThat(result).isNull();
        checkDebugLog(9, "SRM: addNew: Заявка не подходит по метрическим критериям");
    }

    @Test
    void joinRequest_ShouldReturnNull_WhenFilterByEconomyPercentFails() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 120);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 4);
        var waypoints = createWaypoints();
        var requestId = UUID.randomUUID();
        var requestKpi1 = createSrmRequestKpi(1);
        var requestKpi2 = createSrmRequestKpi(1);
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestPrice), 1000L)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), new ArrayList<>(List.of(singleRequestDTO)))
                .create();
        var distanceMatrixResponseDTO = createDistanceMatrixResponseDTO();
        var bunchId = UUID.randomUUID();
        var rideId = UUID.randomUUID();
        var srmWaypoints = new ArrayList<>(List.of(Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.93847545175142)
                        .set(field(SrmWaypoint::getLongitude), 34.07262756028694)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi1.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 0)
                        .set(field(SrmWaypoint::getEventType), EventType.BOARDING)
                        .create(),
                Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.96761772790356)
                        .set(field(SrmWaypoint::getLongitude), 34.08081345831347)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi2.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 1)
                        .set(field(SrmWaypoint::getEventType), EventType.UNBOARDING)
                        .create()));
        var sharedRide = createSrmSharedRide(rideId, tariffId, bunchId, coopTariffParams, srmWaypoints,
                new ArrayList<>(List.of(requestKpi1, requestKpi2)));
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(Optional.of(sharedRide)).when(sharedRideRepository).findById(rideId);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);
        doReturn(true).when(baseTariffRepository).existsById(tariffId);
        doReturn(distanceMatrixResponseDTO).when(geoService).getDistanceMatrix(anyList(), eq(TransportTypeEnum.TAXI));

        var result = srmService.joinRequest(rideId, multipleRequestDTO, 1);
        assertThat(result).isNull();
        checkInfoLog(10, "joinRequest: Заявка не подходит по критериям экономии");
    }

    @Test
    void joinRequest_ShouldReturnNull_WhenSharedRideNotFound() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 120);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 4);
        var waypoints = createWaypoints();
        var requestId = UUID.randomUUID();
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestPrice), 1000L)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), new ArrayList<>(List.of(singleRequestDTO)))
                .create();
        var rideId = UUID.randomUUID();
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(Optional.empty()).when(sharedRideRepository).findById(rideId);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);

        var result = srmService.joinRequest(rideId, multipleRequestDTO, 1);
        assertThat(result).isNull();
        checkInfoLog(1, "joinRequest: Совместная поездка не найдена");
    }

    @Test
    void joinRequest_ShouldReturnNull_WhenNotMatchingByBasicAttributes() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 120);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 4);
        var waypoints = createWaypoints();
        var requestId = UUID.randomUUID();
        var requestKpi1 = createSrmRequestKpi(1);
        var requestKpi2 = createSrmRequestKpi(1);
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_POINTS_FALSE)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestPrice), 1000L)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), new ArrayList<>(List.of(singleRequestDTO)))
                .create();
        var bunchId = UUID.randomUUID();
        var rideId = UUID.randomUUID();
        var srmWaypoints = new ArrayList<>(List.of(Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.93847545175142)
                        .set(field(SrmWaypoint::getLongitude), 34.07262756028694)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi1.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 0)
                        .create(),
                Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.96761772790356)
                        .set(field(SrmWaypoint::getLongitude), 34.08081345831347)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi2.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 1)
                        .create()));
        var sharedRide = createSrmSharedRide(rideId, tariffId, bunchId, coopTariffParams, srmWaypoints,
                new ArrayList<>(List.of(requestKpi1, requestKpi2)));
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(Optional.of(sharedRide)).when(sharedRideRepository).findById(rideId);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);

        var result = srmService.joinRequest(rideId, multipleRequestDTO, 1);
        assertThat(result).isNull();
        checkInfoLog(1, "joinRequest: Заявка не подходит по базовым атрибутам");
    }

    @Test
    void joinRequest_ShouldReturnNull_WhenDebugEnabled() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 120);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 4);
        var waypoints = createWaypoints();
        var requestId = UUID.randomUUID();
        var requestKpi1 = createSrmRequestKpi(1);
        var requestKpi2 = createSrmRequestKpi(1);
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_POINTS_FALSE)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestPrice), 1000L)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), new ArrayList<>(List.of(singleRequestDTO)))
                .create();
        var bunchId = UUID.randomUUID();
        var rideId = UUID.randomUUID();
        var srmWaypoints = new ArrayList<>(List.of(Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.93847545175142)
                        .set(field(SrmWaypoint::getLongitude), 34.07262756028694)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi1.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 0)
                        .create(),
                Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.96761772790356)
                        .set(field(SrmWaypoint::getLongitude), 34.08081345831347)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi2.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 1)
                        .create()));
        var sharedRide = createSrmSharedRide(rideId, tariffId, bunchId, coopTariffParams, srmWaypoints,
                new ArrayList<>(List.of(requestKpi1, requestKpi2)));
        LOGGING_EXTENSION.setLogLevel(Level.DEBUG);
        doReturn(Optional.of(sharedRide)).when(sharedRideRepository).findById(rideId);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);

        var result = srmService.joinRequest(rideId, multipleRequestDTO, 1);
        assertThat(result).isNull();
        checkDebugLog(3, "joinRequest: Заявка не подходит по базовым атрибутам");
    }

    @Test
    void joinRequest_ShouldReturnNull_WhenTariffNotFound() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 120);
        var waypoints = createWaypoints();
        var requestId = UUID.randomUUID();
        var requestKpi1 = createSrmRequestKpi(1);
        var requestKpi2 = createSrmRequestKpi(1);
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_POINTS_FALSE)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestPrice), 1000L)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), new ArrayList<>(List.of(singleRequestDTO)))
                .create();
        var bunchId = UUID.randomUUID();
        var rideId = UUID.randomUUID();
        var srmWaypoints = new ArrayList<>(List.of(Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.93847545175142)
                        .set(field(SrmWaypoint::getLongitude), 34.07262756028694)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi1.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 0)
                        .create(),
                Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.96761772790356)
                        .set(field(SrmWaypoint::getLongitude), 34.08081345831347)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi2.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 1)
                        .create()));
        var sharedRide = createSrmSharedRide(rideId, tariffId, bunchId, coopTariffParams, srmWaypoints,
                new ArrayList<>(List.of(requestKpi1, requestKpi2)));
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(Optional.of(sharedRide)).when(sharedRideRepository).findById(rideId);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.empty()).when(taxiTariffService).findById(tariffId);
        assertThrows(EntityNotFoundException.class,
                () -> srmService.joinRequest(rideId, multipleRequestDTO, 1),
                "Data not found: Entity: Base1Tariff, ID: %s".formatted(tariffId));
    }

    @Test
    void joinRequest_ShouldReturnNull_WhenBaseTariffNotFound() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 120);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 4);
        var waypoints = createWaypoints();
        var requestId = UUID.randomUUID();
        var requestKpi1 = createSrmRequestKpi(1);
        var requestKpi2 = createSrmRequestKpi(1);
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestPrice), 1000L)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), new ArrayList<>(List.of(singleRequestDTO)))
                .create();
        var bunchId = UUID.randomUUID();
        var rideId = UUID.randomUUID();
        var srmWaypoints = new ArrayList<>(List.of(Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.93847545175142)
                        .set(field(SrmWaypoint::getLongitude), 34.07262756028694)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi1.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 0)
                        .set(field(SrmWaypoint::getEventType), EventType.BOARDING)
                        .create(),
                Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.96761772790356)
                        .set(field(SrmWaypoint::getLongitude), 34.08081345831347)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi2.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 1)
                        .set(field(SrmWaypoint::getEventType), EventType.UNBOARDING)
                        .create()));
        var sharedRide = createSrmSharedRide(rideId, tariffId, bunchId, coopTariffParams, srmWaypoints,
                new ArrayList<>(List.of(requestKpi1, requestKpi2)));
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(Optional.of(sharedRide)).when(sharedRideRepository).findById(rideId);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);
        doReturn(false).when(baseTariffRepository).existsById(tariffId);

        var result = srmService.joinRequest(rideId, multipleRequestDTO, 1);
        assertThat(result).isNull();
        checkInfoLog(1, "SRM: joinRequest: Тариф заявки не найден! requestId = %s".formatted(requestId));
    }

    @Test
    void joinRequest_ShouldReturnNull_WhenMinimalDistanceNonReserved() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(10.0, 120);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 4);
        var waypoints = createWaypoints();
        var requestId = UUID.randomUUID();
        var requestKpi1 = createSrmRequestKpi(1);
        var requestKpi2 = createSrmRequestKpi(1);
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_POINTS_FALSE)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestPrice), 1000L)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), new ArrayList<>(List.of(singleRequestDTO)))
                .create();
        var bunchId = UUID.randomUUID();
        var rideId = UUID.randomUUID();
        var srmWaypoints = new ArrayList<>(List.of(Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.93847545175142)
                        .set(field(SrmWaypoint::getLongitude), 34.07262756028694)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi1.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 0)
                        .set(field(SrmWaypoint::getEventType), EventType.BOARDING)
                        .create(),
                Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.96761772790356)
                        .set(field(SrmWaypoint::getLongitude), 34.08081345831347)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi2.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 1)
                        .set(field(SrmWaypoint::getEventType), EventType.UNBOARDING)
                        .create()));
        var sharedRide = Instancio.of(SrmSharedRide.class)
                .set(field(SrmSharedRide::getId), rideId)
                .set(field(SrmSharedRide::isActive), true)
                .set(field(SrmSharedRide::getTariffId), tariffId)
                .set(field(SrmSharedRide::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmSharedRide::getPointMatchingType), PointMatchingType.MATCH_POINTS_FALSE)
                .set(field(SrmSharedRide::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmSharedRide::getBunchId), bunchId)
                .set(field(SrmSharedRide::getCreationTime), LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .set(field(SrmSharedRide::getBunchNumber), 0)
                .set(field(SrmSharedRide::getCoopTariffParams), coopTariffParams)
                .set(field(SrmSharedRide::getWaypoints), srmWaypoints)
                .set(field(SrmSharedRide::getRequestKpiList), new ArrayList<>(List.of(requestKpi1, requestKpi2)))
                .set(field(SrmSharedRide::getRideCost), 10L)
                .create();
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(Optional.of(sharedRide)).when(sharedRideRepository).findById(rideId);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);
        doReturn(true).when(baseTariffRepository).existsById(tariffId);

        var result = srmService.joinRequest(rideId, multipleRequestDTO, 1);
        assertThat(result).isNull();
        checkInfoLog(1, "SRM: joinRequest: Растояние между двумя соседними точками меньше минимального: 20.0");
    }


    @Test
    void joinRequest_ShouldReturnNull_WhenFilterByTimeAndDistance() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 120);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 4);
        var waypoints = new ArrayList<>(List.of(Instancio.of(SrmWaypointPostDTO.class)
                        .set(field(SrmWaypointPostDTO::getLatitude), 45.93847545175141)
                        .set(field(SrmWaypointPostDTO::getLongitude), 35.07262756028693)
                        .create(),
                Instancio.of(SrmWaypointPostDTO.class)
                        .set(field(SrmWaypointPostDTO::getLatitude), 45.96761772790355)
                        .set(field(SrmWaypointPostDTO::getLongitude), 35.08081345831346)
                        .create()));
        var requestId = UUID.randomUUID();
        var requestKpi1 = createSrmRequestKpi(1);
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_FIRST_AND_LAST_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestPrice), 1000L)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), new ArrayList<>(List.of(singleRequestDTO)))
                .create();
        var bunchId = UUID.randomUUID();
        var rideId = UUID.randomUUID();
        var srmWaypoints = new ArrayList<>(List.of(Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.93847545175142)
                        .set(field(SrmWaypoint::getLongitude), 34.07262756028694)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi1.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 0)
                        .set(field(SrmWaypoint::getEventType), EventType.BOARDING)
                        .create(),
                Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.96761772790356)
                        .set(field(SrmWaypoint::getLongitude), 34.08081345831347)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi1.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 1)
                        .set(field(SrmWaypoint::getEventType), EventType.UNBOARDING)
                        .create()));
        var sharedRide = Instancio.of(SrmSharedRide.class)
                .set(field(SrmSharedRide::getId), rideId)
                .set(field(SrmSharedRide::isActive), true)
                .set(field(SrmSharedRide::getTariffId), tariffId)
                .set(field(SrmSharedRide::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmSharedRide::getPointMatchingType), PointMatchingType.MATCH_FIRST_AND_LAST_POINTS)
                .set(field(SrmSharedRide::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmSharedRide::getBunchId), bunchId)
                .set(field(SrmSharedRide::getCreationTime), LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .set(field(SrmSharedRide::getBunchNumber), 0)
                .set(field(SrmSharedRide::getCoopTariffParams), coopTariffParams)
                .set(field(SrmSharedRide::getWaypoints), srmWaypoints)
                .set(field(SrmSharedRide::getRequestKpiList), new ArrayList<>(List.of(requestKpi1)))
                .set(field(SrmSharedRide::getRideCost), 10L)
                .create();
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(Optional.of(sharedRide)).when(sharedRideRepository).findById(rideId);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);
        doReturn(true).when(baseTariffRepository).existsById(tariffId);

        var result = srmService.joinRequest(rideId, multipleRequestDTO, 1);
        assertThat(result).isNull();
        checkInfoLog(1, "joinRequest: Заявка не подходит по критериям времени и расстояния");
    }

    @Test
    void joinRequest_ShouldReturnNull_WhenFilterByCapacityFails() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 120);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 1);
        var waypoints = createWaypoints();
        var requestId = UUID.randomUUID();
        var requestKpi1 = createSrmRequestKpi(2);
        var requestKpi2 = createSrmRequestKpi(1);
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestPrice), 1000L)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), new ArrayList<>(List.of(singleRequestDTO)))
                .create();
        var distanceMatrixResponseDTO = createDistanceMatrixResponseDTO();
        var bunchId = UUID.randomUUID();
        var rideId = UUID.randomUUID();
        var srmWaypoints = new ArrayList<>(List.of(Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.93847545175142)
                        .set(field(SrmWaypoint::getLongitude), 34.07262756028694)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi1.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 0)
                        .set(field(SrmWaypoint::getEventType), EventType.BOARDING)
                        .create(),
                Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.96761772790356)
                        .set(field(SrmWaypoint::getLongitude), 34.08081345831347)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi2.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 1)
                        .set(field(SrmWaypoint::getEventType), EventType.UNBOARDING)
                        .create()));
        var sharedRide = createSrmSharedRide(rideId, tariffId, bunchId, coopTariffParams, srmWaypoints,
                new ArrayList<>(List.of(requestKpi1, requestKpi2)));
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(Optional.of(sharedRide)).when(sharedRideRepository).findById(rideId);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);
        doReturn(true).when(baseTariffRepository).existsById(tariffId);
        doReturn(distanceMatrixResponseDTO).when(geoService).getDistanceMatrix(anyList(), eq(TransportTypeEnum.TAXI));

        var result = srmService.joinRequest(rideId, multipleRequestDTO, 1);
        assertThat(result).isNull();
        checkInfoLog(10, "joinRequest: Заявка не подходит по метрическим критериям");
    }

    @Test
    void joinRequest_ShouldReturnNull_WhenRequestKpiNotFound1Fails() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 120);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 4);
        var waypoints = createWaypoints();
        var requestId = UUID.randomUUID();
        var requestKpi1 = createSrmRequestKpi(1);
        var requestKpi2 = createSrmRequestKpi(1);
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestPrice), 1000L)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), new ArrayList<>(List.of(singleRequestDTO)))
                .create();
        var distanceMatrixResponseDTO = createDistanceMatrixResponseDTO();
        var bunchId = UUID.randomUUID();
        var rideId = UUID.randomUUID();
        var srmWaypoints = new ArrayList<>(List.of(Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.93847545175142)
                        .set(field(SrmWaypoint::getLongitude), 34.07262756028694)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi1.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 0)
                        .set(field(SrmWaypoint::getEventType), EventType.BOARDING)
                        .create(),
                Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.96761772790356)
                        .set(field(SrmWaypoint::getLongitude), 34.08081345831347)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi2.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 1)
                        .set(field(SrmWaypoint::getEventType), EventType.UNBOARDING)
                        .create()));
        var sharedRide = createSrmSharedRide(rideId, tariffId, bunchId, coopTariffParams, srmWaypoints,
                new ArrayList<>());
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(Optional.of(sharedRide)).when(sharedRideRepository).findById(rideId);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);
        doReturn(true).when(baseTariffRepository).existsById(tariffId);
        doReturn(distanceMatrixResponseDTO).when(geoService).getDistanceMatrix(anyList(), eq(TransportTypeEnum.TAXI));

        var result = srmService.joinRequest(rideId, multipleRequestDTO, 1);
        assertThat(result).isNull();
        checkInfoLog(9, "filterByCapacity: requestKpi not found 1!");
    }

    @Test
    void joinRequest_ShouldReturnNull_WhenRequestKpiNotFound2Fails() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 120);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 4);
        var waypoints = createWaypoints();
        var requestId = UUID.randomUUID();
        var requestKpi1 = createSrmRequestKpi(1);
        var requestKpi2 = createSrmRequestKpi(1);
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestPrice), 1000L)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), new ArrayList<>(List.of(singleRequestDTO)))
                .create();
        var distanceMatrixResponseDTO = createDistanceMatrixResponseDTO();
        var bunchId = UUID.randomUUID();
        var rideId = UUID.randomUUID();
        var srmWaypoints = new ArrayList<>(List.of(Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.93847545175142)
                        .set(field(SrmWaypoint::getLongitude), 34.07262756028694)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi1.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 0)
                        .set(field(SrmWaypoint::getEventType), EventType.BOARDING)
                        .create(),
                Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.96761772790356)
                        .set(field(SrmWaypoint::getLongitude), 34.08081345831347)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi2.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 1)
                        .set(field(SrmWaypoint::getEventType), EventType.UNBOARDING)
                        .create()));
        var sharedRide = createSrmSharedRide(rideId, tariffId, bunchId, coopTariffParams, srmWaypoints,
                new ArrayList<>(List.of(requestKpi1)));
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(Optional.of(sharedRide)).when(sharedRideRepository).findById(rideId);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);
        doReturn(true).when(baseTariffRepository).existsById(tariffId);
        doReturn(distanceMatrixResponseDTO).when(geoService).getDistanceMatrix(anyList(), eq(TransportTypeEnum.TAXI));

        var result = srmService.joinRequest(rideId, multipleRequestDTO, 1);
        assertThat(result).isNull();
        checkInfoLog(9, "filterByCapacity: requestKpi not found 2!");
    }

    @Test
    void joinRequest_ShouldReturnNull_WhenFilterByDeviationTimeFails() {
        var tariffId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 1);
        var tariff = createTaxiTariff(tariffId, coopTariffParams, 4);
        var waypoints = createWaypoints();
        var requestId = UUID.randomUUID();
        var requestKpi1 = createSrmRequestKpi(1);
        var requestKpi2 = createSrmRequestKpi(1);
        var pickupTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(1);
        var dropTime = ZonedDateTime.now(ZoneOffset.UTC).plusHours(3);
        var singleRequestDTO = createSingleRequestDTO(requestId, 1, waypoints);
        var multipleRequestDTO = Instancio.of(SrmMultipleRequestDTO.class)
                .set(field(SrmMultipleRequestDTO::getMultipleRequestId), UUID.randomUUID())
                .set(field(SrmMultipleRequestDTO::getTariffId), tariffId)
                .set(field(SrmMultipleRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmMultipleRequestDTO::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmMultipleRequestDTO::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmMultipleRequestDTO::getPickupTime), pickupTime)
                .set(field(SrmMultipleRequestDTO::getDropTime), dropTime)
                .set(field(SrmMultipleRequestDTO::getRequestPrice), 1000L)
                .set(field(SrmMultipleRequestDTO::getRequestDTOList), new ArrayList<>(List.of(singleRequestDTO)))
                .create();
        var distanceMatrixResponseDTO = createDistanceMatrixResponseDTO();
        var bunchId = UUID.randomUUID();
        var rideId = UUID.randomUUID();
        var srmWaypoints = new ArrayList<>(List.of(Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.93847545175142)
                        .set(field(SrmWaypoint::getLongitude), 34.07262756028694)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi2.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 0)
                        .set(field(SrmWaypoint::getEventType), EventType.BOARDING)
                        .create(),
                Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.96761772790356)
                        .set(field(SrmWaypoint::getLongitude), 34.08081345831347)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi1.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 1)
                        .set(field(SrmWaypoint::getEventType), EventType.BOARDING)
                        .create()));
        var sharedRide = createSrmSharedRide(rideId, tariffId, bunchId, coopTariffParams, srmWaypoints,
                new ArrayList<>(List.of(requestKpi1, requestKpi2)));
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(Optional.of(sharedRide)).when(sharedRideRepository).findById(rideId);
        doReturn(taxiTariffService).when(tariffServices).get(TransportTypeEnum.TAXI);
        doReturn(Optional.of(tariff)).when(taxiTariffService).findById(tariffId);
        doReturn(true).when(baseTariffRepository).existsById(tariffId);
        doReturn(distanceMatrixResponseDTO).when(geoService).getDistanceMatrix(anyList(), eq(TransportTypeEnum.TAXI));

        var result = srmService.joinRequest(rideId, multipleRequestDTO, 1);
        assertThat(result).isNull();
        checkInfoLog(12, "joinRequest: слишком поздно! Присоединение невозможно!");
    }

    @Test
    void finishSharedRide() {
        var rideId = UUID.randomUUID();
        var wrongRideId = UUID.randomUUID();
        var tariffId = UUID.randomUUID();
        var bunchId = UUID.randomUUID();
        var coopTariffParams = createCoopTariffParams(1.0, 120);
        var requestKpi1 = createSrmRequestKpi(1);
        var requestKpi2 = createSrmRequestKpi(1);
        var srmWaypoints = new ArrayList<>(List.of(Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.93847545175142)
                        .set(field(SrmWaypoint::getLongitude), 34.07262756028694)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi2.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 0)
                        .set(field(SrmWaypoint::getEventType), EventType.BOARDING)
                        .create(),
                Instancio.of(SrmWaypoint.class)
                        .set(field(SrmWaypoint::getLatitude), 44.96761772790356)
                        .set(field(SrmWaypoint::getLongitude), 34.08081345831347)
                        .set(field(SrmWaypoint::getStartTime), ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(1))
                        .set(field(SrmWaypoint::getRequestKpiId), requestKpi1.getId())
                        .set(field(SrmWaypoint::getOrgOrderingIndex), 1)
                        .set(field(SrmWaypoint::getEventType), EventType.BOARDING)
                        .create()));
        var sharedRide = createSrmSharedRide(rideId, tariffId, bunchId, coopTariffParams, srmWaypoints,
                new ArrayList<>(List.of(requestKpi1, requestKpi2)));
        var expected = Instancio.create(SrmSharedRideDTO.class);
        LOGGING_EXTENSION.setLogLevel(Level.INFO);
        doReturn(Optional.of(sharedRide)).when(sharedRideRepository).findById(rideId);
        doReturn(sharedRide).when(sharedRideRepository).save(any());
        doReturn(expected).when(entityDtoConverter).sharedRideToSharedRideDto(srmSharedRideArgumentCaptor.capture());
        var actual = srmService.finishSharedRide(rideId);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        var saveShareRide = srmSharedRideArgumentCaptor.getValue();
        assertThat(sharedRide)
                .usingRecursiveComparison()
                .ignoringFields("active")
                .isEqualTo(saveShareRide);
        assertThat(saveShareRide.isActive()).isFalse();
        assertThrows(SrmLogicException.class,
                () -> srmService.finishSharedRide(wrongRideId),
                "Совместная поездка не найдена");
    }

    @Test
    void getAll() {
        var srmSharedRide1 = Instancio.create(SrmSharedRide.class);
        var srmSharedRide2 = Instancio.create(SrmSharedRide.class);
        var srmSharedRide3 = Instancio.create(SrmSharedRide.class);
        var srmSharedRideDTO1 = Instancio.create(SrmSharedRideDTO.class);
        var srmSharedRideDTO2 = Instancio.create(SrmSharedRideDTO.class);
        var srmSharedRideDTO3 = Instancio.create(SrmSharedRideDTO.class);
        doReturn(List.of(srmSharedRide1, srmSharedRide2, srmSharedRide3)).when(sharedRideRepository).findAll();
        doReturn(srmSharedRideDTO1).when(entityDtoConverter).sharedRideToSharedRideDto(srmSharedRide1);
        doReturn(srmSharedRideDTO2).when(entityDtoConverter).sharedRideToSharedRideDto(srmSharedRide2);
        doReturn(srmSharedRideDTO3).when(entityDtoConverter).sharedRideToSharedRideDto(srmSharedRide3);
        var actual = srmService.getAll();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(List.of(srmSharedRideDTO1, srmSharedRideDTO2, srmSharedRideDTO3));
    }

    @Test
    void getByRequestId() {
        var srmRequestKpi1 = Instancio.create(SrmRequestKpi.class);
        var srmRequestKpi2 = Instancio.create(SrmRequestKpi.class);
        var srmSharedRide = Instancio.create(SrmSharedRide.class);
        var srmSharedRideDTO = Instancio.create(SrmSharedRideDTO.class);
        var requestId1 = UUID.randomUUID();
        var requestId2 = UUID.randomUUID();
        var requestId3 = UUID.randomUUID();
        doReturn(Optional.of(srmRequestKpi1)).when(requestKpiRepository).findByOrgRequestId(requestId1);
        doReturn(Optional.of(srmRequestKpi2)).when(requestKpiRepository).findByOrgRequestId(requestId2);
        doReturn(Optional.empty()).when(requestKpiRepository).findByOrgRequestId(requestId3);
        doReturn(Optional.of(srmSharedRide)).when(sharedRideRepository).findById(srmRequestKpi1.getSharedRide().getId());
        doReturn(Optional.empty()).when(sharedRideRepository).findById(srmRequestKpi2.getSharedRide().getId());
        doReturn(srmSharedRideDTO).when(entityDtoConverter).sharedRideToSharedRideDto(srmSharedRide);
        assertThat(srmService.getByRequestId(requestId1))
                .usingRecursiveComparison()
                .isEqualTo(srmSharedRideDTO);
        assertThat(srmService.getByRequestId(requestId2)).isNull();
        assertThat(srmService.getByRequestId(requestId3)).isNull();
        verify(requestKpiRepository, times(3)).findByOrgRequestId(any(UUID.class));
        verify(sharedRideRepository, times(2)).findById(any(UUID.class));
        verify(entityDtoConverter).sharedRideToSharedRideDto(any(SrmSharedRide.class));
    }

    private static SrmSharedRide createSrmSharedRide(UUID rideId,
                                                     UUID tariffId,
                                                     UUID bunchId,
                                                     CoopTariffParams coopTariffParams,
                                                     ArrayList<SrmWaypoint> srmWaypoints,
                                                     List<SrmRequestKpi> srmRequestKpiList) {
        return Instancio.of(SrmSharedRide.class)
                .set(field(SrmSharedRide::getId), rideId)
                .set(field(SrmSharedRide::isActive), true)
                .set(field(SrmSharedRide::getTariffId), tariffId)
                .set(field(SrmSharedRide::getTransportType), TransportTypeEnum.TAXI)
                .set(field(SrmSharedRide::getPointMatchingType), PointMatchingType.MATCH_ALL_POINTS)
                .set(field(SrmSharedRide::getTimeZone), ZoneOffset.UTC.getId())
                .set(field(SrmSharedRide::getBunchId), bunchId)
                .set(field(SrmSharedRide::getCreationTime), LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                .set(field(SrmSharedRide::getBunchNumber), 0)
                .set(field(SrmSharedRide::getCoopTariffParams), coopTariffParams)
                .set(field(SrmSharedRide::getWaypoints), srmWaypoints)
                .set(field(SrmSharedRide::getRequestKpiList), srmRequestKpiList)
                .set(field(SrmSharedRide::getRideCost), 10L)
                .create();
    }

    private static TaxiTariff createTaxiTariff(UUID tariffId, CoopTariffParams coopTariffParams, int v) {
        return Instancio.of(TaxiTariff.class)
                .set(field(TaxiTariff::getId), tariffId)
                .set(field(TaxiTariff::getTransportType), TransportTypeEnum.TAXI)
                .set(field(TaxiTariff::getCoopTariffParams), coopTariffParams)
                .set(field(TaxiTariff::getMaxCapacity), v)
                .set(field(TaxiTariff::getRideCostPerKm), 1)
                .set(field(TaxiTariff::getRideCostPerMin), 1)
                .set(field(TaxiTariff::getMinRideTimeCost), 1)
                .set(field(TaxiTariff::getMinRideDistanceCost), 1)
                .set(field(TaxiTariff::getWaitCostPerMin), 1)
                .set(field(TaxiTariff::getWaitCostPerMinIntermediate), 1)
                .set(field(TaxiTariff::getFreeWaitingTime), 10)
                .create();
    }

    private static CoopTariffParams createCoopTariffParams(double distanceDeviationKm, int timeDeviationMin) {
        return Instancio.of(CoopTariffParams.class)
                .set(field(CoopTariffParams::getDistanceDeviationKm), distanceDeviationKm)
                .set(field(CoopTariffParams::getTimeDeviationMin), timeDeviationMin)
                .set(field(CoopTariffParams::getSavingsDeviationPct), 10.0)
                .set(field(CoopTariffParams::getMinCancelTimeMin), 5)
                .create();
    }

    private static SrmSingleRequestDTO createSingleRequestDTO(UUID requestId, int requiredPassengers, List<SrmWaypointPostDTO> waypoints) {
        return Instancio.of(SrmSingleRequestDTO.class)
                .set(field(SrmSingleRequestDTO::getRequestId), requestId)
                .set(field(SrmSingleRequestDTO::getRequiredPassengers), requiredPassengers)
                .set(field(SrmSingleRequestDTO::getWaypoints), waypoints)
                .create();
    }

    private static SrmRequestKpi createSrmRequestKpi(int requiredPassengers) {
        return Instancio.of(SrmRequestKpi.class)
                .set(field(SrmRequestKpi::getSavingsCash), 10000L)
                .set(field(SrmRequestKpi::getSavingsProcents), 15.0)
                .set(field(SrmRequestKpi::getRequiredPassengers), requiredPassengers)
                .set(field(SrmRequestKpi::getRequestPrice), 1000L)
                .set(field(SrmRequestKpi::getRequestFullPrice), 1500L)
                .set(field(SrmRequestKpi::getCostSharePart), 10.0)
                .set(field(SrmRequestKpi::getRequestDistance), 100.0)
                .create();
    }

    @NotNull
    private static List<SrmWaypointPostDTO> createWaypoints() {
        return new ArrayList<>(List.of(Instancio.of(SrmWaypointPostDTO.class)
                        .set(field(SrmWaypointPostDTO::getLatitude), 44.93847545175141)
                        .set(field(SrmWaypointPostDTO::getLongitude), 34.07262756028693)
                        .create(),
                Instancio.of(SrmWaypointPostDTO.class)
                        .set(field(SrmWaypointPostDTO::getLatitude), 44.96761772790355)
                        .set(field(SrmWaypointPostDTO::getLongitude), 34.08081345831346)
                        .create()));
    }

    @NotNull
    private static DistanceMatrixResponseDTO createDistanceMatrixResponseDTO() {
        var distanceMatrixResponseDTO = new DistanceMatrixResponseDTO();
        distanceMatrixResponseDTO.setOrigins(IntStream.range(0, 3)
                .mapToObj(i -> {
                    final var row = new DistanceMatrixResponseDTO.Row();
                    row.setDestinations(IntStream.range(0, 3)
                            .mapToObj(j -> new DistanceMatrixResponseDTO.Element(
                                    60,
                                    100,
                                    "OK"
                            )).collect(Collectors.toList()));
                    return row;
                }).collect(Collectors.toList()));
        return distanceMatrixResponseDTO;
    }

    private static void checkInfoLog(int size, String message) {
        assertThat(LOGGING_EXTENSION.getEvents()).hasSize(size);
        var firstEvent = LOGGING_EXTENSION.getEvents().get(size - 1);
        assertThat(firstEvent.getLoggerName()).isEqualTo(SrmServiceImpl.class.getName());
        assertThat(firstEvent.getFormattedMessage()).isEqualTo(message);
        assertThat(firstEvent.getThrowableProxy()).isNull();
        assertThat(firstEvent.getLevel()).isEqualTo(Level.INFO);
    }

    private static void checkDebugLog(int size, String message) {
        assertThat(LOGGING_EXTENSION.getEvents()).hasSize(size);
        var firstEvent = LOGGING_EXTENSION.getEvents().get(size - 1);
        assertThat(firstEvent.getLoggerName()).isEqualTo(SrmServiceImpl.class.getName());
        assertThat(firstEvent.getFormattedMessage()).isEqualTo(message);
        assertThat(firstEvent.getThrowableProxy().getClassName()).isEqualTo(SrmLogicException.class.getName());
        assertThat(firstEvent.getLevel()).isEqualTo(Level.DEBUG);
    }
}