package ru.sber.transport.address.web.resolver.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.address.business.model.MeetingAddress;
import ru.sber.transport.address.web.resolver.model.MeetingAddressFileDTO;

/**
 * Business to files mapper.
 */
@Mapper
public interface MeetingAddressFileMapper {

    /**
     * File to business.
     *
     * @param source source with data.
     * @return entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)
    MeetingAddress toBusiness(MeetingAddressFileDTO source);
}
