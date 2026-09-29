package ru.sberbank.ditsib.transport.srm.mapper;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.srm.model.SrmSharedRide;
import ru.sberbank.ditsib.transport.srm.model.SrmWaypoint;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@UnitTest
@Isolated
@Feature("app_passenger_srm")
@DisplayName("Проверка конвертера сущностей")
class EntityDtoConverterTest {

    private final EntityDTOMapper mapper = new EntityDTOMapperImpl();

    private final EntityDtoConverter converter = new EntityDtoConverter(mapper);

    @Test
    @DisplayName("Проверка конвертера SharedRideDto. Нет путевых точек")
    void test_sharedRideToSharedRideDto_emptyWaypoints() {
        final var source = Instancio.of(SrmSharedRide.class)
                .set(Select.field(SrmSharedRide::getWaypoints), List.of())
                .create();

        final var actual = converter.sharedRideToSharedRideDto(source);

        assertThat(actual).isNotNull();
        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(source.getId());
            it.assertThat(actual.getRideCost()).isEqualTo(source.getRideCost());
            it.assertThat(actual.getRideTime()).isEqualTo(source.getRideTime().longValue());
            it.assertThat(actual.getOldId()).isEqualTo(source.getOldId());
            it.assertThat(actual.getRideDistance()).isEqualTo(source.getRideDistance());
            it.assertThat(actual.getRequestKpiList()).hasSameSizeAs(source.getRequestKpiList());
            it.assertThat(actual.getWaypoints()).isEmpty();
        });
    }

    @Test
    @DisplayName("Проверка конвертера SharedRideDto. Одна путевая точка")
    void test_sharedRideToSharedRideDto_oneWaypoint() {
        final var source = Instancio.of(SrmSharedRide.class)
                .set(Select.field(SrmSharedRide::getWaypoints), List.of(Instancio.create(SrmWaypoint.class)))
                .create();

        final var actual = converter.sharedRideToSharedRideDto(source);

        assertThat(actual).isNotNull();
        assertSoftly(it -> {
            it.assertThat(actual.getId()).isEqualTo(source.getId());
            it.assertThat(actual.getRideCost()).isEqualTo(source.getRideCost());
            it.assertThat(actual.getRideTime()).isEqualTo(source.getRideTime().longValue());
            it.assertThat(actual.getOldId()).isEqualTo(source.getOldId());
            it.assertThat(actual.getRideDistance()).isEqualTo(source.getRideDistance());
            it.assertThat(actual.getRequestKpiList()).hasSameSizeAs(source.getRequestKpiList());
            it.assertThat(actual.getWaypoints()).hasSize(1);
        });
    }

}