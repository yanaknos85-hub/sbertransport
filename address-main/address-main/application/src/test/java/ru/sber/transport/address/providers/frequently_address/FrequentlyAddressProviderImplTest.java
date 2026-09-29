package ru.sber.transport.address.providers.frequently_address;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.address.business.provider.AddressProvider;
import ru.sber.transport.address.messaging.providers.FrequentlyAddressProvider;
import ru.sber.transport.address.providers.frequently_address.mapper.FrequentlyAddressMapperDatabaseImpl;
import ru.sber.transport.database.addresses.tables.Frequently;
import ru.sber.transport.database.addresses.tables.records.FrequentlyRecord;
import ru.sber.transport.scripting.ScriptUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка провайдера частых адресов")
@JooqTest
@MockBean(ScriptUtils.class)
@AutoConfigurationPackage
@Transactional
@ContextConfiguration(classes = {FrequentlyAddressProviderImpl.class, FrequentlyAddressMapperDatabaseImpl.class})
@Import({JooqDatabaseConfig.class, FrequentlyAddressProviderImpl.class, FrequentlyAddressMapperDatabaseImpl.class})
@ActiveProfiles("test")
class FrequentlyAddressProviderImplTest {

    @Autowired
    private AddressProvider<FrequentlyAddress> searchProvider;

    @Autowired
    private FrequentlyAddressProvider addressProvider;

    @Autowired
    private DSLContext context;

    public static Stream<Arguments> search_data_source() {
        var userId = UUID.randomUUID();
        var rec1 = createRecord(userId, "Россия");
        var rec2 = createRecord(userId, "россия", "Москва");
        var rec3 = createRecord(userId, "рОссия", "Ленинградская область", "москва");
        var rec4 = createRecord(userId, "Русь", "Московия", "Ленино", "Кутузовский проспект");
        var rec5 = createRecord(userId, "Руска", "Санкт-Петербург", "Купчино", "Ленина", "5");
        var rec6 = createRecord(userId, "РФ", "СПб", "Куп", "московская", "50", "6");
        var rec7 = createRecord(userId, "Российская Федерация", "Ивановская", "Иваново", "Клени", "15", "26", "37");
        var source = List.of(rec1, rec2, rec3, rec4, rec5, rec6, rec7);

        var args = new LinkedList<Arguments>();
        args.add(Arguments.of(userId, source, "Рос", List.of(rec1, rec2, rec3, rec7)));
        args.add(Arguments.of(userId, source, "Рус", List.of(rec4, rec5)));
        args.add(Arguments.of(userId, source, "Россия", List.of(rec1, rec2, rec3)));
        args.add(Arguments.of(userId, source, "Российская Федерация", List.of(rec7)));
        args.add(Arguments.of(userId, source, "мос", List.of(rec2, rec3, rec4, rec6)));
        args.add(Arguments.of(userId, source, "Ленин", List.of(rec3, rec4, rec5)));
        args.add(Arguments.of(userId, source, "15", List.of(rec7)));
        args.add(Arguments.of(userId, source, "5", List.of(rec5, rec6)));
        args.add(Arguments.of(userId, source, "Ро", List.of(rec1, rec2, rec3, rec7)));
        args.add(Arguments.of(userId, source, "Рос", List.of(rec1, rec2, rec3, rec7)));
        args.add(Arguments.of(userId, source, "Росс", List.of(rec1, rec2, rec3, rec7)));
        args.add(Arguments.of(userId, source, "Росси", List.of(rec1, rec2, rec3, rec7)));
        args.add(Arguments.of(userId, source, "Россия", List.of(rec1, rec2, rec3)));
        args.add(Arguments.of(userId, source, "Россия ", List.of(rec1, rec2, rec3)));
        args.add(Arguments.of(userId, source, "Россия М", List.of(rec3, rec2)));
        args.add(Arguments.of(userId, source, "Россия Мо", List.of(rec3, rec2)));
        args.add(Arguments.of(userId, source, "Россия Мос", List.of(rec3, rec2)));
        args.add(Arguments.of(userId, source, "Россия Моск", List.of(rec3, rec2)));
        args.add(Arguments.of(userId, source, "Россия Москв", List.of(rec3, rec2)));
        args.add(Arguments.of(userId, source, "Россия Москва", List.of(rec3, rec2)));

        return args.stream();
    }

    @ParameterizedTest
    @MethodSource("search_data_source")
    @DisplayName("Получение адресов по строке")
    void test_getAddresses(UUID userId, List<FrequentlyRecord> source, String search, List<FrequentlyRecord> expected) {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(userId.toString()).build()));

        source.forEach(s -> context.insertInto(Frequently.FREQUENTLY).set(s).onConflictDoNothing().execute());

        var actualList = searchProvider.getAddresses(search);

        assertThat(actualList.parallelStream().map(FrequentlyAddress::getId).toList()).hasSameElementsAs(expected.parallelStream().map(FrequentlyRecord::getId).toList());
    }

    @Test
    @DisplayName("Получение адреса по координатам")
    void test_getAddress() {
        var record = createRecord();
        context.insertInto(Frequently.FREQUENTLY).set(record).execute();

        var actualOpt = searchProvider.getAddress(record.getLatitude(), record.getLongitude());

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();


        assertSoftly(assertion -> {
            assertion.assertThat(actual.getBuilding()).isEqualTo(record.getBuilding());
            assertion.assertThat(actual.getCity()).isEqualTo(record.getCity());
            assertion.assertThat(actual.getCount()).isEqualTo(record.getUsages());
            assertion.assertThat(actual.isFirst()).isEqualTo(record.getFirst());
            assertion.assertThat(actual.getCountry()).isEqualTo(record.getCountry());
            assertion.assertThat(actual.getHouse()).isEqualTo(record.getHouse());
            assertion.assertThat(actual.getLatitude()).isEqualTo(record.getLatitude().setScale(actual.getLatitude().scale(), RoundingMode.HALF_EVEN));
            assertion.assertThat(actual.getLongitude()).isEqualTo(record.getLongitude().setScale(actual.getLongitude().scale(), RoundingMode.HALF_EVEN));
            assertion.assertThat(actual.getRegion()).isEqualTo(record.getRegion());
            assertion.assertThat(actual.getStreet()).isEqualTo(record.getStreet());
            assertion.assertThat(actual.getStructure()).isEqualTo(record.getStructure());
        });

        assertThat(searchProvider.getAddress(Instancio.create(BigDecimal.class), Instancio.create(BigDecimal.class))).isEmpty();
    }

    @Test
    @DisplayName("Проверка существования")
    void test_exists() {
        assertThat(addressProvider.exists(null, UUID.randomUUID(), UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("Получение адресов владельца")
    void test_getOfOwner() {
        var record = createRecord();
        context.insertInto(Frequently.FREQUENTLY).set(record).execute();

        var record2 = createRecord();
        context.insertInto(Frequently.FREQUENTLY).set(record2).execute();

        var actualOpt = addressProvider.getOfOwner(record.getOwnerId());

        assertThat(actualOpt).isNotEmpty().hasSize(1);

        var actual = actualOpt.iterator().next();

        assertThat(actual.getBuilding()).isEqualTo(record.getBuilding());
        assertThat(actual.getCity()).isEqualTo(record.getCity());
        assertThat(actual.getCount()).isEqualTo(record.getUsages());
        assertThat(actual.isFirst()).isEqualTo(record.getFirst());
        assertThat(actual.getCountry()).isEqualTo(record.getCountry());
        assertThat(actual.getHouse()).isEqualTo(record.getHouse());
        assertThat(actual.getLatitude()).isEqualTo(record.getLatitude().round(new MathContext(actual.getLatitude().precision())));
        assertThat(actual.getLongitude()).isEqualTo(record.getLongitude().round(new MathContext(actual.getLongitude().precision())));
        assertThat(actual.getRegion()).isEqualTo(record.getRegion());
        assertThat(actual.getStreet()).isEqualTo(record.getStreet());
        assertThat(actual.getStructure()).isEqualTo(record.getStructure());

        assertThat(addressProvider.getOfOwner(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Получение адреса владельца")
    void test_getOneOfOwner() {
        var record = createRecord();
        context.insertInto(Frequently.FREQUENTLY).set(record).execute();

        var record2 = createRecord();
        context.insertInto(Frequently.FREQUENTLY).set(record2).execute();

        var actualOpt = addressProvider.get(record.getOwnerId(), record.getId());

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();

        assertThat(actual.getBuilding()).isEqualTo(record.getBuilding());
        assertThat(actual.getCity()).isEqualTo(record.getCity());
        assertThat(actual.getCount()).isEqualTo(record.getUsages());
        assertThat(actual.isFirst()).isEqualTo(record.getFirst());
        assertThat(actual.getCountry()).isEqualTo(record.getCountry());
        assertThat(actual.getHouse()).isEqualTo(record.getHouse());
        assertThat(actual.getLatitude()).isEqualTo(record.getLatitude().round(new MathContext(actual.getLatitude().precision())));
        assertThat(actual.getLongitude()).isEqualTo(record.getLongitude().round(new MathContext(actual.getLongitude().precision())));
        assertThat(actual.getRegion()).isEqualTo(record.getRegion());
        assertThat(actual.getStreet()).isEqualTo(record.getStreet());
        assertThat(actual.getStructure()).isEqualTo(record.getStructure());

        assertThat(addressProvider.get(record2.getOwnerId(), record.getId())).isEmpty();
        assertThat(addressProvider.get(record.getOwnerId(), record2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Проверка сохранения")
    void test_save() {
        var expected = Instancio.of(FrequentlyAddress.class)
            .set(Select.field(FrequentlyAddress::getLatitude), BigDecimal.valueOf(new Random().nextDouble(-90, 90)))
            .set(Select.field(FrequentlyAddress::getLongitude), BigDecimal.valueOf(new Random().nextDouble(-180, 180)))
            .create();

        var saved = addressProvider.save(expected);

        assertThat(saved).isEqualTo(expected);

        var actual = context.selectFrom(Frequently.FREQUENTLY).fetchOne();

        assertThat(actual).isNotNull();
        assertThat(actual.getBuilding()).isEqualTo(expected.getBuilding());
        assertThat(actual.getCity()).isEqualTo(expected.getCity());
        assertThat(actual.getUsages()).isEqualTo(expected.getCount());
        assertThat(actual.getCountry()).isEqualTo(expected.getCountry());
        assertThat(actual.getHouse()).isEqualTo(expected.getHouse());
        assertThat(actual.getLatitude()).isEqualTo(expected.getLatitude().round(new MathContext(actual.getLatitude().precision())));
        assertThat(actual.getLongitude()).isEqualTo(expected.getLongitude().round(new MathContext(actual.getLongitude().precision())));
        assertThat(actual.getRegion()).isEqualTo(expected.getRegion());
        assertThat(actual.getStreet()).isEqualTo(expected.getStreet());
        assertThat(actual.getStructure()).isEqualTo(expected.getStructure());
        assertThat(actual.getOwnerId()).isEqualTo(expected.getOwner());
        assertThat(actual.getId()).isEqualTo(saved.getId());

        //noinspection CatchMayIgnoreException
        try {
            addressProvider.save(expected);
        } catch (Exception e) {
            fail("Multiple saved must not to provoke an exception", e);
        }
    }

    @Test
    @DisplayName("Проверка провайдера поиска по строке. null")
    void test_search_request_null() {
        //noinspection DataFlowIssue
        assertThatThrownBy(() -> searchProvider.getAddresses(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Проверка провайдера поиска по координатам. null")
    void test_search_coordinates_null() {
        //noinspection DataFlowIssue
        assertThatThrownBy(() -> searchProvider.getAddress(null, BigDecimal.ONE))
            .isInstanceOf(NullPointerException.class);

        //noinspection DataFlowIssue
        assertThatThrownBy(() -> searchProvider.getAddress(BigDecimal.ONE, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Удаление адреса владельца")
    void test_delete_ofOwner() {
        var record = createRecord();
        context.insertInto(Frequently.FREQUENTLY).set(record).execute();

        assertThat(context.fetchCount(context.selectFrom(Frequently.FREQUENTLY))).isEqualTo(1);

        addressProvider.delete(UUID.randomUUID(), record.getId());
        assertThat(context.fetchCount(context.selectFrom(Frequently.FREQUENTLY))).isEqualTo(1);

        addressProvider.delete(record.getOwnerId(), UUID.randomUUID());
        assertThat(context.fetchCount(context.selectFrom(Frequently.FREQUENTLY))).isEqualTo(1);

        addressProvider.delete(record.getOwnerId(), record.getId());

        assertThat(context.fetchCount(context.selectFrom(Frequently.FREQUENTLY))).isZero();
    }

    private static @NotNull FrequentlyRecord createRecord() {
        return createRecord(UUID.randomUUID());
    }

    private static @NotNull FrequentlyRecord createRecord(UUID userId) {
        return createRecord(userId, null);
    }

    private static @NotNull FrequentlyRecord createRecord(UUID userId, String country) {
        return createRecord(userId, country, null);
    }

    private static @NotNull FrequentlyRecord createRecord(UUID userId, String country, String region) {
        return createRecord(userId, country, region, null);
    }

    private static @NotNull FrequentlyRecord createRecord(UUID userId, String country, String region, String city) {
        return createRecord(userId, country, region, city, null);
    }

    private static @NotNull FrequentlyRecord createRecord(UUID userId, String country, String region, String city, String street) {
        return createRecord(userId, country, region, city, street, null);
    }

    private static @NotNull FrequentlyRecord createRecord(UUID userId, String country, String region, String city, String street, String house) {
        return createRecord(userId, country, region, city, street, house, null);
    }

    private static @NotNull FrequentlyRecord createRecord(UUID userId, String country, String region, String city, String street, String house, String building) {
        return createRecord(userId, country, region, city, street, house, building, null);
    }

    private static @NotNull FrequentlyRecord createRecord(UUID userId, String country, String region, String city, String street, String house, String building, String structure) {
        var frequentlyRecord = new FrequentlyRecord(
            UUID.randomUUID(),
            userId,
            Instancio.create(Integer.class),
            Instancio.create(Boolean.class),
            country,
            region,
            city,
            street,
            house,
            building,
            structure,
            BigDecimal.valueOf(new Random().nextDouble(-90, 90)),
            BigDecimal.valueOf(new Random().nextDouble(-180, 180)),
            null);
        frequentlyRecord.calculateVector();
        return frequentlyRecord;
    }

}