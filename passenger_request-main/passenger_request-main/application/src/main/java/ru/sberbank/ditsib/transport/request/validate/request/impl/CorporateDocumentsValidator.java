package ru.sberbank.ditsib.transport.request.validate.request.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.service.grpc.CorporateDocumentValidationGrpcClient;
import ru.sberbank.ditsib.transport.request.validate.request.NewRequestValidator;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Валидатор корпоративных документов сотрудника при создании заявки на {@link TransportTypeEnum#PERSONAL персонального водителя}.
 * <p>
 * Делегирует проверку gRPC-клиенту {@link CorporateDocumentValidationGrpcClient}, который обращается к сервису
 * {@code corporate-documents} — валидирует наличие и статус необходимых корпоративных документов
 * (доверенность, разрешение на управление ТС и т.п.) для указанного автомобиля на указанную дату.
 * </p>
 *
 * @see NewRequestValidator
 * @see CorporateDocumentValidationGrpcClient
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CorporateDocumentsValidator implements NewRequestValidator {
    @Setter
    @Value("${request.validation.corporate-documents.carsharing.enabled}")
    private boolean corporateDocumentValidationForCarsharingEnabled;

    @Setter
    @Value("${request.validation.corporate-documents.personalTransport.enabled}")
    private boolean corporateDocumentValidationForPersonalEnabled;

    private final CorporateDocumentValidationGrpcClient corporateDocumentValidationGrpcClient;

    /**
     * Выполняет валидацию корпоративных документов сотрудника на дату создания заявки.
     * В зависимости от типа транспорта делегирует проверку для личного автомобиля или каршеринга.
     *
     * @param request  заявка на перевозку
     * @param employee сотрудник, создавший заявку
     * @throws RuntimeException если проверка корпоративных документов не пройдена
     */
    @Override
    public void validate(@NonNull NewRequestDTO request, @NonNull Employee employee) {
        var employeeId = request.getPassenger().id();
        var carId = request.getPersonalCarId();
        var desiredDate = request.getDesiredDate();

        switch (request.getTransportType()) {
            case PERSONAL -> {
                if (corporateDocumentValidationForPersonalEnabled) {
                    validateDocumentsForPersonalTrip(employeeId, carId, desiredDate);
                }
            }
            case CARSHARING -> {
                if (corporateDocumentValidationForCarsharingEnabled) {
                    validateDocumentsForCarSharingTrip(employeeId, desiredDate);
                }
            }
        }
    }

    /**
     * Валидирует корпоративные документы (доверенность, разрешение на управление ТС и т.п.)
     * для личного автомобиля сотрудника на указанную дату.
     *
     * @param employeeId  идентификатор сотрудника
     * @param carId       идентификатор автомобиля
     * @param desiredDate желаемая дата поездки
     */
    private void validateDocumentsForPersonalTrip(UUID employeeId, UUID carId, LocalDateTime desiredDate) {
        corporateDocumentValidationGrpcClient.validateDocuments(employeeId, carId, desiredDate, null);
    }

    /**
     * Валидирует корпоративные документы для поездки на каршеринге.
     *
     * @param employeeId  идентификатор сотрудника
     * @param desiredDate желаемая дата поездки
     */
    private void validateDocumentsForCarSharingTrip(UUID employeeId, LocalDateTime desiredDate) {
        corporateDocumentValidationGrpcClient.validateDocumentsForCarSharingTrip(employeeId, desiredDate);
    }

    /**
     * Возвращает набор типов транспорта, для которых включена валидация корпоративных документов.
     *
     * @return set типов транспорта: PERSONAL и CARSHARING
     */
    @Override
    public Set<TransportTypeEnum> validationEnabledFor() {
        return Set.of(TransportTypeEnum.PERSONAL, TransportTypeEnum.CARSHARING);
    }
}