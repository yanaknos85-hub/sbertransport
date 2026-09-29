package ru.sber.transport.address.web.controller.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.address.business.use_cases.Addresses;
import ru.sber.transport.address.web.mapper.FavoriteAddressWebMapperImpl;
import ru.sber.transport.web.api.SelfAddressesFavoriteApiDelegate;
import ru.sber.transport.web.model.NewFavoriteAddress;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка контроллера предпочтительных адресов пользователя")
class EnteredEmployeeFavoriteAddressesControllerImplTest {

    @SuppressWarnings("unchecked")
    private final Addresses<FavoriteAddress> addresses = mock(Addresses.class);

    private final SelfAddressesFavoriteApiDelegate delegate = new EnteredEmployeeFavoriteAddressesControllerImpl(addresses, new FavoriteAddressWebMapperImpl());

    @Test
    @DisplayName("Сохранение нового адреса")
    void test_postFavorite() {
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(userId.toString()).build()));

        var expected = Instancio.create(NewFavoriteAddress.class);
        var saved = Instancio.create(FavoriteAddress.class);

        when(addresses.save(eq(userId), any(FavoriteAddress.class))).thenReturn(saved);

        var result = delegate.postFavorite(expected);

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getUsages()).isNull();
        assertThat(result.getBody().isFirst()).isNull();
        assertThat(result.getBody().getBuilding()).isEqualTo(saved.getBuilding());
        assertThat(result.getBody().getCity()).isEqualTo(saved.getCity());
        assertThat(result.getBody().getCountry()).isEqualTo(saved.getCountry());
        assertThat(result.getBody().getHouse()).isEqualTo(saved.getHouse());
        assertThat(result.getBody().getLabel()).isEqualTo(saved.getLabel());
        assertThat(result.getBody().getLatitude()).isEqualTo(saved.getLatitude());
        assertThat(result.getBody().getLongitude()).isEqualTo(saved.getLongitude());
        assertThat(result.getBody().getRegion()).isEqualTo(saved.getRegion());
        assertThat(result.getBody().getStreet()).isEqualTo(saved.getStreet());
        assertThat(result.getBody().getStructure()).isEqualTo(saved.getStructure());
    }

    @Test
    @DisplayName("Изменение адреса")
    void test_putFavorite() {
        var addressId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(userId.toString()).build()));

        var saved = Instancio.create(NewFavoriteAddress.class);

        delegate.putFavorite(saved, addressId);

        var addressCaptor = ArgumentCaptor.forClass(FavoriteAddress.class);

        verify(addresses).save(eq(userId), eq(addressId), addressCaptor.capture());

        assertThat(addressCaptor.getValue()).isNotNull();
        assertThat(addressCaptor.getValue().getBuilding()).isEqualTo(saved.getBuilding());
        assertThat(addressCaptor.getValue().getCity()).isEqualTo(saved.getCity());
        assertThat(addressCaptor.getValue().getCountry()).isEqualTo(saved.getCountry());
        assertThat(addressCaptor.getValue().getHouse()).isEqualTo(saved.getHouse());
        assertThat(addressCaptor.getValue().getLabel()).isEqualTo(saved.getLabel());
        assertThat(addressCaptor.getValue().getLatitude()).isEqualTo(saved.getLatitude());
        assertThat(addressCaptor.getValue().getLongitude()).isEqualTo(saved.getLongitude());
        assertThat(addressCaptor.getValue().getRegion()).isEqualTo(saved.getRegion());
        assertThat(addressCaptor.getValue().getStreet()).isEqualTo(saved.getStreet());
        assertThat(addressCaptor.getValue().getStructure()).isEqualTo(saved.getStructure());
    }

    @Test
    @DisplayName("Получение всех")
    void test_getAll() {
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(userId.toString()).build()));
        var expectedList = Instancio.createList(FavoriteAddress.class);

        when(addresses.get(userId)).thenReturn(expectedList);

        var response = delegate.getAllFavorites();

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSameSizeAs(expectedList);

        for (var i = 0; i < response.getBody().size(); i++) {
            var actual = response.getBody().get(i);
            var expected = expectedList.get(i);

            assertThat(actual.getBuilding()).isEqualTo(expected.getBuilding());
            assertThat(actual.getCity()).isEqualTo(expected.getCity());
            assertThat(actual.getLabel()).isEqualTo(expected.getLabel());
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
    @DisplayName("Получение одного")
    void test_getFavorite() {
        var addressId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(userId.toString()).build()));
        var saved = Instancio.create(FavoriteAddress.class);

        when(addresses.get(userId, addressId)).thenReturn(Optional.of(saved));

        var result = delegate.getFavorite(addressId);

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getUsages()).isNull();
        assertThat(result.getBody().isFirst()).isNull();
        assertThat(result.getBody().getBuilding()).isEqualTo(saved.getBuilding());
        assertThat(result.getBody().getCity()).isEqualTo(saved.getCity());
        assertThat(result.getBody().getCountry()).isEqualTo(saved.getCountry());
        assertThat(result.getBody().getHouse()).isEqualTo(saved.getHouse());
        assertThat(result.getBody().getLabel()).isEqualTo(saved.getLabel());
        assertThat(result.getBody().getLatitude()).isEqualTo(saved.getLatitude());
        assertThat(result.getBody().getLongitude()).isEqualTo(saved.getLongitude());
        assertThat(result.getBody().getRegion()).isEqualTo(saved.getRegion());
        assertThat(result.getBody().getStreet()).isEqualTo(saved.getStreet());
        assertThat(result.getBody().getStructure()).isEqualTo(saved.getStructure());
    }

    @Test
    @DisplayName("Удаление")
    void test_deleteFavorite() {
        var addressId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(userId.toString()).build()));

        delegate.deleteFavorite(addressId);

        verify(addresses).delete(userId, addressId);
    }
}