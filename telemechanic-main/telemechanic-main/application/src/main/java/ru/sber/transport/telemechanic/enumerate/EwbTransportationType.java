package ru.sber.transport.telemechanic.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EwbTransportationType {
    /**
     * КП – коммерческие перевозки
     */
    COMMERCIAL_TRANSPORTATION("КП"),
    
    /**
     * СН – перевозки для собственных нужд
     */
    OWN_ACCOUNT_TRANSPORTATION("СН"),
    
    /**
     * СТ – передвижение и работа специальных транспортных средств
     */
    SPECIAL_PURPOSE_VEHICLES("СТ");
    
    private final String code;
}
