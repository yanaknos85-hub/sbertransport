package ru.sber.transport.dispatcher.service.file_resolvers.vehicle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.dispatcher.dto.enums.VehicleType;
import ru.sber.transport.dispatcher.dto.files.vehicle.VehicleFile;
import ru.sber.transport.dispatcher.dto.files.vehicle.VehicleFileConstants;
import ru.sber.transport.dispatcher.dto.search.VehicleSearchDTO;
import ru.sber.transport.dispatcher.mappers.VehicleFileMapper;
import ru.sber.transport.dispatcher.service.AuthCheckService;
import ru.sber.transport.dispatcher.service.AutoparkService;
import ru.sber.transport.dispatcher.service.VehicleService;
import ru.sber.transport.dispatcher.util.WebParamUtils;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.file_works.importer.DataImporter;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public abstract class VehicleFileResolver<T extends VehicleFile> implements DataExporter<T>, DataImporter<T> {

    private final AuthCheckService authCheckService;
    private final VehicleService vehicleService;
    private final AutoparkService autoparkService;
    private final VehicleFileMapper vehicleFileMapper;
    private final VehicleType vehicleType;

    @Value("${dispatcher-room.admin.role:ROLE_DISPATCHER_ROOM_ADMIN}")
    private String dispatcherRoomAdminRole;

    @Value("${dispatcher-room.federal-dispatcher.role:ROLE_FEDERAL_DISPATCHER_CONTRACTOR}")
    private String federalDispatcherRole;

    @Override
    public void importData(T data, @NotNull Map<String, ?> map, @NotNull JwtAuthenticationToken jwtAuthenticationToken) {
        var contractorId = authCheckService.getContractorIdByToken(jwtAuthenticationToken);

        var newVehicleDTO = vehicleFileMapper.toNewVehicleDto(data);
        var autopark = autoparkService.getByName(data.getAutoParkName());

        vehicleService.add(contractorId, autopark.id(), newVehicleDTO);
    }

    @NotNull
    @Override
    public List<T> exportData(@NotNull Map<String, ?> params, @NotNull JwtAuthenticationToken jwtAuthenticationToken) {
        // Выкачивает пустой шаблон
        if (Boolean.TRUE.equals(WebParamUtils.toBoolean(params.get(VehicleFileConstants.IS_SAMPLE)))) {
            return Collections.emptyList();
        }

        if (params.get(VehicleFileConstants.VEHICLE_TYPE) != null
                && !vehicleType.name().equals(params.get(VehicleFileConstants.VEHICLE_TYPE))) {
            return Collections.emptyList();
        }

        var autoParkId = WebParamUtils.toUUID(params.get(VehicleFileConstants.AUTOPARK_ID));
        var contractorId = getContractorId(jwtAuthenticationToken, WebParamUtils.toUUID(params.get(VehicleFileConstants.CONTRACTOR_ID)));

        VehicleSearchDTO vehicleSearchDTO = new VehicleSearchDTO();
        vehicleSearchDTO.setVehicleType(vehicleType);
        vehicleSearchDTO.setBrand((String) params.get(VehicleFileConstants.BRAND));
        vehicleSearchDTO.setModel((String) params.get(VehicleFileConstants.MODEL));
        vehicleSearchDTO.setStateNumber((String) params.get(VehicleFileConstants.STATE_NUMBER));
        vehicleSearchDTO.setManufactureYear(WebParamUtils.toInteger(params.get(VehicleFileConstants.MANUFACTURE_YEAR)));

        return vehicleService.getAll(contractorId, autoParkId, vehicleSearchDTO)
                .stream()
                .map(vehicleFileMapper::<T>toVehicleFile)
                .toList();
    }

    private UUID getContractorId(JwtAuthenticationToken token, UUID contractorId) {
        if (contractorId == null) {
            return authCheckService.getContractorIdByToken(token);
        }
        Collection<? extends String> roles = (Collection<? extends String>) token.getToken().getClaims().get("roles");
        return roles != null && (roles.contains(dispatcherRoomAdminRole) || roles.contains(federalDispatcherRole)) ? contractorId : authCheckService.getContractorIdByToken(token);
    }
}
