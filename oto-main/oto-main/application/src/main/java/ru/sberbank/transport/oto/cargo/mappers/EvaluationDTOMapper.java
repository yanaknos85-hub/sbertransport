package ru.sberbank.transport.oto.cargo.mappers;

import org.mapstruct.*;
import ru.sberbank.ditsib.transport.request.messaging.EvaluationMessage;
import ru.sberbank.transport.oto.cargo.database.model.Evaluation;
import ru.sberbank.transport.oto.cargo.dto.EvaluationDTO;

/**
 * Маппер для работы с оценками заявок
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
        )
public interface EvaluationDTOMapper {
    
    @Mapping(target = "requestId", source = "request.id")
    EvaluationDTO toDTO(Evaluation evaluation);
    
    Evaluation fromDTO(EvaluationDTO evaluationDTO);

    @Mapping(target = "requestId", source = "request.id")
    EvaluationMessage toMessage(Evaluation evaluation);

    Evaluation fromMessage(EvaluationMessage source);

}
