package ru.sber.transport.telemechanic.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.TinRepository;
import ru.sber.transport.telemechanic.database.model.Tin;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@EmbeddedPostgres
class TinRepositoryTest {
    
    @Autowired
    private TinRepository tinRepository;
    
    private static final UUID EMPLOYEE_ID = UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8");
    
    @Test
    @Sql("/scripts/basic_corp_structure.sql")
    void findByEmployeeId() {
        var optionalTin = tinRepository.findByEmployeeId(EMPLOYEE_ID);
        assertThat(optionalTin).isNotPresent();
        tinRepository.save(new Tin(null, EMPLOYEE_ID, "1234567890"));
        
        optionalTin = tinRepository.findByEmployeeId(EMPLOYEE_ID);
        assertThat(optionalTin).isPresent();
        assertThat(optionalTin.get().getTin()).isEqualTo("1234567890");
    }
}
