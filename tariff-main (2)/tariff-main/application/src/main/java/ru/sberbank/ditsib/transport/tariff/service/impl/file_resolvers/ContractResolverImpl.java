package ru.sberbank.ditsib.transport.tariff.service.impl.file_resolvers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.file_works.importer.DataImporter;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.tariff.database.dao.ContractorRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.Contractor;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.files.ContractFileDto;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;
import ru.sberbank.ditsib.transport.tariff.service.ContractorService;
import ru.sberbank.ditsib.transport.tariff.service.OrganizationService;

import java.util.*;

/**
 * Реализация разбора договоров.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ContractResolverImpl implements GeoZoneResolver, DataImporter<ContractFileDto>, DataExporter<ContractFileDto> {
    
    private final ContractService contractService;
    
    private final ContractorRepository contractorRepository;
    
    private final ContractorService contractorService;
    
    private final GeoZoneRepository geoZoneRepository;
    
    private final OrganizationService organizationService;
    
    static final String ORGANIZATION_NAME_SEPARATOR = ";";
    
    @Override
    public void importData(ContractFileDto item, @NotNull Map<String, ?> parameters, @NotNull JwtAuthenticationToken authentication) {
        final var contractor = contractorRepository.findByName(item.getContractor())
                                             .orElseThrow(() -> new EntityNotFoundException(Contractor.class, item.getContractor()));

        var contract = contractService.get(contractor.getId(), item.getContractNumber()).orElse(null);
        
        // Если договор не найден, то создаем только активный договор
        if (contract == null && !item.isActive()) {
            log.warn("Договор с номером {} не существует или уже был деактивирован. Данные из файла по деактивации этого договора проигнорированы",
                     item.getContractNumber());
            return;
        }
        contract = Objects.requireNonNullElse(contract, new Contract());
        
        contract.setVatValue(item.getVatValue());
        contract.setContractNumber(item.getContractNumber());
        contract.setContractorId(contractor.getId());
        contract.setSum(item.getSum());
        contract.setTransportType(TransportTypeEnum.getByRusName(item.getTransportType()).orElseThrow());
        contract.setServiceType(TransportServiceType.valueOfDescription(item.getServiceType()));
        contract.setStartDate(item.getStartDate());

        final var geozone = geoZoneRepository.findByName(item.getRegion()).orElseThrow(() -> new EntityNotFoundException(GeoZone.class, item.getRegion()));
        contract.setRegionIds(geozone == null ? Collections.emptySet() : Set.of(geozone.getId()));
        contract.setEndDate(item.getEndDate());
        contract.setUserId(UUID.fromString(authentication.getToken().getId()));
        contract.setOrganizations(organizationNamesToOrganizations(item.getOrganization()));
        contract.setUvhd(item.getUvhd());
        contract.setActive(item.isActive());
        contract.setIncludeVat(item.isIncludeVat());
        
        contractService.save(contract);
        contractorService.updateContractorsRegionIds(contract);
        log.info("Данные для договора с номером {} были успешно импортированы из файла", item.getContractNumber());
    }
    
    @NotNull
    @Override
    public List<ContractFileDto> exportData(@NotNull Map<String, ?> parameters, @NotNull JwtAuthenticationToken authentication) {
        final var result = new ArrayList<ContractFileDto>();

        final var items = contractService.getAll();
        final var regions = geoResolve(items.stream().map(Contract::getRegionIds).flatMap(Collection::stream).toList());
        for (final var item : items) {
            final var contractor = contractorRepository.findById(item.getContractorId()).map(Contractor::getName).orElse(null);

            final var resItem = new ContractFileDto();
            
            resItem.setContractNumber(item.getContractNumber());
            if (!item.getRegionIds().isEmpty()) {
                item.getRegionIds().stream()
                    .map(regions::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .ifPresent(resItem::setRegion);
            }
            resItem.setEndDate(item.getEndDate());
            resItem.setContractor(contractor);
            resItem.setStartDate(item.getStartDate());
            resItem.setSum(item.getSum());
            resItem.setTransportType(item.getTransportType().getRusName());
            resItem.setVatValue(item.getVatValue());
            resItem.setActive(item.isActive());
            resItem.setServiceType(item.getServiceType().getDescription());
            resItem.setIncludeVat(item.isIncludeVat());
            resItem.setOrganization(organizationsToOrganizationNames(item.getOrganizations()));
            resItem.setUvhd(item.getUvhd());
            result.add(resItem);
        }
        return result;
    }
    
    private String organizationsToOrganizationNames(Set<Organization> organizations) {
        var sb = new StringBuilder();
        if (organizations != null && !organizations.isEmpty()) {
            for (final var organization : organizations) {
                if (!sb.isEmpty()) {
                    sb.append(ORGANIZATION_NAME_SEPARATOR);
                }
                sb.append(organization.getName());
            }
        }
        return sb.toString();
    }
    
    private Set<Organization> organizationNamesToOrganizations(String organizationNames) {
        final var organizations = new HashSet<Organization>();
        if (StringUtils.hasText(organizationNames)) {
            for (final var organizationName : organizationNames.split(ORGANIZATION_NAME_SEPARATOR)) {
                final var organization = organizationService.get(organizationName)
                                                               .orElseThrow(() -> new EntityNotFoundException(Organization.class, organizationName));
                organizations.add(organization);
            }
        }
        return organizations;
    }
}
