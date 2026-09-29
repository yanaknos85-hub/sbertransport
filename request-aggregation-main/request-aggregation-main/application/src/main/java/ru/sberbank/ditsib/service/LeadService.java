package ru.sberbank.ditsib.service;

import org.springframework.web.multipart.MultipartFile;
import ru.sberbank.ditsib.dto.file.ValidateFileResponseDto;
import ru.sberbank.ditsib.dto.lead.LeadFromExcelDto;
import ru.sberbank.ditsib.dto.lead.LeadRequestDto;
import ru.sberbank.ditsib.dto.lead.LeadResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * Порт для работы с данными пользователей
 */
public interface LeadService {

    /**
     * Создает и отправляет заявку для указанного пользователя.
     *
     * @param request данные заявки
     */
    LeadResponseDto createLead(LeadRequestDto request, UUID userId);

    /**
     * Проверяет файл с пользовательскими заявками на валидность.
     * @param file загруженный файл
     * @return результат проверки файла
     */
    ValidateFileResponseDto validateLeads(MultipartFile file);

    /**
     * Массовая загрузка пользовательских заявок
     * @param dtos заявки для загрузки в бд и модель
     */
    void massiveUpload(List<LeadFromExcelDto> dtos);
}
