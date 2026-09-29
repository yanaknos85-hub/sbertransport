package ru.sberbank.ditsib.transport.request.database.dao;

import io.qameta.allure.Feature;
import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.model.RequestForGroupTransfer;
import ru.sberbank.ditsib.transport.request.database.model.RequestHistoryElementForGroupTransfer;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка репозитория получения истории по заявкам на трансфер")
@RequiredArgsConstructor
class RequestForGroupTransferHistoryRepositoryTest extends KafkaTest {
    
    @Autowired
    private RequestForGroupTransferHistoryRepository repository;
    
    @Autowired
    private RequestForGroupTransferRepository requestForGroupTransferRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ContractorRepository contractorRepository;
    
    @DisplayName("Получение истории для заявок по трансферам упорядоченным по датам")
    @Test
    void findAllByIdOrderByChangeDate() {
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        var employee = employeeRepository.save(Employee.builder()
                                                       .id(UUID.randomUUID())
                                                       .firstName("firstName")
                                                       .lastName("lastName")
                                                       .build());


        var request1 = RequestForGroupTransfer.builder()
                .status(TripRequestStatus.GROUP_TRANSFER_APPROVED)
                .humanReadableId("TEST1")
                .author(employee)
                .passenger(employee)
                .transportType(TransportTypeEnum.GROUP_TRANSFER)
                .desiredDate(LocalDateTime.now())
                .contractorId(contractor.getId())
                .build();

        var request2 = RequestForGroupTransfer.builder()
                .status(TripRequestStatus.GROUP_TRANSFER_APPROVED)
                .humanReadableId("TEST2")
                .author(employee)
                .passenger(employee)
                .transportType(TransportTypeEnum.GROUP_TRANSFER)
                .desiredDate(LocalDateTime.now())
                .contractorId(contractor.getId())
                .build();
        
        requestForGroupTransferRepository.save(request1);
        requestForGroupTransferRepository.save(request2);
        requestForGroupTransferRepository.flush();
        
        var record1 = RequestHistoryElementForGroupTransfer.builder()
                                                           .changeDate(LocalDateTime.now().minus(2, ChronoUnit.DAYS))
                                                           .requestForGroupTransfer(request1)
                                                           .status(TripRequestStatus.GROUP_TRANSFER_CANCELLED)
                                                           .initiator(UUID.randomUUID())
                                                           .build();
        
        var record2 = RequestHistoryElementForGroupTransfer.builder()
                                                           .changeDate(LocalDateTime.now().minus(4, ChronoUnit.DAYS))
                                                           .status(TripRequestStatus.GROUP_TRANSFER_AWAITING_APPROVAL)
                                                           .requestForGroupTransfer(request1)
                                                           .initiator(UUID.randomUUID())
                                                           .build();
        
        var record3 = RequestHistoryElementForGroupTransfer.builder()
                                                           .changeDate(LocalDateTime.now().minus(3, ChronoUnit.DAYS))
                                                           .requestForGroupTransfer(request1)
                                                           .status(TripRequestStatus.GROUP_TRANSFER_AWAITING_APPROVAL)
                                                           .initiator(UUID.randomUUID())
                                                           .build();
    
        var record4 = RequestHistoryElementForGroupTransfer.builder()
                                                           .changeDate(LocalDateTime.now().minus(3, ChronoUnit.DAYS))
                                                           .requestForGroupTransfer(request2)
                                                           .status(TripRequestStatus.GROUP_TRANSFER_AWAITING_APPROVAL)
                                                           .initiator(UUID.randomUUID())
                                                           .build();
        
        repository.saveAll(List.of(record1, record2, record3, record4));
        
        assertNotNull(repository);
        var allHistoryRecords = repository.findAll();
        assertEquals(4, allHistoryRecords.size());
    
        var resultHistoryRecords = repository.findAllByRequestForGroupTransferIdOrderByChangeDate(request1.getId());
        assertEquals(3, resultHistoryRecords.size());
        assertAll(
                () -> resultHistoryRecords.get(0).getChangeDate().isBefore(resultHistoryRecords.get(1).getChangeDate()),
                () -> resultHistoryRecords.get(1).getChangeDate().isBefore(resultHistoryRecords.get(2).getChangeDate())
        );
    }
}