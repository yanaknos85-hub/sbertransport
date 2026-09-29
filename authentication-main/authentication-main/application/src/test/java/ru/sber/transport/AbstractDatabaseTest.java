package ru.sber.transport;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.transport.database.authentication.tables.records.RoleRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;

@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractDatabaseTest {

    @Autowired
    protected DSLContext dslContext;

    protected RoleRecord createRole(String code, String description, String name) {
        var role = new RoleRecord();
        role.setCode(code);
        role.setDescription(description);
        role.setName(name);
        return role;
    }

}
