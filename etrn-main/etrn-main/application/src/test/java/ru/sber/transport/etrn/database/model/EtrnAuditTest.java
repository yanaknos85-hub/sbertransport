package ru.sber.transport.etrn.database.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EtrnAuditTest {

    private EtrnAudit audit;

    @BeforeEach
    void setUp() {
        audit = EtrnAudit.builder()
                .etrnId(UUID.randomUUID())
                .action("ЭТрН создана")
                .details("humanReadableId=ETRn-2026-001234")
                .build();
    }

    @Test
    @DisplayName("Builder — создание записи аудита")
    void builder_shouldCreateInstance() {
        assertThat(audit.getEtrnId()).isNotNull();
        assertThat(audit.getAction()).isEqualTo("ЭТрН создана");
        assertThat(audit.getDetails()).isEqualTo("humanReadableId=ETRn-2026-001234");
        assertThat(audit.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Setter — установка createdBy")
    void setCreatedBy_shouldSetValue() {
        UUID createdBy = UUID.randomUUID();
        audit.setCreatedBy(createdBy);

        assertThat(audit.getCreatedBy()).isEqualTo(createdBy);
    }

    @Test
    @DisplayName("NoArgsConstructor — пустой конструктор")
    void noArgsConstructor_shouldCreateEmptyInstance() {
        EtrnAudit empty = new EtrnAudit();
        assertThat(empty.getId()).isNull();
        assertThat(empty.getEtrnId()).isNull();
        assertThat(empty.getAction()).isNull();
        assertThat(empty.getDetails()).isNull();
        assertThat(empty.getCreatedBy()).isNull();
        assertThat(empty.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("AllArgsConstructor — полный конструктор")
    void allArgsConstructor_shouldSetAllFields() {
        UUID id = UUID.randomUUID();
        UUID etrnId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        EtrnAudit full = new EtrnAudit(
                id,
                etrnId,
                "Статус изменён",
                "IDENTIFIED → READY_FOR_BANK_ACTION",
                createdBy,
                now
        );

        assertThat(full.getId()).isEqualTo(id);
        assertThat(full.getEtrnId()).isEqualTo(etrnId);
        assertThat(full.getAction()).isEqualTo("Статус изменён");
        assertThat(full.getDetails()).isEqualTo("IDENTIFIED → READY_FOR_BANK_ACTION");
        assertThat(full.getCreatedBy()).isEqualTo(createdBy);
        assertThat(full.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Setter — установка createdAt")
    void setCreatedAt_shouldSetValue() {
        LocalDateTime now = LocalDateTime.now();
        audit.setCreatedAt(now);

        assertThat(audit.getCreatedAt()).isEqualTo(now);
    }
}
