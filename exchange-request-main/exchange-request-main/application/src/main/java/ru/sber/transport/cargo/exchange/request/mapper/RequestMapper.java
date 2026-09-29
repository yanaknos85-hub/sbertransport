package ru.sber.transport.cargo.exchange.request.mapper;

import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.*;
import ru.sber.transport.cargo.exchange.request.database.model.*;
import ru.sber.transport.cargo.exchange.request.dto.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static ru.sber.transport.cargo.exchange.request.enums.RequestStatus.CARRIER_SELECTED;
import static ru.sber.transport.cargo.exchange.request.enums.RequestStatus.DRAFT;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface RequestMapper {

    // === Основные методы маппинга Request ===

    @Mapping(source = "request.specialConditions", target = "specialConditions")
    @Mapping(source = "request.cargoDetails", target = "cargoDetails")
    @Mapping(source = "request.vehicleRequirements", target = "vehicleRequirements")
    @Mapping(target = "selectedReplyOwner", expression = "java(isSelectedReplyOwner(request, userOrganizationId))")
    @Mapping(target = "attachments", ignore = true)
    RequestDto toDto(Request request, UUID userOrganizationId);

    Request toEntity(RequestDto requestDto);

    SpecialConditionsDto toSpecialConditionsDto(SpecialConditions specialConditions);

    @InheritInverseConfiguration
    SpecialConditions toSpecialConditionsEntity(SpecialConditionsDto specialConditionsDto);

    // === Маппинг CargoDetails ===
    CargoDetailsDto toCargoDetailsDto(CargoDetails cargoDetails);

    @InheritInverseConfiguration
    CargoDetails toCargoDetailsEntity(CargoDetailsDto cargoDetailsDto);

    VehicleRequirementsDto toVehicleRequirementsDto(VehicleRequirements vehicleRequirements);

    @InheritInverseConfiguration
    VehicleRequirements toVehicleRequirementsEntity(VehicleRequirementsDto vehicleRequirementsDto);

    RequestShortDto toShortDto(Request request);

    RequestShortDto toShortDto(RequestDto requestDto);

    /**
     * Обновляет только определённые поля в существующем DTO из сущности Request.
     * Поля: status, ownerId, humanReadableId, createdAt, expiresAt
     * <p>
     * Используется при создании черновика — чтобы заполнить системные поля.
     *
     * @param request сущность с данными
     * @param dto     целевой DTO, который обновляется
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "ownerId", source = "ownerId")
    @Mapping(target = "humanReadableId", source = "humanReadableId")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "organizationId", source = "organizationId")
    void updateDtoFromEntity(Request request, @MappingTarget RequestDto dto);

    // === Маппинг Attachment → AttachmentDto ===
    @Mapping(target = "id", source = "id")
    @Mapping(target = "requestId", source = "requestId")
    @Mapping(target = "originalName", source = "originalName")
    @Mapping(target = "fileType", source = "fileType")
    @Mapping(target = "storagePath", source = "storagePath")
    @Mapping(target = "uploadAt", source = "uploadAt")
    AttachmentDto toAttachmentDto(Attachment attachment);

    @InheritInverseConfiguration
    Attachment toAttachmentEntity(AttachmentDto attachmentDto);

    // Если в RequestDto есть List<AttachmentDto>
    List<AttachmentDto> toAttachmentDtoList(List<Attachment> attachments);

    List<Attachment> toAttachmentEntityList(List<AttachmentDto> attachmentDtos);

    @Mapping(target = "addressFrom", source = "request", qualifiedByName = "fromAddress")
    @Mapping(target = "addressTo", source = "request", qualifiedByName = "toAddress")
    @Mapping(target = "loadingDate", source = "request", qualifiedByName = "getLoadingDate")
    @Mapping(target = "deliveryDate", source = "request", qualifiedByName = "getDeliveryDate")
    @Mapping(target = "status", source = "status.displayName")
    ShipperRequestDto toShipperDto(Request request);

    @Mapping(target = "addressFrom", source = "request", qualifiedByName = "fromAddress")
    @Mapping(target = "addressTo", source = "request", qualifiedByName = "toAddress")
    @Mapping(target = "loadingDate", source = "request", qualifiedByName = "getLoadingDate")
    @Mapping(target = "deliveryDate", source = "request", qualifiedByName = "getDeliveryDate")
    @Mapping(target = "vehicleBodyType", source = "request.vehicleRequirements.vehicleBodyType")
    @Mapping(target = "weight", source = "request.cargoDetails", qualifiedByName = "mapWeightKgToTons")
    @Mapping(target = "volume", source = "request.cargoDetails.volumeM3")
    @Mapping(target = "selfReplied", expression = "java(isSelfReplied(request, userOrganizationId))")
    MarketplaceRequestDto toSummaryDto(Request request, UUID userOrganizationId);

    @Mapping(target = "humanReadableId", source = "humanReadableId")
    @Mapping(target = "addressFrom", source = "request", qualifiedByName = "fromAddress")
    @Mapping(target = "addressTo", source = "request", qualifiedByName = "toAddress")
    @Mapping(target = "loadingDate", source = "request", qualifiedByName = "getLoadingDate")
    @Mapping(target = "deliveryDate", source = "request", qualifiedByName = "getDeliveryDate")
    @Mapping(target = "etrn", source = "useEtrn")
    @Mapping(target = "status", source = "status.displayName")
    CarrierRequestDto toCarrierRequest(Request request);

    RequestDto copyOf(RequestDto dto);

    @Named("fromAddress")
    default String fromAddress(Request request) {
        return getSortedWaypoints(request)
                .findFirst()
                .map(Waypoint::getAddressInfo)
                .map(AddressInfoDto::toFullAddressString)
                .orElse(null);
    }

    @Named("toAddress")
    default String toAddress(Request request) {
        return getSortedWaypoints(request)
                .reduce((first, last) -> last) // Получаем последний элемент
                .map(Waypoint::getAddressInfo)
                .map(AddressInfoDto::toFullAddressString)
                .orElse(null);
    }

    @Named("getLoadingDate")
    default LocalDate getLoadingDate(Request request) {
        return getSortedWaypoints(request)
                .findFirst()
                .flatMap(wp -> Optional.ofNullable(wp.getDate()))
                .orElse(null);
    }

    @Named("getDeliveryDate")
    default LocalDate getDeliveryDate(Request request) {
        return getSortedWaypoints(request)
                .reduce((first, last) -> last)
                .flatMap(wp -> Optional.ofNullable(wp.getDate()))
                .orElse(null);
    }

    @Named("mapWeightKgToTons")
    default Double mapWeightKgToTons(CargoDetails cargoDetails) {
        return Optional.ofNullable(cargoDetails)
                .map(CargoDetails::getWeightKg)
                .map(Number::doubleValue)
                .map(weight -> weight / 1_000.0)
                .orElse(null);
    }

    default Stream<Waypoint> getSortedWaypoints(Request request) {
        return Optional.ofNullable(request)
                .map(Request::getWaypoints)
                .filter(CollectionUtils::isNotEmpty)
                .stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparing(Waypoint::getOrderingIndex));
    }

    default boolean isSelfReplied(Request request, UUID userOrganizationId) {
        if (request == null || request.getCarrierReplyOrganizationIds() == null || userOrganizationId == null) {
            return false;
        }

        return request.getCarrierReplyOrganizationIds().contains(userOrganizationId);
    }

    default boolean isSelectedReplyOwner(Request request, UUID userOrganizationId) {
        if (request == null || request.getCarrierOrganizationId() == null || userOrganizationId == null) {
            return false;
        }

        return CARRIER_SELECTED == request.getStatus() && request.getCarrierOrganizationId().equals(userOrganizationId);
    }

    default ShipperRequestDto toShipperRequestDto(Request request){
        if (request == null) {
            return null;
        }

        if (DRAFT == request.getStatus()) {
            return toShipperDto(toEntity(request.getCopyOfDraftInfoWithId()));
        }

        return toShipperDto(request);
    }
}
