package ru.sber.transport.telemechanic.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.database.model.Request;
import ru.sber.transport.telemechanic.database.model.Transport;
import ru.sber.transport.telemechanic.dto.CheckDto;
import ru.sber.transport.telemechanic.dto.MonitoringRequestDto;
import ru.sber.transport.telemechanic.dto.MonitoringRequestListDto;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.enumerate.TransportStatus;

@DisplayName("Тест маппера список проверок")
class RequestMapperTest {

    private final EmployeeMapper employeeMapper = Mappers.getMapper(EmployeeMapper.class);

    private final CheckMapper checkMapper = Mappers.getMapper(CheckMapper.class);

    private final RequestMapper mapper = new RequestMapperImpl(checkMapper, employeeMapper);

    @Test
    void toRequestDto() {

        var check1 = Check.builder()
            .checkStatus(CheckStatus.DONE)
            .id(UUID.randomUUID())
            .checkType(CheckType.OIL_LEVEL)
            .attempt(2)
            .build();

        var check2 = Check.builder()
            .checkStatus(CheckStatus.DECLINE)
            .id(UUID.randomUUID())
            .checkType(CheckType.INSTRUMENT_PANEL)
            .attempt(0)
            .build();

        var checkList = Request.builder()
            .id(UUID.randomUUID())
            .status(RequestStatus.DONE)
            .author(Employee.builder().id(UUID.randomUUID()).build())
            .humanReadableId("OT-998")
            .transport(
                new Transport(UUID.randomUUID(), "rrrr", null, null, 100000, TransportStatus.IN_USE, "-", "-", 40,
                    Set.of(Instancio.create(Organization.class)), null, null, null))
            .build();
        checkList.getChecks().addAll(List.of(check1, check2));

        var dto = mapper.requestToRequestDto(checkList);

        assertNotNull(dto);
        assertEquals(dto.id(), checkList.getId());
        assertEquals(dto.requestStatus(), checkList.getStatus());
        assertEquals(dto.author().id(), checkList.getAuthor().getId());
        assertEquals(dto.humanReadableId(), checkList.getHumanReadableId());
    }

    @Test
    @DisplayName("Проверка преобразования статусов")
    void shouldReturnCorrectStatuses() {
        var statuses = Set.of(Check.builder().id(UUID.randomUUID())
                .checkType(CheckType.VEHICLE_NUMBER)
                .checkStatus(CheckStatus.DONE)
                .attempt(0)
                .build(),
            Check.builder().id(UUID.randomUUID())
                .checkType(CheckType.OIL_LEVEL)
                .checkStatus(CheckStatus.DECLINE)
                .attempt(2)
                .build(),
            Check.builder().id(UUID.randomUUID())
                .checkType(CheckType.INSTRUMENT_PANEL)
                .checkStatus(CheckStatus.IN_PROGRESS)
                .attempt(1)
                .build());
        var actual = mapper.getCheckStatus(statuses);
        assertEquals(1, actual.success());
        assertEquals(1, actual.progress());
        assertEquals(1, actual.decline());
        assertEquals(3, actual.total());
    }

    @Test
    void requestToRequestDetailsInfo() {
        var request = Instancio.create(Request.class);
        var actual = mapper.requestToRequestDetailsInfo(request);

        assertEquals(request.getId(), actual.getId());
        assertEquals(request.getHumanReadableId(), actual.getHumanReadableId());
        assertEquals(request.getCreationTime(), actual.getCreationTime());
        assertEquals(request.getStatus(), actual.getRequestStatus());
        assertEquals(0, actual.getOdometerOut());
        assertFalse(actual.isEwbPath());

        assertEquals(request.getChecks().size(), actual.getChecks().size());
        assertEquals(request.getChecks().stream().map(Check::getCheckType).toList(),
            actual.getChecks().stream().map(CheckDto::checkType).toList());

        assertNull(actual.getEwb());

        assertEquals(request.getAuthor().getId(), actual.getAuthor().getId());
        assertEquals(request.getAuthor().getFirstName(), actual.getAuthor().getFirstName());
        assertEquals(request.getAuthor().getLastName(), actual.getAuthor().getLastName());
        assertEquals(request.getAuthor().getPatronymic(), actual.getAuthor().getPatronymic());
        assertEquals(request.getAuthor().getPersonnelNumber(), actual.getAuthor().getPersonnelNumber());

        assertEquals(request.getTransport().getId(), actual.getTransport().getId());
        assertEquals(request.getTransport().getStateNumber(), actual.getTransport().getStateNumber());
        assertEquals(request.getTransport().getBrand(), actual.getTransport().getBrand());
        assertEquals(request.getTransport().getModel(), actual.getTransport().getModel());
        assertEquals(request.getTransport().getMileage(), actual.getTransport().getMileage());
    }

    @Test
    void requestToActiveResponseTest() {
        var request = Instancio.create(Request.class);
        var actual = mapper.requestToActiveResponse(request);
        assertEquals(request.getId(), actual.id());
    }

    @Test
    void requestToMonitoringRequestListDto() {
        var request = Instancio.create(Request.class);
        var actual = mapper.requestToMonitoringRequestListDto(request);

        assertThat(actual)
            .extracting(
                MonitoringRequestListDto::id,
                MonitoringRequestListDto::humanReadableId,
                MonitoringRequestListDto::creationTime,
                MonitoringRequestListDto::requestStatus,
                MonitoringRequestListDto::stateNumber,
                MonitoringRequestListDto::organizationName,
                MonitoringRequestListDto::checksStatus
            )
            .containsExactly(
                request.getId(),
                request.getHumanReadableId(),
                request.getCreationTime(),
                request.getStatus(),
                request.getTransport().getStateNumber(),
                request.getAuthor().getOrganization().getOfficialName(),
                mapper.getCheckStatus(request.getChecks())
            );
    }

    @Test
    void requestToMonitoringRequestDto() {
        var request = Instancio.create(Request.class);
        var actual = mapper.requestToMonitoringRequestDto(request);

        assertThat(actual)
            .extracting(
                MonitoringRequestDto::id,
                MonitoringRequestDto::humanReadableId,
                MonitoringRequestDto::creationTime,
                MonitoringRequestDto::requestStatus,
                MonitoringRequestDto::author,
                MonitoringRequestDto::transport,
                MonitoringRequestDto::checksStatus,
                MonitoringRequestDto::comment
            )
            .containsExactly(
                request.getId(),
                request.getHumanReadableId(),
                request.getCreationTime(),
                request.getStatus(),
                employeeMapper.employeeToEmployeeDto(request.getAuthor()),
                mapper.transportToTransportDto(request.getTransport()),
                mapper.getCheckStatus(request.getChecks()),
                request.getComment()
                );
    }

}