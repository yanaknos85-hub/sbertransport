package ru.sberbank.ditsib;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.service.MainLeadFeignClient;
import ru.sberbank.ditsib.dto.CreateMainLeadRequestDto;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
@EnableWireMock({
        @ConfigureWireMock(name = "main-lead-service-server", property = "main-lead-service.url")
})
class MainLeadServiceIntegrationTest {
    @MockitoBean
    protected AuthorizationManager<?> manager;
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    private MainLeadFeignClient mainLeadFeignClient;
    @InjectWireMock("main-lead-service-server")
    protected WireMockServer mainLeadServiceWiremockServer;

    @BeforeEach
    protected void mockAuthorization() {
        AuthorizeUtils.authorize(manager, "ROLE_ADMIN_DATA_MASTER");
    }

    @SneakyThrows
    @Test
    void predict() {
        var result = mainLeadFeignClient.predict(Instancio.create(CreateMainLeadRequestDto.class));
        assertThat(result.points()).hasSize(2);
    }
}
