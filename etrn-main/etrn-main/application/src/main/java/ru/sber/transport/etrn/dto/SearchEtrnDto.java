package ru.sber.transport.etrn.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SearchEtrnDto(
        @NotNull(message = "pageSetting обязателен")
        PageSetting pageSetting,
        SortSetting sortSetting,
        String humanReadableId,
        String statusFilter,
        UUID lockedByMe
) {
    public record PageSetting(int page, int size) {}
    public record SortSetting(boolean directionAsc, String property) {}
}