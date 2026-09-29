package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.messaging.messages.ContactMessage;
import ru.sber.transport.telemechanic.database.dao.ContactRepository;
import ru.sber.transport.telemechanic.database.model.Contact;
import ru.sber.transport.telemechanic.enumerate.ContactType;
import ru.sber.transport.telemechanic.service.ContactService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ContactServiceImpl implements ContactService {
    
    private final ContactRepository contactRepository;
    
    @Override
    @Transactional
    public void saveAll(List<ContactMessage> contacts) {
        contactRepository.saveAll(contacts.stream()
                                          .map(contactMessage -> new Contact(contactMessage.getId(),
                                                                             ContactType.valueOf(contactMessage.getType()),
                                                                             contactMessage.getValue()))
                                          .toList());
    }

    @Override
    @Transactional
    public void deleteAll(List<ContactMessage> contacts) {
        contactRepository.deleteAllById(contacts.stream()
                                                .map(ContactMessage::getId)
                                                .toList());
    }
    
    @Override
    @Transactional
    public void deleteAllUnused() {
        contactRepository.deleteAllUnused();
    }
}
