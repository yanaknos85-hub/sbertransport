package ru.sber.transport.dispatcher.database.dao;

import lombok.NonNull;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.sber.transport.dispatcher.database.model.Dispatcher;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Репозиторий диспетчеров.
 */
public interface DispatcherRepository extends JpaRepository<Dispatcher, UUID>, JpaSpecificationExecutor<Dispatcher> {

    /**
     * Получение всех диспетчеров контрагента.
     *
     * @param contractorId идентификатор контрагента.
     * @param active признак активности.
     * @param page параметры страницы.
     * @return диспетчеры.
     */
    @Query("SELECT disp FROM Dispatcher disp WHERE disp.contractor.id = :contractorId AND disp.active = :active")
    Page<Dispatcher> findAllByContractorIdAndActiveOrderByFirstNameAndPatronymicAndLastName(UUID contractorId, boolean active, Pageable page);

    /**
     * Получение базовой информации всех диспетчеров контрагента.
     *
     * @param contractorId идентификатор контрагента.
     * @param active признак активности.
     * @return диспетчеры.
     */
    @Query("SELECT disp FROM Dispatcher disp WHERE disp.contractor.id = :contractorId AND disp.active = :active ORDER BY disp.firstName, disp.patronymic, disp.lastName")
    List<Dispatcher> findAllByContractorIdAndActiveOrderByFirstNameAndPatronymicAndLastName(UUID contractorId, boolean active);

    /**
     * Поиск по диспетчера контрагента.
     *
     * @param contractorId идентификатор контрагента.
     * @param dispatcherId идентификатор диспетчера.
     * @return диспетчер.
     */
    Optional<Dispatcher> findByContractorIdAndId(@NonNull UUID contractorId, @NonNull UUID dispatcherId);

    /**
     * Поиск активного диспетчера по телефону.
     *
     * @param phone телефон.
     * @return диспетчер.
     */
    Optional<Dispatcher> findByPhoneAndActive(String phone, boolean active);

    /**
     * Поиск активного диспетчера по email.
     *
     * @param email email.
     * @return диспетчер.
     */
    Optional<Dispatcher> findByEmailAndActive(String email, boolean active);

    /**
     * Поиск диспетчера по телефону.
     *
     * @param phone телефон.
     * @param exclude исключение из поиска.
     * @return диспетчер.
     */
    Optional<Dispatcher> findByPhoneAndActiveTrueAndIdNot(String phone, UUID exclude);

    /**
     * Поиск диспетчера по почте.
     *
     * @param email E-Mail.
     * @param exclude исключение из поиска.
     * @return диспетчер.
     */
    Optional<Dispatcher> findByEmailAndActiveTrueAndIdNot(String email, UUID exclude);

    /**
     * Поиск списка диспетчером по идентификатору контрагента.
     *
     * @param contractorId идентификатор контрагента.
     * @return диспетчеры.
     */
    List<Dispatcher> findAllByContractorId(UUID contractorId);

    /**
     * Поиск списка диспетчеров по идентификатору контрагента и списку идентификаторов.
     *
     * @param contractorId идентификатор контрагента.
     * @param ids список идентификаторов
     * @return диспетчеры.
     */
    List<Dispatcher> findAllByContractorIdAndIdIn(UUID contractorId, Set<UUID> ids);

    /**
     * Посчитать количество диспетчеров контрагента.
     *
     * @param contractorId идентификатор контрагента.
     * @return число.
     */
    int countAllByContractorId(UUID contractorId);

    /**
     * Поиск диспетчера по идентификатору OAuth.
     * @param oauthId идентификатор OAuth.
     * @return диспетчер.
     */
    Optional<Dispatcher> findByOauthId(UUID oauthId);

    /**
     * Поиск диспетчера по идентификатору OAuth и ID контрагента.
     * @param oauthId идентификатор OAuth.
     * @param contractorId идентификатор контрагента.
     * @return диспетчер.
     */
    Optional<Dispatcher> findByOauthIdAndContractorId(UUID oauthId, UUID contractorId);

    /**
     * Поиск диспетчера по номеру доверенности и активности
     * @param attorneyNumber номер доверенности.
     * @param active признак активности..
     * @return диспетчер.
     */
    Optional<Dispatcher> findByAttorneyNumberAndActive(String attorneyNumber, boolean active);
}
