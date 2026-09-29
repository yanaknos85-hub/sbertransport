package ru.sber.transport.authentication.business.providers;

import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.RoleDto;

import java.util.ArrayList;
import java.util.Set;

/**
 * Провайдер ролей.
 */
public interface RoleProvider {
    
    /**
     * Установка ролей на УЗ.
     *
     * @param accountDto УЗ.
     * @param roles список ролей.
     */
    void setRoles(AccountDto accountDto, Set<String> roles);
    
    /**
     * Получение ролей УЗ.
     *
     * @param accountDto УЗ.
     * @return список ролей.
     */
    Set<RoleDto> getRoles(AccountDto accountDto);

    /**
     * Получение пространств по умолчанию для ролей УЗ.
     *
     * @param accountDto УЗ.
     * @return список пространств.
     */
    ArrayList<String> getDefaultForList(AccountDto accountDto);
}
