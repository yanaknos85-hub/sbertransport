package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.model.RequestRating;
import ru.sberbank.ditsib.transport.request.messaging.message.TripRatingMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.RequestRatingSender;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка отправки отзыва о поездках в Kafka")
class RequestRatingSenderTest extends KafkaTest {
    
    @Autowired
    private RequestRatingSender sender;
    
    @MockitoBean("requestRatingOutput")
    private OutputBridge requestRatingOutput;
    
    @Test
    @DisplayName("Отправка")
    void test() {
        var requestId = UUID.randomUUID();
        
        var expectedData = new RequestRating();
        expectedData.setRating(1);
        expectedData.setRatingComment("Comment");
        expectedData.getAdvantages().addAll(List.of("advantage1", "advantage2"));
        expectedData.getDrawbacks().addAll(List.of("drawback1", "drawback2", "drawback3"));
        
        sender.send(requestId, expectedData);
        
        final var messageCaptor = ArgumentCaptor.forClass(TripRatingMessage.class);
        verify(requestRatingOutput).send(messageCaptor.capture());
        final var actual = messageCaptor.getValue();
    
        assertThat(actual.getRequestId()).isEqualTo(requestId);
        assertThat(actual.getRating()).isEqualTo(expectedData.getRating());
        assertThat(actual.getRatingComment()).isEqualTo(expectedData.getRatingComment());
        assertThat(actual.getAdvantages()).hasSameSizeAs(expectedData.getAdvantages());
        assertThat(new ArrayList<>(actual.getAdvantages()).get(0))
                .isEqualTo(new ArrayList<>(expectedData.getAdvantages()).get(0));
        assertThat(new ArrayList<>(actual.getAdvantages()).get(1))
                .isEqualTo(new ArrayList<>(expectedData.getAdvantages()).get(1));
        assertThat(actual.getDrawbacks()).hasSameSizeAs(expectedData.getDrawbacks());
        assertThat(new ArrayList<>(actual.getDrawbacks()).get(0))
                .isEqualTo(new ArrayList<>(expectedData.getDrawbacks()).get(0));
        assertThat(new ArrayList<>(actual.getDrawbacks()).get(1))
                .isEqualTo(new ArrayList<>(expectedData.getDrawbacks()).get(1));
        assertThat(new ArrayList<>(actual.getDrawbacks()).get(2))
                .isEqualTo(new ArrayList<>(expectedData.getDrawbacks()).get(2));
    }

}