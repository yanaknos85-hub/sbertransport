package ru.sber.transport.telemechanic.enumerate;

import org.junit.jupiter.api.Test;
import ru.sber.transport.telemechanic.common.EwbTitleType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EwbTitleTypeTest {
    
    @Test
    void shouldReturnNextEwbTitleType() {
        assertEquals(EwbTitleType.SECOND, EwbTitleType.getNextTitleType(EwbTitleType.FIRST));
        assertEquals(EwbTitleType.THIRD, EwbTitleType.getNextTitleType(EwbTitleType.SECOND));
        assertEquals(EwbTitleType.FOURTH, EwbTitleType.getNextTitleType(EwbTitleType.THIRD));
        assertEquals(EwbTitleType.FIFTH, EwbTitleType.getNextTitleType(EwbTitleType.FOURTH));
        assertNull(EwbTitleType.getNextTitleType(EwbTitleType.FIFTH));
    }
}
