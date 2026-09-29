package ru.sber.transport.telemechanic.helper;

import lombok.experimental.UtilityClass;

import java.util.Set;

import static ru.sber.transport.telemechanic.enumerate.Role.*;

@UtilityClass
public class CheckRoleHelper {
    
    public boolean checkHaveAllOrganizationsRoleForReport(Set<String> roles) {
        return roles.stream()
                    .anyMatch(role -> Set.of(ROLE_ADMIN_DATA_MASTER.name(),
                                             ROLE_DISPATCHER_SUPPORT_SERVICE.name(),
                                             ROLE_MEDIC.name())
                                         .contains(role));
    }
    
    public boolean checkHaveAllOrganizationsRoleForMonitoring(Set<String> roles) {
        return roles.stream()
                    .anyMatch(role -> Set.of(ROLE_ADMIN_DATA_MASTER.name(),
                                             ROLE_DISPATCHER_SUPPORT_SERVICE.name(),
                                             ROLE_TELEMECHANIC.name())
                                         .contains(role));
    }
}
