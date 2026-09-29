package ru.sber.transport.address.web.resolver;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.address.business.use_cases.MeetingAddresses;
import ru.sber.transport.address.web.resolver.mapper.MeetingAddressFileMapper;
import ru.sber.transport.address.web.resolver.model.MeetingAddressFileDTO;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.file_works.importer.DataImporter;

import java.util.*;

/**
 * Сервис для распознавания и записи информации о филиалах.
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class MeetingAddressResolver implements DataExporter<MeetingAddressFileDTO>, DataImporter<MeetingAddressFileDTO> {

    private final MeetingAddresses addresses;

    private final MeetingAddressFileMapper mapper;

    private final EmployeeOrganizationFunction function;

    @Override
    public void importData(@NonNull MeetingAddressFileDTO item, @NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
        var organizationId = function.apply(UUID.fromString(authentication.getToken().getId()));
        addresses.save(organizationId, mapper.toBusiness(item));
    }

    @Override
    public @NonNull List<MeetingAddressFileDTO> exportData(@NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
        var result = new ArrayList<MeetingAddressFileDTO>();
        for (var meetingAddress : addresses.get()) {
            var resItem = new MeetingAddressFileDTO();
            resItem.setCountry(meetingAddress.getCountry());
            resItem.setRegion(meetingAddress.getRegion());
            resItem.setCity(meetingAddress.getCity());
            resItem.setStreet(meetingAddress.getStreet());
            resItem.setHouse(meetingAddress.getHouse());
            resItem.setBuilding(meetingAddress.getBuilding());
            resItem.setStructure(meetingAddress.getStructure());
            resItem.setLabel(meetingAddress.getLabel());
            result.add(resItem);
        }
        return result;
    }
}
