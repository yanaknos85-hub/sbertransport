package ru.sber.transport.journal.helper;

import org.junit.jupiter.api.Test;
import ru.sberbank.ditsib.transport.exceptions.IllegalCallerResponseException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

class CanViewHelperTest {
    
    @Test
    void checkCanView() {
        var randomId1 = UUID.randomUUID();
        var randomId2 = UUID.randomUUID();
        var randomId3 = UUID.randomUUID();
        assertThatExceptionOfType(IllegalCallerResponseException.class)
                .isThrownBy(() -> CanViewHelper.checkCanView(randomId1, randomId2, randomId3))
                .withMessage("Только создатель или коллега, указанный в заявке, может ее изменять");
        assertThatNoException()
                .isThrownBy(() -> CanViewHelper.checkCanView(randomId1, randomId1, randomId3));
        assertThatNoException()
                .isThrownBy(() -> CanViewHelper.checkCanView(randomId1, randomId2, randomId1));
        assertThatNoException()
                .isThrownBy(() -> CanViewHelper.checkCanView(randomId1, randomId1, null));
    }
}