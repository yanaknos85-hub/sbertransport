package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.request.database.model.CarLocationTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarLocationTaskRepository extends JpaRepository<CarLocationTask, UUID> {

    List<CarLocationTask> findAllByActiveIsTrue();

    List<CarLocationTask> findByOrderPartnerIdAndActiveIsTrue(String s);

    List<CarLocationTask> findAllByRequestIdAndActiveIsTrue(UUID requestId);

    @Modifying
    void deleteAllByCreatedAtBefore(LocalDateTime expiredAt);
}
