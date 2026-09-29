package ru.sber.transport.cargo.exchange.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.cargo.exchange.request.database.model.CarrierReply;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReplyRepository extends JpaRepository<CarrierReply, UUID> {

    @Query(value = "select c from CarrierReply c " +
            "join fetch c.organization " +
            "where c.request.id = :requestId")
    List<CarrierReply> findAllByRequestId(UUID requestId);

    Optional<CarrierReply> findByRequestIdAndOrganizationId(UUID requestId, UUID organizationId);

    /**
     * Проверяет, существует ли отклик для заданной заявки и организации перевозчика.
     *
     * @param requestId        идентификатор заявки
     * @param organizationId   идентификатор организации перевозчика
     * @return true, если отклик существует; false — в противном случае
     */
    boolean existsByRequestIdAndOrganizationId(UUID requestId, UUID organizationId);

    @Transactional
    @Modifying
    @Query(value = "delete from exchange_request.request_carrier_reply where request_id = :requestId", nativeQuery = true)
    void deleteAllByRequestId(UUID requestId);

}