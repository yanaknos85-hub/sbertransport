package fake.database;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.test.database.test.tables.Test;
import ru.sber.transport.test.database.test.tables.records.TestRecord;

import java.util.UUID;

@JaversSpringDataAuditable
public interface TestRepository extends JooqRepository<Test, TestRecord, UUID> {
}
