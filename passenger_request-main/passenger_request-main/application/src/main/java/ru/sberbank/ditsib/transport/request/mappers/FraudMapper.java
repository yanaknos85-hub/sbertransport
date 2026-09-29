package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.request.database.model.FraudData;
import ru.sberbank.ditsib.transport.request.dto.FraudDto;
import ru.sberbank.ditsib.transport.request.dto.fraud.FraudCommentDTO;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Маппер данных о фроде
 */
@Mapper
public interface FraudMapper {

    /**
     * Собрать строку с данными о фроде
     *
     * @param source список данных о фроде
     * @return строка с данными о фроде
     */
    default FraudDto constructFraudDto(List<FraudData> source) {
        return Optional.ofNullable(constructFraudMessage(source)).map(FraudDto::new).orElse(null);
    }

    /**
     * Собрать строку с данными о фроде
     *
     * @param source список данных о фроде
     * @return строка с данными о фроде
     */
    default String constructFraudMessage(List<FraudData> source) {
        return Optional.ofNullable(source).flatMap(list -> list.stream()
                        .map(it -> it.getType().getDescription() + ": " + it.getComment())
                        .reduce((a, b) -> a + "\n" + b))
                .orElse(null);
    }

    /**
     * Собрать список с данными о фроде
     *
     * @param source список данных о фроде
     * @return список
     */
    default List<FraudCommentDTO> constructFraudComments(List<FraudData> source) {
        return Optional.ofNullable(source)
                .map(
                        list -> list
                                .stream()
                                .map(it -> new FraudCommentDTO(it.getComment(), null, null))
                                .toList()
                )
                .orElse(Collections.emptyList());
    }

    List<RequestMessage.Fraud> toFraudDataList(List<FraudData> fraudDataList);

    @Mapping(target = "requestId", source = "fraudData.request.id")
    RequestMessage.Fraud toFraudData(FraudData fraudData);
}
