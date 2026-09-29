package ru.sber.transport.telemechanic.service.grpc.impl;

import ch.qos.logback.classic.Level;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.ewb.grpc.dto.Dto;
import ru.sber.transport.telemechanic.LoggingExtension;
import ru.sber.transport.telemechanic.config.properties.EwbGrpcFirstTitleProperties;
import ru.sber.transport.telemechanic.database.model.Driver;
import ru.sber.transport.telemechanic.dto.ewb.FirstTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.UuidDto;
import ru.sber.transport.telemechanic.dto.ewb.first_title.FirstTitleRequest;
import ru.sber.transport.telemechanic.service.DriverService;
import ru.sber.transport.telemechanic.service.EwbService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EwbGrpcServiceImplTest {

    @InjectMocks
    private EwbGrpcServiceImpl ewbGrpcService;
    @Mock
    private EwbService ewbService;
    @Mock
    private DriverService driverService;
    @Mock
    private EwbGrpcFirstTitleProperties ewbGrpcFirstTitleProperties;
    @Mock
    private StreamObserver<Dto.FirstTitleResponseList> responseListStreamObserver;
    @Mock
    private StreamObserver<BoolValue> boolValueStreamObserver;
    @Mock
    private Clock clock;
    private final Clock fixedClock = Clock.fixed(
            LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC
    );
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(EwbGrpcServiceImpl.class);

    @Test
    void haveActiveEwb_success() {
        var departmentId = UUID.randomUUID();
        var checkStartDate = Timestamp.newBuilder()
                .setSeconds(fixedClock.instant().getEpochSecond())
                .setNanos(0)
                .build();
        var request = Dto.HaveActiveEwbDto.newBuilder()
                .addDepartmentIds(departmentId.toString())
                .setCheckStartDate(checkStartDate)
                .build();

        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(true).when(ewbService).haveActiveEwb(anyList(), any(LocalDate.class));
        doNothing().when(boolValueStreamObserver).onNext(any(BoolValue.class));
        doNothing().when(boolValueStreamObserver).onCompleted();

        ewbGrpcService.haveActiveEwb(request, boolValueStreamObserver);

        var captor = ArgumentCaptor.forClass(BoolValue.class);
        verify(boolValueStreamObserver).onNext(captor.capture());
        verify(boolValueStreamObserver).onCompleted();

        assertThat(captor.getValue().getValue()).isTrue();
    }

    @Test
    void haveActiveEwb_error() {
        var departmentId = UUID.randomUUID();
        var checkStartDate = Timestamp.newBuilder()
                .setSeconds(fixedClock.instant().getEpochSecond())
                .setNanos(0)
                .build();
        var request = Dto.HaveActiveEwbDto.newBuilder()
                .addDepartmentIds(departmentId.toString())
                .setCheckStartDate(checkStartDate)
                .build();

        doReturn(fixedClock.getZone()).when(clock).getZone();
        doThrow(new RuntimeException("Test error")).when(ewbService).haveActiveEwb(anyList(), any(LocalDate.class));
        doNothing().when(boolValueStreamObserver).onError(any(RuntimeException.class));

        ewbGrpcService.haveActiveEwb(request, boolValueStreamObserver);

        verify(boolValueStreamObserver).onError(any(RuntimeException.class));
    }

    @Test
    void generateFirstTitle_success_singleRequest() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var startDate = LocalDate.now(fixedClock);
        var finishDate = LocalDate.now(fixedClock);
        var transportationType = "СН";
        var communicationType = "Г";
        var tariffDepartmentId = UUID.randomUUID();
        var transportId = UUID.randomUUID();
        var driverId = UUID.randomUUID();

        var firstTitleRequest = Dto.FirstTitleRequest.newBuilder()
                .setId(id.toString())
                .setDispatcherId(userId.toString())
                .setStartDate(startDate.toString())
                .setFinishDate(finishDate.toString())
                .setTransportationType(transportationType)
                .setCommunicationType(communicationType)
                .setTariffDepartmentId(tariffDepartmentId.toString())
                .setTransportId(transportId.toString())
                .setDriverEmployeeId(driverId.toString())
                .build();

        var ewbUuid = UUID.randomUUID();
        var driver = new Driver();
        driver.setId(UUID.randomUUID());

        var firstTitleResponseDto = new FirstTitleResponse(
                "PL-0001-00000001",
                "filename.xml",
                "content".getBytes(),
                LocalDateTime.now(fixedClock)
        );
        
        var expectedResponse = Dto.FirstTitleResponse.newBuilder()
                                                     .setId(id.toString())
                                                     .setEwbId(ewbUuid.toString())
                                                     .setHumanReadableId(firstTitleResponseDto.humanReadableId())
                                                     .setFileName(firstTitleResponseDto.fileName())
                                                     .setContent(ByteString.copyFrom(firstTitleResponseDto.content()))
                                                     .setCreationTime(Timestamp.newBuilder()
                                                                               .setSeconds(firstTitleResponseDto.creationTime().toInstant(ZoneOffset.UTC).getEpochSecond())
                                                                               .setNanos(firstTitleResponseDto.creationTime().toInstant(ZoneOffset.UTC).getNano())
                                                                               .build())
                                                     .build();

        var expectedResponseList = Dto.FirstTitleResponseList.newBuilder()
                .addResponses(expectedResponse)
                .build();

        doReturn(new UuidDto(ewbUuid)).when(ewbService).getUUID();
        doReturn(driver).when(driverService).getByEmployeeId(driverId);
        doReturn(firstTitleResponseDto).when(ewbService).generateFirstTitle(any(FirstTitleRequest.class), eq(userId));
        doReturn(100).when(ewbGrpcFirstTitleProperties).maxRequestSize();

        var responseCaptor = ArgumentCaptor.forClass(Dto.FirstTitleResponseList.class);
        ewbGrpcService.generateFirstTitle(
                Dto.FirstTitleRequestList.newBuilder().addRequests(firstTitleRequest).build(),
                responseListStreamObserver
        );

        verify(responseListStreamObserver).onNext(responseCaptor.capture());
        verify(responseListStreamObserver).onCompleted();

        assertThat(responseCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseList);
    }

    @Test
    void generateFirstTitle_success_multipleRequests() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var startDate = LocalDate.now(fixedClock);
        var finishDate = LocalDate.now(fixedClock);
        var transportationType = "СН";
        var communicationType = "Г";
        var uuidDto = new UuidDto(UUID.randomUUID());

        var driverId1 = UUID.randomUUID();
        var driverId2 = UUID.randomUUID();
        
        var firstTitleRequest1 = Dto.FirstTitleRequest.newBuilder()
                                                      .setId(id1.toString())
                                                      .setDispatcherId(userId.toString())
                                                      .setStartDate(startDate.toString())
                                                      .setFinishDate(finishDate.toString())
                                                      .setTransportationType(transportationType)
                                                      .setCommunicationType(communicationType)
                                                      .setTariffDepartmentId(UUID.randomUUID().toString())
                                                      .setTransportId(UUID.randomUUID().toString())
                                                      .setDriverEmployeeId(driverId1.toString())
                                                      .build();

        var firstTitleRequest2 = Dto.FirstTitleRequest.newBuilder()
                .setId(id2.toString())
                .setDispatcherId(userId.toString())
                .setStartDate(startDate.toString())
                .setFinishDate(finishDate.toString())
                .setTransportationType(transportationType)
                .setCommunicationType(communicationType)
                .setTariffDepartmentId(UUID.randomUUID().toString())
                .setTransportId(UUID.randomUUID().toString())
                .setDriverEmployeeId(driverId2.toString())
                .build();

        var driver1 = new Driver();
        driver1.setId(UUID.randomUUID());

        var driver2 = new Driver();
        driver2.setId(UUID.randomUUID());

        var firstTitleResponse1 = new FirstTitleResponse(
                "PL-0001-00000001",
                "filename1.xml",
                "content1".getBytes(),
                LocalDateTime.now(fixedClock)
        );

        var firstTitleResponse2 = new FirstTitleResponse(
                "PL-0001-00000002",
                "filename2.xml",
                "content2".getBytes(),
                LocalDateTime.now(fixedClock)
        );
        
        var expectedResponse1 = Dto.FirstTitleResponse.newBuilder()
                                                      .setId(id1.toString())
                                                      .setEwbId(uuidDto.uuid().toString())
                                                      .setHumanReadableId(firstTitleResponse1.humanReadableId())
                                                      .setFileName(firstTitleResponse1.fileName())
                                                      .setContent(ByteString.copyFrom(firstTitleResponse1.content()))
                                                      .setCreationTime(Timestamp.newBuilder()
                                                                                .setSeconds(firstTitleResponse1.creationTime().toInstant(ZoneOffset.UTC).getEpochSecond())
                                                                                .setNanos(firstTitleResponse1.creationTime().toInstant(ZoneOffset.UTC).getNano())
                                                                                .build())
                                                      .build();
        
        var expectedResponse2 = Dto.FirstTitleResponse.newBuilder()
                                                      .setId(id2.toString())
                                                      .setEwbId(uuidDto.uuid().toString())
                                                      .setHumanReadableId(firstTitleResponse2.humanReadableId())
                                                      .setFileName(firstTitleResponse2.fileName())
                                                      .setContent(ByteString.copyFrom(firstTitleResponse2.content()))
                                                      .setCreationTime(Timestamp.newBuilder()
                                                                                .setSeconds(firstTitleResponse2.creationTime().toInstant(ZoneOffset.UTC).getEpochSecond())
                                                                                .setNanos(firstTitleResponse2.creationTime().toInstant(ZoneOffset.UTC).getNano())
                                                                                .build())
                                                      .build();

        var expectedResponseList = Dto.FirstTitleResponseList.newBuilder()
                .addResponses(expectedResponse1)
                .addResponses(expectedResponse2)
                .build();

        doReturn(uuidDto).when(ewbService).getUUID();
        doReturn(driver1).when(driverService).getByEmployeeId(driverId1);
        doReturn(driver2).when(driverService).getByEmployeeId(driverId2);
        doReturn(firstTitleResponse1, firstTitleResponse2).when(ewbService).generateFirstTitle(any(FirstTitleRequest.class), eq(userId));
        doReturn(100).when(ewbGrpcFirstTitleProperties).maxRequestSize();

        var responseCaptor = ArgumentCaptor.forClass(Dto.FirstTitleResponseList.class);
        ewbGrpcService.generateFirstTitle(
                Dto.FirstTitleRequestList.newBuilder().addRequests(firstTitleRequest1).addRequests(firstTitleRequest2).build(),
                responseListStreamObserver
        );

        verify(responseListStreamObserver).onNext(responseCaptor.capture());
        verify(responseListStreamObserver).onCompleted();

        assertThat(responseCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseList);
    }

    @Test
    void generateFirstTitle_error_singleRequest() {
        var userId = UUID.randomUUID();
        var startDate = LocalDate.now(fixedClock);
        var finishDate = LocalDate.now(fixedClock);
        var transportationType = "СН";
        var communicationType = "Г";
        var tariffDepartmentId = UUID.randomUUID();
        var transportId = UUID.randomUUID();
        var driverId = UUID.randomUUID();

        var firstTitleRequest = Dto.FirstTitleRequest.newBuilder()
                .setDispatcherId(userId.toString())
                .setStartDate(startDate.toString())
                .setFinishDate(finishDate.toString())
                .setTransportationType(transportationType)
                .setCommunicationType(communicationType)
                .setTariffDepartmentId(tariffDepartmentId.toString())
                .setTransportId(transportId.toString())
                .setDriverEmployeeId(driverId.toString())
                .build();

        var driver = new Driver();
        driver.setId(UUID.randomUUID());

        var expectedErrorResponse = Dto.FirstTitleResponse.newBuilder()
                .setErrorText("Error occurred")
                .build();

        var expectedResponseList = Dto.FirstTitleResponseList.newBuilder()
                .addResponses(expectedErrorResponse)
                .build();

        doReturn(new UuidDto(UUID.randomUUID())).when(ewbService).getUUID();
        doReturn(driver).when(driverService).getByEmployeeId(driverId);
        doThrow(new RuntimeException("Error occurred")).when(ewbService).generateFirstTitle(any(FirstTitleRequest.class), eq(userId));
        doReturn(100).when(ewbGrpcFirstTitleProperties).maxRequestSize();

        var responseCaptor = ArgumentCaptor.forClass(Dto.FirstTitleResponseList.class);
        ewbGrpcService.generateFirstTitle(
                Dto.FirstTitleRequestList.newBuilder().addRequests(firstTitleRequest).build(),
                responseListStreamObserver
        );

        verify(responseListStreamObserver).onNext(responseCaptor.capture());
        verify(responseListStreamObserver).onCompleted();

        assertThat(responseCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseList);
    }

    @Test
    void generateFirstTitle_error_duringProcessing() {
        var userId = UUID.randomUUID();
        var startDate = LocalDate.now(fixedClock);
        var finishDate = LocalDate.now(fixedClock);
        var transportationType = "СН";
        var communicationType = "Г";
        var tariffDepartmentId = UUID.randomUUID();
        var transportId = UUID.randomUUID();
        var driverId = UUID.randomUUID();

        var firstTitleRequest = Dto.FirstTitleRequest.newBuilder()
                .setDispatcherId(userId.toString())
                .setStartDate(startDate.toString())
                .setFinishDate(finishDate.toString())
                .setTransportationType(transportationType)
                .setCommunicationType(communicationType)
                .setTariffDepartmentId(tariffDepartmentId.toString())
                .setTransportId(transportId.toString())
                .setDriverEmployeeId(driverId.toString())
                .build();

        var expectedErrorResponse = Dto.FirstTitleResponse.newBuilder()
                .setErrorText("Error occurred")
                .build();

        var expectedResponseList = Dto.FirstTitleResponseList.newBuilder()
                .addResponses(expectedErrorResponse)
                .build();

        doReturn(new UuidDto(UUID.randomUUID())).when(ewbService).getUUID();
        doThrow(new RuntimeException("Error occurred")).when(driverService).getByEmployeeId(driverId);
        doReturn(100).when(ewbGrpcFirstTitleProperties).maxRequestSize();

        var responseCaptor = ArgumentCaptor.forClass(Dto.FirstTitleResponseList.class);
        ewbGrpcService.generateFirstTitle(
                Dto.FirstTitleRequestList.newBuilder().addRequests(firstTitleRequest).build(),
                responseListStreamObserver
        );

        verify(responseListStreamObserver).onNext(responseCaptor.capture());
        verify(responseListStreamObserver).onCompleted();

        assertThat(responseCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseList);
    }
    
    @Test
    void generateFirstTitle_limit_multipleRequests() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var startDate = LocalDate.now(fixedClock);
        var finishDate = LocalDate.now(fixedClock);
        var transportationType = "СН";
        var communicationType = "Г";
        
        var driverId1 = UUID.randomUUID();
        var driverId2 = UUID.randomUUID();
        
        var firstTitleRequest1 = Dto.FirstTitleRequest.newBuilder()
                                                      .setId(id1.toString())
                                                      .setDispatcherId(userId.toString())
                                                      .setStartDate(startDate.toString())
                                                      .setFinishDate(finishDate.toString())
                                                      .setTransportationType(transportationType)
                                                      .setCommunicationType(communicationType)
                                                      .setTariffDepartmentId(UUID.randomUUID().toString())
                                                      .setTransportId(UUID.randomUUID().toString())
                                                      .setDriverEmployeeId(driverId1.toString())
                                                      .build();
        
        var firstTitleRequest2 = Dto.FirstTitleRequest.newBuilder()
                                                      .setId(id2.toString())
                                                      .setDispatcherId(userId.toString())
                                                      .setStartDate(startDate.toString())
                                                      .setFinishDate(finishDate.toString())
                                                      .setTransportationType(transportationType)
                                                      .setCommunicationType(communicationType)
                                                      .setTariffDepartmentId(UUID.randomUUID().toString())
                                                      .setTransportId(UUID.randomUUID().toString())
                                                      .setDriverEmployeeId(driverId2.toString())
                                                      .build();

        doReturn(1).when(ewbGrpcFirstTitleProperties).maxRequestSize();
        
        var responseCaptor = ArgumentCaptor.forClass(Dto.FirstTitleResponseList.class);
        ewbGrpcService.generateFirstTitle(
                Dto.FirstTitleRequestList.newBuilder().addRequests(firstTitleRequest1).addRequests(firstTitleRequest2).build(),
                responseListStreamObserver);
        
        verify(responseListStreamObserver, never()).onNext(responseCaptor.capture());
        verify(responseListStreamObserver, never()).onCompleted();
        
        LOGGING_EXTENSION.assertLogEvents(1, new Tuple[] {
                tuple(
                        Level.ERROR,
                        "Error in generateFirstTitle: Превышен лимит запросов: допустимо не более 1 запросов, получено 2",
                        false
                     )
        });
    }
}
