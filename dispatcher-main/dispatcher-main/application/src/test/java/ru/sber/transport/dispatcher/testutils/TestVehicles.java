package ru.sber.transport.dispatcher.testutils;

import ru.sber.transport.dispatcher.database.model.*;
import ru.sber.transport.dispatcher.dto.CarModelDto;
import ru.sber.transport.dispatcher.dto.NewVehicleDTO;
import ru.sber.transport.dispatcher.dto.VehicleAdditional;
import ru.sber.transport.dispatcher.dto.VehicleDTO;
import ru.sber.transport.dispatcher.dto.enums.VehicleType;
import ru.sber.transport.dispatcher.mappers.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static ru.sber.transport.dispatcher.testutils.TestUtils.replaceEnd;

public class TestVehicles {

    private static final String STATE_NUMBER_FORMAT = "X%03dXX163RUS";

    private static final VehicleMapper mapper = new VehicleMapperImpl(new CarModelMapperImpl(), new AutoparkMapperImpl(new ContractorMapperImpl(new BooleanMapperImpl(), new DispatcherMapperImpl())));

    public static Vehicle createTestCargoVehicle(Autopark autopark, int i) {
        return Vehicle.builder()
                .passport("00TK000000")
                .vin("XYX000000X0000000")
                .stateNumber(String.format(STATE_NUMBER_FORMAT, 0))
                .insuranceNumber("AAA0123456789")
                .autopark(autopark)
                .model(createCarModel("Skoda " + i, "Octavia " + i, 2019))
                .bodyType("Sedan")
                .color("Eggplant")
                .ecoClass(EcoClass.EURO_5)
                .chassisType("Front")
                .transmissionType("Auto")
                .fuelConsumption(5.5)
                .manufactureYear(2020)
                .mileage(10000)
                .packageClass("AMBITION PLUS")
                .maxAllowedWeight(1000)
                .engineType("Бензиновый")
                .inExploitation(true)
                .vehicleType(VehicleType.CARGO)
                .vehicleAdditional(Map.of(
                        VehicleAdditional.Fields.VOLUME.getFieldName(), 12.0
                ))
                .build();
    }

    public static Vehicle createTestPassengerVehicle(Autopark autopark, int i) {
        return Vehicle.builder()
                .passport("00TK000000")
                .vin("XYX000000X0000000")
                .stateNumber(String.format(STATE_NUMBER_FORMAT, 0))
                .insuranceNumber("AAA0123456789")
                .autopark(autopark)
                .model(createCarModel("Skoda " + i, "Octavia " + i, 2019))
                .bodyType("Sedan")
                .color("Eggplant")
                .ecoClass(EcoClass.EURO_5)
                .chassisType("Front")
                .transmissionType("Auto")
                .fuelConsumption(5.5)
                .manufactureYear(2020)
                .mileage(10000)
                .packageClass("AMBITION PLUS")
                .maxAllowedWeight(1000)
                .engineType("Бензиновый")
                .inExploitation(true)
                .vehicleType(VehicleType.PASSENGER)
                .vehicleAdditional(null)
                .build();
    }

    public static Vehicle createTestUniversalVehicle(Autopark autopark, int i) {
        return Vehicle.builder()
                .passport("00TK000000")
                .vin("XYX000000X0000000")
                .stateNumber(String.format(STATE_NUMBER_FORMAT, 0))
                .insuranceNumber("AAA0123456789")
                .autopark(autopark)
                .model(createCarModel("Skoda " + i, "Octavia " + i, 2019))
                .bodyType("Sedan")
                .color("Eggplant")
                .ecoClass(EcoClass.EURO_5)
                .chassisType("Front")
                .transmissionType("Auto")
                .fuelConsumption(5.5)
                .manufactureYear(2020)
                .mileage(10000)
                .packageClass("AMBITION PLUS")
                .maxAllowedWeight(1000)
                .engineType("Бензиновый")
                .inExploitation(true)
                .vehicleType(VehicleType.UNIVERSAL)
                .vehicleAdditional(null)
                .build();
    }

    public static Vehicle createTestVehicle(Autopark autopark, int i) {
        return Vehicle.builder()
                .passport("00TK000000")
                .vin("XYX000000X0000000")
                .stateNumber(String.format(STATE_NUMBER_FORMAT,0))
                .insuranceNumber("AAA0123456789")
                .autopark(autopark)
                .model(createCarModel("Skoda " + i, "Octavia " + i, 2019))
                .bodyType("Sedan")
                .color("Eggplant")
                .ecoClass(EcoClass.EURO_5)
                .chassisType("Front")
                .transmissionType("Auto")
                .fuelConsumption(5.5)
                .manufactureYear(2020)
                .mileage(10000)
                .packageClass("AMBITION PLUS")
                .maxAllowedWeight(1000)
                .engineType("Бензиновый")
                .inExploitation(true)
                .vehicleType(VehicleType.PASSENGER)
                .vehicleAdditional(new HashMap<>())
                .build();
    }
    
    private static CarModel createCarModel(String brand, String model, int year) {
        var carModel = new CarModel();
    
        carModel.setYear(year);
        carModel.setName(model);
        carModel.setBrand(brand);
        
        return carModel;
    }
    
    public static NewVehicleDTO createTestVehicleDTO() {
        return NewVehicleDTO.builder()
                .passport("00TK000000")
                .vin("XYX000000X0000000")
                .stateNumber("X000YX163RUS")
                .insuranceNumber("AAA0123456789")
                .model(CarModelDto.builder()
                        .brand("Skoda")
                        .name("Octavia")
                        .year(2019)
                        .build()
                )
                .bodyType("Sedan")
                .color("Eggplant")
                .ecoClass(EcoClass.EURO_5)
                .chassisType("Front")
                .transmissionType("Auto")
                .fuelConsumption(5.5)
                .manufactureYear(2020)
                .mileage(10000)
                .packageClass("AMBITION PLUS")
                .maxAllowedWeight(1000)
                .engineType("Бензиновый")
                .inExploitation(true)
                .vehicleType(VehicleType.CARGO)
                .vehicleAdditional(new CargoVehicleData("DA1111111RUS", 12, 312, 123, 321))
                .build();
    }

    public static Vehicle incrementVehicle(Vehicle v, int i) {
        var newV = mapper.toDto(v, null);
        newV.setPassport(replaceEnd(v.getPassport(), i));
        newV.setVin(replaceEnd(v.getVin(), i));
        newV.setStateNumber(String.format(STATE_NUMBER_FORMAT, Integer.parseInt(v.getStateNumber().substring(1, 4)) + i));
        newV.setInsuranceNumber(replaceEnd(v.getInsuranceNumber(),i));
        var ap = v.getAutopark();

        v = new Vehicle();
        mapper.update(v, newV);
        v.setAutopark(ap);
        return v;
    }

    public static NewVehicleDTO incrementVehicleDTO(NewVehicleDTO newVehicle, int i){
        newVehicle.setPassport(replaceEnd(newVehicle.getPassport(), i));
        newVehicle.setVin(replaceEnd(newVehicle.getVin(), i));
        newVehicle.setStateNumber(String.format(STATE_NUMBER_FORMAT, Integer.parseInt(newVehicle.getStateNumber().substring(1, 4)) + i));
        newVehicle.setInsuranceNumber(replaceEnd(newVehicle.getInsuranceNumber(),i));
        return newVehicle;
    }
    
    public static VehicleDTO createVehicleDTOwithId() {
        return VehicleDTO.builder()
                         .passport("00TK000000")
                         .vin("XYX000000X0000000")
                         .stateNumber("X000XX163RUS")
                         .insuranceNumber("AAA0123456789")
                         .model(CarModelDto.builder()
                                                      .brand("Skoda")
                                                      .name("Octavia")
                                                      .year(2019)
                                                      .build()
                               )
                         .bodyType("Sedan")
                         .color("Eggplant")
                         .ecoClass(EcoClass.EURO_5)
                         .chassisType("Front")
                         .transmissionType("Auto")
                         .fuelConsumption(5.5)
                         .manufactureYear(2020)
                         .mileage(10000)
                         .packageClass("AMBITION PLUS")
                         .maxAllowedWeight(1000)
                         .engineType("Бензиновый")
                         .inExploitation(true)
                         .id(UUID.randomUUID())
                         .build();
    }
}
