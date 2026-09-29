package ru.sber.transport.dispatcher.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;

import java.util.UUID;

@Entity
@Table(schema = "dispatcher", name = "integration_client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@SQLDelete(sql = "update dispatcher.integration_client set active = false where id = ?")
public class IntegrationClient {

    @Id
    private UUID id;

    /**
     * ID контрагента
     */
    @Column(name = "contractor_id", nullable = false)
    private UUID contractorId;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

}
