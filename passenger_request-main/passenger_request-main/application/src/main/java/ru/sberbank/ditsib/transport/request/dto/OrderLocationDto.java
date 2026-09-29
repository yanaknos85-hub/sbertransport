package ru.sberbank.ditsib.transport.request.dto;

public record OrderLocationDto(
        String orderPartnerId,
        OrderCoordinates coordinates,
        int duration
) {

    public record OrderCoordinates(
            double latitude,
            double longitude
    ) {}
}
