package ru.sber.transport.address.providers.favorite_address.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.address.providers.indexed_address.mapper.AddressMapperDatabase;
import ru.sber.transport.database.addresses.tables.records.FavoriteRecord;

@Mapper
public interface FavoriteAddressMapperDatabase extends AddressMapperDatabase<FavoriteAddress, FavoriteRecord> {

    @Mapping(target = "owner", source = "ownerId")
    FavoriteAddress toBusiness(FavoriteRecord source);

    @Mapping(target = "ownerId", source = "owner")
    @Mapping(target = "document", ignore = true)
    FavoriteRecord toModel(FavoriteAddress source);

    @Override
    @Mapping(target = "ownerId", source = "owner")
    @Mapping(target = "document", ignore = true)
    void update(@MappingTarget FavoriteRecord target, FavoriteAddress source);
}
