package ru.sber.transport.address.providers.meeting_address;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.model.MeetingAddress;
import ru.sber.transport.address.business.provider.AddressDataProvider;
import ru.sber.transport.address.business.provider.MeetingAddressProvider;
import ru.sber.transport.address.providers.meeting_address.mapper.MeetingAddressDatabaseMapperImpl;
import ru.sber.transport.database.addresses.tables.Meetings;
import ru.sber.transport.database.addresses.tables.records.MeetingsRecord;
import ru.sber.transport.scripting.ScriptUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@JooqTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка провайдера корпоративных адресов")
@AutoConfigurationPackage
@MockBean(ScriptUtils.class)
@Transactional
@ContextConfiguration(classes = {JooqDatabaseConfig.class, MeetingProviderImpl.class, MeetingAddressDatabaseMapperImpl.class})
@Import({JooqDatabaseConfig.class, MeetingProviderImpl.class, MeetingAddressDatabaseMapperImpl.class})
@ActiveProfiles("test")
class MeetingAddressProviderImplTest {

    @Autowired
    private AddressDataProvider<MeetingAddress> searchProvider;

    @Autowired
    private MeetingAddressProvider addressProvider;

    @Autowired
    private DSLContext context;

    @Test
    @DisplayName("Получение адреса")
    void test_get() {
        var record = new MeetingsRecord(
            UUID.randomUUID(),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            BigDecimal.valueOf(new Random().nextDouble(-90, 90)),
            BigDecimal.valueOf(new Random().nextDouble(-180, 180)),
            UUID.randomUUID());
        context.insertInto(Meetings.MEETINGS).set(record).execute();

        var actualOpt = addressProvider.get(record.getLabel());

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();

        assertSoftly(assertion -> {
            assertion.assertThat(actual.getBuilding()).isEqualTo(record.getBuilding());
            assertion.assertThat(actual.getCity()).isEqualTo(record.getCity());
            assertion.assertThat(actual.getLabel()).isEqualTo(record.getLabel());
            assertion.assertThat(actual.getCountry()).isEqualTo(record.getCountry());
            assertion.assertThat(actual.getHouse()).isEqualTo(record.getHouse());
            assertion.assertThat(actual.getLatitude()).isEqualTo(record.getLatitude().round(new MathContext(actual.getLatitude().precision())));
            assertion.assertThat(actual.getLongitude()).isEqualTo(record.getLongitude().round(new MathContext(actual.getLongitude().precision())));
            assertion.assertThat(actual.getRegion()).isEqualTo(record.getRegion());
            assertion.assertThat(actual.getStreet()).isEqualTo(record.getStreet());
            assertion.assertThat(actual.getStructure()).isEqualTo(record.getStructure());
        });

        assertThat(addressProvider.get("Test label")).isEmpty();
    }

    @Test
    @DisplayName("Проверка существования")
    void test_exists() {
        var organizationId = UUID.randomUUID();
        var record = new MeetingsRecord(
            UUID.randomUUID(),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            BigDecimal.valueOf(new Random().nextDouble(-90, 90)),
            BigDecimal.valueOf(new Random().nextDouble(-180, 180)),
            organizationId);
        context.insertInto(Meetings.MEETINGS).set(record).execute();

        assertThat(addressProvider.exists(record.getLabel(), organizationId)).isTrue();
        assertThat(addressProvider.exists(record.getLabel(), organizationId, record.getId())).isFalse();
        assertThat(addressProvider.exists(record.getLabel(), UUID.randomUUID(), record.getId())).isFalse();
        assertThat(addressProvider.exists(record.getLabel(), organizationId, UUID.randomUUID())).isTrue();
        assertThat(addressProvider.exists(record.getLabel(), UUID.randomUUID(), UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Получение адресов владельца")
    void test_getOfOwner() {
        var organizationId = UUID.randomUUID();
        var record = new MeetingsRecord(
            UUID.randomUUID(),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            BigDecimal.valueOf(new Random().nextDouble(-90, 90)),
            BigDecimal.valueOf(new Random().nextDouble(-180, 180)),
            organizationId);
        context.insertInto(Meetings.MEETINGS).set(record).execute();

        var actualOpt = addressProvider.getOfOwner(organizationId);

        assertThat(actualOpt).isNotEmpty().hasSize(1);

        var actual = actualOpt.iterator().next();

        assertThat(actual.getBuilding()).isEqualTo(record.getBuilding());
        assertThat(actual.getCity()).isEqualTo(record.getCity());
        assertThat(actual.getLabel()).isEqualTo(record.getLabel());
        assertThat(actual.getCountry()).isEqualTo(record.getCountry());
        assertThat(actual.getHouse()).isEqualTo(record.getHouse());
        assertThat(actual.getLatitude()).isEqualTo(record.getLatitude().round(new MathContext(actual.getLatitude().precision())));
        assertThat(actual.getLongitude()).isEqualTo(record.getLongitude().round(new MathContext(actual.getLongitude().precision())));
        assertThat(actual.getRegion()).isEqualTo(record.getRegion());
        assertThat(actual.getStreet()).isEqualTo(record.getStreet());
        assertThat(actual.getStructure()).isEqualTo(record.getStructure());
    }

    @Test
    @DisplayName("Получение адреса владельца")
    void test_getOneOfOwner() {
        var organizationId = UUID.randomUUID();
        var record = new MeetingsRecord(
            UUID.randomUUID(),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            BigDecimal.valueOf(new Random().nextDouble(-90, 90)),
            BigDecimal.valueOf(new Random().nextDouble(-180, 180)),
            organizationId);
        context.insertInto(Meetings.MEETINGS).set(record).execute();

        var actualOpt = addressProvider.get(organizationId, record.getId());

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();

        assertSoftly(assertion -> {
            assertion.assertThat(actual.getBuilding()).isEqualTo(record.getBuilding());
            assertion.assertThat(actual.getCity()).isEqualTo(record.getCity());
            assertion.assertThat(actual.getLabel()).isEqualTo(record.getLabel());
            assertion.assertThat(actual.getCountry()).isEqualTo(record.getCountry());
            assertion.assertThat(actual.getHouse()).isEqualTo(record.getHouse());
            assertion.assertThat(actual.getLatitude()).isEqualTo(record.getLatitude().round(new MathContext(actual.getLatitude().precision())));
            assertion.assertThat(actual.getLongitude()).isEqualTo(record.getLongitude().round(new MathContext(actual.getLongitude().precision())));
            assertion.assertThat(actual.getRegion()).isEqualTo(record.getRegion());
            assertion.assertThat(actual.getStreet()).isEqualTo(record.getStreet());
            assertion.assertThat(actual.getStructure()).isEqualTo(record.getStructure());
        });
    }

    @Test
    @DisplayName("Удаление адреса владельца")
    void test_delete_ofOwner() {
        var organizationId = UUID.randomUUID();
        var record = new MeetingsRecord(
            UUID.randomUUID(),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            Instancio.create(String.class),
            BigDecimal.valueOf(new Random().nextDouble(-90, 90)),
            BigDecimal.valueOf(new Random().nextDouble(-180, 180)),
            organizationId);
        context.insertInto(Meetings.MEETINGS).set(record).execute();

        assertThat(context.fetchCount(context.selectFrom(Meetings.MEETINGS))).isEqualTo(1);

        addressProvider.delete(organizationId, record.getId());

        assertThat(context.fetchCount(context.selectFrom(Meetings.MEETINGS))).isZero();
    }

    @Test
    @DisplayName("Проверка сохранения")
    void test_save() {
        var expected = Instancio.of(MeetingAddress.class)
            .ignore(Select.field(MeetingAddress::getId))
            .set(Select.field(MeetingAddress::getLatitude), BigDecimal.valueOf(new Random().nextDouble(-90, 90)))
            .set(Select.field(MeetingAddress::getLongitude), BigDecimal.valueOf(new Random().nextDouble(-180, 180)))
            .create();

        var saved = addressProvider.save(expected);

        assertThat(saved).isEqualTo(expected);

        var actual = context.selectFrom(Meetings.MEETINGS).fetchOne();

        assertThat(actual).isNotNull();
        assertThat(actual.getBuilding()).isEqualTo(expected.getBuilding());
        assertThat(actual.getCity()).isEqualTo(expected.getCity());
        assertThat(actual.getLabel()).isEqualTo(expected.getLabel());
        assertThat(actual.getCountry()).isEqualTo(expected.getCountry());
        assertThat(actual.getHouse()).isEqualTo(expected.getHouse());
        assertThat(actual.getLatitude()).isEqualTo(expected.getLatitude().round(new MathContext(actual.getLatitude().precision())));
        assertThat(actual.getLongitude()).isEqualTo(expected.getLongitude().round(new MathContext(actual.getLongitude().precision())));
        assertThat(actual.getRegion()).isEqualTo(expected.getRegion());
        assertThat(actual.getStreet()).isEqualTo(expected.getStreet());
        assertThat(actual.getStructure()).isEqualTo(expected.getStructure());
    }

}