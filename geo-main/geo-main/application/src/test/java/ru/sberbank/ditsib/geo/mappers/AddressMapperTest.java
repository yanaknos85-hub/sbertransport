package ru.sberbank.ditsib.geo.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.geo.model.Coordinates;
import ru.sberbank.ditsib.geo.model.Segment;
import ru.sberbank.ditsib.geo.model.Segment.SegmentBuilder;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_geo")
@DisplayName("Тестирование маппера адресов")
public class AddressMapperTest {

    private final AddressMapper mapper = new AddressMapperImpl();

    @DisplayName("duration to long")
    @Test
    public void testMapDurationToLong() {
        assertThat(mapper.durationToLong(null)).isEqualTo(null);
        assertThat(mapper.durationToLong(Duration.ofMillis(999111))).isEqualTo(999111);
    }

    @DisplayName("Segment to GeoDescriptor.Segment")
    @Test
    public void testMapSegmentToGeoDescriptorSegment() {
        var coordinates = List.of(new Coordinates(55.74339357458361, 37.54573687155735));
        var segment = Segment.builder()
                .distance(10.001)
                .time(Duration.ofMillis(10002020))
                .coordinates(coordinates)
                .build();
        assertThat(mapper.toSegment(segment).getTime()).isEqualTo(segment.getTime().toMillis());
        assertThat(mapper.toSegment(segment).getDistance()).isEqualTo(10.001);
        assertThat(mapper.toSegment(segment).getCoordinates(0).getLatitude()).isEqualTo(55.74339357458361);
        assertThat(mapper.toSegment(segment).getCoordinates(0).getLongitude()).isEqualTo(37.54573687155735);
    }
}
