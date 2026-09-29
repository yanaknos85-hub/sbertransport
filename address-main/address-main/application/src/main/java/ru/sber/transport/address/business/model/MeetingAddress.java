package ru.sber.transport.address.business.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Corporate address entity.
 */
@Getter
@Setter
public class MeetingAddress extends OwnedAddress {

    /**
     * Label of address.
     */
    private String label;

    public UUID getOrganizationId() {
        return getOwner();
    }

    public void setOrganizationId(UUID owner) {
        setOwner(owner);
    }
}
