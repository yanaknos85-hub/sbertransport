package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.UUID;

@Entity
@Table(schema = "request", name = "fraud")
@Getter
@Setter
public class FraudData {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "type")
    private FraudType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    /**
     * Комментарий антифрода
     */
    @Column
    private String comment;

}
