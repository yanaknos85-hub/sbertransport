package ru.sber.transport.address.web.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.address.business.use_cases.Addresses;
import ru.sber.transport.address.web.mapper.AddressMapperToWeb;
import ru.sber.transport.web.api.SelfAddressesFrequentlyApiDelegate;
import ru.sber.transport.web.model.UserAddress;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of frequently addresses controller.
 */
@RequiredArgsConstructor
@Component
class EnteredEmployeeFrequentlyAddressesControllerImpl extends BaseAddressControllerImpl
        implements SelfAddressesFrequentlyApiDelegate {

    private final Addresses<FrequentlyAddress> addresses;

    private final AddressMapperToWeb<FrequentlyAddress> mapper;

    @Override
    public ResponseEntity<List<UserAddress>> getAllFrequently() {
        var userId = getAuthenticated();
        return ResponseEntity.ok(addresses.get(userId).stream()
            .map(mapper::toUserWeb).toList());
    }

    @Override
    public ResponseEntity<Void> deleteFrequently(UUID id) {
        addresses.delete(getAuthenticated(), id);
        return ResponseEntity.ok().build();
    }
}
