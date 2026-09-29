package ru.sber.transport.etrn.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockStatusTest {

    @Test
    @DisplayName("LockStatus — все значения имеют описание")
    void allStatuses_haveDescription() {
        for (LockStatus status : LockStatus.values()) {
            assertThat(status.getDescription())
                    .as("Description for " + status.name())
                    .isNotNull()
                    .isNotBlank();
        }
    }

    @Test
    @DisplayName("LockStatus — проверять ACQUIRING")
    void acquiring_hasCorrectDescription() {
        assertThat(LockStatus.ACQUIRING.getDescription())
                .isEqualTo("Блокировка захватывается");
    }

    @Test
    @DisplayName("LockStatus — проверять LOCKED_BY_OTHER")
    void lockedByOther_hasCorrectDescription() {
        assertThat(LockStatus.LOCKED_BY_OTHER.getDescription())
                .isEqualTo("Заблокировано другим пользователем");
    }
}
