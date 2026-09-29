package ru.sberbank.ditsib.transport.reports.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sber.transport.exceptions.dto.Constraint;
import ru.sber.transport.exceptions.dto.ExceptionBody;
import ru.sber.transport.exceptions.dto.Problem;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.controller.PaymentController;
import ru.sberbank.ditsib.transport.reports.service.RecalculateService;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@E2EController
@RequiredArgsConstructor
class PaymentControllerImpl implements PaymentController {
    
    private final RecalculateService recalculateService;
    
    @Override
    public ResponseEntity<Object> recalculate(List<TripRequestStatus> statuses, LocalDateTime from, LocalDateTime to, List<UUID> authorId) {
        List<Problem> problems = new ArrayList<>();
        if (statuses == null || statuses.isEmpty()) {
            var constraint = Constraint.builder().type("NotEmpty").build();
            problems.add(Problem.builder().field("statuses").value("[]").constraints(List.of(constraint)).build());
        }
        if (from == null) {
            var constraint = Constraint.builder().type("NotNull").build();
            problems.add(Problem.builder().field("from").value("null").constraints(List.of(constraint)).build());
        }
        if (to == null) {
            var constraint = Constraint.builder().type("NotNull").build();
            problems.add(Problem.builder().field("to").value("null").constraints(List.of(constraint)).build());
        }
        if (from != null && to != null && from.isAfter(to)) {
            String rawTo = to.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String rawFrom = from.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            var fromConstraint = Constraint.builder().type("NotAfter").value(rawTo).build();
            var toConstraint = Constraint.builder().type("NotBefore").value(rawFrom).build();
            problems.add(Problem.builder().field("from").value(rawFrom).constraints(List.of(fromConstraint)).build());
            problems.add(Problem.builder().field("to").value(rawTo).constraints(List.of(toConstraint)).build());
        }
        if (!problems.isEmpty()) {
            return ResponseEntity.badRequest().body(ExceptionBody.builder().problems(problems).message("Type mismatch").path("/payment/recalculate")
                                                                 .timestamp(OffsetDateTime.now()).build());
        }
        recalculateService.recalculate(statuses, from, to, authorId);
        return ResponseEntity.ok().build();
    }
}
