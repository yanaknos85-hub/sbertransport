package ru.sber.transport;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.transport.database.authentication.tables.records.RoleRecord;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public abstract class AbstractContextedTest {

    protected RoleRecord createRole(String code, String description, String name) {
        var role = new RoleRecord();
        role.setCode(code);
        role.setDescription(description);
        role.setName(name);
        return role;
    }

}
