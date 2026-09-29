package ru.sber.transport.token_generator.grpc.providers;

import java.util.List;

/**
 * Провайдер ролей.
 */
public interface RolesProvider {

    /**
     * Проверка наличия флага мастера данных.
     *
     * @param roles список ролей для проверки.
     * @return <code>true</code> если хотя бы одна роль является мастером данных.
     */
    boolean isDataMaster(List<String> roles);

}
