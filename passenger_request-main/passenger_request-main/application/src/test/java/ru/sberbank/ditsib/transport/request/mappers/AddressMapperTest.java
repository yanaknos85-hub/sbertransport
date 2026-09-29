package ru.sberbank.ditsib.transport.request.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.database.model.Address;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка маппера адресов")
class AddressMapperTest {
    
    private final AddressMapper mapper = new AddressMapperImpl();
    
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
        address.setId(UUID.randomUUID());
        
        var actual = mapper.toMessage(address);
        
        assertThat(actual.getBuilding()).isEqualTo(address.getBuilding());
        assertThat(actual.getId()).isEqualTo(address.getId());
        assertThat(actual.getCity()).isEqualTo(address.getCity());
        assertThat(actual.getCountry()).isEqualTo(address.getCountry());
        assertThat(actual.getHouse()).isEqualTo(address.getHouse());
        assertThat(actual.getLatitude()).isEqualTo(address.getLatitude());
        assertThat(actual.getLongitude()).isEqualTo(address.getLongitude());
        assertThat(actual.getRegion()).isEqualTo(address.getRegion());
        assertThat(actual.getStreet()).isEqualTo(address.getStreet());
        assertThat(actual.getStructure()).isEqualTo(address.getStructure());
    }
    
}