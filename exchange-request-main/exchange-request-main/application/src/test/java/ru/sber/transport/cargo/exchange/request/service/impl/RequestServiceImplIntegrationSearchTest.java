package ru.sber.transport.cargo.exchange.request.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.cargo.exchange.request.RequestApplication;
import ru.sber.transport.cargo.exchange.request.database.dao.RequestRepository;
import ru.sber.transport.cargo.exchange.request.dto.MarketplaceRequestDto;
import ru.sber.transport.cargo.exchange.request.dto.MarketplaceRequestFilterDto;
import ru.sber.transport.cargo.exchange.request.dto.common.FilterComponents;
import ru.sber.transport.cargo.exchange.request.service.RequestService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RequestApplication.class)
@ActiveProfiles("test")
@EmbeddedPostgres
@DisplayName("Интеграционный тест: RequestServiceImpl - поиск опубликованных заявок")
@Transactional
class RequestServiceImplIntegrationSearchTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestRepository requestRepository;


    @AfterEach
    @Sql("/sql/clearAll.sql")
    void afterEach() {
        // Очистка после каждого теста
    }

    @Test
    @DisplayName("Должен вернуть пустой результат, если нет опубликованных заявок")
    void shouldReturnEmptyPage_WhenNoPublishedRequests() {
        // given
        MarketplaceRequestFilterDto searchDto = new MarketplaceRequestFilterDto();

        // when
        Page<MarketplaceRequestDto> result = requestService.searchPublishedRequests(searchDto, UUID.randomUUID());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getNumber()).isZero();
    }

    @Test
    @DisplayName("Должен отфильтровать по городу отправления")
    @Sql("/sql/test_data_requests.sql")
    void shouldFilterByFromCity() {
        // given
        MarketplaceRequestFilterDto searchDto = new MarketplaceRequestFilterDto();
        searchDto.setFromCity("санкт-петербург");

        // when
        Page<MarketplaceRequestDto> result = requestService.searchPublishedRequests(searchDto, UUID.randomUUID());

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().addressFrom().toLowerCase())
                .contains("санкт-петербург");
    }

    @Test
    @DisplayName("Должен отфильтровать по городу назначения")
    @Sql("/sql/test_data_requests.sql")
    void shouldFilterByToCity() {
        // given
        MarketplaceRequestFilterDto searchDto = new MarketplaceRequestFilterDto();
        searchDto.setToCity("Москва");

        // when
        Page<MarketplaceRequestDto> result = requestService.searchPublishedRequests(searchDto, UUID.randomUUID());

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().addressTo().toLowerCase())
                .contains("москва");
    }

    @Test
    @DisplayName("Должен отфильтровать по диапазону дат погрузки")
    @Sql("/sql/test_data_requests.sql")
    void shouldFilterByLoadingDateRange() {
        // given
        MarketplaceRequestFilterDto searchDto = new MarketplaceRequestFilterDto();
        searchDto.setLoadingDateRange(new FilterComponents.DateRange(
                LocalDate.of(2026, 2, 2),
                LocalDate.of(2026, 2, 2)
        ));

        // when
        Page<MarketplaceRequestDto> result = requestService.searchPublishedRequests(searchDto, UUID.randomUUID());

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().loadingDate())
                .isEqualTo(LocalDate.of(2026, 2, 2));
    }

    @Test
    @DisplayName("Должен отфильтровать по типу кузова (пересечение массивов)")
    @Sql("/sql/test_data_requests.sql")
    void shouldFilterByVehicleBodyType() {
        // given
        MarketplaceRequestFilterDto searchDto = new MarketplaceRequestFilterDto();
        searchDto.setBodyTypes(Set.of("tarp"));

        // when
        Page<MarketplaceRequestDto> result = requestService.searchPublishedRequests(searchDto, UUID.randomUUID());

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().vehicleBodyType()).contains("tarp");
    }

}