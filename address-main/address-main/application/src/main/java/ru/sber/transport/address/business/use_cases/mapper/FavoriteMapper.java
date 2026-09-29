package ru.sber.transport.address.business.use_cases.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.sber.transport.address.business.model.FavoriteAddress;

@Mapper
public interface FavoriteMapper extends AddressBusinessMapper<FavoriteAddress> {

    void update(@MappingTarget FavoriteAddress target, FavoriteAddress source);

}
