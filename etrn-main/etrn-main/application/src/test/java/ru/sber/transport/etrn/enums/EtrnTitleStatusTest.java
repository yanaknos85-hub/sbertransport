package ru.sber.transport.etrn.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EtrnTitleStatusTest {

    @Test
    @DisplayName("EtrnTitleStatus — все значения имеют описание")
    void allStatuses_haveDescription() {
        for (EtrnTitleStatus status : EtrnTitleStatus.values()) {
            assertThat(status.getDescription())
                    .as("Description for " + status.name())
                    .isNotNull()
                    .isNotBlank();
        }
    }

    @Test
    @DisplayName("EtrnTitleStatus — проверять EXPECTED")
    void expected_hasCorrectDescription() {
        assertThat(EtrnTitleStatus.EXPECTED.getDescription())
                .isEqualTo("Т3 ожидается");
    }

    @Test
    @DisplayName("EtrnTitleStatus — проверять ACCEPTED_BY_OPERATOR")
    void acceptedByOperator_hasCorrectDescription() {
        assertThat(EtrnTitleStatus.ACCEPTED_BY_OPERATOR.getDescription())
                .isEqualTo("Принят оператором");
    }
}
