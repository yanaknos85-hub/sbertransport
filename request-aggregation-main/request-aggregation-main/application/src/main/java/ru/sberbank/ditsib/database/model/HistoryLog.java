package ru.sberbank.ditsib.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Сущность для логирования истории обработки документов.
 * Хранит информацию о загруженных файлах и их статусе обработки.
 */
@Entity
@Table(name = "history_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoryLog {

    /**
     * Уникальный идентификатор записи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Дата и время создания записи.
     */
    @Column(nullable = false)
    private LocalDateTime createDateTime;

    /**
     * Формат обрабатываемого документа (например, Excel, CSV).
     */
    @Column(nullable = false)
    private String docFormat;

    /**
     * Количество записей в документе.
     */
    @Column(nullable = false, columnDefinition = "numeric")
    private Integer countRecords;

    /**
     * Отправитель документа.
     */
    @Column(nullable = false)
    private String sender;

    /**
     * Статус обработки документа.
     */
    @Column(nullable = false, columnDefinition = "numeric")
    private Integer processingStatus;

    /**
     * Описание статуса обработки.
     */
    private String descriptionStatus;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistoryLog that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
