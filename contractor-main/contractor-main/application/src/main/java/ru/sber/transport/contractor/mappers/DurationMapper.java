package ru.sber.transport.contractor.mappers;

import org.mapstruct.Mapper;

import java.time.Duration;

@Mapper
public interface DurationMapper {

    default Duration map(Long source) {
        if (source == null) {
            return null;
        }
        return Duration.ofMillis(source);
    }

    default Long map(Duration source) {
        if (source == null) {
            return null;
        }
        return source.toMillis();
    }

}
