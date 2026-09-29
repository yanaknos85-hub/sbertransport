package ru.sber.transport.notifications.database.model;

public interface HasPhone extends HasContactData {

    String getPhone();

    boolean isPhoneConfirmed();

}
