package ru.sber.transport.address.business.use_cases.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.address.business.model.GeoAddress;
import ru.sber.transport.address.business.provider.AddressDataProvider;
import ru.sber.transport.address.business.use_cases.Frequentlies;
import ru.sber.transport.address.business.use_cases.mapper.FrequentlyMapper;
import ru.sber.transport.address.business.use_cases.mapper.FrequentlyMapperImpl;
import ru.sber.transport.address.messaging.sender.AddressSender;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка кейсов работы с частыми адресами")
class FrequentlyImplTest {

    private final AddressDataProvider<FrequentlyAddress> provider = mock(AddressDataProvider.class);

    private final FrequentlyMapper mapper = new FrequentlyMapperImpl();

    private final AddressSender<FrequentlyAddress> sender = mock(AddressSender.class);

    private final BaseAddressImpl<FrequentlyAddress> addresses = new FrequentlyImpl(provider, mapper, sender);

    @Test
    @DisplayName("Проверка сохранения нового")
    void test_save_new() {
        var source = Instancio.create(FrequentlyAddress.class);
        var ownerId = source.getOwner();

        when(provider.save(any(FrequentlyAddress.class))).then(inv -> inv.getArgument(0));

        var saved = addresses.save(ownerId, source);

        verify(provider).save(saved);
        verify(sender).send(saved, false);
    }

    @Test
    @DisplayName("Проверка сохранения измененного")
    void test_save_edited() {
        var source = Instancio.create(FrequentlyAddress.class);
        var ownerId = source.getOwner();

        when(provider.save(any(FrequentlyAddress.class))).then(inv -> inv.getArgument(0));
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
        var address = Instancio.create(FrequentlyAddress.class);

        when(provider.get(ownerId, id)).thenReturn(Optional.of(address));
        addresses.delete(ownerId, id);

        verify(provider).delete(ownerId, id);
        verify(sender).send(address, true);
    }

    @Test
    @DisplayName("Проверка получения всех по владельцу")
    void test_get_ofOwner() {
        var ownerId = UUID.randomUUID();
        var expectedList = Instancio.createList(FrequentlyAddress.class);

        when(provider.getOfOwner(ownerId)).thenReturn(expectedList);

        assertThat(addresses.get(ownerId)).isEqualTo(expectedList);
    }

    @Test
    @DisplayName("Проверка получения по владельцу")
    void test_get_ofOwner_one() {
        var ownerId = UUID.randomUUID();
        var expected = Instancio.create(FrequentlyAddress.class);

        when(provider.get(ownerId, expected.getId())).thenReturn(Optional.of(expected));

        assertThat(addresses.get(ownerId, expected.getId())).isEqualTo(Optional.of(expected));
    }

    @Test
    @DisplayName("Увеличение счетчика. Новый адрес.")
    void test_increaseUsage_new() {
        var address = Instancio.create(GeoAddress.class);
        var ownerId = UUID.randomUUID();

        ((Frequentlies) addresses).increaseUsage(address, true, ownerId);

        var addressCaptor = ArgumentCaptor.forClass(FrequentlyAddress.class);

        verify(provider).save(addressCaptor.capture());

        assertThat(addressCaptor.getValue()).isNotNull();
        assertThat(addressCaptor.getValue().getCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Увеличение счетчика. Существующий адрес.")
    void test_increaseUsage_exists() {
        var address = Instancio.create(GeoAddress.class);
        var ownerId = UUID.randomUUID();
        var source = Instancio.create(FrequentlyAddress.class);
        var count = source.getCount();

        when(provider.get(ownerId, address.getId())).thenReturn(Optional.of(source));

        ((Frequentlies) addresses).increaseUsage(address, false, ownerId);

        var addressCaptor = ArgumentCaptor.forClass(FrequentlyAddress.class);

        verify(provider).save(addressCaptor.capture());

        assertThat(addressCaptor.getValue()).isNotNull();
        assertThat(addressCaptor.getValue().getCount()).isEqualTo(count + 1);
        assertThat(addressCaptor.getValue().isFirst()).isFalse();
    }

}