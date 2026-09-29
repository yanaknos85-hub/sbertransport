package ru.sber.transport.dispatcher.controller;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.database.model.Dispatcher;

import java.util.UUID;

public interface DispatcherCreator {

    default Dispatcher createTestDispatcher(UUID id, Contractor contractor, int number) {
        return jdbcTemplate().queryForObject("""
                INSERT INTO dispatcher.dispatcher
                 (id, human_readable_id, last_name, first_name, patronymic, phone, email, contractor_id)
                 VALUES
                 (?, ?, 'Ivan', 'Petrov', 'Petrovich', '+79000000000', ?, ?)
                 RETURNING *
                """, new DataClassRowMapper<>(Dispatcher.class), id, "HRIDISPATCHER" + number, "email%d@mail.ru".formatted(number), contractor.getId());
    }

    default Dispatcher createTestDispatcher(UUID id, Contractor contractor) {
        return createTestDispatcher(id, contractor, 1);
    }

    JdbcTemplate jdbcTemplate();

}
