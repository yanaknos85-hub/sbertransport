package ru.sberbank.ditsib.transport.reports.mappers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import jakarta.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

@SpringBootTest
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
class RequestForXlsxMapperTest extends KafkaTest {
    
    @Autowired
    private RequestForXlsxMapper requestForXlsxMapper;
    
    private static final String EXCEPTION_MESSAGE = "organizationId: не должно равняться null";
    
    @Test
    @DisplayName("Проверка успешного заполнения DTO")
    void testFillDTO() {
        var map = new HashMap<String, Object>();
        map.put("filters", new String(Base64.getEncoder().encode("{\"organizationId\":\"b02cbb7b-74e2-4b25-a15f-51b2f347a538\"}"
                                                                         .getBytes(StandardCharsets.UTF_8))));
        
        var personalDto = requestForXlsxMapper.toPersonalDto(map);
        Assertions.assertNotNull(personalDto);
        Assertions.assertEquals(personalDto.getOrganizationId().toString(), "b02cbb7b-74e2-4b25-a15f-51b2f347a538");
        
        var taxiDto = requestForXlsxMapper.toTaxiDto(map);
        Assertions.assertNotNull(taxiDto);
        Assertions.assertEquals(taxiDto.getOrganizationId().toString(), "b02cbb7b-74e2-4b25-a15f-51b2f347a538");
        
        var publicDto = requestForXlsxMapper.toPublicDto(map);
        Assertions.assertNotNull(publicDto);
        Assertions.assertEquals(publicDto.getOrganizationId().toString(), "b02cbb7b-74e2-4b25-a15f-51b2f347a538");
        
        var carsharingDto = requestForXlsxMapper.toCarsharingDto(map);
        Assertions.assertNotNull(carsharingDto);
        Assertions.assertEquals(carsharingDto.getOrganizationId().toString(), "b02cbb7b-74e2-4b25-a15f-51b2f347a538");
    }
    
    @Test
    @DisplayName("Проверка ошибочного заполнения DTO, отсутствие organizationId")
    @WithMockUser
    void testFillDTOFail() throws Exception {
        var map = new HashMap<String, Object>();
        map.put("filters", new String(Base64.getEncoder().encode("{}".getBytes(StandardCharsets.UTF_8))));
        try {
            requestForXlsxMapper.toTaxiDto(map);
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof ConstraintViolationException);
        }
        
        try {
            requestForXlsxMapper.toPersonalDto(map);
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof ConstraintViolationException);
        }
        
        try {
            requestForXlsxMapper.toPublicDto(map);
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof ConstraintViolationException);
        }
        try {
            requestForXlsxMapper.toCarsharingDto(map);
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof ConstraintViolationException);
        }
    }
}