package ru.sber.transport.cargo.exchange.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import ru.sber.transport.cargo.exchange.request.database.model.Request_;
import ru.sber.transport.cargo.exchange.request.dto.common.FilterComponents;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShipperRequestFilterDtoTest {

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        // Given
        UUID organizationId = UUID.randomUUID();
        String humanreadableId = "ОР-202602-0000001";
        Set<RequestStatus> statusSet = Set.of(RequestStatus.DRAFT, RequestStatus.PUBLISHED);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(1);

        FilterComponents.DateRange creationDateRange = new FilterComponents.DateRange(startDate, endDate);
        FilterComponents.DateRange loadingDateRange = new FilterComponents.DateRange(startDate.minusDays(1), startDate);
        FilterComponents.DateRange deliveryDateRange = new FilterComponents.DateRange(endDate, endDate.plusDays(1));

        FilterComponents.SortSetting sortSetting = new FilterComponents.SortSetting(
                FilterComponents.SortOption.LOADING_DATE,
                true
        );

        FilterComponents.PageSetting pageSetting = new FilterComponents.PageSetting(0, 10);

        // When
        ShipperRequestFilterDto dto = new ShipperRequestFilterDto();
        dto.setOrganizationId(organizationId);
        dto.setHumanreadableId(humanreadableId);
        dto.setStatusSet(statusSet);
        dto.setCreationDateRange(creationDateRange);
        dto.setLoadingDateRange(loadingDateRange);
        dto.setDeliveryDateRange(deliveryDateRange);
        dto.setSortSetting(sortSetting);
        dto.setPageSetting(pageSetting);

        // Then
        assertThat(dto.getOrganizationId()).isEqualTo(organizationId);
        assertThat(dto.getHumanreadableId()).isEqualTo(humanreadableId);
        assertThat(dto.getStatusSet()).containsExactlyInAnyOrder(RequestStatus.DRAFT, RequestStatus.PUBLISHED);
        assertThat(dto.getCreationDateRange()).usingRecursiveComparison().isEqualTo(creationDateRange);
        assertThat(dto.getLoadingDateRange()).usingRecursiveComparison().isEqualTo(loadingDateRange);
        assertThat(dto.getDeliveryDateRange()).usingRecursiveComparison().isEqualTo(deliveryDateRange);
        assertThat(dto.getSortSetting()).usingRecursiveComparison().isEqualTo(sortSetting);
        assertThat(dto.getPageSetting()).usingRecursiveComparison().isEqualTo(pageSetting);
    }

    @Test
    void getSort_WhenSortSettingIsNull_ShouldReturnDefaultSortByCreatedAtDesc() {
        // Given
        ShipperRequestFilterDto dto = new ShipperRequestFilterDto();
        dto.setSortSetting(null);

        // When
        Sort sort = dto.getSort();

        // Then
        assertThat(sort).isNotNull();
        assertThat(sort.iterator().next().getProperty()).isEqualTo(Request_.CREATED_AT);
        assertThat(sort.iterator().next().getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getSort_WhenSortingByRequestId_ShouldReturnSortById() {
        // Given
        ShipperRequestFilterDto dto = new ShipperRequestFilterDto();
        dto.setSortSetting(new FilterComponents.SortSetting(
                FilterComponents.SortOption.REQUEST_ID,
                false
        ));

        // When
        Sort sort = dto.getSort();

        // Then
        assertThat(sort.iterator().next().getProperty()).isEqualTo(Request_.ID);
        assertThat(sort.iterator().next().getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getSort_WhenSortingByRequestHumanIdAsc_ShouldReturnSortByHumanreadableIdAsc() {
        // Given
        ShipperRequestFilterDto dto = new ShipperRequestFilterDto();
        dto.setSortSetting(new FilterComponents.SortSetting(
                FilterComponents.SortOption.REQUEST_HUMAN_ID,
                true
        ));

        // When
        Sort sort = dto.getSort();

        // Then
        assertThat(sort.iterator().next().getProperty()).isEqualTo(Request_.HUMAN_READABLE_ID);
        assertThat(sort.iterator().next().getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getSort_WhenSortingByCreationDateDesc_ShouldReturnSortByCreatedAtDesc() {
        // Given
        ShipperRequestFilterDto dto = new ShipperRequestFilterDto();
        dto.setSortSetting(new FilterComponents.SortSetting(
                FilterComponents.SortOption.CREATION_DATE,
                false
        ));

        // When
        Sort sort = dto.getSort();

        // Then
        assertThat(sort.iterator().next().getProperty()).isEqualTo(Request_.CREATED_AT);
        assertThat(sort.iterator().next().getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getSort_WhenSortingByLoadingDateAsc_ShouldReturnSortByLoadingDateAsc() {
        // Given
        ShipperRequestFilterDto dto = new ShipperRequestFilterDto();
        dto.setSortSetting(new FilterComponents.SortSetting(
                FilterComponents.SortOption.LOADING_DATE,
                true
        ));

        // When
        Sort sort = dto.getSort();

        // Then
        assertThat(sort.iterator().next().getProperty()).isEqualTo(Request_.LOADING_DATE);
        assertThat(sort.iterator().next().getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getSort_WhenSortingByDeliveryDateDesc_ShouldReturnSortByDeliveryDateDesc() {
        // Given
        ShipperRequestFilterDto dto = new ShipperRequestFilterDto();
        dto.setSortSetting(new FilterComponents.SortSetting(
                FilterComponents.SortOption.DELIVERY_DATE,
                false
        ));

        // When
        Sort sort = dto.getSort();

        // Then
        assertThat(sort.iterator().next().getProperty()).isEqualTo(Request_.DELIVERY_DATE);
        assertThat(sort.iterator().next().getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}