package ru.sber.transport.dispatcher.grpc.impl;

import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.database.dao.ContractorRepository;
import ru.sber.transport.dispatcher.grpc.dto.GetOrganizationsForPenaltiesRequest;
import ru.sber.transport.dispatcher.grpc.dto.GetOrganizationsForPenaltiesResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_dispatcher")
@DisplayName("Проверка gRPC метода getOrganizationsForPenalties")
class DispatcherPenaltyGrpcServiceImplTest {

    @Test
    @DisplayName("Успешное получение TIN")
    void getOrganizationsForPenalties_success() {
        var mockRepository = mock(ContractorRepository.class);
        when(mockRepository.findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue())
                .thenReturn(List.of("7707083893", "7701234567", "7709876543"));

        var service = new DispatcherPenaltyGrpcServiceImpl(mockRepository);
        var request = GetOrganizationsForPenaltiesRequest.newBuilder().build();

        var capturedResponse = new Object[1];
        var responseObserver = new StreamObserver<GetOrganizationsForPenaltiesResponse>() {
            @Override
            public void onNext(GetOrganizationsForPenaltiesResponse value) {
                capturedResponse[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error", t);
            }

            @Override
            public void onCompleted() {
            }
        };

        service.getOrganizationsForPenalties(request, responseObserver);

        assertThat(capturedResponse[0]).isNotNull();
        var response = (GetOrganizationsForPenaltiesResponse) capturedResponse[0];
        assertThat(response.getTinsList()).hasSize(3);
        assertThat(response.getTinsList()).containsExactlyElementsOf(List.of("7707083893", "7701234567", "7709876543"));
    }

    @Test
    @DisplayName("Пустой список TIN")
    void getOrganizationsForPenalties_empty() {
        var mockRepository = mock(ContractorRepository.class);
        when(mockRepository.findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue())
                .thenReturn(List.of());

        var service = new DispatcherPenaltyGrpcServiceImpl(mockRepository);
        var request = GetOrganizationsForPenaltiesRequest.newBuilder().build();

        var capturedResponse = new Object[1];
        var responseObserver = new StreamObserver<GetOrganizationsForPenaltiesResponse>() {
            @Override
            public void onNext(GetOrganizationsForPenaltiesResponse value) {
                capturedResponse[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error", t);
            }

            @Override
            public void onCompleted() {
            }
        };

        service.getOrganizationsForPenalties(request, responseObserver);

        assertThat(capturedResponse[0]).isNotNull();
        var response = (GetOrganizationsForPenaltiesResponse) capturedResponse[0];
        assertThat(response.getTinsList()).isEmpty();
    }
}
