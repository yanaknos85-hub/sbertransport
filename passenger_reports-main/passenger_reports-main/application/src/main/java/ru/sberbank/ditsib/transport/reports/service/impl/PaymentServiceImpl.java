package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.PaymentTypeCode;
import ru.sberbank.ditsib.transport.constants.PersonalCarOwnerInfo;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dao.RequestRepository;
import ru.sberbank.ditsib.transport.reports.model.ExpectedData;
import ru.sberbank.ditsib.transport.reports.model.PaymentData;
import ru.sberbank.ditsib.transport.reports.model.PersonalCar;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.tariff.PersonalTariff;
import ru.sberbank.ditsib.transport.reports.service.PaymentService;

import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Transactional
@Slf4j
@Component
@RequiredArgsConstructor
class PaymentServiceImpl implements PaymentService {
    
    private final RequestRepository requestRepository;
    
    @Override
    public Request fillPaymentData(Request request) {
        if (request.getEmployeeDriverId() != null && request.getPersonalCar() != null
            && TransportTypeEnum.PERSONAL.name().equals(request.getTransportType())
            && request.getOrderPaymentFormationStartDate() != null) {
            var requestByDriverAndMonthList = requestRepository.findByDriverAndMonth(
                    request.getEmployeeDriverId(),
                    request.getOrderPaymentFormationStartDate().getYear(),
                    request.getOrderPaymentFormationStartDate().getMonthValue());
            
            request.setPaymentData(getPersonalPaymentData(requestByDriverAndMonthList, request));
            return requestRepository.save(request);
        }
        return request;
    }
    
    private PaymentData getPersonalPaymentData(List<Request> requestByDriverAndMonthList, Request request) {
        var expectedCost = Optional.ofNullable(request.getExpected()).map(ExpectedData::getCost).orElse(null);
        if (expectedCost == null) {
            return null;
        }
        var statusesForPayment = List.of(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION,
                                         TripRequestStatus.PERSONAL_PAYMENT_AWAITING,
                                         TripRequestStatus.PERSONAL_PAYMENT_DONE,
                                         TripRequestStatus.PERSONAL_PAYMENT_DECLINED);
        
        if (statusesForPayment.stream().noneMatch(status -> status.name().equals(request.getStatus()))) {
            return null;
        }
        
        var requestCost = request.getExpected().getCost().longValue();
        var additionalSum = Optional.ofNullable(request.getAdditionalSum()).orElse(0L);
        
        if (PersonalCarOwnerInfo.THIRD_PARTY.equals(request.getPersonalCar().getOwnerInfo())) {
            PaymentData paymentData = new PaymentData();
            paymentData.setPaymentTypeCodeMain(PaymentTypeCode.CODE_4664);
            paymentData.setPaymentPriceMain(requestCost + additionalSum);
            return paymentData;
        }
        
        var engineVolumeMax = requestByDriverAndMonthList.stream()
                                                         .map(Request::getPersonalCar)
                                                         .filter(Objects::nonNull)
                                                         .map(PersonalCar::getEngineVolume)
                                                         .max(Integer::compareTo).orElse(0);
        var limit = PaymentTypeCode.CODE_4661.getPaymentLimitForEngineVolume(engineVolumeMax);
        
        /*
            Расчет общей стоимости заявок за месяц,
            оплата по которым будет производиться по коду 4661
        */
        long sumWithCode4661OtherRequest = requestByDriverAndMonthList.stream()
                                                                      .filter(r -> !request.getId().equals(r.getId()))
                                                                      .filter(r -> statusesForPayment.stream().anyMatch(s -> s.name().equals(r.getStatus())))
                                                                      .mapToLong(this::getSumWithCode4661)
                                                                      .sum();
        var paymentData = new PaymentData();
        
        /*
            Расчет суммы страхования
            Сумма страхования учитывается всегда, она вычитается из суммы оплаты по коду 4661 и оплачивается по коду 4664
         */
        var tariff = (PersonalTariff) request.getTariff();
        var trustSum = Math.round(request.getExpected().getDistance() * tariff.getTrustIdx()) * 100;
        
        if (trustSum > requestCost) {
            trustSum = requestCost;
            log.warn(String.format("Сумма страхования больше суммы поездки, trustIdx = %s, requestCost = %s, requestId = %s",
                                   tariff.getTrustIdx(),
                                   requestCost,
                                   request.getId())
                    );
        }
        
        /*
            Если общая стоимость заявок за месяц превышает или равна лимиту оплаты по коду 4661,
            оплата по текущей заявке будет произведена по коду 4665 и 4664
        */
        if (sumWithCode4661OtherRequest >= limit) {
            paymentData.setPaymentTypeCodeMain(PaymentTypeCode.CODE_4665);
            paymentData.setPaymentPriceMain(requestCost - trustSum);
            paymentData.setPaymentTypeCodeInsurance(PaymentTypeCode.CODE_4664);
            paymentData.setPaymentPriceInsurance(trustSum + additionalSum);
            return paymentData;
        }
        
        /*
            Расчет общей стоимости заявок за месяц с учетом текущей заявки
            Если общая стоимость заявок стала превышать лимит оплаты по коду 4661 только после
            добавления к общей сумме цены текущей заявки, то часть заявки будет оплачена по коду 4661,
            а остальная ее часть по коду 4665, сумма страхования будет оплачена по коду 4664
        */
        long sumAllRequest = sumWithCode4661OtherRequest + (requestCost - trustSum);
        if (sumAllRequest > limit) {
            paymentData.setPaymentTypeCodeMain(PaymentTypeCode.CODE_4661);
            paymentData.setPaymentPriceMain(limit.longValue() - sumWithCode4661OtherRequest);
            
            paymentData.setPaymentTypeCodeInsurance(PaymentTypeCode.CODE_4664);
            paymentData.setPaymentPriceInsurance(trustSum + additionalSum);
            
            paymentData.setPaymentTypeCodeOptional(PaymentTypeCode.CODE_4665);
            paymentData.setPaymentPriceOptional((requestCost - trustSum) - paymentData.getPaymentPriceMain());
            return paymentData;
        }
        
        /*
            Если ни одно из описанных ранее условий не выполнилось,
            значит текущая заявка может быть оплачена полностью по коду 4661 с учетом суммы страхования,
            оплачиваемой по коду 4664
         */
        paymentData.setPaymentTypeCodeMain(PaymentTypeCode.CODE_4661);
        paymentData.setPaymentPriceMain(requestCost - trustSum);
        
        paymentData.setPaymentTypeCodeInsurance(PaymentTypeCode.CODE_4664);
        paymentData.setPaymentPriceInsurance(trustSum + additionalSum);
        
        paymentData.setPaymentTypeCodeOptional(PaymentTypeCode.CODE_4665);
        paymentData.setPaymentPriceOptional(0L);
        return paymentData;
    }
    
    private long getSumWithCode4661(Request request) {
        long sum = 0;
        if (request != null && request.getExpected() != null && request.getExpected().getCost() != null && request.getPaymentData() != null) {
            if (request.getPaymentData().getPaymentTypeCodeMain() == PaymentTypeCode.CODE_4661 &&
                request.getPaymentData().getPaymentPriceMain() != null) {
                sum = sum + request.getPaymentData().getPaymentPriceMain();
            }
            if (request.getPaymentData().getPaymentTypeCodeOptional() == PaymentTypeCode.CODE_4661 &&
                request.getPaymentData().getPaymentPriceOptional() != null) {
                sum = sum + request.getPaymentData().getPaymentPriceOptional();
            }
        }
        return sum;
    }
}
