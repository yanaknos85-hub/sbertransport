package ru.sberbank.ditsib.transport.srm.service;

import ru.sber.transport.magenta.model.MagentaOrgUpdateInfoRequestDTO;

import java.util.concurrent.CompletableFuture;

/**
 * Сервис для работы с маджентой
 */
public interface MagentaUpdateInfoService {
    
    /**
     * Обновление по состоянию совместной поездки: апи совместимости с маджентой
     *
     * @param updateRequest совместная поездка
     * @return асинхронный таск
     */
    CompletableFuture<Void> updateInfo(MagentaOrgUpdateInfoRequestDTO updateRequest);
}
