package ru.sberbank.transport.oto.cargo.dto.cargo;

/**
 * Data transfer object with data about expected trip data.
 *
 * @param waypointsCount общее количество пунктов маршрута
 */
public record ExpectedCargoDataDTO(
        Integer waypointsCount
) {
}
