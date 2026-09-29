package ru.sber.transport.telemechanic.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringToMedicRequestStatusConverterTest {
    
    private final StringToMedicRequestStatusConverter converter = new StringToMedicRequestStatusConverter();
    
    @ParameterizedTest
    @EnumSource(TelemedicineStatus.class)
    void from(TelemedicineStatus status) {
        var actual = converter.from(status.name());
        assertEquals(actual, status);
    }
    
    @ParameterizedTest
    @EnumSource(TelemedicineStatus.class)
    void to(TelemedicineStatus status) {
        var actual = converter.to(status);
        assertEquals(actual, status.name());
    }
    
    @Test
    void fromType() {
        assertEquals(String.class, converter.fromType());
    }
    
    @Test
    void toType() {
        assertEquals(TelemedicineStatus.class, converter.toType());
    }
}
