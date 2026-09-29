package ru.sberbank.ditsib.transport.tariff.service.impl.file_resolvers;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sberbank.ditsib.transport.tariff.database.model.BaseTariffWithContract;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.dto.files.TariffContractFileDto;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffSearchDTOMapper;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;
import ru.sberbank.ditsib.transport.tariff.service.ContractorService;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Общая часть получателя тарифов из файла с договорами.
 *
 * @param <S> тип сущности в БД.
 * @param <T> тип объекта обмена данными.
 */
abstract class BaseTariffContractResolverImpl<S extends BaseTariffWithContract, T extends TariffContractFileDto>
        extends BaseTariffResolverImpl<S, T> {

    private final ContractorService contractorService;

    private final ContractService contractService;

    BaseTariffContractResolverImpl(
            TariffService tariffService, ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository organizationService, TariffSearchDTOMapper tariffSearchDTOMapper,
            ContractorService contractorService,
            ContractService contractService
    ) {
        super(tariffService, organizationService, tariffSearchDTOMapper);
        this.contractorService = contractorService;
        this.contractService = contractService;
    }

    @NotNull
    @Override
    public List<T> exportData(@NotNull Map<String, ?> parameters, @NotNull JwtAuthenticationToken authentication) {
        final var result = new ArrayList<T>();

        final var filters = getTariffSearchDTOMapper().mapFromParameters(parameters);
        final var transportType = transportType();
        final var tariffService = getTariffService();
        if (!tariffService.canReportBeGenerated(filters, transportType)) {
            return result;
        }
        final var regionId = filters.getRegionId();
        final var items = ReflectionUtils.castObjectToList(
                tariffService.getAll(transportType, regionId != null ? Collections.singletonList(regionId) :
                                Collections.emptyList(),
                        filters.getOrganizationId(),
                        filters.getActive(), filters.getContractorId(),
                        filters.getHumanReadableId(), filters.getIsNightTariff(), filters.getContractNumber(),
                        filters.getTransportClass(), null),
                tariffClass());

        final var regionIds = items.stream().map(BaseTariffWithContract::getRegionId).toList();
        final var regions = geoResolve(regionIds);
        final var organizationIds = items.stream().map(BaseTariffWithContract::getOrganization).map(Organization::getId).collect(Collectors.toSet());
        final var organizations = getOrganizationNames(organizationIds);
        final var contracts = contractService.get(items.stream().map(BaseTariffWithContract::getContract)
                .filter(Objects::nonNull)
                .map(Contract::getId).toList());
        final var contractNames = contracts.stream().collect(Collectors.toMap(Contract::getId, Contract::getContractNumber));
        final var contractContractors = contracts.stream()
                .filter(contract -> contract.getContractorId() != null)
                .collect(Collectors.toMap(Contract::getId, Contract::getContractorId));
        final var contractorNames = contractorService.getNames(contractContractors.values());

        for (final var item : items) {
            final var resItem = newItem();

            final var organization = organizations.get(item.getOrganization().getId());
            final var region = regions.get(item.getRegionId());
            final var id = item.getHumanReadableId();
            final var contract = item.getContract();

            resItem.setOrganization(organization);
            resItem.setRegion(region);
            resItem.setId(id);
            resItem.setActive(item.isActive());

            if (contract != null) {
                resItem.setContract(contractNames.get(contract.getId()));
                final var contractor = contractContractors.get(contract.getId());
                resItem.setContractor(contractor == null ? null : contractorNames.get(contractor));
            }

            exportMapping(resItem, item);

            result.add(resItem);
        }

        return result;
    }
}
