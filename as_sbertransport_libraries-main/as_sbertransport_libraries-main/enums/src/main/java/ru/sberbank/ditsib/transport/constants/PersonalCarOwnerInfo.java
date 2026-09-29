package ru.sberbank.ditsib.transport.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Информация о владельце ТС
 */
@Getter
@RequiredArgsConstructor
public enum  PersonalCarOwnerInfo {

    /**
     * В собственности пользователя.
     */
    USER("в собственности пользователя"),

    /**
     * В собственности супруга (-и).
     */
    SPOUSE("в собственности супруга/супруги пользователя"),

    /**
     * В собственности третьих лиц.
     */
    THIRD_PARTY("в собственности третьих лиц");
    
    private final String rusName;
}
