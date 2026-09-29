package ru.sberbank.ditsib.transport.reports.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.reports.dao.PersonalCarRepository;
import ru.sberbank.ditsib.transport.reports.dao.RequestRepository;
import ru.sberbank.ditsib.transport.reports.model.Employee;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.service.PaymentService;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Transactional
@EmbeddedPostgres
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Проверка запуска пересчета")
class RecalculateServiceImplTest {
    
    @Autowired
    private RequestRepository requestRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PersonalCarRepository personalCarRepository;
    
    private final PaymentService paymentService = mock(PaymentService.class);
    
    @Test
    @DisplayName("Проверка запуска поиска")
    void test_findData() {
        var recalculateService = new RecalculateServiceImpl(requestRepository, paymentService);
        
        var author = employeeRepository.save(Employee.builder().id(UUID.randomUUID()).build());
        var request = Request.builder()
                             .id(UUID.randomUUID())
                             .status(TripRequestStatus.PERSONAL_PAYMENT_AWAITING.name())
                             .orderPaymentFormationStartDate(LocalDateTime.now().plusDays(10))
                             .author(author)
                             .transportType(TransportTypeEnum.PERSONAL.name())
                             .build();
        requestRepository.save(request);
        
        
        recalculateService.recalculate(List.of(TripRequestStatus.PERSONAL_PAYMENT_AWAITING),
                                       LocalDateTime.now(), LocalDateTime.now().plusDays(50),
                                       List.of(author.getId()));
        
        var requestCaptor = ArgumentCaptor.forClass(Request.class);
        
        verify(paymentService).fillPaymentData(requestCaptor.capture());
        
        var actualRequests = requestCaptor.getAllValues();
        assertThat(actualRequests).hasSize(1);
        assertThat(actualRequests.get(0).getId()).isEqualTo(request.getId());
    }
    
}