package ru.sber.transport.telemechanic.database.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.projection.DriverByFioProjection;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.dto.driver.DriverSearchRequest;
import ru.sber.transport.telemechanic.dto.driver.DriverSortOption;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@EmbeddedPostgres
@Sql(scripts = {
        "/scripts/cleanup_database.sql",
        "/scripts/basic_corp_structure.sql",
        "/scripts/driver.sql",
        "/scripts/fleet_owner_organization.sql",
        "/scripts/transport.sql"
})
class DriverRepositoryTest {
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Test
    void findByFio() {
        var userOrganizationId = UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396");
        var transportId = UUID.fromString("5fedf8df-d19e-44a0-8528-4e3e81b1e55f");
        var driversByFio = driverRepository.findByFio(userOrganizationId, "нОВ иВанИва", transportId, PageRequest.of(0, 10));
        assertThat(driversByFio).hasSize(1);
        var list = driversByFio.get().toList();
        assertThat(list).hasSize(1)
                        .extracting(
                                DriverByFioProjection::getId,
                                DriverByFioProjection::getPersonnelNumber,
                                DriverByFioProjection::getFullName,
                                DriverByFioProjection::getOrganizationName,
                                DriverByFioProjection::getDepartmentId,
                                DriverByFioProjection::getDepartmentName,
                                DriverByFioProjection::getTin,
                                DriverByFioProjection::getDrivingLicenceId,
                                DriverByFioProjection::getSeries,
                                DriverByFioProjection::getNumber,
                                DriverByFioProjection::getIssueDate
                                   )
                        .containsExactlyInAnyOrder(
                                tuple(
                                        UUID.fromString("0dd8dd1c-04d0-4c1f-9889-2c846881e2ed"),
                                        "2016498",
                                        "Иванов Иван Иванович",
                                        "Тест2",
                                        UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4"),
                                        "Test2",
                                        "0000001",
                                        UUID.fromString("66c38d4c-5f88-42c2-9ae1-a5ea36324d4e"),
                                        "s02",
                                        "1",
                                        LocalDate.parse("2023-01-01")
                                     )
                                                  );
    }
    
    @Test
    void search() {
        var req = new DriverSearchRequest(null, null, null, null,
                                          new PageSettingDto(0, 10),
                                          new DriverSearchRequest.SortSettingDto(DriverSortOption.PERSONNEL_NUMBER, false));
        var search = driverRepository.search(req, req.preparePageRequest());
        assertThat(search).hasSize(2);
        var list = search.get().toList();
        assertThat(list).element(0).satisfies(driver -> {
            assertEquals(UUID.fromString("bc2f5dbf-3585-4b8f-9f37-60ba133e5670"), driver.getId());
            assertEquals("3016497", driver.getPersonnelNumber());
            assertEquals("Александров Александр Александрович", driver.getFullName());
            assertEquals("ЦА", driver.getOrganizationName());
            assertEquals("ПАО «Сбербанк России» (ЦА)", driver.getDepartmentName());
            assertEquals("0000002", driver.getTin());
            assertEquals("559-144-305 02", driver.getSnils());
            assertEquals("0123", driver.getSeries());
            assertEquals("001122", driver.getNumber());
            assertEquals(LocalDate.parse("2020-01-01"), driver.getIssueDate());
            assertEquals(LocalDate.parse("2030-01-01"), driver.getExpiryDate());
            assertFalse(driver.isActive());
            assertEquals(List.of("В", "С1"), driver.getCategoryNames());
        });
        assertThat(list).element(1).satisfies(driver -> {
            assertEquals(UUID.fromString("0dd8dd1c-04d0-4c1f-9889-2c846881e2ed"), driver.getId());
            assertEquals("2016498", driver.getPersonnelNumber());
            assertEquals("Иванов Иван Иванович", driver.getFullName());
            assertEquals("Тест2", driver.getOrganizationName());
            assertEquals("Test2", driver.getDepartmentName());
            assertEquals("0000001", driver.getTin());
            assertEquals("186-345-573 03", driver.getSnils());
            assertEquals("s02", driver.getSeries());
            assertEquals("1", driver.getNumber());
            assertEquals(LocalDate.parse("2023-01-01"), driver.getIssueDate());
            assertEquals(LocalDate.parse("2033-01-01"), driver.getExpiryDate());
            assertTrue(driver.isActive());
            assertEquals(List.of("А", "В", "С1"), driver.getCategoryNames());
        });
        
        var req2 = new DriverSearchRequest(null, null, null, "2016",
                                           new PageSettingDto(0, 1), null);
        var result = driverRepository.search(req2, req2.preparePageRequest());
        assertThat(result).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result).element(0).satisfies(driver -> {
            assertEquals(UUID.fromString("0dd8dd1c-04d0-4c1f-9889-2c846881e2ed"), driver.getId());
            assertEquals("2016498", driver.getPersonnelNumber());
            assertEquals("Иванов Иван Иванович", driver.getFullName());
            assertEquals("Тест2", driver.getOrganizationName());
            assertEquals("Test2", driver.getDepartmentName());
            assertEquals("0000001", driver.getTin());
            assertEquals("186-345-573 03", driver.getSnils());
            assertEquals("s02", driver.getSeries());
            assertEquals("1", driver.getNumber());
            assertEquals(LocalDate.parse("2023-01-01"), driver.getIssueDate());
            assertEquals(LocalDate.parse("2033-01-01"), driver.getExpiryDate());
            assertTrue(driver.isActive());
            assertEquals(List.of("А", "В", "С1"), driver.getCategoryNames());
        });
        
        var req3 = new DriverSearchRequest(null, null, true, null,
                                           null, null);
        
        var actual = driverRepository.search(req3, req3.preparePageRequest());
        assertThat(actual).hasSize(1);
        assertThat(actual).element(0).satisfies(driver -> {
            assertEquals(UUID.fromString("0dd8dd1c-04d0-4c1f-9889-2c846881e2ed"), driver.getId());
            assertEquals("2016498", driver.getPersonnelNumber());
            assertEquals("Иванов Иван Иванович", driver.getFullName());
            assertEquals("Тест2", driver.getOrganizationName());
            assertEquals("Test2", driver.getDepartmentName());
            assertEquals("0000001", driver.getTin());
            assertEquals("186-345-573 03", driver.getSnils());
            assertEquals("s02", driver.getSeries());
            assertEquals("1", driver.getNumber());
            assertEquals(LocalDate.parse("2023-01-01"), driver.getIssueDate());
            assertEquals(LocalDate.parse("2033-01-01"), driver.getExpiryDate());
            assertTrue(driver.isActive());
            assertEquals(List.of("А", "В", "С1"), driver.getCategoryNames());
        });
    }
    
    @Test
    void driverExistsByTinAndSnilsAndActiveEmployeeIdNot() {
        var trueResult = driverRepository.existsByTinAndSnilsAndActiveEmployeeNot("0000003", "112-233-445 01",
                                                                                    UUID.fromString("a9eec4f1-ce45-412f-9403-ba851e4156f8"));
        assertTrue(trueResult);
        
        var falseResult = driverRepository.existsByTinAndSnilsAndActiveEmployeeNot("0000004", "763-285-635 04",
                                                                                     UUID.fromString("a9eec4f1-ce45-412f-9403-ba851e4156f8"));
        assertFalse(falseResult);
    }
}

