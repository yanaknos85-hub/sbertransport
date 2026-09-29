package ru.sber.transport.telemechanic.enumerate;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Schema(title = "Тип проверки", description = "Описание типов проверок")
public enum CheckTypeMonitoring {
    
    VEHICLE_NUMBER("Распознавание государственного номера", null, false),
    OIL_LEVEL("Уровень масла", null, false),
    POWER_STEERING_LIQUID_LEVEL("Уровень жидкости гур", null, false),
    COOLANT_LEVEL("Уровень охлаждающей жидкости", null, false),
    
    SPLASH_GUARDS("Брызговики", null, true),
    SPLASH_GUARDS_LF("Брызговик передний левый", SPLASH_GUARDS, false),
    SPLASH_GUARDS_LR("Брызговик задний левый", SPLASH_GUARDS, false),
    SPLASH_GUARDS_RR("Брызговик задний правый", SPLASH_GUARDS, false),
    SPLASH_GUARDS_RF("Брызговик передний правый", SPLASH_GUARDS, false),
    
    SIDE_MIRRORS("Зеркало", null, true),
    SIDE_MIRRORS_R("Зеркало правое", SIDE_MIRRORS, false),
    SIDE_MIRRORS_L("Зеркало левое", SIDE_MIRRORS, false),
    
    INSTRUMENT_PANEL("Отсутствие критичных знаков на приборной панели", null, false),
    ODOMETER("Внесение показаний одометра", null, false),
    LITREAGE("Внесение остатка топлива", null, false),
    WIND_SCREEN("Целостность лобового стекла", null, false),
    WINDSHIELD_WIPERS_AND_LIQUID("Работоспособность дворников и подачи омывающей жидкости", null, false),
    
    HEADLAMPS("Фары", null, true),
    HEADLAMPS_LF("Левая передняя фара", HEADLAMPS, false),
    HEADLAMPS_LR("Левая задняя фара", HEADLAMPS, false),
    HEADLAMPS_RR("Правая задняя фара", HEADLAMPS, false),
    HEADLAMPS_RF("Правая передняя фар", HEADLAMPS, false),
    
    BODY_DAMAGE("Повреждение кузова", null, false),
    
    SAFETY("Безопасность", null, true),
    BRAKE_SYSTEM("Исправность тормозной система", SAFETY, false),
    SIDE_LIGHTS_HIGH_BEAM_HEADLIGHTS("Исправность габаритов, дальнего света фар", SAFETY, false),
    STEERING("Исправность рулевого управления", SAFETY, false),
    WHEELS_AND_TIRES("Исправность колес и шин", SAFETY, false),
    HORN("Исправность звукового сигнала", SAFETY, false),
    SATELLITE_NAVIGATION("Исправность спутниковой навигации", SAFETY, false),
    BODY_LOCKS_FUEL_TANK_CAPS("Исправность замков кузова, пробок топливного бака", SAFETY, false),
    DRIVER_SEAT_CUSHION_AND_BACKREST("Исправность подушки и спинки водительского сиденья", SAFETY, false),
    WINDOW_HEATING_AND_DEFROSTER("Исправность обогрева и обдува стекол", SAFETY, false),
    TOW_HITCHES_AND_CABLES("Исправность тягово-сцепных устройств и тросов", SAFETY, false),
    SPARE_WHEEL_HOLDER("Исправность держателя запасного колеса", SAFETY, false),
    SEAT_BELTS("Исправность ремней безопасности", SAFETY, false),
    EXHAUST_SYSTEM("Исправность системы отработавших газов", SAFETY, false),
    FIRST_AID_KIT_FIRE_EXTINGUISHER_ANTI_ROLLBACK("Укомплектованность аптечки, огнетушителя, противооткатов (при наличии)", SAFETY, false);
    
    private final String description;
    private final CheckTypeMonitoring parent;
    private final boolean main;
}
