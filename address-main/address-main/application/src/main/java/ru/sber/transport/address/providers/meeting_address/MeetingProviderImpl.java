package ru.sber.transport.address.providers.meeting_address;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.address.business.model.MeetingAddress;
import ru.sber.transport.address.business.provider.AddressDataProvider;
import ru.sber.transport.address.business.provider.MeetingAddressProvider;
import ru.sber.transport.address.providers.meeting_address.mapper.MeetingAddressDatabaseMapper;
import ru.sber.transport.database.addresses.tables.Meetings;
import ru.sber.transport.database.addresses.tables.records.MeetingsRecord;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
class MeetingProviderImpl implements MeetingAddressProvider, AddressDataProvider<MeetingAddress>, JooqRepository<Meetings, MeetingsRecord, UUID> {

    private final MeetingAddressDatabaseMapper mapper;

    @Override
    public Collection<MeetingAddress> getOfOwner(UUID ownerId) {
        return context().selectFrom(table()).where(table().ORGANIZATION_ID.eq(ownerId)).fetchInto(MeetingsRecord.class)
            .parallelStream().map(mapper::toBusiness).toList();
    }

    @Override
    public Optional<MeetingAddress> get(UUID ownerId, UUID id) {
        return context().selectFrom(table()).where(table().ID.eq(id)).and(table().ORGANIZATION_ID.eq(ownerId)).fetchOptional()
            .map(mapper::toBusiness);
    }

    @Override
    public MeetingAddress save(MeetingAddress address) {
        var found = findById(address.getId()).orElseGet(MeetingsRecord::new);
        if (found.getId() == null) {
            found.setId(UUID.randomUUID());
        }
        mapper.update(found, address);
        return mapper.toBusiness(save(found));
    }

    @Override
    public void delete(UUID ownerId, UUID id) {
        context().deleteFrom(table()).where(table().ID.eq(id)).and(table().ORGANIZATION_ID.eq(ownerId)).execute();
    }

    @Override
    public boolean exists(String label, UUID userId, UUID... exclusions) {
        var request = context().selectFrom(table())
            .where(table().LABEL.eq(label))
            .and(table().ORGANIZATION_ID.eq(userId));
        if (exclusions.length > 0) {
            request = request.and(table().ID.notIn(exclusions));
        }
        return context().fetchExists(request);
    }

    @Override
    public Optional<MeetingAddress> get(String label) {
        return context().selectFrom(table())
            .where(table().LABEL.eq(label))
            .fetchOptional().map(mapper::toBusiness);
    }

    @Override
    public List<MeetingAddress> get() {
        return findAll().parallelStream().map(mapper::toBusiness).toList();
    }

    @Override
    public Meetings table() {
        return Meetings.MEETINGS;
    }
}
