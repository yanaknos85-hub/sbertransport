package ru.sber.transport.telemechanic.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.mapper.CheckMapper;
import ru.sber.transport.telemechanic.mapper.CheckMapperImpl;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CheckHelperTest {
    private final CheckMapper checkMapper = new CheckMapperImpl();
    private final CheckHelper checkHelper = new CheckHelper(checkMapper);
    
    private final Set<Check> checks = Set.of(
            Check.builder().checkType(CheckType.VEHICLE_NUMBER).attempt(1).checkStatus(CheckStatus.DONE).build(),
            Check.builder().checkType(CheckType.ODOMETER).attempt(0).checkStatus(CheckStatus.IN_PROGRESS).build(),
            Check.builder().checkType(CheckType.SPLASH_GUARDS_RR).attempt(0).checkStatus(CheckStatus.IN_PROGRESS).build(),
            Check.builder().checkType(CheckType.SPLASH_GUARDS_LR).attempt(1).checkStatus(CheckStatus.DECLINE).build(),
            Check.builder().checkType(CheckType.WINDSHIELD_WIPERS_AND_LIQUID).attempt(1).checkStatus(CheckStatus.DECLINE).build(),
            Check.builder().checkType(CheckType.WIND_SCREEN).attempt(0).checkStatus(CheckStatus.IN_PROGRESS).build(),
            Check.builder().checkType(CheckType.SAFETY).attempt(1).checkStatus(CheckStatus.DECLINE).build(),
            Check.builder().checkType(CheckType.BRAKE_SYSTEM).attempt(1).checkStatus(CheckStatus.DECLINE).build(),
            Check.builder().checkType(CheckType.SIDE_LIGHTS_HIGH_BEAM_HEADLIGHTS).attempt(1).checkStatus(CheckStatus.DONE).build(),
            Check.builder().checkType(CheckType.STEERING).attempt(1).checkStatus(CheckStatus.DONE).build(),
            Check.builder().checkType(CheckType.WHEELS_AND_TIRES).attempt(1).checkStatus(CheckStatus.DONE).build(),
            Check.builder().checkType(CheckType.HORN).attempt(1).checkStatus(CheckStatus.DONE).build(),
            Check.builder().checkType(CheckType.SATELLITE_NAVIGATION).attempt(1).checkStatus(CheckStatus.DONE).build(),
            Check.builder().checkType(CheckType.BODY_LOCKS_FUEL_TANK_CAPS).attempt(1).checkStatus(CheckStatus.DONE).build(),
            Check.builder().checkType(CheckType.DRIVER_SEAT_CUSHION_AND_BACKREST).attempt(1).checkStatus(CheckStatus.DECLINE).build(),
            Check.builder().checkType(CheckType.WINDOW_HEATING_AND_DEFROSTER).attempt(1).checkStatus(CheckStatus.DECLINE).build(),
            Check.builder().checkType(CheckType.TOW_HITCHES_AND_CABLES).attempt(1).checkStatus(CheckStatus.DECLINE).build(),
            Check.builder().checkType(CheckType.SPARE_WHEEL_HOLDER).attempt(1).checkStatus(CheckStatus.DONE).build(),
            Check.builder().checkType(CheckType.SEAT_BELTS).attempt(1).checkStatus(CheckStatus.DONE).build(),
            Check.builder().checkType(CheckType.EXHAUST_SYSTEM).attempt(1).checkStatus(CheckStatus.DONE).build(),
            Check.builder().checkType(CheckType.FIRST_AID_KIT_FIRE_EXTINGUISHER_ANTI_ROLLBACK).attempt(1).checkStatus(CheckStatus.DONE).build()
                                            );
    
    @MethodSource
    @ParameterizedTest(name = "{0}")
    void isCallTelemech(String testName, Set<Check> checks, boolean expected) {
        boolean actual = CheckHelper.isCallTelemech(checks);
        assertThat(actual).isEqualTo(expected);
    }
    
    @Test
    void shouldCreateCheckTree() {
        var result = checkHelper.createChecksTree(checks);
        var pass = result.getPass();
        var finished = result.getFinished();
        
        assertThat(pass).hasSize(3);
        assertThat(finished).hasSize(4);
        assertThat(finished.get(3).getChildren()).hasSize(14);
    }
    
    static Stream<Arguments> isCallTelemech() {
        return Stream.of(
                Arguments.of("Не все проверки пройдены хотя бы раз",
                             Set.of(Check.builder().checkType(CheckType.VEHICLE_NUMBER).attempt(1).checkStatus(CheckStatus.DONE).build(),
                                    Check.builder().checkType(CheckType.ODOMETER).attempt(1).checkStatus(CheckStatus.IN_PROGRESS).build(),
                                    Check.builder().checkType(CheckType.SPLASH_GUARDS_RR).attempt(0).checkStatus(CheckStatus.IN_PROGRESS).build()),
                             false),
                Arguments.of("Проверка гос номера не в финальном статусе",
                             Set.of(Check.builder().checkType(CheckType.VEHICLE_NUMBER).attempt(1).checkStatus(CheckStatus.IN_PROGRESS).build(),
                                    Check.builder().checkType(CheckType.ODOMETER).attempt(1).checkStatus(CheckStatus.IN_PROGRESS).build(),
                                    Check.builder().checkType(CheckType.SPLASH_GUARDS_RR).attempt(1).checkStatus(CheckStatus.IN_PROGRESS).build(),
                                    Check.builder().checkType(CheckType.SPLASH_GUARDS_LR).attempt(1).checkStatus(CheckStatus.DECLINE).build()),
                             false),
                Arguments.of("Все условия соблюдены",
                             Set.of(Check.builder().checkType(CheckType.VEHICLE_NUMBER).attempt(1).checkStatus(CheckStatus.DONE).build(),
                                    Check.builder().checkType(CheckType.ODOMETER).attempt(1).checkStatus(CheckStatus.IN_PROGRESS).build(),
                                    Check.builder().checkType(CheckType.SPLASH_GUARDS_RR).attempt(1).checkStatus(CheckStatus.IN_PROGRESS).build()),
                             true));
        
        
    }
}
