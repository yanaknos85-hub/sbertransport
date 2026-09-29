package ru.sberbank.ditsib.transport.srm.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.magenta.model.MagentaOrgUpdateInfoRequestDTO;
import ru.sberbank.ditsib.transport.srm.service.MagentaUpdateInfoService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@UnitTest
@Isolated
@Feature("app_passenger_srm")
@DisplayName("Проверка обновления информации в смежной системе")
class MagentaUpdateInfoServiceImplTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);

    private final MagentaUpdateInfoService service = new MagentaUpdateInfoServiceImpl(restTemplate);

    @Test
    @DisplayName("Проверка обновления информации в смежной системе")
    void test_updateInfo() throws InterruptedException, ExecutionException {
        final var request = Instancio.create(MagentaOrgUpdateInfoRequestDTO.class);

        String url = "url";
        String login = "login";
        String password = "password";

        ((MagentaUpdateInfoServiceImpl) service).setUpdateInfoUrl(url);
        ((MagentaUpdateInfoServiceImpl) service).setUpdateInfoLogin(login);
        ((MagentaUpdateInfoServiceImpl) service).setUpdateInfoPassword(password);

        service.updateInfo(request).get();

        final var requestEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.POST), requestEntityCaptor.capture(), eq(Object.class));

        final var requestEntity = requestEntityCaptor.getValue();
        assertThat(requestEntity).isNotNull();
        assertThat(Objects.requireNonNull(requestEntity.getHeaders().get("Authorization")).get(0)).isEqualTo("Basic %s".formatted(Base64.getEncoder().encodeToString("%s:%s".formatted(login, password).getBytes(StandardCharsets.UTF_8))));
        assertThat(requestEntity.getBody()).isInstanceOf(MagentaOrgUpdateInfoRequestDTO.class);

        final var requestBody = (MagentaOrgUpdateInfoRequestDTO) requestEntity.getBody();
        assertThat(requestBody).isNotNull();
        assertThat(requestBody.getTransportUnitedTrip()).isEqualTo(request.getTransportUnitedTrip());
    }

}