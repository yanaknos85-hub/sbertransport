package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.messaging.messages.ContactMessage;
import ru.sber.transport.telemechanic.database.dao.OrganizationContactRepository;
import ru.sber.transport.telemechanic.database.model.OrganizationContact;
import ru.sber.transport.telemechanic.database.model.OrganizationContactKey;
import ru.sber.transport.telemechanic.service.ContactService;
import ru.sber.transport.telemechanic.service.OrganizationContactService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrganizationContactServiceImpl implements OrganizationContactService {
    
    private final ContactService contactService;
    private final OrganizationContactRepository organizationContactRepository;
    
    @Override
    @Transactional
    public void save(UUID organizationId, List<ContactMessage> contacts) {
        var contactsToDelete = contacts.stream()
                                       .filter(ContactMessage::isDeleted)
                                       .toList();
        contacts.removeAll(contactsToDelete);
        saveContacts(organizationId, contacts);
        deleteContacts(organizationId, contactsToDelete);
    }
    
    @Override
    @Transactional
    public void deleteAll(UUID organizationId) {
        organizationContactRepository.deleteAllByOrganizationContactKeyOrganizationId(organizationId);
        contactService.deleteAllUnused();
    }
    
    private void saveContacts(UUID organizationId, List<ContactMessage> contacts) {
        if (!contacts.isEmpty()) {
            contactService.saveAll(contacts);
            organizationContactRepository.saveAll(contacts.stream()
                                                          .map(contactMessage -> new OrganizationContact(new OrganizationContactKey(organizationId,
                                                                                                                                    contactMessage.getId())))
                                                          .toList());
        }
    }
    
    private void deleteContacts(UUID organizationId, List<ContactMessage> contacts) {
        if (!contacts.isEmpty()) {
            organizationContactRepository.deleteAllById(contacts.stream()
                                                                .map(contactMessage -> new OrganizationContactKey(organizationId,
                                                                                                                  contactMessage.getId()))
                                                                .toList());
            contactService.deleteAll(contacts);
        }
    }
}
