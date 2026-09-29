package ru.sber.transport.constants.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.constants.dto.*;
import ru.sberbank.ditsib.transport.constants.*;

/**
 * Mapper of constants.
 */
@Mapper
public interface ConstantsMapper {

    /**
     * Map constant to web-object.
     *
     * @param source constant.
     * @return web-object.
     */
    @Mapping(target = "name", expression = "java(source.name())")
    @Mapping(target = "rusName", source = "description")
    @Mapping(target = "finalStatus", source = "terminal")
    RequestStatusDTO toDto(TripRequestStatus source);

    /**
     * Map constant to web-object.
     *
     * @param source constant.
     * @return web-object.
     */
    @Mapping(target = "name", expression = "java(source.name())")
    @Mapping(target = "rusName", source = "description")
    RequestOptionsDTO toDto(RequestOptions source);

    /**
     * Map constant to web-object.
     *
     * @param source constant.
     * @return web-object.
     */
    TransportTypeDTO toDto(TransportTypeEnum source);

    /**
     * Map constant to web-object.
     *
     * @param source constant.
     * @return web-object.
     */
    @Mapping(target = "name", expression = "java(source.name())")
    @Mapping(target = "rusName", source = "description")
    TaxiIntegrationTypeDTO toDto(TaxiExternalIntegrationType source);

    /**
     * Map constant to web-object.
     *
     * @param source constant.
     * @return web-object.
     */
    @Mapping(target = "name", expression = "java(source.name())")
    CargoTransportTypeDTO toCargoDto(TransportTypeEnum source);

    /**
     * Map constant to web-object.
     *
     * @param source constant.
     * @return web-object.
     */
    @Mapping(target = "name", expression = "java(source.name())")
    PersonalTransportTypeDTO toPersonalDto(PersonalTransportType source);

    /**
     * Map constant to web-object.
     *
     * @param source constant.
     * @return web-object.
     */
    @Mapping(target = "name", expression = "java(source.name())")
    PersonalCarOwnerInfoEnumDTO toDto(PersonalCarOwnerInfo source);

    /**
     * Map constant to web-object.
     *
     * @param source constant.
     * @return web-object.
     */
    @Mapping(target = "name", expression = "java(source.name())")
    PublicCompensationTypeDTO toDto(PublicCompensationType source);

    /**
     * Map constant to web-object.
     *
     * @param source constant.
     * @return web-object.
     */
    @Mapping(target = "name", expression = "java(source.name())")
    PublicTransportTypeDTO toDto(PublicTransportType source);
}
