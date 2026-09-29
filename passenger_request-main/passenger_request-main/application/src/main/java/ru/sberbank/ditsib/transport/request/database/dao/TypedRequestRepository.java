package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.Request;

import java.util.UUID;

/**
 * Обобщенный интерфейс репозиториев заявок на поездку.
 *
 * @param <T> тип заявки
 */
@NoRepositoryBean
public interface TypedRequestRepository<T extends Request> extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {

    /**
     * Получить тип заявки, который обрабатывается данным репозиторием.
     * @return тип заявки
     */
    TransportTypeEnum type();

}
