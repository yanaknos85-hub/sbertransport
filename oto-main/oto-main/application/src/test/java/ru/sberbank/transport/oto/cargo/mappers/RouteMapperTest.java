package ru.sberbank.transport.oto.cargo.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.transport.oto.cargo.utils.TestUtils;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@Isolated
@Feature("app_passenger_oto")
class RouteMapperTest {
    
    private final RouteMapper mapper = new RouteMapperImpl();
    
    @Test
    void messageToEntityTest() {
        var message = TestUtils.buildRouteMessage();
        
        var entity = mapper.messageToEntity(message);
        
        assertAll(
                () -> assertEquals(message.id(), entity.getId()),
                () -> assertEquals(message.humanReadableId(), entity.getHumanReadableId()),
                () -> assertEquals(message.tariffId(), entity.getTariffId()),
                () -> assertEquals(message.cost(), entity.getCost()),
                () -> assertEquals(message.distance(), entity.getDistance()),
                () -> assertEquals(message.contractor().id(), entity.getContractor().getId())
                 );
    }

}