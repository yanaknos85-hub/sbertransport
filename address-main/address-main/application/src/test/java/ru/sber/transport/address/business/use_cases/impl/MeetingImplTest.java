package ru.sber.transport.address.business.use_cases.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.model.GeoAddress;
import ru.sber.transport.address.business.model.MeetingAddress;
import ru.sber.transport.address.business.provider.MeetingAddressProvider;
import ru.sber.transport.address.business.use_cases.MeetingAddresses;
import ru.sber.transport.address.business.use_cases.mapper.MeetingMapper;
import ru.sber.transport.address.business.use_cases.mapper.MeetingMapperImpl;
import ru.sber.transport.address.messaging.sender.AddressSender;
import ru.sber.transport.address.providers.common_address.client.GeoClient;
import ru.sber.transport.exceptions.DuplicateDataException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка кейсов работы с адресами встреч")
class MeetingImplTest {

    private final MeetingAddressProvider provider = mock(MeetingAddressProvider.class);

    private final MeetingMapper mapper = new MeetingMapperImpl();

    private final AddressSender<MeetingAddress> sender = mock(AddressSender.class);

    private final GeoClient geoClient = mock(GeoClient.class);

    private final BaseAddressImpl<MeetingAddress> addresses = new MeetingsImpl(provider, geoClient, mapper, sender);

    @Test
    @DisplayName("Проверка сохранения нового")
    void test_save_new() {
        var source = Instancio.create(MeetingAddress.class);

        when(provider.exists(eq(source.getLabel()), any(UUID.class), any(UUID.class))).thenReturn(false);
        when(provider.save(any(MeetingAddress.class))).then(inv -> inv.getArgument(0));
        when(geoClient.getAddresses(any(String.class), eq(null), eq(null), eq(null), eq(null))).thenReturn(List.of(source));

        var saved = addresses.save(UUID.randomUUID(), source);

        verify(provider).save(saved);
        verify(sender).send(saved, false);
    }

    @Test
    @DisplayName("Проверка сохранения нового. Конфликт")
    void test_save_conflict() {
        var source = Instancio.create(MeetingAddress.class);

        when(provider.exists(eq(source.getLabel()), any(UUID.class), any(UUID.class))).thenReturn(true);

        assertThatThrownBy(() -> addresses.save(UUID.randomUUID(), source))
            .isInstanceOf(DuplicateDataException.class)
            .hasMessageStartingWith("Conflict data on entity MeetingAddress. Conflicted:");
    }

    @Test
    @DisplayName("Проверка сохранения измененного")
    void test_save_edited() {
        var source = Instancio.create(MeetingAddress.class);

        when(provider.exists(eq(source.getLabel()), any(UUID.class), eq(source.getId()))).thenReturn(false);
        when(provider.save(any(MeetingAddress.class))).then(inv -> inv.getArgument(0));
        when(provider.get(any(UUID.class), eq(source.getId()))).thenReturn(Optional.of(source));
        when(geoClient.getAddresses(any(String.class), eq(null), eq(null), eq(null), eq(null))).thenReturn(List.of(source));

        var saved = addresses.save(UUID.randomUUID(), source);

        verify(provider).save(saved);
        verify(sender).send(saved, false);
    }

    @Test
    @DisplayName("Проверка удаления")
    void test_delete() {
        var ownerId = UUID.randomUUID();
        var id = UUID.randomUUID();
        var address = Instancio.create(MeetingAddress.class);

        when(provider.get(ownerId, id)).thenReturn(Optional.of(address));
        addresses.delete(ownerId, id);

        verify(sender).send(address, true);
        verify(provider).delete(ownerId, id);
    }

    @Test
    @DisplayName("Проверка получения всех по владельцу")
    void test_get_ofOwner() {
        var ownerId = UUID.randomUUID();
        var expectedList = Instancio.createList(MeetingAddress.class);

        when(provider.getOfOwner(ownerId)).thenReturn(expectedList);

        assertThat(addresses.get(ownerId)).isEqualTo(expectedList);
    }

    @Test
    @DisplayName("Проверка получения по владельцу")
    void test_get_ofOwner_one() {
        var ownerId = UUID.randomUUID();
        var expected = Instancio.create(MeetingAddress.class);

        when(provider.get(ownerId, expected.getId())).thenReturn(Optional.of(expected));

        assertThat(addresses.get(ownerId, expected.getId())).isEqualTo(Optional.of(expected));
    }

    @Test
    @DisplayName("Проверка получения всех")
    void test_getAll() {
        var expectedList = Instancio.createList(MeetingAddress.class);

        when(provider.get()).thenReturn(expectedList);

        assertThat(((MeetingAddresses) addresses).get()).isEqualTo(expectedList);
    }

    @Test
    @DisplayName("Проверка сохранения нового")
    void test_save() {
        var source = Instancio.create(MeetingAddress.class);
        var geoAddress = Instancio.create(GeoAddress.class);

        when(geoClient.getAddresses(anyString(), eq(null), eq(null), eq(null), eq(null)))
            .thenReturn(List.of(geoAddress));

        ((MeetingAddresses) addresses).save(source.getOrganizationId(), source);

        var addressCaptor = ArgumentCaptor.forClass(MeetingAddress.class);

        verify(provider).save(addressCaptor.capture());

        assertThat(addressCaptor.getValue()).isNotNull();
        assertThat(addressCaptor.getValue().getLabel()).isEqualTo(source.getLabel());
        assertThat(addressCaptor.getValue().getBuilding()).isEqualTo(geoAddress.getBuilding());
        assertThat(addressCaptor.getValue().getCity()).isEqualTo(geoAddress.getCity());
        assertThat(addressCaptor.getValue().getCountry()).isEqualTo(geoAddress.getCountry());
        assertThat(addressCaptor.getValue().getHouse()).isEqualTo(geoAddress.getHouse());
        assertThat(addressCaptor.getValue().getLatitude()).isEqualTo(geoAddress.getLatitude());
        assertThat(addressCaptor.getValue().getLongitude()).isEqualTo(geoAddress.getLongitude());
    }

    @Test
    @DisplayName("Проверка сохранения существующего")
    void test_save_edit() {
        var source = Instancio.create(MeetingAddress.class);
        var newAddress = Instancio.of(MeetingAddress.class)
            .set(Select.field(MeetingAddress::getLabel), source.getLabel())
            .create();
        var geoAddress = Instancio.create(GeoAddress.class);

        when(provider.get(source.getLabel())).thenReturn(Optional.of(newAddress));
        when(geoClient.getAddresses(anyString(), eq(null), eq(null), eq(null), eq(null)))
            .thenReturn(List.of(geoAddress));

        ((MeetingAddresses) addresses).save(source.getOrganizationId(), source);

        var addressCaptor = ArgumentCaptor.forClass(MeetingAddress.class);

        verify(provider).save(addressCaptor.capture());

        assertThat(addressCaptor.getValue()).isNotNull();
        assertThat(addressCaptor.getValue().getLabel()).isEqualTo(source.getLabel());
        assertThat(addressCaptor.getValue().getBuilding()).isEqualTo(geoAddress.getBuilding());
        assertThat(addressCaptor.getValue().getCity()).isEqualTo(geoAddress.getCity());
        assertThat(addressCaptor.getValue().getCountry()).isEqualTo(geoAddress.getCountry());
        assertThat(addressCaptor.getValue().getHouse()).isEqualTo(geoAddress.getHouse());
        assertThat(addressCaptor.getValue().getLatitude()).isEqualTo(geoAddress.getLatitude());
        assertThat(addressCaptor.getValue().getLongitude()).isEqualTo(geoAddress.getLongitude());
    }

}