package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.services.Processor;
import ru.sber.transport.notifications.services.TripApproveService;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@DisplayName("Проверка процессора заявок на поездку")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class TripRequestProcessorImplTest {
    
    private final TripApproveService approveService = mock(TripApproveService.class);
    
    private final Processor<TripApprove> approveProcessor = mock(Processor.class);
    
    private final Processor<TripRequest> processor = new TripRequestProcessorImpl(approveService, approveProcessor);

    @Test
    @DisplayName("Проверка запуска процессинга")
    void test_process() throws JsonProcessingException {
        var id = UUID.randomUUID();
    
        var request = new TripRequest();
        request.setId(id);
        request.setPassengerId(UUID.randomUUID());
        request.setTransportType(TransportTypeEnum.CARSHARING);
        request.setPurposeId(UUID.randomUUID());
        request.setAuthorId(UUID.randomUUID());
        
        var approve = new TripApprove();
        approve.setRequestId(request.getId());
        approve.setApproverId(UUID.randomUUID());
        approve.setStatus(true);
        
        when(approveService.getByRequestId(id)).thenReturn(Optional.of(approve));
        
        processor.process(request);
        
        verify(approveProcessor).process(approve);
    }

    @Test
    @DisplayName("Проверка запуска процессинга без согласования")
    void test_process_noApproval() throws JsonProcessingException {
        var id = UUID.randomUUID();
    
        var request = new TripRequest();
        request.setId(id);
        request.setPassengerId(UUID.randomUUID());
        request.setTransportType(TransportTypeEnum.CARSHARING);
        request.setPurposeId(UUID.randomUUID());
        request.setAuthorId(UUID.randomUUID());
        
        when(approveService.getByRequestId(id)).thenReturn(Optional.empty());
        
        processor.process(request);
        
        verify(approveProcessor, never()).process(any(TripApprove.class));
    }
}