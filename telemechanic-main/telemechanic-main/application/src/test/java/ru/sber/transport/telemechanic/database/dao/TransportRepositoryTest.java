package ru.sber.transport.telemechanic.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.TransportRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedPostgres
class TransportRepositoryTest {
    
    @Autowired
    private TransportRepository transportRepository;
    
    private final UUID organizationId1 = UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6");
    private final UUID organizationId2 = UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396");
    
    @Test
    @Sql(value = { "/scripts/cleanup_database.sql",
                   "/scripts/basic_corp_structure.sql",
                   "/scripts/transport.sql" })
    void findAllByStateNumberInOrganization() {
        var pageRequest = PageRequest.of(0, 10);
        
        var result1 = transportRepository.findAllByStateNumber("а77", organizationId1, pageRequest);
        assertEquals(2, result1.getTotalElements());
        assertEquals("А777АА777", result1.getContent().get(0).getStateNumber());
        assertEquals("А777АА778", result1.getContent().get(1).getStateNumber());
        
        var result2 = transportRepository.findAllByStateNumber(null, organizationId1, pageRequest);
        assertEquals(2, result2.getTotalElements());
        assertEquals("А777АА777", result2.getContent().get(0).getStateNumber());
        assertEquals("А777АА778", result2.getContent().get(1).getStateNumber());
        
        var result3 = transportRepository.findAllByStateNumber("778", organizationId1, pageRequest);
        assertEquals(1, result3.getTotalElements());
        assertEquals("А777АА778", result3.getContent().get(0).getStateNumber());
        
        var result4 = transportRepository.findAllByStateNumber("777", organizationId2, pageRequest);
        assertEquals(1, result4.getTotalElements());
        assertEquals("А779АА777", result4.getContent().get(0).getStateNumber());
        
        var result5 = transportRepository.findAllByStateNumber(null, null, pageRequest);
        assertEquals(4, result5.getTotalElements());
        assertEquals("А777АА777", result5.getContent().get(0).getStateNumber());
        assertEquals("А777АА778", result5.getContent().get(1).getStateNumber());
        assertEquals("А779АА777", result5.getContent().get(2).getStateNumber());
        assertEquals("А779АА779", result5.getContent().get(3).getStateNumber());
    }
}
