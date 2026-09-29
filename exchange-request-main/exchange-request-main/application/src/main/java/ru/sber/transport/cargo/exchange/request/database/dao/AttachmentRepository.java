package ru.sber.transport.cargo.exchange.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.cargo.exchange.request.database.model.Attachment;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с прикреплёнными файлами (вложениями) к заявкам.
 * Предоставляет доступ к данным таблицы attachments.
 */
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    /**
     * Находит вложение по идентификатору заявки и оригинальному имени файла.
     *
     * @param requestId идентификатор заявки
     * @param originalName оригинальное имя файла (например, "накладная.pdf")
     * @return Optional с найденным вложением или пустой, если не найдено
     */
    Optional<Attachment> findByRequestIdAndOriginalName(UUID requestId, String originalName);

    /**
     * Удаляет все вложения по идентификатору заявки.
     *
     * @param requestId идентификатор заявки
     */
    long deleteByRequestId(UUID requestId);
}
