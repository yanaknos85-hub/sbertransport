package ru.sber.transport.trip.providers.contractor;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.SelectWhereStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.messages.ContractorMessage;
import ru.sber.transport.trip.business.dto.IntegrationType;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.database.trips.tables.records.ContractorsRecord;
import ru.sber.transport.trip.database.trips.tables.records.TripsRecord;
import ru.sber.transport.trip.providers.contractor.mapper.ContractorMapper;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@SpringBootTest
@DisplayName("Проверка провайдера контрагентов")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
class ContractorProviderImplTest extends KafkaTest {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ContractorMapper contractorMapper;


    @DisplayName("Проверка сохранения")
    @Test
    void test_save() throws ExecutionException, InterruptedException {
        var provider = new ContractorProviderImpl(dslContext, contractorMapper);

        var id = UUID.randomUUID();
        var message = Instancio.of(ContractorMessage.class)
                .set(Select.field(ContractorMessage::id), id)
                .create();

        var stage = provider.save(id, message);

//        var future = stage.toCompletableFuture();
//        await().pollInterval(Duration.ofSeconds(1)).timeout(Duration.ofSeconds(30))
//                .until(future::isDone);
//
//        assertThat(future.get()).isEqualTo(1);
        assertThat(stage).isEqualTo(1);

        var actual = dslContext.selectFrom(Tables.CONTRACTORS).where(Tables.CONTRACTORS.ID.eq(id)).fetchSingle();

        assertThat(actual.getDigitId().intValue()).isEqualTo(message.digitId());
        assertThat(actual.getId()).isEqualTo(message.getId());
    }

    @DisplayName("Получение следующего числа. Нет поездок")
    @Test
    void text_nextDigitId_noTrips() {
        var id = UUID.randomUUID();

        var contractor = new ContractorsRecord();
        contractor.setDigitId(BigInteger.valueOf(1));
        contractor.setId(id);

        dslContext.insertInto(Tables.CONTRACTORS).set(contractor).execute();

        var provider = new ContractorProviderImpl(dslContext, contractorMapper);

        assertThat(provider.nextDigit(id)).isEqualTo(1);
    }

    @DisplayName("Получение следующего числа. Есть поездки")
    @Test
    void text_nextDigitId_trips() {
        var id = UUID.randomUUID();

        var contractor = new ContractorsRecord();
        contractor.setDigitId(BigInteger.valueOf(1));
        contractor.setId(id);

        var trip = new TripsRecord();
        trip.setDigitId(BigInteger.valueOf(1));
        trip.setId(UUID.randomUUID());
        trip.setContractorId(id);
        trip.setRequests(JSON.valueOf("[]"));
        trip.setReportCreated(false);

        dslContext.insertInto(Tables.CONTRACTORS).set(contractor).execute();

        dslContext.insertInto(Tables.TRIPS_).set(trip).execute();

        var provider = new ContractorProviderImpl(dslContext, contractorMapper);

        assertThat(provider.nextDigit(id)).isEqualTo(2);
    }

    @DisplayName("Получение информации о типе интеграции водителя")
    @Test
    void text_isDispatcher() {
        var id = UUID.randomUUID();

        var contractor = new ContractorsRecord();
        contractor.setDigitId(BigInteger.valueOf(1));
        contractor.setId(id);
        contractor.setIntegrationType(IntegrationType.DISPATCHER.name());

        dslContext.insertInto(Tables.CONTRACTORS).set(contractor).execute();

        var provider = new ContractorProviderImpl(dslContext, contractorMapper);

        assertThat(provider.isDispatcher(id)).isTrue();
    }

    @Test
    @DisplayName("Получение списка контрагентов")
    void getContractorsByIdsTest() {
        var contractor1 = new ContractorsRecord();
        contractor1.setDigitId(BigInteger.valueOf(1));
        contractor1.setId(UUID.randomUUID());
        contractor1.setIntegrationType(IntegrationType.DISPATCHER.name());

        dslContext.insertInto(Tables.CONTRACTORS).set(contractor1).execute();

        var contractor2 = new ContractorsRecord();
        contractor2.setDigitId(BigInteger.valueOf(1));
        contractor2.setId(UUID.randomUUID());
        contractor2.setIntegrationType(IntegrationType.DISPATCHER.name());

        dslContext.insertInto(Tables.CONTRACTORS).set(contractor2).execute();

        var contractor3 = new ContractorsRecord();
        contractor3.setDigitId(BigInteger.valueOf(1));
        contractor3.setId(UUID.randomUUID());
        contractor3.setIntegrationType(IntegrationType.DISPATCHER.name());

        dslContext.insertInto(Tables.CONTRACTORS).set(contractor3).execute();

        var provider = new ContractorProviderImpl(dslContext, contractorMapper);

        assertEquals(2, provider.getContractorsByIds(List.of(contractor1.getId(), contractor2.getId())).size());
    }

//    @Test
//    void testGetContractorsByIds() {
//        // Моки
//        DSLContext dslContext = mock(DSLContext.class);
//        // Промежуточные шаги query - здесь проще мокать результат fetchInto
//        SelectWhereStep<Record> selectStep = mock(SelectWhereStep.class);
//
//        List<ContractorsRecord> expected = List.of(new ContractorsRecord(), new ContractorsRecord());
//
//        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
//
//        // Поведение моков
//        when(dslContext.selectFrom(Tables.CONTRACTORS)).thenReturn(selectStep);
//        when(selectStep.where(Tables.CONTRACTORS.ID.in(ids))).thenReturn(selectStep);
//        when(selectStep.fetchInto(ContractorsRecord.class)).thenReturn(expected);
//
//        // Класс с методом
//        var repository = new ContractorsRepository(dslContext);
//
//        // Вызов тестируемого метода
//        List<ContractorsRecord> result = repository.getContractorsByIds(ids);
//
//        // Проверка результата
//        assertNotNull(result);
//        assertEquals(expected.size(), result.size());
//        assertEquals(expected, result);
//
//        // Проверка правильности вызовов
//        verify(dslContext).selectFrom(Tables.CONTRACTORS);
//        verify(selectStep).where(Tables.CONTRACTORS.ID.in(ids));
//        verify(selectStep).fetchInto(ContractorsRecord.class);
//    }

}