package ru.sber.transport.authentication.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sber.transport.authentication.business.dto.AuditDto;

@RequestMapping("/audit")
@Tag(name = "Аудит входа", description = "Аудит входа")
public interface AuditRecordsController {
    
    @Operation(summary = "Аудит", description = "Список записей аудита")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    Iterable<AuditDto> getRecords(
            @Parameter(description = "Размер страницы. По-умолчанию - " + Integer.MAX_VALUE)
            @RequestParam(value = "size", required = false) Integer size,
            @Parameter(description = "Номер страницы. По-умолчанию - 0")
            @RequestParam(value = "page", required = false) Integer page
                                 );
    
}
