package ru.sber.transport.address.messaging.sender.impl;

import io.qameta.allure.Feature;
import lombok.NonNull;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.address.business.model.MeetingAddress;
import ru.sber.transport.address.messaging.mapper.MeetingAddressMapperImpl;
import ru.sber.transport.address.messaging.sender.AddressSender;
import ru.sber.transport.messages.addresses.avro.MeetingAddressMessage;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.math.BigDecimal;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка отправки предпочитаемых адресов")
class MeetingAddressSenderImplTest {

    private final ObjectProvider<OutputBridge> bridge = new ObjectProvider<>() {

        @Override
        public @NonNull OutputBridge getObject() throws BeansException {
            return ob;
        }

        @Override
        public @NonNull OutputBridge getObject(Object @NonNull ... args) throws BeansException {
            return ob;
        }

        @Override
        public OutputBridge getIfAvailable() throws BeansException {
            return ob;
        }

        @Override
        public OutputBridge getIfUnique() throws BeansException {
            return ob;
        }

        private final OutputBridge ob = mock(OutputBridge.class);
    };

    private final AddressSender<MeetingAddress> sender = new MeetingAddressSenderImpl(bridge, new MeetingAddressMapperImpl());

    @Test
    @DisplayName("Отправка")
    void test_send() {
        var address = Instancio.of(MeetingAddress.class)
            .set(Select.field(MeetingAddress::getLatitude), BigDecimal.valueOf(new Random().nextDouble(0, 360)))
            .set(Select.field(MeetingAddress::getLongitude), BigDecimal.valueOf(new Random().nextDouble(0, 360)))
            .create();

        sender.send(address, false);

        var addressCaptor = ArgumentCaptor.forClass(MeetingAddressMessage.class);

        verify(bridge.getIfAvailable()).send(addressCaptor.capture());

        assertThat(addressCaptor.getValue()).isNotNull();
        assertThat(addressCaptor.getValue().getId()).isEqualTo(address.getId());
        assertThat(addressCaptor.getValue().getAddress().getBuilding()).isEqualTo(address.getBuilding());
        assertThat(addressCaptor.getValue().getAddress().getCity()).isEqualTo(address.getCity());
        assertThat(addressCaptor.getValue().getAddress().getCountry()).isEqualTo(address.getCountry());
        assertThat(addressCaptor.getValue().getAddress().getHouse()).isEqualTo(address.getHouse());
        assertThat(addressCaptor.getValue().getLabel()).isEqualTo(address.getLabel());
        assertThat(addressCaptor.getValue().getAddress().getRegion()).isEqualTo(address.getRegion());
        assertThat(addressCaptor.getValue().getAddress().getStreet()).isEqualTo(address.getStreet());
        assertThat(addressCaptor.getValue().getAddress().getStructure()).isEqualTo(address.getStructure());
    }

    @Test
    @DisplayName("Отправка короткого")
    void test_send_short() {
        var address = Instancio.of(MeetingAddress.class)
            .ignore(Select.field(FavoriteAddress::getCity))
            .ignore(Select.field(FavoriteAddress::getStreet))
            .ignore(Select.field(FavoriteAddress::getHouse))
            .ignore(Select.field(FavoriteAddress::getBuilding))
            .ignore(Select.field(FavoriteAddress::getStructure))
            .create();

        sender.send(address, false);

        var addressCaptor = ArgumentCaptor.forClass(MeetingAddressMessage.class);

        verify(bridge.getIfAvailable()).send(addressCaptor.capture());

        assertThat(addressCaptor.getValue()).isNotNull();
        assertThat(addressCaptor.getValue().getId()).isEqualTo(address.getId());
        assertThat(addressCaptor.getValue().getAddress().getBuilding()).isEqualTo(address.getBuilding());
        assertThat(addressCaptor.getValue().getAddress().getCity()).isEqualTo(address.getCity());
        assertThat(addressCaptor.getValue().getAddress().getCountry()).isEqualTo(address.getCountry());
        assertThat(addressCaptor.getValue().getAddress().getHouse()).isEqualTo(address.getHouse());
        assertThat(addressCaptor.getValue().getLabel()).isEqualTo(address.getLabel());
        assertThat(addressCaptor.getValue().getCoordinates().getLatitude()).isEqualTo(address.getLatitude());
        assertThat(addressCaptor.getValue().getCoordinates().getLongitude()).isEqualTo(address.getLongitude());
        assertThat(addressCaptor.getValue().getAddress().getRegion()).isEqualTo(address.getRegion());
        assertThat(addressCaptor.getValue().getAddress().getStreet()).isEqualTo(address.getStreet());
        assertThat(addressCaptor.getValue().getAddress().getStructure()).isEqualTo(address.getStructure());
    }

    @Test
    @DisplayName("Отправка удаленного")
    void test_send_deleted() {
        var address = Instancio.of(MeetingAddress.class)
            .set(Select.field(MeetingAddress::getLatitude), BigDecimal.valueOf(new Random().nextDouble(0, 360)))
            .set(Select.field(MeetingAddress::getLongitude), BigDecimal.valueOf(new Random().nextDouble(0, 360)))
            .create();

        sender.send(address, true);

        verify(bridge.getIfAvailable(), never()).send(any(MeetingAddressMessage.class));
    }

}