package ru.sberbank.ditsib.transport.reports.utils;

import lombok.Builder;
import lombok.Data;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class ReportsXlsxUtils {

    /**
     * Класс для получения аргументов имени xlsx файла, даты используются для определения диапазона запросов к БД
     */
    @Data
    @Builder
    public static class ArgsContainer {
        private LocalDateTime creationDateFrom;
        private LocalDateTime creationDateTo;
        private String filename;
    }

    public static ArgsContainer getCreateDateArgs(RequestReportDTO requestDTO) {
        LocalDateTime creationDateFrom = null;
        LocalDateTime creationDateTo = null;

        if (requestDTO.getCreationDate() != null) {
            creationDateFrom = requestDTO.getCreationDate().getStart();
            creationDateTo = requestDTO.getCreationDate().getEnd();
        }

        if (creationDateTo == null) {
            creationDateTo = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        }
        if (creationDateFrom == null) {
            creationDateFrom = creationDateTo.minusMonths(1);
        }

        return ArgsContainer.builder()
                .creationDateFrom(creationDateFrom)
                .creationDateTo(creationDateTo)
                .build();
    }

}
