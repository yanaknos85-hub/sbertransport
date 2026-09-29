package ru.sber.transport.address.web.mapper;

import ru.sber.transport.web.model.Address;
import ru.sber.transport.web.model.UserAddress;

/**
 * Маппер адресов.
 */
public interface AddressMapperToWeb<T extends ru.sber.transport.address.business.model.Address> {

    Address toWeb(T source);

    UserAddress toUserWeb(T source);
}
