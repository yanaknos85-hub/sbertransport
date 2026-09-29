package ru.sberbank.ditsib.transport.request.validate.request.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.RequestRepository;
import ru.sberbank.ditsib.transport.request.database.model.ExpectedData_;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.Request_;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee_;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.exceptions.CreateRequestConflictException;
import ru.sberbank.ditsib.transport.request.validate.request.NewRequestValidator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Transactional
@ConditionalOnProperty(prefix = "request.validation.others.enabled", value = "true", matchIfMissing = true)
public class OtherRequestsValidator implements NewRequestValidator {

    /**
     * Множитель для перевода из наносекунд в секунды
     */
    private static final double NANOS_TO_SECS_MULTIPLIER = 0.000000001;

    private final RequestRepository requestRepository;

    private final Set<TransportTypeEnum> validation;

    @Getter
    @Accessors(fluent = true)
    private final Set<TransportTypeEnum> validationDisabledFor;

    @Getter
    @Accessors(fluent = true)
    private final Set<TransportTypeEnum> validationEnabledFor;

    public OtherRequestsValidator(RequestRepository requestRepository,
                                  @Value("${request.validation.others.enabled-for:}")
                                  Set<TransportTypeEnum> validationEnabledFor,
                                  @Value("${request.validation.others.disabled-for:}")
                                  Set<TransportTypeEnum> validationDisabledFor) {
        this.validationEnabledFor = validationEnabledFor;
        this.validationDisabledFor = validationDisabledFor;
        this.requestRepository = requestRepository;
        this.validation = validationEnabledFor().parallelStream().filter(it -> !validationDisabledFor().contains(it)).collect(Collectors.toUnmodifiableSet());
        log.info("OtherRequestsValidator initialized with validation {}", validation);
    }

    @Override
    public void validate(@NonNull NewRequestDTO request, @NonNull Employee employee) {
        final var desiredDate = request.getDesiredDate();
        final var expected = request.getExpected();
        final var expectedEndDate = desiredDate.plus(expected.getTime());
        final var passenger = request.getPassenger();
        final var passengerId = passenger.id();
        final var spec = (Specification<Request>) (root, q, cb) -> {
            final var passengerJoin = root.join(Request_.passenger);
            final var zero = cb.literal(0);
            final var makeInterval = cb.function("make_interval", Duration.class, zero, zero, zero, zero, zero, zero, cb.prod(root.get(Request_.expected).get(ExpectedData_.time).as(Long.class), NANOS_TO_SECS_MULTIPLIER));
            final var expectedEndTime = cb.sum(root.get(Request_.desiredDate).as(Long.class), makeInterval.as(Long.class)).as(LocalDateTime.class);
            return cb.and(
                    root.get(Request_.transportType).in(validation),
                    cb.equal(passengerJoin.get(Employee_.id), passengerId),
                    cb.not(root.get(Request_.status).in(TripRequestStatus.getTerminalStatus(true))),
                    cb.or(
                            // Начало старой заявки в периоде новой ( -|----{---|- )
                            cb.between(root.get(Request_.desiredDate), cb.literal(desiredDate), cb.literal(expectedEndDate)),

                            // Окончание старой заявки в периоде новой ( -|-----}----|- )
                            cb.between(expectedEndTime, cb.literal(desiredDate), cb.literal(expectedEndDate)),

                            //  Новая заявка включает в себя старую ( -{---|-----|--}- )
                            cb.and(
                            cb.between(cb.literal(desiredDate), root.get(Request_.desiredDate), expectedEndTime),
                            cb.between(cb.literal(expectedEndDate), root.get(Request_.desiredDate), expectedEndTime)
                            )
                    )
            );
        };
        if (log.isDebugEnabled()) {
            final var existRequests = requestRepository.findAll(spec);
            log.debug("Validating request for passenger {}\ndesiredDate {}, expectedTime {}, expectedEndDate {} found: {}\nPcs:\n{}\n",
                    passengerId, desiredDate, expected.getTime(), expectedEndDate, existRequests.size(),
                    existRequests.parallelStream().map(it -> "%s (%s, desiredDate %s, expectedTime %s, expectedEndDate %s, status %s)".formatted(it.getId(), it.getTransportType(), it.getDesiredDate(), it.getExpected().getTime(), it.getDesiredDate().plus(it.getExpected().getTime()), it.getStatus())).map(Object::toString).collect(Collectors.joining("\n ")));
        }
        if (requestRepository.exists(spec)) {
            final var requestZone = ZoneOffset.of(request.getTimeZone().replace("GMT", ""));
            final var originalDesiredDate = desiredDate.atOffset(ZoneOffset.UTC).atZoneSameInstant(requestZone);
            final var originalExpectedEndDate = expectedEndDate.atOffset(ZoneOffset.UTC).atZoneSameInstant(requestZone);
            log.warn("Duplicate request found for passenger {}, start {}, end {}", passengerId, originalDesiredDate, originalExpectedEndDate);
            throw new CreateRequestConflictException("На выбранные дату и время уже есть активная заявка. Измените время или отмените действующую заявку.");
        }
    }
}