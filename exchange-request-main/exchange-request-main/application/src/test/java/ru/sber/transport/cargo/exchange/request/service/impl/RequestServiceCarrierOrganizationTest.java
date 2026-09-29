package ru.sber.transport.cargo.exchange.request.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.cargo.exchange.request.RequestApplication;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.dto.CarrierRequestDto;
import ru.sber.transport.cargo.exchange.request.dto.CarrierRequestFilterDto;
import ru.sber.transport.cargo.exchange.request.dto.common.FilterComponents;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.service.RequestService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RequestApplication.class)
@EmbeddedPostgres
@ActiveProfiles("test")
@Transactional
@DisplayName("RequestService :: searchRequestsCarrierOrganizationId - Интеграционный тест с использованием RequestSearchForCarrierSpecImpl")
class RequestServiceCarrierOrganizationTest {

    @Autowired
    private RequestService requestService;

    private RequestSearchForCarrierSpecImpl specBuilder;

    private UUID carrierOrgId;

    @BeforeEach
    void setUp() {
        specBuilder = new RequestSearchForCarrierSpecImpl();
        carrierOrgId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380088");
    }

    @Test
    @DisplayName("Должен найти заявки по городу отправления")
    @Sql("/sql/test_data_requests_carrier.sql")
    void shouldFindRequestsByFromCity() {
        // Given
        CarrierRequestFilterDto filter = CarrierRequestFilterDto.builder()
                .addressFrom("Калининград")
                .pageSetting(createPageSetting(0, 10))
                .build();

        Specification<Request> spec = specBuilder.getRequestSearchForCarrierBySpec(filter, carrierOrgId);

        // When
        Page<CarrierRequestDto> result = requestService.searchRequestsCarrierOrganizationId(filter, carrierOrgId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();

        assertThat(result.getContent())
                .allMatch(request ->
                        request.addressFrom().contains("Калининград")
                );
    }

    @Test
    @DisplayName("Должен найти заявки по городу выгрузки")
    @Sql("/sql/test_data_requests_carrier.sql")
    void shouldFindRequestsByToCity() {
        // Given
        CarrierRequestFilterDto filter = CarrierRequestFilterDto.builder()
                .addressTo("Москва")
                .pageSetting(createPageSetting(0, 10))
                .build();

        Page<CarrierRequestDto> result = requestService.searchRequestsCarrierOrganizationId(filter, carrierOrgId);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();

        assertThat(result.getContent())
                .allMatch(request ->
                        request.addressTo().contains("Москва")
                );
    }

    @Test
    @DisplayName("Должен найти заявки по городу отправления и выгрузки одновременно")
    @Sql("/sql/test_data_requests_carrier.sql")
    void shouldFindRequestsByFromCityAndToCity() {
        // Given
        CarrierRequestFilterDto filter = CarrierRequestFilterDto.builder()
                .addressFrom("Калининград")
                .addressTo("Москва")
                .pageSetting(createPageSetting(0, 10))
                .build();


        // When
        Page<CarrierRequestDto> result = requestService.searchRequestsCarrierOrganizationId(filter, carrierOrgId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();

        assertThat(result.getContent())
                .allMatch(request -> {
                    boolean hasLoadFromKaliningrad = request.addressFrom().contains("Калининград");

                    boolean hasUnloadToMoscow = request.addressTo().contains("Москва");

                    return hasLoadFromKaliningrad && hasUnloadToMoscow;
                });
    }

    @Test
    @DisplayName("Должен найти заявки по диапазону дат погрузки")
    @Sql("/sql/test_data_requests_carrier.sql")
    void shouldFindRequestsByLoadingDateRange() {
        // Given
        FilterComponents.DateRange dateRange = new FilterComponents.DateRange(
                LocalDate.of(2026, 2, 4),
                LocalDate.of(2026, 2, 5)
        );

        CarrierRequestFilterDto filter = CarrierRequestFilterDto.builder()
                .loadingDateRange(dateRange)
                .pageSetting(createPageSetting(0, 10))
                .build();

        // When
        Page<CarrierRequestDto> result = requestService.searchRequestsCarrierOrganizationId(filter, carrierOrgId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).hasSize(2);

        assertThat(result.getContent())
                        .anyMatch(r ->
                                !r.loadingDate().isBefore(dateRange.getStart()) &&
                                !r.loadingDate().isAfter(dateRange.getEnd()));
    }

    @Test
    @DisplayName("Должен вернуть пустую страницу при несуществующем городе")
    @Sql("/sql/test_data_requests_carrier.sql")
    void shouldReturnEmptyWhenCityNotFound() {
        // Given
        CarrierRequestFilterDto filter = CarrierRequestFilterDto.builder()
                .addressFrom("НеизвестныйГород")
                .pageSetting(createPageSetting(0, 10))
                .build();

        // When
        Page<CarrierRequestDto> result = requestService.searchRequestsCarrierOrganizationId(filter, carrierOrgId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Должен применять фильтр по статусу заявки")
    @Sql("/sql/test_data_requests_carrier.sql")
    void shouldFilterByStatus() {
        // Given
        List<RequestStatus> statusNames = List.of(
                RequestStatus.CARRIER_SELECTED,
                RequestStatus.PUBLISHED
        );

        CarrierRequestFilterDto filter = CarrierRequestFilterDto.builder()
                .statusSet(statusNames.stream().map(RequestStatus::name).toList())
                .pageSetting(createPageSetting(0, 10))
                .build();

        // When
        Page<CarrierRequestDto> result = requestService.searchRequestsCarrierOrganizationId(filter, carrierOrgId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent())
                .allMatch(req -> statusNames.stream().map(RequestStatus::getDisplayName).toList()
                        .contains(req.status()));
    }

    @Test
    @DisplayName("Должен обрабатывать null-фильтры без ошибок")
    @Sql("/sql/test_data_requests_carrier.sql")
    void shouldHandleNullFilters() {
        // Given
        CarrierRequestFilterDto filter = CarrierRequestFilterDto.builder()
                .pageSetting(createPageSetting(0, 10))
                .build();

        // When
        Page<CarrierRequestDto> result = requestService.searchRequestsCarrierOrganizationId(filter, carrierOrgId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    @DisplayName("Должен найти заявки по диапазону дат выгрузки")
    @Sql("/sql/test_data_requests_carrier.sql")
    void shouldFindRequestsByDeliveryDateRange() {
        // Given
        FilterComponents.DateRange dateRange = new FilterComponents.DateRange(
                LocalDate.of(2026, 2, 7),
                LocalDate.of(2026, 2, 9)
        );

        CarrierRequestFilterDto filter = CarrierRequestFilterDto.builder()
                .deliveryDateRange(dateRange)
                .pageSetting(createPageSetting(0, 10))
                .build();

        FilterComponents.SortSetting sortSetting = new FilterComponents.SortSetting(
                FilterComponents.SortOption.DELIVERY_DATE,
                true
        );
        filter.setSortSetting(sortSetting);

        Page<CarrierRequestDto> result = requestService.searchRequestsCarrierOrganizationId(filter, carrierOrgId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).hasSize(2);

        assertThat(result.getContent())
                .anyMatch(r ->
                        !r.deliveryDate().isBefore(dateRange.getStart()) &&
                                !r.deliveryDate().isAfter(dateRange.getEnd()));

        assertThat(result.getContent())
                .extracting(CarrierRequestDto::deliveryDate)
                .containsExactly( LocalDate.of(2026,2,7), LocalDate.of(2026,2,9));

        filter.setSortSetting(new FilterComponents.SortSetting(
                FilterComponents.SortOption.DELIVERY_DATE,
                false
        ));
        result = requestService.searchRequestsCarrierOrganizationId(filter, carrierOrgId);
        assertThat(result.getContent())
                .extracting(CarrierRequestDto::deliveryDate)
                .containsExactly( LocalDate.of(2026,2,9), LocalDate.of(2026,2,7));

    }

    private FilterComponents.PageSetting createPageSetting(int page, int size) {
        return new FilterComponents.PageSetting(page, size);
    }
}