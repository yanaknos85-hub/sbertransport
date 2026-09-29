package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "check")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
@EqualsAndHashCode(of = {"id", "checkType"})
public class Check {

    /**
     * Идентификатор записи о проверке
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Тип проверки
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private CheckType checkType;

    /**
     * Статус проверки
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CheckStatus checkStatus;

    /**
     * Количество попыток пройти проверку
     */
    @NotNull
    private Integer attempt;

    /**
     * Идентификатор записи о заявке
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    /**
     * Фотографии проверок
     */
    @OneToMany(mappedBy = "checkId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<CheckPhoto> photos = new HashSet<>();

    private String comment;
    
    @PrePersist
    private void addLinks() {
        if (this.getPhotos() != null) {
            this.getPhotos().forEach(item -> item.setCheckId(this.getId()));
        }
    }

    @PreRemove
    private void removeLinks() {
        this.getPhotos().clear();
    }
}