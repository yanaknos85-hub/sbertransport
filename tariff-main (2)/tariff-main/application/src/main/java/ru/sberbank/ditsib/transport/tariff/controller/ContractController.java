package ru.sberbank.ditsib.transport.tariff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.ContractType;
import ru.sberbank.ditsib.transport.tariff.dto.ContractDTO;
import ru.sberbank.ditsib.transport.tariff.dto.ContractSearchDTO;
import ru.sberbank.ditsib.transport.tariff.dto.GetContractDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.UUID;

/**
 * Controller interface for contracts.
 */
@RequestMapping(value = {"contracts","contracts/"})
@Tag(name = "Договора контрагентов", description = "Набор операций для договоров контрагентов")
public interface ContractController {
    
    /**
     * Add new contract.
     *
     * @param contractDTO new contract data.
     *
     * @return added contract.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление нового договора (только для админов)",
               description = "Добавление нового договора (только для админов)")
    GetContractDTO add(
            @Valid @RequestBody ContractDTO contractDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                      );
    
    /**
     * Edit contract.
     *
     * @param contractId new data of contract.
     * @param contractDTO new data of contract.
     */
    @PutMapping(value = {"{contractId}","{contractId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение данных договора", description = "Изменение данных договора")
    void edit(
            @PathVariable("contractId") UUID contractId,
            @Valid @RequestBody ContractDTO contractDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
             );
    
    /**
     * Delete contract.
     *
     * @param contractId ID of contract to delete.
     */
    @DeleteMapping(value = {"{contractId}","{contractId}/"})
    @Operation(summary = "Удаление данных договора", description = "Удаление данных договора")
    void delete(
            @PathVariable("contractId") UUID contractId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
               );
    
    /**
     * Get contract with ID.
     *
     * @param contractId ID of contract to get.
     *
     * @return contract.
     */
    @GetMapping(value = "{contractId:^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение данных договора", description = "Получение данных договора")
    GetContractDTO get(@PathVariable("contractId") @NotNull UUID contractId);
    
    /**
     * Get all unique uvhd of contractor.
     *
     * @param contractorId id of contractor.
     * @param uvhd substring uvhd of contract.
     * @param pageable pagination.
     *
     * @return page of uvhd.
     */
    @ResponseBody
    @GetMapping(value = {"uvhd","uvhd/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение договоров УВХД", description = "Получение договоров УВХД для переданного контрагента")
    Page<String> getUniqueUvhd(
            @RequestParam(name = "contractorId") @NotNull UUID contractorId,
            @RequestParam(name = "uvhd", required = false) String uvhd,
            @RequestParam(name = "transportType", required = false) TransportTypeEnum transportType,
            Pageable pageable);
    
    /**
     * Get all contracts.
     *
     * @param contractType тип договора.
     *
     * @return list of contracts.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех договоров", description = "Получение всех договоров")
    Collection<? extends GetContractDTO> getAll(@RequestParam(name = "contractType", defaultValue = "TRANSITIONAL") @NotNull ContractType contractType);
    
    @PostMapping(value = {"search","search/"}, produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск контакта по фильтру", description = "Поиск контакта по фильтру")
    Page<? extends GetContractDTO> search(
            @RequestBody ContractSearchDTO searchDTO,
            @PageableDefault(size = 20, sort = { "contractNumber" }, direction =
                    Sort.Direction.ASC) Pageable pageable
                                         );
}
