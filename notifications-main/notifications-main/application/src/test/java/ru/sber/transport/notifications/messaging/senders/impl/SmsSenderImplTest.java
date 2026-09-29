package ru.sber.transport.notifications.messaging.senders.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.messages.sms.avro.Data;
import ru.sber.transport.messages.sms.avro.DataList;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.SmsMessage;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("Проверка отправки SMS")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class SmsSenderImplTest {

    private final OutputBridge ob = mock(OutputBridge.class);

    private final ObjectProvider<OutputBridge> bridge = new ObjectProvider<>() {
        @NotNull
        @Override
        public OutputBridge getObject(@NotNull Object... args) throws BeansException {
            return ob;
        }

        @Override
        public OutputBridge getIfAvailable() throws BeansException {
            return ob;
        }

        @Override
        public OutputBridge getIfUnique() throws BeansException {
            return ob;
        }

        @NotNull
        @Override
        public OutputBridge getObject() throws BeansException {
            return ob;
        }
    };

    @Test
    @DisplayName("Отправка SMS")
    void test_send() {
        final var smsSender = new SmsSenderImpl(bridge);

        var token1 = Instancio.create(String.class);
        var token2 = Instancio.create(String.class);
        var token3 = Instancio.create(String.class);

        smsSender.send(List.of(token1, token2, token3), "Template",
                Map.of("1", 1,
                        "2", true,
                        "3", 3L,
                        "4", 4D,
                        "5", "5",
                        "6", "6".getBytes(),
                        "7", List.of("8", Map.of("9", false)),
                "10", List.of(
                        Map.of("city", "Minsk", "street", "Mira", "house", "10"),
                        Map.of("city", "Minsk2", "street", "Mira2", "house", "102")
                        ),
                        "11", Map.of("12", Map.of("13", "14"))
                )
        );

        var actualAvro = ArgumentCaptor.forClass(ru.sber.transport.messages.sms.avro.SmsMessage.class);

        verify(ob).send(actualAvro.capture());

        final var avro = actualAvro.getValue();

        assertThat(avro.getId()).isNotNull();
        assertThat(avro.getPhones()).hasSize(3).hasSameElementsAs(List.of(token1, token2, token3));
        assertThat(avro.getTemplate()).isEqualTo("Template");
        assertThat(avro.getData().getValues()).hasSize(9);
        assertThat(avro.getData().getValues().parallelStream().filter(it -> it.getKey().equals("1")).map(Data::getValue)).contains(1);
        assertThat(avro.getData().getValues().parallelStream().filter(it -> it.getKey().equals("2")).map(Data::getValue)).contains(true);
        assertThat(avro.getData().getValues().parallelStream().filter(it -> it.getKey().equals("3")).map(Data::getValue)).contains(3L);
        assertThat(avro.getData().getValues().parallelStream().filter(it -> it.getKey().equals("4")).map(Data::getValue)).contains(4D);
        assertThat(avro.getData().getValues().parallelStream().filter(it -> it.getKey().equals("5")).map(Data::getValue)).contains("5");
        assertThat(avro.getData().getValues().parallelStream().filter(it -> it.getKey().equals("6")).map(Data::getValue)).contains("6".getBytes());

        final var rawList = avro.getData().getValues().parallelStream().filter(it -> it.getKey().equals("7")).map(Data::getValue).findFirst().orElseThrow();
        assertThat(rawList).isInstanceOf(DataList.class);
        final var list = (DataList) rawList;
        assertThat(list.getValues().get(0)).isEqualTo(Data.newBuilder().setKey("0").setValue("8").build());

        final var actualSecond = list.getValues().get(1);
        assertThat(actualSecond).isInstanceOf(Data.class);
        assertThat(actualSecond.getKey()).isEqualTo("1");
        assertThat(actualSecond.getValue()).isEqualTo(DataList.newBuilder().setValues(List.of(Data.newBuilder().setKey("9").setValue(false).build())).build());

        final var rawMapActual = avro.getData().getValues().parallelStream().filter(it -> it.getKey().equals("11")).map(Data::getValue).findFirst().orElseThrow();
        assertThat(rawMapActual).isInstanceOf(DataList.class);
        final var mapActual = (DataList) rawMapActual;
        assertThat(mapActual.getValues().getFirst().getKey()).isEqualTo("12");
        assertThat(mapActual.getValues().getFirst().getValue()).isEqualTo(DataList.newBuilder().setValues(List.of(Data.newBuilder().setKey("13").setValue("14").build())).build());
    }

}