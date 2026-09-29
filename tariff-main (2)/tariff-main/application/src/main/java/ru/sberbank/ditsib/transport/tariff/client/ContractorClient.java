package ru.sberbank.ditsib.transport.tariff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.Field;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.SortDirection;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.TaskDTO;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.TransportPageDTO;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "ContractorClient", url = "${contractorClient.url}")
public interface ContractorClient {
    
    @GetMapping(
            value = "/{contractorId}/transport/",
            produces = "application/json"
    )
    TransportPageDTO getTransports(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(value = "startDate", required = false) @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(value = "endDate", required = false) @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "result", required = false) List<Field> result,
            @RequestParam(value = "direction", required = false) SortDirection direction,
            @RequestParam(value = "field", required = false) String field,
            @PathVariable(value = "contractorId") UUID contractorId,
            @RequestHeader(value = "Authorization") String token
                                  );
    
    @GetMapping(
            value = "/{contractorId}/transport/{transportId}/trips/",
            produces = "application/json"
    )
    List<TaskDTO> getTransport(
            @RequestParam(value = "startDate", required = false) @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(value = "endDate", required = false) @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PathVariable(value = "contractorId") UUID contractorId,
            @PathVariable(value = "transportId") UUID transportId,
            @RequestHeader(value = "Authorization") String token
                              );
    
}
