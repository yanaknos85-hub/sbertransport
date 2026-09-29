package ru.sberbank.ditsib.transport.srm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.srm.config.GisDataProperties;
import ru.sberbank.ditsib.transport.srm.config.GisProvidersProperties;
import ru.sberbank.ditsib.transport.srm.dto.DistanceMatrixResponseDTO;
import ru.sberbank.ditsib.transport.srm.dto.twogis.*;
import ru.sberbank.ditsib.transport.srm.exception.TwoGisExchangeException;
import ru.sberbank.ditsib.transport.srm.feign.GisAsyncFeignClient;
import ru.sberbank.ditsib.transport.srm.feign.GisAsyncResultFeignClient;
import ru.sberbank.ditsib.transport.srm.feign.GisFeignClient;
import ru.sberbank.ditsib.transport.srm.feign.SowaFeignClient;
import ru.sberbank.ditsib.transport.srm.model.SrmWaypoint;
import ru.sberbank.ditsib.transport.srm.service.GeoService;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static ru.sberbank.ditsib.transport.srm.dto.twogis.TaskStatusEnum.TASK_CANCELED;
import static ru.sberbank.ditsib.transport.srm.dto.twogis.TaskStatusEnum.TASK_DONE;

/**
 * Имплементация сервиса 2гис для получения матрицы расстояний Distance Matrix API supports two modes of operation: Calculating requests that contain
 * up to 25 starting or ending points in synchronous mode. In this mode, the request returns the result of calculation. Calculating requests that
 * contain up to 1000 starting or ending points in asynchronous mode. In this mode, the request returns the task ID, which should be used to
 * periodically check if the calculation is complete (see Large number of points).
 * <a href="https://docs.2gis.com/en/api/navigation/distance-matrix/reference/get_dist_matrix#/paths/~1get_dist_matrix/post">...</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeoService2GisImpl implements GeoService {

    private static final String GIS_VERSION = "2.0";
    private final GisFeignClient gisFeignClient;
    private final GisAsyncFeignClient gisAsyncFeignClient;
    private final GisAsyncResultFeignClient gisAsyncResultFeignClient;
    private final SowaFeignClient sowaFeignClient;
    private final GisProvidersProperties gisProvidersProperties;
    private final GisDataProperties gisDataProperties;
    private final ObjectMapper objectMapper;

    @Override
    public DistanceMatrixResponseDTO getDistanceMatrix(List<SrmWaypoint> waypointList, TransportTypeEnum transportType) {
            try {
                return getDistanceMatrixResponseDTO(waypointList, transportType, true);
            } catch (Exception e) {
                return getDistanceMatrixResponseDTO(waypointList, transportType, false);
            }
    }

    @Override
    public DistanceMatrixResponseDTO getDistanceMatrixAsync(List<SrmWaypoint> waypointList, TransportTypeEnum transportType) {
        final var timeStart = LocalDateTime.now();
        var distanceMatrixResponseDTO = getDistanceMatrixAsyncInternal(waypointList, transportType, true);
        if (distanceMatrixResponseDTO == null) {
            log.info("SRM: getDistanceMatrixAsync: an error occurred when building a route with exclusion of dirty roads");
            distanceMatrixResponseDTO = getDistanceMatrixAsyncInternal(waypointList, transportType, false);
        }
        var timeDuration = Duration.between(LocalDateTime.now(), timeStart).abs().toMillis();
        log.trace("SRM: getDistanceMatrixAsync finish: duration = {}", timeDuration);
        return distanceMatrixResponseDTO;
    }

    private DistanceMatrixResponseDTO getDistanceMatrixAsyncInternal(
            List<SrmWaypoint> waypointList,
            TransportTypeEnum transportType,
            boolean excludeDirtRoad
    ) {
        var executor = Executors.newSingleThreadExecutor();
        var feature = CompletableFuture.supplyAsync(() -> {
                    try {
                        return requestDistanceMatrixAndGetResult(waypointList, transportType, excludeDirtRoad);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return new DistanceMatrixResponseDTO();
                    }
                }, executor)
                .completeOnTimeout(new DistanceMatrixResponseDTO(), gisDataProperties.getTimeout(), TimeUnit.MILLISECONDS)
                .exceptionally(e -> {
                    log.error("requestDistanceMatrixAndGetResult: Error while getting result", e);
                    Thread.currentThread().interrupt();
                    return new DistanceMatrixResponseDTO();
                });
        try {
            return feature.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("requestDistanceMatrixAndGetResult: Error while getting result", e);
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdownNow();
        }
        return new DistanceMatrixResponseDTO();
    }

    private DistanceMatrixResponseDTO requestDistanceMatrixAndGetResult(
            List<SrmWaypoint> waypointList, TransportTypeEnum transportType, boolean excludeDirtRoad
    ) throws InterruptedException {
        log.trace("requestDistanceMatrixAndGetResult: start with num of waypoints = {}", waypointList.size());
        final var twoGisMatrixAsyncCreateDto = requestDistanceMatrixAsync(waypointList, transportType, excludeDirtRoad);
        log.trace("requestDistanceMatrixAndGetResult: twoGisMatrixAsyncCreateDto = {}", twoGisMatrixAsyncCreateDto);
        var twoGisMatrixAsyncCheckStatusDto = checkDistanceMatrixStatus(twoGisMatrixAsyncCreateDto);
        try {
            final var resultLink = twoGisMatrixAsyncCheckStatusDto.resultLink()
                    .replaceFirst("https?://disk.2gis.com", gisDataProperties.getSowaUrl());
            log.trace("requestDistanceMatrixAndGetResult: going to download from = {}", resultLink);
            final var response = sowaFeignClient.getDistMatrix(URI.create(resultLink));
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.info("Wrong download from sowa response, status:{}, body:{}", response.getStatusCode(), response.getBody());
                throw new TwoGisExchangeException();
            }
            var result = response.getBody();
            log.trace("requestDistanceMatrixAndGetResult: matrix = {}", result);
            var twoGisMatrixResponseDto = objectMapper.readValue(result, TwoGisMatrixResponseDto.class);
            return convert(twoGisMatrixResponseDto, waypointList.size());
        } catch (Exception e) {
            log.error("requestDistanceMatrixAndGetResult: exception caught!", e);
        }
        return null;
    }

    private TwoGisMatrixAsyncCheckStatusDto checkDistanceMatrixStatus(TwoGisMatrixAsyncCreateDto twoGisMatrixAsyncCreateDto)
            throws InterruptedException {
        var result = getDistanceMatrixAsyncResult(twoGisMatrixAsyncCreateDto);
        if (!TASK_DONE.name().equals(result.status()) && !TASK_CANCELED.name().equals(result.status())) {
            if (gisDataProperties.getSleepTime() > 0) {
                Thread.sleep(gisDataProperties.getSleepTime());
            }
            return checkDistanceMatrixStatus(twoGisMatrixAsyncCreateDto);
        }
        return result;
    }

    private TwoGisMatrixAsyncCreateDto requestDistanceMatrixAsync(List<SrmWaypoint> waypointList,
                                                                  TransportTypeEnum transportType,
                                                                  boolean excludeDirtRoad) {
        final var twoGisMatrixRequestDto = build2GisMatrixRequest(waypointList, transportType, excludeDirtRoad);
        return gisAsyncFeignClient.getDistMatrix(gisProvidersProperties.getTwogisasync().getApiKey(),
                GIS_VERSION,
                twoGisMatrixRequestDto);
    }

    private TwoGisMatrixAsyncCheckStatusDto getDistanceMatrixAsyncResult(TwoGisMatrixAsyncCreateDto twoGisMatrixAsyncCreateDto) {
        return gisAsyncResultFeignClient.getDistMatrix(twoGisMatrixAsyncCreateDto.getTask_id(),
                gisProvidersProperties.getTwogisasyncresult().getApiKey());
    }

    @NotNull
    private DistanceMatrixResponseDTO getDistanceMatrixResponseDTO(List<SrmWaypoint> waypointList,
                                                                   TransportTypeEnum transportType,
                                                                   boolean excludeDirtRoad) {
        final var twoGisMatrixRequestDto = build2GisMatrixRequest(waypointList, transportType, excludeDirtRoad);
        return makeDistanceMatrixRequest(waypointList, twoGisMatrixRequestDto);
    }

    private DistanceMatrixResponseDTO makeDistanceMatrixRequest(List<SrmWaypoint> waypointList,
                                                                TwoGisMatrixRequestDto twoGisMatrixRequestDto) {
        final var result = gisFeignClient.getDistMatrix(gisProvidersProperties.getTwogis().getApiKey(),
                GIS_VERSION,
                twoGisMatrixRequestDto);
        return convert(result, waypointList.size());
    }

    private TwoGisMatrixRequestDto build2GisMatrixRequest(List<SrmWaypoint> waypointList,
                                                          TransportTypeEnum transportType,
                                                          boolean excludeDirtRoad) {
        final var twoGisMatrixRequestDto = new TwoGisMatrixRequestDto();

        for (final var waypoint : waypointList) {
            final var point = new TwoGisMatrixRequestDto.Point();
            point.setLat(waypoint.getLatitude());
            point.setLon(waypoint.getLongitude());
            twoGisMatrixRequestDto.getPoints().add(point);
        }
        for (int i = 0; i < waypointList.size(); i++) {
            twoGisMatrixRequestDto.getSources().add(i);
            twoGisMatrixRequestDto.getTargets().add(i);
        }
        if (Objects.requireNonNull(transportType) == TransportTypeEnum.TAXI || transportType == TransportTypeEnum.PERSONAL) {
            twoGisMatrixRequestDto.setMode(TwoGisModeEnum.driving.name());
            twoGisMatrixRequestDto.setType(TwoGisTypeEnum.shortest.name());
        }
        if (excludeDirtRoad) {
            twoGisMatrixRequestDto.setFilters(Stream.of(TwoGisFilterEnum.dirt_road).map(TwoGisFilterEnum::name).toList());
        }
        return twoGisMatrixRequestDto;
    }

    private DistanceMatrixResponseDTO convert(TwoGisMatrixResponseDto twoGisMatrixResponseDto, int numOfWaypoints) {
        final var distanceMatrixResponseDTO = new DistanceMatrixResponseDTO();
        if (twoGisMatrixResponseDto.getRoutes().isEmpty()) {
            return distanceMatrixResponseDTO;
        }
        // initializing matrix with 'INIT' elements
        final var elements = new DistanceMatrixResponseDTO.Element[numOfWaypoints][numOfWaypoints];
        // filling matrix with real elements
        for (final var route : twoGisMatrixResponseDto.getRoutes()) {
            final var element = new DistanceMatrixResponseDTO.Element(
                    route.duration(),
                    route.distance(),
                    route.status()
                    );
            elements[route.source_id()][route.target_id()] = element;
        }
        // transferring data from array to result object
        for (int org = 0; org < numOfWaypoints; org++) {
            final var destinations = Arrays.asList(elements[org]).subList(0, numOfWaypoints);
            if (destinations.stream().anyMatch(Objects::nonNull)) {
                final var row = new DistanceMatrixResponseDTO.Row();
                row.getDestinations().addAll(destinations);
                distanceMatrixResponseDTO.getOrigins().add(row);
            }
        }
        return distanceMatrixResponseDTO;
    }
}