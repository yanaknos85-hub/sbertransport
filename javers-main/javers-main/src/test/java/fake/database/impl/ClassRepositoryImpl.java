package fake.database.impl;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.stereotype.Component;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.test.database.test.tables.Test;
import ru.sber.transport.test.database.test.tables.records.TestRecord;

import java.util.UUID;

@JaversSpringDataAuditable
@Component("classRepository")
public class ClassRepositoryImpl implements JooqRepository<Test, TestRecord, UUID> {
    @Override
    public Test table() {
        return Test.TEST_;
    }
}
