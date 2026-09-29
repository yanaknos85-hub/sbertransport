package ru.sberbank.ditsib.transport.audit;

import io.qameta.allure.Feature;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.dao.publicTransport.CompensationDocumentRepository;
import ru.sberbank.ditsib.transport.request.database.dao.publicTransport.CustomRevisionRepository;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.corp.Position;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CustomRevision;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.UploadFileFormats;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@SpringBootTest(classes= RequestApplication.class)
@DisplayName("Тест аудита таблицы request_for_public (схема audit)")
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
class PublicTransportCompensationRequestAuditTest extends KafkaTest {
    
    private static final String USER_ID = "9efe9571-4da4-4151-b8a4-1acc9a7e69cb";
    
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AddressRepository addressRepo;
    @Autowired
    private TripPurposeRepository tripPurposeRepo;
    @Autowired
    private CompensationDocumentRepository compensationDocumentRepository;
    @Autowired
    private CustomRevisionRepository revisionRepository;
    @Autowired
    private RequestForPublicRepository requestForPublicRepository;
    @Autowired
    private WaypointRepository waypointRepository;
    
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    RequestForPublic cityTransportRequest;
    RequestForPublic suburbTransportRequest;
    RequestForPublic travelCardRequest;

    @BeforeEach
    @WithMockUser
    void init() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").jti(USER_ID).header("algo", "none").build()));
        
        compensationDocumentRepository.deleteAll();
        requestForPublicRepository.clearPublic();
        revisionRepository.clearPublic();
        revisionRepository.deleteAll();
        revisionRepository.flush();
        waypointRepository.deleteAll();
        addressRepo.deleteAll();
        tripPurposeRepo.deleteAll();
        employeeRepository.deleteAll();
        Organization organization =
                organizationRepository.save(Organization.builder()
                        .digitId(1L)
                        .id(UUID.randomUUID())
                        .build());
        
        Department department = departmentRepository.save(
                Department.builder()
                          .departmentName("Отдел")
                          .humanReadableId("ОТД-1")
                          .id(UUID.randomUUID())
                          .location("Samara").build()
                                                         );
    
        Position position = positionRepository.save(
                Position.builder()
                        .availableClasses(Set.of(TaxiClass.ECONOMY, TaxiClass.COMFORT))
                        .id(UUID.randomUUID())
                        .organizationId(organization.getId())
                        .positionName("должность")
                        .selfApproved(false)
                        .build());
        
        Employee employee1 = employeeRepository.save(
                Employee.builder().firstName("Имя1").lastName("Фамилия1").id(UUID.randomUUID())
                        .humanReadableId("ЧЕЛ-1").department(department).personnelNumber("№1")
                        .mobilePhone("889").positionId(position.getId()).userId(UUID.randomUUID()).build()
                                                    );
    
        Employee employee2 = employeeRepository.save(
                Employee.builder().firstName("Имя2").lastName("Фамилия2").id(UUID.randomUUID())
                        .humanReadableId("ЧЕЛ-2").department(department).personnelNumber("№2")
                        .mobilePhone("8810").positionId(position.getId()).userId(UUID.randomUUID()).build()
                                                    );
        
        TripPurpose tripPurpose = tripPurposeRepo.save(TripPurpose.builder()
                                                                  .id(UUID.randomUUID()).organization(UUID.randomUUID())
                                                                  .purpose("Цель поездки").build());
        
        Address address1 = addressRepo.save(Address.builder().latitude(53.1955).longitude(50.1018).country("РФ")
                                                   .region("Самарская обл").city("Самара").street("ул Стара-Загора")
                                                   .house("183").build());
        Address address2 = addressRepo.save(Address.builder().latitude(53.2000).longitude(50.2000).country("РФ")
                                                   .region("Самарская обл").city("Самара").street("ул Ново-Садовая")
                                                   .house("1").build());
        Address address3 = addressRepo.save(Address.builder().latitude(59.9386).longitude(30.3141).country("РФ")
                                                   .region("Ленинградская обл").city("Ленинград")
                                                   .street("3-я ул Строителей").house("3").build());
        Address address4 = addressRepo.save(Address.builder().latitude(55.7522).longitude(37.6155).country("РФ")
                                                   .region("Московская обл").city("Москва").street("Кремль")
                                                   .house("1").build());
    
        final UUID folder2 = UUID.randomUUID();
        final UUID folder3 = UUID.randomUUID();
        
        CompensationDocument doc1 = compensationDocumentRepository.save(
                CompensationDocument.builder().fileName("File1.jpeg")
                                    .folder(folder2).creationTime(LocalDateTime.now())
                                    .fileSize(10*1024*1024).fileFormat(UploadFileFormats.JPEG).build()
        );
        CompensationDocument doc2 = compensationDocumentRepository.save(
                CompensationDocument.builder().fileName("File2.pdf")
                                    .folder(folder2).creationTime(LocalDateTime.now())
                                    .fileSize(1024 * 1024).fileFormat(UploadFileFormats.PDF).build()
        );
        CompensationDocument doc3 = compensationDocumentRepository.save(
                CompensationDocument.builder().fileName("File3.docx")
                                    .folder(folder3).creationTime(LocalDateTime.now())
                                    .fileSize(1024).fileFormat(UploadFileFormats.DOCX).build()
        );
        CompensationDocument doc4 = compensationDocumentRepository.save(
                CompensationDocument.builder().fileName("File4.heic")
                                    .folder(folder3).creationTime(LocalDateTime.now())
                                    .fileSize(20*1024*1024).fileFormat(UploadFileFormats.HEIC).build()
        );
                
        cityTransportRequest =
                RequestForPublic.builder()
                                .status(TripRequestStatus.PUBLIC_AWAITING_APPROVAL)
                                .transportType(TransportTypeEnum.PUBLIC)
                                .tariffId(UUID.randomUUID())
                                .expected(
                                        ExpectedData.builder().cost(100.0).distance(0.0).time(Duration.ZERO).build()
                                         ).creationTime(LocalDateTime.now()).humanReadableId("ПР-00001")
                                .purpose(tripPurpose).author(employee1).passenger(employee2)
                                .desiredDate(LocalDateTime.now()).build();
        
        suburbTransportRequest =
                RequestForPublic.builder()
                                .status(TripRequestStatus.PUBLIC_AWAITING_APPROVAL)
                                .transportType(TransportTypeEnum.PUBLIC)
                                .tariffId(UUID.randomUUID())
                                .compensationDocuments(List.of(doc1, doc2))
                                .expected(
                                        ExpectedData.builder().cost(200.50).distance(10.0).time(Duration.ZERO).build()
                                         ).creationTime(LocalDateTime.now()).humanReadableId("ПР-00002")
                                .purpose(tripPurpose).author(employee1).passenger(employee1)
                                .desiredDate(LocalDateTime.now()).build();
        
        travelCardRequest =
                RequestForPublic.builder()
                                .status(TripRequestStatus.PUBLIC_AWAITING_APPROVAL)
                                .transportType(TransportTypeEnum.PUBLIC)
                                .tariffId(UUID.randomUUID())
                                .compensationDocuments(List.of(doc3, doc4))
                                .expected(
                                        ExpectedData.builder().cost(379.99).distance(0.0).time(Duration.ZERO).build()
                                         ).creationTime(LocalDateTime.now()).humanReadableId("ПР-00003")
                                .purpose(tripPurpose).author(employee2).passenger(employee2)
                                .desiredDate(LocalDateTime.now()).build();
        
        Waypoint waypoint1 = Waypoint.builder().address(address1).request(cityTransportRequest)
                                     .waitTime(Duration.ofMinutes(1L)).build();
        Waypoint waypoint2 = Waypoint.builder().address(address2).request(cityTransportRequest)
                                     .waitTime(Duration.ofMinutes(2L)).build();
        Waypoint waypoint3 = Waypoint.builder().address(address3).request(suburbTransportRequest)
                                     .waitTime(Duration.ofMinutes(3L)).build();
        Waypoint waypoint4 = Waypoint.builder().address(address4).request(suburbTransportRequest)
                                     .waitTime(Duration.ofMinutes(4L)).build();
        Waypoint waypoint5 = Waypoint.builder().address(address2).request(travelCardRequest)
                                     .waitTime(Duration.ofMinutes(5L)).build();
        Waypoint waypoint6 = Waypoint.builder().address(address4).request(travelCardRequest)
                                     .waitTime(Duration.ofMinutes(6L)).build();
        
        cityTransportRequest.getWaypoints().addAll(List.of(waypoint1, waypoint2));
        suburbTransportRequest.getWaypoints().addAll(List.of(waypoint3, waypoint4));
        travelCardRequest.getWaypoints().addAll(List.of(waypoint5, waypoint6));
        requestForPublicRepository.saveAll(Set.of(cityTransportRequest, suburbTransportRequest, travelCardRequest));
    }
    
    @AfterEach
    @WithMockUser
    void clear() {
        compensationDocumentRepository.deleteAll();
        requestForPublicRepository.clearPublic();
        revisionRepository.clearPublic();
        revisionRepository.deleteAll();
        revisionRepository.flush();
        waypointRepository.deleteAll();
        addressRepo.deleteAll();
        tripPurposeRepo.deleteAll();
        employeeRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Удаление request'ов для всех типов компенсации")
    void delete() {
        final UUID cityRequestId =
                requestForPublicRepository.findAllByHumanReadableId(cityTransportRequest.getHumanReadableId()).get(0).getId();
        
        compensationDocumentRepository.deleteAll();
        List<RequestForPublic> all = requestForPublicRepository.findAll();
        requestForPublicRepository.deleteAll(all);
    
        assertEquals(0, compensationDocumentRepository.count());
        assertEquals(0, requestForPublicRepository.count());
        assertEquals(2, revisionRepository.count());
        List<CustomRevision> revisions =
                revisionRepository.findAll().stream().sorted(Comparator.comparing(CustomRevision::getRev)).toList();
        //проверим revisionRepository
        assertEquals(USER_ID, revisions.get(0).getUserId().toString());
        
        assertEquals(USER_ID, revisions.get(1).getUserId().toString());
    
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        var cityActual = AuditReaderFactory.get(entityManager)
                                            .createQuery().forRevisionsOfEntity(RequestForPublic.class, false, true)
                                            .add(AuditEntity.property("id").eq(cityRequestId))
                                            .getResultList();
    
        assertEquals(2, cityActual.size());
        Object[] cityAuditDelete = (Object[]) cityActual.get(1);
        assertInstanceOf(RevisionType.class, ((Object[]) cityAuditDelete)[2]);
        assertEquals(RevisionType.DEL, ((Object[]) cityAuditDelete)[2]);
        
        Object[] cityAuditAdd = (Object[]) cityActual.get(0);
        assertInstanceOf(RevisionType.class, ((Object[]) cityAuditAdd)[2]);
        assertEquals(RevisionType.ADD, ((Object[]) cityAuditAdd)[2]);
        assertInstanceOf(RequestForPublic.class, ((Object[]) cityAuditAdd)[0]);
        RequestForPublic cityRequest = (RequestForPublic) ((Object[]) cityAuditAdd)[0];
        assertEquals(cityTransportRequest.getHumanReadableId(), cityRequest.getHumanReadableId());
        assertEquals(cityRequestId, cityRequest.getId());
    }
    
    @Test
    @DisplayName("Изменение request'ов для всех типов компенсации")
    @WithMockUser(username = USER_ID)
    void modify() {
        RequestForPublic cityRequest =
                requestForPublicRepository.findAllByHumanReadableId(cityTransportRequest.getHumanReadableId()).get(0);
        cityRequest.setExpected(ExpectedData.builder().time(Duration.ZERO).distance(0.0).cost(200.0).build());
        requestForPublicRepository.saveAndFlush(cityRequest);
        
        RequestForPublic suburbRequest =
                requestForPublicRepository.findAllByHumanReadableId(suburbTransportRequest.getHumanReadableId()).get(0);
        suburbRequest.setStatus(TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE);
        requestForPublicRepository.saveAndFlush(suburbRequest);
        
        RequestForPublic travelRequest =
                requestForPublicRepository.findAllByHumanReadableId(travelCardRequest.getHumanReadableId()).get(0);
        travelRequest.setStatus(TripRequestStatus.PUBLIC_TRIP_CONFIRMATION);
        travelRequest.setExpected(ExpectedData.builder().time(Duration.ZERO).distance(0.0).cost(530.50).build());
        requestForPublicRepository.saveAndFlush(travelRequest);
        
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        var cityActual = AuditReaderFactory.get(entityManager)
                                            .createQuery().forRevisionsOfEntity(RequestForPublic.class, false, false)
                                            .add(AuditEntity.property("humanReadableId")
                                                            .eq(cityTransportRequest.getHumanReadableId()))
                                            .getResultList();
    
        assertEquals(2, cityActual.size());
        Object[] cityAuditMod = (Object[]) cityActual.get(1);
        assertInstanceOf(RevisionType.class, ((Object[]) cityAuditMod)[2]);
        assertEquals(RevisionType.MOD, ((Object[]) cityAuditMod)[2]);

        assertInstanceOf(RequestForPublic.class, ((Object[]) cityActual.get(1))[0]);
        RequestForPublic cityActualElement = (RequestForPublic) ((Object[]) cityActual.get(1))[0];
        assertEquals(cityTransportRequest.getHumanReadableId(), cityRequest.getHumanReadableId());
        assertEquals(cityActualElement.getExpected().getCost(), cityRequest.getExpected().getCost());
        
        var suburbActual = AuditReaderFactory.get(entityManager)
                                              .createQuery().forRevisionsOfEntity(RequestForPublic.class, false, false)
                                              .add(AuditEntity.property("humanReadableId")
                                                              .eq(suburbTransportRequest.getHumanReadableId()))
                                              .getResultList();
    
        assertEquals(2,suburbActual.size());
        Object[] suburbAuditMod = (Object[]) cityActual.get(1);
        assertInstanceOf(RevisionType.class, ((Object[]) suburbAuditMod)[2]);
        assertEquals(RevisionType.MOD, ((Object[]) suburbAuditMod)[2]);

        assertInstanceOf(RequestForPublic.class, ((Object[]) suburbActual.get(1))[0]);
        RequestForPublic suburbActualElement = (RequestForPublic) ((Object[]) suburbActual.get(1))[0];
        assertEquals(suburbTransportRequest.getHumanReadableId(), suburbRequest.getHumanReadableId());
        assertEquals(suburbActualElement.getStatus(), suburbRequest.getStatus());
        
        var cardActual = AuditReaderFactory.get(entityManager)
                                            .createQuery().forRevisionsOfEntity(RequestForPublic.class, false, false)
                                            .add(AuditEntity.property("humanReadableId")
                                                            .eq(travelCardRequest.getHumanReadableId()))
                                            .getResultList();
    
        assertEquals(2, suburbActual.size());
        Object[] travelCardAuditMod = (Object[]) cityActual.get(1);
        assertInstanceOf(RevisionType.class, ((Object[]) travelCardAuditMod)[2]);
        assertEquals(RevisionType.MOD, ((Object[]) travelCardAuditMod)[2]);

        assertInstanceOf(RequestForPublic.class, ((Object[]) cardActual.get(1))[0]);
        RequestForPublic cardActualElement = (RequestForPublic) ((Object[]) cardActual.get(1))[0];
        assertEquals(travelCardRequest.getHumanReadableId(), travelRequest.getHumanReadableId());
        assertEquals(cardActualElement.getExpected().getCost(), travelRequest.getExpected().getCost());
        assertEquals(cardActualElement.getStatus(), travelRequest.getStatus());
    }
}
