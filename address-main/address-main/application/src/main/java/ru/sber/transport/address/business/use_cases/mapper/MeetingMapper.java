package ru.sber.transport.address.business.use_cases.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.address.business.model.GeoAddress;
import ru.sber.transport.address.business.model.MeetingAddress;

/**
 * Business mapper of corporate addresses.
 */
@Mapper
public interface MeetingMapper extends AddressBusinessMapper<MeetingAddress> {

    /**
     * Update the target entity.
     *
     * @param target target to update.
     * @param source source with data.
     */
    void update(@MappingTarget MeetingAddress target, MeetingAddress source);

    /**
     * Update the target entity.
     *
     * @param target target to update.
     * @param source source with data.
     */
    @Mapping(target = "label", ignore = true)
    void update(@MappingTarget MeetingAddress target, GeoAddress source);
}
