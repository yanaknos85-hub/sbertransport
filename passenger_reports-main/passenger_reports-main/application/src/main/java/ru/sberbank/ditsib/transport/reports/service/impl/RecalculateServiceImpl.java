package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dao.RequestRepository;
import ru.sberbank.ditsib.transport.reports.model.Employee_;
import ru.sberbank.ditsib.transport.reports.model.Request_;
import ru.sberbank.ditsib.transport.reports.service.PaymentService;
import ru.sberbank.ditsib.transport.reports.service.RecalculateService;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
class RecalculateServiceImpl implements RecalculateService {
    
    private final RequestRepository requestRepository;
    
    private final PaymentService paymentService;
    
    @Override
    @Async
    public void recalculate(@NonNull List<TripRequestStatus> statuses, @NonNull LocalDateTime from, @NonNull LocalDateTime to, List<UUID> authorId) {
        var requests = new LinkedList<>(requestRepository.findAll((root, query, cb) -> {
            var predicate = root.get(Request_.status).in(statuses.stream().map(Enum::name).toList());
            predicate = cb.and(predicate, cb.equal(root.get(Request_.transportType), TransportTypeEnum.PERSONAL.name()));
            predicate = cb.and(predicate, cb.between(root.get(Request_.orderPaymentFormationStartDate), from, to));
            if (authorId != null && !authorId.isEmpty()) {
                var author = root.join(Request_.author);
                predicate = cb.and(predicate, author.get(Employee_.id).in(authorId));
            }
            return predicate;
        }, Pageable.unpaged()).getContent());
        long totalElements = requests.size();
        log.info("Found %s requests".formatted(totalElements));
        for (var i = 0; i < totalElements; i++) {
            var request = requests.pollFirst();
            if (request == null) {
                return;
            }
            try {
                paymentService.fillPaymentData(request);
            } catch (Exception e) {
                log.error("Processing request %s failed".formatted(request.getHumanReadableId()), e);
            }
            var processed = i + 1;
            log.info("%s/%s requests processed (%03f%%)".formatted(processed, totalElements, (double) (processed) / totalElements * 100D));
        }
    }
}
