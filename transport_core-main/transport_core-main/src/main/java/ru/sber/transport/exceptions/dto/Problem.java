package ru.sber.transport.exceptions.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Проблема.
 */
@Builder
@Getter
@EqualsAndHashCode(exclude = {"constraints"})
public class Problem {

    private final String field;

    private final String value;

    @Builder.Default
    private final List<Constraint> constraints = new ArrayList<>();

}