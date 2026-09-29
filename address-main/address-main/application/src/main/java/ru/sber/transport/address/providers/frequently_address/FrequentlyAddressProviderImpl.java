package ru.sber.transport.address.providers.frequently_address;

import lombok.extern.slf4j.Slf4j;
import org.jooq.Field;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.address.database.TsVector;
import ru.sber.transport.address.messaging.providers.FrequentlyAddressProvider;
import ru.sber.transport.address.providers.indexed_address.BaseIndexedAddressProvider;
import ru.sber.transport.address.providers.indexed_address.mapper.AddressMapperDatabase;
import ru.sber.transport.database.addresses.tables.Frequently;
import ru.sber.transport.database.addresses.tables.records.FrequentlyRecord;

import java.math.BigDecimal;
import java.util.*;

/**
 * Реализация провайдера частых адресов.
 */
@Component
@Transactional
@Slf4j
class FrequentlyAddressProviderImpl extends BaseIndexedAddressProvider<FrequentlyAddress, Frequently, FrequentlyRecord> implements FrequentlyAddressProvider {

    public FrequentlyAddressProviderImpl(AddressMapperDatabase<FrequentlyAddress, FrequentlyRecord> mapper) {
        super(mapper);
    }

    @Override
    public boolean exists(String label, UUID userId, UUID... exclusions) {
        return false;
    }

    @Override
    public Frequently table() {
        return Frequently.FREQUENTLY;
    }

    @Override
    protected Field<UUID> id(Frequently table) {
        return table.ID;
    }

    @Override
    protected Field<TsVector> document(Frequently table) {
        return table.DOCUMENT;
    }

    @Override
    protected Field<UUID> owner(Frequently table) {
        return table.OWNER_ID;
    }

    @Override
    protected Field<BigDecimal> latitude(Frequently table) {
        return table.LATITUDE;
    }

    @Override
    protected Field<BigDecimal> longitude(Frequently table) {
        return table.LONGITUDE;
    }

    @Override
    protected Class<FrequentlyRecord> recordClass() {
        return FrequentlyRecord.class;
    }
}
