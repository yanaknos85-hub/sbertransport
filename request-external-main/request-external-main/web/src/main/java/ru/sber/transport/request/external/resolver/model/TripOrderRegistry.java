package ru.sber.transport.request.external.resolver.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sber.transport.request.external.model.Assessment;
import ru.sber.transport.request.external.model.Assessments;
import ru.sber.transport.request.external.model.Employee;
import ru.sber.transport.request.external.model.OrderData;
import ru.sber.transport.request.external.model.State;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.model.TripOrderHistory;
import ru.sber.transport.request.external.model.WaypointData;
import ru.sber.transport.request.external.web.util.WebParamUtils;

/**
 * Строка файла в реестре
 */
@Getter
public class TripOrderRegistry {

    private static final Logger log = LoggerFactory.getLogger(TripOrderRegistry.class);
    /**
     * Номер заявки
     */
    private final String number;

    /**
     * Место возникновения затрат (МВЗ)
     */
    private final String costCenter;

    /**
     * Дата заявки
     */
    private final LocalDateTime createDate;

    /**
     * Статус заявки
     */
    private final String status;

    /**
     * Статус поездки (причина отмены)
     */
    private final String reason;

    /**
     * Вид тарифа
     */
    private final String tariff;

    /**
     * Адрес отправления
     */
    private final String startPoint;

    /**
     * Адрес прибытия
     */
    private final String endPoint;

    /**
     * Дата/время поездки
     */
    private final LocalDateTime tripDate;

    /**
     * Дата утверждения
     */
    private final LocalDateTime approvalDate;

    /**
     * Контрольный срок
     */
    private final LocalDateTime paymentDeadline;

    /**
     * Контрольный срок нарушен
     */
    private final String paymentDeadlineViolated;

    /**
     * Комментарий
     */
    private final String comment;

    /**
     * ФИО пассажира
     */
    private final String passenger;

    /**
     * ФИО согласующего
     */
    private final String approver;

    /**
     * Плановая стоимость
     */
    private final BigDecimal plannedCost;

    /**
     * Фактическая стоимость
     */
    private final BigDecimal factCost;

    /**
     * Плановая длительность (в минутах)
     */
    private final Integer plannedDuration;

    /**
     * Плановое расстояние,км
     */
    private final BigDecimal distance;

    /**
     * Оценка сервису
     */
    private final Short rating;

    /**
     * Комментарий к оценке
     */
    private final String assessmentComment;

    /**
     * Табельный номер
     */
    private final String personnelNumber;

    /**
     * Комментарий
     */
    private final FileAssessment assessment;

    /**
     * Экономия(руб.) при заказе я.такси
     */
    private final BigDecimal economy;

    /**
     * Создает строку реестра
     *
     * @param source    - исходный объект заявки
     * @param histories - история заявки
     */
    public TripOrderRegistry(TripOrderData source, List<TripOrderHistory> histories) {
        final var offset = WebParamUtils.parseTimeZoneToOffset(source.getTimeZone());
        number = source.getHumanReadableId();
        createDate = histories.stream()
                .map(TripOrderHistory::getModifiedAt)
                .min(Comparator.naturalOrder())
                .map(it -> it.atZoneSameInstant(offset).toLocalDateTime())
                .orElse(null);
        status = switch (source.getStatus()) {
            case NEW -> "Создана";
            case CONFIRMATION_NEEDED -> "Требуется подтверждение завершения";
            case CONFIRMATION -> "Требуется утверждение руководителем";
            case CONFIRMED, DECLINED -> "Завершена";
            case GENAI_CHECK -> "Проверка GenAI";
            case DATA_NEEDED -> "Требуются сведения";
            case CANCELLED -> "Отменена";
            case ORDER_PAYMENT_FORMATION -> "Формирование приказа на выплату";
            case PAYMENT_AWAITING -> "Ожидание выплаты";
            case PAYMENT_DONE -> "Выплата произведена";
            case PAYMENT_NOT_DONE -> "Выплата не произведена";
        };
        reason = source.getReason();
        tariff = source.getTariff().name();
        passenger = getFullName(source.getPassenger());
        personnelNumber = source.getPassenger().getPersonnelNumber();
        costCenter = Optional.ofNullable(source.getCostCenter()).orElse("");
        tripDate = source.getDate().atZoneSameInstant(offset).toLocalDateTime();
        approvalDate = histories.stream()
                .filter(it -> Objects.equals(State.ORDER_PAYMENT_FORMATION, it.getStatus()))
                .findFirst()
                .map(it -> it.getModifiedAt().atZoneSameInstant(offset).toLocalDateTime())
                .orElse(null);
        comment = source.getComment();
        paymentDeadline = Optional.ofNullable(approvalDate).map(it -> it.plusDays(13)).orElse(null);
        paymentDeadlineViolated = isPaymentDeadlineViolated(histories);
        approver = getFullName(source.getApprover());
        final var waypoints = source.getWaypoints();
        startPoint = waypoints == null || waypoints.isEmpty() ? "" : toAddress(waypoints.getFirst());
        endPoint = waypoints == null || waypoints.isEmpty() ? "" : toAddress(waypoints.getLast());
        plannedCost = source.getPlanned().getCost();
        factCost = source.getActual().getCost();
        economy = Optional.ofNullable(source)
                .map(TripOrderData::getEconomy)
                .orElse(null);
        distance = Optional.of(source)
                .map(TripOrderData::getPlanned)
                .map(OrderData::getDistance)
                .map(meters -> BigDecimal.valueOf(meters)
                        .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);
        plannedDuration = Optional.of(source)
                .map(TripOrderData::getPlanned)
                .map(OrderData::getDuration)
                .map(Duration::toMinutes)
                .map(Math::toIntExact)
                .orElse(0);
        rating = Optional.ofNullable(source)
                .map(TripOrderData::getAssessments)
                .map(Assessments::getService)
                .map(Assessment::getRating)
                .map(Byte::shortValue)
                .orElse(null);

        assessmentComment = Optional.ofNullable(source)
                .map(TripOrderData::getAssessments)
                .map(Assessments::getService)
                .map(Assessment::getComment)
                .orElse(null);
        assessment = new FileAssessment(source.getAssessments().getService());
    }

    private String toAddress(WaypointData waypointData) {
        return Stream.of(
                waypointData.getCountry(),
                waypointData.getRegion(),
                waypointData.getCity(),
                waypointData.getStreet(),
                waypointData.getHouse(),
                Optional.ofNullable(waypointData.getBuilding()).map(it -> "корп. " + it).orElse(null),
                Optional.ofNullable(waypointData.getStructure()).map(it -> "стр. " + it).orElse(null)
        ).filter(Objects::nonNull).collect(Collectors.joining(", "));
    }

    private static @NotNull String getFullName(Employee source) {
        return Stream.of(source.getFirstName(), source.getPatronymic(), source.getLastName())
            .filter(Objects::nonNull)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.joining(" "));
    }

    private String isPaymentDeadlineViolated(List<TripOrderHistory> histories) {
        var paymentDoneDate = histories.stream()
                .filter(it -> State.PAYMENT_DONE.equals(it.getStatus()))
                .findFirst()
                .map(TripOrderHistory::getModifiedAt);

        var confirmedDate = histories.stream()
                .filter(it -> State.CONFIRMED.equals(it.getStatus()))
                .findFirst()
                .map(TripOrderHistory::getModifiedAt);

        if (paymentDoneDate.isEmpty() || confirmedDate.isEmpty()) {
            return "Нет";
        }

        return (ChronoUnit.DAYS.between(confirmedDate.get(), paymentDoneDate.get()) > 12) ? "Да" : "Нет";
    }
}
