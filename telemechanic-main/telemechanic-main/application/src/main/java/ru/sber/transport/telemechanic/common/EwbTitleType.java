package ru.sber.transport.telemechanic.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EwbTitleType {
    FIRST("Первый титул", "ON_PTLSSOBTS"),
    SECOND("Второй титул", "ON_PTLSPRMO"),
    THIRD("Третий титул", "ON_PTLSVIPTS"),
    FOURTH("Четвертый титул", "ON_PTLSODVZD"),
    FIFTH("Пятый титул", "ON_PTLSODPARK");
    
    private final String name;
    private final String prefix;
    
    public static EwbTitleType getNextTitleType(EwbTitleType titleType) {
        var lastTitleType = values().length - 1;
        return titleType.ordinal() < lastTitleType
               ? values()[titleType.ordinal() + 1]
               : null;
    }
}
