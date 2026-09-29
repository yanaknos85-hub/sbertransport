package ru.sber.transport.notifications.messaging.listeners.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.sber.transport.messages.corporate.avro.Contact;
import ru.sber.transport.messages.corporate.avro.ContactEmployeeType;
import ru.sber.transport.messages.corporate.avro.EmployeeMessage;
import ru.sber.transport.notifications.database.model.coprorate.Employee;

import java.util.List;

/**
 * Маппер сотрудников.
 */
@Mapper
public interface EmployeeMapper {

    /**
     * Обновить данные.
     *
     * @param target цель.
     * @param source источник.
     */
    @Mapping(target = "phone", source = "contacts", qualifiedByName = "phone")
    @Mapping(target = "phoneConfirmed", source = "contacts", qualifiedByName = "isConfirmedPhone")
    @Mapping(target = "email", source = "contacts", qualifiedByName = "email")
    void update(@MappingTarget Employee target, EmployeeMessage source);

    /**
     * Получить телефон.
     *
     * @param contacts контакты.
     * @return телефон.
     */
    @Named("phone")
    default String phone(List<Contact> contacts) {
        return contacts.stream().filter(it -> ContactEmployeeType.MOBILE.equals(it.getType()))
                .map(Contact::getValue).findFirst().orElse(null);
    }

    @Named("isConfirmedPhone")
    default boolean isConfirmedPhone(List<Contact> contacts) {
        return contacts.stream()
                .filter(it -> ContactEmployeeType.MOBILE.equals(it.getType()))
                .anyMatch(Contact::getIsConfirmed);
    }

    /**
     * Получить E-Mail.
     *
     * @param contacts контакты.
     * @return E-Mail.
     */
    @Named("email")
    default String email(List<Contact> contacts) {
        return contacts.stream()
                .filter(it -> ContactEmployeeType.EMAIL.equals(it.getType()))
                .map(Contact::getValue).findFirst().orElse(null);
    }

}
