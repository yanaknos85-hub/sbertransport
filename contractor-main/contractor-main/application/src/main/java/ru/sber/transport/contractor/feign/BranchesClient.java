package ru.sber.transport.contractor.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.contractor.dto.internal.BranchDto;
import ru.sber.transport.contractor.dto.internal.BranchResponseDto;

import java.net.URI;
import java.util.UUID;

@FeignClient(value = "branches", url = "localhost")
public interface BranchesClient {

    @PostMapping(value = "/{contractorId}/autopark/")
    void addBranch(URI baseUrl,
                   @PathVariable("contractorId") UUID id,
                   @RequestBody BranchDto dto,
                   @RequestHeader(name = "Authorization") String authorization);

    @PutMapping(value = "/{contractorId}/autopark/{autoparkId}/")
    void updateBranch(URI baseUrl,
                      @PathVariable("contractorId") UUID id,
                      @PathVariable("autoparkId") UUID autoparkId,
                      @RequestBody BranchDto dto,
                      @RequestHeader(name = "Authorization") String authorization);

    @DeleteMapping(value = "/{contractorId}/autopark/{autoparkId}/")
    void deleteBranch(URI baseUrl,
                      @PathVariable("contractorId") UUID id,
                      @PathVariable("autoparkId") UUID autoparkId,
                      @RequestHeader(name = "Authorization") String authorization);

    @GetMapping(value = "/{contractorId}/autopark/")
    Page<BranchResponseDto> getBranches(URI baseUrl,
                                        @PathVariable("contractorId") UUID id,
                                        @RequestParam(value = "name", required = false) String name,
                                        @RequestParam("page") int page,
                                        @RequestParam("size") int size,
                                        @RequestParam("active") boolean active,
                                        @RequestParam("routingId") UUID routingId,
                                        @RequestHeader(name = "Authorization") String authorization);

}
