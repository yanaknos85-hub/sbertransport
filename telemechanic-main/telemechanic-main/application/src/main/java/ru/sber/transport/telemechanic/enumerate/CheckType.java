package ru.sber.transport.telemechanic.enumerate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static ru.sber.transport.telemechanic.enumerate.CheckTypeMonitoring.*;

@RequiredArgsConstructor
@Getter
@Schema(title = "Тип проверки", description = "Описание типов проверок")
public enum CheckType {
    
    VEHICLE_NUMBER("Распознавание государственного номера", null, null, false, 1, 1, 1, null),
    OIL_LEVEL("Уровень масла", null, null, false, 1, 2, 2,  null),
    POWER_STEERING_LIQUID_LEVEL("Уровень жидкости гур", null, null, false, 1, 3, 3, null),
    COOLANT_LEVEL("Уровень охлаждающей жидкости", null, null, false, 1, 4, 4, null),
    INSTRUMENT_PANEL("Отсутствие критичных знаков на приборной панели", null, null, false, 1, 5, 11, null),
    ODOMETER("Внесение показаний одометра", null, null, false, 1, 6, 12, true),
    LITREAGE("Внесение остатка топлива", null, null, false, 1, 7, 13, true),
    WIND_SCREEN("Целостность лобового стекла", null, null, false, 1, 8, 14, null),
    WINDSHIELD_WIPERS_AND_LIQUID("Работоспособность дворников и подачи омывающей жидкости", null, null, false, 1, 9, 15, null),
    SPLASH_GUARDS_LF("Брызговик передний левый", null, SPLASH_GUARDS, false, 1, 10, 5, null),
    SIDE_MIRRORS_L("Зеркало левое", null, SIDE_MIRRORS, false, 1, 11, 10, null),
    SPLASH_GUARDS_LR("Брызговик задний левый", null, SPLASH_GUARDS, false, 1, 12, 6, null),
    HEADLAMPS_LR("Левая задняя фара", null, HEADLAMPS, false, 1, 13, 17, null),
    HEADLAMPS_RR("Правая задняя фара", null, HEADLAMPS,false, 1, 14, 18, null),
    SPLASH_GUARDS_RR("Брызговик задний правый", null, SPLASH_GUARDS, false, 1, 15, 7, null),
    SIDE_MIRRORS_R("Зеркало правое", null, SIDE_MIRRORS, false, 1, 16, 9, null),
    SPLASH_GUARDS_RF("Брызговик передний правый", null, SPLASH_GUARDS,  false, 1, 17, 8, null),
    HEADLAMPS_RF("Правая передняя фар", null, HEADLAMPS, false, 1, 18, 19, null),
    HEADLAMPS_LF("Левая передняя фара", null, HEADLAMPS, false, 1, 19, 16, null),
    BODY_DAMAGE("Повреждение кузова", null, null, false, 1, 20, 20, null),
    SAFETY("Безопасность", null, null, true, 1, 21, 21, null),
    
    //SAFETY SUB CHECKS
    BRAKE_SYSTEM("Исправность тормозной система", SAFETY, CheckTypeMonitoring.SAFETY, false, 1, 1, 1, null),
    SIDE_LIGHTS_HIGH_BEAM_HEADLIGHTS("Исправность габаритов, дальнего света фар", SAFETY, CheckTypeMonitoring.SAFETY, false, 1, 2, 2, null),
    STEERING("Исправность рулевого управления", SAFETY, CheckTypeMonitoring.SAFETY, false, 1, 3, 3, null),
    WHEELS_AND_TIRES("Исправность колес и шин", SAFETY, CheckTypeMonitoring.SAFETY, false, 1, 4, 4, null),
    HORN("Исправность звукового сигнала", SAFETY, CheckTypeMonitoring.SAFETY, false, 1, 5, 5, null),
    SATELLITE_NAVIGATION("Исправность спутниковой навигации", SAFETY, CheckTypeMonitoring.SAFETY, false, 1, 6, 6, null),
    BODY_LOCKS_FUEL_TANK_CAPS("Исправность замков кузова, пробок топливного бака", SAFETY, CheckTypeMonitoring.SAFETY, false, 1, 7, 7, null),
    DRIVER_SEAT_CUSHION_AND_BACKREST("Исправность подушки и спинки водительского сиденья", SAFETY, CheckTypeMonitoring.SAFETY, false, 1, 8, 8, null),
    WINDOW_HEATING_AND_DEFROSTER("Исправность обогрева и обдува стекол", SAFETY, CheckTypeMonitoring.SAFETY, false, 1, 9, 9, null),
    TOW_HITCHES_AND_CABLES("Исправность тягово-сцепных устройств и тросов", SAFETY, CheckTypeMonitoring.SAFETY, false, 1, 10, 10, null),
    SPARE_WHEEL_HOLDER("Исправность держателя запасного колеса", SAFETY, CheckTypeMonitoring.SAFETY, false, 1, 11, 11, null),
    SEAT_BELTS("Исправность ремней безопасности", SAFETY, CheckTypeMonitoring.SAFETY, false, 1, 12, 12, null),
    EXHAUST_SYSTEM("Исправность системы отработавших газов", SAFETY, CheckTypeMonitoring.SAFETY, false, 1, 13, 13, null),
    FIRST_AID_KIT_FIRE_EXTINGUISHER_ANTI_ROLLBACK("Укомплектованность аптечки, огнетушителя, противооткатов (при наличии)", SAFETY, CheckTypeMonitoring.SAFETY, false, 1, 14, 14, null);
    
    private final String description;
    private final CheckType parent;
    private final CheckTypeMonitoring parentMonitoring;
    private final boolean main;
    private final int maxAttempt;
    private final int ordinal;
    private final int monitoringOrdinal;
    private final Boolean inEwbPath;
    
    public static Set<CheckType> getSafetySubChecks() {
        return stream(values())
                .filter(type -> type.getParent() == SAFETY)
                .collect(Collectors.toSet());
    }
    
    public static Set<CheckType> getParentCheckWithSubChecks(CheckType checkType) {
        return stream(values())
                .filter(type -> type.equals(checkType) || (type.getParent() != null && type.getParent().equals(checkType)))
                .collect(Collectors.toSet());
    }
}
