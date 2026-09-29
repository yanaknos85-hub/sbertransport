package ru.sber.transport.address.providers.frequently_address.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.address.providers.indexed_address.mapper.AddressMapperDatabase;
import ru.sber.transport.database.addresses.tables.records.FrequentlyRecord;

@Mapper
public interface FrequentlyAddressMapperDatabase extends AddressMapperDatabase<FrequentlyAddress, FrequentlyRecord> {

    @Mapping(target = "owner", source = "ownerId")
    @Mapping(target = "count", source = "usages")
    FrequentlyAddress toBusiness(FrequentlyRecord source);

    @Mapping(target = "ownerId", source = "owner")
    @Mapping(target = "usages", source = "count")
    @Mapping(target = "document", ignore = true)
    FrequentlyRecord toModel(FrequentlyAddress source);

    @Override
    @Mapping(target = "ownerId", source = "owner")
    @Mapping(target = "usages", source = "count")
    @Mapping(target = "document", ignore = true)
    void update(@MappingTarget FrequentlyRecord target, FrequentlyAddress source);
}
