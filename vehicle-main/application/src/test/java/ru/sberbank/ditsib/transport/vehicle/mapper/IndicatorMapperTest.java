package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import ru.sberbank.ditsib.transport.vehicle.database.model.OdometerValue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IndicatorMapperTest {
    
    private final IndicatorMapper mapper = new IndicatorMapperImpl();
    
    @Test
    void odometerValueToGetIndicatorValueDto() {
        var odometerValue = Instancio.create(OdometerValue.class);
        var result = mapper.odometerValueToGetIndicatorValueDto(odometerValue);
        assertNotNull(result);
        assertEquals(odometerValue.getValue(), result.value());
    }
}
