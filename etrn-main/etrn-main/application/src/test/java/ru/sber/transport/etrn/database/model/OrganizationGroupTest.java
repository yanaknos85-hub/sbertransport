package ru.sber.transport.etrn.database.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationGroupTest {

    @Test
    @DisplayName("Builder — создание OrganizationGroup")
    void builder_shouldCreateInstance() {
        UUID id = UUID.randomUUID();
        OrganizationGroup group = OrganizationGroup.builder()
                .id(id)
                .name("Внутренние организации")
                .internal(true)
                .build();

        assertThat(group.getId()).isEqualTo(id);
        assertThat(group.getName()).isEqualTo("Внутренние организации");
        assertThat(group.isInternal()).isTrue();
    }

    @Test
    @DisplayName("Builder — internal по умолчанию false")
    void builder_defaultInternalIsFalse() {
        OrganizationGroup group = OrganizationGroup.builder()
                .id(UUID.randomUUID())
                .name("Внешние организации")
                .build();

        assertThat(group.isInternal()).isFalse();
    }

    @Test
    @DisplayName("NoArgsConstructor — пустой конструктор")
    void noArgsConstructor_shouldCreateEmptyInstance() {
        OrganizationGroup group = new OrganizationGroup();
        assertThat(group.getId()).isNull();
        assertThat(group.getName()).isNull();
        assertThat(group.isInternal()).isFalse();
    }
}
