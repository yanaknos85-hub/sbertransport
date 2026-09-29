package ru.sber.transport.telemechanic.database.dao;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.MedicRequestRepository;
import ru.sber.transport.telemechanic.database.model.MedicRequest_;
import ru.sber.transport.telemechanic.dto.telemedicine.TelemedicineSearchResponse;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;
import ru.sber.transport.telemechanic.mapper.TelemedicineMapper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres
@SpringBootTest
@Sql({ "/scripts/cleanup_database.sql",
       "/scripts/basic_corp_structure.sql",
       "/scripts/ewb_integration_test.sql" })
class MedicRequestRepositoryTest {
    
    @Autowired
    private MedicRequestRepository medicRequestRepository;
    @Autowired
    private TelemedicineMapper telemedicineMapper;
    
    @MethodSource
    @ParameterizedTest
    void searchMedicRequests(
            UUID userId,
            String searchText,
            UUID organizationId,
            Set<String> requestStatusSet,
            LocalDateTime creationTimeStart,
            LocalDateTime creationTimeEnd,
            LocalDateTime medicDecisionTimeStart,
            LocalDateTime medicDecisionTimeEnd,
            PageRequest pageRequest,
            int totalElements,
            int size,
            int totalPages,
            int pageNumber,
            int numberOfElements,
            Sort sort,
            List<TelemedicineSearchResponse> expected
                            ) {
        var actual = medicRequestRepository.searchMedicRequests(userId,
                                                                searchText,
                                                                organizationId,
                                                                requestStatusSet,
                                                                creationTimeStart,
                                                                creationTimeEnd,
                                                                medicDecisionTimeStart,
                                                                medicDecisionTimeEnd,
                                                                pageRequest);
        assertThat(actual.getTotalElements()).isEqualTo(totalElements);
        assertThat(actual.getSize()).isEqualTo(size);
        assertThat(actual.getTotalPages()).isEqualTo(totalPages);
        assertThat(actual.getNumber()).isEqualTo(pageNumber);
        assertThat(actual.getNumberOfElements()).isEqualTo(numberOfElements);
        assertThat(actual.getSort()).isEqualTo(sort);
        assertThat(actual.getPageable()).isEqualTo(pageRequest);
        assertThat(telemedicineMapper.listTelemedicineSearchProjectionToListTelemedicineSearchResponse(actual.getContent()))
                .usingRecursiveComparison(RecursiveComparisonConfiguration
                                                  .builder()
                                                  .withComparatorForType(Comparator.comparing((LocalDateTime o) -> o.truncatedTo(ChronoUnit.SECONDS)),
                                                                         LocalDateTime.class)
                                                  .build())
                .isEqualTo(expected);
    }
    
    static Stream<Arguments> searchMedicRequests() {
        var response1 = new TelemedicineSearchResponse(
                UUID.fromString("6c157938-3604-42a0-bc86-f16526031d5b"),
                "TL-0000-00000000",
                UUID.fromString("660184ec-a2be-4340-b482-cb57082567a8"),
                "EWB_ID10",
                "ЦА",
                TelemedicineStatus.IN_PROGRESS.name(),
                LocalDateTime.of(2024, 7, 31, 13, 11, 39, 0),
                "Клюнков Василий Александрович");
        var response2 = new TelemedicineSearchResponse(
                UUID.fromString("75542a8a-b815-43d6-9a33-63046b19f030"),
                "TL-0000-00000001",
                UUID.fromString("d87dfe9a-5915-4d37-855f-a1598d4819c3"),
                "EWB_ID2",
                "ЦА",
                TelemedicineStatus.DONE.name(),
                LocalDateTime.of(2024, 7, 31, 13, 11, 39, 0),
                "Клюнков Василий Александрович");
        var response3 = new TelemedicineSearchResponse(
                UUID.fromString("5456bbff-1f45-4f9a-90d6-85304513d54c"),
                "TL-0000-00000002",
                UUID.fromString("a4f3e1c8-fd4b-4088-9c2d-9d36490e08a7"),
                "EWB_ID4",
                "ЦА",
                TelemedicineStatus.DECLINED.name(),
                LocalDateTime.of(2024, 7, 31, 13, 11, 39, 0),
                "Клюнков Василий Александрович");
        var response4 = new TelemedicineSearchResponse(
                UUID.fromString("b5a6b2cf-06d3-41f9-9571-43e86ffffa71"),
                "TL-0008-00000005",
                UUID.fromString("140e4734-4909-4bf3-b98f-f24c90d8005f"),
                "EWB_ID1",
                "ЦА",
                TelemedicineStatus.IN_PROGRESS.name(),
                LocalDateTime.of(2024, 10, 10, 10, 47, 11, 977),
                "Клюнков Василий Александрович");
        var response5 = new TelemedicineSearchResponse(
                UUID.fromString("8ad5e9c6-604f-4baa-861f-d7d4c320eb72"),
                "TL-0000-00000003",
                UUID.fromString("29d4ef98-51fa-43b3-b3ed-a9870ad4b659"),
                "EWB_ID5",
                "Тест2",
                TelemedicineStatus.DECLINED.name(),
                LocalDateTime.of(2024, 7, 31, 13, 11, 39, 0),
                "Клюнков Василий Александрович");
        var response6 = new TelemedicineSearchResponse(
                UUID.fromString("8ad5e9c6-604f-4baa-861f-d7d4c320eb72"),
                "TL-0000-00000003",
                UUID.fromString("8800a83d-e9a1-4fc9-b85d-be1221fd294e"),
                "EWB_ID6",
                "Тест2",
                TelemedicineStatus.DECLINED.name(),
                LocalDateTime.of(2024, 7, 31, 13, 11, 39, 0),
                "Клюнков Василий Александрович");
        return Stream.of(
                noFilters(),
                userIdFilter(List.of(response1, response4, response2, response3, response5, response6)),
                searchTextFilter(List.of(response1, response4, response2, response3, response5, response6)),
                organizationIdFilter(List.of(response5, response6)),
                requestStatusSetFilter(List.of(response1, response4, response2)),
                creationTimeFilter(List.of(response1, response2, response3, response5, response6)),
                medicDecisionTimeFilter(List.of(response1, response2, response3, response5, response6)),
                paginationFilter(List.of(response2, response3))
                        );
    }
    
    @NotNull
    private static Arguments noFilters() {
        return Arguments.of(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 20, Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME))),
                0,
                20,
                0,
                0,
                0,
                Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME)),
                Collections.emptyList());
    }
    
    @NotNull
    private static Arguments userIdFilter(List<TelemedicineSearchResponse> expected) {
        return Arguments.of(
                UUID.fromString("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 20, Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME))),
                6,
                20,
                1,
                0,
                6,
                Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME)),
                expected);
    }
    
    @NotNull
    private static Arguments searchTextFilter(List<TelemedicineSearchResponse> expected) {
        return Arguments.of(
                UUID.fromString("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"),
                "нков",
                null,
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 20, Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME))),
                6,
                20,
                1,
                0,
                6,
                Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME)),
                expected);
    }
    
    @NotNull
    private static Arguments organizationIdFilter(List<TelemedicineSearchResponse> expected) {
        return Arguments.of(
                UUID.fromString("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"),
                null,
                UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"),
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 20, Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME))),
                2,
                20,
                1,
                0,
                2,
                Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME)),
                expected);
    }
    
    @NotNull
    private static Arguments requestStatusSetFilter(List<TelemedicineSearchResponse> expected) {
        return Arguments.of(
                UUID.fromString("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"),
                null,
                null,
                Set.of(TelemedicineStatus.DONE.name(), TelemedicineStatus.IN_PROGRESS.name()),
                null,
                null,
                null,
                null,
                PageRequest.of(0, 20, Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME))),
                3,
                20,
                1,
                0,
                3,
                Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME)),
                expected);
    }
    
    @NotNull
    private static Arguments creationTimeFilter(List<TelemedicineSearchResponse> expected) {
        return Arguments.of(
                UUID.fromString("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"),
                null,
                null,
                null,
                LocalDateTime.of(2024, 7, 1, 10, 0, 0, 0),
                LocalDateTime.of(2024, 8, 1, 10, 0, 0, 0),
                null,
                null,
                PageRequest.of(0, 20, Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME))),
                5,
                20,
                1,
                0,
                5,
                Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME)),
                expected);
    }
    
    @NotNull
    private static Arguments medicDecisionTimeFilter(List<TelemedicineSearchResponse> expected) {
        return Arguments.of(
                UUID.fromString("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"),
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2024, 7, 1, 10, 0, 0, 0),
                LocalDateTime.of(2024, 8, 1, 10, 0, 0, 0),
                PageRequest.of(0, 20, Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME))),
                5,
                20,
                1,
                0,
                5,
                Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME)),
                expected);
    }
    
    @NotNull
    private static Arguments paginationFilter(List<TelemedicineSearchResponse> expected) {
        return Arguments.of(
                UUID.fromString("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(1, 2, Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME))),
                6,
                2,
                3,
                1,
                2,
                Sort.by(MedicRequest_.STATUS).descending().and(Sort.by(MedicRequest_.CREATION_TIME)),
                expected);
    }
}