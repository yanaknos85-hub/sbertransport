package ru.sber.transport.driver_track.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.messaging.Message;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сообщение фактической дистанции трипа.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TripFactDistanceMessage implements Message<UUID> {

    /**
     * Идентификатор трипа.
     */
    private UUID id;

    /**
     * Список фактических дистанций.
     */
    private Map<String, Double> distances;
}
