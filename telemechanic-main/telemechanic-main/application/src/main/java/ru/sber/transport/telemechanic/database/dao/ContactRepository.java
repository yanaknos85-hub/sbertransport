package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.Contact;

import java.util.UUID;

/**
 * Контактные данные
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {
    
    @Modifying
    @Query(value = """
                   delete
                   from telemechanic.contact c
                   where c.id not in (select contact_id from telemechanic.organization_contact)
                   """, nativeQuery = true)
    void deleteAllUnused();
}
