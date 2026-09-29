package ru.sber.transport.etrn.database.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.sber.transport.etrn.config.converters.IsoLocalDateTimeSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "etrn", schema = "etrn_cargo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etrn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "humanreadableid", nullable = false, unique = true, length = 64)
    private String humanReadableId;

    @Column(name = "status", nullable = false, length = 64)
    private String status;

    @Column(name = "sender_name", length = 512)
    private String senderName;

    @Column(name = "receiver_name", length = 512)
    private String receiverName;

    @Column(name = "carrier_name", length = 512)
    private String carrierName;

    @Column(name = "timezone", length = 64)
    private String timeZone;

    @Column(name = "sla")
    @JsonSerialize(using = IsoLocalDateTimeSerializer.class)
    private LocalDateTime sla;

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    // === Поля из contract.md раздел 3 ===
    @Column(name = "application_number", length = 64)
    private String applicationNumber;

    @Column(name = "route_number", length = 64)
    private String routeNumber;

    @Column(name = "cargo_description", length = 512)
    private String cargoDescription;

    @Column(name = "cargo_places")
    private Integer cargoPlaces;

    @Column(name = "cargo_weight_kg", precision = 14, scale = 2)
    private BigDecimal cargoWeightKg;

    @Column(name = "route", length = 512)
    private String route;

    /* Machine-Readable Power of Attorney — МЧД (машино-читаемая доверенность) */
    @Column(name = "mrpa_expires_at")
    private LocalDate mrpaExpiresAt;

    @Column(name = "cargo_length", precision = 10, scale = 2)
    private BigDecimal cargoLength;

    @Column(name = "cargo_width", precision = 10, scale = 2)
    private BigDecimal cargoWidth;

    @Column(name = "cargo_height", precision = 10, scale = 2)
    private BigDecimal cargoHeight;

    /* Simple Electronic Signature — ПЭП (простая электронная подпись) */
    @Column(name = "ses_full_name", length = 256)
    private String sesFullName;

    @Column(name = "ses_role", length = 128)
    private String sesRole;

    @Column(name = "ses_event_datetime")
    private LocalDateTime sesEventDatetime;

    @Column(name = "ses_event_id", length = 64)
    private String sesEventId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "title_chain", columnDefinition = "jsonb")
    private List<TitleEntry> titleChain;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "verifications", columnDefinition = "jsonb")
    private Verifications verifications;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lock_info", columnDefinition = "jsonb")
    private LockInfo lockInfo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    public record TitleEntry(String title, LocalDateTime signedAt, String signedBy) {}

    public record Verifications(List<CheckEntry> checks, Boolean overallPassed, LocalDateTime verifiedAt) {
        public record CheckEntry(String name, Boolean passed) {}
    }

    public record LockInfo(
            UUID userId,
            @JsonSerialize(using = IsoLocalDateTimeSerializer.class)
            LocalDateTime lockUntil) {
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}