package ru.sberbank.ditsib.transport.tariff.service.impl.file_resolvers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.file_works.importer.DataImporter;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.BaseTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.database.model.WalkTariff;
import ru.sberbank.ditsib.transport.tariff.dto.files.TariffFileDto;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffSearchDTOMapper;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Общая часть получателя тарифов из файла.
 *
 * @param <S> тип сущности в БД.
 * @param <T> тип объекта обмена данными.
 */
@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
abstract class BaseTariffResolverImpl<S extends BaseTariff, T extends TariffFileDto> implements GeoZoneResolver, DataExporter<T>, DataImporter<T> {

    private final TariffService tariffService;

    private final OrganizationRepository organizationRepository;

    private final TariffSearchDTOMapper tariffSearchDTOMapper;

    protected Organization getOrganization(String name) {
        return organizationRepository.findByName(name).orElseThrow(() -> new EntityNotFoundException(Organization.class, Map.of("name", name)));
    }

    protected S getTariff(T item) {
        return Optional.ofNullable(item.getId())
                .map(it -> tariffService.get(transportType(), it).orElseThrow(() -> new EntityNotFoundException(WalkTariff.class, it)))
                .map(ReflectionUtils::<S>cast)
                .orElseGet(this::newTariff);
    }

    @NotNull
    @Override
    public List<T> exportData(@NotNull Map<String, ?> parameters, @NotNull JwtAuthenticationToken authentication) {
        final var result = new ArrayList<T>();

        final var filters = tariffSearchDTOMapper.mapFromParameters(parameters);
        if (!tariffService.canReportBeGenerated(filters, transportType())) {
            return result;
        }
        final var items = ReflectionUtils.castObjectToList(
                tariffService.getAll(transportType(), filters.getRegionId() != null ? Collections.singletonList(filters.getRegionId()) :
                                Collections.emptyList(),
                        filters.getOrganizationId(),
                        filters.getActive(), filters.getContractorId(),
                        filters.getHumanReadableId(), filters.getIsNightTariff(), filters.getContractNumber(),
                        filters.getTransportClass(), null),
                tariffClass());

        final var regionIds = items.stream().map(BaseTariff::getRegionId).toList();
        final var regions = GeoZoneResolver.super.geoResolve(regionIds);
        final var organizationIds = items.stream().map(BaseTariff::getOrganization).map(Organization::getId).collect(Collectors.toSet());
        final var organizations = getOrganizationNames(organizationIds);

        for (final var item : items) {
            final var resItem = newItem();

            final var organization = organizations.get(item.getOrganization().getId());
            final var region = regions.get(item.getRegionId());
            final var id = item.getHumanReadableId();

            resItem.setOrganization(organization);
            resItem.setRegion(region);
            resItem.setId(id);
            resItem.setActive(item.isActive());

            exportMapping(resItem, item);

            result.add(resItem);
        }

        return result;
    }

    @NotNull
    protected Map<UUID, String> getOrganizationNames(Set<UUID> organizationIds) {
        return organizationRepository.findAllById(organizationIds).stream().collect(Collectors.toMap(Organization::getId, Organization::getName));
    }

    /**
     * Тип сущности, который обслуживается текущей реализацией.
     *
     * @return обслуживаемый получателем тип транспорта.
     */
    protected abstract TransportTypeEnum transportType();

    /**
     * Класс сущности, который обслуживается текущей реализацией.
     *
     * @return класс сущности.
     */
    protected abstract Class<S> tariffClass();

    /**
     * Получение нового объекта обмена данными.
     *
     * @return пустой объект обмена данными.
     */
    protected abstract T newItem();

    /**
     * Создание нового объекта сущности.
     *
     * @return новый объект сущности.
     */
    protected abstract S newTariff();

    /**
     * Маппинг данных для экспорта.
     *
     * @param target целевой объект.
     * @param source исходный объект.
     */
    protected void exportMapping(T target, S source) {
    }

}
