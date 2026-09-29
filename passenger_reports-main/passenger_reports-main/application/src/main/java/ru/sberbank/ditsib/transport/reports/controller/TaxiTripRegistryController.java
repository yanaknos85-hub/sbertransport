package ru.sberbank.ditsib.transport.reports.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sberbank.ditsib.transport.reports.dto.NewTaxiTripRegistryDTO;
import ru.sberbank.ditsib.transport.reports.dto.RegistryPerContractorDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryShortDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Контроллер для импорта Реестра поездок на такси от контрагентов
 */
@RequestMapping({"xls/import/taxi/registry","xls/import/taxi/registry/"})
@Tag(
        name = "Работа с Реестрами поездок на такси от контрагентов",
        description = "Поддерживаемые расширения файлов: xls, xlsx"
)
@Validated
public interface TaxiTripRegistryController {
    
    @GetMapping({"download/{contractorId}/{year}/{month}","download/{contractorId}/{year}/{month}/"})
    @Operation(summary = "Выгрузка файла с сервера", description = "Выгрузка файла с сервера")
    @ResponseBody
    ResponseEntity<Resource> downloadFile(@PathVariable UUID contractorId,
                                          @PathVariable @Min(2000) @Max(2100) Integer year,
                                          @PathVariable @Min(1) @Max(12) Integer month,
                                          @Parameter(hidden = true) JwtAuthenticationToken authentication);
    
    @GetMapping(path = {"{contractorId}/{date}","{contractorId}/{date}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(
            summary = "Найти загруженный Реестр за конкретную дату. Число в дате может быть любым",
            description = "Найти загруженный Реестр за конкретную дату")
    TaxiTripRegistryDTO findRegistry(@PathVariable UUID contractorId, @PathVariable String date);
    
    @GetMapping(path = {"{contractorId}","{contractorId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(
            summary = "Найти все загруженные Реестры для контрагента. Сортировка - по возрастанию дат",
            description = "Найти все загруженные Реестры для контрагента. Сортировка - по возрастанию дат")
    List<TaxiTripRegistryShortDTO> findAllRegistries(@PathVariable UUID contractorId);
    
    @GetMapping(path = {"all","all/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(
            summary = "Найти все загруженные Реестры всех контрагентов. Сортировка - по возрастанию дат",
            description = "Найти все загруженные Реестры всех контрагентов. Сортировка - по возрастанию дат")
    Set<RegistryPerContractorDTO> findAllRegistries();
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(
            summary = "Импорт Реестра поездок на такси от контрагентов. *Запрос не работает через swagger",
            description = "Импорт Реестра поездок на такси от контрагентов. *Запрос не работает через swagger",
            deprecated = true)
    TaxiTripRegistryDTO importRegistry(
            @Valid @RequestPart("request") NewTaxiTripRegistryDTO registryDTO,
            @RequestPart("file") @Parameter(description = "Файл реестра (.xls или .xlsx)")
                    MultipartFile file);
    
  
    @PostMapping(path = {"{registryId}","{registryId}/"}, produces = MediaType.APPLICATION_JSON_VALUE,
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    @Operation(
            summary = "Замена Реестра поездок на такси от контрагентов",
            description = "Замена Реестра поездок на такси от контрагентов")
    TaxiTripRegistryDTO replaceRegistry(
            @PathVariable UUID registryId,
            @RequestParam(value = "file") @Parameter(description = "полный путь в файловой системе/имя.расширение")
                    MultipartFile file);
}
