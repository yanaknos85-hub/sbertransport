package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sber.transport.telemechanic.common.EwbTitleType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "title", uniqueConstraints = @UniqueConstraint(columnNames = {"ewb_id", "type"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = {"ewb", "type"})
public class EwbTitle {

    /**
     * Идентификатор записи загруженного файла
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * ЭПЛ для которого сохранен титул
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ewb_id", referencedColumnName = "id")
    private Ewb ewb;

    /**
     * Номер титула
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private EwbTitleType type;

    /**
     * Название файла
     */
    @NotBlank
    private String fileName;

    /**
     * Название файла в s3
     */
    @NotBlank
    @Column(name = "s3_file_name")
    private String s3FileName;

    /**
     * Название файла в s3 с подписью
     */
    @Column(name = "signature_s3_file_name")
    private String signatureS3FileName;

    /**
     * Дата создания файла
     */
    private LocalDateTime createdAt;

    /**
     * Дата отправки файла
     */
    private LocalDateTime sentAt;

    /**
     * Идентификатор документа в системе Корус
     */
    private UUID korusId;

    /**
     * Идентификатор цепочки документов в системе Корус
     */
    private UUID chainId;

}
