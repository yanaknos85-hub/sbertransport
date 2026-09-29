package ru.sber.transport.deadline.grpc.common.helper;

import com.google.protobuf.Timestamp;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import ru.sber.transport.deadline.grpc.common.dto.Unit;
import ru.sber.transport.deadline.grpc.dto.GrpcUnit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GrpcTypesHelper {

    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return LocalDateTime.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos(), ZoneOffset.UTC);
    }

    public static LocalDate toLocalDate(Timestamp timestamp) {
        return toLocalDateTime(timestamp).toLocalDate();
    }

    public static Timestamp fromLocalDateTime(LocalDateTime localDateTime) {
        return Timestamp.newBuilder()
                .setSeconds(localDateTime.toInstant(ZoneOffset.UTC).getEpochSecond())
                .build();
    }

    public static Timestamp fromLocalDate(LocalDate localDate) {
        return fromLocalDateTime(localDate.atStartOfDay());
    }
    
    public static Unit convertUnit(GrpcUnit unit) {
        return Unit.valueOf(unit.name());
    }

    public static GrpcUnit convertUnit(Unit unit) {
        return GrpcUnit.valueOf(unit.name());
    }

}

