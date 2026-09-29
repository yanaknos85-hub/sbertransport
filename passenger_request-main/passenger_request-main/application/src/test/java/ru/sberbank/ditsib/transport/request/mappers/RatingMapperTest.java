package ru.sberbank.ditsib.transport.request.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.database.model.RequestRating;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка маппера рейтинга")
class RatingMapperTest {
    
    private final RatingMapper mapper = new RatingMapperImpl();
    
    @Test
    @DisplayName("Модель в сообщение")
    void test_toMessage() {
        var requestId = UUID.randomUUID();
        
        var expectedData = new RequestRating();
        expectedData.setRating(1);
        expectedData.setRatingComment("Comment");
        expectedData.getAdvantages().addAll(List.of("advantage1", "advantage2"));
        expectedData.getDrawbacks().addAll(List.of("drawback1", "drawback2", "drawback3"));
        
        var actual = mapper.toMessage(requestId, expectedData);
        
        assertThat(actual.getRequestId()).isEqualTo(requestId);
        assertThat(actual.getRating()).isEqualTo(expectedData.getRating());
        assertThat(actual.getRatingComment()).isEqualTo(expectedData.getRatingComment());
        assertThat(actual.getAdvantages().size()).isEqualTo(expectedData.getAdvantages().size());
        assertThat(new ArrayList<>(actual.getAdvantages()).get(0))
                .isEqualTo(new ArrayList<>(expectedData.getAdvantages()).get(0));
        assertThat(new ArrayList<>(actual.getAdvantages()).get(1))
                .isEqualTo(new ArrayList<>(expectedData.getAdvantages()).get(1));
        assertThat(actual.getDrawbacks().size()).isEqualTo(expectedData.getDrawbacks().size());
        assertThat(new ArrayList<>(actual.getDrawbacks()).get(0))
                .isEqualTo(new ArrayList<>(expectedData.getDrawbacks()).get(0));
        assertThat(new ArrayList<>(actual.getDrawbacks()).get(1))
                .isEqualTo(new ArrayList<>(expectedData.getDrawbacks()).get(1));
        assertThat(new ArrayList<>(actual.getDrawbacks()).get(2))
                .isEqualTo(new ArrayList<>(expectedData.getDrawbacks()).get(2));
    }
    
}