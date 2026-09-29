package ru.sberbank.ditsib.transport.request.service.personal;

import ru.sberbank.ditsib.transport.request.dto.personal.PersonalTransportSplitCheckRqDTO;

/**
 * Сервис проверки заявок с личным транспортом на дробление
 */
public interface PersonalTransportRequestSplitCheckService {

    /**
     * Проверка поездок на дробление
     *
     * @param rqDTO - дто запроса
     */
    void check(PersonalTransportSplitCheckRqDTO rqDTO);
}
