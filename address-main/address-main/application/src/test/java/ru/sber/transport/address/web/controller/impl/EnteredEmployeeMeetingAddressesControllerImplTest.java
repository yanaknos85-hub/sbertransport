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
import ru.sber.transport.address.business.model.MeetingAddress;
import ru.sber.transport.address.business.use_cases.Addresses;
import ru.sber.transport.address.web.mapper.MeetingAddressWebMapperImpl;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.web.api.SelfAddressesMeetingApiDelegate;
import ru.sber.transport.web.model.NewMeetingAddress;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка контроллера адресов встреч")
class EnteredEmployeeMeetingAddressesControllerImplTest {

    @SuppressWarnings("unchecked")
    private final Addresses<MeetingAddress> addresses = mock(Addresses.class);

    private final EmployeeOrganizationFunction function = mock(EmployeeOrganizationFunction.class);

    private final SelfAddressesMeetingApiDelegate delegate = new EnteredEmployeeMeetingAddressesControllerImpl(addresses, new MeetingAddressWebMapperImpl(), function);

    @Test
    @DisplayName("Получение всех")
    void test_getAll() {
        var userId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(userId.toString()).build()));
        var expectedList = Instancio.createList(MeetingAddress.class);

        when(addresses.get(organizationId)).thenReturn(expectedList);
        when(function.apply(userId)).thenReturn(organizationId);

        var response = delegate.getAllMeetings(organizationId);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSameSizeAs(expectedList);

        for (var i = 0; i < response.getBody().size(); i++) {
            var actual = response.getBody().get(i);
            var expected = expectedList.get(i);

            assertThat(actual.getAddress().getBuilding()).isEqualTo(expected.getBuilding());
            assertThat(actual.getAddress().getCity()).isEqualTo(expected.getCity());
            assertThat(actual.getLabel()).isEqualTo(expected.getLabel());
            assertThat(actual.getAddress().getCountry()).isEqualTo(expected.getCountry());
            assertThat(actual.getAddress().getHouse()).isEqualTo(expected.getHouse());
            assertThat(actual.getAddress().getLatitude()).isEqualTo(expected.getLatitude());
            assertThat(actual.getAddress().getLongitude()).isEqualTo(expected.getLongitude());
            assertThat(actual.getAddress().getRegion()).isEqualTo(expected.getRegion());
            assertThat(actual.getAddress().getStreet()).isEqualTo(expected.getStreet());
            assertThat(actual.getAddress().getStructure()).isEqualTo(expected.getStructure());
        }
    }

    @Test
    @DisplayName("Удаление")
    void test_deleteMeetings() {
        var addressId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        when(function.apply(userId)).thenReturn(organizationId);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(userId.toString()).build()));

        delegate.deleteMeeting(addressId);

        verify(addresses).delete(organizationId, addressId);
    }

    @SuppressWarnings("CatchMayIgnoreException")
    @Test
    @DisplayName("Удаление. Нет данных")
    void test_deleteMeetings_noData() {
        var addressId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(userId.toString()).build()));

        try {
            delegate.deleteMeeting(addressId);
        } catch (Exception e) {
            assertThat(e)
                .isInstanceOf(EntityNotFoundException.class)
                .hasFieldOrPropertyWithValue("entityId", Map.of("userId", userId))
                .hasFieldOrPropertyWithValue("entityName", "Employee");
        }
    }

    @Test
    @DisplayName("Сохранение нового адреса")
    void test_postMeeting() {
        var userId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(userId.toString()).build()));

        var expected = Instancio.create(NewMeetingAddress.class);
        var saved = Instancio.create(MeetingAddress.class);

        when(function.apply(userId)).thenReturn(organizationId);
        when(addresses.save(eq(organizationId), any(MeetingAddress.class))).thenReturn(saved);

        var result = delegate.postMeeting(expected);

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getAddress().getBuilding()).isEqualTo(saved.getBuilding());
        assertThat(result.getBody().getAddress().getCity()).isEqualTo(saved.getCity());
        assertThat(result.getBody().getAddress().getCountry()).isEqualTo(saved.getCountry());
        assertThat(result.getBody().getAddress().getHouse()).isEqualTo(saved.getHouse());
        assertThat(result.getBody().getLabel()).isEqualTo(saved.getLabel());
        assertThat(result.getBody().getAddress().getLatitude()).isEqualTo(saved.getLatitude());
        assertThat(result.getBody().getAddress().getLongitude()).isEqualTo(saved.getLongitude());
        assertThat(result.getBody().getAddress().getRegion()).isEqualTo(saved.getRegion());
        assertThat(result.getBody().getAddress().getStreet()).isEqualTo(saved.getStreet());
        assertThat(result.getBody().getAddress().getStructure()).isEqualTo(saved.getStructure());
    }

    @Test
    @DisplayName("Изменение адреса")
    void test_putMeeting() {
        var addressId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(userId.toString()).build()));

        var saved = Instancio.create(NewMeetingAddress.class);

        when(function.apply(userId)).thenReturn(organizationId);

        delegate.putMeeting(saved, addressId);

        var addressCaptor = ArgumentCaptor.forClass(MeetingAddress.class);

        verify(addresses).save(eq(organizationId), eq(addressId), addressCaptor.capture());

        assertThat(addressCaptor.getValue()).isNotNull();
        assertThat(addressCaptor.getValue().getBuilding()).isEqualTo(saved.getAddress().getBuilding());
        assertThat(addressCaptor.getValue().getCity()).isEqualTo(saved.getAddress().getCity());
        assertThat(addressCaptor.getValue().getCountry()).isEqualTo(saved.getAddress().getCountry());
        assertThat(addressCaptor.getValue().getHouse()).isEqualTo(saved.getAddress().getHouse());
        assertThat(addressCaptor.getValue().getLabel()).isEqualTo(saved.getLabel());
        assertThat(addressCaptor.getValue().getLatitude()).isEqualTo(saved.getAddress().getLatitude());
        assertThat(addressCaptor.getValue().getLongitude()).isEqualTo(saved.getAddress().getLongitude());
        assertThat(addressCaptor.getValue().getRegion()).isEqualTo(saved.getAddress().getRegion());
        assertThat(addressCaptor.getValue().getStreet()).isEqualTo(saved.getAddress().getStreet());
        assertThat(addressCaptor.getValue().getStructure()).isEqualTo(saved.getAddress().getStructure());
    }

}