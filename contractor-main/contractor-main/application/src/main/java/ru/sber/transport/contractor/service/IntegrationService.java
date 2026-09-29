package ru.sber.transport.contractor.service;

import ru.sber.transport.contractor.database.model.Contractor;
import ru.sber.transport.contractor.database.model.ContractorType;

import java.util.UUID;

public interface IntegrationService {

    /**
     * Добавление нового контрагента.
     *
     * @param contractor    новый контрагент.
     * @param password      пароль от туз
     * @param authorization токен авторизации
     * @param oauthId       id контактного лица в СберТраспорт
     * @return id контрактора во внешней системе
     */
    default UUID add(Contractor contractor, String password, String authorization, UUID oauthId){
        return null;
    }

    /**
     * Edit a contractor.
     *
     * @param id            the identifier of the contractor to be modified.
     * @param contractor    new contractor data.
     * @param authorization authorization token
     */
    default void edit(UUID id, Contractor contractor, String authorization){};

    /**
     * Delete contractor.
     *
     * @param id            ID of contractor to delete.
     * @param authorization токен авторизации
     */
    default void delete(UUID id, String authorization){};

    /**
     * Change contact person of contractor.
     * @param contractor    контрагент
     * @param authorization токен авторизации
     * @param dispatcherId Id диспетчера
     */
    default void changeContactPerson(Contractor contractor, String authorization, UUID dispatcherId){};

    /**
     * Метод интеграции
     *
     * @return метод интеграции
     */
    default ContractorType getType(){
        return null;
    };
}
