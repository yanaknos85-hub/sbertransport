package ru.sber.transport.cargo.exchange.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.transport.cargo.exchange.request.RequestApplication;
import ru.sber.transport.cargo.exchange.request.database.dao.HumanReadableIdCounterRepository;
import ru.sber.transport.cargo.exchange.request.database.model.HumanReadableIdCounter;
import ru.sber.transport.cargo.exchange.request.database.model.HumanReadableIdCounterId;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка humanReadableIdGenerator")
@SpringBootTest(classes = RequestApplication.class, properties = {"logger.level.root=debug", "spring.main.cloud-platform=none"})
@ActiveProfiles("test")
@EmbeddedPostgres
class HumanReadableIdGeneratorIntegrationTest {

    @Autowired
    private HumanReadableIdGenerator generator;

    @Autowired
    private HumanReadableIdCounterRepository counterRepository;

    LocalDateTime fixedDateTime = LocalDateTime.of(2025, 4, 15, 10, 0);


    private String currentYearMonth = "202504";

    @BeforeEach
    void setUp() {
        // Очистка таблицы перед каждым тестом
        counterRepository.deleteAll();
    }

    @Test
    void shouldGenerateSequentialIdsForSamePrefix() {
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);

            // When
            String id1 = generator.generateHumanReadableId("ОР");
            String id2 = generator.generateHumanReadableId("ОР");
            // Then
            assertThat(id1).isEqualTo("ОР-202504-00000001");
            assertThat(id2).isEqualTo("ОР-202504-00000002");

        // Проверим состояние счётчика в БД
        var counter = counterRepository.findById(new HumanReadableIdCounterId(currentYearMonth, "ОР")).orElse(null);
        assertThat(counter).isNotNull();
        assertThat(counter.getNextVal()).isEqualTo(2L); // следующий будет 3
        }
    }

    @Test
    void shouldGenerateIndependentCountersForDifferentPrefixes() {
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
            // When
            String id1 = generator.generateHumanReadableId("ОР");
            String id2 = generator.generateHumanReadableId("АУК");
            String id3 = generator.generateHumanReadableId("ОР");
            String id4 = generator.generateHumanReadableId("ФРХ");

            // Then
            assertThat(id1).isEqualTo("ОР-202504-00000001");
            assertThat(id2).isEqualTo("АУК-202504-00000001");
            assertThat(id3).isEqualTo("ОР-202504-00000002");
            assertThat(id4).isEqualTo("ФРХ-202504-00000001");

            // Проверим счётчики
            var orCounter = counterRepository.findById(new HumanReadableIdCounterId(currentYearMonth, "ОР")).orElse(null);
            var aukCounter = counterRepository.findById(new HumanReadableIdCounterId(currentYearMonth, "АУК")).orElse(null);
            var frhCounter = counterRepository.findById(new HumanReadableIdCounterId(currentYearMonth, "ФРХ")).orElse(null);

            assertThat(orCounter.getNextVal()).isEqualTo(2L);
            assertThat(aukCounter.getNextVal()).isEqualTo(1L);
            assertThat(frhCounter.getNextVal()).isEqualTo(1L);
        }
    }

    @Test
    void shouldUseDefaultPrefixWhenNotSpecified() {
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
            // Given — default prefix is "ОР" (from application.properties)

            // When
            String id1 = generator.generateHumanReadableId();
            String id2 = generator.generateHumanReadableId();

            // Then
            assertThat(id1).isEqualTo("ОР-202504-00000001");
            assertThat(id2).isEqualTo("ОР-202504-00000002");
        }
    }

    @Test
    void shouldResetCounterPerMonth() {
        try (MockedStatic<LocalDateTime> mockedStatic = Mockito.mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedDateTime);
            // Создадим данные за предыдущий месяц
            var prevMonthCounter = new HumanReadableIdCounter();
            prevMonthCounter.setYearMonth("202503");
            prevMonthCounter.setPrefix("ОР");
            prevMonthCounter.setNextVal(50L);
            counterRepository.save(prevMonthCounter);

            // When — в апреле счётчик начинается заново
            String id = generator.generateHumanReadableId("ОР");

            // Then
            assertThat(id).isEqualTo("ОР-202504-00000001");
        }
    }
}