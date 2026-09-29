package ru.sber.transport.address.messaging.listeners;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.use_cases.Employees;
import ru.sber.transport.address.business.model.Employee;
import ru.sber.transport.address.business.model.GeoAddress;
import ru.sber.transport.address.business.use_cases.Frequentlies;
import ru.sber.transport.address.messaging.mapper.AddressMessageMapperImpl;
import ru.sber.transport.address.messaging.mapper.EmployeeMessageMapper;
import ru.sber.transport.messages.addresses.avro.UsedAddressMessage;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка работы слушателя")
class ListenerConfigTest {

    private final ListenerConfig config = new ListenerConfig();

    @Test
    @DisplayName("Проверка увеличения использования")
    void test_increaseUsage() {
        var frequentlies = mock(Frequentlies.class);

        var input = config.userAddressInput(frequentlies, Jackson2ObjectMapperBuilder.json().featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build());
        var message = Map.<String, Object>of(
            "country", Instancio.create(String.class),
            "region", Instancio.create(String.class),
            "city", Instancio.create(String.class),
            "street", Instancio.create(String.class),
            "house", Instancio.create(String.class),
            "building", Instancio.create(String.class),
            "structure", Instancio.create(String.class),
            "latitude", Instancio.create(Double.class),
            "employeeId", Instancio.create(UUID.class),
            "longitude", Instancio.create(Double.class)
        );
        var rawMessage = MessageBuilder.withPayload(message).build();

        input.accept(rawMessage);

        var addressCaptor = ArgumentCaptor.forClass(GeoAddress.class);

        verify(frequentlies).increaseUsage(addressCaptor.capture(), eq(false), eq((UUID) message.get("employeeId")));

        assertThat(addressCaptor.getValue().getBuilding()).isEqualTo(message.get("building"));
        assertThat(addressCaptor.getValue().getCity()).isEqualTo(message.get("city"));
        assertThat(addressCaptor.getValue().getCountry()).isEqualTo(message.get("country"));
        assertThat(addressCaptor.getValue().getHouse()).isEqualTo(message.get("house"));
        assertThat(addressCaptor.getValue().getLatitude().doubleValue()).isEqualTo(message.get("latitude"));
        assertThat(addressCaptor.getValue().getLongitude().doubleValue()).isEqualTo(message.get("longitude"));
        assertThat(addressCaptor.getValue().getRegion()).isEqualTo(message.get("region"));
        assertThat(addressCaptor.getValue().getStreet()).isEqualTo(message.get("street"));
        assertThat(addressCaptor.getValue().getStructure()).isEqualTo(message.get("structure"));
    }

    @Test
    @DisplayName("Проверка увеличения использования avro")
    void test_increaseUsageAvro() {
        var frequentlies = mock(Frequentlies.class);

        var input = config.userAddressInputAvro(frequentlies, new AddressMessageMapperImpl());
        var message = Instancio.create(UsedAddressMessage.class);
        var rawMessage = MessageBuilder.withPayload(message).build();

        input.accept(rawMessage);

        var addressCaptor = ArgumentCaptor.forClass(GeoAddress.class);

        verify(frequentlies).increaseUsage(addressCaptor.capture(), eq(message.getFirst()), eq(message.getEmployeeId()));

        assertThat(addressCaptor.getValue().getBuilding()).isEqualTo(message.getAddress().getBuilding());
        assertThat(addressCaptor.getValue().getCity()).isEqualTo(message.getAddress().getCity());
        assertThat(addressCaptor.getValue().getCountry()).isEqualTo(message.getAddress().getCountry());
        assertThat(addressCaptor.getValue().getHouse()).isEqualTo(message.getAddress().getHouse());
        assertThat(addressCaptor.getValue().getLatitude()).isEqualTo(message.getCoordinates().getLatitude());
        assertThat(addressCaptor.getValue().getLongitude()).isEqualTo(message.getCoordinates().getLongitude());
        assertThat(addressCaptor.getValue().getRegion()).isEqualTo(message.getAddress().getRegion());
        assertThat(addressCaptor.getValue().getStreet()).isEqualTo(message.getAddress().getStreet());
        assertThat(addressCaptor.getValue().getStructure()).isEqualTo(message.getAddress().getStructure());
    }

    @Test
    @DisplayName("Проверка увеличения использования. Нет сотрудника")
    void test_increaseUsage_noEmployee() {
        var frequentlies = mock(Frequentlies.class);
        var objectMapper = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

        var input = config.userAddressInput(frequentlies, objectMapper);
        var message = Map.<String, Object>of(
            "country", Instancio.create(String.class),
            "region", Instancio.create(String.class),
            "city", Instancio.create(String.class),
            "street", Instancio.create(String.class),
            "house", Instancio.create(String.class),
            "building", Instancio.create(String.class),
            "structure", Instancio.create(String.class),
            "latitude", Instancio.create(Double.class),
            "longitude", Instancio.create(Double.class)
        );
        var rawMessage = MessageBuilder.withPayload(message).build();

        input.accept(rawMessage);

        verify(frequentlies, never()).increaseUsage(any(GeoAddress.class), any(Boolean.class), any(UUID.class));
    }

    @Test
    @DisplayName("Получение данных сотрудника")
    void test_receive_employee() {
        var employee = mock(Employees.class);

        var input = config.employeeInput(employee, Mappers.getMapper(EmployeeMessageMapper.class));

        var message = Instancio.create(EmployeeMessage.class);
        var raw = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        input.accept(raw);

        var employeeCaptor = ArgumentCaptor.forClass(Employee.class);

        verify(employee).update(employeeCaptor.capture());

        assertThat(employeeCaptor.getValue()).isNotNull();
        assertSoftly(soft -> {
            soft.assertThat(employeeCaptor.getValue().id()).isEqualTo(message.getId());
            soft.assertThat(employeeCaptor.getValue().userId()).isEqualTo(message.getUserId());
            soft.assertThat(employeeCaptor.getValue().organizationId()).isEqualTo(message.getOrganizationId());
        });
    }

    @Test
    @DisplayName("Получение данных сотрудника SSL")
    void test_receive_employee_ssl() {
        var employee = mock(Employees.class);

        var input = config.employeeInputSsl(employee, Mappers.getMapper(EmployeeMessageMapper.class));

        var message = Instancio.create(EmployeeMessage.class);
        var raw = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        input.accept(raw);

        var employeeCaptor = ArgumentCaptor.forClass(Employee.class);

        verify(employee).update(employeeCaptor.capture());

        assertThat(employeeCaptor.getValue()).isNotNull();
        assertSoftly(soft -> {
            soft.assertThat(employeeCaptor.getValue().id()).isEqualTo(message.getId());
            soft.assertThat(employeeCaptor.getValue().userId()).isEqualTo(message.getUserId());
            soft.assertThat(employeeCaptor.getValue().organizationId()).isEqualTo(message.getOrganizationId());
        });
    }

    @Test
    @DisplayName("Получение данных сотрудника AVRO")
    void test_receive_employee_avro() {
        var employee = mock(Employees.class);

        var input = config.employeeInputAvro(employee, Mappers.getMapper(EmployeeMessageMapper.class));

        var message = Instancio.create(ru.sber.transport.messages.corporate.avro.EmployeeMessage.class);
        var raw = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        input.accept(raw);

        var employeeCaptor = ArgumentCaptor.forClass(Employee.class);

        verify(employee).update(employeeCaptor.capture());

        assertThat(employeeCaptor.getValue()).isNotNull();
        assertSoftly(soft -> {
            soft.assertThat(employeeCaptor.getValue().id()).isEqualTo(message.getId());
            soft.assertThat(employeeCaptor.getValue().userId()).isEqualTo(message.getUserId());
            soft.assertThat(employeeCaptor.getValue().organizationId()).isEqualTo(message.getOrganizationId());
        });
    }

}