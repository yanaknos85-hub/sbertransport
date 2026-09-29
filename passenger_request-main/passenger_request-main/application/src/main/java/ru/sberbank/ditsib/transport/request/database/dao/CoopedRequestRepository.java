package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.repository.NoRepositoryBean;
import ru.sberbank.ditsib.transport.request.database.model.magenta.CoopRequest;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс репозиториев заявок с возможностью совместной поездки
 * @param <T> тип заявки
 */
@NoRepositoryBean
public interface CoopedRequestRepository<T extends CoopRequest> {

    /**
     * Поиск заявка по идентификатору поездки
     *
     * @param rideId идентификатор поездки
     * @return список заявок
     */
    List<T> findByRideId(UUID rideId);

}
