package ru.sber.transport.address.providers.favorite_address;

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
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.address.business.provider.AddressProvider;
import ru.sber.transport.address.messaging.providers.FavoriteAddressProvider;
import ru.sber.transport.address.providers.favorite_address.mapper.FavoriteAddressMapperDatabaseImpl;
import ru.sber.transport.database.addresses.tables.Favorite;
import ru.sber.transport.database.addresses.tables.records.FavoriteRecord;
import ru.sber.transport.scripting.ScriptUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JooqTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка провайдера предпочтительных адресов")
@MockBean(ScriptUtils.class)
@AutoConfigurationPackage
@Transactional
@Import({JooqDatabaseConfig.class, FavoriteAddressProviderImpl.class, FavoriteAddressMapperDatabaseImpl.class})
@ContextConfiguration(classes = {JooqDatabaseConfig.class, FavoriteAddressProviderImpl.class, FavoriteAddressMapperDatabaseImpl.class})
@ActiveProfiles("test")
class FavoriteAddressProviderImplTest {

    @Autowired
    private AddressProvider<FavoriteAddress> searchProvider;

    @Autowired
    private FavoriteAddressProvider addressProvider;

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

    @ParameterizedTest
    @MethodSource("search_data_source")
    @DisplayName("Получение адресов по строке")
    void test_getAddresses(UUID userId, List<FavoriteRecord> source, String search, List<FavoriteRecord> expected) {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(userId.toString()).build()));

        source.forEach(s -> context.insertInto(Favorite.FAVORITE).set(s).onConflictDoNothing().execute());

        var actualList = searchProvider.getAddresses(search);
        context.selectFrom(Favorite.FAVORITE).fetchInto(FavoriteRecord.class);

        assertThat(actualList.parallelStream().map(FavoriteAddress::getId).toList()).hasSameElementsAs(expected.parallelStream().map(FavoriteRecord::getId).toList());
    }

    @Test
    @DisplayName("Получение адреса по координатам")
    void test_getAddress() {
        var record = createRecord();
        context.insertInto(Favorite.FAVORITE).set(record).execute();

        var actualOpt = searchProvider.getAddress(record.getLatitude(), record.getLongitude());

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();

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

        assertThat(searchProvider.getAddress(Instancio.create(BigDecimal.class), Instancio.create(BigDecimal.class))).isEmpty();
    }

    @Test
    @DisplayName("Получение адреса")
    void test_get() {
        var record = createRecord();
        context.insertInto(Favorite.FAVORITE).set(record).execute();

        var actualOpt = addressProvider.get(record.getId());

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();

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

        assertThat(addressProvider.get(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Проверка сохранения")
    void test_save() {
        var expected = Instancio.of(FavoriteAddress.class)
            .ignore(Select.field(FavoriteAddress::getId))
            .set(Select.field(FavoriteAddress::getLatitude), BigDecimal.valueOf(new Random().nextDouble(-90, 90)))
            .set(Select.field(FavoriteAddress::getLongitude), BigDecimal.valueOf(new Random().nextDouble(-180, 180)))
            .create();

        var saved = addressProvider.save(expected);

        assertThat(saved).isEqualTo(expected);

        var actual = context.selectFrom(Favorite.FAVORITE).fetchOne();

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
        assertThat(actual.getOwnerId()).isEqualTo(expected.getOwner());
        assertThat(actual.getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("Проверка существования")
    void test_exists() {
        var record = createRecord();
        context.insertInto(Favorite.FAVORITE).set(record).execute();

        assertThat(addressProvider.exists(record.getLabel(), record.getOwnerId(), record.getId())).isFalse();
        assertThat(addressProvider.exists(record.getLabel(), record.getOwnerId(), UUID.randomUUID())).isTrue();
    }

    @Test
    @DisplayName("Получение адресов владельца")
    void test_getOfOwner() {
        var record = createRecord();
        context.insertInto(Favorite.FAVORITE).set(record).execute();

        var record2 = createRecord();
        context.insertInto(Favorite.FAVORITE).set(record2).execute();

        var actualOpt = addressProvider.getOfOwner(record.getOwnerId());

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

        assertThat(addressProvider.getOfOwner(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Получение адреса владельца")
    void test_getOneOfOwner() {
        var record = createRecord();
        context.insertInto(Favorite.FAVORITE).set(record).execute();

        var record2 = createRecord();
        context.insertInto(Favorite.FAVORITE).set(record2).execute();

        var actualOpt = addressProvider.get(record.getOwnerId(), record.getId());

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.orElseThrow();

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

        assertThat(addressProvider.get(record2.getOwnerId(), record.getId())).isEmpty();
        assertThat(addressProvider.get(record.getOwnerId(), record2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Удаление адреса")
    void test_delete() {
        var record = createRecord();
        context.insertInto(Favorite.FAVORITE).set(record).execute();

        assertThat(context.fetchCount(context.selectFrom(Favorite.FAVORITE))).isEqualTo(1);

        addressProvider.delete(record.getId());

        assertThat(context.fetchCount(context.selectFrom(Favorite.FAVORITE))).isZero();
    }

    @Test
    @DisplayName("Удаление адреса владельца")
    void test_delete_ofOwner() {
        var record = createRecord();
        context.insertInto(Favorite.FAVORITE).set(record).execute();

        assertThat(context.fetchCount(context.selectFrom(Favorite.FAVORITE))).isEqualTo(1);

        addressProvider.delete(UUID.randomUUID(), record.getId());
        assertThat(context.fetchCount(context.selectFrom(Favorite.FAVORITE))).isEqualTo(1);

        addressProvider.delete(record.getOwnerId(), UUID.randomUUID());
        assertThat(context.fetchCount(context.selectFrom(Favorite.FAVORITE))).isEqualTo(1);

        addressProvider.delete(record.getOwnerId(), record.getId());

        assertThat(context.fetchCount(context.selectFrom(Favorite.FAVORITE))).isZero();
    }

    private static @NotNull FavoriteRecord createRecord() {
        return createRecord(UUID.randomUUID());
    }

    private static @NotNull FavoriteRecord createRecord(UUID userId) {
        return createRecord(userId, null);
    }

    private static @NotNull FavoriteRecord createRecord(UUID userId, String country) {
        return createRecord(userId, country, null);
    }

    private static @NotNull FavoriteRecord createRecord(UUID userId, String country, String region) {
        return createRecord(userId, country, region, null);
    }

    private static @NotNull FavoriteRecord createRecord(UUID userId, String country, String region, String city) {
        return createRecord(userId, country, region, city, null);
    }

    private static @NotNull FavoriteRecord createRecord(UUID userId, String country, String region, String city, String street) {
        return createRecord(userId, country, region, city, street, null);
    }

    private static @NotNull FavoriteRecord createRecord(UUID userId, String country, String region, String city, String street, String house) {
        return createRecord(userId, country, region, city, street, house, null);
    }

    private static @NotNull FavoriteRecord createRecord(UUID userId, String country, String region, String city, String street, String house, String building) {
        return createRecord(userId, country, region, city, street, house, building, null);
    }

    private static @NotNull FavoriteRecord createRecord(UUID userId, String country, String region, String city, String street, String house, String building, String structure) {
        var frequentlyRecord = new FavoriteRecord(
            UUID.randomUUID(),
            Instancio.create(String.class),
            userId,
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