package ru.sber.transport.authentication.web;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.authentication.tables.records.AuditRecord;
import ru.sber.transport.AbstractContextedTest;
import ru.sber.transport.authentication.providers.auditor.dao.AuditRepository;
import ru.sber.transport.authentication.providers.auditor.model.Action;
import ru.sber.transport.authentication.providers.auditor.model.Result;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@AutoConfigureMockMvc
@Transactional
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка аудита")
class AuditRecordsControllerTest extends AbstractContextedTest {
    
    private static final String USER_ID = "95a9ddc6-e62d-4061-9e65-47982ec2cf4c";
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private AuditRepository repository;
    
    @Test
    @DisplayName("Получение записей")
    @WithMockUser(username = USER_ID, roles = "GUEST")
    void test_getRecords() throws Exception {
        var expectedList = new LinkedList<AuditRecord>();
        
        for (var i = 0; i < 100; i++) {
            var audit = repository.save(createAudit(i));
            expectedList.addFirst(audit);
        }
        {
            var result = mockMvc.perform(get("/audit"))
                    .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content.length()").value(expectedList.size()));
            var iterator = expectedList.iterator();
            for (var i = 0; i < 100; i++) {
                var expected = iterator.next();
        
                result.andExpect(jsonPath("$.content.[" + i + "].timestamp").value(expected.getTimestamp().format(
                              DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                      .andExpect(jsonPath("$.content.[" + i + "].action").value(expected.getAction()))
                      .andExpect(jsonPath("$.content.[" + i + "].result").value(expected.getResult()))
                      .andExpect(jsonPath("$.content.[" + i + "].login").value(expected.getLogin()));
            }
        }
        {
            var result = mockMvc.perform(get("/audit?size=20"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content.length()").value(20));
            var iterator = expectedList.iterator();
            for (var i = 0; i < 20; i++) {
                var expected = iterator.next();
        
                result.andExpect(jsonPath("$.content.[" + i + "].timestamp").value(expected.getTimestamp().format(
                              DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                      .andExpect(jsonPath("$.content.[" + i + "].action").value(expected.getAction()))
                      .andExpect(jsonPath("$.content.[" + i + "].result").value(expected.getResult()))
                      .andExpect(jsonPath("$.content.[" + i + "].login").value(expected.getLogin()));
            }
        }
        {
            var result = mockMvc.perform(get("/audit?size=20&page=1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content.length()").value(20));
            var iterator = expectedList.stream().skip(20).iterator();
            for (var i = 0; i < 20; i++) {
                var expected = iterator.next();
        
                result.andExpect(jsonPath("$.content.[" + i + "].timestamp").value(expected.getTimestamp().format(
                              DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                      .andExpect(jsonPath("$.content.[" + i + "].action").value(expected.getAction()))
                      .andExpect(jsonPath("$.content.[" + i + "].result").value(expected.getResult()))
                      .andExpect(jsonPath("$.content.[" + i + "].login").value(expected.getLogin()));
            }
        }
    }

    private AuditRecord createAudit(int i) {
        var audit = new AuditRecord();
        audit.setLogin("Login " + i);
        audit.setResult(Result.values()[i % Result.values().length].name());
        audit.setAction(Action.values()[i % Action.values().length].name());
        audit.setTimestamp(LocalDateTime.of(2000, 1, 1, 0, 0).plusDays(i));
        return audit;
    }

}