package ru.sberbank.ditsib.transport.tariff.service.impl.file_resolvers;

import lombok.NonNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.GroupTransferTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.files.TransferFileDto;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffSearchDTOMapper;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;
import ru.sberbank.ditsib.transport.tariff.service.ContractorService;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;
import ru.sberbank.ditsib.transport.tariff.service.mapper.TransferFileDtoMapper;

import javax.naming.OperationNotSupportedException;
import java.util.Map;
import java.util.Optional;

/**
 * Получатель данных из файла о тарифе трансфер.
 */
@Component
public class TransferTariffResolverImpl extends BaseTariffContractResolverImpl<GroupTransferTariff, TransferFileDto> {

    private final ContractorService contractorService;
    private final TransferFileDtoMapper transferFileDtoMapper;
    private final GeoZoneRepository geoZoneRepository;
    private final TariffService tariffService;

    TransferTariffResolverImpl(
            TariffService tariffService, OrganizationRepository organizationRepository, TariffSearchDTOMapper tariffSearchDTOMapper,
            ContractorService contractorService, ContractService contractService,
            TransferFileDtoMapper transferFileDtoMapper,
            GeoZoneRepository geoZoneRepository
    ) {
        super(tariffService, organizationRepository, tariffSearchDTOMapper, contractorService, contractService);
        this.contractorService = contractorService;
        this.transferFileDtoMapper = transferFileDtoMapper;
        this.geoZoneRepository = geoZoneRepository;
        this.tariffService = tariffService;
    }

    @Override
    protected GroupTransferTariff newTariff() {
        return new GroupTransferTariff();
    }

    @Override
    public void importData(TransferFileDto transferFileDto, @NonNull Map<String, ?> map, @NonNull JwtAuthenticationToken jwtAuthenticationToken)
            throws OperationNotSupportedException {
        throw new OperationNotSupportedException("Not implemented");
    }

    @Override
    protected void exportMapping(TransferFileDto resItem, GroupTransferTariff item) {
        final var contractor = Optional.ofNullable(item.getContract())
                .map(Contract::getContractorId)
                .flatMap(contractorService::get)
                .orElse(null);

        final var region = geoZoneRepository.findById(item.getRegionId());
        final var regionName = region.map(GeoZone::getName).orElse(null);

        if (item.getDepartment() != null) {
            var department = tariffService.getDepartmentById(item.getDepartment().getId());
            department.ifPresent(value -> resItem.setDepartmentHumanReadableId(value.getHumanReadableId()));
        }
        transferFileDtoMapper.mapToTransferFileDto(resItem, item, contractor, regionName);
    }

    @Override
    protected TransportTypeEnum transportType() {
        return TransportTypeEnum.GROUP_TRANSFER;
    }

    @Override
    protected Class<GroupTransferTariff> tariffClass() {
        return GroupTransferTariff.class;
    }

    @Override
    protected TransferFileDto newItem() {
        return new TransferFileDto();
    }
}
