package ru.sberbank.ditsib.transport.request.dto.mapper.db.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.sberbank.ditsib.transport.request.dto.personal.PersonalCarDTO;

@Component
@RequiredArgsConstructor
@Converter(autoApply = true)
public class PersonalCarDtoConverter implements AttributeConverter<PersonalCarDTO, String> {
    private final ObjectMapper objectMapper;
    
    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(PersonalCarDTO attribute) {
        if (attribute == null) {
            return null;
        }
        return objectMapper.writeValueAsString(attribute);
    }
    
    @SneakyThrows
    @Override
    public PersonalCarDTO convertToEntityAttribute(String dbData) {
        return !StringUtils.hasText(dbData) ? null : objectMapper.readValue(dbData, PersonalCarDTO.class);
    }
}
