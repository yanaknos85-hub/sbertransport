package ru.sber.transport.address.business.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public abstract class OwnedAddress extends GeoAddress {

    private UUID owner;

}
