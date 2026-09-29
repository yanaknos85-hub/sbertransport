package ru.sber.transport.telemechanic.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.ContactRepository;
import ru.sber.transport.telemechanic.database.model.Contact;
import ru.sber.transport.telemechanic.enumerate.ContactType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedPostgres
@Sql(scripts = {
        "/scripts/cleanup_database.sql",
        "/scripts/basic_corp_structure.sql"
})
class ContactRepositoryTest {
    
    @Autowired
    private ContactRepository contactRepository;
    
    @Test
    @Transactional
    void deleteAllUnused() {
        contactRepository.save(new Contact(UUID.randomUUID(), ContactType.PHONE, "+11122233344"));
        assertThat(contactRepository.findAll()).hasSize(3);
        contactRepository.deleteAllUnused();
        assertThat(contactRepository.findAll()).hasSize(2);
    }
}