package ru.sber.transport.dispatcher.testutils;

import ru.sber.transport.dispatcher.database.model.Autopark;
import ru.sber.transport.dispatcher.database.model.Contractor;

public class TestAutoparks {
    public static Autopark createTestAutopark(Contractor contractor) {
        return Autopark.builder()
                .name("Автопарк 0")
                .contractor(contractor)
                .build();
    }
}
