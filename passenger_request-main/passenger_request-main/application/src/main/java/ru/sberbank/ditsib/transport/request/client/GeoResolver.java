package ru.sberbank.ditsib.transport.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.request.database.model.Address;
import ru.sberbank.ditsib.transport.request.database.model.RouteRequest;

import java.util.List;

@FeignClient(name = "geo", url = "${feign.url.geo:}")
public interface GeoResolver {
    /**
     * Получение адресов.
     *
     * @param search поисковая строка.
     * @param token токен запроса.
     * @return список адресов.
     */
    @GetMapping("/address")
    List<Address> getAddresses(@RequestParam("location") String search,
                               @RequestHeader("Authorization") String token);
    

    @PostMapping(path = "/route", produces = MediaType.APPLICATION_JSON_VALUE)
    Object getRouters(@RequestBody RouteRequest routeRequest, @RequestHeader("Authorization") String token);
    
}
