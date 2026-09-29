package ru.sberbank.ditsib.transport.srm.feign;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sberbank.ditsib.transport.srm.dto.twogis.TwoGisMatrixRequestDto;
import ru.sberbank.ditsib.transport.srm.dto.twogis.TwoGisMatrixResponseDto;

/**
 * Черновик Feign клиента для взаимодействия с внешним сервисом
 */
@FeignClient(
        name = "gis-feign-client"
)
public interface GisFeignClient {

    @Headers({
            "accept:application/json",
            "content-type:application/json"
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    TwoGisMatrixResponseDto getDistMatrix(
            @RequestParam("key") String key,
            @RequestParam("version") String version,
            @RequestBody TwoGisMatrixRequestDto twoGisMatrixRequestDto
    );
} 