package ru.sber.transport.address.business.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FrequentlyAddress extends OwnedAddress {

    private int count;

    private boolean first;
}
