package ru.sber.transport.contractor.feign;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.contractor.dto.PatchData;
import ru.sber.transport.contractor.dto.VehicleNormDto;
import ru.sber.transport.contractor.dto.internal.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@FeignClient(value = "internal", url = "localhost")
public interface InternalClient {
    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    DispatcherResponseDTO add(URI baseUrl,
                              @RequestBody InternalContractorRequestDto contractor,
                              @RequestHeader(name = "Authorization") String authorization);

    @PutMapping(value = "/{contractorId}/", consumes = MediaType.APPLICATION_JSON_VALUE)
    void edit(URI baseUrl,
              @PathVariable("contractorId") UUID id,
              @RequestBody InternalContractorRequestDto contractor,
              @RequestHeader(name = "Authorization") String authorization);

    @DeleteMapping("/{contractorId}/")
    void delete(URI baseUrl,
                @PathVariable("contractorId") UUID id,
                @RequestHeader(name = "Authorization") String authorization);

    @PostMapping(value = "/link")
    @Operation(summary = "Связать контрагентов", description = "Связывание контрагентов между разными истансами")
    void linkContractor(URI baseUrl, @RequestBody LinkRequestDTO requestDto);

    @PatchMapping(value = "/{contractorId}/")
    void patchContractor(URI baseUrl,
                         @PathVariable("contractorId") UUID id,
                         @RequestBody List<PatchData> data,
                         @RequestHeader(name = "Authorization") String authorization);

    @GetMapping(value = "/{contractorId}/vehicle-norm")
    VehicleNormDto getVehicleNorm(URI baseUrl,
                                  @Parameter(description = "Идентификатор контрагента")
                                  @PathVariable("contractorId") UUID contractorId,
                                  @RequestHeader(name = "Authorization") String authorization);
}
