package ru.sber.transport.telemechanic.helper;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_DRIVER;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_MEDIC;

class CheckRoleHelperTest {
    
    @Test
    void checkHaveAllOrganizationsRoleMedicForReport() {
        var result = CheckRoleHelper.checkHaveAllOrganizationsRoleForReport(Set.of(ROLE_MEDIC.name()));
        assertTrue(result);
    }
    
    @Test
    void checkHaveAllOrganizationForbiddenRoleForReport() {
        var result = CheckRoleHelper.checkHaveAllOrganizationsRoleForReport(Set.of(ROLE_DRIVER.name()));
        assertFalse(result);
    }
}