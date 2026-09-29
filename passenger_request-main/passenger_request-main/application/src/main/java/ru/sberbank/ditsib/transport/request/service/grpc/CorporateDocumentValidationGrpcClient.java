package ru.sberbank.ditsib.transport.request.service.grpc;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * gRPC-клиент для валидации корпоративных документов сотрудника.
 * <p>
 * Обращается к сервису {@code corporate-documents} и проверяет наличие и статус документов
 * (доверенность, разрешение на управление ТС и т.п.), необходимых для использования
 * указанного автомобиля {@code carId} сотрудником {@code employeeId} на дату {@code desiredDate}.
 * </p>
 * <p>
 * Результатом является void — при успехе возвращается без ошибок, при неудаче кидает
 * одно из исключений:
 * <ul>
 *     <li>{@link ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationFailedException} — если сервер ответил, но документы невалидны</li>
 *     <li>{@link ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationTimeoutException} — если сервер не отвечает (DEADLINE_EXCEEDED / UNAVAILABLE)</li>
 * </ul>
 * </p>
 * @see ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationFailedException
 * @see ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationTimeoutException
 */
public interface CorporateDocumentValidationGrpcClient {

    /**
     * Валидирует корпоративные документы сотрудника для личного автомобиля.
     * <p>
     * Проверяет наличие и статус документов (доверенность, разрешение на управление ТС и т.п.)
     * для указанного автомобиля на указанную дату. Если {@code colleagueEmployeeId} не null,
     * проверяет документы для коллеги (пассажир перевезён на автомобиле сотрудника).
     * </p>
     *
     * @param employeeId идентификатор сотрудника (владельца автомобиля)
     * @param carId идентификатор автомобиля
     * @param desiredDate желаемая дата поездки
     * @param colleagueEmployeeId идентификатор коллеги (может быть null)* @throws ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationFailedException если документы невалидны
     */
    void validateDocuments(UUID employeeId, UUID carId, LocalDateTime desiredDate, UUID colleagueEmployeeId);

    /**
     * Валидирует корпоративные документы для поездки на каршеринге.
     *
     * @param employeeId идентификатор сотрудника
     * @param desiredDate желаемая дата поездки
     * @throws ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationFailedException если документы невалидны
     * @throws ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationTimeoutException если сервис недоступен или истек таймаут
     */
    void validateDocumentsForCarSharingTrip(UUID employeeId, LocalDateTime desiredDate);
}