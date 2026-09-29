package ru.sberbank.ditsib.transport.request.database.model.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sberbank.ditsib.transport.request.dto.GroupTransferRequestInformationDTO;

@Converter
@RequiredArgsConstructor
@Slf4j
public class NewRequestInformationDTOToStringConverter implements AttributeConverter<GroupTransferRequestInformationDTO, String> {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public String convertToDatabaseColumn(GroupTransferRequestInformationDTO attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public GroupTransferRequestInformationDTO convertToEntityAttribute(String dbData) {
        if (dbData == null || "null".equals(dbData)) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, GroupTransferRequestInformationDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
