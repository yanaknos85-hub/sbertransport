package ru.sber.transport.authsb.database.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authsb.api.service.client.RestApiSberBuisness;
import ru.sber.transport.authsb.database.model.Session;
import ru.sber.transport.authsb.services.ServiceController;
import ru.sber.transport.authsb.utils.HashUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@DisplayName("Тест")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(properties = {"logger.level.root=debug", "spring.main.cloud-platform=none"})
@Transactional
@EmbeddedPostgres
class SessionRepositoryTest {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ServiceController sberBusinessIdService;

    @MockBean
    private RestApiSberBuisness restApiSberBuisness;

    @Test
    @DisplayName("Проверка сохранения сессии")
    void test() {
        if (sessionRepository != null) {
            Session session = new Session();
            session.setIdToken("1L");
            session.setActive(true);
            session.setNonce("123");
            session.setState(HashUtils.hashSha256("123"));
            session.setCode("890");
            session.setCreationTime(LocalDateTime.now().plusMinutes(10));
            sessionRepository.save(session);
            var result = sessionRepository.findByStateAndActiveTrue(HashUtils.hashSha256("123"));
            if (result.isPresent()) {
                assertThat(result.get().getState()).isEqualTo(session.getState());
                assertThat(result.get().getNonce()).isEqualTo(session.getNonce());
                assertThat(result.get().getCreationTime()).isEqualTo(session.getCreationTime());
                assertThat(result.get().getCode()).isEqualTo(session.getCode());
                assertThat(result.get().getIdToken()).isEqualTo(session.getIdToken());
                assertThat(result.get().getRefreshToken()).isEqualTo(session.getRefreshToken());
                assertThat(result.get().getActive()).isTrue();
            }
            sessionRepository.findByState(HashUtils.hashSha256("123")).ifPresentOrElse(s -> {
                assertThat(s).isEqualTo(session);
                }, () -> {
                    throw new RuntimeException("Сессия не найдена");
                    });
            }
    }

    @Test
    @DisplayName("Проверка создания сессии")
    void test1() {
        when(restApiSberBuisness.sendForm("authorization_code", "code"))
                .thenReturn(Collections.emptyMap());

        String url = sberBusinessIdService.createUrl("SESSION_ID");

        if (url != null) {
            var result = sessionRepository.findByState(HashUtils.hashSha256("SESSION_ID"));
            assertNotNull(result.get().getNonce());
            assertThat(result.get().getState()).isEqualTo(HashUtils.hashSha256("SESSION_ID"));
            assertThat(result.get().getActive()).isTrue();
            assertThat(result.get().getCode()).isNull();
        }

    }
}