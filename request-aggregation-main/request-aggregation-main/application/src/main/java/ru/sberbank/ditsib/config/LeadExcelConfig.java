package ru.sberbank.ditsib.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "lead.config")
public record LeadExcelConfig(
        @NotNull
        DataSize maxFileSize,
        @Positive
        int maxRowCount,
        @Positive
        int departureDelay
) {
}
