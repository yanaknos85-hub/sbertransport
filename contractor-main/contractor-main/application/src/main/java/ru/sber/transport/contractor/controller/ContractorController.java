package ru.sber.transport.contractor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.contractor.database.model.ServiceType;
import ru.sber.transport.contractor.dto.*;
import ru.sber.transport.contractor.dto.enums.ContractorProjection;
import ru.sber.transport.contractor.dto.search.ContractorSearchDTO;
import ru.sber.transport.contractor.dto.search.TransportSearchDTO;

import java.util.List;
import java.util.UUID;

/**
 * Controller for working with contractors.
 */
@RequestMapping("/")
@Tag(name = "Контрагенты", description = "Набор операций для работы с контрагентами")
public interface ContractorController {

    /**
     * Add a new contractor.
     *
     * @param contractor new data of contractor.
     * @return added contractor.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление нового контрагента")
    ContractorDTO add(@RequestBody @Valid NewContractorDTO contractor,
                      @Parameter(description = "Идентификатор организации, к которому требуется привязать контрагента. Игнорируется при работе обычных пользователей. Работает для СМД")
                      @RequestHeader(name = "X-Organization-Id", required = false) UUID organizationId,
                      @Parameter(hidden = true) JwtAuthenticationToken token,
                      HttpServletRequest request);

    /**
     * Edit data about contractor.
     *
     * @param id         ID of contractor.
     * @param contractor new data of contractor.
     */
    @PutMapping(value = "/{contractorId}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение", description = "Изменение данных контрагента")
    void edit(@PathVariable("contractorId") UUID id, @RequestBody @Valid NewContractorDTO contractor,
              @Parameter(description = "Идентификатор организации, к которому требуется привязать контрагента. Игнорируется при работе обычных пользователей. Работает для СМД")
              @RequestHeader(name = "X-Organization-Id", required = false) UUID organizationId,
              @Parameter(hidden = true) JwtAuthenticationToken token,
              HttpServletRequest request);

    /**
     * Delete contractor with ID.
     *
     * @param id ID of contractor to delete.
     */
    @DeleteMapping("/{contractorId}/")
    @Operation(summary = "Удаление", description = "Удаление данных контрагента")
    void delete(@PathVariable("contractorId") UUID id, HttpServletRequest request);

    /**
     * Get contractor by ID.
     *
     * @param id ID of contractor to get data.
     * @return contractor.
     */
    @GetMapping(value = "/{contractorId}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных контрагента")
    ContractorDTO get(@PathVariable("contractorId") UUID id);

    /**
     * Get all contractors.
     *
     * @return get collection with contractors.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех контрагентов")
    Iterable<ContractorDTO> getAll(
            @RequestHeader(name = "X-Paged", required = false) Boolean paged,
            ContractorSearchDTO contractorSearchDTO,
            @Parameter(name = "projection") ContractorProjection projection,
            @Parameter(hidden = true) JwtAuthenticationToken authenticationToken
    );

    /**
     * Редактирование флага автоназначение водителя.
     *
     * @param contractorId contractorId.
     * @param autoassign   flag value.
     */
    @PutMapping(value = "/{contractorId}/{autoassign}/")
    @Operation(summary = "Изменение флага <Автоназначение водителя>",
            description = "Изменение флага <Автоназначение водителя>")
    void editAutoassignFlag(
            @PathVariable("contractorId") UUID contractorId,
            @PathVariable("autoassign") Boolean autoassign
    );

    @PatchMapping(value = "/{contractorId}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение поездки", description = "Частичное изменение данных поездки")
    void patchContractor(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @RequestBody List<PatchData> data,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    );

    @GetMapping(value = "/{contractorId}/transport/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение данных всех свободных автомобилей")
    Object getAllFreeTransport(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            TransportSearchDTO transportSearchDTO
    );

    @GetMapping(value = "/{contractorId}/transport/{vehicleId}/trips/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных о занятости автомобиля")
    Object getTransport(
            @Parameter(description = "Идентификатор контрагента")
            @PathVariable("contractorId") UUID contractorId,
            @Parameter(description = "Идентификатор автомобиля")
            @PathVariable("vehicleId") UUID vehicleId,
            TransportSearchDTO transportSearchDTO
    );

    @GetMapping(value = "/services")
    @Operation(summary = "Получение услуг", description = "Получение списка услуг")
    List<EnumRusNameDTO> getServiceTypes(@RequestParam(name = "specialService", required = false) boolean specialService);

    @GetMapping(value = "/services/{serviceType}/")
    @Operation(summary = "Получение типов контрактора", description = "Получение списка типов контрактора")
    List<EnumRusNameDTO> getContractorTypes(@PathVariable ServiceType serviceType);

    /**
     * Получение нормы автомобилей для внутреннего автопарка.
     * @param organizationId идентификатор организации
     * @param token токен
     * @return норма автомобилей
     */
    @GetMapping(value = "/internal-auto-park/vehicle-norm", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение нормы автомобилей", description = "Получение нормы автомобилей для внутреннего автопарка по идентификатору организации")
    VehicleNormDto getVehicleNorm(@Parameter(description = "Идентификатор организации") @RequestParam("organizationId") UUID organizationId,
                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String token);
}