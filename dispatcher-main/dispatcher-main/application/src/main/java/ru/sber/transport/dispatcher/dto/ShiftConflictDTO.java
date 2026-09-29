package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Setter
@Getter
@SuperBuilder
@Schema(title = "Данные о коинфликтах", description = "Данные о коинфликтах в смене")
public class ShiftConflictDTO extends ShiftDTO {

    private String message;

    private ConflictEntityDTO entity;

    private List<ProblemDTO> problems;

}
