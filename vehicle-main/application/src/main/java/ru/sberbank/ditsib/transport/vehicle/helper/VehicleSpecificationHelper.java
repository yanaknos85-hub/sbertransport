package ru.sberbank.ditsib.transport.vehicle.helper;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.sberbank.ditsib.transport.vehicle.database.model.*;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleSearchDto;

import java.util.Optional;
import java.util.stream.Stream;

@UtilityClass
public class VehicleSpecificationHelper {
    public Specification<Vehicle> prepareSearchingRequest(VehicleSearchDto searchDto) {
        return (root, query, builder) -> {
            var vehiclePredicates = Optional.ofNullable(searchDto.vehicle())
                    .map(vehicle -> Stream.of(
                            Optional.ofNullable(vehicle.brand())
                                    .map(brand -> builder.equal(
                                            root.get(Vehicle_.MODEL).get(Model_.BRAND).get(Brand_.ID),
                                            brand)),
                            Optional.ofNullable(vehicle.model())
                                    .map(model -> builder.equal(
                                            root.get(Vehicle_.MODEL).get(Model_.ID),
                                            model)),
                            Optional.ofNullable(vehicle.bodyType())
                                    .map(bodyType -> builder.equal(
                                            root.get(Vehicle_.BODY_TYPE).get(BodyType_.ID),
                                            bodyType)),
                            Optional.ofNullable(vehicle.manufactureYear())
                                    .map(manufactureYear -> builder.and(
                                                    builder.lessThanOrEqualTo(root.get(Vehicle_.YEAR_MANUFACTURE_BEGIN),
                                                            manufactureYear),
                                                    builder.or(
                                                            builder.greaterThanOrEqualTo(root.get(Vehicle_.YEAR_MANUFACTURE_END),
                                                                    manufactureYear),
                                                            builder.isNull(root.get(Vehicle_.YEAR_MANUFACTURE_END))))),
                            Optional.ofNullable(vehicle.manufacturePeriod())
                                    .map(manufacturePeriod -> builder.and(
                                            builder.equal(builder.concat(
                                                            builder.concat(builder.toString(root.get(Vehicle_.YEAR_MANUFACTURE_BEGIN)), builder.literal(" - ")),
                                                            builder.coalesce(builder.toString(root.get(Vehicle_.YEAR_MANUFACTURE_END)), builder.literal("н.в."))),
                                                    manufacturePeriod))))
                            )
                    .orElse(Stream.empty());
            var enginePredicates = Optional.ofNullable(searchDto.engine())
                    .map(engine -> Stream.of(
                            Optional.ofNullable(engine.engineType())
                                    .map(engineType -> builder.equal(
                                            root.get(Vehicle_.ENGINE_TYPE).get(EngineType_.ID),
                                            engineType)),
                            Optional.ofNullable(engine.drive())
                                    .map(drive -> builder.equal(
                                            root.get(Vehicle_.DRIVE).get(Drive_.ID),
                                            drive)),
                            Optional.ofNullable(engine.transmissionType())
                                    .map(transmissionType -> builder.equal(
                                            root.get(Vehicle_.TRANSMISSION_TYPE).get(TransmissionType_.ID),
                                            transmissionType)),
                            Optional.ofNullable(engine.engineCapacity())
                                    .map(engineCapacity -> builder.equal(
                                            root.get(Vehicle_.ENGINE_CAPACITY),
                                            engineCapacity)),
                            Optional.ofNullable(engine.enginePower())
                                    .map(enginePower -> builder.equal(
                                            root.get(Vehicle_.ENGINE_POWER),
                                            enginePower))
                    ))
                    .orElse(Stream.empty());
            return Stream.concat(vehiclePredicates, enginePredicates)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .reduce(builder.isTrue(builder.literal(true)), builder::and);
        };
    }
}
