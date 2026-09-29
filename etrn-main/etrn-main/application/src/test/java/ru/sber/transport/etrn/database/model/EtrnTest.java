package ru.sber.transport.etrn.database.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.transport.etrn.database.dao.EtrnRepository;
import ru.sber.transport.etrn.exceptions.EtrnNotFoundException;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA-тесты для модели Etrn — prePersist и preUpdate работают
 * через JPA-провайдер при save/flush.
 */
@EmbeddedPostgres
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Тесты модели Etrn через JPA")
class EtrnTest {

    private static final String HUMAN_READABLE_ID_1 = "ETRn-2026-001234";
    private static final String HUMAN_READABLE_ID_2 = "ETRn-2026-001235";
    private static final String STATUS_IDENTIFIED = "IDENTIFIED";
    private static final String STATUS_WAIT_KORUS_DATA = "WAIT_KORUS_DATA";

    @Autowired
    private EtrnRepository repository;

    @Test
    @DisplayName("prePersist — установка createdAt и updatedAt при сохранении")
    void prePersist_shouldSetTimestampsOnSave() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        Etrn etrn = Etrn.builder()
                .humanReadableId(HUMAN_READABLE_ID_1)
                .status(STATUS_IDENTIFIED)
                .active(true)
                .version(0L)
                .build();

        repository.saveAndFlush(etrn);

        Etrn saved = repository.findByHumanReadableId(HUMAN_READABLE_ID_1)
                .orElseThrow(() -> new EtrnNotFoundException(HUMAN_READABLE_ID_1));

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertThat(saved.getCreatedAt()).isBetween(before, after);
        assertThat(saved.getUpdatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("preUpdate — обновление updatedAt при изменении")
    void preUpdate_shouldUpdateTimestampOnSaveChanges() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        Etrn etrn = Etrn.builder()
                .humanReadableId(HUMAN_READABLE_ID_2)
                .status(STATUS_IDENTIFIED)
                .active(true)
                .version(0L)
                .build();

        var saved = repository.saveAndFlush(etrn);

        LocalDateTime initialUpdatedAt = repository.findByHumanReadableId(HUMAN_READABLE_ID_2)
                .orElseThrow(() -> new EtrnNotFoundException(HUMAN_READABLE_ID_2)).getUpdatedAt();
        assertThat(initialUpdatedAt).isAfterOrEqualTo(before);

        // Обновляем
        saved.setStatus(STATUS_WAIT_KORUS_DATA);
        repository.saveAndFlush(saved);

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        Etrn updated = repository.findByHumanReadableId(HUMAN_READABLE_ID_2)
                .orElseThrow(() -> new EtrnNotFoundException(HUMAN_READABLE_ID_2));

        assertThat(updated.getUpdatedAt()).isAfter(initialUpdatedAt);
        assertThat(updated.getUpdatedAt()).isBefore(after);
        assertThat(updated.getStatus()).isEqualTo(STATUS_WAIT_KORUS_DATA);
    }
}
