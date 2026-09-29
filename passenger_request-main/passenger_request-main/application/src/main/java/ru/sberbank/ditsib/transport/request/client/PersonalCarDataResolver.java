package ru.sberbank.ditsib.transport.request.client;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;
import ru.sberbank.ditsib.transport.request.dto.personal.PersonalCarDTO;

import java.util.UUID;

@FeignClient(name = "corporate-service", url = "${feign.url.corporate:}")
public interface PersonalCarDataResolver {

    @GetMapping(value = "/{organizationId}/departments/{departmentId}/employees/{employeeId}/cars/{autoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    PersonalCarDTO getPersonalCar(
            @PathVariable("organizationId") UUID organizationId,
            @PathVariable("departmentId") UUID departmentId,
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("autoId") UUID autoId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token);
    
    @GetMapping(value = "/{driverId}", produces = MediaType.APPLICATION_JSON_VALUE)
    Driver getDriver(
            @PathVariable("driverId") UUID driverId,
            @Parameter(hidden = true) @RequestHeader("Authorization") String token);

}
