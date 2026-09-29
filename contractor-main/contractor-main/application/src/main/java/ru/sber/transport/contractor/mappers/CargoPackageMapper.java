package ru.sber.transport.contractor.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.contractor.messages.CargoPackageMessage;
import ru.sber.transport.contractor.database.model.CargoPackage;
import ru.sber.transport.contractor.dto.cargo.CargoPackageDto;
import ru.sber.transport.contractor.dto.cargo.NewCargoPackageDto;

import static ru.sber.transport.contractor.mappers.BooleanMapper.NEGATE;

/**
 * Маппер грузовых сущностей.
 */
@Mapper(uses = BooleanMapper.class)
public interface CargoPackageMapper {

    /**
     * Конвертация в объект обмена данных.
     *
     * @param cargoPackage источник.
     * @return результат.
     */
    @Mapping(target = "contractor", source = "contractor.id")
    CargoPackageDto toDto(CargoPackage cargoPackage);

    /**
     * Обновление модели.
     *
     * @param target модель для обновления.
     * @param source источник.
     */
    @Mapping(target = "contractor.id", source = "contractor")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    void update(@MappingTarget CargoPackage target, NewCargoPackageDto source);

    /**
     * Конвертация модели в сообщение.
     *
     * @param source источник.
     * @return сообщение.
     */
    @Mapping(target = "organization", source = "contractor.id")
    @Mapping(target = "deleted", source = "active", qualifiedByName = NEGATE)
    CargoPackageMessage toMessage(CargoPackage source);
}
