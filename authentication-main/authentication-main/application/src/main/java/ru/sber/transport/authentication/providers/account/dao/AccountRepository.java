package ru.sber.transport.authentication.providers.account.dao;

import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.authentication.tables.Account;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с УЗ.
 */
public interface AccountRepository extends JooqRepository<Account, AccountRecord, UUID> {
    
    /**
     * Получение активной УЗ по логину.
     *
     * @param login логин.
     *
     * @return УЗ.
     */
    Optional<AccountRecord> findByActiveTrueAndLogin(String login);
    
    /**
     * Получение активной УЗ по логину.
     *
     * @param userId идентификатор пользователя.
     *
     * @return УЗ.
     */
    Optional<AccountRecord> findByActiveTrueAndId(UUID userId);
    
    /**
     * Получить УЗ по логину.
     *
     * @param login логин.
     * @return УЗ.
     */
    AccountRecord getByLogin(String login);

    /**
     * Получить количество похожих логинов. Похожесть определяется оператором like.
     *
     * @param login логин.
     * @return количество похожих лошгинов.
     */
    int countByLoginLike(String login);

    /**
     * Получение активных УЗ по идентификаторам.
     *
     * @param id идентификаторы.
     * @return УЗ.
     */
    Collection<AccountRecord> findAllByActiveTrueAndIdIn(Collection<UUID> id);

    /**
     * Получение активных УЗ по логинам.
     *
     * @param logins логины.
     * @return УЗ.
     */
    Collection<AccountRecord> findAllByActiveTrueAndLoginIn(List<String> logins);

    /**
     * Получение активной УЗ по почте.
     *
     * @param email почта.
     * @return УЗ.
     */
    Optional<AccountRecord> findAllByActiveIsTrueAndEmail(String email);
}
