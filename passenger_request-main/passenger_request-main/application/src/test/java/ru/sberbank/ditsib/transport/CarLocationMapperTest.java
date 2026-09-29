package ru.sberbank.ditsib.transport;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mapstruct.factory.Mappers;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.integrations.messaging.OrdersLocationMessage;
import ru.sberbank.ditsib.transport.request.dto.OrderLocationDto;
import ru.sberbank.ditsib.transport.request.dto.OrderLocationDto.OrderCoordinates;
import ru.sberbank.ditsib.transport.request.mappers.CarLocationMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка маппера локаций транспорта")
class CarLocationMapperTest {

    private final CarLocationMapper carLocationMapper = Mappers.getMapper(CarLocationMapper.class);

    @Test
    void ordersLocationMessageListToOrdersLocationDtoList() {
        var orderLocationMessageList = List.of(
                new OrdersLocationMessage.OrderLocationMessage(
                        "testOrderPartnerId1",
                        new OrdersLocationMessage.OrderCoordinatesMessage(
                                100.0,
                                200.0
                        ),
                        120
                ),
                new OrdersLocationMessage.OrderLocationMessage(
                        "testOrderPartnerId2",
                        new OrdersLocationMessage.OrderCoordinatesMessage(
                                300.0,
                                400.0
                        ),
                        240
                )
        );
        var mappedObj = carLocationMapper.ordersLocationMessageListToOrdersLocationDtoList(orderLocationMessageList);
        assertThat(mappedObj)
                .hasSize(2)
                .usingRecursiveComparison()
                .isEqualTo(
                        List.of(
                                new OrderLocationDto(
                                        "testOrderPartnerId1",
                                        new OrderCoordinates(
                                                100.0,
                                                200.0
                                        ),
                                        120
                                ),
                                new OrderLocationDto(
                                        "testOrderPartnerId2",
                                        new OrderCoordinates(
                                                300.0,
                                                400.0
                                        ),
                                        240
                                )
                        )
                );
    }
}
