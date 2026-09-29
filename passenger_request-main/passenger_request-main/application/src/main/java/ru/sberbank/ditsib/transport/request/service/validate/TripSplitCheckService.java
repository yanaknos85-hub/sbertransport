package ru.sberbank.ditsib.transport.request.service.validate;


import ru.sberbank.ditsib.transport.request.dto.validation.TripSplitCheckDTO;
import ru.sberbank.ditsib.transport.request.dto.validation.TripSplitCheckResultDTO;

/**
 * Сервис првоерки заявки на дробление поездки
 */
public interface TripSplitCheckService {

    TripSplitCheckResultDTO check(TripSplitCheckDTO tripSplitCheckDTO);
}
