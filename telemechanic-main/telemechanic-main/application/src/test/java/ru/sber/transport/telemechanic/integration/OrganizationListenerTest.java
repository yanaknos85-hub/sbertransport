package ru.sber.transport.telemechanic.integration;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.ContactRepository;
import ru.sber.transport.telemechanic.database.dao.OrganizationContactRepository;
import ru.sber.transport.telemechanic.database.dao.OrganizationGroupRepository;
import ru.sber.transport.telemechanic.database.dao.OrganizationRepository;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.enumerate.ContactType;
import ru.sberbank.ditsib.transport.messaging.messages.ContactMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@SpringBootTest(properties = "extlogging.kafka: false")
@EmbeddedPostgres
class OrganizationListenerTest extends KafkaTest {
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private OrganizationGroupRepository organizationGroupRepository;
    @Autowired
    private OrganizationContactRepository organizationContactRepository;
    @Autowired
    private ContactRepository contactRepository;
    
    @Test
    @Sql(scripts = { "/scripts/cleanup_database.sql", "/scripts/basic_corp_structure.sql" })
    @SneakyThrows
    void listen() {
        var organizationGroup = new OrganizationGroup(UUID.randomUUID(),
                                                      "name1",
                                                      false);
        var organization1 = new Organization(UUID.randomUUID(),
                                             1L,
                                             "officialName1",
                                             "msrn1",
                                             "tin1",
                                             organizationGroup.getId(),
                                             true, emptySet(), null, null);
        var organization2 = new Organization(UUID.randomUUID(),
                                             2L,
                                             "officialName2",
                                             "msrn2",
                                             "tin2",
                                             null,
                                             true, emptySet(), null, null);
        var organization3 = new Organization(UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"),
                                             8L,
                                             "ЦА",
                                             "88888888",
                                             "888888",
                                             null,
                                             true, emptySet(), null, null);
        var organization4 = new Organization(UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"),
                                             11L,
                                             "Тест2",
                                             "11111111",
                                             "111111",
                                             null,
                                             true, emptySet(), null, null);
        var organization5 = new Organization(UUID.fromString("11501599-b498-4e9c-8b75-3e5899c445c0"),
                                             77L,
                                             "Тест3",
                                             "11111122",
                                             "111122",
                                             null,
                                             true, emptySet(), null, null
                                             );
        var contactMessage1 = Instancio.of(ContactMessage.class)
                                .set(field(ContactMessage::getType), ContactType.SITE.name())
                                .set(field(ContactMessage::isDeleted), false)
                                .create();
        var contactMessage2 = Instancio.of(ContactMessage.class)
                                       .set(field(ContactMessage::getType), ContactType.PHONE.name())
                                       .set(field(ContactMessage::isDeleted), false)
                                       .create();
        var contactMessage3 = Instancio.of(ContactMessage.class)
                                       .set(field(ContactMessage::getType), ContactType.EMAIL.name())
                                       .set(field(ContactMessage::isDeleted), false)
                                       .create();
        var contactMessage4 = Instancio.of(ContactMessage.class)
                                       .set(field(ContactMessage::getType), ContactType.EMAIL.name())
                                       .set(field(ContactMessage::isDeleted), false)
                                       .create();
        var contactMessage5 = ContactMessage.builder()
                                            .id(UUID.fromString("d05f9447-c9ce-47fa-8a54-655cea9ba324"))
                                            .type("EMAIL")
                                            .value("4343@mail.ru")
                                            .using("DEFAULT")
                                            .employeeId(null)
                                            .deleted(true)
                                            .build();
        var organizationContact1 = new OrganizationContact(new OrganizationContactKey(organization1.getId(), contactMessage1.getId()));
        var organizationContact2 = new OrganizationContact(new OrganizationContactKey(organization1.getId(), contactMessage2.getId()));
        var organizationContact3 = new OrganizationContact(new OrganizationContactKey(organization1.getId(), contactMessage3.getId()));
        var organizationContact4 = new OrganizationContact(new OrganizationContactKey(organization3.getId(), contactMessage4.getId()));
        var organizationContact5 = new OrganizationContact(new OrganizationContactKey(organization3.getId(),
                                                                                      UUID.fromString("d05f9447-c9ce-47fa-8a54-655cea9ba324")));
        var contact1 = new Contact(contactMessage1.getId(), ContactType.valueOf(contactMessage1.getType()), contactMessage1.getValue());
        var contact2 = new Contact(contactMessage2.getId(), ContactType.valueOf(contactMessage2.getType()), contactMessage2.getValue());
        var contact3 = new Contact(contactMessage3.getId(), ContactType.valueOf(contactMessage3.getType()), contactMessage3.getValue());
        var contact4 = new Contact(contactMessage4.getId(), ContactType.valueOf(contactMessage4.getType()), contactMessage4.getValue());
        var contact5 = new Contact(UUID.fromString("bdc1789e-7b12-47b5-8553-077406f0ad3c"), ContactType.PHONE, "+79163313365");
        var contact6 = new Contact(UUID.fromString("d05f9447-c9ce-47fa-8a54-655cea9ba324"), ContactType.EMAIL, "4343@mail.ru");
        produceMessage("service.organization",
                       new OrganizationMessage(organization1.getId(),
                                               organization1.getDigitId(),
                                               organization1.getOfficialName(),
                                               null,
                                               organization1.getMsrn(),
                                               organization1.getTin(),
                                               null,
                                               Stream.of(contactMessage1, contactMessage2, contactMessage3)
                                                     .collect(Collectors.toCollection(ArrayList::new)),
                                               false,
                                               new OrganizationMessage.OrganizationGroup(organizationGroup.getId(),
                                                                                         organizationGroup.getName(),
                                                                                         organizationGroup.isInternal())
                       ));
        produceMessage("service.organization",
                       new OrganizationMessage(organization2.getId(),
                                               organization2.getDigitId(),
                                               organization2.getOfficialName(),
                                               null,
                                               organization2.getMsrn(),
                                               organization2.getTin(),
                                               null,
                                               Collections.emptyList(),
                                               false,
                                               null));
        produceMessage("service.organization",
                       new OrganizationMessage(organization3.getId(),
                                               organization3.getDigitId(),
                                               organization3.getOfficialName(),
                                               null,
                                               organization3.getMsrn(),
                                               organization3.getTin(),
                                               null,
                                               Stream.of(contactMessage4, contactMessage5)
                                                     .collect(Collectors.toCollection(ArrayList::new)),
                                               false,
                                               null));
        var actualOrganizations = organizationRepository.findAll();
        var actualOrganizationGroups = organizationGroupRepository.findAll();
        var actualOrganizationContacts = organizationContactRepository.findAll();
        var actualContacts = contactRepository.findAll();
        assertThat(actualOrganizations)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields("contacts")
                .isEqualTo(List.of(organization1, organization2, organization3, organization4, organization5));
        assertThat(actualOrganizationGroups)
                .usingRecursiveComparison()
                .isEqualTo(Collections.singletonList(organizationGroup));
        assertThat(actualOrganizationContacts)
                  .containsExactlyInAnyOrder(organizationContact1,
                                             organizationContact2,
                                             organizationContact3,
                                             organizationContact4,
                                             organizationContact5);
        assertThat(actualContacts)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(contact1, contact2, contact3, contact4, contact5, contact6));
    }
}