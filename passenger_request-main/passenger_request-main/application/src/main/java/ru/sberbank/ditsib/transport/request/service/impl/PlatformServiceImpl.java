package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.request.dto.ExecutorGroupDTO;
import ru.sberbank.ditsib.transport.request.messaging.resolvers.PlatformResolver;
import ru.sberbank.ditsib.transport.request.service.PlatformService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Service
public class PlatformServiceImpl implements PlatformService {

    private final PlatformResolver platformResolver;

    @Override
    public ExecutorGroupDTO getExecutorGroup(UUID employeeId, List<UUID> regionIds, String token) {
        try {
            return CompletableFuture.supplyAsync(() -> platformResolver.getExecutorGroupByEmployeeId(employeeId, regionIds, token)).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Thread was interrupted. {}", e.getMessage());
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.error("Заявка не получила привязку к группе исполнителей. Ошибка: {}", e.getLocalizedMessage());
            return null;
        }
    }
}
