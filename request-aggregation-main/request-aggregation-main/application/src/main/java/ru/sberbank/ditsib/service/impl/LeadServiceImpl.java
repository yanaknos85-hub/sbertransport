package ru.sberbank.ditsib.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.spreadsheet.base.reader.exception.HeaderValidationException;
import ru.sberbank.ditsib.config.LeadExcelConfig;
import ru.sberbank.ditsib.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.database.dao.LeadRepository;
import ru.sberbank.ditsib.database.model.PointLead;
import ru.sberbank.ditsib.dto.file.ValidateFileResponseDto;
import ru.sberbank.ditsib.dto.lead.LeadExcelDto;
import ru.sberbank.ditsib.dto.lead.LeadFromExcelDto;
import ru.sberbank.ditsib.dto.lead.LeadRequestDto;
import ru.sberbank.ditsib.dto.lead.LeadResponseDto;
import ru.sberbank.ditsib.exception.FileUploadException;
import ru.sberbank.ditsib.exception.LeadDepartureTimeException;
import ru.sberbank.ditsib.exception.excel.ExcelHeaderValidationException;
import ru.sberbank.ditsib.exception.excel.MaxFileSizeException;
import ru.sberbank.ditsib.helper.BaseExcelImporter;
import ru.sberbank.ditsib.helper.excel.ExcelImportProperties;
import ru.sberbank.ditsib.mappers.LeadMapper;
import ru.sberbank.ditsib.mappers.PointLeadMapper;
import ru.sberbank.ditsib.service.LeadService;
import ru.sberbank.ditsib.service.validation.LeadExcelValidator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA адаптер для работы с данными пользователей
 */
@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final EmployeeRepository employeeRepository;
    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;
    private final PointLeadMapper pointLeadMapper;
    private final LeadExcelConfig leadExcelConfig;
    private final LeadExcelValidator leadExcelDtoValidator;

    @Override
    @Transactional
    public LeadResponseDto createLead(LeadRequestDto request, UUID employeeId) {
        var withLag = LocalDateTime.now().plusHours(leadExcelConfig.departureDelay());
        if (withLag.isAfter(request.departureTime())) {
            throw new LeadDepartureTimeException(leadExcelConfig.departureDelay());
        }
        var employee = employeeRepository.findById(employeeId).orElseThrow(() -> new
                EntityNotFoundException("Сотрудник не найден, табельный номер: " + employeeId));
        var lead = leadMapper.toEntity(request, employee);

        List<PointLead> points = new ArrayList<>();
        for (var pointDto : request.points()) {
            var point = pointLeadMapper.toEntity(pointDto);
            point.setLead(lead);
            point.setPointNumber(points.size() + 1);

            points.add(point);
        }
        lead.setPoints(points);

        var savedLead = leadRepository.save(lead);

        return leadMapper.toDto(savedLead);
    }

    @Override
    public ValidateFileResponseDto validateLeads(MultipartFile file) {
        if (file.getSize() > leadExcelConfig.maxFileSize().toBytes()) {
            throw new MaxFileSizeException(leadExcelConfig.maxFileSize());
        }
        var fileType = BaseExcelImporter.resolveFileType(file.getOriginalFilename());
        try {
            var dtos = BaseExcelImporter.parse(file.getInputStream(), LeadExcelDto.class,
                    new ExcelImportProperties(fileType, LeadExcelDto.getExcelFieldInfoList(),
                            leadExcelConfig.maxRowCount()));
            return leadExcelDtoValidator.validate(dtos, leadExcelConfig.departureDelay());
        } catch (HeaderValidationException e) {
            throw new ExcelHeaderValidationException();
        } catch (IOException e) {
            throw new FileUploadException();
        }
    }

    @Override
    public void massiveUpload(List<LeadFromExcelDto> dtos) {
        //загрузка заявок до 800 штук за раз и отправка в модель для формирования основного лида
    }
}
