package ru.sber.transport.telemechanic.dto.ewb;

import java.util.UUID;

/**
 * Ответ системы Корус на отправку титула
 * */
public record KorusEwbTitleResponse(
        /**
         * Идентификатор документа
         * */
        UUID id,

        /**
         * Идентификатор цепочки документов
         * */
        UUID chainId
) {
}
