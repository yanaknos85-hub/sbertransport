package ru.sber.transport.etrn.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EtrnCardStatusTest {

    @Test
    @DisplayName("EtrnCardStatus — ровно 6 статусов в порядке жизненного цикла")
    void values_returnsExactlySixLifecycleStatuses() {
        assertThat(EtrnCardStatus.values())
                .containsExactly(
                        EtrnCardStatus.IDENTIFIED,
                        EtrnCardStatus.WAIT_KORUS_DATA,
                        EtrnCardStatus.WAIT_CONDITIONS,
                        EtrnCardStatus.READY_FOR_BANK_ACTION,
                        EtrnCardStatus.WAIT_KORUS_CONFIRMATION,
                        EtrnCardStatus.PROCESS_COMPLETED
                );
    }

    @Test
    @DisplayName("EtrnCardStatus — все значения имеют описание")
    void allStatuses_haveDescription() {
        for (EtrnCardStatus status : EtrnCardStatus.values()) {
            assertThat(status.getDescription())
                    .as("Description for " + status.name())
                    .isNotNull()
                    .isNotBlank();
        }
    }

    @Test
    @DisplayName("EtrnCardStatus — проверять описание IDENTIFIED")
    void identified_hasCorrectDescription() {
        assertThat(EtrnCardStatus.IDENTIFIED.getDescription())
                .isEqualTo("Карточка создана, идентичность разрешена");
    }

    @Test
    @DisplayName("EtrnCardStatus — проверять описание PROCESS_COMPLETED")
    void processCompleted_hasCorrectDescription() {
        assertThat(EtrnCardStatus.PROCESS_COMPLETED.getDescription())
                .isEqualTo("Процесс завершён");
    }
}
