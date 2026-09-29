package ru.sberbank.ditsib.transport.srm.feign;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sberbank.ditsib.transport.srm.dto.twogis.TwoGisMatrixAsyncCheckStatusDto;

/**
 * Черновик Feign клиента для взаимодействия с внешним сервисом
 */
@FeignClient(
        name = "gis-async-result-feign-client"
)
public interface GisAsyncResultFeignClient {

    @Headers({
            "accept:application/json",
            "content-type:application/json"
    })
    @GetMapping(value = "{task_id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    TwoGisMatrixAsyncCheckStatusDto getDistMatrix(
            @PathVariable("task_id") String taskId,
            @RequestParam("key") String key
    );
} 