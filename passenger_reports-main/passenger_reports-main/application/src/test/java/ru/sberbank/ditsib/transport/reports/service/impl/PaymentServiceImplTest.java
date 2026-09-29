package ru.sberbank.ditsib.transport.reports.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sberbank.ditsib.transport.constants.PaymentTypeCode;
import ru.sberbank.ditsib.transport.constants.PersonalCarOwnerInfo;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dao.RequestRepository;
import ru.sberbank.ditsib.transport.reports.model.ExpectedData;
import ru.sberbank.ditsib.transport.reports.model.PersonalCar;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.tariff.PersonalTariff;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Проверка начисления суммы доплаты за пассажиров")
class PaymentServiceImplTest {
    
    @Test
    @DisplayName("Доплата за пассажиров (автомобиль в собственности водителя)")
    void additionalSum_with4664_test() {
        
        var requestRepository = mock(RequestRepository.class);
        
        var cut = new PaymentServiceImpl(requestRepository);
        List<Request> requests = new ArrayList<>();
        
        Request request = Request.builder()
                .employeeDriverId(UUID.randomUUID())
                .personalCar(PersonalCar.builder().ownerInfo(PersonalCarOwnerInfo.USER).build())
                .transportType(TransportTypeEnum.PERSONAL.name())
                .orderPaymentFormationStartDate(LocalDateTime.of(2023, 9, 11,9,11,0))
                .expected(ExpectedData.builder().cost(555.0).distance(0.0).build())
                .status(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name())
                .tariff(PersonalTariff.builder().trustIdx(1.0).build())
                .additionalSum(12345L)
                                 .build();
        
        when(requestRepository.findByDriverAndMonth(any(), anyInt(), anyInt())).thenReturn(requests);
        when(requestRepository.save(any())).thenReturn(request);
        
        request = cut.fillPaymentData(request);
        
        assertEquals(request.getPaymentData().getPaymentTypeCodeInsurance(), PaymentTypeCode.CODE_4664,
                     "Сумма доплаты за пассажиров должна попадать на код выплаты 4664");
        assertEquals(request.getPaymentData().getPaymentPriceInsurance(), 12345L,
                     "Сумма доплаты за пассажиров должна попадать на код выплаты 4664");
    }
    
    @Test
    @DisplayName("Доплата за пассажиров (автомобиль в собственности 3-их лиц)")
    void additionalSum_with4664_thirdParty_test() {
        
        var requestRepository = mock(RequestRepository.class);
        
        var cut = new PaymentServiceImpl(requestRepository);
        List<Request> requests = new ArrayList<>();
        
        Request request = Request.builder()
                                 .employeeDriverId(UUID.randomUUID())
                                 .personalCar(PersonalCar.builder().ownerInfo(PersonalCarOwnerInfo.THIRD_PARTY).build())
                                 .transportType(TransportTypeEnum.PERSONAL.name())
                                 .orderPaymentFormationStartDate(LocalDateTime.of(2023, 9, 11,9,11,0))
                                 .expected(ExpectedData.builder().cost(555.0).distance(0.0).build())
                                 .status(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION.name())
                                 .tariff(PersonalTariff.builder().trustIdx(1.0).build())
                                 .additionalSum(12345L)
                                 .build();
        
        when(requestRepository.findByDriverAndMonth(any(), anyInt(), anyInt())).thenReturn(requests);
        when(requestRepository.save(any())).thenReturn(request);
        
        request = cut.fillPaymentData(request);
        
        assertEquals(request.getPaymentData().getPaymentTypeCodeMain(), PaymentTypeCode.CODE_4664,
                     "Сумма доплаты за пассажиров должна попадать на основной код выплаты 4664");
        assertEquals(request.getPaymentData().getPaymentPriceMain(), 12900,
                     "Сумма доплаты за пассажиров должна добавиться к сумме на основном коде выплаты 4664 (555 + 12345 = 12900), где 555 - " +
                     "стоимость поездки, 12345 - сумма доплаты за пассажира");
    }
}