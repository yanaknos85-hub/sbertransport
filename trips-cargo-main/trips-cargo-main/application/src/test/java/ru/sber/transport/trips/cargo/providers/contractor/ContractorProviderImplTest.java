package ru.sber.transport.trips.cargo.providers.contractor;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.messages.ContractorMessage;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.trips.cargo.providers.contractor.mapper.ContractorMapper;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trips_cargo.database.trips_cargo.Tables;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.ContractorsRecord;
import ru.sber.transport.trips_cargo.database.trips_cargo.tables.records.TripsRecord;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@DisplayName("Проверка провайдера контрагентов")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@UnitTest
@IsolatedTest
@Feature("app_platform_trips_cargo")
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

        dslContext.insertInto(Tables.CONTRACTORS).set(contractor).execute();

        dslContext.insertInto(Tables.TRIPS).set(trip).execute();

        var provider = new ContractorProviderImpl(dslContext, contractorMapper);

        assertThat(provider.nextDigit(id)).isEqualTo(2);
    }

}