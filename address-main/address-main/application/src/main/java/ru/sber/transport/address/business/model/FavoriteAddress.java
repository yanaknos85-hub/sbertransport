package ru.sber.transport.address.business.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FavoriteAddress extends OwnedAddress {

    private String label;

}
