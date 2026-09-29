package ru.sberbank.ditsib.transport.role.service;

import org.springframework.data.domain.Page;
import ru.sber.transport.roles.database.roles.tables.records.RoleRecord;
import ru.sberbank.ditsib.request.Direction;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с ролями.
 */
public interface RoleService {
    
    /**
     * Проверка существования роли.
     *
     * @param code код для проверки.
     * @param name название для проверки.
     * @return <code>true</code> если существуют роли с одним из указанных значений.
     */
    boolean isExists(String code, String name);
    
    /**
     * Добавление роли.
     *
     * @param role роль.
     * @return добавленная роль.
     */
    RoleRecord add(RoleRecord role);
    
    /**
     * Редактирование роли.
     *
     * @param oldEntity старый объект для изменения.
     * @param newEntity новые данные.
     * @return измененная роль.
     */
    RoleRecord edit(RoleRecord oldEntity, RoleRecord newEntity);
    
    /**
     * Удаление роли.
     *
     * @param role роль.
     */
    void delete(RoleRecord role);
    
    /**
     * Получение роли.
     *
     * @param code код роли для получения.
     * @return роль.
     */
    Optional<RoleRecord> get(String code);
    
    /**
     * Получить список ролей.
     *
     * @return список ролей.
     * @param page номер страницы.
     * @param size размер.
     * @param direction направление сортировки.
     * @param name название поля для сортировки.
     */
    Page<RoleRecord> get(int page, int size, Direction direction, String name);

    /**
     * Получить список ролей.
     *
     * @return список ролей.
     */
    List<RoleRecord> get();

}
