package ru.sber.transport.address.web.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.address.business.use_cases.Addresses;
import ru.sber.transport.address.web.mapper.AddressMapperToWeb;
import ru.sber.transport.web.api.SelfAddressesApiDelegate;
import ru.sber.transport.web.model.UserAddress;

import java.util.List;

/**
 * Implementation of entered employees addresses controller.
 */
@RequiredArgsConstructor
@Component
class EnteredEmployeeAddressesControllerImpl extends BaseAddressControllerImpl implements SelfAddressesApiDelegate {

    private final Addresses<FrequentlyAddress> addresses;

    private final AddressMapperToWeb<FrequentlyAddress> addressMapper;

    @Override
    public ResponseEntity<List<UserAddress>> getAll() {
        return ResponseEntity.ok(addresses.get(getAuthenticated()).parallelStream()
            .map(addressMapper::toUserWeb)
            .toList());
    }
}
