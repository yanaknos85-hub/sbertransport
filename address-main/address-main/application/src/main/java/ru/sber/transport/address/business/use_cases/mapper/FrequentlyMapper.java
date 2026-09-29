package ru.sber.transport.address.business.use_cases.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.address.business.model.GeoAddress;
import ru.sber.transport.address.business.model.FrequentlyAddress;

/**
 * Бизнес-маппер частых адресов.
 */
@Mapper
public interface FrequentlyMapper extends AddressBusinessMapper<FrequentlyAddress> {

    @Override
    void update(@MappingTarget FrequentlyAddress target, FrequentlyAddress source);

    /**
     * Построить объект частого адреса по обычному.
     *
     * @param source исходный адрес.
     * @return частый адрес.
     */
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "count", constant = "0")
    @Mapping(target = "first", constant = "false")
    FrequentlyAddress toFrequently(GeoAddress source);
}
