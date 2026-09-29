package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.*;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelType;
import ru.sberbank.ditsib.transport.vehicle.database.model.Transport;
import ru.sberbank.ditsib.transport.vehicle.database.model.Vehicle;
import ru.sberbank.ditsib.transport.vehicle.database.projection.TransportShortProjection;
import ru.sberbank.ditsib.transport.vehicle.dto.files.TransportReportDto;
import ru.sberbank.ditsib.transport.vehicle.dto.files.TransportReportProjection;
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransportInfoDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.OrganizationRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSearchingRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSelfSearchingRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create.TransportCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.update.TransportUpdateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.update.VehicleUpdateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.*;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.TransportMessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        builder = @Builder(disableBuilder = true),
        uses = {OrganizationMapper.class, DepartmentMapper.class, FuelTypeMapper.class})
public interface TransportMapper {

    @Mapping(target = "organizationId", source = "organizationId")
    TransportSearchingRequestDto transportSelfSearchingRequestDtoToTransportSearchingRequestDto(TransportSelfSearchingRequestDto selfSearchingRequestDto,
                                                                                                UUID organizationId);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "stateNumber", source = "stateNumber")
    @Mapping(target = "brand", source = "vehicle.model.brand.title")
    @Mapping(target = "model", source = "vehicle.model.title")
    @Mapping(target = "year", source = "year")
    @Mapping(target = "currentMileage", source = "currentMileage")
    @Mapping(target = "accessiblePositionId", source = "accessiblePosition.id")
    @Mapping(target = "balanceUnitNumber", source = "balanceUnitNumber")
    @Mapping(target = "facility", source = "facility")
    @Mapping(target = "equipmentUnitSystemNumber", source = "equipmentUnitSystemNumber")
    @Mapping(target = "vehicle", source = "transport")
    @Mapping(target = "organizations", source = "transport", qualifiedByName = "mapTransportToListOrganizationResponseDto")
    @Mapping(target = "location", source = "transport")
    @Mapping(target = "subtypeId", source = "subtype.id")
    @Mapping(target = "typeId", source = "subtype.type.id")
    @Mapping(target = "documents.passportNumber", source = "passportNumber")
    @Mapping(target = "documents.passportIssuedDate", source = "passportIssuedDate")
    @Mapping(target = "documents.brandByPassport", source = "brandByPassport")
    @Mapping(target = "documents.modelByPassport", source = "modelByPassport")
    @Mapping(target = "documents.certificateNumber", source = "certificateNumber")
    @Mapping(target = "documents.certificateIssuedDate", source = "certificateIssuedDate")
    @Mapping(target = "documents.vehicleType", source = "vehicleType")
    @Mapping(target = "engine.engineType", source = "vehicle.engineType.title")
    @Mapping(target = "engine.engineCapacity", source = "vehicle.engineCapacity")
    @Mapping(target = "engine.enginePower", source = "vehicle.enginePower")
    @Mapping(target = "engine.fuelType", source = "transport.vehicle.fuelTypes", qualifiedByName = "mapFuelTypesToString")
    @Mapping(target = "engine.cityConsumptionRate", source = "vehicle.cityConsumptionRate")
    @Mapping(target = "engine.countryConsumptionRate", source = "vehicle.countryConsumptionRate")
    @Mapping(target = "engine.hybridConsumptionRate", source = "vehicle.hybridConsumptionRate")
    @Mapping(target = "general", source = "transport")
    @Mapping(target = "service.serviceIntervalMileage", source = "vehicle.serviceIntervalMileage")
    @Mapping(target = "service.serviceIntervalDays", source = "vehicle.serviceIntervalDays")
    @Mapping(target = "service.serviceAuthorizationMileage", source = "vehicle.serviceAuthorizationMileage")
    @Mapping(target = "service.serviceAuthorizationDays", source = "vehicle.serviceAuthorizationDays")
    TransportResponseDto transportToTransportResponseDto(Transport transport);

    @Mapping(target = "exploitationStart", source = "transport.exploitationStart")
    @Mapping(target = "exploitationEnd", source = "transport.exploitationEnd")
    @Mapping(target = "locationAddress", source = "transport.locationAddress")
    @Mapping(target = "parkingAddress", source = "transport.parkingAddress")
    LocationResponseDto transportToLocationResponseDto(Transport transport);

    @Mapping(target = "organizations", ignore = true)
    @Mapping(target = "departments", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "subtype", ignore = true)
    @Mapping(target = "telematics", ignore = true)
    @Mapping(target = "exploitationStart", source = "location.exploitationStart")
    @Mapping(target = "locationAddress", source = "location.locationAddress")
    @Mapping(target = "parkingAddress", source = "location.parkingAddress")
    @Mapping(target = "stateNumber", source = "vehicle.stateNumber")
    @Mapping(target = "year", source = "vehicle.year")
    @Mapping(target = "currentMileage", source = "vehicle.currentMileage")
    @Mapping(target = "vinCode", source = "vehicle.vinCode")
    @Mapping(target = "assetNumber", source = "vehicle.assetNumber")
    @Mapping(target = "inventoryNumber", source = "vehicle.inventoryNumber")
    @Mapping(target = "bodyNumber", source = "vehicle.bodyNumber")
    @Mapping(target = "chassisNumber", source = "vehicle.chassisNumber")
    @Mapping(target = "bodyColor", source = "vehicle.bodyColor")
    @Mapping(target = "passportNumber", source = "documents.passportNumber")
    @Mapping(target = "passportIssuedDate", source = "documents.passportIssuedDate")
    @Mapping(target = "brandByPassport", source = "documents.brandByPassport")
    @Mapping(target = "modelByPassport", source = "documents.modelByPassport")
    @Mapping(target = "certificateNumber", source = "documents.certificateNumber")
    @Mapping(target = "certificateIssuedDate", source = "documents.certificateIssuedDate")
    @Mapping(target = "vehicleType", source = "documents.vehicleType")
    Transport transportCreateDtoToTransport(TransportCreateDto createDto);

    @Mapping(target = "organizations", source = "transport", qualifiedByName = "mapTransportToListOrganizationRequestDto")
    @Mapping(target = "location.exploitationStart", source = "exploitationStart")
    @Mapping(target = "location.locationAddress", source = "locationAddress")
    @Mapping(target = "location.parkingAddress", source = "parkingAddress")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "accessiblePositionId", source = "accessiblePosition.id")
    @Mapping(target = "vehicle", source = "transport", qualifiedByName = "mapTransportToVehicleUpdateDto")
    @Mapping(target = "documents.certificateNumber", source = "certificateNumber")
    @Mapping(target = "documents.certificateIssuedDate", source = "certificateIssuedDate")
    @Mapping(target = "documents.vehicleType", source = "vehicleType")
    @Mapping(target = "contractorId", source = "contractorId")
    @Mapping(target = "autoparkId", source = "autoparkId")
    @Mapping(target = "balanceUnitNumber", source = "balanceUnitNumber")
    @Mapping(target = "facility", source = "facility")
    @Mapping(target = "equipmentUnitSystemNumber", source = "equipmentUnitSystemNumber")
    TransportUpdateDto transportToTransportUpdateDto(Transport transport);

    TransportSearchResponseDto transportToTransportSearchResponseDto(Transport transport);
    List<TransportSearchResponseDto> transportToTransportResponseDtoList(List<Transport> fetchedResult);

    @Mapping(target = "brand", source = "brandByPassport")
    @Mapping(target = "model", source = "modelByPassport")
    TransportSearchResponseDtoV2 transportToTransportSearchWithBrandAndModelResponseDto(Transport transport);

    @Mapping(target = "brand", source = "brandByPassport")
    @Mapping(target = "model", source = "modelByPassport")
    @Mapping(target = "transportType", source = "vehicleType")
    TransportInfoDto transportToTransportInfoDto(Transport source);
    
    List<TransportInfoDto> transportToTransportInfoDto(List<Transport> transports);

    @Mapping(target = "id", source = "vehicle.id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "manufacturer", source = "vehicle.manufacturer")
    @Mapping(target = "vinCode", source = "vinCode")
    @Mapping(target = "assetNumber", source = "assetNumber")
    @Mapping(target = "inventoryNumber", source = "inventoryNumber")
    @Mapping(target = "bodyNumber", source = "bodyNumber")
    @Mapping(target = "chassisNumber", source = "chassisNumber")
    @Mapping(target = "type", source = "subtype.type.title")
    @Mapping(target = "subtype", source = "subtype.title")
    @Mapping(target = "drive", source = "vehicle.drive.title")
    @Mapping(target = "ecologicalClass", source = "vehicle.ecologicalClass")
    @Mapping(target = "bodyType", source = "vehicle.bodyType.title")
    @Mapping(target = "transmissionType", source = "vehicle.transmissionType.title")
    @Mapping(target = "manufacturePeriod", source = "vehicle", qualifiedByName = "mapManufacturePeriod")
    VehicleResponseDto transportToVehicleResponseDto(Transport transport);

    @Mapping(target = "height", source = "vehicle.height")
    @Mapping(target = "width", source = "vehicle.width")
    @Mapping(target = "length", source = "vehicle.length")
    @Mapping(target = "weight", source = "vehicle.weight")
    @Mapping(target = "maxWeight", source = "vehicle.maxWeight")
    @Mapping(target = "bodyColor", source = "bodyColor")
    @Mapping(target = "telematics", source = "telematics.title")
    @Mapping(target = "telematicsId", source = "telematics.id")
    @Mapping(target = "category", source = "vehicle.category.category")
    @Mapping(target = "categoryName", source = "vehicle.category.title")
    @Mapping(target = "fuelTankVolume", source = "vehicle.fuelTankVolume")
    @Mapping(target = "spareWheelHolderInstalled", source = "vehicle.spareWheelHolderInstalled")
    @Mapping(target = "mudguardInstalled", source = "vehicle.mudguardInstalled")
    @Mapping(target = "frontWheelSize", source = "vehicle.frontWheelSize.title")
    @Mapping(target = "rearWheelSize", source = "vehicle.rearWheelSize.title")
    GeneralResponseDto transportToGeneralResponseDto(Transport transport);
    
    TransportSearchWithStructureResponseDto transportShortProjectionToTransportSearchWithStructureResponseDto(TransportShortProjection source);
    
    List<TransportSearchWithStructureResponseDto> listTransportShortProjectionToListTransportSearchWithStructureResponseDto(List<TransportShortProjection> source);

    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "brand", source = "transport.brandByPassport")
    @Mapping(target = "model", source = "transport.modelByPassport")
    @Mapping(target = "transportType", source = "transport.vehicleType")
    @Mapping(target = "vin", source = "transport.vinCode")
    @Mapping(target = "type", source = "transport.subtype.type.title")
    @Mapping(target = "subtype", source = "transport.subtype.title")
    @Mapping(target = "organizationIds", source = "transport.organizations")
    @Mapping(target = "departmentIds", source = "transport.departments")
    @Mapping(target = "vehicleId", source = "transport.vehicle.id")
    @Mapping(target = "status", expression = "java(transport.getStatus().name())")
    @Mapping(target = "modelId", source = "transport.vehicle.model.id")
    @Mapping(target = "brandId", source = "transport.vehicle.model.brand.id")
    @Mapping(target = "fuelTypeIds", source = "transport.vehicle.fuelTypes")
    @Mapping(target = "engineTypeId", source = "transport.vehicle.engineType.id")
    @Mapping(target = "fuelTankVolume", source = "transport.vehicle.fuelTankVolume")
    @Mapping(target = "cityConsumptionRate", source = "transport.vehicle.cityConsumptionRate")
    @Mapping(target = "countryConsumptionRate", source = "transport.vehicle.countryConsumptionRate")
    @Mapping(target = "hybridConsumptionRate", source = "transport.vehicle.hybridConsumptionRate")
    @Mapping(target = "accessiblePositionId", source = "transport.accessiblePosition.id")
    @Mapping(target = "bodyTypeTitle", source = "transport.vehicle.bodyType.title")
    TransportMessage transportToTransportMessage(Transport transport, boolean deleted);

    default LocalDateTime localDateToLocalDateTime(LocalDate localDate) {
        return Optional.ofNullable(localDate)
                .map(LocalDate::atStartOfDay)
                .orElse(null);
    }

    @Named("mapManufacturePeriod")
    default String mapManufacturePeriod(Vehicle source) {
        return "%s - %s".formatted(source.getYearManufactureBegin(),
                Optional.ofNullable(source.getYearManufactureEnd())
                        .map(Object::toString)
                        .orElse("н.в."));
    }
    
    @Named("mapTransportToListOrganizationResponseDto")
    default List<OrganizationResponseDto> mapTransportToListOrganizationResponseDto(Transport transport) {
        return transport.getOrganizations().stream()
                        .map(org -> {
                            // Ищем департамент, связанный с этой организацией
                            var department = transport.getDepartments()
                                                             .stream()
                                                             .filter(dep -> dep.getOrganization() != null && dep.getOrganization().getId().equals(org.getId()))
                                                             .findFirst()
                                                             .orElse(null);
                            
                            return new OrganizationResponseDto(
                                    org.getId(),
                                    org.getOfficialName(),
                                    department != null ? department.getId() : null,
                                    department != null ? department.getDepartmentName() : null
                            );
                        })
                        .toList();
    }

    @Named("mapTransportToListOrganizationRequestDto")
    default List<OrganizationRequestDto> mapTransportToListOrganizationRequestDto(Transport transport) {
        return transport.getDepartments().stream()
                .map(department -> new OrganizationRequestDto(department.getOrganization().getId(), department.getId()))
                .toList();
    }

    @Named("mapFuelTypesToString")
    default String mapFuelTypesToString(Set<FuelType> fuelTypes) {
        return fuelTypes.stream()
                .map(FuelType::getTitle)
                .collect(Collectors.joining(", "));
    }
    
    List<TransportReportDto> listTransportReportProjectionToTransportReportDto(List<TransportReportProjection> source);

    @Named("mapTransportToVehicleUpdateDto")
    default VehicleUpdateDto mapTransportToVehicleUpdateDto(Transport transport) {
        return new VehicleUpdateDto(
                transport.getVehicle() != null ? transport.getVehicle().getId() : null,
                transport.getStateNumber(),
                transport.getVinCode(),
                transport.getAssetNumber(),
                transport.getInventoryNumber(),
                transport.getBodyNumber(),
                transport.getChassisNumber(),
                transport.getTelematics() != null ? transport.getTelematics().getId() : null,
                transport.getCurrentMileage(),
                transport.getSubtype().getId()
        );
    }
}
