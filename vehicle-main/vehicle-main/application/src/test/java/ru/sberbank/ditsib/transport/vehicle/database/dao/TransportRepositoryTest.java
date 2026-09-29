package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.vehicle.constants.TransportStatus;
import ru.sberbank.ditsib.transport.vehicle.database.projection.TransportShortProjection;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSearchWithStructureRequestDto;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres
@SpringBootTest
@Sql("/scripts/vehicle_integration_test.sql")
@Sql("/scripts/transport_integration_test.sql")
@Sql("/scripts/indicators_integration_test.sql")
@Sql(value = "/scripts/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TransportRepositoryTest {
    private final SpelAwareProxyProjectionFactory factory = new SpelAwareProxyProjectionFactory();

    private final List<UUID> departmentIds = List.of(UUID.fromString("d728e86f-d624-fbf7-3fe6-fa07f3ead218"),
            UUID.fromString("003a33fb-faa6-49a7-be37-106fdbef2324"),
            UUID.fromString("000098ba-5c12-423f-ba6a-de9c76db31a5"));

    private final PageSettingDto pageSettingDto = new PageSettingDto(0, 10);

    @Autowired
    private TransportRepository transportRepository;

    @Test
    void findTransportWithStructureWithTransportInUse() {
        var projection1 = factory.createProjection(TransportShortProjection.class);
        projection1.setId(UUID.fromString("9ad7733b-c506-4984-b893-56d7022b705b"));
        projection1.setModel("2114");
        projection1.setBrand("Лада");
        projection1.setStateNumber("А111АА111");
        var projection2 = factory.createProjection(TransportShortProjection.class);
        projection2.setId(UUID.fromString("bc3f3b07-f136-4270-af0b-ff84af381ac4"));
        projection2.setModel("2114");
        projection2.setBrand("Лада");
        projection2.setStateNumber("А111АА116");
        var expectedProjections = List.of(projection1, projection2);

        var searchDto = new TransportSearchWithStructureRequestDto(pageSettingDto, "111");
        var pageable = getPageRequest(searchDto);

        var actual = transportRepository.findTransportWithStructure(departmentIds, searchDto.searchText(), pageable);

        assertProjections(actual, expectedProjections);
    }

    @Test
    void findTransportWithStructureWithTransportNotInUse() {
        var projection = factory.createProjection(TransportShortProjection.class);
        projection.setId(UUID.fromString("b83c1f6b-c9ab-4652-8f07-99aa1fa048d1"));
        projection.setModel("V90");
        projection.setBrand("Changan");
        projection.setStateNumber("А777АА78");
        var expectedProjections = List.of(projection);

        var searchDto = new TransportSearchWithStructureRequestDto(pageSettingDto, "777");
        var pageable = getPageRequest(searchDto);

        var actual = transportRepository.findTransportWithStructure(departmentIds, searchDto.searchText(), pageable);

        assertThat(actual).isEmpty();

        var transport = transportRepository.findById(projection.getId()).orElseThrow();
        transport.setStatus(TransportStatus.IN_USE);
        transportRepository.save(transport);

        actual = transportRepository.findTransportWithStructure(departmentIds, searchDto.searchText(), pageable);

        assertProjections(actual, expectedProjections);

        transport = transportRepository.findById(projection.getId()).orElseThrow();
        transport.setStatus(TransportStatus.NOT_IN_USE);
        transportRepository.save(transport);

        actual = transportRepository.findTransportWithStructure(departmentIds, searchDto.searchText(), pageable);

        assertThat(actual).isEmpty();
    }

    @NotNull
    private PageRequest getPageRequest(@NotNull TransportSearchWithStructureRequestDto searchDto) {
        return PageRequest.of(searchDto.page().page(), searchDto.page().size());
    }

    private void assertProjections(@NotNull Page<TransportShortProjection> actual,
                                   @NotNull List<TransportShortProjection> expectedProjections) {
        assertThat(actual).hasSize(expectedProjections.size());
        AtomicInteger i = new AtomicInteger();
        actual.stream().forEach(actualProjection ->
                assertProjection(actualProjection, expectedProjections.get(i.getAndIncrement())));
    }

    private void assertProjection(@NotNull TransportShortProjection actualProjection,
                                  @NotNull TransportShortProjection expectedProjection) {
        assertThat(actualProjection.getId()).isEqualTo(expectedProjection.getId());
        assertThat(actualProjection.getModel()).isEqualTo(expectedProjection.getModel());
        assertThat(actualProjection.getBrand()).isEqualTo(expectedProjection.getBrand());
        assertThat(actualProjection.getStateNumber()).isEqualTo(expectedProjection.getStateNumber());
    }
}
