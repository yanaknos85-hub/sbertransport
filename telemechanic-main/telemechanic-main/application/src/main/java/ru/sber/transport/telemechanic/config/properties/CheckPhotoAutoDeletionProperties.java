package ru.sber.transport.telemechanic.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "check-photo.auto-deletion")
public record CheckPhotoAutoDeletionProperties(
        boolean enabled,
        String cron,
        String lockAtLeastFor,
        String lockAtMostFor,
        Duration expirationDuration,
        int batchSize,
        int maxQuantity) {
    
}
