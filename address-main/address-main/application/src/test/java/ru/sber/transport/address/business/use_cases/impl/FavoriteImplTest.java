package ru.sber.transport.address.business.use_cases.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.address.business.provider.AddressDataProvider;
import ru.sber.transport.address.business.use_cases.mapper.AddressBusinessMapper;
import ru.sber.transport.address.messaging.sender.AddressSender;
import ru.sber.transport.exceptions.DuplicateDataException;

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
@DisplayName("Проверка кейсов работы с адресами")
class FavoriteImplTest {

    private final AddressDataProvider<FavoriteAddress> provider = mock(AddressDataProvider.class);

    private final AddressBusinessMapper<FavoriteAddress> mapper = mock(AddressBusinessMapper.class);

    private final AddressSender<FavoriteAddress> sender = mock(AddressSender.class);

    private final BaseAddressImpl<FavoriteAddress> addresses = new FavoriteImpl(provider, mapper, sender);

    @Test
    @DisplayName("Проверка сохранения нового")
    void test_save_new() {
        var source = Instancio.create(FavoriteAddress.class);
        var ownerId = source.getOwner();

        when(provider.exists(eq(source.getLabel()), eq(ownerId), any(UUID.class))).thenReturn(false);
        when(provider.save(any(FavoriteAddress.class))).then(inv -> inv.getArgument(0));

        var saved = addresses.save(ownerId, source);

        verify(provider).save(saved);
        verify(sender).send(saved, false);
    }

    @Test
    @DisplayName("Проверка сохранения нового. Конфликт")
    void test_save_conflict() {
        var source = Instancio.create(FavoriteAddress.class);
        var ownerId = source.getOwner();

        when(provider.exists(eq(source.getLabel()), eq(ownerId), any(UUID.class))).thenReturn(true);

        assertThatThrownBy(() -> addresses.save(ownerId, source))
            .isInstanceOf(DuplicateDataException.class)
            .hasMessageStartingWith("Conflict data on entity FavoriteAddress. Conflicted:");
    }

    @Test
    @DisplayName("Проверка сохранения измененного")
    void test_save_edited() {
        var source = Instancio.create(FavoriteAddress.class);
        var ownerId = source.getOwner();

        when(provider.exists(source.getLabel(), ownerId, source.getId())).thenReturn(false);
        when(provider.save(any(FavoriteAddress.class))).then(inv -> inv.getArgument(0));
        when(provider.get(ownerId, source.getId())).thenReturn(Optional.of(source));

        var saved = addresses.save(ownerId, source);

        verify(provider).save(saved);
        verify(sender).send(saved, false);
    }

    @Test
    @DisplayName("Проверка удаления")
    void test_delete() {
        var ownerId = UUID.randomUUID();
        var id = UUID.randomUUID();
        var address = Instancio.create(FavoriteAddress.class);

        when(provider.get(ownerId, id)).thenReturn(Optional.of(address));

        addresses.delete(ownerId, id);

        verify(sender).send(address, true);
        verify(provider).delete(ownerId, id);
    }

    @Test
    @DisplayName("Проверка получения всех по владельцу")
    void test_get_ofOwner() {
        var ownerId = UUID.randomUUID();
        var expectedList = Instancio.createList(FavoriteAddress.class);

        when(provider.getOfOwner(ownerId)).thenReturn(expectedList);

        assertThat(addresses.get(ownerId)).isEqualTo(expectedList);
    }

    @Test
    @DisplayName("Проверка получения по владельцу")
    void test_get_ofOwner_one() {
        var ownerId = UUID.randomUUID();
        var expected = Instancio.create(FavoriteAddress.class);

        when(provider.get(ownerId, expected.getId())).thenReturn(Optional.of(expected));

        assertThat(addresses.get(ownerId, expected.getId())).isEqualTo(Optional.of(expected));
    }

}