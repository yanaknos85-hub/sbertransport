package ru.sberbank.ditsib.transport.reports.mappers.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForGroupTransferReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.mappers.RequestForXlsxMapper;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Component
@NoArgsConstructor
public class RequestForXlsxMapperImpl implements RequestForXlsxMapper {
    
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    @Override
    public RequestForPersonalReportDTO toPersonalDto(Map<String, ?> parameters) {
        return mapToRequestReportDTO(parameters, RequestForPersonalReportDTO.class);
    }
    
    @Override
    public RequestForPublicReportDTO toPublicDto(Map<String, ?> parameters) {
        return mapToRequestReportDTO(parameters, RequestForPublicReportDTO.class);
    }
    
    @Override
    public RequestForTaxiReportDTO toTaxiDto(Map<String, ?> parameters) {
        return mapToRequestReportDTO(parameters, RequestForTaxiReportDTO.class);
    }
    
    @Override
    public RequestForCarsharingReportDTO toCarsharingDto(Map<String, ?> parameters) {
        return mapToRequestReportDTO(parameters, RequestForCarsharingReportDTO.class);
    }
    
    @Override
    public RequestForGroupTransferReportDTO toGroupTransferDto(Map<String, ?> parameters) {
        return mapToRequestReportDTO(parameters, RequestForGroupTransferReportDTO.class);
    }
    
    private <T> T mapToRequestReportDTO(Map<String, ?> parameters, Class<T> clazz) {
        var filters = (String) Optional.ofNullable(parameters).map(p -> p.get("filters")).orElseThrow(() -> new RuntimeException("Отсутствует " +
                                                                                                                                 "параметр filters"));
        T requestReportDTO;
        try {
            requestReportDTO = mapper.readValue(new String(Base64.getDecoder().decode(filters), StandardCharsets.UTF_8),
                                                clazz);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось разобрать параметр filters", e);
        }
        
        var validatorFactory = Validation.buildDefaultValidatorFactory();
        var validationResult = validatorFactory.getValidator().validate(requestReportDTO);
        if (!validationResult.isEmpty()) {
            throw new ConstraintViolationException(validationResult);
        }
        
        return requestReportDTO;
    }
}
