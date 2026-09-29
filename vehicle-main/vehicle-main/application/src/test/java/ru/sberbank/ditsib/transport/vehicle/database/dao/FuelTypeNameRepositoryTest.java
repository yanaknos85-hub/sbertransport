package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelTypeName;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@EmbeddedPostgres
@SpringBootTest
@Sql({"/scripts/vehicle_integration_test.sql"})
class FuelTypeNameRepositoryTest {

    @Autowired
    private FuelTypeNameRepository repo;

    @Test
    void findByNameIgnoreCaseIn() {
        var actual = repo.findByNameIgnoreCaseIn(List.of("электричество"));
        assertThat(actual)
                .hasSize(1)
                .contains(new FuelTypeName(UUID.fromString("e705ffd8-f158-4455-a005-5ea644614223"),
                        "ЭЛЕКТРИЧЕСТВО"));
    }

}
