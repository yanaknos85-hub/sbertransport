package ru.sber.transport.request.external.providers.organization;

import static ru.sber.transport.database.external_request.Tables.ORGANIZATION_TRANSPORT_TYPE;
import static ru.sber.transport.database.external_request.Tables.TRANSPORT_TYPES;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.business.providers.OrganizationsProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.tables.records.OrganizationRecord;
import ru.sber.transport.request.external.model.Organization;

@Slf4j
@Transactional
@RequiredArgsConstructor
public class OrganizationsDataProviderImpl implements OrganizationsProvider,
    JooqRepository<ru.sber.transport.database.external_request.tables.Organization, OrganizationRecord, UUID> {

    private final OrganizationsProvider organizationsProvider;

    @Override
    public ru.sber.transport.database.external_request.tables.Organization table() {
        return Tables.ORGANIZATION;
    }

    @Override
    public Organization save(Organization source) {
        for (final var clazz : source.getAvailableClasses()) {
            context().insertInto(TRANSPORT_TYPES)
                .values(clazz)
                .onConflictDoNothing()
                .execute();
        }

        final var item = findById(source.getId()).orElseGet(OrganizationRecord::new);
        item.setId(source.getId());
        item.setDigitId((int) source.getDigitId());

        final var saved = saveAll(List.of(item), List.of(table().ID)).iterator().next();

        context().deleteFrom(ORGANIZATION_TRANSPORT_TYPE)
            .where(ORGANIZATION_TRANSPORT_TYPE.ORGANIZATION_ID.eq(source.getId())).execute();
        for (final var clazz : source.getAvailableClasses()) {
            context().insertInto(ORGANIZATION_TRANSPORT_TYPE)
                .set(ORGANIZATION_TRANSPORT_TYPE.ORGANIZATION_ID, source.getId())
                .set(ORGANIZATION_TRANSPORT_TYPE.TRANSPORT_TYPE, clazz)
                .onConflictDoNothing().execute();
        }
        return createOrganization(saved);
    }

    @Override
    public Organization get(UUID id) {
        return findById(id).map(this::createOrganization)
            .map(it -> {
                log.info("Organization {} found", id);
                return it;
            })
            .orElseGet(() -> {
                final var saved = save(organizationsProvider.get(id));
                log.info("Responded organization {} saved", id);
                return saved;
            });
    }

    @NotNull
    private Organization createOrganization(OrganizationRecord source) {
        return new Organization() {

            @Override
            public UUID getId() {
                return source.getId();
            }

            @Override
            public long getDigitId() {
                return source.getDigitId();
            }

            @Override
            public List<String> getAvailableClasses() {
                return context().select(TRANSPORT_TYPES.TRANSPORT_TYPE)
                    .from(ORGANIZATION_TRANSPORT_TYPE)
                    .innerJoin(TRANSPORT_TYPES)
                    .on(ORGANIZATION_TRANSPORT_TYPE.TRANSPORT_TYPE.eq(TRANSPORT_TYPES.TRANSPORT_TYPE))
                    .where(ORGANIZATION_TRANSPORT_TYPE.ORGANIZATION_ID.eq(source.getId()))
                    .fetch(TRANSPORT_TYPES.TRANSPORT_TYPE);
            }
        };
    }

}
