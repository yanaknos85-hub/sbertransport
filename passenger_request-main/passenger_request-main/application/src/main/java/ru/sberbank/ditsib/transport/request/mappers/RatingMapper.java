package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.request.messaging.message.TripRatingMessage;
import ru.sberbank.ditsib.transport.request.database.model.RequestRating;

import java.util.UUID;

/**
 * Маппер рейтингов.
 */
@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface RatingMapper {
    
    @Mapping(target = "requestId", source = "requestId")
    @Mapping(target = "advantages", source = "model.advantages")
    @Mapping(target = "drawbacks", source = "model.drawbacks")
    @Mapping(target = "rating", source = "model.rating")
    @Mapping(target = "ratingComment", source = "model.ratingComment")
    TripRatingMessage toMessage(UUID requestId, RequestRating model);
}
