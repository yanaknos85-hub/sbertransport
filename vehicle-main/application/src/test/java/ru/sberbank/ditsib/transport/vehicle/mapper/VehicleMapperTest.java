package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.vehicle.database.model.*;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleShortDto;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка маппера автомобилей")
class VehicleMapperTest {
    private final VehicleMapper mapper = Mappers.getMapper(VehicleMapper.class);

    private final List<Vehicle> sourceList = List.of(Vehicle.builder()
                    .id(UUID.randomUUID())
                    .model(Model.builder()
                            .title("2114")
                            .brand(Brand.builder()
                                    .title("Лада")
                                    .build())
                            .build())
                    .fuelTypes(Set.of(
                            FuelType.builder()
                                    .title("АИ-98")
                                    .engineType(EngineType.builder()
                                            .title("Бензин")
                                            .build())
                                    .build())
                    )
                    .category(Category.builder()
                            .id(UUID.randomUUID())
                            .title("В")
                            .build())
                    .engineCapacity(1500)
                    .fuelTankVolume(55)
                    .drive(Drive.builder()
                            .title("Полный")
                            .build())
                    .spareWheelHolderInstalled(true)
                    .mudguardInstalled(false)
                    .transmissionType(TransmissionType.builder()
                            .id(UUID.randomUUID())
                            .title("МКПП 3")
                            .build())
                    .bodyType(BodyType.builder()
                            .id(UUID.randomUUID())
                            .title("Седан")
                            .build())
                    .frontWheelSize(WheelSize.builder()
                            .id(UUID.randomUUID())
                            .title("215/50 R17")
                            .build())
                    .rearWheelSize(WheelSize.builder()
                            .id(UUID.randomUUID())
                            .title("215/50 R18")
                            .build())
                    .yearManufactureBegin(1980)
                    .weight(1000)
                    .height(100)
                    .weight(200)
                    .length(300)
                    .engineType(EngineType.builder()
                            .id(UUID.randomUUID())
                            .title("Бензин")
                            .build())
                    .build(),
            Vehicle.builder()
                    .id(UUID.randomUUID())
                    .model(Model.builder()
                            .title("k5")
                            .brand(Brand.builder()
                                    .title("Кия")
                                    .build())
                            .build())
                    .fuelTypes(Set.of(FuelType.builder()
                            .title("АИ-80")
                            .engineType(EngineType.builder()
                                    .title("Углеводород")
                                    .build())
                            .build()))
                    .category(Category.builder()
                            .id(UUID.randomUUID())
                            .title("С")
                            .build())
                    .engineCapacity(4500)
                    .fuelTankVolume(155)
                    .drive(Drive.builder()
                            .title("Задний")
                            .build())
                    .spareWheelHolderInstalled(false)
                    .mudguardInstalled(true)
                    .transmissionType(TransmissionType.builder()
                            .id(UUID.randomUUID())
                            .title("РКПП 10")
                            .build())
                    .bodyType(BodyType.builder()
                            .id(UUID.randomUUID())
                            .title("Кабриолет")
                            .build())
                    .frontWheelSize(WheelSize.builder()
                            .id(UUID.randomUUID())
                            .title("240/75 R19")
                            .build())
                    .rearWheelSize(WheelSize.builder()
                            .id(UUID.randomUUID())
                            .title("240/75 R20")
                            .build())
                    .yearManufactureBegin(2023)
                    .yearManufactureEnd(2024)
                    .weight(2000)
                    .height(400)
                    .weight(500)
                    .length(600)
                    .engineType(EngineType.builder()
                            .id(UUID.randomUUID())
                            .title("Углеводород")
                            .build())
                    .build()
    );

    @Test
    void toShortDto() {
        var resultList = mapper.listVehicleToListVehicleShortDto(sourceList);
        assertThat(sourceList).hasSameSizeAs(resultList);
        for (int i = 0; i < sourceList.size(); i++) {
            checkShortEquality(sourceList.get(i), resultList.get(i));
        }
    }

    private void checkShortEquality(Vehicle source, VehicleShortDto result) {
        SoftAssertions.assertSoftly(as -> {
            as.assertThat(source.getId()).isEqualTo(result.id());
            as.assertThat(source.getModel().getBrand().getTitle()).isEqualTo(result.brand());
            as.assertThat(source.getModel().getTitle()).isEqualTo(result.model());
            as.assertThat(source.getEngineType().getTitle()).isEqualTo(result.engineType());
            assertThat(
                    source.getFuelTypes().stream()
                            .map(FuelType::getTitle)
                            .collect(Collectors.joining(", "))
            ).isEqualTo(result.fuelType());
            as.assertThat(source.getEngineCapacity()).isEqualTo(result.engineCapacity());
            as.assertThat(source.getFuelTankVolume()).isEqualTo(result.fuelTankVolume());
            as.assertThat(source.getDrive().getTitle()).isEqualTo(result.drive());
            as.assertThat(source.isSpareWheelHolderInstalled()).isEqualTo(result.spareWheelHolderInstalled());
            as.assertThat(source.isMudguardInstalled()).isEqualTo(result.mudguardInstalled());
            if (Objects.isNull(source.getYearManufactureEnd())) {
                as.assertThat("%s - %s".formatted(source.getYearManufactureBegin(), "н.в.")).isEqualTo(result.manufacturePeriod());
            } else {
                as.assertThat("%s - %s".formatted(source.getYearManufactureBegin(), source.getYearManufactureEnd())).isEqualTo(result.manufacturePeriod());
            }
            as.assertThat(source.getWeight()).isEqualTo(result.weight());
            as.assertThat(source.getTransmissionType().getTitle()).isEqualTo(result.transmissionType());
            as.assertThat(source.getWeight()).isEqualTo(result.weight());
            as.assertThat(source.getBodyType().getTitle()).isEqualTo(result.bodyType());
            as.assertThat("%dx%dx%d".formatted(source.getLength(), source.getWidth(), source.getHeight())).isEqualTo(result.dimensions());
        });
    }
}