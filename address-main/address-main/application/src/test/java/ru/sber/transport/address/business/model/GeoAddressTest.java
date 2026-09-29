package ru.sber.transport.address.business.model;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка логики работы адресов")
class GeoAddressTest {

    @Test
    @DisplayName("Проверка равенства")
    void test_equals() {
        var address1 = Instancio.create(GeoAddress.class);
        var address2 = Instancio.of(FrequentlyAddress.class)
            .set(Select.field(FrequentlyAddress::getLatitude), address1.getLatitude())
            .set(Select.field(FrequentlyAddress::getLongitude), address1.getLongitude())
            .create();
        var address3 = Instancio.of(FavoriteAddress.class)
            .set(Select.field(FavoriteAddress::getRegion), address1.getRegion())
            .set(Select.field(FavoriteAddress::getCity), address1.getCity())
            .set(Select.field(FavoriteAddress::getStreet), address1.getStreet())
            .set(Select.field(FavoriteAddress::getHouse), address1.getHouse())
            .set(Select.field(FavoriteAddress::getBuilding), address1.getBuilding())
            .set(Select.field(FavoriteAddress::getStructure), address1.getStructure())
            .create();
        var address4 = Instancio.create(MeetingAddress.class);
        var address5 = Instancio.create(BigDecimal.class);

        assertThat(address1).isEqualTo(address2).isEqualTo(address3).isNotEqualTo(address4).isNotEqualTo(address5);
    }

}