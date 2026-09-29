package ru.sberbank.ditsib.transport.request.messaging.resolvers;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sberbank.ditsib.transport.request.dto.ExecutorGroupDTO;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "platform", url = "${feign.url.corporate:}")
public interface PlatformResolver {

    /**
     * Получение группы исполнителей по id пользователя
     *
     * @param employeeId id пользователя
     *
     * @return DTO с данными группы исполнителей
     */
    @GetMapping(value = "/executorGroup/affiliation/{employeeId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ExecutorGroupDTO getExecutorGroupByEmployeeId(@PathVariable("employeeId") @NotNull UUID employeeId,
                                                  @RequestParam("geoZoneIds") List<UUID> geoZoneIds,
                                                  @Parameter(hidden = true) @RequestHeader("Authorization") String token);
}
