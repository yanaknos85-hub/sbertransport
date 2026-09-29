package ru.sber.transport.address.providers.favorite_address;

import lombok.extern.slf4j.Slf4j;
import org.jooq.Field;
import org.springframework.stereotype.Component;
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.address.database.TsVector;
import ru.sber.transport.address.messaging.providers.FavoriteAddressProvider;

import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.address.providers.indexed_address.BaseIndexedAddressProvider;
import ru.sber.transport.address.providers.indexed_address.mapper.AddressMapperDatabase;
import ru.sber.transport.database.addresses.tables.Favorite;
import ru.sber.transport.database.addresses.tables.records.FavoriteRecord;

import java.math.BigDecimal;
import java.util.*;

/**
 * Реализация провайдера любимых адресов.
 */
@Component
@Transactional
@Slf4j
class FavoriteAddressProviderImpl extends BaseIndexedAddressProvider<FavoriteAddress, Favorite, FavoriteRecord> implements FavoriteAddressProvider {

    public FavoriteAddressProviderImpl(AddressMapperDatabase<FavoriteAddress, FavoriteRecord> mapper) {
        super(mapper);
    }

    @Override
    protected Field<UUID> id(Favorite table) {
        return table.ID;
    }

    @Override
    protected Field<TsVector> document(Favorite table) {
        return table.DOCUMENT;
    }

    @Override
    protected Field<UUID> owner(Favorite table) {
        return table.OWNER_ID;
    }

    @Override
    protected Field<BigDecimal> latitude(Favorite table) {
        return table.LATITUDE;
    }

    @Override
    protected Field<BigDecimal> longitude(Favorite table) {
        return table.LONGITUDE;
    }

    @Override
    protected Class<FavoriteRecord> recordClass() {
        return FavoriteRecord.class;
    }

    @Override
    public boolean exists(String label, UUID userId, UUID... exclusions) {
        var request = context().selectFrom(table())
            .where(table().LABEL.eq(label))
            .and(table().OWNER_ID.eq(userId));
        if (exclusions.length > 0) {
            request = request.and(table().ID.notIn(exclusions));
        }
        return context().fetchExists(request);
    }

    @Override
    public Optional<FavoriteAddress> get(UUID id) {
        return findById(id).map(mapper()::toBusiness);
    }

    @Override
    public void delete(UUID id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    public Favorite table() {
        return Favorite.FAVORITE;
    }
}
