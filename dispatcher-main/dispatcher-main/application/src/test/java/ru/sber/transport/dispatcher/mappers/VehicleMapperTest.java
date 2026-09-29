package ru.sber.transport.dispatcher.mappers;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.database.model.Vehicle;
import ru.sber.transport.dispatcher.dto.enums.VehicleType;
import ru.sber.transport.dispatcher.messages.TransportMessage;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@DisplayName("Проверка маппера транспортного средства")
class VehicleMapperTest {
    private VehicleMapper vehicleMapper = new VehicleMapperImpl(
            new CarModelMapperImpl(),
            new AutoparkMapperImpl(new ContractorMapperImpl(new BooleanMapperImpl(), new DispatcherMapperImpl()))
    );

    @DisplayName("Проверка создания транспортного средства из сообщения")
    @ParameterizedTest
    @CsvSource({
            "IN_USE, true, Легковой",
            "NOT_IN_USE, false, Грузовой"
    })
    void testCreateVehicleFromTransportMessage(String status, boolean value, String subType) {
        var message = Instancio.of(TransportMessage.class)
                .set(field(TransportMessage::subtype), subType)
                .set(field(TransportMessage::status), status)
                .ignore(field(TransportMessage::exploitationEnd))
                .create();

        var entity = new Vehicle();
        vehicleMapper.updateVehicleEntityWithUniversalType(entity, message);

        assertNull(entity.getId());
        assertEquals(message.stateNumber(), entity.getStateNumber());
        assertEquals(message.vin(), entity.getVin());
        assertEquals(message.brand(), entity.getModel().getBrand());
        assertEquals(message.model(), entity.getModel().getName());
        assertEquals(message.year(), entity.getModel().getYear());
        assertEquals(message.currentMileage(), entity.getMileage());
        assertNull(entity.getInsuranceNumber());
        assertNull(entity.getEcoClass());
        assertNull(entity.getFuelConsumption());
        assertNull(entity.getPackageClass());
        assertNull(entity.getColor());
        assertNull(entity.getManufactureYear());
        assertNull(entity.getMaxAllowedWeight());
        assertNull(entity.getChassisType());
        assertNull(entity.getTransmissionType());
        assertNull(entity.getBodyType());
        assertNull(entity.getPassport());
        assertNull(entity.getEngineType());
        assertTrue(entity.isInExploitation());
        assertEquals(value, entity.isActive());
        assertEquals(VehicleType.UNIVERSAL, entity.getVehicleType());
        assertEquals(0, entity.getVehicleAdditional().size());
    }

    @DisplayName("Проверка обновления транспортного средства из сообщения")
    @ParameterizedTest
    @CsvSource({
            "IN_USE, true, Легковой",
            "NOT_IN_USE, false, Грузовой"
    })
    void testUpdateVehicleFromTransportMessage(String status, boolean value, String subType) {
        var message = Instancio.of(TransportMessage.class)
                .set(field(TransportMessage::subtype), subType)
                .set(field(TransportMessage::status), status)
                .ignore(field(TransportMessage::exploitationEnd))
                .create();

        var entity = Instancio.of(Vehicle.class).create();
        vehicleMapper.updateVehicleEntityWithUniversalType(entity, message);

        assertEquals(message.stateNumber(), entity.getStateNumber());
        assertEquals(message.vin(), entity.getVin());
        assertEquals(message.currentMileage(), entity.getMileage());
        assertEquals(message.brand(), entity.getModel().getBrand());
        assertEquals(message.model(), entity.getModel().getName());
        assertEquals(message.year(), entity.getModel().getYear());
        assertTrue(entity.isInExploitation());
        assertEquals(value, entity.isActive());
        assertEquals(VehicleType.UNIVERSAL, entity.getVehicleType());
        assertEquals(message.parkingAddress(), entity.getParkingAddress());
        assertEquals(message.locationAddress(), entity.getLocationAddress());
    }
}
