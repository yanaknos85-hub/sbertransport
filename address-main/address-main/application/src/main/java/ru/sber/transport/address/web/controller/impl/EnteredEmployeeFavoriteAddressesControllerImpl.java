package ru.sber.transport.address.web.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.address.business.use_cases.Addresses;
import ru.sber.transport.address.web.mapper.FavoriteAddressWebMapper;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.web.api.SelfAddressesFavoriteApiDelegate;
import ru.sber.transport.web.model.NewFavoriteAddress;
import ru.sber.transport.web.model.UserAddress;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of controller of entered employees addresses.
 */
@Component
@RequiredArgsConstructor
class EnteredEmployeeFavoriteAddressesControllerImpl extends BaseAddressControllerImpl
        implements SelfAddressesFavoriteApiDelegate {

    private final Addresses<FavoriteAddress> addresses;

    private final FavoriteAddressWebMapper addressMapper;

    @Override
    public ResponseEntity<UserAddress> postFavorite(NewFavoriteAddress newAddress) {
        var userId = getAuthenticated();
        return ResponseEntity.ok(addressMapper.toUserWeb(addresses.save(userId, addressMapper.toModel(newAddress))));
    }

    @Override
    public ResponseEntity<List<UserAddress>> getAllFavorites() {
        var userId = getAuthenticated();
        return ResponseEntity.ok(addresses.get(userId).stream().map(addressMapper::toUserWeb).toList());
    }

    @Override
    public ResponseEntity<UserAddress> getFavorite(UUID id) {
        var userId = getAuthenticated();
        var address = addresses.get(userId, id).orElseThrow(() -> new EntityNotFoundException(FavoriteAddress.class, id));
        return ResponseEntity.ok(addressMapper.toUserWeb(address));
    }

    @Override
    public ResponseEntity<Void> putFavorite(NewFavoriteAddress newAddress, UUID id) {
        var userId = getAuthenticated();
        addresses.save(userId, id, addressMapper.toModel(newAddress));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteFavorite(UUID id) {
        var userId = getAuthenticated();
        addresses.delete(userId, id);
        return ResponseEntity.ok().build();
    }
}
