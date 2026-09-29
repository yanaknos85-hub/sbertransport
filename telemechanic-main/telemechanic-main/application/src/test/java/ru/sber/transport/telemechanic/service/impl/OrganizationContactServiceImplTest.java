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
import ru.sber.transport.telemechanic.database.dao.OrganizationContactRepository;
import ru.sber.transport.telemechanic.database.model.OrganizationContact;
import ru.sber.transport.telemechanic.database.model.OrganizationContactKey;
import ru.sber.transport.telemechanic.enumerate.ContactType;
import ru.sber.transport.telemechanic.service.ContactService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationContactServiceImplTest {
    
    @InjectMocks
    private OrganizationContactServiceImpl organizationContactService;
    @Mock
    private ContactService contactService;
    @Mock
    private OrganizationContactRepository organizationContactRepository;
    @Captor
    private ArgumentCaptor<List<OrganizationContact>> listArgumentCaptor;
    
    @Test
    void save() {
        var organizationId = UUID.randomUUID();
        var message1 = Instancio.of(ContactMessage.class)
                                .set(field(ContactMessage::getType), ContactType.PHONE.name())
                                .set(field(ContactMessage::isDeleted), false)
                                .create();
        var message2 = Instancio.of(ContactMessage.class)
                                .set(field(ContactMessage::getType), ContactType.SITE.name())
                                .set(field(ContactMessage::isDeleted), true)
                                .create();
        var expected1 = new OrganizationContact(new OrganizationContactKey(organizationId, message1.getId()));
        doNothing().when(contactService).saveAll(Collections.singletonList(message1));
        doReturn(Collections.singletonList(expected1)).when(organizationContactRepository).saveAll(listArgumentCaptor.capture());
        doNothing().when(organizationContactRepository)
                   .deleteAllById(Collections.singletonList(new OrganizationContactKey(organizationId, message2.getId())));
        doNothing().when(contactService).deleteAll(Collections.singletonList(message2));
        organizationContactService.save(organizationId, Stream.of(message1, message2)
                                                              .collect(Collectors.toCollection(ArrayList::new)));
        var actual = listArgumentCaptor.getValue();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(List.of(expected1));
        verify(contactService).saveAll(anyList());
        verify(organizationContactRepository).saveAll(anyList());
        verify(organizationContactRepository).deleteAllById(anyList());
        verify(contactService).deleteAll(anyList());
    }
    
    @Test
    void deleteAll() {
        var organizationId = UUID.randomUUID();
        doNothing().when(organizationContactRepository).deleteAllByOrganizationContactKeyOrganizationId(organizationId);
        doNothing().when(contactService).deleteAllUnused();
        organizationContactService.deleteAll(organizationId);
        verify(organizationContactRepository).deleteAllByOrganizationContactKeyOrganizationId(any(UUID.class));
        verify(contactService).deleteAllUnused();
    }
}