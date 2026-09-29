package ru.sberbank.ditsib.dto;

import ru.sberbank.ditsib.dto.point.PointDto;

import java.util.List;

/**
 * Ответ по созданию основного лида
 */
public record CreateMainLeadResponseDto(
        List<PointDto> points
) {
}