package ru.sber.transport.exceptions.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные сущности.
 */
@Builder
@Getter
public class Entity {

    private final String name;

    private final Object id;

}