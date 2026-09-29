package ru.sber.transport.request.external.resolver.model;

import java.math.BigDecimal;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.sber.transport.request.external.model.Employee;
import ru.sber.transport.request.external.model.OrderData;
import ru.sber.transport.request.external.model.TripOrderData;

@Getter
@AllArgsConstructor
public class TripOrderRegistryPaymentAggregation {
    private static final Integer PAYMENT_CODE = 4666;
    private static final String RESOURCE_VALUE = "31530";
    /**
     * Вид оплаты
     */
    private int paymentCode;

    /**
     * Ресурс
     */
    private String resource;

    /**
     * Стоимость
     */
    private BigDecimal factCost;

    /**
     * Табельный номер
     */
    private String personnelNumber;

    /**
     * ФИО пользователя
     */
    private String fio;

    /**
     * Период формирования приказа на выплату
     */
    private String periodFormationForPayment;

    public TripOrderRegistryPaymentAggregation(TripOrderData source) {
        this.paymentCode = PAYMENT_CODE;
        this.resource = RESOURCE_VALUE;
        this.factCost = getFactCost(source.getActual());
        this.fio = getFullName(source.getPassenger());
        this.personnelNumber = source.getPassenger().getPersonnelNumber();
    }

    private static @NotNull String getFullName(Employee source) {
        return "%s %s %s".formatted(source.getFirstName(), source.getPatronymic(), source.getLastName());
    }

    private static @NotNull BigDecimal getFactCost(OrderData source) {
        return Optional.ofNullable(source).map(OrderData::getCost).orElse(BigDecimal.ZERO);
    }
}
