package ru.sber.transport.contractor.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.contractor.dto.PatchData;
import ru.sber.transport.contractor.dto.internal.DispatcherStaffDto;
import ru.sber.transport.contractor.dto.internal.DriverStaffDto;
import ru.sber.transport.contractor.dto.internal.StaffDto;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@FeignClient(value = "staff", url = "localhost")
public interface StaffClient {

    @PostMapping(value = "/{contractorId}/dispatcher/")
    void addDispatcher(URI baseUrl,
                       @PathVariable("contractorId") UUID id,
                       @RequestBody DispatcherStaffDto dto,
                       @RequestHeader(name = "Authorization") String authorization);

    @PostMapping(value = "/{contractorId}/drivers/")
    void addDriver(URI baseUrl,
                   @PathVariable("contractorId") UUID id,
                   @RequestBody DriverStaffDto dto,
                   @RequestHeader(name = "Authorization") String authorization);

    @GetMapping(value = "/{contractorId}/dispatcher/")
    Page<StaffDto> getDispatchers(URI baseUrl,
                                  @PathVariable("contractorId") UUID id,
                                  @RequestParam("page") int page,
                                  @RequestParam("size") int size,
                                  @RequestParam("active") boolean active,
                                  @RequestHeader(name = "Authorization") String authorization);

    @GetMapping(value = "/{contractorId}/drivers/")
    Page<StaffDto> getDrivers(URI baseUrl,
                              @PathVariable("contractorId") UUID id,
                              @RequestParam("page") int page,
                              @RequestParam("size") int size,
                              @RequestParam("isActive") boolean active,
                              @RequestHeader(name = "Authorization") String authorization);

    @DeleteMapping(value = "/{contractorId}/drivers/{driverId}/")
    void deleteDriver(URI baseUrl,
                      @PathVariable("contractorId") UUID contractorId,
                      @PathVariable("driverId") UUID driverId,
                      @RequestHeader(name = "Authorization") String authorization);

    @DeleteMapping(value = "/{contractorId}/dispatcher/{dispatcherId}/")
    void deleteDispatcher(URI baseUrl,
                      @PathVariable("contractorId") UUID contractorId,
                      @PathVariable("dispatcherId") UUID driverId,
                      @RequestHeader(name = "Authorization") String authorization);

    @PatchMapping(value = "/{contractorId}/dispatcher/{dispatcherId}/")
    void patchDispatcher(URI baseUrl,
                         @PathVariable("contractorId") UUID contractorId,
                         @PathVariable("dispatcherId") UUID dispatcherId,
                         @RequestBody List<PatchData> dto,
                         @RequestHeader(name = "Authorization") String authorization);

    @PatchMapping(value = "/{contractorId}/drivers/{driverId}/")
    void patchDriver(URI baseUrl,
                     @PathVariable("contractorId") UUID contractorId,
                     @PathVariable("driverId") UUID driverId,
                     @RequestBody List<PatchData> dto,
                     @RequestHeader(name = "Authorization") String authorization);

}
