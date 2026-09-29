package ru.sberbank.ditsib.transport.request.human_readable_id.service;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.human_readable_id.dao.CompanySQRepositoryRequest;

import jakarta.validation.ConstraintViolationException;
import ru.sberbank.ditsib.transport.request.human_readable_id.model.Prefix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@AutoConfigureMockMvc
@MockitoBean(types = JwtDecoder.class)
class SQCreatorImplTest extends KafkaTest {
    
    @Autowired
    @Qualifier("sQGeneratorHR")
    SQGenerator sqGenerator;
    
    @Autowired
    CompanySQRepositoryRequest companySQRepository;
    
    @AfterEach
    public void tearDown(){
        companySQRepository.deleteAll();
        companySQRepository.flush();
    }
    
    @Test
    void getNextId() {
        
        assertEquals("OT-0001-00000001", sqGenerator.getNextId(Prefix.OT, 1L));
        assertEquals("OT-0001-00000002", sqGenerator.getNextId(Prefix.OT, 1L));
        assertEquals("OT-9999-00000001", sqGenerator.getNextId(Prefix.OT, 9999L));
        
    }
    
    
    @Test
    void getNextId_corporateClientId_LessMin() {
        Exception exception =
                assertThrows(ConstraintViolationException.class, () -> sqGenerator.getNextId(Prefix.LD, -1L));
        String actualMessage = exception.getMessage();
        assertThat(actualMessage)
                .startsWith("getNextId.organizationId: ")
                .endsWith(" 1")
        ;
    }

    @Test
    void getNextId_corporateClientId_MoreMax() {
        Exception exception =
                assertThrows(ConstraintViolationException.class, () -> sqGenerator.getNextId(Prefix.LD,
                                                                                             10000L));
        String actualMessage = exception.getMessage();
        assertThat(actualMessage)
                .startsWith("getNextId.organizationId: ")
                .endsWith(" 9999")
        ;
    }
    
}