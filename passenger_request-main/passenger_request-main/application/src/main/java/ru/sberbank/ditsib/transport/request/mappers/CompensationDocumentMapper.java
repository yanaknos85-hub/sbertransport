package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.CompensationDocumentDTO;

import java.util.List;

/**
 * Маппер для Документов на компенсацию
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
)
public interface CompensationDocumentMapper {
    
    CompensationDocumentDTO entityToDto(CompensationDocument entity);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    List<CompensationDocumentDTO> entitiesToDtoList(List<CompensationDocument> entities);
}
