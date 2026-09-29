package ru.sber.transport.etrn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.etrn.client.DispatcherClient;
import ru.sber.transport.etrn.dto.AttorneyCheckResponseDto;
import ru.sber.transport.etrn.exceptions.AttorneyCheckException;
import ru.sber.transport.etrn.service.AttorneyCheckService;

import java.time.LocalDate;

/**
 * Реализация сервиса проверки доверенностей.
 * Интегрируется с Dispatcher-сервисом для получения информации о доверенностях.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class AttorneyCheckServiceImpl implements AttorneyCheckService {

    private final DispatcherClient dispatcherClient;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<AttorneyCheckResponseDto> checkAttorney() {
        try {
            var response = dispatcherClient.getSelfProfile();
            var dispatcherDto = response.getBody();

            if (dispatcherDto == null) {
                log.error("Dispatcher-сервис вернул пустой ответ при проверке доверенности");
                throw new AttorneyCheckException("Не удалось получить данные о доверенности: пустой ответ");
            }

            // Проверка диапазона дат доверенности
            LocalDate today = LocalDate.now();
            if (dispatcherDto.expiryDate() != null
                    && (today.isBefore(dispatcherDto.issueDate()) || today.isAfter(dispatcherDto.expiryDate()))) {
                log.warn("Дверенность недействительна: номер = {}, дата начала = {},дата окончания = {}",
                        dispatcherDto.attorneyNumber(), dispatcherDto.issueDate(), dispatcherDto.expiryDate());
                throw new AttorneyCheckException(
                        String.format("Дверенность недействительна: номер = %s, дата начала = %s, дата окончания = %s",
                                dispatcherDto.attorneyNumber(), dispatcherDto.issueDate(), dispatcherDto.expiryDate()));
            }

            log.info("Проверка доверенности пройдена успешно: номер = {}, issued = {}, expires = {}",
                    dispatcherDto.attorneyNumber(),
                    dispatcherDto.issueDate(),
                    dispatcherDto.expiryDate());

            return ResponseEntity.ok(new AttorneyCheckResponseDto(
                    dispatcherDto.attorneyNumber(),
                    dispatcherDto.issueDate(),
                    dispatcherDto.expiryDate()
            ));

        } catch (AttorneyCheckException e) {
            log.warn("Проверка доверенности не пройдена: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при проверке доверенности", e);
            throw new AttorneyCheckException("Ошибка при проверке доверенности: " + e.getMessage(), e);
        }
    }
}
