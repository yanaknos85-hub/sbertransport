package ru.sberbank.ditsib.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.sberbank.ditsib.controller.LeadController;
import ru.sberbank.ditsib.dto.file.ValidateFileResponseDto;
import ru.sberbank.ditsib.dto.lead.LeadFromExcelDto;
import ru.sberbank.ditsib.dto.lead.LeadRequestDto;
import ru.sberbank.ditsib.service.LeadService;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
public class LeadControllerImpl implements LeadController {

    private final LeadService leadService;

    @Override
    public void createLead(UUID userId, LeadRequestDto request) {
        leadService.createLead(request, userId);
    }

    @Override
    public void massiveUpload(List<LeadFromExcelDto> dtos) {
        leadService.massiveUpload(dtos);
    }

    @Override
    public ValidateFileResponseDto validate(MultipartFile file) {
        return leadService.validateLeads(file);
    }
}