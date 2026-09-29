package ru.sber.transport.address.web.controller.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.address.business.use_cases.Addresses;
import ru.sber.transport.address.web.mapper.FrequentlyAddressWebMapperImpl;
import ru.sber.transport.web.api.SelfAddressesFrequentlyApiDelegate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка контроллера частых адресов пользователя")
class EnteredEmployeeFrequentlyAddressesControllerImplTest {

    @SuppressWarnings("unchecked")
    private final Addresses<FrequentlyAddress> addresses = mock(Addresses.class);

    private final SelfAddressesFrequentlyApiDelegate delegate = new EnteredEmployeeFrequentlyAddressesControllerImpl(addresses, new FrequentlyAddressWebMapperImpl());

    @Test
    @DisplayName("Получение всех")
    void test_getAll() {
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(userId.toString()).build()));
        var expectedList = Instancio.createList(FrequentlyAddress.class);

        when(addresses.get(userId)).thenReturn(expectedList);

        var response = delegate.getAllFrequently();

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSameSizeAs(expectedList);

        for (var i = 0; i < response.getBody().size(); i++) {
            var actual = response.getBody().get(i);
            var expected = expectedList.get(i);

            assertThat(actual.getBuilding()).isEqualTo(expected.getBuilding());
            assertThat(actual.getCity()).isEqualTo(expected.getCity());
            assertThat(actual.isFirst()).isEqualTo(expected.isFirst());
            assertThat(actual.getUsages()).isEqualTo(expected.getCount());
            assertThat(actual.getCountry()).isEqualTo(expected.getCountry());
            assertThat(actual.getHouse()).isEqualTo(expected.getHouse());
            assertThat(actual.getLatitude()).isEqualTo(expected.getLatitude());
            assertThat(actual.getLongitude()).isEqualTo(expected.getLongitude());
            assertThat(actual.getRegion()).isEqualTo(expected.getRegion());
            assertThat(actual.getStreet()).isEqualTo(expected.getStreet());
            assertThat(actual.getStructure()).isEqualTo(expected.getStructure());
        }
    }

    @Test
    @DisplayName("Удаление")
    void test_deleteFrequently() {
        var addressId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(userId.toString()).build()));

        delegate.deleteFrequently(addressId);

        verify(addresses).delete(userId, addressId);
    }

}