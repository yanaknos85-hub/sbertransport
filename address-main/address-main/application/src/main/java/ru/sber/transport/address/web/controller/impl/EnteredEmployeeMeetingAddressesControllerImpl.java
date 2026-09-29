package ru.sber.transport.address.web.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.sber.transport.address.business.model.Employee;
import ru.sber.transport.address.business.use_cases.Addresses;
import ru.sber.transport.address.web.mapper.MeetingAddressWebMapper;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.web.api.SelfAddressesMeetingApiDelegate;
import ru.sber.transport.web.model.MeetingAddress;
import ru.sber.transport.web.model.NewMeetingAddress;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of controller of entered employees meeting addresses.
 */
@Component
@RequiredArgsConstructor
public class EnteredEmployeeMeetingAddressesControllerImpl extends BaseAddressControllerImpl
        implements SelfAddressesMeetingApiDelegate {

    private final Addresses<ru.sber.transport.address.business.model.MeetingAddress> addresses;

    private final MeetingAddressWebMapper addressMapper;

    private final EmployeeOrganizationFunction employeeOrganizationFunction;

    @Override
    public ResponseEntity<MeetingAddress> postMeeting(NewMeetingAddress newMeetingAddress) {
        var model = addressMapper.toModel(newMeetingAddress);
        var saved = addresses.save(getOrganizationId(), model);
        return ResponseEntity.ok(addressMapper.toWeb(saved));
    }

    @Override
    public ResponseEntity<Void> putMeeting(NewMeetingAddress newMeetingAddress, UUID id) {
        addresses.save(getOrganizationId(), id, addressMapper.toModel(newMeetingAddress));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteMeeting(UUID id) {
        addresses.delete(getOrganizationId(), id);
        return ResponseEntity.ok().build();
    }

    @CheckOrganizationAccess
    @Override
    public ResponseEntity<List<MeetingAddress>> getAllMeetings(@Organization UUID organizationId) {
        return ResponseEntity.ok(addresses.get(getOrganizationId()).stream()
            .map(addressMapper::toWeb).toList());
    }

    private UUID getOrganizationId() {
        var authenticated = getAuthenticated();
        var organizationId = employeeOrganizationFunction.apply(authenticated);
        if (organizationId == null) {
            throw new EntityNotFoundException(Employee.class, Map.of("userId", authenticated));
        }
        return organizationId;
    }

}
