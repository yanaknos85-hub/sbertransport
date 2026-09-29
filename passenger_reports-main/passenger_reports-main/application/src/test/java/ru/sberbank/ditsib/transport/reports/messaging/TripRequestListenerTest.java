package ru.sberbank.ditsib.transport.reports.messaging;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.PaymentTypeCode;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.reports.dao.*;
import ru.sberbank.ditsib.transport.reports.enums.InboxMessageClassNameEnum;
import ru.sberbank.ditsib.transport.reports.enums.InboxMessageStatusEnum;
import ru.sberbank.ditsib.transport.reports.mappers.RequestMapper;
import ru.sberbank.ditsib.transport.reports.model.ExpectedData;
import ru.sberbank.ditsib.transport.reports.model.InboxMessage;
import ru.sberbank.ditsib.transport.reports.model.PaymentData;
import ru.sberbank.ditsib.transport.reports.model.Request;

import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({ "OptionalGetWithoutIsPresent" })
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@DisplayName("Проверка получения заявок")
@Transactional
@MockitoBean(types = JwtDecoder.class)
class TripRequestListenerTest extends SharedTest {
    
    @Autowired
    private Consumer<Message<RequestMessage>> requestInput;
    @Autowired
    private RequestMapper mapper;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private CarsharingTariffRepository carsharingTariffRepository;
    @Autowired
    private TaxiTariffRepository taxiTariffRepository;
    @Autowired
    private PublicTariffRepository publicTariffRepository;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private ContractorRepository contractorRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private OrderKpiRepository orderKpiRepository;
    @Autowired
    private SharedRideRepository magentaSharedRideRepository;
    @Autowired
    private TransportCompensationRepository transportCompensationRepository;
    @Autowired
    private SharedRequestKpiRepository sharedRequestKpiRepository;
    @Autowired
    private SharedRideRepository sharedRideRepository;
    @Autowired
    private PersonalTariffRepository personalTariffRepository;
    @Autowired
    private InboxMessageRepository inboxMessageRepository;
    
    @SneakyThrows
    @BeforeEach
    public void setUp() {
        organizationRepository.save(organization1);
        organizationRepository.save(organization2);
        organizationRepository.save(organization3);
        organizationRepository.save(organization5);
        magentaSharedRideRepository.save(sharedRide1);
        
        contractorRepository.save(contractor1);
        contractorRepository.save(contractor2);
        contractorRepository.save(carsharingContractor);

        contractRepository.save(contract1);
        contractRepository.save(contract2);
        contractRepository.save(contract3);

        taxiTariffRepository.save(taxiTariff1);
        publicTariffRepository.save(publicTariff);
        carsharingTariffRepository.save(carsharingTariff1);
        personalTariffRepository.save(personalTariff);
        
    }

    @Test
    @WithMockUser(username=USER1_ID_STR, roles=ROLE_STR)
    @DisplayName("Получение заявки с компенсацией личного транспорта")
    @Disabled("Требуется актуализация")
    void handlePersonalRequestMessageTest(){
        request1.setStatus(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name());
        RequestMessage requestMessage = mapper.toMessage(request1);
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        Request request = requestRepository.findById(requestMessage.getId()).get();
        assertEquals(170000, request.getExpected().getCost());

        PaymentData paymentData = request.getPaymentData();
        assertNotNull(paymentData);
        assertEquals(PaymentTypeCode.CODE_4661.getCode(), paymentData.getPaymentTypeCodeMain().getCode());
        assertEquals(120000, paymentData.getPaymentPriceMain());
        assertEquals(PaymentTypeCode.CODE_4665.getCode(), paymentData.getPaymentTypeCodeOptional().getCode());
        assertEquals(50000, paymentData.getPaymentPriceOptional());

        assertEquals(request.getAuthor().getId(), requestMessage.getAuthorId());
        assertEquals(request.getPassenger().getId(), requestMessage.getPassengerId());
        assertEquals(request.getPassengerCount(), requestMessage.getPassengerCount());
        assertEquals(request.isCoopTrip(), requestMessage.isCoopTrip());
        assertEquals(request.getCommentForDriver(), requestMessage.getCommentForDriver());
        assertEquals(request.getWaypoints().get(0).getAddress().getCountry(), address1.getCountry() );
        assertEquals(request.getWaypoints().get(request.getWaypoints().size()-1).getAddress().getCountry(),
                address2.getCountry() );
        assertEquals(request.getId(), requestMessage.getId());
        assertEquals(request.getPurpose().getId(), requestMessage.getPurposeId());
        assertEquals(request.getStatus(), requestMessage.getStatus() );
        assertEquals(request.getHumanReadableId(), requestMessage.getHumanReadableId() );
    }
    
    @Test
    @WithMockUser(username=USER1_ID_STR, roles=ROLE_STR)
    @DisplayName("Получение заявки с компенсацией личного транспорта. MessageId передан")
    void handlePersonalRequestMessageTestWithMessageId(){
        request1.setStatus(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name());
        RequestMessage requestMessage = mapper.toMessage(request1);
        requestInput.accept(MessageBuilder.createMessage(requestMessage, new MessageHeaders(Map.of("messageId", UUID.randomUUID()))));
        List<InboxMessage> results = inboxMessageRepository.findAllByStatus(InboxMessageStatusEnum.NEW.name());
        
        assertEquals(1, results.size());
        assertEquals(requestMessage.getId(),results.get(0).getEntityId());
        assertNotNull(results.get(0).getReceivedAt());
        assertNull(results.get(0).getUpdatedAt());
        assertEquals(InboxMessageClassNameEnum.REQUEST_MESSAGE.getValue(), results.get(0).getClassName());
    }
    
    @Test
    @WithMockUser(username=USER1_ID_STR, roles=ROLE_STR)
    @DisplayName("Пересчет выплат в разрезе кодов при получении заявки с компенсацией личного транспорта")
    @Disabled("Требуется актуализация")
    void handlePersonalRequestMessageTestithRecalcCode(){
        request1.setStatus(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name());
        request1.getWaypoints().clear();
        request1.setExpected(ExpectedData.builder().cost(50000.0).distance(1000.0).time(Duration.of(10, ChronoUnit.MINUTES)).build());
        RequestMessage requestMessage1 = mapper.toMessage(request1);
        requestInput.accept(MessageBuilder.withPayload(requestMessage1).build());
    
        request1.setExpected(ExpectedData.builder().cost(80000.0).distance(1000.0).time(Duration.of(10, ChronoUnit.MINUTES)).build());
        request1.setId(UUID.randomUUID());
        request1.setHumanReadableId("ЛT-0001-2");
        RequestMessage requestMessage2 = mapper.toMessage(request1);
        requestInput.accept(MessageBuilder.withPayload(requestMessage2).build());
    
        request1.setStatus(TripRequestStatus.PERSONAL_CANCELLED.name());
        request1.setExpected(ExpectedData.builder().cost(80000.0).distance(1000.0).time(Duration.of(10, ChronoUnit.MINUTES)).build());
        RequestMessage requestMessage3 = mapper.toMessage(request1);
        requestInput.accept(MessageBuilder.withPayload(requestMessage3).build());
    
        request1.setStatus(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name());
        request1.setId(UUID.randomUUID());
        request1.setHumanReadableId("ЛT-0001-3");
        request1.setExpected(ExpectedData.builder().cost(170000.0).distance(1000.0).time(Duration.of(10, ChronoUnit.MINUTES)).build());
        RequestMessage requestMessage4 = mapper.toMessage(request1);
        requestInput.accept(MessageBuilder.withPayload(requestMessage4).build());
    
        request1.setStatus(TripRequestStatus.PERSONAL_PAYMENT_AWAITING.name());
        RequestMessage requestMessage = mapper.toMessage(request1);
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        
        Request request = requestRepository.findById(requestMessage.getId()).get();
        assertEquals(170000, request.getExpected().getCost());
        
        PaymentData paymentData = request.getPaymentData();
        assertNotNull(paymentData);
        assertEquals(paymentData.getPaymentTypeCodeMain().getCode(), PaymentTypeCode.CODE_4661.getCode());
        assertEquals(81000, paymentData.getPaymentPriceMain());
        assertEquals(PaymentTypeCode.CODE_4665.getCode(), paymentData.getPaymentTypeCodeOptional().getCode());
        assertEquals(89000, paymentData.getPaymentPriceOptional());
    }

    @Test
    @WithMockUser(username=USER1_ID_STR, roles=ROLE_STR)
    @DisplayName("Получение заявки с компенсацией общественного транспорта")
    void handlePublicRequestMessageTest(){
        sharedRideKPI1 = sharedRequestKpiRepository.save(sharedRideKPI1);
        orderKpi1.setKpiId(sharedRideKPI1.getId());
        orderKpi1 = orderKpiRepository.save(orderKpi1);
        sharedRideKPI1.getOrdersKpi().add(orderKpi1);
        sharedRideKPI1 = sharedRequestKpiRepository.save(sharedRideKPI1);
        sharedRide1.setKpi(sharedRideKPI1);
        sharedRideRepository.save(sharedRide1);
        request2.setStatus("PUBLIC_AWAITING_APPROVAL");
        request2.setSharedRide(sharedRide1);
        
        RequestMessage requestMessage = mapper.toMessage(request2);
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        requestRepository.save(request2);
        
        Request request = requestRepository.findById(requestMessage.getId()).get();

        assertEquals(request.getAuthor().getId(), requestMessage.getAuthorId());
        assertEquals(request.getPassenger().getId(), requestMessage.getPassengerId());
        assertEquals(request.getTariff().getId(),requestMessage.getTariffId());
        assertNull(requestMessage.getApprovalId());
        assertEquals(request.getPassengerCount(), requestMessage.getPassengerCount());
        assertEquals(request.isCoopTrip(), requestMessage.isCoopTrip());
        assertEquals(request.getCommentForDriver(), requestMessage.getCommentForDriver());
        assertEquals(request.getWaypoints().get(0).getAddress().getCountry(), address1.getCountry() );
        assertEquals(request.getWaypoints().get(request.getWaypoints().size()-1).getAddress().getCountry(),
                address2.getCountry() );
        assertEquals(request.getId(), requestMessage.getId());
        assertEquals(request.getPurpose().getId(), requestMessage.getPurposeId());
        assertEquals(request.getStatus(), requestMessage.getStatus() );
        assertEquals(request.getHumanReadableId(), requestMessage.getHumanReadableId() );
        assertNull(request.getRideId());
    }

    @Test
    @DisplayName("Получение заявки с совместной поездкой")
    void handleRequestMessageTest(){
        sharedRideKPI1 = sharedRequestKpiRepository.save(sharedRideKPI1);
        orderKpi1.setKpiId(sharedRideKPI1.getId());
        orderKpi1 = orderKpiRepository.save(orderKpi1);
        sharedRideKPI1.getOrdersKpi().add(orderKpi1);
        sharedRideKPI1 = sharedRequestKpiRepository.save(sharedRideKPI1);
        sharedRide1.setKpi(sharedRideKPI1);
        sharedRideRepository.save(sharedRide1);
    
        request4.setSharedRide(sharedRide1);
        request4.setApprovedBy(null);
        
        RequestMessage requestMessage = mapper.toMessage(request4);
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        
        Request request = requestRepository.findById(requestMessage.getId()).get();
        
        assertEquals(request.getAuthor().getId(), requestMessage.getAuthorId());
        assertEquals(request.getPassenger().getId(), requestMessage.getPassengerId());
        assertEquals(request.getTariff().getId(),requestMessage.getTariffId());
        assertNull(requestMessage.getApprovalId());
        assertEquals(request.getPassengerCount(), requestMessage.getPassengerCount());
        assertEquals(request.isCoopTrip(), requestMessage.isCoopTrip());
        assertEquals(request.getCommentForDriver(), requestMessage.getCommentForDriver());
        assertEquals(request.getWaypoints().get(0).getAddress().getCountry(), address1.getCountry() );
        assertEquals(request.getWaypoints().get(request.getWaypoints().size()-1).getAddress().getCountry(),
                     address2.getCountry() );
        assertEquals(request.getId(), requestMessage.getId());
        assertEquals(request.getPurpose().getId(), requestMessage.getPurposeId());
        assertEquals(request.getStatus(), requestMessage.getStatus() );
        assertEquals(request.getHumanReadableId(), requestMessage.getHumanReadableId() );
        //SRMTODO: check test
        //assertEquals(request.getMagentaOrderId(), requestMessage.getMagentaOrderId());
        //assertEquals(request.getMagentaSharedRequest().getMagentaId(), requestMessage.getMagentaSharedRequest().getMagentaId());
        //assertEquals(request.getMagentaSharedRequest().getKpi().getId(), requestMessage.getMagentaSharedRequest().getKpi().getId());
        //assertEquals(request.getMagentaSharedRequest().getKpi().getOrdersKpi().iterator().next().getId(),
                      //requestMessage.getMagentaSharedRequest().getKpi().getOrdersKpi().get(0).getId());
    }
    
    @Test
    @DisplayName("Обновление заявки с совместной поездкой")
    void handleSharedRequestMessageTest(){
        sharedRideKPI1 = sharedRequestKpiRepository.save(sharedRideKPI1);
        orderKpi1.setKpiId(sharedRideKPI1.getId());
        sharedRideKPI1.getOrdersKpi().add(orderKpi1);
        sharedRide1.setKpi(sharedRideKPI1);
        request4.setSharedRide(sharedRide1);
        RequestMessage requestMessage = mapper.toMessage(request4);
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        request4.getSharedRide().getKpi().getOrdersKpi().clear();
        orderKpi1.setId(UUID.randomUUID());
        orderKpi1.setSavings(5000.0);
        request4.getSharedRide().getKpi().getOrdersKpi().add(orderKpi1);
        requestMessage = mapper.toMessage(request4);
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        
        Request request = requestRepository.findById(requestMessage.getId()).get();
        assertEquals(request.getAuthor().getId(), requestMessage.getAuthorId());
        assertEquals(request.getPassenger().getId(), requestMessage.getPassengerId());
        assertEquals(request.getTariff().getId(), requestMessage.getTariffId());
        assertEquals(request.getApprovedBy().getId(), requestMessage.getApprovalId());
        assertEquals(request.getPassengerCount(), requestMessage.getPassengerCount());
        assertEquals(request.isCoopTrip(), requestMessage.isCoopTrip());
        assertEquals(request.getCommentForDriver(), requestMessage.getCommentForDriver());
        assertEquals(request.getWaypoints().get(0).getAddress().getCountry(), address1.getCountry() );
        assertEquals(request.getWaypoints().get(request.getWaypoints().size()-1).getAddress().getCountry(),
                     address2.getCountry() );
        assertEquals(request.getId(), requestMessage.getId());
        assertEquals(request.getPurpose().getId(), requestMessage.getPurposeId());
        assertEquals(request.getStatus(), requestMessage.getStatus() );
        assertEquals(request.getHumanReadableId(), requestMessage.getHumanReadableId() );
        //SRMTODO: check test
        //assertEquals(request.getMagentaOrderId(), requestMessage.getMagentaOrderId());
        //assertEquals(request.getMagentaSharedRequest().getMagentaId(), requestMessage.getMagentaSharedRequest().getMagentaId());
        //assertEquals(request.getMagentaSharedRequest().getKpi().getId(), requestMessage.getMagentaSharedRequest().getKpi().getId());
        //var actualOrderKpi = request.getMagentaSharedRequest().getKpi().getOrdersKpi().iterator().next();
        //assertEquals(actualOrderKpi.getId(), orderKpi1.getId());
        //assertEquals(actualOrderKpi.getSavings(), orderKpi1.getSavings());
    }
    
    @Test
    @DisplayName("Получение заявки по каршерингу")
    void handleCarsharingRequestMessageTest(){
        RequestMessage requestMessage = mapper.toMessage(request5);
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        Request request = requestRepository.findById(requestMessage.getId()).get();
        assertEquals(request.getAuthor().getId(), requestMessage.getAuthorId());
        assertEquals(request.getPassenger().getId(), requestMessage.getPassengerId());
        assertEquals(request.getTariff().getId(), requestMessage.getTariffId());
        assertEquals(request.getApprovedBy().getId(), requestMessage.getApprovalId());
        assertEquals(request.getPassengerCount(), requestMessage.getPassengerCount());
        assertEquals(request.isCoopTrip(), requestMessage.isCoopTrip());
        assertEquals(request.getWaypoints().get(0).getAddress().getCountry(), address1.getCountry() );
        assertEquals(request.getWaypoints().get(request.getWaypoints().size()-1).getAddress().getCountry(),
                     address2.getCountry() );
        assertEquals(request.getId(), requestMessage.getId());
        assertEquals(request.getPurpose().getId(), requestMessage.getPurposeId());
        assertEquals(request.getStatus(), requestMessage.getStatus() );
        assertEquals(request.getHumanReadableId(), requestMessage.getHumanReadableId() );
        assertEquals(request.getCarsharingClass(), requestMessage.getCarsharingClass() );
        assertEquals(request.getContractor().getId(), requestMessage.getContractorId() );
    }
}