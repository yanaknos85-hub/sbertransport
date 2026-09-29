package ru.sber.transport.contractor.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * Маппер логических представлений.
 */
@Mapper
public interface BooleanMapper {

    /**
     * Name of a boolean negation operation.
     */
    String NEGATE = "negate";

    /**
     * Negate boolean value.
     *
     * @param source source value.
     * @return negated value.
     */
    @Named(NEGATE)
    default boolean doNegate(boolean source) {
        return !source;
    }

}
