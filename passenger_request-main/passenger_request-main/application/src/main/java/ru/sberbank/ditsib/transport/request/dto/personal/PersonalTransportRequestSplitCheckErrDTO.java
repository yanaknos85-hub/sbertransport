package ru.sberbank.ditsib.transport.request.dto.personal;

import java.util.UUID;

/**
 * ДТО ошибки при проверки заявки на дробление
 *
 * @param id идентификатор заявки
 * @param humanReadableId идентификатор заявки в человекочитаемом виде
 */
public record PersonalTransportRequestSplitCheckErrDTO(
        UUID id,
        String humanReadableId,
        String status) {
}
