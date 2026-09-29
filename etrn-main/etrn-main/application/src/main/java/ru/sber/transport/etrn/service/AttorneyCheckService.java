package ru.sber.transport.etrn.service;

import org.springframework.http.ResponseEntity;
import ru.sber.transport.etrn.dto.AttorneyCheckResponseDto;

/**
 * Сервис проверки доверенностей сотрудников.
 */
public interface AttorneyCheckService {

    /**
     * Проверяет доверенности текущего сотрудника через Dispatcher-сервис.
     *
     * @return ResponseEntity с информацией о доверенности
     */
    ResponseEntity<AttorneyCheckResponseDto> checkAttorney();
}
