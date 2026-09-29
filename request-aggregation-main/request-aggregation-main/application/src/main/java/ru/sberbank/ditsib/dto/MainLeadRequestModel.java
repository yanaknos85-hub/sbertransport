package ru.sberbank.ditsib.dto;

import java.util.List;

/**
 * Доменная модель запроса на создание основной заявки
 * Содержит заявки, тарифы и матрицу расстояний для обработки
 */
public record MainLeadRequestModel(
        List<CreateMainLeadRequestModel> leads,
        List<TariffRequestMainDto> tariffs,
        List<AddressMatrixRequestDto> addressMatrix
) {
} 