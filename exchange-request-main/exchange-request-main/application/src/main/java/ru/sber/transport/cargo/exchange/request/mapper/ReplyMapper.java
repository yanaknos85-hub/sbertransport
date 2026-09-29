package ru.sber.transport.cargo.exchange.request.mapper;

import org.mapstruct.*;
import ru.sber.transport.cargo.exchange.request.database.model.CarrierReply;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyDto;
import ru.sber.transport.cargo.exchange.request.dto.CarrierReplyShortDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface ReplyMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "request.id", source = "requestId")
    @Mapping(target = "organization.id", source = "organizationId")
    @Mapping(target = "reply", source = "source")
    @Mapping(target = "createdAt", expression = "java(now())")
    CarrierReply toModel(UUID requestId, UUID organizationId, CarrierReplyDto source);

    @Mapping(target = ".", source = "source.reply")
    CarrierReplyDto toDto(CarrierReply source);

    @Mapping(target = "carrier", source = "source.organization.name")
    @Mapping(target = "phone", source = "source.organization.contactPhone")
    @Mapping(target = "rate", constant = "4.5")
    @Mapping(target = "auto.type", source = "source.reply.auto", qualifiedByName = "toAutoType")
    @Mapping(target = "auto.name", source = "source.reply.auto", qualifiedByName = "toAutoName")
    @Mapping(target = "cost", source = "source.reply.cost")
    @Mapping(target = "comment", source = "source.reply.comment")
    CarrierReplyShortDto toShortDto(CarrierReply source);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<CarrierReplyShortDto> toShortDtos(List<CarrierReply> source);

    @Named("toAutoType")
    default String toAutoType(CarrierReplyDto.Auto auto) {
        return Optional.ofNullable(auto)
                .map(a -> Stream.of(a.mark(), a.year())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" ")))
                .orElse(null);
    }

    @Named("toAutoName")
    default String toAutoName(CarrierReplyDto.Auto auto) {
        if (auto == null) {
            return null;
        }

        return auto.name();
    }

    default LocalDateTime now() {
        return LocalDateTime.now();
    }
}
