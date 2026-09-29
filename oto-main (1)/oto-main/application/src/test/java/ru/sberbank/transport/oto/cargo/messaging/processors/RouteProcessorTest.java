package ru.sberbank.transport.oto.cargo.messaging.processors;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.transport.oto.cargo.mappers.RouteMapper;
import ru.sberbank.transport.oto.cargo.service.RouteService;
import ru.sberbank.transport.oto.cargo.utils.TestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@UnitTest
@Isolated
@Feature("app_passenger_oto")
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class RouteProcessorTest {
    
    @InjectMocks
    private RouteProcessor routeProcessor;
    
    @Mock
    private RouteService routeService;
    
    @Spy
    @SuppressWarnings("unused")
    private RouteMapper routeMapper = Mappers.getMapper(RouteMapper.class);
    
    @Test
    void testProcess() {
        // given
        var message = TestUtils.buildRouteMessage();
        
        // when
        routeProcessor.handle(message);
        
        // then
        verify(routeService, times(1))
                .save(any());
    }
    
}