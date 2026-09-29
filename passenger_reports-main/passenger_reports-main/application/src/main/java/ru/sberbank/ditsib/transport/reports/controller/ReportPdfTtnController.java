package ru.sberbank.ditsib.transport.reports.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping({"cargo","cargo/"})
@Tag(name = "Транспортная накладная", description = "Создание транспортной накладной")
public interface ReportPdfTtnController {
    
    @GetMapping(value = {"{requestId}/print/ttn","{requestId}/print/ttn/"}, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    @Operation(summary = "Создание транспортной накладной в PDF",
               description = "Создание транспортной накладной в PDF")
    byte[] getReportPdfTtn(@PathVariable UUID requestId);
}
