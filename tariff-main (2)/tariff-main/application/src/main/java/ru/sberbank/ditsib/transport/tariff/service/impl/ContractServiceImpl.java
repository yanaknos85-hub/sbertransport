package ru.sberbank.ditsib.transport.tariff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.tariff.database.dao.ContractRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.ContractorRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.dto.ContractSearchDTO;
import ru.sberbank.ditsib.transport.tariff.dto.TariffSearchDTO;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.ContractSender;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractServiceImpl implements ContractService {
    
    private final ContractRepository contractRepository;
    private final TariffService tariffService;
    
    private final ContractorRepository contractorRepository;
    private final ContractSender contractSender;
    
    @Override
    public void checkExpiredContracts() {
        // find all active contracts
        List<Contract> contractList = contractRepository.findByActive(true);
        // run in loop - if one is expired - mark it inactive and save
        for (Contract contract : contractList) {
            if ((contract.getEndDate() != null) && (contract.getEndDate().isBefore(LocalDate.now()))) {
                contract.setActive(false);
                contractRepository.saveAndFlush(contract);
            }
        }
    }
    
    @Override
    @Transactional
    public Contract add(final Contract contract) {
        contract.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        Contract result;
        try {
            result = contractRepository.save(contract);
            contractSender.send(result, false);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return result;
    }
    
    @Override
    @Transactional
    public Contract save(Contract contract) {
        try {
            if (contract.getCreationTime() == null) {
                contract.setCreationTime(LocalDateTime.now(ZoneOffset.UTC));
            }
            contract = contractRepository.save(contract);
            Page<? extends BaseTariff> linkedTariff =
                    tariffService.search(TariffSearchDTO.builder().contractId(contract.getId()).build(),
                                         PageRequest.of(0, Integer.MAX_VALUE));
            if (linkedTariff.getTotalElements() > 0) {
                Contract finalContract = contract;
                linkedTariff.stream().filter(BaseTariffWithContract.class::isInstance).forEach(elt -> {
                    ((BaseTariffWithContract) elt).setContract(finalContract);
                    tariffService.save(elt);
                });
            }
            contractSender.send(contract, !contract.isActive());
            return contract;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void delete(Contract contract) {
        try {
            contract.setActive(false);
            contract = save(contract);
            contractSender.send(contract, true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public Contract get(UUID id) {
        return contractRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(Contract.class, id));
    }
    
    @Override
    public List<Contract> getAll() {
        return contractRepository.findAll();
    }
    
    @Override
    public List<Contract> getAllByContractType(ContractType contractType) {
        return contractRepository.findAllByContractType(contractType);
    }
    
    @Override
    public List<Contract> getByContractor(UUID contractorId) {
        return contractRepository.findByContractorIdAndActiveTrue(contractorId);
    }
    
    @Override
    public Page<String> getUniqueUvhd(UUID contractorId, String uvhd, TransportTypeEnum transportType, Pageable pageable) {
        return contractRepository.getUniqueUvhd(contractorId, "%%%s%%".formatted(uvhd), transportType, pageable);
    }
    
    @Override
    public Page<Contract> search(ContractSearchDTO searchDTO, Pageable pageable) {
        if (searchDTO == null) {
            return contractRepository
                    .findAll((Specification<Contract>) (root, query, builder) -> builder.and(), pageable);
        }
        return contractRepository.findAll((Specification<Contract>) (root, query, builder) -> {
            Predicate predicate = builder.and();
            query.distinct(true);
            if (searchDTO.getServiceType() != null) {
                predicate = builder.and(predicate, builder.equal(root.get(Contract_.serviceType),
                                                                 searchDTO.getServiceType()));
            }
            if (searchDTO.getTransportType() != null) {
                predicate = builder.and(predicate, builder.equal(root.get(Contract_.transportType),
                                                                 searchDTO.getTransportType()));
            }
            if (searchDTO.getContractorId() != null) {
                predicate = builder.and(predicate, builder.equal(root.get(Contract_.contractorId),
                                                                 searchDTO.getContractorId()));
            }
            
            if (searchDTO.getOrganizationId() != null) {
                Join<Contract, Organization> organizationJoin = root.join(Contract_.organizations);
                predicate = builder.and(predicate, builder.equal(organizationJoin.get(Organization_.id),
                                                                 searchDTO.getOrganizationId()));
            }
            if (searchDTO.getActive() != null) {
                predicate = builder.and(predicate, builder.equal(
                        root.get(Contract_.active), searchDTO.getActive()));
            }
            if (searchDTO.getRegion() != null) {
                var regionIds = root.join(Contract_.regionIds);
                predicate = builder.and(predicate,
                                        builder.equal(regionIds, searchDTO.getRegion()));
            }
            if (searchDTO.getRegionIds() != null) {
                var regionIds = root.join(Contract_.regionIds);
                predicate = builder.and(predicate, regionIds.in(searchDTO.getRegionIds()));
            }
            if (StringUtils.hasText(searchDTO.getContractNumber())) {
                predicate = builder.and(predicate, builder.like(builder.lower(
                        root.get(Contract_.contractNumber)), searchDTO.getContractNumber().toLowerCase(Locale.ROOT)));
            }
            if (searchDTO.getStartDate() != null) {
                predicate =
                        builder.and(predicate, builder.greaterThanOrEqualTo(root.get(Contract_.endDate),
                                                                            searchDTO.getStartDate()));
            }
            if (searchDTO.getEndDate() != null) {
                predicate =
                        builder.and(predicate,
                                    builder.lessThanOrEqualTo(root.get(Contract_.startDate), searchDTO.getEndDate()));
            }
            if (searchDTO.getContractType() != null) {
                predicate =
                        builder.and(predicate,
                                    builder.equal(root.get(Contract_.contractType), searchDTO.getContractType()));
            }
            query.orderBy(builder.asc(root.get(Contract_.startDate)));
            return predicate;
        }, pageable);
    }
    
    @Override
    public Optional<Contract> get(UUID contractorId, String contractNumber) {
        return contractRepository.findByContractorIdAndContractNumberAndActiveTrue(contractorId, contractNumber);
    }
    
    @Override
    public Optional<Contract> get(String contractorName, String contractNumber) {
        var contractor = contractorRepository.findByName(contractorName)
                                             .orElseThrow(() -> new EntityNotFoundException(Contractor.class,
                                                                                            contractorName));
        return contractRepository.findByContractorIdAndContractNumberAndActiveTrue(contractor.getId(), contractNumber);
    }
    
    @Override
    public List<Contract> get(List<UUID> ids) {
        return contractRepository.findAllById(ids);
    }
    
    @Override
    public void resend() {
        log.info("Начало повторной отправки всех контрактов {}", LocalDateTime.now());
        var contracts = contractRepository.findAll();
        log.info("Найдено {} контрактов для повторной отправки", contracts.size());
        contracts.forEach(contract -> contractSender.send(contract, !contract.isActive()));
        log.info("Завершение повторной отправки всех контрактов {}", LocalDateTime.now());
    }
}
