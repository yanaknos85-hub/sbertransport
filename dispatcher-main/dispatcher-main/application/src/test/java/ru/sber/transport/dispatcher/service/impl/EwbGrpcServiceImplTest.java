package ru.sber.transport.dispatcher.service.impl;


import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.dto.ShiftForEwbDto;
import ru.sber.transport.ewb.grpc.dto.Dto;
import ru.sber.transport.ewb.grpc.service.EwbServiceGrpc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@Slf4j
@DisplayName("Проверка отправки данных по grpc")
public class EwbGrpcServiceImplTest {

    @Rule
    public final GrpcCleanupRule grpcCleanupRule = new GrpcCleanupRule();

    @Test
    @SneakyThrows
    @DisplayName("Успешная отправка ")
    void testSuccessSend() {
        // given
        var serverName = InProcessServerBuilder.generateName();
        var request = List.of(Instancio.of(ShiftForEwbDto.class).create());


        grpcCleanupRule.register(InProcessServerBuilder.forName(serverName).directExecutor()
                .addService(createEwb(null)).build().start());

        var channel = grpcCleanupRule.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        var stub = EwbServiceGrpc.newBlockingStub(channel);

        var client = new EwbGrpcServiceImpl();
        ReflectionTestUtils.setField(client, "stub", stub);

        assertDoesNotThrow(() -> client.send(request));
    }

    private EwbServiceGrpc.EwbServiceImplBase createEwb(StatusRuntimeException exception) {
        return mock(EwbServiceGrpc.EwbServiceImplBase.class, delegatesTo(
                new EwbServiceGrpc.EwbServiceImplBase() {
                    @Override
                    public void generateFirstTitle(Dto.FirstTitleRequestList request, StreamObserver<Dto.FirstTitleResponseList> responseObserver) {
                        if (exception != null) {
                            responseObserver.onError(exception);
                            return;
                        }
                        var responseList = Dto.FirstTitleResponseList.newBuilder();
                        var response = Dto.FirstTitleResponse.newBuilder();
                        response.setHumanReadableId("ROUTE-001");
                        response.setFileName("EWB_ROUTE-001.pdf");
                        response.setContent(ByteString.copyFrom(new byte[0]));
                        response.setCreationTime(Timestamp.newBuilder().setNanos(0).setSeconds(0).build());
                        response.setErrorText("");
                        response.setId(UUID.randomUUID().toString());
                        response.setEwbId(UUID.randomUUID().toString());
                        responseList.addResponses(response);
                        responseObserver.onNext(responseList.build());
                        responseObserver.onCompleted();
                    }
                }
        ));
    }

}
