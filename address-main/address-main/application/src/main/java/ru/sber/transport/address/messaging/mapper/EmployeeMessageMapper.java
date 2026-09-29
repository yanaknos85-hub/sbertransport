package ru.sber.transport.address.messaging.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.address.business.model.Employee;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

/**
 * Interface to map employee from message to business.
 */
@Mapper
public interface EmployeeMessageMapper {

    /**
     * Convert message to business.
     *
     * @param source message.
     * @return business entity.
     */
    Employee toBusiness(EmployeeMessage source);

    /**
     * Convert message to business.
     *
     * @param source message.
     * @return business entity.
     */
    Employee toBusiness(ru.sber.transport.messages.corporate.avro.EmployeeMessage source);

}
