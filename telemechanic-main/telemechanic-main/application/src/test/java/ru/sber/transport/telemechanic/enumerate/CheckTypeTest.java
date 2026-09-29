package ru.sber.transport.telemechanic.enumerate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.sber.transport.telemechanic.enumerate.CheckType.*;

class CheckTypeTest {
    
    @Test
    @DisplayName("Возвращение списка дочерних проверок для безопасности")
    void shouldReturnSafetySubChecks() {
        var subChecks = CheckType.getSafetySubChecks();
        var expectedList = List.of(
                BRAKE_SYSTEM,
                SIDE_LIGHTS_HIGH_BEAM_HEADLIGHTS,
                STEERING,
                WHEELS_AND_TIRES,
                HORN,
                SATELLITE_NAVIGATION,
                BODY_LOCKS_FUEL_TANK_CAPS,
                DRIVER_SEAT_CUSHION_AND_BACKREST,
                WINDOW_HEATING_AND_DEFROSTER,
                TOW_HITCHES_AND_CABLES,
                SPARE_WHEEL_HOLDER,
                SEAT_BELTS,
                EXHAUST_SYSTEM,
                FIRST_AID_KIT_FIRE_EXTINGUISHER_ANTI_ROLLBACK
                                  );
        assertTrue(expectedList.containsAll(subChecks));
    }
}
