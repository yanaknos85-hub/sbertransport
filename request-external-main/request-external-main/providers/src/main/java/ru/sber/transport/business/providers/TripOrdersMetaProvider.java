package ru.sber.transport.business.providers;

import java.util.UUID;
import ru.sber.transport.request.external.model.Modifiable;

/**
 * Провайдер данных о заявках
 */
public interface TripOrdersMetaProvider {

    /**
     * Получает метаданные заявки на поездку
     *
     * @param organizationId идентификатор организации
     * @param requestId идентификатор заявки на поездку
     * @return метаданные заявки на поездку
     */
    Modifiable meta(UUID organizationId, UUID requestId);

}
