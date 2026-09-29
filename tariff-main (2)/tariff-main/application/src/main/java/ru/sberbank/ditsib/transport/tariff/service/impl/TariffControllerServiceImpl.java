package ru.sberbank.ditsib.transport.tariff.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff.model.BaseTariffDataDto;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.tariff.database.dao.ContractorRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.*;
import ru.sberbank.ditsib.transport.tariff.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.tariff.exceptions.TariffLogicException;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.TariffSender;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;
import ru.sberbank.ditsib.transport.tariff.service.GeoZoneService;
import ru.sberbank.ditsib.transport.tariff.service.TariffControllerService;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;

import java.util.*;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.CARSHARING;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.TAXI;

/**
 * Base controller service for tariffs.
 */
@Slf4j
@Component
@AllArgsConstructor
@Transactional
public class TariffControllerServiceImpl implements TariffControllerService {
    
    private final TariffService tariffService;
    
    private final ContractService contractService;
    
    private final TariffSender tariffSender;
    
    private final GeoZoneService geoZoneService;
    
    private final EntityDTOMapper mapper;
    
    private final ContractorRepository contractorRepository;
    
    
    @Override
    public TaxiTariffDTO addTaxi(NewTaxiTariffDTO newData) {
        // choose contract for binding new tariff
        final TaxiTariff tariff = tariffService.newDtoToTaxiTariff(newData);
        TaxiTariff savedTariff = (TaxiTariff) tariffService.save(tariff);
        tariffSender.send(savedTariff, false);
        return tariffService.taxiTariffToDTO(savedTariff);
    }
    
    @Override
    public PersonalTariffDTO addPersonal(NewPersonalTariffDTO newData) {
        var tariff = (PersonalTariff) tariffService.save(mapper.newDtoToPersonalTariff(newData));
        tariffSender.send(tariff, false);
        return mapper.personalTariffToDTO(tariff);
    }
    
    @Override
    public CarSharingTariffDTO addCarSharing(NewCarSharingTariffDTO newData, UUID organizationId, boolean dataMaster) {
        //проверить, что тариф еще не создан
        Contract contract = contractService.get(newData.getContractId());
        TariffSearchDTO tariffSearchDTO = TariffSearchDTO.builder()
                                                         .contractId(contract.getId()).active(true)
                                                         .organizationId(organizationId)
                                                         .transportType(CARSHARING)
                                                         .build();
        List<? extends BaseTariff> tariffs =
                tariffService.search(tariffSearchDTO, organizationId, dataMaster, PageRequest.of(0, Integer.MAX_VALUE)).toList();
        if (!tariffs.isEmpty()) {
            throw new DuplicateDataException(CarSharingTariff.class,
                                             Map.of("transportType", CARSHARING, "contratId", contract.getId()));
        }
        var tariff = (CarSharingTariff) tariffService.save(mapper.newDtoToCarSharingTariff(newData));
        tariffSender.send(tariff, false);
        return mapper.carsharingTariffToDTO(tariff);
    }
    
    @Override
    public BicycleTariffDTO addBicycle(NewBicycleTariffDTO newData) {
        var tariff = (BicycleTariff) tariffService.save(mapper.newDtoToBicycleTariff(newData));
        tariffSender.send(tariff, false);
        return mapper.bicycleTariffToDTO(tariff);
    }
    
    @Override
    public ScooterTariffDTO addScooter(NewScooterTariffDTO newData) {
        var tariff = (ScooterTariff) tariffService.save(mapper.newDtoToScooterTariff(newData));
        tariffSender.send(tariff, false);
        return mapper.scooterTariffToDTO(tariff);
    }
    
    @Override
    public PublicTariffDTO addPublic(NewPublicTariffDTO newData) {
        var tariff = (PublicTariff) tariffService.save(mapper.newDtoToPublicTariff(newData));
        tariffSender.send(tariff, false);
        return mapper.publicTariffToDTO(tariff);
    }
    
    @Override
    public GroupTransferTariffDTO addGroupTransfer(NewGroupTransferTariffDTO newData) {
        var tariff = (GroupTransferTariff) tariffService.save(mapper.newDtoToGroupTransferTariff(newData));
        return mapper.groupTransferTariffToDTO(tariff);
    }
    
    @Override
    public void edit(UUID transportType, UUID tariffId, NewBaseTariffDto newData) {
        var entity = getTariff(transportType, tariffId);
        BaseTariff baseTariff = editEntity(entity, newData);
        var tariff = tariffService.save(baseTariff);
        tariffSender.send(tariff, false);
    }
    
    
    @Override
    public void delete(UUID transportType, UUID tariffId) {
        var entity = getTariff(transportType, tariffId);
        tariffService.delete(entity);
        tariffSender.send(entity, true);
    }
    
    @Override
    public BaseTariffDataDto get(UUID transportType, UUID tariffId) {
        return convertToResponse(getTariff(transportType, tariffId));
    }
    
    @Override
    public TaxiTariffDTO getTaxi(UUID tariffId) {
        return tariffService.taxiTariffToDTO(getTaxiTariff(tariffId));
    }
    
    @Override
    public PersonalTariffDTO getPersonal(UUID tariffId) {
        return mapper.personalTariffToDTO(getPersonalTariff(tariffId));
    }
    
    @Override
    public CarSharingTariffDTO getCarSharing(UUID tariffId) {
        return mapper.carsharingTariffToDTO(getCarSharingTariff(tariffId));
    }
    
    @Override
    public BicycleTariffDTO getBicycle(UUID tariffId) {
        return mapper.bicycleTariffToDTO(getBicycleTariff(tariffId));
    }
    
    @Override
    public ScooterTariffDTO getScooter(UUID tariffId) {
        return mapper.scooterTariffToDTO(getScooterTariff(tariffId));
    }
    
    @Override
    public PublicTariffDTO getPublic(UUID tariffId) {
        return mapper.publicTariffToDTO(getPublicTariff(tariffId));
    }
    
    @Override
    public GroupTransferTariffDTO getGroupTransfer(UUID tariffId) {
        return mapper.groupTransferTariffToDTO(getGroupTransferTariff(tariffId));
    }
    
    @Override
    public List<? extends ShortTariffDto> getAll(UUID regionId, ContractType contractType) {
        return tariffService.getAll(regionId, contractType).stream().map(this::convertToShortResponse)
                            .collect(Collectors.toList());
    }
    
    @Override
    public List<? extends ShortTariffDto> getAll(UUID transportType, UUID regionId, ContractType contractType) {
        return tariffService.getAll(transportType, regionId, contractType).stream().map(this::convertToShortResponse)
                            .collect(Collectors.toList());
    }
    
    @Override
    public List<TaxiTariffDTO> getAllTaxi(UUID regionId) {
        return tariffService.getAll(TAXI, regionId, null).stream()//FIXME CONTRACT TYPE
                            .map(elt -> tariffService.taxiTariffToDTO((TaxiTariff) elt)).collect(Collectors.toList());
    }
    
    @Override
    public List<CarSharingTariffDTO> getAllCarSharing(UUID regionId) {
        return tariffService.getAll(CARSHARING, regionId, null).stream()//FIXME CONTRACT TYPE
                            .map(elt -> mapper.carsharingTariffToDTO((CarSharingTariff) elt))
                            .collect(Collectors.toList());
    }
    
    @Override
    public List<BicycleTariffDTO> getAllBicycle(UUID regionId) {
        return tariffService.getAll(TransportTypeEnum.BICYCLE, regionId, null).stream()//FIXME CONTRACT TYPE
                            .map(elt -> mapper.bicycleTariffToDTO((BicycleTariff) elt))
                            .collect(Collectors.toList());
        
    }
    
    @Override
    public List<ScooterTariffDTO> getAllScooter(UUID regionId) {
        return tariffService.getAll(TransportTypeEnum.SCOOTER, regionId, null).stream()//FIXME CONTRACT TYPE
                            .map(elt -> mapper.scooterTariffToDTO((ScooterTariff) elt))
                            .collect(Collectors.toList());
    }
    
    @Override
    public List<GroupTransferTariffDTO> getAllGroupTransfer(UUID regionId) {
        return tariffService.getAll(TransportTypeEnum.GROUP_TRANSFER, regionId, null).stream()//FIXME CONTRACT TYPE
                            .map(elt -> mapper.groupTransferTariffToDTO((GroupTransferTariff) elt))
                            .collect(Collectors.toList());
    }
    
    @Override
    public Page<ShortTariffDto> search(TariffSearchDTO searchDTO, UUID organizationId, boolean dataMaster, Pageable pageable) {
        Page<? extends BaseTariff> page = tariffService.search(searchDTO, organizationId, dataMaster, pageable);
        return new PageImpl<>(page.stream().map(this::convertToShortResponse)
                                  .collect(Collectors.toList()), pageable, page.getTotalElements());
        
    }
    
    @Override
    public List<PersonalTariffDTO> getAllPersonal(UUID regionId) {
        return tariffService.getAll(TransportTypeEnum.PERSONAL, regionId, null).stream()//FIXME CONTRACT TYPE
                            .map(elt -> mapper.personalTariffToDTO((PersonalTariff) elt))
                            .collect(Collectors.toList());
    }
    
    @Override
    public List<PublicTariffDTO> getAllPublic(UUID regionId) {
        return tariffService.getAll(TransportTypeEnum.PUBLIC, regionId, null).stream()//FIXME CONTRACT TYPE
                            .map(elt -> mapper.publicTariffToDTO((PublicTariff) elt)).collect(Collectors.toList());
    }
    
    /**
     * Convert entity to response.
     *
     * @param tariff source entity.
     *
     * @return response.
     */
    private BaseTariffDataDto convertToResponse(BaseTariff tariff) {
        UUID contractId = null;
        UUID contractorId = null;
        String workgroup = null;
        
        if (tariff instanceof BaseTariffWithContract) {
            contractId = Optional.ofNullable(((BaseTariffWithContract) tariff).getContract())
                                 .map(Contract::getId)
                                 .orElse(null);
            contractorId = Optional.ofNullable(((BaseTariffWithContract) tariff).getContract())
                                   .map(Contract::getContractorId)
                                   .orElse(null);
            
            if (tariff instanceof TaxiTariff) {
                workgroup = ((TaxiTariff) tariff).getWorkGroup();
            } else {
                try {
                    workgroup = tariffService.getWorkgroup((BaseTariffWithContract) tariff).getFullName();
                } catch (Exception e) {
                    log.error("Error when getting workgroup for tariff with id: {}", tariff.getId());
                }
            }
        }
        
        return BaseTariffDataDto.builder()
                                .id(tariff.getId())
                                .regionId(Collections.singleton(tariff.getRegionId()))
                                .serviceType(tariff.getServiceType())
                                .organizationId(tariff.getOrganization().getId())
                                .humanReadableId(tariff.getHumanReadableId())
                                .transportType(tariff.getTransportType())
                                .contractId(contractId)
                                .contractorId(contractorId)
                                .workgroup(workgroup)
                                .active(tariff.isActive())
                                .build();
    }
    
    /**
     * Convert entity to response.
     *
     * @param tariff source entity.
     *
     * @return response.
     */
    private ShortTariffDto convertToShortResponse(BaseTariff tariff) {
        UUID contractId = null;
        UUID contractorId = null;
        String contractNumber = null;
        if (tariff instanceof TaxiTariff taxiTariff) {
            if (taxiTariff.getContract() != null) {
                contractId = taxiTariff.getContract().getId();
                contractorId = taxiTariff.getContract().getContractorId();
                contractNumber = taxiTariff.getContract().getContractNumber();
            } else {
                throw new RuntimeException("Для такси тарифа " + tariff.getId() + " не найден контракт");
            }
        } else if (tariff instanceof GroupTransferTariff groupTransferTariff) {
            if (groupTransferTariff.getContract() != null) {
                contractId = groupTransferTariff.getContract().getId();
                contractorId = groupTransferTariff.getContract().getContractorId();
                contractNumber = groupTransferTariff.getContract().getContractNumber();
            } else {
                throw new RuntimeException("Для тарифа на групповой трансфер " + tariff.getId() + " не найден контракт");
            }
        }
        
        final UUID finalContractorId = contractorId;
        var contractorName = Optional.ofNullable(finalContractorId)
                                     .map(contractorRepository::findById)
                                     .map(opt -> opt.orElseThrow(() -> new EntityNotFoundException(Contractor.class, finalContractorId)))
                                     .map(Contractor::getName)
                                     .orElse(null);
        var shortTariff = ShortTariffDto.builder().id(tariff.getId())
                                        .humanReadableId(tariff.getHumanReadableId())
                                        .region(tariff.getRegion())
                                        .regionId(tariff.getRegionId())
                                        .transportType(tariff.getTransportType())
                                        .serviceType(tariff.getServiceType())
                                        .organizationId(tariff.getOrganization().getId())
                                        .contractId(contractId)
                                        .contractorId(contractorId)
                                        .contractorName(contractorName)
                                        .contractNumber(contractNumber)
                                        .active(tariff.isActive());
        if (tariff.getDepartment() != null && tariff.getDepartment().getId() != null) {
            shortTariff.departmentHumanReadableId(tariff.getDepartment().getHumanReadableId());
        }
        if (tariff instanceof TaxiTariff taxiTariff) {
            shortTariff.isNightTariff(taxiTariff.getIsNightTariff());
        }
        return shortTariff.build();
    }
    
    /**
     * Convert request to entity.
     *
     * @param entity source entity.
     * @param newData new item request.
     *
     * @return entity.
     */
    private BaseTariff editEntity(BaseTariff entity, NewBaseTariffDto newData) {
        BaseTariff tariff = null;
        
        try {
            switch (entity.getTransportType()) {
                case TAXI -> tariff = tariffService.newDtoToTaxiTariff((NewTaxiTariffDTO) newData);
                case PERSONAL -> tariff = mapper.newDtoToPersonalTariff((NewPersonalTariffDTO) newData);
                case PUBLIC -> tariff = mapper.newDtoToPublicTariff((NewPublicTariffDTO) newData);
                case CARSHARING -> tariff = mapper.newDtoToCarSharingTariff((NewCarSharingTariffDTO) newData);
                case BICYCLE -> tariff = mapper.newDtoToBicycleTariff((NewBicycleTariffDTO) newData);
                case SCOOTER -> tariff = mapper.newDtoToScooterTariff((NewScooterTariffDTO) newData);
                case GROUP_TRANSFER -> tariff = mapper.newDtoToGroupTransferTariff((NewGroupTransferTariffDTO) newData);
                default -> throw new TariffLogicException("Не предусмотренный вид транспорта transportType: " + entity.getTransportType());
            }
        } catch (ClassCastException e) {
            log.error(String.format("ClassCastException for edit tariff %s", entity.getHumanReadableId()), e);
        }
        
        if (tariff != null) {
            return tariff.toBuilder()
                         .id(entity.getId())
                         .humanReadableId(entity.getHumanReadableId())
                         .organization(entity.getOrganization())
                         .transportType(entity.getTransportType())
                         .serviceType(entity.getServiceType())
                         .region(geoZoneService.get(tariff.getRegionId()).map(GeoZone::getName).orElse(null))
                         .regionId(tariff.getRegionId())
                         .active(true)
                         .build();
        }
        
        return entity.toBuilder()
                     .organization(Organization.builder()
                             .id(newData.getOrganizationId())
                             .build())
                     .regionId(newData.getRegionId().stream().findFirst().orElse(null))
                     .build();
    }
    
    
    /**
     * Get tariff.
     *
     * @param transportType ID of transport type.
     * @param tariffId ID of tariff.
     *
     * @return tariff.
     *
     * @throws EntityNotFoundException tariff with given ID not found.
     */
    private BaseTariff getTariff(UUID transportType, UUID tariffId) {
        return tariffService.get(transportType, tariffId).orElseThrow(
                () -> new EntityNotFoundException(BaseTariff.class, tariffId));
    }
    
    /**
     * Получить тариф такси
     *
     * @param tariffId ID of tariff.
     *
     * @return tariff.
     *
     * @throws EntityNotFoundException tariff with given ID not found.
     */
    private TaxiTariff getTaxiTariff(UUID tariffId) {
        return (TaxiTariff) tariffService.get(TAXI, tariffId)
                                         .orElseThrow(() -> new EntityNotFoundException(TaxiTariff.class, tariffId));
    }
    
    private PersonalTariff getPersonalTariff(UUID tariffId) {
        return (PersonalTariff) tariffService.get(TransportTypeEnum.PERSONAL, tariffId)
                                             .orElseThrow(() -> new EntityNotFoundException(PersonalTariff.class, tariffId));
    }
    
    private CarSharingTariff getCarSharingTariff(UUID tariffId) {
        return (CarSharingTariff) tariffService.get(CARSHARING, tariffId)
                                               .orElseThrow(() -> new EntityNotFoundException(CarSharingTariff.class, tariffId));
    }
    
    private BicycleTariff getBicycleTariff(UUID tariffId) {
        return (BicycleTariff) tariffService.get(TransportTypeEnum.BICYCLE, tariffId)
                                            .orElseThrow(() -> new EntityNotFoundException(BicycleTariff.class, tariffId));
    }
    
    private ScooterTariff getScooterTariff(UUID tariffId) {
        return (ScooterTariff) tariffService.get(TransportTypeEnum.SCOOTER, tariffId)
                                            .orElseThrow(() -> new EntityNotFoundException(ScooterTariff.class, tariffId));
    }
    
    private PublicTariff getPublicTariff(UUID tariffId) {
        return (PublicTariff) tariffService.get(TransportTypeEnum.PUBLIC, tariffId)
                                           .orElseThrow(() -> new EntityNotFoundException(PublicTariff.class, tariffId));
    }
    
    private GroupTransferTariff getGroupTransferTariff(UUID tariffId) {
        return (GroupTransferTariff) tariffService.get(TransportTypeEnum.GROUP_TRANSFER, tariffId)
                                                  .orElseThrow(() -> new EntityNotFoundException(GroupTransferTariff.class, tariffId));
    }
}
