package ru.sberbank.ditsib.dto;

import java.util.List;

/**
 * Запрос, содержащий заявки, тарифы и матрицу расстояний
 */
public record CreateMainLeadRequestDto(
        int timeToWork,
        List<LeadRequestMainDto> leads,
        List<TariffRequestMainDto> tariffs,
        List<AddressMatrixRequestDto> addressMatrix
) {
}