package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.integrations.messaging.OrdersLocationMessage;
import ru.sberbank.ditsib.transport.request.dto.OrderLocationDto;
import ru.sberbank.ditsib.transport.request.dto.OrderLocationDto.OrderCoordinates;

import java.util.List;

@Mapper
public interface CarLocationMapper {

    List<OrderLocationDto> ordersLocationMessageListToOrdersLocationDtoList(List<OrdersLocationMessage.OrderLocationMessage> source);

    @Mapping(target = "coordinates", expression = "java(orderCoordinatesMessageToOrderCoordinates(source.orderLocation()))")
    OrderLocationDto ordersLocationMessageToOrdersLocationDto(OrdersLocationMessage.OrderLocationMessage source);

    OrderCoordinates orderCoordinatesMessageToOrderCoordinates(OrdersLocationMessage.OrderCoordinatesMessage source);
}
