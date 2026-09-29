package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "request")
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@NamedEntityGraph(name = "search-request", attributeNodes = {
        @NamedAttributeNode(value = Request_.TRANSPORT),
        @NamedAttributeNode(value = Request_.AUTHOR, subgraph = Request_.AUTHOR)
 }, subgraphs = {
        @NamedSubgraph(name = Request_.AUTHOR, attributeNodes = {
                @NamedAttributeNode(value = Employee_.ORGANIZATION),
                @NamedAttributeNode(value = Employee_.DEPARTMENT),
                @NamedAttributeNode(value = Employee_.POSITION)
        })
})
public class Request {

    /**
     * Идентификатор записи о заявке
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Человекочитаемый идентификатор
     */
    @NotBlank
    @Column(updatable = false)
    private String humanReadableId;

    /**
     * Создатель заявки
     */
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    private Employee author;

    /**
     * Дата и время создания заявки
     */
    @NotNull
    private LocalDateTime creationTime;

    /**
     * Транспортное средство
     */
    @NotNull
    @ManyToOne
    @JoinColumn(name = "transport_id", referencedColumnName = "id")
    private Transport transport;

    /**
     * Статус заявки
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    /**
     * Проверки
     */
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<Check> checks = new HashSet<>();

    /**
     * Комментарий
     */
    @Size(max = 255)
    private String comment;

    /**
     * Сотрудник, проводивший контроль
     */
    @ManyToOne
    @JoinColumn(name = "inspector_id")
    private Employee inspector;

    /**
     * Дата и время проведения контроля
     */
    private LocalDateTime inspectionTime;
    
    /**
     * Дата и время начала прохождения проверок
     */
    private LocalDateTime checksStartedTime;

    /**
     * Дата и время завершения прохождения проверок
     */
    private LocalDateTime checksFinishedTime;
    
    /**
     * Идентификатор записи об организации
     */
    @NotNull
    private UUID organizationId;

    @PrePersist
    private void addLinks() {
        if (this.getChecks() != null) {
            this.getChecks().forEach(item -> item.setRequest(this));
        }
    }

    @PreRemove
    private void removeLinkedEntities() {
        this.checks.clear();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (Request) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
