package ru.sberbank.ditsib.transport.srm.service.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.sber.transport.magenta.model.MagentaOrgUpdateInfoRequestDTO;
import ru.sberbank.ditsib.transport.srm.service.MagentaUpdateInfoService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис парсинга XML
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MagentaUpdateInfoServiceImpl implements MagentaUpdateInfoService {

    private final RestTemplate restTemplate;

    @AllArgsConstructor
    private class RunnableTask implements Runnable {

        private MagentaOrgUpdateInfoRequestDTO updateRequest;

        @Override
        public void run() {
            log.info("Update sberfriend data");
            updateInfoActual(updateRequest);
        }

        private HttpHeaders getHeaders() {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            String sb = "%s:%s".formatted(updateInfoLogin, updateInfoPassword);
            final byte[] authBytes = sb.getBytes(StandardCharsets.UTF_8);
            final String authEncoded = Base64.getEncoder().encodeToString(authBytes);
            String auth = "Basic " + authEncoded;
            log.debug("MagentaUpdateInfoService: Authorization = {}", auth);
            httpHeaders.add("Authorization", auth);
            return httpHeaders;
        }

        private void updateInfoActual(MagentaOrgUpdateInfoRequestDTO updateRequest) {
            log.debug("MagentaSrm: start: updateInfoActual input is {}", updateRequest);
            final var httpEntity = new HttpEntity<>(updateRequest, getHeaders());

            restTemplate.exchange(updateInfoUrl, HttpMethod.POST, httpEntity, Object.class);
        }
    }

    @Setter(AccessLevel.PACKAGE)
    @Value("${drug.updateInfoUrl:}")
    private String updateInfoUrl;

    @Setter(AccessLevel.PACKAGE)
    @Value("${drug.updateInfoLogin:}")
    private String updateInfoLogin;

    @Setter(AccessLevel.PACKAGE)
    @Value("${drug.updateInfoPassword:}")
    private String updateInfoPassword;

    @Override
    public CompletableFuture<Void> updateInfo(MagentaOrgUpdateInfoRequestDTO updateRequest) {
        return CompletableFuture.runAsync(new RunnableTask(updateRequest));
    }
}
