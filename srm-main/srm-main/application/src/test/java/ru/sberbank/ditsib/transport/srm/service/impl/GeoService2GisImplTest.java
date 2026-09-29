package ru.sberbank.ditsib.transport.srm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.srm.config.GisDataProperties;
import ru.sberbank.ditsib.transport.srm.config.GisProviderProperties;
import ru.sberbank.ditsib.transport.srm.config.GisProvidersProperties;
import ru.sberbank.ditsib.transport.srm.dto.DistanceMatrixResponseDTO;
import ru.sberbank.ditsib.transport.srm.dto.twogis.TaskStatusEnum;
import ru.sberbank.ditsib.transport.srm.dto.twogis.TwoGisMatrixAsyncCheckStatusDto;
import ru.sberbank.ditsib.transport.srm.dto.twogis.TwoGisMatrixAsyncCreateDto;
import ru.sberbank.ditsib.transport.srm.dto.twogis.TwoGisMatrixResponseDto;
import ru.sberbank.ditsib.transport.srm.feign.GisAsyncFeignClient;
import ru.sberbank.ditsib.transport.srm.feign.GisAsyncResultFeignClient;
import ru.sberbank.ditsib.transport.srm.feign.GisFeignClient;
import ru.sberbank.ditsib.transport.srm.feign.SowaFeignClient;
import ru.sberbank.ditsib.transport.srm.model.SrmWaypoint;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;
import static ru.sberbank.ditsib.transport.srm.dto.twogis.TaskStatusEnum.*;

@ExtendWith(MockitoExtension.class)
class GeoService2GisImplTest {
    @InjectMocks
    private GeoService2GisImpl geoService;
    @Mock
    private GisFeignClient gisFeignClient;
    @Mock
    private GisAsyncFeignClient gisAsyncFeignClient;
    @Mock
    private GisAsyncResultFeignClient gisAsyncResultFeignClient;
    @Mock
    private SowaFeignClient sowaFeignClient;
    @Mock
    private GisProvidersProperties gisProvidersProperties;
    @Mock
    private GisDataProperties gisDataProperties;
    @Mock
    private ObjectMapper objectMapper;

    @Test
    void getDistanceMatrix_withEmployeeTransportation_andSuccess() {
        final var waypoints = Instancio.ofList(SrmWaypoint.class)
                .size(3)
                .create();
        final var gisResponse = Instancio.of(TwoGisMatrixResponseDto.class)
                .set(field(TwoGisMatrixResponseDto::getRoutes), List.of(
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), 100)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), 60)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 0)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "OK")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 0)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), null)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), null)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 0)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "NO_ROUTE")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 1)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), null)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), null)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 0)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "NO_ROUTE")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 2)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), null)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), null)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 1)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "NO_ROUTE")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 0)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), 100)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), 60)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 1)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "OK")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 1)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), null)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), null)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 1)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "NO_ROUTE")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 2)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), null)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), null)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 2)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "NO_ROUTE")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 0)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), null)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), null)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 2)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "NO_ROUTE")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 1)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), 100)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), 60)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 2)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "OK")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 2)
                                .create()
                ))
                .create();
        final var gisProperties = Instancio.create(GisProviderProperties.class);
        doReturn(gisProperties).when(gisProvidersProperties).getTwogis();
        doReturn(gisResponse).when(gisFeignClient).getDistMatrix(anyString(), anyString(), any());

        final var expected = new DistanceMatrixResponseDTO();
        expected.setOrigins(IntStream.range(0, 3)
                .mapToObj(i -> {
                    final var row = new DistanceMatrixResponseDTO.Row();
                    row.setDestinations(IntStream.range(0, 3)
                            .mapToObj(j -> {
                                if (i == j) {
                                    return new DistanceMatrixResponseDTO.Element(
                                            60,
                                            100,
                                            "OK"
                                    );
                                } else {
                                    return new DistanceMatrixResponseDTO.Element(
                                            null,
                                            null,
                                            "NO_ROUTE"
                                    );
                                }
                            }).collect(Collectors.toList()));
                    return row;
                }).collect(Collectors.toList()));

        final var actual = geoService.getDistanceMatrix(waypoints, TransportTypeEnum.TAXI);

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        verify(gisFeignClient, times(1)).getDistMatrix(anyString(), anyString(), any());
    }

    @Test
    void getDistanceMatrix_withEmployeeTransportation_andError_thenFallback() {
        final var waypoints = Instancio.ofList(SrmWaypoint.class)
                .size(3)
                .create();
        var gisProperties = Instancio.create(GisProviderProperties.class);
        doReturn(gisProperties).when(gisProvidersProperties).getTwogis();
        doThrow(new RuntimeException("Simulated error")).when(gisFeignClient).getDistMatrix(anyString(), anyString(), any());
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> geoService.getDistanceMatrix(waypoints, TransportTypeEnum.TAXI))
                .withMessage("Simulated error");
        verify(gisFeignClient, times(2)).getDistMatrix(anyString(), anyString(), any());
    }

    @Test
    void getDistanceMatrixAsync_withTaskTimeout_returnsEmpty() {
        final var waypoints = Instancio.ofList(SrmWaypoint.class)
                .size(3)
                .create();
        final var createResponse = Instancio.of(TwoGisMatrixAsyncCreateDto.class)
                .set(field(TwoGisMatrixAsyncCreateDto::getTask_id), "task-123")
                .create();
        final var statusResponse = Instancio.of(TwoGisMatrixAsyncCheckStatusDto.class)
                .set(field(TwoGisMatrixAsyncCheckStatusDto::status), TASK_RUNNING.name())
                .create();
        final var gisAsyncProperties = Instancio.create(GisProviderProperties.class);
        final var gisAsyncResultProperties = Instancio.create(GisProviderProperties.class);
        doReturn(gisAsyncProperties).when(gisProvidersProperties).getTwogisasync();
        doReturn(gisAsyncResultProperties).when(gisProvidersProperties).getTwogisasyncresult();
        doReturn(3000).when(gisDataProperties).getTimeout();
        doReturn(1000).when(gisDataProperties).getSleepTime();
        doReturn(createResponse).when(gisAsyncFeignClient).getDistMatrix(anyString(), anyString(), any());
        doReturn(statusResponse).when(gisAsyncResultFeignClient).getDistMatrix(eq("task-123"), anyString());

        final var expected = new DistanceMatrixResponseDTO();

        final var actual = geoService.getDistanceMatrixAsync(waypoints, TransportTypeEnum.TAXI);

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        verify(gisAsyncFeignClient, times(1)).getDistMatrix(anyString(), anyString(), any());
        verify(gisAsyncResultFeignClient, atLeast(3)).getDistMatrix(eq("task-123"), anyString());
        verify(gisAsyncResultFeignClient, atMost(4)).getDistMatrix(eq("task-123"), anyString());
    }

    @SneakyThrows
    @Test
    void getDistanceMatrixAsyncInternal_withMultipleStatusChecks() {
        final var waypoints = Instancio.ofList(SrmWaypoint.class)
                .size(3)
                .create();
        final var createResponse = Instancio.of(TwoGisMatrixAsyncCreateDto.class)
                .set(field(TwoGisMatrixAsyncCreateDto::getTask_id), "task-123")
                .create();
        final var statusResponse1 = Instancio.of(TwoGisMatrixAsyncCheckStatusDto.class)
                .set(field(TwoGisMatrixAsyncCheckStatusDto::status), TASK_RUNNING.name())
                .create();
        final var statusResponse2 = Instancio.of(TwoGisMatrixAsyncCheckStatusDto.class)
                .set(field(TwoGisMatrixAsyncCheckStatusDto::status), TASK_DONE.name())
                .set(field(TwoGisMatrixAsyncCheckStatusDto::resultLink), "https://result.com")
                .create();
        final var body = """
                    {
                      "generation_time": null,
                      "routes": [
                        {
                          "distance": 100,
                          "duration": 60,
                          "source_id": 0,
                          "status": "OK",
                          "target_id": 0
                        },
                        {
                          "distance": 100,
                          "duration": 60,
                          "source_id": 0,
                          "status": "OK",
                          "target_id": 1
                        },
                        {
                          "distance": 100,
                          "duration": 60,
                          "source_id": 0,
                          "status": "OK",
                          "target_id": 2
                        },
                        {
                          "distance": 100,
                          "duration": 60,
                          "source_id": 1,
                          "status": "OK",
                          "target_id": 0
                        },
                        {
                          "distance": 100,
                          "duration": 60,
                          "source_id": 1,
                          "status": "OK",
                          "target_id": 1
                        },
                        {
                          "distance": 100,
                          "duration": 60,
                          "source_id": 1,
                          "status": "OK",
                          "target_id": 2
                        },
                        {
                          "distance": 100,
                          "duration": 60,
                          "source_id": 2,
                          "status": "OK",
                          "target_id": 0
                        },
                        {
                          "distance": 100,
                          "duration": 60,
                          "source_id": 2,
                          "status": "OK",
                          "target_id": 1
                        },
                        {
                          "distance": 100,
                          "duration": 60,
                          "source_id": 2,
                          "status": "OK",
                          "target_id": 2
                        }
                      ]
                    }
                """;
        final var twoGisResult = ResponseEntity.ok()
                .body(body);
        final var gisResultMapped = createTwoGisMatrixResponseDto();
        final var gisAsyncProperties = Instancio.create(GisProviderProperties.class);
        final var gisAsyncResultProperties = Instancio.create(GisProviderProperties.class);
        doReturn(gisAsyncProperties).when(gisProvidersProperties).getTwogisasync();
        doReturn(gisAsyncResultProperties).when(gisProvidersProperties).getTwogisasyncresult();
        doReturn(Instancio.create(String.class)).when(gisDataProperties).getSowaUrl();
        doReturn(10000).when(gisDataProperties).getTimeout();
        doReturn(1000).when(gisDataProperties).getSleepTime();
        doReturn(createResponse).when(gisAsyncFeignClient).getDistMatrix(anyString(), anyString(), any());
        doReturn(statusResponse1, statusResponse2).when(gisAsyncResultFeignClient).getDistMatrix(eq("task-123"), anyString());
        doReturn(twoGisResult).when(sowaFeignClient).getDistMatrix(any());
        doReturn(gisResultMapped).when(objectMapper).readValue(twoGisResult.getBody(), TwoGisMatrixResponseDto.class);

        final var expected = new DistanceMatrixResponseDTO();
        expected.setOrigins(IntStream.range(0, 3)
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

        final var actual = geoService.getDistanceMatrixAsync(waypoints, TransportTypeEnum.TAXI);

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        verify(gisAsyncResultFeignClient, times(2)).getDistMatrix(eq("task-123"), anyString());
    }

    @Test
    void getDistanceMatrixAsync_withTaskDoneStatus() {
        getDistanceMatrixAsync_withTaskStatus(TASK_DONE);
    }

    @Test
    void getDistanceMatrixAsync_withTaskCancelStatus() {
        getDistanceMatrixAsync_withTaskStatus(TASK_CANCELED);
    }

    @SneakyThrows
    private void getDistanceMatrixAsync_withTaskStatus(TaskStatusEnum taskStatusEnum) {
        final var waypoints = Instancio.ofList(SrmWaypoint.class)
                .size(3)
                .create();
        final var createResponse = Instancio.of(TwoGisMatrixAsyncCreateDto.class)
                .set(field(TwoGisMatrixAsyncCreateDto::getTask_id), "task-123")
                .create();
        final var statusResponse = Instancio.of(TwoGisMatrixAsyncCheckStatusDto.class)
                .set(field(TwoGisMatrixAsyncCheckStatusDto::status), taskStatusEnum.name())
                .set(field(TwoGisMatrixAsyncCheckStatusDto::resultLink), "https://example.com/result")
                .create();
        final var body = createSowaResponseBody();
        final var gisResult = ResponseEntity.ok()
                .body(body);
        final var gisResultMapped = createTwoGisMatrixResponseDto();
        final var gisAsyncProperties = Instancio.create(GisProviderProperties.class);
        final var gisAsyncResultProperties = Instancio.create(GisProviderProperties.class);
        doReturn(gisAsyncProperties).when(gisProvidersProperties).getTwogisasync();
        doReturn(gisAsyncResultProperties).when(gisProvidersProperties).getTwogisasyncresult();
        doReturn(Instancio.create(String.class)).when(gisDataProperties).getSowaUrl();
        doReturn(10000).when(gisDataProperties).getTimeout();
        doReturn(createResponse).when(gisAsyncFeignClient).getDistMatrix(anyString(), anyString(), any());
        doReturn(statusResponse).when(gisAsyncResultFeignClient).getDistMatrix(eq("task-123"), anyString());
        doReturn(gisResult).when(sowaFeignClient).getDistMatrix(any());
        doReturn(gisResultMapped).when(objectMapper).readValue(gisResult.getBody(), TwoGisMatrixResponseDto.class);
        final var expected = new DistanceMatrixResponseDTO();
        expected.setOrigins(IntStream.range(0, 3)
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
        final var actual = geoService.getDistanceMatrixAsync(waypoints, TransportTypeEnum.TAXI);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        verify(gisAsyncFeignClient, times(1)).getDistMatrix(anyString(), anyString(), any());
        verify(gisAsyncResultFeignClient, times(1)).getDistMatrix(eq("task-123"), anyString());
        verify(sowaFeignClient, times(1)).getDistMatrix(any());
    }

    @NotNull
    private static String createSowaResponseBody() {
        return """
                {
                  "generation_time": null,
                  "routes": [
                    {
                      "distance": 100,
                      "duration": 60,
                      "source_id": 0,
                      "status": "OK",
                      "target_id": 0
                    },
                    {
                      "distance": 100,
                      "duration": 60,
                      "source_id": 0,
                      "status": "OK",
                      "target_id": 1
                    },
                    {
                      "distance": 100,
                      "duration": 60,
                      "source_id": 0,
                      "status": "OK",
                      "target_id": 2
                    },
                    {
                      "distance": 100,
                      "duration": 60,
                      "source_id": 1,
                      "status": "OK",
                      "target_id": 0
                    },
                    {
                      "distance": 100,
                      "duration": 60,
                      "source_id": 1,
                      "status": "OK",
                      "target_id": 1
                    },
                    {
                      "distance": 100,
                      "duration": 60,
                      "source_id": 1,
                      "status": "OK",
                      "target_id": 2
                    },
                    {
                      "distance": 100,
                      "duration": 60,
                      "source_id": 2,
                      "status": "OK",
                      "target_id": 0
                    },
                    {
                      "distance": 100,
                      "duration": 60,
                      "source_id": 2,
                      "status": "OK",
                      "target_id": 1
                    },
                    {
                      "distance": 100,
                      "duration": 60,
                      "source_id": 2,
                      "status": "OK",
                      "target_id": 2
                    }
                  ]
                }
                """;
    }

    private static TwoGisMatrixResponseDto createTwoGisMatrixResponseDto() {
        return Instancio.of(TwoGisMatrixResponseDto.class)
                .set(field(TwoGisMatrixResponseDto::getRoutes), List.of(
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), 100)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), 60)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 0)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "OK")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 0)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), 100)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), 60)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 0)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "OK")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 1)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), 100)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), 60)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 0)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "OK")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 2)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), 100)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), 60)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 1)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "OK")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 0)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), 100)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), 60)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 1)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "OK")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 1)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), 100)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), 60)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 1)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "OK")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 2)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), 100)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), 60)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 2)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "OK")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 0)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), 100)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), 60)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 2)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "OK")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 1)
                                .create(),
                        Instancio.of(TwoGisMatrixResponseDto.Route.class)
                                .set(field(TwoGisMatrixResponseDto.Route::distance), 100)
                                .set(field(TwoGisMatrixResponseDto.Route::duration), 60)
                                .set(field(TwoGisMatrixResponseDto.Route::source_id), 2)
                                .set(field(TwoGisMatrixResponseDto.Route::status), "OK")
                                .set(field(TwoGisMatrixResponseDto.Route::target_id), 2)
                                .create()
                ))
                .create();
    }
}