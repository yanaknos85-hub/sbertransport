package ru.sber.transport.request.external.providers.department;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jdk.jfr.Name;
import org.junit.jupiter.api.Test;
import ru.sber.transport.request.external.model.DepartmentStatus;

class DepartmentStatusMapperTest {

    @Test
    @Name("Проверка маппинга статуса отдела в статус отдела в БД")
    void test_toJooq() {
        for (DepartmentStatus status : DepartmentStatus.values()) {
            ru.sber.transport.database.external_request.enums.DepartmentStatus result = DepartmentStatusMapper.toJooq(status);
            assertEquals(status.name(), result.name());
        }
    }

    @Test
    @Name("Проверка маппинга статуса отдела в статус отдела в БД, когда передан null")
    void test_toJooq_and_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> DepartmentStatusMapper.toJooq(null));
    }
}