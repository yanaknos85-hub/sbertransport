package ru.sber.transport.notifications.database.model;

import java.util.UUID;

public interface HasId extends HasContactData {

    UUID getId();

}
