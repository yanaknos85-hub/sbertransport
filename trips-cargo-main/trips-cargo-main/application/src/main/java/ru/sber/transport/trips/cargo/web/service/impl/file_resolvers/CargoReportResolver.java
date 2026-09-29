package ru.sber.transport.trips.cargo.web.service.impl.file_resolvers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.function.ThrowingSupplier;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.trips.cargo.business.dto.CargoExportDto;
import ru.sber.transport.trips.cargo.business.dto.TripsExportFiltersDTO;
import ru.sber.transport.trips.cargo.business.model.*;
import ru.sber.transport.trips.cargo.business.providers.TripProvider;
import ru.sber.transport.trips.cargo.messaging.providers.DriverProvider;
import ru.sber.transport.trips.cargo.messaging.providers.VehicleProvider;
import ru.sber.transport.trips.cargo.web.service.AuthCheckService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CargoReportResolver implements DataExporter<CargoExportDto> {

    private final ObjectMapper objectMapper;

    private final TripProvider tripProvider;

    private final DriverProvider driverProvider;

    private final VehicleProvider vehicleProvider;

    private final AuthCheckService authCheckService;

    @Override
    @SneakyThrows
    public List<CargoExportDto> exportData(Map<String, ?> map, JwtAuthenticationToken authentication) {
        TripsExportFiltersDTO filters = Optional.ofNullable(map.get("filters"))
                .map(Object::toString)
                .map(f -> new String(Base64.getDecoder().decode(f), StandardCharsets.UTF_8))
                .map(f -> ThrowingSupplier.of(() -> objectMapper.readValue(f, TripsExportFiltersDTO.class)).get())
                .orElse(new TripsExportFiltersDTO());

        // В отчёт должны включаться только маршруты с финальным статусом "Заказ выполнен"
        filters.setStatuses(List.of(TripStatus.ORDER_FINISHED));

        var dispatcher = authCheckService.dispatcherAuthCheck(null, (JwtAuthenticationToken) authentication);
        var trips = tripProvider.findAllByContractorIdOrderByDigitId(dispatcher == null ? filters.getContractorId() : dispatcher.getContractorId(), filters);

        var result = new ArrayList<CargoExportDto>(trips.size());

        for (int i = 0; i < trips.size(); i++) {
            var item = trips.get(i);

            var driver = Optional.ofNullable(item.getDriverId())
                    .flatMap(driverProvider::get);

            var vehicle = Optional.ofNullable(item.getVehicleId()).flatMap(vehicleProvider::get);

            var requests = Optional.ofNullable(objectMapper.convertValue(item.getRequests(),
                            new TypeReference<List<CargoRequest>>() {}))
                    .orElse(Collections.emptyList());

            var waypoints = item.getWaypoints()
                    .stream()
                    .sorted(Comparator.comparing(Waypoint::orderingIndex))
                    .toList();

            var weight = requests.stream()
                    .filter(c -> c.getWeight() != null)
                    .mapToDouble(CargoRequest::getWeight)
                    .sum();

            var firstAddress = waypoints.stream()
                    .findFirst()
                    .map(this::mapAddress)
                    .orElse(null);

            var lastAddress = waypoints.stream()
                    .skip(waypoints.size() - 1)
                    .findFirst()
                    .map(this::mapAddress)
                    .orElse(null);

            var intermediateAddresses = waypoints.stream().skip(1)
                    .limit(waypoints.size() - 2)
                    .map(this::mapAddress)
                    .collect(Collectors.joining("; "));

            var intermediateWaitTimeHours = Optional.ofNullable(item.getLoadersWorkTime()).map(t -> t / (double) 3_600_000)
                    .map(d -> BigDecimal.valueOf(d).setScale(1, RoundingMode.HALF_UP).doubleValue())
                    .orElse(null);

            result.add(new CargoExportDto(
                    i + 1,
                    driver.flatMap(this::mapFullName).orElse(null),
                    vehicle.map(Vehicle::getStateNumber).orElse(null),
                    item.getRouteHumanReadableId(),
                    Optional.ofNullable(item.getArrivedDate()).map(OffsetDateTime::toLocalDateTime).orElse(null),
                    firstAddress,
                    intermediateAddresses,
                    lastAddress,
                    Optional.ofNullable(item.getStartTime()).map(OffsetDateTime::toLocalDateTime).orElse(null),
                    Optional.ofNullable(item.getFinishTime()).map(OffsetDateTime::toLocalDateTime).orElse(null),
                    item.getFactDistance(),
                    Optional.ofNullable(item.getDriverWaitingTime()).map(Duration::getSeconds).map(t -> t / 60).orElse(null),
                    intermediateWaitTimeHours,
                    weight,
                    item.getCapacity(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ));
        }


        return result;
    }

    private Optional<String> mapFullName(HasName hasName) {
        return Optional.ofNullable(hasName)
                .map(fullName -> Stream.of(
                                fullName.getLastName(),
                                fullName.getFirstName(),
                                fullName.getPatronymic())
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.joining(" "))
                );
    }

    private String mapAddress(Waypoint waypoint) {
        return Optional.ofNullable(waypoint)
                .map(w -> Stream.of(
                                w.region(),
                                w.city(),
                                w.street(),
                                w.house())
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.joining(", "))
                )
                .orElse(null);
    }
}
