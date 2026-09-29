package ru.sberbank.ditsib.transport.tariff.controller.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.controller.ContractController;
import ru.sberbank.ditsib.transport.tariff.database.dao.ConnectionRestrictionRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.ContractDTO;
import ru.sberbank.ditsib.transport.tariff.dto.ContractSearchDTO;
import ru.sberbank.ditsib.transport.tariff.dto.GetContractDTO;
import ru.sberbank.ditsib.transport.tariff.dto.TariffSearchDTO;
import ru.sberbank.ditsib.transport.tariff.exceptions.ContractLogicException;
import ru.sberbank.ditsib.transport.tariff.exceptions.PaymentOrganizationException;
import ru.sberbank.ditsib.transport.tariff.mappers.ContractMapper;
import ru.sberbank.ditsib.transport.tariff.service.*;
import ru.sberbank.ditsib.transport.tariff.util.VatTypeValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of limit controller service.
 */
@RequiredArgsConstructor
@RestController
@Transactional
public class ContractControllerImpl implements ContractController {

    private final ContractorService contractorService;

    private final ContractService contractService;

    private final OrganizationService organizationService;

    private final TariffService tariffService;

    private final GeoZoneService geoZoneService;

    private final ConnectionRestrictionRepository connectionRestrictionRepository;

    private final EmployeeService employeeService;

    private final ContractMapper contractMapper;

    @Override
    public GetContractDTO add(ContractDTO contractDTO, JwtAuthenticationToken authentication) {
        checkCorrectVatValue(contractDTO.getVatValue());

        if (contractDTO.getOrganizationIds() == null) {
            contractDTO.setOrganizationIds(List.of());
        }

        var userId = UUID.fromString(authentication.getToken().getId());

        HashSet<Organization> organizations = new HashSet<>();
        for (UUID organizationId : contractDTO.getOrganizationIds()) {
            Organization organization = organizationService.get(organizationId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            Organization.class,
                            organizationId));
            organizations.add(organization);
        }

        var contractType = getContractType(contractDTO, organizations);
        checkCorrectPaymentOrganization(contractDTO, contractType);

        for (Organization organization : organizations) {
            ContractSearchDTO contractSearchDTO = ContractSearchDTO.builder()
                    .organizationId(organization.getId())
                    .serviceType(
                            TransportServiceType.EMPLOYEE_TRANSPORTATION)
                    .transportType(contractDTO.getTransportType())
                    .region(contractDTO.getRegionIds() == null ? null :
                            contractDTO.getRegionIds().stream()
                                    .findFirst().orElse(null))
                    .contractorId(contractDTO.getContractorId())
                    .active(true)
                    .contractType(contractType)
                    .build();
            Page<Contract> contractPage = contractService.search(contractSearchDTO,
                    PageRequest.of(0, Integer.MAX_VALUE));
            // если уже есть хотя бы один активный контракт с пересекающимися датами - отклонить новый
            for (Contract contract : contractPage) {
                LocalDate existStartDate = contract.getStartDate() != null ? contract.getStartDate() : LocalDate.MIN;
                LocalDate existEndDate = contract.getEndDate() != null ? contract.getEndDate() : LocalDate.MAX;
                LocalDate newStartDate = contractDTO.getStartDate() != null ? contractDTO.getStartDate() : LocalDate.MIN;
                LocalDate newEndDate = contractDTO.getEndDate() != null ? contractDTO.getEndDate() : LocalDate.MAX;
                if (!((newStartDate.isBefore(existStartDate) && newEndDate.isBefore(existStartDate)) ||
                        (newStartDate.isAfter(existEndDate) && newEndDate.isAfter(existEndDate)))) {
                    throw new ContractLogicException("Даты нового договора накладываются на договор " + contract.getContractNumber());
                }
            }
        }
        ContractSearchDTO contractSearchDTO = ContractSearchDTO.builder()
                .contractNumber(contractDTO.getContractNumber())
                .build();
        Page<Contract> contractPage = contractService.search(contractSearchDTO,
                PageRequest.of(0, Integer.MAX_VALUE));
        if (contractPage.hasContent()) {
            throw new ContractLogicException("Номер договора не уникален!");
        }

        var regionIds = contractDTO.getRegionIds();
        if (!regionIds.isEmpty()) {
            for (UUID regionId : regionIds) {
                Optional<GeoZone> geoZone = geoZoneService.get(regionId);
                geoZone.orElseThrow(() -> new EntityNotFoundException(GeoZone.class, regionId));
            }
        }

        var contract = createContract(contractDTO, organizations, regionIds, userId, contractType);
        contract = contractService.add(contract);

        updateRestrictions(contract, contractDTO.getRestrictedIds());
        contractorService.updateContractorsRegionIds(contract);

        return transformEntityToDTO(contract);
    }

    @NotNull
    private Contract createContract(ContractDTO contractDTO, HashSet<Organization> organizations, Set<UUID> regionIds, UUID userId, ContractType contractType) {
        var contract = contractMapper.toContract(contractDTO);
        contract.setOrganizations(organizations);
        contract.setRegionIds(regionIds);
        contract.setUserId(userId);
        contract.setContractType(contractType);
        contract.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));

        var responsibleEmployeeId = Optional.ofNullable(contractDTO.getResponsibleEmployeeId())
                .orElseGet(() -> employeeService.getByUserId(userId)
                        .orElseThrow(() -> new EntityNotFoundException(Employee.class, userId))
                        .getId());
        contract.setResponsibleEmployeeId(responsibleEmployeeId);

        if (contract.getContractType().equals(ContractType.TRANSITIONAL) && contract.getUvhd() == null) {
            throw new ContractLogicException("Для транзитных договоров УВХД должен быть заполнен");
        }
        return contract;
    }

    private static void checkCorrectPaymentOrganization(ContractDTO contractDTO, ContractType contractType) {
        if (!ContractType.TRANSITIONAL.equals(contractType)) {
            return;
        }

        if (contractDTO.getTransportType().equals(TransportTypeEnum.TAXI) && contractDTO.getPaymentOrganizationId() == null) {
            throw new PaymentOrganizationException("Для договора с типом Такси необходимо указать организацию, ответственную за оплату");
        }
    }

    private static void checkCorrectVatValue(Integer vatValueInput) {
        VatTypeValidator.getByValue(vatValueInput);
    }

    @Override
    public void edit(
            UUID contractId, ContractDTO contractDTO, JwtAuthenticationToken authentication
    ) {
        checkCorrectVatValue(contractDTO.getVatValue());

        var contract = contractService.get(contractId);
        checkCorrectPaymentOrganization(contractDTO, contract.getContractType());
        contract.setSum(contractDTO.getSum());
        contract.setContractNumber(contractDTO.getContractNumber());
        contract.setIncludeVat(contractDTO.isIncludeVat());
        contract.setVatValue(contractDTO.getVatValue());
        contract.setRegionIds(contractDTO.getRegionIds());
        contract.setUvhd(contractDTO.getUvhd());
        contract.setStartDate(contractDTO.getStartDate());
        contract.setEndDate(contractDTO.getEndDate());
        contract.setRestrictionType(contractDTO.getRestrictionType());
        updateRestrictions(contract, contractDTO.getRestrictedIds());
        contract.setDriverLatePickupPenalty(contractDTO.getDriverLatePickupPenalty());
        contract.setPoorServiceQualityPenalty(contractDTO.getPoorServiceQualityPenalty());
        contract.setDriverOrderCancellationPenalty(contractDTO.getDriverOrderCancellationPenalty());
        var responsibleEmployeeId = Optional.ofNullable(contractDTO.getResponsibleEmployeeId()).orElse(contract.getUserId());
        contract.setResponsibleEmployeeId(responsibleEmployeeId);
        contract.setPaymentOrganizationId(contractDTO.getPaymentOrganizationId());
        contractService.save(contract);
    }

    @Override
    public void delete(UUID contractId, JwtAuthenticationToken authentication) {
        Contract contract = contractService.get(contractId);
        if (isTariffsExist(contract)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Удаление не возможно: к договору привязаны активные тарифы!");
        }
        contractService.delete(contract);
    }

    @Override
    public GetContractDTO get(UUID contractId) {
        Contract contract = contractService.get(contractId);
        return transformEntityToDTO(contract);
    }

    @Override
    public Page<String> getUniqueUvhd(UUID contractorId, String uvhd, TransportTypeEnum transportType, Pageable pageable) {
        var checkedUvhd = Objects.nonNull(uvhd) ? uvhd : "";
        return contractService.getUniqueUvhd(contractorId, checkedUvhd, transportType, pageable);
    }

    @Override
    public List<? extends GetContractDTO> getAll(ContractType contractType) {
        List<Contract> list = contractService.getAllByContractType(contractType);
        return list.stream().map(this::transformEntityToDTO).collect(Collectors.toList());
    }

    @Override
    public Page<? extends GetContractDTO> search(ContractSearchDTO searchDTO, Pageable pageable) {
        if (searchDTO.getContractType() == null) {
            searchDTO = searchDTO.toBuilder().contractType(ContractType.TRANSITIONAL).build();
        }
        Page<Contract> page = contractService.search(searchDTO, pageable);
        return new PageImpl<>(page.stream().map(this::transformEntityToDTO)
                .collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    private void updateRestrictions(Contract contract, List<UUID> restrictedIds) {
        if (contract.getContractType() == ContractType.TRANSITIONAL) {
            return;
        }

        if (contract.getRestrictionType() == RestrictionType.NONE) {
            if (contract.getContractType() == ContractType.INCOME) {
                var organizationsIds = contract.getOrganizations().stream().map(Organization::getId).toList();
                var connections = connectionRestrictionRepository.findByOrganizationIdIn(organizationsIds);
                connections = connections.stream().filter(q -> q.getRestrictionSource().equals(RestrictionSource.CUSTOMER)).toList();
                connectionRestrictionRepository.deleteAll(connections);
            } else if (contract.getContractType() == ContractType.OUTCOME) {
                var connections = connectionRestrictionRepository.findByAndContractorId(contract.getContractorId());
                connections = connections.stream().filter(q -> q.getRestrictionSource().equals(RestrictionSource.EXECUTOR)).toList();
                connectionRestrictionRepository.deleteAll(connections);
            }
            return;
        }

        if (contract.getContractType() == ContractType.INCOME) {
            updateIncomeRestrictions(restrictedIds, contract.getOrganizations().iterator().next());
        } else if (contract.getContractType() == ContractType.OUTCOME) {
            updateOutcomeRestrictions(restrictedIds, contract.getContractorId());
        }
    }

    private void updateIncomeRestrictions(List<UUID> contractorIds, Organization organization) {
        var currentRestrictions = connectionRestrictionRepository.findByOrganizationId(organization.getId());
        var currentRestrictedIds = currentRestrictions.stream().map(ConnectionRestriction::getContractor).map(Contractor::getId).toList();

        var toDelete = new LinkedList<>(currentRestrictedIds);
        var toAdd = new LinkedList<>(contractorIds);

        toDelete.removeAll(contractorIds);
        toAdd.removeAll(currentRestrictedIds);

        connectionRestrictionRepository.deleteByContractorIdsAndOrganizationId(toDelete, organization.getId());

        toAdd.forEach(restrictedId -> createRestriction(organization, restrictedId, RestrictionSource.CUSTOMER));
    }

    private void updateOutcomeRestrictions(List<UUID> organizationIds, UUID contractorId) {
        var currentRestrictions = connectionRestrictionRepository.findByAndContractorId(contractorId);
        var currentRestrictedIds = currentRestrictions.stream().map(ConnectionRestriction::getOrganization).map(Organization::getId).toList();

        var toDelete = new LinkedList<>(currentRestrictedIds);
        var toAdd = new LinkedList<>(organizationIds);

        toDelete.removeAll(organizationIds);
        toAdd.removeAll(currentRestrictedIds);

        connectionRestrictionRepository.deleteByOrganizationIdsAndContractorId(toDelete, contractorId);

        toAdd.forEach(restrictedId -> {
            var restrictedOrganization =
                    organizationService.get(restrictedId).orElseThrow(() -> new EntityNotFoundException(Organization.class, restrictedId));
            createRestriction(restrictedOrganization, contractorId, RestrictionSource.EXECUTOR);
        });
    }

    private void createRestriction(Organization organization, UUID contractorId, RestrictionSource restrictionSource) {
        var contractor = contractorService.get(contractorId)
                .orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractorId));
        var newRestriction = new ConnectionRestriction(null, organization, contractor, restrictionSource);
        connectionRestrictionRepository.save(newRestriction);
    }

    private static ContractType getContractType(ContractDTO contractDTO, HashSet<Organization> organizations) {
        ContractType contractType;
        if (organizations.isEmpty() && contractDTO.getContractorId() != null) {
            contractType = ContractType.OUTCOME;
        } else if (!organizations.isEmpty() && contractDTO.getContractorId() == null) {
            contractType = ContractType.INCOME;
        } else if (!organizations.isEmpty()) {
            contractType = ContractType.TRANSITIONAL;
        } else {
            throw new ContractLogicException("Организация и контрагент не могут одновременно быть пустыми");
        }

        if (contractType != ContractType.TRANSITIONAL && organizations.size() > 1) {
            throw new ContractLogicException("Не тразитный договор не может относится к более чем 1 организации");
        }

        return contractType;
    }

    private GetContractDTO transformEntityToDTO(Contract contract) {
        if (contract == null) {
            return null;
        }
        var getContractDTO = contractMapper.toGetContractDTO(contract);
        var contractor = contractorService.get(contract.getContractorId());
        contractor.ifPresent(value -> getContractDTO.setContractorName(value.getName()));
        getContractDTO.setOrganizationIds(contract.getOrganizations().stream()
                .map(Organization::getId)
                .collect(Collectors.toList()));
        getContractDTO.setOrganizationNames(contract.getOrganizations().stream()
                .map(Organization::getName)
                .collect(Collectors.toList()));
        if (contract.getRegionIds() != null && !contract.getRegionIds().isEmpty()) {
            Optional<GeoZone> geoZone = geoZoneService.get(contract.getRegionIds().iterator().next());
            geoZone.ifPresent(elt -> getContractDTO.setRegion(elt.getName()));
        }
        var restrictedConnections = switch (contract.getContractType()) {
            case INCOME -> connectionRestrictionRepository
                    .findByOrganizationIdIn(getContractDTO.getOrganizationIds());
            case OUTCOME -> connectionRestrictionRepository
                    .findByAndContractorId(contract.getContractorId());
            default -> new LinkedList<ConnectionRestriction>();
        };
        var filteredRestrictedIds = switch (contract.getContractType()) {
            case INCOME -> restrictedConnections
                    .stream()
                    .filter(q -> q.getRestrictionSource().equals(RestrictionSource.CUSTOMER))
                    .map(ConnectionRestriction::getContractor)
                    .map(Contractor::getId)
                    .toList();
            case OUTCOME -> restrictedConnections
                    .stream()
                    .filter(q -> q.getRestrictionSource().equals(RestrictionSource.EXECUTOR))
                    .map(ConnectionRestriction::getOrganization)
                    .map(Organization::getId)
                    .toList();
            default -> null;
        };
        getContractDTO.setRestrictedIds(filteredRestrictedIds);
        var diffContractType = switch (contract.getContractType()) {
            case INCOME -> ContractType.OUTCOME;
            case OUTCOME -> ContractType.INCOME;
            default -> null;
        };
        if (diffContractType != null) {
            var restrictedIds = switch (contract.getContractType()) {
                case INCOME -> restrictedConnections
                        .stream()
                        .map(ConnectionRestriction::getContractor)
                        .map(Contractor::getId)
                        .toList();
                case OUTCOME -> restrictedConnections
                        .stream()
                        .map(ConnectionRestriction::getOrganization)
                        .map(Organization::getId)
                        .toList();
                default -> null;
            };
            var searchDTO = ContractSearchDTO.builder()
                    .contractType(diffContractType)
                    .regionIds(contract.getRegionIds().stream().toList())
                    .active(true)
                    .build();
            var diffContractsPage = contractService.search(searchDTO, PageRequest.of(0, 1000));
            List<String> diffContracts;
            if (diffContractType == ContractType.INCOME) {
                diffContracts = diffContractsPage
                        .get()
                        .filter(c -> c.getOrganizations().stream().map(Organization::getId)
                                .anyMatch(id -> restrictedIds.stream().noneMatch(id::equals)))
                        .map(Contract::getContractNumber).toList();
            } else {
                diffContracts = diffContractsPage
                        .get()
                        .filter(c -> !restrictedIds.contains(c.getContractorId()))
                        .map(Contract::getContractNumber).toList();
            }
            getContractDTO.setConnectedContracts(diffContracts);
        }

        if (contract.getResponsibleEmployeeId() != null) {
            var responsibleEmployeeName = employeeService.get(contract.getResponsibleEmployeeId())
                    .map(e -> Stream.of(e.getLastName(), e.getFirstName(), e.getPatronymic())
                            .filter(Objects::nonNull).collect(Collectors.joining(" ")))
                    .orElseThrow(() -> new EntityNotFoundException(Employee.class, contract.getResponsibleEmployeeId()));
            getContractDTO.setResponsibleEmployeeId(contract.getResponsibleEmployeeId());
            getContractDTO.setResponsibleEmployeeName(responsibleEmployeeName);
        }
        return getContractDTO;
    }

    private boolean isTariffsExist(Contract contract) {
        TariffSearchDTO tariffSearchDTO = TariffSearchDTO.builder()
                .contractId(contract.getId())
                .build();
        Page<? extends BaseTariff> tariffPage = tariffService.search(tariffSearchDTO, null, false,
                PageRequest.of(0, Integer.MAX_VALUE));
        return !tariffPage.filter(BaseTariff::isActive).toList().isEmpty();
    }
}