package ru.sber.transport.telemechanic.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.mapstruct.Builder;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Request;
import ru.sber.transport.telemechanic.database.model.RequestHistory;
import ru.sber.transport.telemechanic.database.model.Transport;
import ru.sber.transport.telemechanic.dto.ChecksStatus;
import ru.sber.transport.telemechanic.dto.CreatedRequestDto;
import ru.sber.transport.telemechanic.dto.MonitorCheckTreeDto;
import ru.sber.transport.telemechanic.dto.MonitoringCheckDto;
import ru.sber.transport.telemechanic.dto.MonitoringRequestDto;
import ru.sber.transport.telemechanic.dto.MonitoringRequestListDto;
import ru.sber.transport.telemechanic.dto.PatchMonitoringResponse;
import ru.sber.transport.telemechanic.dto.RequestDetailsInfo;
import ru.sber.transport.telemechanic.dto.RequestDto;
import ru.sber.transport.telemechanic.dto.RequestHistoryDto;
import ru.sber.transport.telemechanic.dto.TransportDto;
import ru.sber.transport.telemechanic.dto.request.ActiveResponse;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = {CheckMapper.class, EmployeeMapper.class},
    builder = @Builder(disableBuilder = true))
public interface RequestMapper {

    @Mapping(source = "status", target = "requestStatus")
    @Mapping(source = "transport", target = "vehicle")
    RequestDto requestToRequestDto(Request source);

    @Mapping(target = "requestStatus", source = "status")
    @Mapping(target = "checks", ignore = true)
    CreatedRequestDto requestToCreatedRequestDto(Request source);

    TransportDto transportToTransportDto(Transport source);

    @Mapping(target = "checksStatus", expression = "java(getCheckStatus(source.getChecks()))")
    @Mapping(target = "requestStatus", source = "status")
    @Mapping(target = "stateNumber", source = "transport.stateNumber")
    @Mapping(target = "organizationName", source = "author.organization.officialName")
    MonitoringRequestListDto requestToMonitoringRequestListDto(Request source);

    @Mapping(expression = "java(getCheckStatus(source.getChecks()))", target = "checksStatus")
    @Mapping(target = "checks", ignore = true)
    @Mapping(target = "requestStatus", source = "status")
    MonitoringRequestDto requestToMonitoringRequestDto(Request source);

    @Mapping(expression = "java(getFullNameForEmployee(source))", target = "initiatorName")
    RequestHistoryDto requestHistoryToRequestHistoryDto(RequestHistory source);

    @Mapping(target = "ewbPath", constant = "false")
    @Mapping(target = "requestStatus", source = "status")
    RequestDetailsInfo requestToRequestDetailsInfo(Request source);

    @Mapping(target = "requestStatus", source = "request.status")
    @Mapping(target = "checks", source = "checks")
    PatchMonitoringResponse requestToPatchMonitoringResponse(Request request, List<MonitorCheckTreeDto> checks);

    ActiveResponse requestToActiveResponse(Request source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestId", source = "source.id")
    @Mapping(target = "oldStatus", source = "source.status")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "changeTime", expression = "java(LocalDateTime.now())")
    RequestHistory requestToRequestHistory(Request source, RequestStatus status, String comment, Employee initiator);

    default ChecksStatus getCheckStatus(Set<Check> checks) {
        int total = 0;
        int success = 0;
        int decline = 0;
        int progress = 0;
        for (Check check : checks) {
            if (Objects.isNull(check.getCheckType().getParent())) {
                total++;
                if (check.getCheckStatus().equals(CheckStatus.DONE)) {
                    success++;
                } else if (check.getCheckStatus().equals(CheckStatus.IN_PROGRESS)) {
                    progress++;
                } else if (check.getCheckStatus().equals(CheckStatus.DECLINE)) {
                    decline++;
                }
            }
        }
        return new ChecksStatus(total, success, decline, progress);
    }

    default List<MonitoringCheckDto> getChecks(Set<Check> checks) {
        return checks.stream()
            .sorted(Comparator.comparing((Check check) -> check.getCheckStatus().equals(CheckStatus.DONE))
                .thenComparing(check -> check.getCheckStatus().equals(CheckStatus.IN_PROGRESS))
                .thenComparing(check -> check.getCheckStatus().equals(CheckStatus.DECLINE))
                .thenComparing(check -> check.getCheckType().ordinal()))
            .map(check -> Mappers.getMapper(CheckMapper.class).checkToMonitoringCheckDto(check))
            .toList();
    }

    default String getFullNameForEmployee(RequestHistory source) {
        return source.getInitiator().getLastName() + " "
            + source.getInitiator().getFirstName()
            + (source.getInitiator().getPatronymic() == null ? "" : (" " + source.getInitiator().getPatronymic()));
    }

}