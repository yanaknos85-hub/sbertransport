package ru.sber.transport.cargo.exchange.request.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность, представляющая прикреплённый файл к заявке.
 * Хранится в таблице 'attachments' схемы 'exchange_request'.
 * Поддерживает два типа файлов: фото груза и документы.
 */
@Entity
@Table(schema = "exchange_request", name = "attachments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    /**
     * Уникальный идентификатор файла.
     * Первичный ключ.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Ссылка на заявку, к которой прикреплён файл.
     * Отношение many-to-one — у одной заявки может быть несколько файлов.
     */
    @Column(nullable = false)
    private UUID requestId;

    /**
     * Тип файла: 'cargo_photo' — фото груза, 'document' — документ.
     * Обязательное поле.
     */
    @Column(nullable = false, length = 20)
    private String fileType;

    /**
     * Оригинальное имя файла при загрузке.
     * Максимальная длина — 500 символов.
     */
    @Column(nullable = false, length = 500)
    private String originalName;


    /**
     * Путь к файлу в системе хранения (объектном хранилище).
     * <p>
     * Пример: /uploads/2026/02/01/abc123.pdf.
     * Максимальная длина — 1000 символов. Обязательное поле.
     */
    @Column(length = 1000, nullable = false)
    private String storagePath;

    /**
     * Размер файла в байтах.
     * Должен быть больше 0.
     */
    @Column(nullable = false)
    private Long fileSize;

    /**
     * MIME-тип файла (например, image/jpeg, application/pdf).
     * Используется для валидации и отображения.
     */
    @Column(nullable = false, length = 100)
    private String mimeType;

    /**
     * Время загрузки файла.
     * Фиксируется при добавлении записи.
     */
    @Column(nullable = false)
    private LocalDateTime uploadAt;
}