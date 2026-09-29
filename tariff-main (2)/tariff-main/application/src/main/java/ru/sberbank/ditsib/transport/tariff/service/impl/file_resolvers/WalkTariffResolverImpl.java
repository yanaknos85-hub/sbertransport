package ru.sberbank.ditsib.transport.tariff.service.impl.file_resolvers;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.WalkTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.files.WalkFileDto;
import ru.sberbank.ditsib.transport.tariff.exceptions.ImportChangingValuesException;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffSearchDTOMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.TariffSender;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;

import java.util.Map;

/**
 * Получатель данных из файла о пешеходном тарифе.
 */
@Component
@Transactional
public class WalkTariffResolverImpl extends BaseTariffResolverImpl<WalkTariff, WalkFileDto> {

    private final TariffService tariffService;

    private final TariffSender tariffSender;

    private final GeoZoneRepository geoZoneRepository;

    public WalkTariffResolverImpl(
            TariffService tariffService, TariffSender tariffSender, OrganizationRepository organizationRepository, GeoZoneRepository geoZoneRepository,
            TariffSearchDTOMapper tariffSearchDTOMapper
    ) {
        super(tariffService, organizationRepository, tariffSearchDTOMapper);
        this.tariffService = tariffService;
        this.tariffSender = tariffSender;
        this.geoZoneRepository = geoZoneRepository;
    }

    @Override
    protected WalkTariff newTariff() {
        return new WalkTariff();
    }

    @Override
    public void importData(WalkFileDto item, @NotNull Map<String, ?> params, @NotNull JwtAuthenticationToken authenticationToken) {
        final var organization = getOrganization(item.getOrganization());
        final var tariff = getTariff(item);
        tariff.setActive(item.isActive());
        if (item.getId() == null || item.getId().isEmpty()) {
            tariff.setOrganization(organization);
            final var geozone = geoZoneRepository.findByName(item.getRegion()).orElseThrow(() -> new EntityNotFoundException(GeoZone.class, item.getRegion()));
            tariff.setRegionId(geozone == null ? null : geozone.getId());
        } else {
            if (!item.getOrganization().equals(tariff.getOrganization().getName())) {
                throw new ImportChangingValuesException(ImportChangingValuesException.ORGANIZATION_NAME_CHANGING);
            }
            if (!item.getRegion().equals(geoZoneRepository.findById(tariff.getRegionId()).orElseThrow().getName())) {
                throw new ImportChangingValuesException(ImportChangingValuesException.REGION_NAME_CHANGING);
            }
        }
        tariff.setTransportType(TransportTypeEnum.WALK);
        tariffService.save(tariff);

        tariffSender.send(tariff, !tariff.isActive());
    }

    @Override
    protected TransportTypeEnum transportType() {
        return TransportTypeEnum.WALK;
    }

    @Override
    protected Class<WalkTariff> tariffClass() {
        return WalkTariff.class;
    }

    @Override
    protected WalkFileDto newItem() {
        return new WalkFileDto();
    }
}
