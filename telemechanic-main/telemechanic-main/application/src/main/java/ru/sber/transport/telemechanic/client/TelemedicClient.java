package ru.sber.transport.telemechanic.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.telemechanic.dto.telemedicine.TelemedicFirstTitleRequest;

@FeignClient("telemedic")
public interface TelemedicClient {
    
    @PostMapping("sber-transport/t1")
    ResponseEntity<Void> sendFirstTitle(
            @RequestBody TelemedicFirstTitleRequest request,
            @RequestHeader("X-Api-Key") String apiKey
                                       );
}
