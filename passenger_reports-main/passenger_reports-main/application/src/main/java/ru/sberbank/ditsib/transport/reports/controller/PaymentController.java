package ru.sberbank.ditsib.transport.reports.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequestMapping({"payment","payment/"})
public interface PaymentController {
    
    @GetMapping({"recalculate","recalculate/"})
    ResponseEntity<Object> recalculate(
            @RequestParam(value = "statuses", required = false) List<TripRequestStatus> statuses,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(value = "from", required = false) LocalDateTime from,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(value = "to", required = false) LocalDateTime to,
            @RequestParam(value = "authorId", required = false) List<UUID> authorId
                    );
    
}
