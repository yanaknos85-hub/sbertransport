package ru.sberbank.ditsib.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;
import ru.sberbank.ditsib.dto.*;

/**
 * Маппер для преобразования доменных моделей в DTO
 * Обеспечивает конвертацию данных между слоями приложения
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class MainLeadMapper {

    /**
     * Преобразует доменную модель в DTO для внешнего API
     *
     * @param model доменная модель запроса
     * @return DTO для отправки во внешний сервис
     */
    @Mapping(target = "timeToWork", constant = "1")
    public abstract CreateMainLeadRequestDto toDto(MainLeadRequestModel model);

    protected abstract CoordinateDto tariffCoordinateModelToCoordinateDto(TariffCoordinateModel source);
}