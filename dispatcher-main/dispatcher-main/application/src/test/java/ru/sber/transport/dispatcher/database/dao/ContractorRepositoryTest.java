package ru.sber.transport.dispatcher.database.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedPostgres
@Transactional
@DisplayName("Тесты для ContractorRepository с реальной БД")
class ContractorRepositoryTest extends KafkaTest {

@Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        contractorRepository.deleteAll();
    }

    @Test
    @DisplayName("Метод findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue не возвращает дубликаты")
    void findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue_noDuplicates() {
        saveContractor("7707083893", true, true);
        saveContractor("7701234567", true, true);
        saveContractor("7707083893", true, true);
        saveContractor("7709998888", true, false);

       var tins = contractorRepository.findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue();

        assertThat(tins).hasSize(2)
                .containsExactlyInAnyOrder("7707083893", "7701234567")
                .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Метод возвращает пустой список когда нет активных контрагентов")
    void findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue_empty() {
        var tins = contractorRepository.findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue();

        assertThat(tins).isEmpty();
    }

    @Test
    @DisplayName("Метод возвращает пустой список когда нет активных контрагентов с включенной настройкой")
    void findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue_noActiveContractors() {
        saveContractor("7707083893", false, true);
        saveContractor("7701234567", true, false);

        var tins = contractorRepository.findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue();

        assertThat(tins).isEmpty();
    }

    @Test
    @DisplayName("Метод возвращает уникальный список ИНН при наличии нескольких записей с одинаковым ИНН")
    void findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue_distinctValues() {
        saveContractor("7707083893", true, true);
        saveContractor("7707083893", true, true);
        saveContractor("7707083893", true, true);
        saveContractor("7701234567", true, true);

        var tins = contractorRepository.findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue();

        assertThat(tins).hasSize(2)
                        .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Метод возвращает ИНН только активных контрагентов с флагом isFineFetchRequired = true")
    void findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue_filtersCorrectly() {
        saveContractor("1111111111", true, true);
        saveContractor("2222222222", true, true);
        saveContractor("3333333333", false, true);
        saveContractor("4444444444", true, false);
        saveContractor("5555555555", false, false);

        var tins = contractorRepository.findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue();

        assertThat(tins).hasSize(2)
                .containsExactlyInAnyOrder("1111111111", "2222222222");
    }

    @Test
    @DisplayName("Метод корректно обрабатывает большой список контрагентов")
    void findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue_largeDataSet() {
        for (int i = 0; i < 100; i++) {
            saveContractor(String.format("%010d", i), true, true);
        }
        for (int i = 100; i < 150; i++) {
            saveContractor(String.format("%010d", i), false, true);
        }
        for (int i = 150; i < 200; i++) {
            saveContractor(String.format("%010d", i), true, false);
        }

        var tins = contractorRepository.findDistinctTinByActiveIsTrueAndIsFineFetchRequiredIsTrue();

        assertThat(tins).hasSize(100)
                .allMatch(tin -> Long.parseLong(tin) < 100);
    }

    private void saveContractor(String tin, boolean active, boolean isFineFetchRequired) {
        jdbcTemplate.update(
            """
                INSERT INTO dispatcher.contractor (id, name, tin, msrn, active, is_fine_fetch_required)
                VALUES (gen_random_uuid(), ?, ?, ?, ?, ?)
                """,
            tin + "@example.com",
            tin,
            "770708389300" + tin.substring(0, 2),
            active,
            isFineFetchRequired
        );
    }
}
