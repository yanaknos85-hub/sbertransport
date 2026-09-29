package ru.sber.transport.dispatcher.mappers;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.database.model.Autopark;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@DisplayName("Проверка маппера автопарков")
public class AutoparkMapperTest {

    private final AutoparkMapper autoparkMapper = new AutoparkMapperImpl(
            new ContractorMapperImpl(new BooleanMapperImpl(), new DispatcherMapperImpl()));

    @Test
    void entityToAutoparkMessageTest() {
        var entity = Instancio.of(Autopark.class).create();
        var message = autoparkMapper.toAutoparkMessage(entity);
        assertEquals(entity.getId(), message.id());
        assertEquals(entity.getName(), message.name());
        assertEquals(entity.getContractor().getId(), message.contractorId());
        assertEquals(entity.getRoutingId(), message.routingId());
        assertEquals(entity.getVehicleCountNorm(), message.vehicleCountNorm());
    }

    @Test
    void entityToAutoparkDtoTest() {
        var entity = Instancio.of(Autopark.class).create();
        var dto = autoparkMapper.toAutoparkDTO(entity);
        assertEquals(entity.getId(), dto.id());
        assertEquals(entity.getName(), dto.name());
        assertEquals(entity.getContractor().getId(), dto.contractor().id());
        assertEquals(entity.getContractor().getTin(), dto.contractor().tin());
        assertEquals(entity.getRoutingId(), dto.routingId());
    }

}
