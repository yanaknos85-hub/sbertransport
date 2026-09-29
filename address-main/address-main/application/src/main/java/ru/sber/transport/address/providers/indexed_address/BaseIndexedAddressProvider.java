package ru.sber.transport.address.providers.indexed_address;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Field;
import org.jooq.impl.TableImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.address.business.model.Address;
import ru.sber.transport.address.business.provider.AddressDataProvider;
import ru.sber.transport.address.business.provider.AddressProvider;
import ru.sber.transport.address.database.TsVector;
import ru.sber.transport.address.providers.indexed_address.mapper.AddressMapperDatabase;
import ru.sber.transport.address.providers.model.IndexedRecord;
import ru.sber.transport.address.utils.Translit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;



@RequiredArgsConstructor
@Slf4j
public abstract class BaseIndexedAddressProvider<M extends Address, T extends TableImpl<R>, R extends IndexedRecord>
    implements AddressProvider<M>, JooqRepository<T, R, UUID>, AddressDataProvider<M> {

    private final AddressMapperDatabase<M, R> mapper;

    @SuppressWarnings("java:S3958")
    @Override
    public Set<M> getAddresses(@NonNull String search) {
        log.debug("Поиск частых адресов для строки %s".formatted(search));

        var id = ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getToken().getId();

        var effectiveSearch = search.trim();
        var query = "%s:*".formatted(String.join(" & ", effectiveSearch.split(" ")));
        return context().selectFrom(table())
            .where("%s @@ '%s'::tsquery".formatted(document(table()).toString().toLowerCase(), Translit.transliterate(query.toLowerCase())))
            .and(owner(table()).eq(UUID.fromString(id)))
            .fetchInto(recordClass())
            .parallelStream()
            .map(mapper::toBusiness)
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<M> getAddress(@NonNull BigDecimal latitude, @NonNull BigDecimal longitude) {
        return context().selectFrom(table())
            .where(latitude(table()).eq(latitude.setScale(6, RoundingMode.HALF_EVEN)))
            .and(longitude(table()).eq(longitude.setScale(6, RoundingMode.HALF_EVEN)))
            .fetchOptional()
            .map(mapper::toBusiness);
    }

    @Override
    public M save(M source) {
        var data = findById(source.getId()).orElseGet(() -> mapper.toModel(source));
        mapper.update(data, source);
        if (data.getId() == null) {
            data.setId(UUID.randomUUID());
        }
        data.calculateVector();
        return mapper().toBusiness(save(data));
    }

    @Override
    public Collection<M> getOfOwner(UUID userId) {
        return context().selectFrom(table())
            .where(owner(table()).eq(userId))
            .fetchInto(recordClass()).parallelStream().map(mapper::toBusiness).toList();
    }

    @Override
    public Optional<M> get(UUID userId, UUID id) {
        return context().selectFrom(table())
            .where(owner(table()).eq(userId))
            .and(id(table()).eq(id))
            .fetchOptional().map(mapper()::toBusiness);
    }

    @Override
    public void delete(UUID userId, UUID id) {
        context().deleteFrom(table())
            .where(owner(table()).eq(userId))
            .and(id(table()).eq(id))
            .execute();
    }

    protected AddressMapperDatabase<M, R> mapper() {
        return mapper;
    }

    protected abstract Field<UUID> id(T table);

    protected abstract Field<TsVector> document(T table);

    protected abstract Field<UUID> owner(T table);

    protected abstract Field<BigDecimal> latitude(T table);

    protected abstract Field<BigDecimal> longitude(T table);

    protected abstract Class<R> recordClass();

}
