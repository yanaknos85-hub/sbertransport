package ru.sber.transport.address.providers.meeting_address.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.address.business.model.MeetingAddress;
import ru.sber.transport.database.addresses.tables.records.MeetingsRecord;

/**
 * Database to business entities mapper.
 */
@Mapper
public interface MeetingAddressDatabaseMapper {

    /**
     * Map database to business.
     *
     * @param source entity.
     * @return mapped entity.
     */
    MeetingAddress toBusiness(MeetingsRecord source);

    /**
     * Map business to database.
     *
     * @param source entity.
     * @return mapped entity.
     */
    MeetingsRecord toModel(MeetingAddress source);

    /**
     * Update database entity with business data.
     *
     * @param target database entity.
     * @param source business entity.
     */
    @Mapping(target = "id", ignore = true)
    void update(@MappingTarget MeetingsRecord target, MeetingAddress source);
}
