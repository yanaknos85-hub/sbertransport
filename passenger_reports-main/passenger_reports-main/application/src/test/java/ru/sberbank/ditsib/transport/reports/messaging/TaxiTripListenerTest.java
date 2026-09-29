package ru.sberbank.ditsib.transport.reports.messaging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TripType;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.messaging.messages.trip.TaxiTripMessage;
import ru.sberbank.ditsib.transport.reports.dao.*;
import ru.sberbank.ditsib.transport.reports.mappers.TaxiTripMapper;
import ru.sberbank.ditsib.transport.reports.model.Organization;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip;
import ru.sberbank.ditsib.transport.reports.service.TaxiTripService;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({ "OptionalGetWithoutIsPresent" })
@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка получения поездок на такси")
@MockitoBean(types = JwtDecoder.class)
class TaxiTripListenerTest extends SharedTest {
    @Autowired
    private Consumer<Message<TaxiTripMessage>> taxiTripInput;
    @Autowired
    private TaxiTripRepository taxiTripRepository;
    @Autowired
    private TaxiTripMapper mapper;
    @Autowired
    private TaxiTripService taxiTripService;
    @Autowired
    private TaxiTariffRepository taxiTariffRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    @Autowired
    private SharedRideRepository sharedRideRepository;
    @Autowired
    private AddressRepository addressRepository;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm", new Locale("ru"));
    
    @AfterEach
    void dropRepository() {
        taxiTripRepository.deleteAll();
        requestRepository.deleteAll();
        taxiTariffRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Получение совместной поездки на такси")
    void handleCoopTaxiTripMessageTest() {
        coopTaxiTrip1 = taxiTripRepository.save(coopTaxiTrip1);
        var taxiTripMessage = TaxiTripMessage.builder()
                                                         .id(coopTaxiTrip1.getId())
                                                         .sharedRideId(coopTaxiTrip1.getSharedRide().getId())
                                                         .organizationId(coopTaxiTrip1.getOrganizationId())
                                                         .tripType(TripType.COOP.name())
                                                         .status(InboundTaxiTripStatus.SENT_TO_CONTRACTOR.name())
                                                         .tariffId(coopTaxiTrip1.getTariff().getId())
                                                         .build();
        taxiTripInput.accept(MessageBuilder.withPayload(taxiTripMessage).build());
        CoopTaxiTrip coopTaxiTrip = taxiTripService.findCoopTripById(taxiTripMessage.getId()).get();
        assertTrip(taxiTripMessage, coopTaxiTrip);
        
        taxiTripMessage = mapper.toMessage(coopTaxiTrip1);
        taxiTripInput.accept(MessageBuilder.withPayload(taxiTripMessage).build());
        coopTaxiTrip = taxiTripService.findCoopTripById(taxiTripMessage.getId()).get();
        
        assertTrip(taxiTripMessage, coopTaxiTrip);
    }
    
    @Test
    @DisplayName("Получение индивидуальной поездки на такси")
    void handleSingleTaxiTripMessageTest() {
        organizationRepository.save(Organization.builder().id(singleTaxiTrip1.getRequest().getPassenger().getDepartment().getOrganizationId())
                                                .officialName("test organization").build());
        departmentRepository.save(singleTaxiTrip1.getRequest().getPassenger().getDepartment());
        positionRepository.save(singleTaxiTrip1.getRequest().getPassenger().getPosition());
        employeeRepository.save(singleTaxiTrip1.getRequest().getPassenger());
        tripPurposeRepository.save(singleTaxiTrip1.getRequest().getPurpose());
        addressRepository.saveAll(addresses);
        taxiTariffRepository.save(singleTaxiTrip1.getTariff());
        requestRepository.save(singleTaxiTrip1.getRequest());
        var taxiTripMessage = TaxiTripMessage.builder()
                                                         .id(singleTaxiTrip1.getId())
                                                         .organizationId(singleTaxiTrip1.getOrganizationId())
                                                         .tripType(TripType.SINGLE.name())
                                                         .status(InboundTaxiTripStatus.SENT_TO_CONTRACTOR.name())
                                                         .tariffId(singleTaxiTrip1.getTariff().getId())
                                                         .requestId(singleTaxiTrip1.getRequest().getId())
                                                         .build();
        taxiTripInput.accept(MessageBuilder.withPayload(taxiTripMessage).build());
        SingleTaxiTrip singleTaxiTrip = taxiTripService.findSingleTripById(taxiTripMessage.getId()).get();
        assertTrip(taxiTripMessage, singleTaxiTrip);
        
        taxiTripMessage = mapper.toMessage(singleTaxiTrip1);
        taxiTripInput.accept(MessageBuilder.withPayload(taxiTripMessage).build());
        singleTaxiTrip = taxiTripService.findSingleTripById(taxiTripMessage.getId()).get();
        assertTrip(taxiTripMessage, singleTaxiTrip);
        
    }
    
    private void assertTrip(TaxiTripMessage taxiTripMessage, TaxiTrip taxiTrip) {
        assertEquals(taxiTrip.getId(), taxiTripMessage.getId());
        assertEquals(Optional.ofNullable(taxiTrip.getFactParametersSettingTime()).map(d -> d.format(DATE_TIME_FORMATTER)).orElse(null),
                     Optional.ofNullable(taxiTripMessage.getFactParametersSettingTime()).map(d -> d.format(DATE_TIME_FORMATTER)).orElse(null));
        assertEquals(Optional.ofNullable(taxiTrip.getDateTimeRegistered()).map(d -> d.format(DATE_TIME_FORMATTER)).orElse(null),
                     Optional.ofNullable(taxiTripMessage.getDateTimeRegistered()).map(d -> d.format(DATE_TIME_FORMATTER)).orElse(null));
        assertEquals(taxiTrip.getOrganizationId(), taxiTripMessage.getOrganizationId());
        assertEquals(taxiTrip.getStatus(), taxiTripMessage.getStatus());
        assertEquals(taxiTrip.getTariff().getId(), taxiTripMessage.getTariffId());
        assertEquals(taxiTrip.getTaxiId(), taxiTripMessage.getTaxiId());
        assertEquals(taxiTrip.getTripFactDistance(), taxiTripMessage.getTripFactDistance());
        assertEquals(taxiTrip.getTripFactDuration(), taxiTripMessage.getTripFactDuration());
        assertEquals(taxiTrip.getTripFactPrice(), taxiTripMessage.getTripFactPrice());
        assertEquals(taxiTrip.getTripFactWaitTime(), taxiTripMessage.getTripFactWaitTime());
        assertEquals(Optional.ofNullable(taxiTrip.getLastXmlReceivedDateTime()).map(d -> d.format(DATE_TIME_FORMATTER)).orElse(null),
                     Optional.ofNullable(taxiTripMessage.getLastXmlReceivedDateTime()).map(d -> d.format(DATE_TIME_FORMATTER)).orElse(null));
        assertEquals(Optional.ofNullable(taxiTrip.getTripFinishTime()).map(d -> d.format(DATE_TIME_FORMATTER)).orElse(null),
                     Optional.ofNullable(taxiTripMessage.getTripFinishTime()).map(d -> d.format(DATE_TIME_FORMATTER)).orElse(null));
        assertEquals(Optional.ofNullable(taxiTrip.getTripStartTime()).map(d -> d.format(DATE_TIME_FORMATTER)).orElse(null),
                     Optional.ofNullable(taxiTripMessage.getTripStartTime()).map(d -> d.format(DATE_TIME_FORMATTER)).orElse(null));
        assertEquals(taxiTrip.getTripType().name(), taxiTripMessage.getTripType());
        if (taxiTrip instanceof CoopTaxiTrip coopTaxiTrip) {
            assertEquals(coopTaxiTrip.getSharedRide().getId(), taxiTripMessage.getSharedRideId());
            assertEquals(coopTaxiTrip.getRideId(), taxiTripMessage.getSharedRideId());
        } else if (taxiTrip instanceof SingleTaxiTrip singleTaxiTrip) {
            assertEquals(singleTaxiTrip.getRequest().getId(), taxiTripMessage.getRequestId());
        }
    }
    
}
