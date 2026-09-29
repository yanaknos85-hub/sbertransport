package ru.sber.transport.push.web.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.push.business.dto.PlatformType;
import ru.sber.transport.push.business.dto.TokenData;
import ru.sber.transport.push.database.push.Tables;

import java.util.LinkedHashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Isolated
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@Feature("app_platform_push")
@DisplayName("Проверка контроллера токенов")
@ActiveProfiles("test")
@MockBean(JwtDecoder.class)
public class TokenControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorizationManager<?> manager;

    @Autowired
    private DSLContext dslContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void createData() {
        AuthorizeUtils.authorize(manager);
    }

    @Test
    @DisplayName("Проверка сохранения")
    void test_save() throws Exception {
        var tokenData = Instancio.create(TokenData.class);
        var recipientId = Instancio.create(UUID.class);

        uploadRecord(recipientId, tokenData, null);
    }

    @Test
    @DisplayName("Проверка обновления")
    void test_update() throws Exception {
        var tokenData = Instancio.create(TokenData.class);
        var recipientId = Instancio.create(UUID.class);

        var tokenId = uploadRecord(recipientId, tokenData, null);

        tokenData.setValue(Instancio.create(String.class));
        tokenData.setPlatformType(Instancio.create(PlatformType.class));

        uploadRecord(recipientId, tokenData, tokenId);
    }

    private UUID uploadRecord(UUID recipientId, TokenData tokenData, UUID tokenId) throws Exception {
        var userId = UUID.randomUUID().toString();
        var url = "/token/" + recipientId.toString() + "/";
        if (tokenId != null) {
            url = url + "?tokenId=" + tokenId;
        }

        var response = mockMvc.perform(post(url)
                        .with(jwt().jwt(builder -> builder.jti(userId))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenData))
                ).andExpectAll(status().isOk())
                .andReturn();

        var dataString = response.getResponse().getContentAsString();
        var data = objectMapper.readValue(dataString,
                new TypeReference<LinkedHashMap<String, Object>>() {
                });

        assertNotNull(data.get("tokenId"));
        var newTokenId = UUID.fromString(data.get("tokenId").toString());

        var record = dslContext
                .selectFrom(Tables.TOKEN)
                .where(Tables.TOKEN.ID.eq(newTokenId))
                .fetchOneInto(Tables.TOKEN);

        assertNotNull(record);
        assertEquals(recipientId, record.getRecipientId());
        assertEquals(tokenData.getPlatformType().name(), record.getPlatformType());
        assertEquals(tokenData.getValue(), record.getValue());

        return newTokenId;
    }
}
