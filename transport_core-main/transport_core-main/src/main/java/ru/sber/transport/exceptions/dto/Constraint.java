package ru.sber.transport.exceptions.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Ограничение.
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Constraint {

    private final String type;

    private final Object value;

}