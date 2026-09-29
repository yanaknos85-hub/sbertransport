package ru.sberbank.ditsib.transport.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Состояния согласования.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ApprovalState {

    /**
     * Ожидает согласования.
     */
    AWAITING_APPROVAL("Ожидает согласования"),

    /**
     * Согласована.
     */
    APPROVED("Согласована"),

    /**
     * Отклонена.
     */
    DECLINED("Отклонена");
    
    private final String description;
}
