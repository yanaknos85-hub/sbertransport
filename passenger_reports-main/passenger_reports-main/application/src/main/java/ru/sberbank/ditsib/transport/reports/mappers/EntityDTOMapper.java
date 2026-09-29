package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.*;
import ru.sber.transport.request.messaging.AddressMessage;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.messaging.messages.trip.SharedRideMessage;
import ru.sberbank.ditsib.transport.reports.dto.*;
import ru.sberbank.ditsib.transport.reports.dto.response.PersonalResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PublicResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.publicTransport.TransportCompensationDTO;
import ru.sberbank.ditsib.transport.reports.model.*;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistry;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistryString;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRideKPI;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Mapper(uses = { PositionMapper.class, DepartmentMapper.class },
        imports = { ChronoUnit.class })
public interface EntityDTOMapper {

    Address addressMessageToAddress(AddressMessage addressMessage);

    SharedRideKPI sharedRideKPIMessageToModel(SharedRideMessage.SharedRideKPI sharedRideKpi);
    
    ExpectedData expectedDataMessageToModel(RequestMessage.ExpectedData expected);
    
    ContractorDTO contractorToDto(Contractor contractor);
    
    Contractor dtoToContractor(ContractorDTO dto);
    
    TaxiTripRegistryStringDTO taxiRegistryStringToDto(TaxiTripRegistryString string);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<TaxiTripRegistryStringDTO> taxiRegistryStringsToDtoList(List<TaxiTripRegistryString> strings);
    
    @Mapping(target = "contractor.id", source = "contractorId")
    @Mapping(target = "date", source = "date", qualifiedByName = "dateWithFirstDayOfMonth")
    TaxiTripRegistry newDtoToTaxiRegistry(NewTaxiTripRegistryDTO dto);
    
    @Mapping(target = "stringsQnt", source = "registryStrings", qualifiedByName = "listSizeToInt")
    TaxiTripRegistryDTO taxiRegistryToDto(TaxiTripRegistry registry);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<TaxiTripRegistryDTO> taxiRegistryToDtoList(List<TaxiTripRegistry> registries);
    
    @Named("toShortDto")
    @Mapping(target = "stringsQnt", source = "registryStrings", qualifiedByName = "listSizeToInt")
    TaxiTripRegistryShortDTO taxiRegistryToShortDto(TaxiTripRegistry registry);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT, qualifiedByName = "toShortDto")
    List<TaxiTripRegistryShortDTO> taxiRegistryToShortDtoList(List<TaxiTripRegistry> registries);
    
    @Named("toShortDtoWithoutContractor")
    @Mapping(target = "stringsQnt", source = "registryStrings", qualifiedByName = "listSizeToInt")
    TaxiTripRegistryShortWithoutContractorDTO taxiRegistryToShortDtoWithoutContractor(TaxiTripRegistry registry);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT, qualifiedByName = "toShortDtoWithoutContractor")
    List<TaxiTripRegistryShortWithoutContractorDTO> taxiRegistryToShortDtoListWithoutContractor(List<TaxiTripRegistry> registries);
    
    @Named("listSizeToInt")
    static Integer parsedStringsSizeToInt(List<TaxiTripRegistryString> registryStrings) {
        return registryStrings == null ? 0 : registryStrings.size();
    }
    
    //Конвертер даты - установка на первое число месяца
    @Named("dateWithFirstDayOfMonth")
    static LocalDate setFirstDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }
    
    TaxiTripRegistryCheckDTO toCheckRegistry(TaxiTripRegistryString registryString);
    
    /**
     * Получить период выплаты Если поездка попала в числа месяца (включительно): с 1 - 7, то вернуть 1, с 8 - 15 , то вернуть 2, с 16 - 23, то
     * вернуть 3, с 24 - 31, то вернуть 4
     *
     * @param request модель поездки, совмещенная сущность заявки и поездки
     *
     * @return Integer
     */
    default Integer getPeriodOfPaymentFromRequestDTO(Request request) {
        if (request.getCreationTime() == null) {
            return null;
        } else {
            return request.getPeriodOfPayment();
        }
    }
    
    default List<PaymentDataDTO> getPaymentDataList(Request request) {
        PaymentData paymentData = request.getPaymentData();
        if (paymentData == null) {
            return Collections.emptyList();
        }
        
        PaymentDataDTO paymentMain = PaymentDataDTO.builder().
                                                   paymentTypeCode(paymentData.getPaymentTypeCodeMain()).
                                                   paymentPrice(paymentData.getPaymentPriceMain()).
                                                   build();
        
        ArrayList<PaymentDataDTO> paymentDataDTOS = new ArrayList<>();
        paymentDataDTOS.add(paymentMain);
        
        if (paymentData.getPaymentTypeCodeOptional() != null) {
            PaymentDataDTO paymentOptional = PaymentDataDTO.builder().
                                                           paymentTypeCode(paymentData.getPaymentTypeCodeOptional()).
                                                           paymentPrice(paymentData.getPaymentPriceOptional()).
                                                           build();
            
            paymentDataDTOS.add(paymentOptional);
        }
        
        if (paymentData.getPaymentTypeCodeInsurance() != null) {
            PaymentDataDTO paymentInsurance = PaymentDataDTO.builder()
                                                            .paymentTypeCode(paymentData.getPaymentTypeCodeInsurance())
                                                            .paymentPrice(paymentData.getPaymentPriceInsurance())
                                                            .build();
            
            paymentDataDTOS.add(paymentInsurance);
        }
        return paymentDataDTOS;
    }
    
    default Integer getWaypointsCount(List<Waypoint> waypointList) {
        if (waypointList == null) {
            return 0;
        }
        return waypointList.size();
    }
    
    default int getWaypointsCountWithCheckIn(List<Waypoint> waypointList) {
        return getWaypointsCountWithCheckInStatused(waypointList, true);
    }
    
    default int getWaypointsCountWithoutCheckIn(List<Waypoint> waypointList) {
        return getWaypointsCountWithCheckInStatused(waypointList, false);
    }
    
    default int getWaypointsCountWithCheckInStatused(List<Waypoint> waypointList, boolean status) {
        if (waypointList == null) {
            return 0;
        }
        return (int) waypointList.stream().filter(waypoint ->
                                                          waypoint != null && (
                                                                  waypoint.getCheckinAutomatic() == null ||
                                                                  waypoint.getCheckinManual() == null ||
                                                                  Objects.equals(waypoint.getCheckinAutomatic(), status) ||
                                                                  Objects.equals(waypoint.getCheckinManual(), status)))
                                 .count();
    }
    
    @Mapping(target = "cost", source = "expected.cost")
    @Mapping(target = "distance", source = "expected.distance")
    @Mapping(target = "time", expression = "java(source.getExpected().getTime().get(ChronoUnit.SECONDS))")
    @Mapping(target = "waypoints", source = "waypoints")
    @Mapping(target = "waypointsCount", expression = "java(getWaypointsCount(source.getWaypoints()))")
    @Mapping(expression = "java(getWaypointsCountWithCheckIn(source.getWaypoints()))", target = "waypointsCountWithCheckIn")
    @Mapping(expression = "java(getWaypointsCountWithoutCheckIn(source.getWaypoints()))", target = "waypointsCountWithoutCheckIn")
    ExpectedDataDTO requestToExpectedDataDTO(Request source);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<WaypointDTO> waypointListToDTOList(List<Waypoint> list);
    
    default Integer map(Duration value) {
        return Long.valueOf(value == null ? 0 : value.toMinutes()).intValue();
    }

    default WaypointDTO waypointToDto(Waypoint source) {
        if (source != null) {
            return WaypointDTO.builder()
                              .building(source.getAddress().getBuilding())
                              .city(source.getAddress().getCity())
                              .country(source.getAddress().getCountry())
                              .house(source.getAddress().getHouse())
                              .region(source.getAddress().getRegion())
                              .street(source.getAddress().getStreet())
                              .structure(source.getAddress().getStructure())
                              .existInVspGosbTbRegistry(source.getAddress().getExistInVspGosbTbRegistry())
                              .build();
        } else {
            return WaypointDTO.builder()
                              .building(null)
                              .city(null)
                              .country(null)
                              .house(null)
                              .region(null)
                              .street(null)
                              .structure(null)
                              .existInVspGosbTbRegistry(null)
                              .build();
        }
    }
    
    TransportCompensationDTO toTransportCompensationDTO(TransportCompensation transportCompensation);
    
    @Mapping(source = "passenger.itinerantType", target = "itinerantType")
    @Mapping(source = "approvalDate", target = "approveDate")
    @Mapping(source = "passenger.costCenter", target = "costCenter")
    @Mapping(source = "passenger.department", target = "department")
    @Mapping(source = "passenger.position", target = "position")
    @Mapping(target = "paymentQuarter", expression = "java(getPeriodOfPaymentFromRequestDTO(request))")
    @Mapping(target = "expected", expression = "java(requestToExpectedDataDTO(request))")
    @Mapping(target = "humanReadableLimitId", source = "limit.humanReadableId")
    @Mapping(target = "tariff", ignore = true)
    PublicResponseDTO requestToGetPublicDTO(Request request);
    
    @Mapping(source = "rideId", target = "sharedRideId")
    @Mapping(source = "passenger.itinerantType", target = "itinerantType")
    @Mapping(source = "approvalDate", target = "approveDate")
    @Mapping(source = "passenger.costCenter", target = "costCenter")
    @Mapping(source = "passenger.department", target = "department")
    @Mapping(source = "passenger.position", target = "position")
    @Mapping(target = "expected", expression = "java(requestToExpectedDataDTO(request))")
    @Mapping(target = "paymentDataList", expression = "java(getPaymentDataList(request))")
    @Mapping(target = "paymentPeriod", expression = "java(getPeriodOfPaymentFromRequestDTO(request))")
    @Mapping(source = "sharedRide.kpi", target = "kpi")
    @Mapping(target = "humanReadableLimitId", source = "limit.humanReadableId")
    @Mapping(target = "tariff", ignore = true)
    PersonalResponseDTO requestToGetPersonalDTO(Request request);

}
