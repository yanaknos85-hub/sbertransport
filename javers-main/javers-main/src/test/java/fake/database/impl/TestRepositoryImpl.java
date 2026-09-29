package fake.database.impl;

import fake.database.TestRepository;
import org.springframework.stereotype.Component;
import ru.sber.transport.test.database.test.tables.Test;

@Component
public class TestRepositoryImpl implements TestRepository {
    @Override
    public Test table() {
        return Test.TEST_;
    }
}
