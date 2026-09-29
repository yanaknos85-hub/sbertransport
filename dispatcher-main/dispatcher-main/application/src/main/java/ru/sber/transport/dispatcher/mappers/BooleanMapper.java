package ru.sber.transport.dispatcher.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * Маппер логических представлений.
 */
@Mapper
public interface BooleanMapper {

    String NEGATE = "negate";

    @Named(NEGATE)
    default boolean doNegate(boolean source) {
        return !source;
    }

}
