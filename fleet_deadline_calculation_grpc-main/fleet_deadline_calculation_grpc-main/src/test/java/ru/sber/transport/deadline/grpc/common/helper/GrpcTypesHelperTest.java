package ru.sber.transport.deadline.grpc.common.helper;

import com.google.protobuf.Timestamp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.deadline.grpc.common.dto.Unit;
import ru.sber.transport.deadline.grpc.dto.GrpcUnit;

import java.time.LocalDate;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проврка вспомогательного класса")
class GrpcTypesHelperTest {
    
    @Test
    void toLocalDateTime() {
        assertThat(GrpcTypesHelper.toLocalDateTime(Timestamp.newBuilder().setSeconds(1701129600L).build()))
                .isEqualTo(LocalDate.parse("2023-11-28").atStartOfDay());
    }
    
    @Test
    void toLocalDate() {
        assertThat(GrpcTypesHelper.toLocalDate(Timestamp.newBuilder().setSeconds(1701129600L).build()))
                .isEqualTo(LocalDate.parse("2023-11-28"));
    }
    
    @Test
    void fromLocalDateTime() {
        assertThat(GrpcTypesHelper.fromLocalDateTime(LocalDate.parse("2023-11-28").atStartOfDay()))
                .isEqualTo(Timestamp.newBuilder().setSeconds(1701129600L).build());
    }
    
    @Test
    void fromLocalDate() {
        assertThat(GrpcTypesHelper.fromLocalDate(LocalDate.parse("2023-11-28")))
                .isEqualTo(Timestamp.newBuilder().setSeconds(1701129600L).build());
    }
    
    @Test
    void convertGrpcUnit() {
        Assertions.assertThat(GrpcUnit.values())
                .filteredOn(not(GrpcUnit.UNRECOGNIZED::equals))
                .hasSameSizeAs(Unit.values());
        
        assertThat(GrpcTypesHelper.convertUnit(GrpcUnit.BUSINESS_DAY))
                .isEqualTo(Unit.BUSINESS_DAY);
        assertThat(GrpcTypesHelper.convertUnit(GrpcUnit.CALENDAR_DAY))
                .isEqualTo(Unit.CALENDAR_DAY);
        assertThat(GrpcTypesHelper.convertUnit(GrpcUnit.BUSINESS_HOUR))
                .isEqualTo(Unit.BUSINESS_HOUR);
    }
    
    @Test
    void convertUnit() {
        assertThat(GrpcUnit.values())
                .filteredOn(not(GrpcUnit.UNRECOGNIZED::equals))
                .hasSameSizeAs(Unit.values());
        
        assertThat(GrpcTypesHelper.convertUnit(Unit.BUSINESS_DAY))
                .isEqualTo(GrpcUnit.BUSINESS_DAY);
        assertThat(GrpcTypesHelper.convertUnit(Unit.CALENDAR_DAY))
                .isEqualTo(GrpcUnit.CALENDAR_DAY);
        assertThat(GrpcTypesHelper.convertUnit(Unit.BUSINESS_HOUR))
                .isEqualTo(GrpcUnit.BUSINESS_HOUR);
    }
    
}