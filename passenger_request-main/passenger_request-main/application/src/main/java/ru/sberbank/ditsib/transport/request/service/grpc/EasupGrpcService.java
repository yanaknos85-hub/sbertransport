package ru.sberbank.ditsib.transport.request.service.grpc;

import ru.sberbank.ditsib.transport.request.dto.fraud.EasupAbsenceRequest;
import ru.sberbank.ditsib.transport.request.dto.fraud.EasupAbsenceResponse;

import java.util.Optional;

/**
 * Сервис для работы с ЕАСУП по grpc
 */
public interface EasupGrpcService {

    /**
     * Получить информацию об отсутствии сотрудника в ЕАСУП
     * @param request {@link EasupAbsenceRequest}
     * @return {@link EasupAbsenceResponse}
     */
    Optional<EasupAbsenceResponse> resolveAbsence(EasupAbsenceRequest request);
}
