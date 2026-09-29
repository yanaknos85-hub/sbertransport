package ru.sber.transport.telemechanic.database.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.model.Dispatcher;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.dto.dispatcher.SearchDispatcherRequest;
import ru.sber.transport.telemechanic.enumerate.DispatcherSortOption;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EmbeddedPostgres
@Sql(scripts = {
        "/scripts/basic_corp_structure.sql",
        "/scripts/dispatcher.sql"
})
@Sql(scripts = "/scripts/cleanup_database.sql",
     executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DispatcherRepositoryTest {
    
    @Autowired
    private DispatcherRepository dispatcherRepository;
    
    @Test
    void findById() {
        var actual = dispatcherRepository.findById(UUID.fromString("983e4049-e4ac-4639-a782-5a266f4321a8"))
                                         .orElseThrow(() -> new JUnitException("Dispatcher not found"));
        assertThat(actual).isNotNull();
        assertThat(actual)
                .extracting(
                        Dispatcher::getId,
                        el -> el.getEmployee().getId(),
                        el -> el.getOrganization().getId(),
                        el -> el.getDepartment().getId(),
                        el -> el.getAttorney().getNumber(),
                        el -> el.getAttorney().getIssueDate(),
                        el -> el.getAttorney().getExpiryDate(),
                        el -> el.getAttorney().getCreationSystem(),
                        Dispatcher::isActive
                           )
                .containsExactly(
                        UUID.fromString("983e4049-e4ac-4639-a782-5a266f4321a8"),
                        UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"),
                        UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"),
                        UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"),
                        UUID.fromString("76a3d37e-636c-4a66-8782-6199fe002f25"),
                        LocalDate.of(2020, 1, 1),
                        LocalDate.of(2030, 1, 1),
                        "sys",
                        true
                                );
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource
    void findAllBySearchFilters(
            String name,
            SearchDispatcherRequest filters,
            int expectedSize,
            List<UUID> expectedIds
                               ) {
        var pageRequest = filters.preparePageRequest();
        var actual = dispatcherRepository.findAllBySearchFilters(filters,
                                                                 pageRequest);
        assertThat(actual.getContent()).hasSize(expectedSize);
        actual.getContent().forEach(el -> assertThat(expectedIds).contains(el.getId()));
    }
    
    @Test
    void findByEmployeeUserIdAndActiveIsTrueAndActiveIsTrue() {
        var actual = dispatcherRepository.findByEmployeeUserIdAndActiveIsTrue(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8"));
        assertThat(actual).hasSize(1).allSatisfy(d -> assertTrue(d.isActive()));
    }
    
    static Stream<Arguments> findAllBySearchFilters() {
        return Stream.of(
                Arguments.of(
                        "без фильтров",
                        new SearchDispatcherRequest(
                                null,
                                null,
                                null,
                                null,
                                new SearchDispatcherRequest.SortSetting(DispatcherSortOption.PERSONNEL_NUMBER, false),
                                new PageSettingDto(0, 10)
                        ),
                        3,
                        List.of(
                                UUID.fromString("983e4049-e4ac-4639-a782-5a266f4321a8"),
                                UUID.fromString("0975c771-059c-4705-aafd-57b6748ff527"),
                                UUID.fromString("e8c396ae-0fba-4a26-a425-0e9073ae71f1")
                               )
                            ),
                Arguments.of(
                        "со всеми фильтрами",
                        new SearchDispatcherRequest(
                                UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"),
                                UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"),
                                false,
                                "3016497",
                                new SearchDispatcherRequest.SortSetting(DispatcherSortOption.ORGANIZATION_NAME, true),
                                new PageSettingDto(0, 20)
                        ),
                        1,
                        List.of(
                                UUID.fromString("0975c771-059c-4705-aafd-57b6748ff527")
                               )
                            )
                        );
    }
}
