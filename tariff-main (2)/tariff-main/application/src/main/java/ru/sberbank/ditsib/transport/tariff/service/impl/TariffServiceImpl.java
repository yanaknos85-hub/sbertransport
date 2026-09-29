package ru.sberbank.ditsib.transport.tariff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.humanreadableid.model.Prefix;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.transport.tariff.TariffSearchSpecHelper;
import ru.sberbank.ditsib.transport.tariff.database.dao.*;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.NewTaxiTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.TariffSearchDTO;
import ru.sberbank.ditsib.transport.tariff.dto.TaxiTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.TransportClass;
import ru.sberbank.ditsib.transport.tariff.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.*;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;
import ru.sberbank.ditsib.transport.tariff.util.ConvertUtils;
import ru.sberbank.utils.reflection.ReflectionUtils;
import ru.sberbank.ditsib.transport.tariff.dto.*;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.SingularAttribute;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.*;

/**
 * имплементация сервиса тарифов
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class TariffServiceImpl implements TariffService {
    
    private static final String AGGREGATOR_NAME = "ООО \"Транспортные решения\"";
    
    private final TariffRepository<BaseTariff> repository;
    
    private final WalkTariffRepository walkTariffRepository;
    
    private final TaxiTariffRepository taxiTariffRepository;
    
    private final PersonalTariffRepository personalTariffRepository;
    
    private final PublicTariffRepository publicTariffRepository;
    
    private final BicycleTariffRepository bicycleTariffRepository;
    
    private final ScooterTariffRepository scooterTariffRepository;
    
    private final CarSharingTariffRepository carSharingTariffRepository;
    
    private final EntityDTOMapper entityDTOMapper;
    
    private final ContractorRepository contractorRepository;
    
    @Qualifier("sQGeneratorTariff")
    private final SQGenerator sqGenerator;
    
    private final OrganizationRepository organizationRepository;
    
    private final TaxiTariffSender taxiTariffSender;
    
    private final PersonalTariffSender personalTariffSender;
    
    private final PublicTariffSender publicTariffSender;
    
    private final CarSharingTariffSender carSharingTariffSender;
    
    private final GroupTransferTariffSender groupTransferTariffSender;
    
    private final GeoZoneRepository geoZoneRepository;
    
    private final ContractRepository contractRepository;
    
    private final DepartmentRepository departmentRepository;
    
    private final GroupTransferTariffRepository groupTransferTariffRepository;
    
    @Value("${tariff.export.onlyActive:false}")
    private boolean exportOnlyActive;
    
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public BaseTariff save(BaseTariff tariff) {
        Organization organization =
                organizationRepository.findById(tariff.getOrganization().getId())
                                      .orElseThrow(() -> new EntityNotFoundException(Organization.class, tariff.getOrganization().getId()));
        
        if (tariff.getDepartment() != null && tariff.getDepartment().getId() == null) {
            tariff.setDepartment(null);
        }
        Long organizationDigitId = organization.getDigitId();
        if (tariff.getHumanReadableId() == null) {
            String humanReadableId = sqGenerator.getNextId(Prefix.TF, organizationDigitId);
            tariff.setHumanReadableId(humanReadableId);
        }
        disableOldTariff(tariff);
        
        switch (tariff.getTransportType()) {
            case TAXI: {
                Contract contract =
                        contractRepository.findById(((BaseTariffWithContract) tariff).getContract().getId()).orElseThrow(
                                () -> new EntityNotFoundException(Contract.class, ((BaseTariffWithContract) tariff).getContract().getId()));
                TaxiTariff taxiTariff = (TaxiTariff) tariff;
                if (StringUtils.hasText(taxiTariff.getContractorTariffId())) {
                    taxiTariff.setContractorTariffId(taxiTariff.getContractorTariffId().trim().toLowerCase());
                }
                GeoZone geoZone = geoZoneRepository.findById(tariff.getRegionId()).orElseThrow(
                        () -> new EntityNotFoundException(GeoZone.class, tariff.getRegionId()));
                taxiTariff.setRegion(geoZone.getName());
                if (contract.getContractorId() != null) {
                    var contractor = contractorRepository.findById(contract.getContractorId());
                    if (contractor.isPresent()) {
                        var organizationName = organization.getName();
                        if (contract.getContractType().equals(ContractType.OUTCOME)) {
                            organizationName = AGGREGATOR_NAME;
                        }
                        // setting workgroup on every save. if workgroup is to be edited code needs rewriting.
                        if (contractor.get().getIntegrationType().equals(TaxiExternalIntegrationType.JSON_API_1_0.name())) {
                            taxiTariff.setWorkGroup(organizationName + "/перевозка_пассажиров/" + geoZone.getName());
                        } else {
                            taxiTariff.setWorkGroup(WorkGroup.builder().organizationName(organizationName)
                                                             .regionName(geoZone.getName())
                                                             .contractNumber(contract.getContractNumber())
                                                             .build().getFullName());
                        }
                    }
                }
                //Проставляется так, чтобы нормально мапились в сообщениях значения из связанных сущностей, иначе там только id
                taxiTariff.setContract(contract);
                taxiTariff = saveTaxiTariff(taxiTariff);
                taxiTariffSender.send(taxiTariff);
                return taxiTariff;
            }
            case PERSONAL:
                PersonalTariff personalTariff = savePersonalTariff((PersonalTariff) tariff);
                personalTariffSender.send(personalTariff);
                return personalTariff;
            case PUBLIC:
                var publicTariff = publicTariffRepository.saveAndFlush((PublicTariff) tariff);
                publicTariffSender.send(publicTariff);
                return publicTariff;
            case CARSHARING:
                CarSharingTariff carSharingTariff = carSharingTariffRepository.saveAndFlush((CarSharingTariff) tariff);
                carSharingTariffSender.send(carSharingTariff);
                return carSharingTariff;
            case BICYCLE:
                return bicycleTariffRepository.saveAndFlush((BicycleTariff) tariff);
            case SCOOTER:
                return scooterTariffRepository.saveAndFlush((ScooterTariff) tariff);
            case GROUP_TRANSFER:
                Contract contract =
                        contractRepository.findById(((BaseTariffWithContract) tariff).getContract().getId()).orElseThrow(
                                () -> new EntityNotFoundException(Contract.class, ((BaseTariffWithContract) tariff).getContract().getId()));
                var groupTransferTariff = groupTransferTariffRepository.save((GroupTransferTariff) tariff);
                if (StringUtils.hasText(groupTransferTariff.getContractorTariffId())) {
                    groupTransferTariff.setContractorTariffId(groupTransferTariff.getContractorTariffId().trim().toLowerCase());
                }
                GeoZone geoZone = geoZoneRepository.findById(tariff.getRegionId()).orElseThrow(
                        () -> new EntityNotFoundException(GeoZone.class, tariff.getRegionId()));
                groupTransferTariff.setRegion(geoZone.getName());
                var contractor = contractorRepository.findById(contract.getContractorId());
                if (contractor.isPresent()) {
                    // setting workgroup on every save. if workgroup is to be edited code needs rewriting.
                    if (contractor.get().getIntegrationType().equals(TaxiExternalIntegrationType.JSON_API_1_0.name())) {
                        groupTransferTariff.setWorkGroup(organization.getName() + "/перевозка_пассажиров/" + geoZone.getName());
                    } else {
                        groupTransferTariff.setWorkGroup(WorkGroup.builder().organizationName(organization.getName())
                                                                  .regionName(geoZone.getName())
                                                                  .contractNumber(contract.getContractNumber())
                                                                  .build().getFullName());
                    }
                } else {
                    throw new EntityNotFoundException(Contractor.class, contract.getContractorId());
                }
                //Проставляется так, чтобы нормально мапились в сообщениях значения из связанных сущностей, иначе там только id
                groupTransferTariff.setContract(contract);
                groupTransferTariffSender.send(groupTransferTariff);
                return tariff;
            default:
                return repository.saveAndFlush(tariff);
        }
    }
    
    @Override
    public void delete(BaseTariff tariff) {
        tariff.setActive(false);
        tariff = repository.save(tariff);
        switch (tariff.getTransportType()) {
            case TAXI -> taxiTariffSender.sendDeleted((TaxiTariff) tariff);
            case PERSONAL -> personalTariffSender.sendDeleted((PersonalTariff) tariff);
            case CARSHARING -> carSharingTariffSender.sendDeleted((CarSharingTariff) tariff);
            case PUBLIC -> publicTariffSender.sendDeleted((PublicTariff) tariff);
            case GROUP_TRANSFER -> groupTransferTariffSender.send((GroupTransferTariff) tariff);
        }
    }
    
    @Override
    public Optional<? extends BaseTariff> get(TransportTypeEnum transportType, UUID tariffId) {
        switch (transportType) {
            case TAXI:
                return taxiTariffRepository.findById(tariffId);
            case PERSONAL:
                return personalTariffRepository.findById(tariffId);
            case PUBLIC:
                return publicTariffRepository.findById(tariffId);
            case CARSHARING:
                return carSharingTariffRepository.findById(tariffId);
            case BICYCLE:
                return bicycleTariffRepository.findById(tariffId);
            case SCOOTER:
                return scooterTariffRepository.findById(tariffId);
            case GROUP_TRANSFER:
                return groupTransferTariffRepository.findById(tariffId);
            default:
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<TaxiTariff> findByDepartmentIdAndTransportTypeAndTaxiClassAndRegionIdAndActive(
            UUID departmentID, TransportTypeEnum transportTypeEnum, TaxiClass taxiClass, UUID regionId, boolean active
                                                                                                  ) {
        return taxiTariffRepository.findByDepartmentIdAndTransportTypeAndTaxiClassAndRegionIdAndActive(departmentID, transportTypeEnum, taxiClass,
                                                                                                       regionId, active);
    }
    
    @Override
    public Optional<? extends BaseTariff> get(UUID transportTypeId, UUID tariffId) {
        TransportTypeEnum transportTypeEnum = TransportTypeEnum.fromId(transportTypeId).orElseThrow(
                () -> new EntityNotFoundException(TransportTypeEnum.class, transportTypeId));
        if (transportTypeEnum == null) {
            return Optional.empty();
        }
        return get(transportTypeEnum, tariffId);
    }
    
    @Override
    public Optional<Department> getDepartmentByHumanReadableId(String humanReadableId) {
        return departmentRepository.findByHumanReadableId(humanReadableId);
    }
    
    @Override
    public Optional<Department> getDepartmentById(UUID id) {
        return departmentRepository.findById(id);
    }
    
    @Override
    public BaseTariff getById(UUID tariffId) {
        return repository.getById(tariffId);
    }
    
    @Override
    public Optional<BaseTariff> findById(UUID tariffId) {
        return repository.findById(tariffId);
    }
    
    @Override
    public List<? extends BaseTariff> getAll(UUID regionId, ContractType contractType) {
        return getAll(regionId, (Boolean) null, contractType);
    }
    
    @Override
    public List<? extends BaseTariff> getAll(UUID regionId, Boolean isActive, ContractType contractType) {
        return getAll(regionId, null, isActive, contractType);
    }
    
    @Override
    public List<BaseTariff> getAll(UUID regionId, UUID organizationId, Boolean isActive, ContractType contractType) {
        return getAll(regionId, organizationId, isActive, null, Collections.emptyList(), contractType);
    }
    
    @Override
    public List<BaseTariff> getAll(
            UUID regionId, UUID organizationId, Boolean isActive, TransportServiceType type, List<TransportTypeEnum> transportTypeList,
            ContractType contractType
                                  ) {
        Specification<BaseTariff> spec = (root, query, builder) -> {
            var predicate = builder.equal(builder.literal(1), 1);
            if (regionId != null) {
                predicate = builder.and(predicate, builder.equal(root.get(BaseTariff_.REGION_ID), regionId));
            }
            if (isActive != null) {
                predicate = builder.and(predicate, builder.equal(root.get(BaseTariff_.ACTIVE), isActive));
            }
            if (organizationId != null) {
                var organization = root.join(BaseTariff_.ORGANIZATION);
                predicate = builder.and(predicate, builder.equal(organization.get(Organization_.ID), organizationId));
            }
            if (type != null) {
                predicate = builder.and(predicate, builder.equal(root.get(BaseTariff_.SERVICE_TYPE), type));
            }
            if (transportTypeList != null && !transportTypeList.isEmpty()) {
                log.debug("Adding transportTypes to query, value -" + transportTypeList.stream().map(TransportTypeEnum::name).collect(
                        Collectors.joining(",")));
                predicate = builder.and(predicate, root.get(BaseTariff_.TRANSPORT_TYPE).in(transportTypeList));
            }
            if (contractType != null) {
                Root<BaseTariffWithContract> rootContract = builder.treat(root, BaseTariffWithContract.class);
                Join<BaseTariffWithContract, Contract> contractJoin = rootContract.join("contract");
                predicate = builder.and(predicate, builder.equal(contractJoin.get("contractType"), contractType));
            }
            return predicate;
        };
        List<BaseTariff> result = ReflectionUtils.cast(repository.findAll(spec));
        result.sort(Comparator.comparing(BaseTariff::getHumanReadableId));
        return result;
    }
    
    @Override
    public List<? extends BaseTariff> getAll(UUID transportTypeId, UUID regionId, ContractType contractType) {
        TransportTypeEnum transportTypeEnum = TransportTypeEnum.fromId(transportTypeId).orElseThrow(
                () -> new EntityNotFoundException(TransportTypeEnum.class, transportTypeId));
        if (transportTypeEnum == null) {
            return Collections.emptyList();
        }
        return getAll(transportTypeEnum, regionId, contractType);
    }
    
    @Override
    public List<? extends BaseTariff> getAll(TransportTypeEnum transportType, UUID regionId, ContractType contractType) {
        return getAll(transportType, regionId, null, contractType);
    }
    
    
    @Override
    public List<? extends BaseTariff> getAll(TransportTypeEnum transportType, UUID regionId, UUID organizationId, ContractType contractType) {
        return getAll(transportType, regionId, organizationId, null, null, null, contractType);
    }
    
    @Override
    public List<? extends BaseTariff> getAll(TransportTypeEnum transportType, UUID regionId, UUID organizationId, Boolean isActive, ContractType contractType) {
        return getAll(transportType, regionId, organizationId, isActive, null, null, contractType);
    }
    
    @Override
    public List<? extends BaseTariff> getAll(
            TransportTypeEnum transportType, UUID regionId, UUID organizationId, Boolean isActive,
            UUID contractorId, String humanReadableId, ContractType contractType
                                            ) {
        return getAll(transportType, regionId != null ? Collections.singletonList(regionId) : Collections.emptyList(), organizationId, isActive,
                      contractorId, humanReadableId, null, contractType);
    }
    
    @Override
    public List<? extends BaseTariff> getAll(TransportTypeEnum transportType, List<UUID> regionIds, UUID organizationId, Boolean isActive, ContractType contractType) {
        return getAll(transportType, regionIds, organizationId, isActive, null, null, null, contractType);
    }
    
    @Override
    public List<? extends BaseTariff> getAll(
            TransportTypeEnum transportType, List<UUID> regionIds, UUID organizationId, Boolean isActive,
            UUID contractorId, String humanReadableId, Boolean isNightTariff, ContractType contractType
                                            ) {
        return getAll(transportType, regionIds, organizationId, isActive, contractorId, humanReadableId, isNightTariff, null, null, null);
    }
    
    @Override
    public List<? extends BaseTariff> getAll(
            TransportTypeEnum transportType, List<UUID> regionIds, UUID organizationId, Boolean isActive,
            UUID contractorId, String humanReadableId, Boolean isNightTariff, String contractNumber, TransportClass transportClass,
            ContractType contractType
                                            ) {
        if(contractNumber != null && (PERSONAL == transportType || PUBLIC == transportType || WALK == transportType)) {
            return Collections.emptyList();
        }
        if (transportClass != null) {
            if (TAXI != transportType && GROUP_TRANSFER != transportType) {
                return Collections.emptyList();
            }
            if (TAXI == transportType && transportClass.getTaxiValue() == null) {
                return Collections.emptyList();
            }
            if (GROUP_TRANSFER == transportType && transportClass.getGroupTransferValue() == null) {
                return Collections.emptyList();
            }
        }
        
        Specification<BaseTariff> spec = (root, query, builder) -> {
            var predicate = builder.equal(builder.literal(1), 1);
            if (isActive != null || exportOnlyActive) {
                predicate = builder.and(predicate, builder.equal(root.get(BaseTariff_.active), Boolean.TRUE.equals(isActive) || exportOnlyActive));
            }
            if (regionIds != null && !regionIds.isEmpty()) {
                predicate = builder.and(predicate, root.get(BaseTariff_.regionId).in(regionIds));
            }
            if (organizationId != null) {
                var organization = root.join(BaseTariff_.organization);
                predicate = builder.and(predicate, builder.equal(organization.get(Organization_.id), organizationId));
            }
            if (contractNumber != null) {
                Root<BaseTariffWithContract> rootContract = builder.treat(root, BaseTariffWithContract.class);
                predicate = builder.and(predicate, builder.equal(rootContract.get(BaseTariffWithContract_.CONTRACT).get(Contract_.CONTRACT_NUMBER),
                                                                 contractNumber
                                                                ));
            }
            if (transportType != null) {
                predicate = builder.and(predicate, builder.equal(root.get(BaseTariff_.transportType), transportType));
            }
            if (transportClass != null) {
                if(TAXI == transportType) {
                    predicate = builder.and(predicate, getTaxiClassPredicate(transportClass, root, builder));
                }
                if(GROUP_TRANSFER == transportType) {
                    predicate = builder.and(predicate, getGroupTransferPredicate(transportClass, root, builder));
                }
            }
            try {
                if (contractorId != null && BaseTariffWithContract.class.isAssignableFrom(mapTransportTypeToTariffClass(transportType))) {
                    Root<BaseTariffWithContract> rootContract = builder.treat(root, BaseTariffWithContract.class);
                    Join<BaseTariffWithContract, Contract> contractJoin = rootContract.join(BaseTariffWithContract_.CONTRACT);
                    predicate = builder.and(predicate, builder.equal(contractJoin.get("contractorId"),
                                                                     contractorId));
                }
            } catch (Exception e) {
                log.error("Failed to add the filter by contractorId", e);
            }
            if (Optional.ofNullable(isNightTariff).orElse(false)) {
                Root<TaxiTariff> rootTaxi = builder.treat(root, TaxiTariff.class);
                predicate = builder.and(predicate, builder.equal(rootTaxi.get(TaxiTariff_.IS_NIGHT_TARIFF), true));
            }
            if (StringUtils.hasText(humanReadableId)) {
                predicate = builder.and(predicate, builder.like(builder.lower(
                        root.get(BaseTariff_.humanReadableId)), "%" + humanReadableId.toLowerCase(Locale.ROOT) +
                                                                "%"));
            }
            if (contractType != null && List.of(TransportTypeEnum.TAXI, TransportTypeEnum.CARSHARING).contains(transportType)) {
                Root<BaseTariffWithContract> rootContract = builder.treat(root, BaseTariffWithContract.class);
                Join<BaseTariffWithContract, Contract> contractJoin = rootContract.join("contract");
                predicate = builder.and(predicate, builder.equal(contractJoin.get("contractType"), contractType));
            }
            query.orderBy(builder.asc(root.get(BaseTariff_.HUMAN_READABLE_ID)));
            return predicate;
        };
        return switch (transportType) {
            case TAXI -> taxiTariffRepository.findAll((Specification<TaxiTariff>) ReflectionUtils.cast(spec));
            case PERSONAL -> personalTariffRepository.findAll((Specification<PersonalTariff>) ReflectionUtils.cast(spec));
            case CARSHARING -> carSharingTariffRepository.findAll((Specification<CarSharingTariff>) ReflectionUtils.cast(spec));
            case BICYCLE -> bicycleTariffRepository.findAll((Specification<BicycleTariff>) ReflectionUtils.cast(spec));
            case SCOOTER -> scooterTariffRepository.findAll((Specification<ScooterTariff>) ReflectionUtils.cast(spec));
            case PUBLIC -> publicTariffRepository.findAll((Specification<PublicTariff>) ReflectionUtils.cast(spec));
            case WALK -> walkTariffRepository.findAll();
            case GROUP_TRANSFER -> groupTransferTariffRepository.findAll((Specification<GroupTransferTariff>) ReflectionUtils.cast(spec));
            default -> Collections.emptyList();
        };
    }
    
    @Override
    public Page<? extends BaseTariff> search(TariffSearchDTO searchDTO, UUID organizationId, boolean dataMaster, Pageable pageable) {
        if (searchDTO == null) {
            return repository.findAll(TariffSearchSpecHelper.getSpecificationWithoutFilters(organizationId, dataMaster), pageable);
        }
        return repository.findAll(TariffSearchSpecHelper.getSpecification(searchDTO, organizationId, dataMaster), pageable);
    }

    private Predicate getGroupTransferPredicate(TransportClass transportClass, Root<BaseTariff> root, CriteriaBuilder builder) {
        return GroupTransferClass.getByName(transportClass.getGroupTransferValue()).map(mappedGroupTransfer -> {
            Root<GroupTransferTariff> groupTransferTariffRoot = builder.treat(root, GroupTransferTariff.class);
            return builder.equal(groupTransferTariffRoot.get(GroupTransferTariff_.GROUP_TRANSFER_CLASS), mappedGroupTransfer);
        }).orElse(null);
    }
    
    private Predicate getTaxiClassPredicate(TransportClass transportClass, Root<BaseTariff> root, CriteriaBuilder builder) {
        return TaxiClass.getByName(transportClass.getTaxiValue()).map(taxiClass -> {
            Root<TaxiTariff> taxiTariffRoot = builder.treat(root, TaxiTariff.class);
            return builder.equal(taxiTariffRoot.get(TaxiTariff_.TAXI_CLASS), taxiClass);
        }).orElse(null);
    }
    
    @Override
    public Page<? extends BaseTariff> search(TariffSearchDTO searchDTO, Pageable pageable) {
        return search(searchDTO, null, false, pageable);
    }
    
    @Override
    public Optional<? extends BaseTariff> get(TransportTypeEnum transportType, String humanReadableId) {
        return repository.findByTransportTypeAndHumanReadableId(transportType, humanReadableId);
    }
    
    @Override
    public List<TaxiTariff> getDepartmentUniqueTariff(UUID departmentId, TransportTypeEnum transportType) {
        return taxiTariffRepository.findAllByDepartmentIdAndTransportType(departmentId, transportType);
    }
    
    @Override
    public WorkGroup getWorkgroup(BaseTariffWithContract tariff) {
        var organization = organizationRepository.findById(tariff.getOrganization().getId())
                                                 .orElseThrow(
                                                         () -> new EntityNotFoundException(Organization.class, tariff.getOrganization().getId()));
        
        var contract = contractRepository.findById(tariff.getContract().getId()).orElseThrow(
                () -> new EntityNotFoundException(Contract.class, tariff.getContract().getId()));
        
        var geoZone = geoZoneRepository.findById(tariff.getRegionId()).orElseThrow(
                () -> new EntityNotFoundException(GeoZone.class, tariff.getRegionId()));
        
        return WorkGroup.builder()
                        .organizationName(organization.getName())
                        .regionName(geoZone.getName())
                        .contractNumber(contract.getContractNumber())
                        .build();
    }
    
    @Override
    public TaxiTariff newDtoToTaxiTariff(NewTaxiTariffDTO source) {
        return entityDTOMapper.newDtoToTaxiTariff(source);
    }
    
    @Override
    public TaxiTariffDTO taxiTariffToDTO(TaxiTariff source) {
        return entityDTOMapper.taxiTariffToDTO(source);
    }
    
    @Override
    public Map<UUID, List<GroupTransferTariff>> findConflictTariff(
            LocalDate tariffStartDate, LocalDate tariffEndDate, UUID contractorId, UUID organizationId, Set<UUID> regionIds, Set<UUID> transportIds
                                                                  ) {
        var conflictTariff = groupTransferTariffRepository.findConflictTariff(tariffStartDate, tariffEndDate, organizationId, contractorId,
                                                                              ConvertUtils.setToStringArray(regionIds),
                                                                              ConvertUtils.setToStringArray(transportIds));
        var map = new HashMap<UUID, List<GroupTransferTariff>>();
        for (var tariff : conflictTariff) {
            for (var transport : tariff.getTransportIds()) {
                var list = map.getOrDefault(transport, new ArrayList<>());
                list.add(tariff);
                map.put(transport, list);
            }
        }
        return map;
    }
    
    @Override
    public boolean canReportBeGenerated(@NonNull TariffSearchDTO dto, @NonNull TransportTypeEnum transportType) {
        //Отчёт должен быть сформирован, если совпадает тип транспорта или сервиса или фильтры отсутствует
        return ((dto.getTransportType() == null || transportType.equals(dto.getTransportType())) &&
                (dto.getServiceType() == null || transportType.getServiceType().equals(dto.getServiceType())));
    }
    
    @Override
    public void resend() {
        log.info("Начало повторной отправки тарифов {}", LocalDateTime.now());
        var tariffs = repository.findAll();
        log.info("Найдено {} тарифов для повторной отправки", tariffs.size());
        tariffs.forEach(tariff -> {
            switch (tariff.getTransportType()) {
                case TAXI -> taxiTariffSender.send((TaxiTariff) tariff);
                case GROUP_TRANSFER -> groupTransferTariffSender.send((GroupTransferTariff) tariff);
                case PERSONAL -> personalTariffSender.send((PersonalTariff) tariff);
                case PUBLIC -> publicTariffSender.send((PublicTariff) tariff);
                case CARSHARING -> carSharingTariffSender.send((CarSharingTariff) tariff);
                default -> log.warn("Для тарифа {} {} нет имплементации отправителя", tariff.getId(), tariff.getTransportType());
            }
        });
        log.info("Окончание повторной отправки тарифов {}", LocalDateTime.now());
    }
    
    private Class<? extends BaseTariff> mapTransportTypeToTariffClass(TransportTypeEnum transportType) {
        switch (transportType) {
            case TAXI:
                return TaxiTariff.class;
            case PERSONAL:
                return PersonalTariff.class;
            case CARSHARING:
                return CarSharingTariff.class;
            case BICYCLE:
                return BicycleTariff.class;
            case SCOOTER:
                return ScooterTariff.class;
            case PUBLIC:
                return PublicTariff.class;
            case WALK:
                return WalkTariff.class;
            case GROUP_TRANSFER:
                return GroupTransferTariff.class;
            default:
        }
        throw new IllegalArgumentException("This type of transport is not handled");
    }
    
    private TaxiTariff saveTaxiTariff(TaxiTariff tariff) {
        return taxiTariffRepository.save(tariff);
    }
    
    private PersonalTariff savePersonalTariff(PersonalTariff tariff) {
        return personalTariffRepository.save(tariff);
    }
    
    //Проверка уникальности и отключение тарифа по списку параметров, соответствующему типу транспорта
    private void disableOldTariff(BaseTariff tariff) {
        Specification<? extends BaseTariff> spec = (root, query, builder) -> {
            var predicate = builder.equal(builder.literal(1), 1);
            if (tariff.getDepartment() != null && tariff.getDepartment().getId() != null) {
                predicate = builder.and(predicate, builder.equal(root.get(BaseTariff_.DEPARTMENT), tariff.getDepartment()));
            }
            if (tariff.getId() != null) {
                predicate = builder.and(predicate, builder.notEqual(root.get(BaseTariff_.ID), tariff.getId()));
            }
            if (!TransportTypeEnum.INTERREGIONAL.equals(tariff.getTransportType())
                && !TransportTypeEnum.COURIER.equals(tariff.getTransportType())
                && !TransportTypeEnum.DOMESTIC_COURIER.equals(tariff.getTransportType())
                && tariff.getRegionId() != null) {
                predicate =
                        builder.and(predicate, builder.equal(root.get(BaseTariff_.REGION_ID), tariff.getRegionId()));
            }
            var organization = root.join(BaseTariff_.organization);
            predicate = builder.and(predicate, builder.equal(organization.get(Organization_.id),
                                                             tariff.getOrganization().getId()));
            predicate = builder.and(predicate, builder.equal(root.get(BaseTariff_.ACTIVE), true));
            if (tariff.getTransportType() == TAXI) {
                predicate = builder.and(predicate, builder.equal(root.get(TaxiTariff_.IS_NIGHT_TARIFF), ((TaxiTariff) tariff).getIsNightTariff()));
                SingularAttribute contract = TaxiTariff_.contract;
                root.fetch(contract, JoinType.INNER);
                TaxiTariff taxiTariff = (TaxiTariff) tariff;
                Join<BaseTariff, Contract> contractJoin = root.join(contract);
                predicate =
                        builder.and(predicate, builder.equal(contractJoin.get(Contract_.id),
                                                             ((TaxiTariff) tariff).getContract().getId()));
                predicate = builder.and(predicate, builder.equal(root.get(TaxiTariff_.TAXI_CLASS),
                                                                 taxiTariff.getTaxiClass()));
            }
            return predicate;
        };
        if (tariff.getId() == null || tariff.isActive()) {
            List<? extends BaseTariff> sameUniqueFieldsList;
            switch (tariff.getTransportType()) {
                case TAXI -> {
                    sameUniqueFieldsList =
                            taxiTariffRepository.findAll((Specification<TaxiTariff>) ReflectionUtils.cast(spec));
                    for (var taxiTariff : sameUniqueFieldsList) {
                        taxiTariff.setActive(false);
                        taxiTariffRepository.save((TaxiTariff) taxiTariff);
                        log.info("Отключен тариф для такси {}, так как загружается похожий тариф {}",
                                 taxiTariff.getHumanReadableId(), tariff.getHumanReadableId());
                    }
                }
                case PERSONAL -> {
                    sameUniqueFieldsList =
                            personalTariffRepository.findAll(
                                    (Specification<PersonalTariff>) ReflectionUtils.cast(spec));
                    for (var personalTariff : sameUniqueFieldsList) {
                        personalTariff.setActive(false);
                        personalTariffRepository.save((PersonalTariff) personalTariff);
                        log.info("Отключен тариф для личного транспорта {}, так как загружается похожий тариф {}",
                                 personalTariff.getHumanReadableId(), tariff.getHumanReadableId());
                    }
                }
                case PUBLIC -> {
                    sameUniqueFieldsList =
                            publicTariffRepository.findAll((Specification<PublicTariff>) ReflectionUtils.cast(spec));
                    for (var publicTariff : sameUniqueFieldsList) {
                        publicTariff.setActive(false);
                        publicTariffRepository.save((PublicTariff) publicTariff);
                        log.info("Отключен тариф для общественного транспорта {}, так как загружается похожий тариф {}",
                                 publicTariff.getHumanReadableId(), tariff.getHumanReadableId());
                    }
                }
                case BICYCLE -> {
                    sameUniqueFieldsList =
                            bicycleTariffRepository.findAll((Specification<BicycleTariff>) ReflectionUtils.cast(spec));
                    for (var bicycleTariff : sameUniqueFieldsList) {
                        bicycleTariff.setActive(false);
                        bicycleTariffRepository.save((BicycleTariff) bicycleTariff);
                        log.info("Отключен тариф для велосипеда {}, так как загружается похожий тариф {}",
                                 bicycleTariff.getHumanReadableId(), tariff.getHumanReadableId());
                    }
                }
                case WALK, SCOOTER -> {
                    sameUniqueFieldsList =
                            scooterTariffRepository.findAll((Specification<ScooterTariff>) ReflectionUtils.cast(spec));
                    for (var scooterTariff : sameUniqueFieldsList) {
                        scooterTariff.setActive(false);
                        scooterTariffRepository.save((ScooterTariff) scooterTariff);
                        log.info("Отключен тариф для самоката {}, так как загружается похожий тариф {}",
                                 scooterTariff.getHumanReadableId(), tariff.getHumanReadableId());
                    }
                }
                case CARSHARING -> {
                    sameUniqueFieldsList =
                            carSharingTariffRepository.findAll((Specification<CarSharingTariff>) ReflectionUtils.cast(spec));
                    for (var carSharingTariff : sameUniqueFieldsList) {
                        carSharingTariff.setActive(false);
                        carSharingTariffRepository.save((CarSharingTariff) carSharingTariff);
                        log.info("Отключен тариф для каршеринга {}, так как загружается похожий тариф {}",
                                 carSharingTariff.getHumanReadableId(), tariff.getHumanReadableId());
                    }
                }
                case GROUP_TRANSFER -> {
//                    sameUniqueFieldsList =
//                            groupTransferTariffRepository.findAll((Specification<GroupTransferTariff>) ReflectionUtils.cast(spec));
//                    for (var groupTransferTariff : sameUniqueFieldsList) {
//                        groupTransferTariff.setActive(false);
//                        groupTransferTariffRepository.save((GroupTransferTariff) groupTransferTariff);
//                        log.info("Отключен тариф для для группового трансфера {}, так как загружается похожий тариф {}",
//                                 groupTransferTariff.getHumanReadableId(), tariff.getHumanReadableId());
//                    }
                }
                default -> {
                }
            }
        }
    }
}
