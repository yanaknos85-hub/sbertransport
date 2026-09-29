package ru.sber.transport.dto;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("lib_transport_core")
@DisplayName("Тест объекта страницы")
class PageTest {

    @Test
    @DisplayName("Создание объекта страницы")
    void test_createPage() {
        var list = Instancio.ofList(String.class).create();
        var total = Instancio.create(Integer.class);
        var pageNumber = Instancio.create(Integer.class);
        var pageSize = Instancio.create(Integer.class);
        var direction = Instancio.create(Sort.Direction.class);
        var property = Instancio.create(String.class);

        var pageable = PageRequest.of(pageNumber, pageSize, direction, property);
        var sourcePage = new PageImpl<>(list, pageable, total);

        var actualPage = new Page<>(sourcePage);

        var actualPageData = actualPage.getPageData();
        var actualSortData = actualPage.getSortData();
        var actualContent = actualPage.getContent();

        assertThat(actualPageData).isNotNull();
        assertThat(actualPageData.first()).isEqualTo(sourcePage.isFirst());
        assertThat(actualPageData.last()).isEqualTo(sourcePage.isLast());
        assertThat(actualPageData.totalPages()).isEqualTo(sourcePage.getTotalPages());
        assertThat(actualPageData.size()).isEqualTo(sourcePage.getSize());
        assertThat(actualPageData.numberOfElements()).isEqualTo(sourcePage.getNumberOfElements());
        assertThat(actualPageData.totalElements()).isEqualTo(sourcePage.getTotalElements());

        assertThat(actualSortData).isNotNull();
        assertThat(actualSortData.asc()).isEqualTo(direction.equals(Sort.Direction.ASC));
        assertThat(actualSortData.field()).isEqualTo(property);

        assertThat(actualContent).isNotNull().hasSameElementsAs(list);
    }

    @Test
    @DisplayName("Создание объекта страницы с маппингом")
    void test_createPage_mapping() {
        var list = Instancio.ofList(String.class).create();
        var total = Instancio.create(Integer.class);
        var pageNumber = Instancio.create(Integer.class);
        var pageSize = Instancio.create(Integer.class);
        var direction = Instancio.create(Sort.Direction.class);
        var property = Instancio.create(String.class);

        var pageable = PageRequest.of(pageNumber, pageSize, direction, property);
        var sourcePage = new PageImpl<>(list, pageable, total);

        var actualPage = new Page<>(sourcePage).map(s -> "Mapped " + s);

        var actualPageData = actualPage.getPageData();
        var actualSortData = actualPage.getSortData();
        var actualContent = actualPage.getContent();

        assertThat(actualPageData).isNotNull();
        assertThat(actualPageData.first()).isEqualTo(sourcePage.isFirst());
        assertThat(actualPageData.last()).isEqualTo(sourcePage.isLast());
        assertThat(actualPageData.totalPages()).isEqualTo(sourcePage.getTotalPages());
        assertThat(actualPageData.size()).isEqualTo(sourcePage.getSize());
        assertThat(actualPageData.numberOfElements()).isEqualTo(sourcePage.getNumberOfElements());
        assertThat(actualPageData.totalElements()).isEqualTo(sourcePage.getTotalElements());

        assertThat(actualSortData).isNotNull();
        assertThat(actualSortData.asc()).isEqualTo(direction.equals(Sort.Direction.ASC));
        assertThat(actualSortData.field()).isEqualTo(property);

        assertThat(actualContent).isNotNull().hasSameElementsAs(list.parallelStream().map(s -> "Mapped " + s).toList());
    }

}