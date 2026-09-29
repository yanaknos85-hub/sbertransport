package ru.sber.transport.address.business.use_cases.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.address.business.model.Address;
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.address.business.provider.*;
import ru.sber.transport.address.business.use_cases.Search;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Реализация бизнес-логики адресов.
 */
@Slf4j
@RequiredArgsConstructor
@Component
class SearchImpl implements Search {

    private final List<AddressProvider<? extends Address>> providers;

    @Override
    public List<Address> search(@NonNull String request) {
        return providers.parallelStream().flatMap(p -> p.getAddresses(request).parallelStream())
            .sorted(this::compareByType)
            .sorted(this::compareInType)
            .collect(new AddressesCollector());
    }

    @Override
    public Address search(@NonNull BigDecimal latitude, @NonNull BigDecimal longitude) {
        return providers.parallelStream().map(p -> p.getAddress(latitude, longitude))
            .filter(Optional::isPresent)
            .findFirst().orElse(Optional.empty()).orElse(null);
    }

    private int compareByType(Address addresses, Address addresses1) {
        var type1 = getWeight(addresses);
        var type2 = getWeight(addresses1);
        return type2 - type1;
    }

    private int compareInType(Address address, Address address1) {
        if (!address.getClass().equals(address1.getClass())) {
            return 0;
        }
        if (address instanceof FrequentlyAddress frequentlyAddress) {
            return ((FrequentlyAddress) address1).getCount() - (frequentlyAddress).getCount();
        }
        if (address instanceof FavoriteAddress favoriteAddress) {
            return (favoriteAddress.getLabel().compareTo(((FavoriteAddress) address1).getLabel()));
        }
        return toAddressString(address).compareTo(toAddressString(address1));
    }

    private int getWeight(Address addresses) {
        if (addresses instanceof FrequentlyAddress) {
            return 0b10;
        } else if (addresses instanceof FavoriteAddress) {
            return 0b01;
        }
        return 0b00;
    }

    private String toAddressString(Address address) {
        return "%s, %s, %s, %s, %s, %s".formatted(address.getRegion(), address.getCity(), address.getStreet(), address.getHouse(), address.getBuilding(), address.getStructure());
    }

    /**
     * Сборщик адресов. Из выборки исключаются повторяющиеся адреса в одинаковым текстовым или координатным значением.
     */
    private static class AddressesCollector implements Collector<Address, List<Address>, List<Address>> {

        @Override
        public Supplier<List<Address>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<Address>, Address> accumulator() {
            return (list, item) -> {
                if (list.stream().noneMatch(p -> Objects.equals(item, p))) {
                    list.add(item);
                }
            };
        }

        @Override
        public BinaryOperator<List<Address>> combiner() {
            return (left, right) -> {
                left.addAll(right);
                return left;
            };
        }

        @Override
        public Function<List<Address>, List<Address>> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of(Characteristics.IDENTITY_FINISH);
        }
    }
}
