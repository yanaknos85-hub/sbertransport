package ru.sber.transport.dispatcher.mappers;

import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.dto.DriverDTO;
import ru.sber.transport.dispatcher.dto.NewDriverDTO;
import ru.sber.transport.dispatcher.messages.ContractorUpdateTripMessage;
import ru.sber.transport.dispatcher.messages.DriverMessage;

import java.util.Objects;

/**
 * Маппер данных водителей.
 */
@Mapper(uses = { AttributeMapper.class })
public interface DriverMapper {

    /**
     * Преобразовать модель в объект обмена данными.
     *
     * @param source исходный объект.
     * @return объект обмена данными.
     */
    @Mapping(target = "contractorId", source = "contractor.id")
    @Mapping(target = "autoparkId", source = "autopark.id")
    @Mapping(target = "autoparkName", source = "autopark.name")
    DriverDTO toDto(Driver source);

    /**
     * Преобразовать модель в сообщение.
     *
     * @param driver исходный объект.
     * @return сообщение.
     */
    @Mapping(target = "contractorId", source = "contractor.id")
    @Mapping(target = "licenseClasses", source = "driverLicenses")
    @Mapping(target = "tags", source = "attributes")
    ContractorUpdateTripMessage.Driver toMessage(Driver driver);

    /**
     * Преобразовать модель в сообщение.
     *
     * @param driver исходный объект.
     * @return сообщение.
     */
    @Mapping(target = "contractorId", source = "driver.contractor.id")
    @Mapping(target = "licenseClasses", source = "driver.driverLicenses")
    @Mapping(target = "autoparkId", source = "autopark.id")
    DriverMessage toDriverMessage(Driver driver);

    /**
     * Обновить объект модель данных.
     *
     * @param target целевой объект.
     * @param source исходный объект.
     */
    @Mapping(target = "autopark", ignore = true)
    void update(@MappingTarget Driver target, NewDriverDTO source);

    /**
     * Обновить объект модель данных.
     *
     * @param target целевой объект.
     * @param source исходный объект.
     */
    @Mapping(target = "driverSpeciality", ignore = true)
    @Mapping(target = "driverLicenses", ignore = true)
    @Mapping(target = "contractor", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    void update(@MappingTarget Driver target, DriverMessage source);

    @BeforeMapping
    default void setPhoneConfirmed(@MappingTarget Driver target, NewDriverDTO newData) {
        target.setPhoneConfirmed(Objects.equals(target.getContactPhone(), newData.contactPhone()));
    }

}
