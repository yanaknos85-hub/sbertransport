package ru.sber.transport.telemechanic.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EwbStatus {
    
    ON_THE_LINE("На линии", true, true),
    IN_GARAGE("Заезд в парк", true, true),
    EWB_CREATED("ЭПЛ создан", false, false),
    MEDIC_IN_PROGRESS("Прохождение медика", false, false),
    MEDIC_DECLINED("Медик не пройден", false, false),
    TELEMECH_IN_PROGRESS("Прохождение телемеханика", true, false),
    DRIVER_CANCELED("Отменена водителем", true, false),
    TELEMECH_DECLINED("Телемеханик не пройден", true, false),
    KORUS_DECLINED("Отказ в формировании QR", true, true),
    EWB_CLOSED("ЭПЛ закрыт", true, true),
    EXPIRED("ЭПЛ просрочен", false, false),
    EWB_CANCELLED("ЭПЛ отменена", false, false);
    
    private final String rusName;
    private final boolean medicSuccess;
    private final boolean telemechSuccess;
}
