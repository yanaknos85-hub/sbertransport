package ru.sberbank.ditsib.mappers;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.database.model.Employee;
import ru.sberbank.ditsib.database.model.Lead;
import ru.sberbank.ditsib.dto.EmployeeDto;
import ru.sberbank.ditsib.dto.lead.LeadExcelDto;
import ru.sberbank.ditsib.dto.lead.LeadRequestDto;
import ru.sberbank.ditsib.dto.point.PointLeadRequestDto;
import ru.sberbank.ditsib.dto.point.PointLeadResponseDto;
import ru.sberbank.ditsib.enumerate.LeadStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.instancio.Select.field;

@DisplayName("тест мапера пользовательских заявок")
class LeadMapperTest {

    private final LeadMapper leadMapper = new LeadMapperImpl(Mappers.getMapper(PointLeadMapper.class),
            Mappers.getMapper(EmployeeMapper.class));

    @Test
    void toDto() {
        var lead = Instancio.create(Lead.class);
        var actual = leadMapper.toDto(lead);

        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(lead.getId());
        assertThat(actual.createDateTime()).isEqualTo(lead.getCreateDateTime());
        assertThat(actual.departureTime()).isEqualTo(lead.getDepartureTime());
        assertThat(actual.comment()).isEqualTo(lead.getComment());
        assertThat(actual.status()).isEqualTo(lead.getStatus());
        assertThat(actual.transportType()).isEqualTo(lead.getTransportType());
        assertThat(actual.tripType()).isEqualTo(lead.getTripType());
        assertThat(actual.employee()).isInstanceOf(EmployeeDto.class);
        assertThat(actual.employee().id()).isEqualTo(lead.getEmployee().getId());
        assertThat(actual.points()).hasSize(lead.getPoints().size());
        assertThat(actual.points().get(0)).isInstanceOf(PointLeadResponseDto.class);
    }

    @Test
    void toLeadRequest() {
        var row = Instancio.of(LeadExcelDto.class)
                .set(field("transportType"), "TAXI")
                .set(field("transportClass"), "COMFORT")
                .set(field("tripType"), "DAYTIME_TRIP")
                .create();
        var from = Instancio.create(PointLeadRequestDto.class);
        var to = Instancio.create(PointLeadRequestDto.class);

        var actual = leadMapper.toLeadRequest(row, from, to);
        assertThat(actual).isNotNull();
        assertThat(actual.departureTime()).isEqualTo(LocalDateTime.of(row.getOrderDate(), row.getOrderTime()));
        assertThat(actual.transportClass().name()).isEqualTo(row.getTransportClass());
        assertThat(actual.comment()).isNull();
        assertThat(actual.isDriver()).isFalse();
        assertThat(actual.transportType().name()).isEqualTo(row.getTransportType());
        assertThat(actual.tripType().name()).isEqualTo(row.getTripType());
        assertThat(actual.points()).hasSize(2);
    }

    @Test
    void toEntity() {
        var dto = Instancio.create(LeadRequestDto.class);
        var employee = Instancio.create(Employee.class);

        var actual = leadMapper.toEntity(dto, employee);

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNull();
        assertThat(actual.getMainLeadId()).isNull();
        assertThat(actual.getCost()).isNull();
        assertThat(actual.getPoints()).isNull();
        assertThat(actual.getCreateDateTime().toLocalDate()).isEqualTo(LocalDate.now());
        assertThat(actual.getDepartureTime()).isEqualTo(dto.departureTime());
        assertThat(actual.getComment()).isEqualTo(dto.comment());
        assertThat(actual.getStatus()).isEqualTo(LeadStatus.PROCESSING);
        assertThat(actual.getTransportClass()).isEqualTo(dto.transportClass());
        assertThat(actual.getTransportType()).isEqualTo(dto.transportType());
        assertThat(actual.getTripType()).isEqualTo(dto.tripType());
        assertThat(actual.getEmployee().getId()).isEqualTo(employee.getId());
    }
}