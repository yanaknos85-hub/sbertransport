package ru.sberbank.ditsib.transport.tariff.service.impl.file_resolvers;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.PublicTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.files.PublicCostDto;
import ru.sberbank.ditsib.transport.tariff.dto.files.PublicFileDto;
import ru.sberbank.ditsib.transport.tariff.exceptions.ImportChangingValuesException;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffSearchDTOMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.TariffSender;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;
import ru.sberbank.utils.reflection.ReflectionUtils;

import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Получатель данных из файла о тарифе на общественный транспорт.
 */
@Transactional
@Component
public class PublicTariffResolverImpl extends BaseTariffResolverImpl<PublicTariff, PublicFileDto> {
    
    private final TariffService tariffService;
    
    private final TariffSender tariffSender;
    
    private final GeoZoneRepository geoZoneRepository;

    private final TariffSearchDTOMapper tariffSearchDTOMapper;

    public PublicTariffResolverImpl(
            TariffService tariffService, TariffSender tariffSender, OrganizationRepository organizationRepository, GeoZoneRepository geoZoneRepository,
            TariffSearchDTOMapper tariffSearchDTOMapper
    ) {
        super(tariffService, organizationRepository, tariffSearchDTOMapper);
        this.tariffService = tariffService;
        this.tariffSender = tariffSender;
        this.geoZoneRepository = geoZoneRepository;
        this.tariffSearchDTOMapper = tariffSearchDTOMapper;
    }

    @Override
    protected PublicTariff newTariff() {
        return new PublicTariff();
    }

    @Override
    public void importData(PublicFileDto item, @NotNull Map<String, ?> parameters, @NotNull JwtAuthenticationToken authenticationToken) {
        final var organization = getOrganization(item.getOrganization());
        final var tariff = getTariff(item);
        tariff.setActive(item.isActive());
        if (item.getId() == null || item.getId().isEmpty()) {
            tariff.setOrganization(organization);
            var geozone = geoZoneRepository.findByName(item.getRegion()).orElse(null);
            tariff.setRegionId(geozone == null ? null : geozone.getId());
        } else {
            if (!item.getOrganization().equals(tariff.getOrganization().getName())) {
                throw new ImportChangingValuesException(ImportChangingValuesException.ORGANIZATION_NAME_CHANGING);
            }
            if (!item.getRegion().equals(geoZoneRepository.findById(tariff.getRegionId()).orElseThrow().getName())) {
                throw new ImportChangingValuesException(ImportChangingValuesException.REGION_NAME_CHANGING);
            }
        }
        tariff.setTransportType(TransportTypeEnum.PUBLIC);
        final var cost = item.getCost();
        executeIf(cost, i -> i.getMetro() != null, i -> (int) (Math.round(i.getMetro())), value -> {
            tariff.setMetroTicketCost(value);
            tariff.setMetroAvailability(true);
        });
        executeIf(cost, i -> i.getBus() != null, i -> (int) (Math.round(i.getBus())), value -> {
            tariff.setBusTicketCost(value);
            tariff.setBusAvailability(true);
        });
        executeIf(cost, i -> i.getTram() != null, i -> (int) (Math.round(i.getTram())), value -> {
            tariff.setTramTicketCost(value);
            tariff.setTramAvailability(true);
        });
        executeIf(cost, i -> i.getTrolleybus() != null, i -> (int) (Math.round(i.getTrolleybus())), value -> {
            tariff.setTrolleybusTicketCost(value);
            tariff.setTrolleybusAvailability(true);
        });
        
        executeIf(cost, i -> i.getTravelCardMetro() != null, i -> (int) (Math.round(i.getTravelCardMetro())), value -> {
            tariff.setTravelCardMetroCost(value);
            tariff.setTravelCardMetroAvailability(true);
        });
        executeIf(cost, i -> i.getTravelCardBus() != null, i -> (int) (Math.round(i.getTravelCardBus())), value -> {
            tariff.setTravelCardBusCost(value);
            tariff.setTravelCardBusAvailability(true);
        });
        executeIf(cost, i -> i.getTravelCardTram() != null, i -> (int) (Math.round(i.getTravelCardTram())), value -> {
            tariff.setTravelCardTramCost(value);
            tariff.setTravelCardTramAvailability(true);
        });
        executeIf(cost, i -> i.getTravelCardTrolleybus() != null, i -> (int) (Math.round(i.getTravelCardTrolleybus())), value -> {
            tariff.setTravelCardTrolleybusCost(value);
            tariff.setTravelCardTrolleybusAvailability(true);
        });
        
        var saved = (PublicTariff) tariffService.save(tariff);
        
        tariffSender.send(saved, !saved.isActive());
    }
    
    @NotNull
    @Override
    public List<PublicFileDto> exportData(@NotNull Map<String, ?> parameters, @NotNull JwtAuthenticationToken authentication) {
        final var result = new ArrayList<PublicFileDto>();
        final var filters = tariffSearchDTOMapper.mapFromParameters(parameters);
        if (!tariffService.canReportBeGenerated(filters, TransportTypeEnum.PUBLIC)) {
            return result;
        }
        final var items = ReflectionUtils.castObjectToList(tariffService.getAll(TransportTypeEnum.PUBLIC, filters.getRegionId() != null ?
                                                                                                    Collections.singletonList(filters.getRegionId()) :
                                                                                                    Collections.emptyList(),
                                                                          filters.getOrganizationId(),
                                                                          filters.getActive(), filters.getContractorId(),
                                                                          filters.getHumanReadableId(), null,
                                                                          filters.getContractNumber(), filters.getTransportClass(),
                                                                          null),
                                                     PublicTariff.class);
        for (final var item : items) {
            final var resItem = new PublicFileDto();
            final var cost = new PublicCostDto();
            resItem.setCost(cost);
            
            resItem.setActive(item.isActive());
            geoZoneRepository.findById(item.getRegionId())
                             .map(GeoZone::getName)
                             .ifPresent(resItem::setRegion);
            resItem.setOrganization(item.getOrganization().getName());
            resItem.setId(item.getHumanReadableId());

            executeIf(item, PublicTariff::isBusAvailability, PublicTariff::getBusTicketCost, value -> cost.setBus((double) value));
            executeIf(item, PublicTariff::isTrolleybusAvailability, PublicTariff::getTrolleybusTicketCost,
                      value -> cost.setTrolleybus((double) value));
            executeIf(item, PublicTariff::isTramAvailability, PublicTariff::getTramTicketCost, value -> cost.setTram((double) value));
            executeIf(item, PublicTariff::isMetroAvailability, PublicTariff::getMetroTicketCost, value -> cost.setMetro((double) value));
            
            executeIf(item, PublicTariff::isTravelCardBusAvailability, PublicTariff::getTravelCardBusCost,
                      value -> cost.setTravelCardBus((double) value));
            executeIf(item, PublicTariff::isTravelCardTrolleybusAvailability, PublicTariff::getTravelCardTrolleybusCost,
                      value -> cost.setTravelCardTrolleybus((double) value));
            executeIf(item, PublicTariff::isTravelCardTramAvailability, PublicTariff::getTravelCardTramCost,
                      value -> cost.setTravelCardTram((double) value));
            executeIf(item, PublicTariff::isTravelCardMetroAvailability, PublicTariff::getTravelCardMetroCost,
                      value -> cost.setTravelCardMetro((double) value));
            
            Optional.ofNullable(item.getServiceType()).map(TransportServiceType::getDescription).ifPresent(resItem::setServiceType);
            
            result.add(resItem);
        }
        
        return result;
    }

    @Override
    protected TransportTypeEnum transportType() {
        return TransportTypeEnum.PUBLIC;
    }

    @Override
    protected Class<PublicTariff> tariffClass() {
        return PublicTariff.class;
    }

    @Override
    protected PublicFileDto newItem() {
        return new PublicFileDto();
    }

    private <S, T> void executeIf(
            S tariff, Predicate<S> predicate, Function<S, T> getAction,
            Consumer<T> setAction
                                 ) {
        if (predicate.test(tariff)) {
            var value = getAction.apply(tariff);
            setAction.accept(value);
        }
    }
}
