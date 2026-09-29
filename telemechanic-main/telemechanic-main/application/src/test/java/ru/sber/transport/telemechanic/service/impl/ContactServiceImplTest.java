package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.messaging.messages.ContactMessage;
import ru.sber.transport.telemechanic.database.dao.ContactRepository;
import ru.sber.transport.telemechanic.database.model.Contact;
import ru.sber.transport.telemechanic.enumerate.ContactType;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {
    
    @InjectMocks
    private ContactServiceImpl contactService;
    @Mock
    private ContactRepository contactRepository;
    @Captor
    private ArgumentCaptor<List<Contact>> listContactArgumentCaptor;
    @Captor
    private ArgumentCaptor<List<UUID>> listUuidArgumentCaptor;
    
    @Test
    void saveAll() {
        var message1 = Instancio.of(ContactMessage.class)
                                       .set(field(ContactMessage::getType), ContactType.PHONE.name())
                                       .create();
        var message2 = Instancio.of(ContactMessage.class)
                                .set(field(ContactMessage::getType), ContactType.SITE.name())
                                .create();
        var expected1 = new Contact(message1.getId(), ContactType.valueOf(message1.getType()), message1.getValue());
        var expected2 = new Contact(message2.getId(), ContactType.valueOf(message2.getType()), message2.getValue());
        doReturn(List.of(expected1, expected2)).when(contactRepository).saveAll(listContactArgumentCaptor.capture());
        contactService.saveAll(List.of(message1, message2));
        var actual = listContactArgumentCaptor.getValue();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(List.of(expected1, expected2));
        verify(contactRepository).saveAll(anyCollection());
    }
    
    @Test
    void deleteAll() {
        var message1 = Instancio.create(ContactMessage.class);
        var message2 = Instancio.create(ContactMessage.class);
        doNothing().when(contactRepository).deleteAllById(listUuidArgumentCaptor.capture());
        contactService.deleteAll(List.of(message1, message2));
        var actual = listUuidArgumentCaptor.getValue();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(List.of(message1.getId(), message2.getId()));
        verify(contactRepository).deleteAllById(anyCollection());
    }
    
    @Test
    void deleteAllUnused() {
        doNothing().when(contactRepository).deleteAllUnused();
        contactService.deleteAllUnused();
        verify(contactRepository).deleteAllUnused();
    }
}