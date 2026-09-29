package ru.sber.transport.address.web.mapper;

/**
 * Маппер адресов.
 */
public interface AddressMapperToModel<T extends ru.sber.transport.address.business.model.Address, S> {

    T toModel(S source);

}
