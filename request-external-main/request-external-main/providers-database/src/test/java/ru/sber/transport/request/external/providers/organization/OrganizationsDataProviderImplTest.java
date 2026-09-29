package ru.sber.transport.request.external.providers.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.sber.transport.database.external_request.Tables.ORGANIZATION_TRANSPORT_TYPE;
import static ru.sber.transport.database.external_request.Tables.TRANSPORT_TYPES;

import io.qameta.allure.Feature;
import java.util.UUID;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.OrganizationsProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.external.providers.model.TestOrganization;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера организаций")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class OrganizationsDataProviderImplTest {

    @Autowired
    private DSLContext context;

    private final OrganizationsProvider additional = mock(OrganizationsProvider.class);

    private final OrganizationsProvider organizationsProvider = new OrganizationsDataProviderImpl(additional) {

        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Проверка сохранения организации")
    void test_save() {
        final var source = new TestOrganization(
                UUID.randomUUID(),
                Instancio.create(Long.class),
                Instancio.createList(String.class)
        );

        organizationsProvider.save(source);

        assertThat(context.fetchCount(Tables.ORGANIZATION)).isEqualTo(1);

        final var actual = context.selectFrom(Tables.ORGANIZATION).fetchSingle();
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(source.getId());
        assertThat(actual.getDigitId().longValue()).isEqualTo(source.getDigitId());

        assertThat(context.fetchCount(ORGANIZATION_TRANSPORT_TYPE)).isEqualTo(source.getAvailableClasses().size());
        assertThat(context.fetchCount(TRANSPORT_TYPES)).isEqualTo(source.getAvailableClasses().size());

        assertThat(context.select(TRANSPORT_TYPES.TRANSPORT_TYPE).from(TRANSPORT_TYPES).fetch(TRANSPORT_TYPES.TRANSPORT_TYPE)).hasSameElementsAs(source.getAvailableClasses());
        assertThat(context.select(ORGANIZATION_TRANSPORT_TYPE.TRANSPORT_TYPE).from(ORGANIZATION_TRANSPORT_TYPE).fetch(ORGANIZATION_TRANSPORT_TYPE.TRANSPORT_TYPE)).hasSameElementsAs(source.getAvailableClasses());
        assertThat(context.select(ORGANIZATION_TRANSPORT_TYPE.ORGANIZATION_ID).from(ORGANIZATION_TRANSPORT_TYPE).fetch(ORGANIZATION_TRANSPORT_TYPE.ORGANIZATION_ID)).allMatch(id -> source.getId().equals(id));
    }

    @Test
    @DisplayName("Проверка получения организации")
    void test_get() {
        final var id = UUID.randomUUID();
        context.insertInto(Tables.ORGANIZATION)
                .set(Tables.ORGANIZATION.ID, id)
                .set(Tables.ORGANIZATION.DIGIT_ID, 1)
                .execute();

        final var actual = organizationsProvider.get(id);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getDigitId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Проверка получения организации из внешнего источника")
    void test_externalRequest() {
        final var id = UUID.randomUUID();

        final var organization = new TestOrganization(
                UUID.randomUUID(),
                Instancio.create(Long.class),
                Instancio.createList(String.class)
        );
        when(additional.get(id)).thenReturn(organization);

        final var actual = organizationsProvider.get(id);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(organization.getId());
        assertThat(actual.getDigitId()).isEqualTo(organization.getDigitId());

        assertThat(context.fetchCount(Tables.ORGANIZATION)).isEqualTo(1);

        final var actualDb = context.selectFrom(Tables.ORGANIZATION).fetchSingle();
        assertThat(actualDb).isNotNull();
        assertThat(actualDb.getId()).isEqualTo(organization.getId());
        assertThat(actualDb.getDigitId().longValue()).isEqualTo(organization.getDigitId());

        assertThat(context.select(TRANSPORT_TYPES.TRANSPORT_TYPE).from(TRANSPORT_TYPES).fetch(TRANSPORT_TYPES.TRANSPORT_TYPE)).hasSameElementsAs(organization.getAvailableClasses());
        assertThat(context.select(ORGANIZATION_TRANSPORT_TYPE.TRANSPORT_TYPE).from(ORGANIZATION_TRANSPORT_TYPE).fetch(ORGANIZATION_TRANSPORT_TYPE.TRANSPORT_TYPE)).hasSameElementsAs(organization.getAvailableClasses());
        assertThat(context.select(ORGANIZATION_TRANSPORT_TYPE.ORGANIZATION_ID).from(ORGANIZATION_TRANSPORT_TYPE).fetch(ORGANIZATION_TRANSPORT_TYPE.ORGANIZATION_ID)).allMatch(org -> organization.getId().equals(org));
    }

}