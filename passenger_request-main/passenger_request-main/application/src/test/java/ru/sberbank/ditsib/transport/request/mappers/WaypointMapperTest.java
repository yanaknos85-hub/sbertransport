package ru.sberbank.ditsib.transport.request.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.database.model.Address;
import ru.sberbank.ditsib.transport.request.database.model.Waypoint;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка маппера путевых точек")
class WaypointMapperTest {
    
    private final WaypointsMapper mapper = new WaypointsMapperImpl(new AddressMapperImpl());
    
    @Test
    @DisplayName("Модель в сообщение")
    void test_toMessage() {
        var address = new Address();
        address.setBuilding("Building");
        address.setCity("City");
        address.setCountry("Country");
        address.setHouse("House");
        address.setLatitude(0.5);
        address.setLongitude(1.5);
        address.setRegion("Region");
        address.setStreet("Street");
        address.setStructure("Structure");
        
        var expectedData = new Waypoint();
        expectedData.setAddress(address);
        expectedData.setWaitTime(Duration.ofHours(1));
        
        var actual = mapper.toMessage(expectedData);
        
        assertThat(actual.getWaitTime()).isEqualTo(expectedData.getWaitTime());
        assertThat(actual.getAddress().getBuilding()).isEqualTo(address.getBuilding());
        assertThat(actual.getAddress().getCity()).isEqualTo(address.getCity());
        assertThat(actual.getAddress().getCountry()).isEqualTo(address.getCountry());
        assertThat(actual.getAddress().getHouse()).isEqualTo(address.getHouse());
        assertThat(actual.getAddress().getLatitude()).isEqualTo(address.getLatitude());
        assertThat(actual.getAddress().getLongitude()).isEqualTo(address.getLongitude());
        assertThat(actual.getAddress().getRegion()).isEqualTo(address.getRegion());
        assertThat(actual.getAddress().getStreet()).isEqualTo(address.getStreet());
        assertThat(actual.getAddress().getStructure()).isEqualTo(address.getStructure());
    }
    
}