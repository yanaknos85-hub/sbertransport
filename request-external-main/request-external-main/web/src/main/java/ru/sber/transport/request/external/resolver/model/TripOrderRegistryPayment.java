package ru.sber.transport.request.external.resolver.model;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.sber.transport.request.external.model.Employee;
import ru.sber.transport.request.external.model.OrderData;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.web.util.WebParamUtils;

@Getter
public class TripOrderRegistryPayment {

    private static final Integer PAYMENT_CODE = 4666;
    private static final Integer PERIOD_1 = 1;
    private static final Integer PERIOD_1_LAST_DAY = 7;
    private static final Integer PERIOD_2 = 2;
    private static final Integer PERIOD_2_LAST_DAY = 15;
    private static final Integer PERIOD_3 = 3;
    private static final Integer PERIOD_3_LAST_DAY = 23;
    private static final Integer PERIOD_4 = 4;
    private static final String RESOURCE_VALUE = "31530";
    /**
     * ID заявки
     */
    private String humanReadableId;

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
    private Integer period;

    public TripOrderRegistryPayment(TripOrderData source) {
        final var offset = WebParamUtils.parseTimeZoneToOffset(source.getTimeZone());
        this.humanReadableId = source.getHumanReadableId();
        this.paymentCode = PAYMENT_CODE;
        this.resource = RESOURCE_VALUE;
        this.factCost = getFactCost(source.getActual());
        this.personnelNumber = source.getPassenger().getPersonnelNumber();
        this.fio = getFullName(source.getPassenger());
        this.period = getPeriod(source, offset);
    }

    private static @NotNull String getFullName(Employee source) {
        return "%s %s %s".formatted(source.getFirstName(), source.getPatronymic(), source.getLastName());
    }

    private static Integer getPeriod(TripOrderData source, ZoneOffset offset) {
        int day = source.getDate().atZoneSameInstant(offset).toLocalDateTime().getDayOfMonth();
        if (day <= PERIOD_1_LAST_DAY) {
            return PERIOD_1;
        } else if (day <= PERIOD_2_LAST_DAY) {
            return PERIOD_2;
        } else if (day <= PERIOD_3_LAST_DAY) {
            return PERIOD_3;
        } else {
            return PERIOD_4;
        }
    }

    private static @NotNull BigDecimal getFactCost(OrderData source) {
        return Optional.ofNullable(source).map(OrderData::getCost).orElse(BigDecimal.ZERO);
    }
}
