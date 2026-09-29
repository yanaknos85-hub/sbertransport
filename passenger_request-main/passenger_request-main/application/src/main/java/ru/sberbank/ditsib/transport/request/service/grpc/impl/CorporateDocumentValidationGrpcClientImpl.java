package ru.sberbank.ditsib.transport.request.service.grpc.impl;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.sber.transport.corporate.grpc.service.DocumentValidationServiceGrpc;
import ru.sber.transport.corporate.grpc.service.ValidateDocumentsForCarSharingRequest;
import ru.sber.transport.corporate.grpc.service.ValidateDocumentsRequest;
import ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationFailedException;
import ru.sberbank.ditsib.transport.request.exceptions.DocumentsValidationTimeoutException;
import ru.sberbank.ditsib.transport.request.service.grpc.CorporateDocumentValidationGrpcClient;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CorporateDocumentValidationGrpcClientImpl implements CorporateDocumentValidationGrpcClient {
    private static final long DEADLINE_SECONDS = 3;

    /**
     * gRPC stub для вызова сервиса валидации документов.
     */
    @GrpcClient("corporate-documents")
    private DocumentValidationServiceGrpc.DocumentValidationServiceBlockingStub stub;

    @Override
    public void validateDocuments(UUID employeeId, UUID carId, LocalDateTime desiredDate, UUID colleagueEmployeeId) {
        var requestBuilder = ValidateDocumentsRequest.newBuilder()
                .setEmployeeId(employeeId.toString())
                .setCarId(carId.toString())
                .setDesiredDate(Timestamp.newBuilder()
                        .setSeconds(desiredDate.toEpochSecond(ZoneOffset.UTC))
                        .setNanos(desiredDate.getNano())
                        .build());

        if (colleagueEmployeeId != null) {
            requestBuilder.setColleagueEmployeeId(colleagueEmployeeId.toString());
        }

        var request = requestBuilder.build();

        try {
            var response = stub
                    .withDeadlineAfter(DEADLINE_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                    .validateDocuments(request);

            if (!response.getValid()) {
                log.warn("Документы не валидны: employeeId={}, errors={}",
                        employeeId, response.getErrorsList());
                throw new DocumentsValidationFailedException(response.getErrorsList());
            }
        } catch (StatusRuntimeException e) {
            throw handleGrpcError(e);
        }
    }

    @Override
    public void validateDocumentsForCarSharingTrip(UUID employeeId, LocalDateTime desiredDate) {
        var request = ValidateDocumentsForCarSharingRequest.newBuilder()
                .setEmployeeId(employeeId.toString())
                .setDesiredDate(Timestamp.newBuilder()
                        .setSeconds(desiredDate.toEpochSecond(ZoneOffset.UTC))
                        .setNanos(desiredDate.getNano())
                        .build())
                .build();

        try {
            var response = stub
                    .withDeadlineAfter(DEADLINE_SECONDS, java.util.concurrent.TimeUnit.SECONDS)
                    .validateDocumentsForCarSharing(request);

            if (!response.getValid()) {
                log.warn("Документы не валидны: employeeId={}, error={}",
                        employeeId, response.getError());
                throw new DocumentsValidationFailedException(response.getError());
            }
        } catch (StatusRuntimeException e) {
            throw handleGrpcError(e);
        }
    }

    /**
     * Обрабатывает gRPC-ошибку и выбрасывает соответствующее исключение.
     * <p>
     * Если код ошибки {@code DEADLINE_EXCEEDED} или {@code UNAVAILABLE} —
     * {@link DocumentsValidationTimeoutException} с сообщением "Сервис CorporateService не отвечает",
     * для любых других кодов — {@link DocumentsValidationTimeoutException} с общим сообщением.
     * </p>
     *
     * @param e исключение от gRPC
     * @return DocumentsValidationTimeoutException с соответствующим сообщением
     */
    private DocumentsValidationTimeoutException handleGrpcError(StatusRuntimeException e) {
        var statusCode = e.getStatus().getCode();
        log.error("gRPC ошибка при вызове CorporateService: code={}, message={}",
                statusCode, e.getMessage());
        if (statusCode == io.grpc.Status.Code.DEADLINE_EXCEEDED
                || statusCode == io.grpc.Status.Code.UNAVAILABLE) {
            throw new DocumentsValidationTimeoutException("Сервис CorporateService не отвечает");
        }
        throw new DocumentsValidationTimeoutException("Сервис валидации документов временно недоступен");
    }
}