package ru.sber.transport.contractor.database.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(schema = "contractors", name = "employee")
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee implements HasName {

    @Id
    private UUID id;

    @JsonAlias("first_name")
    @Column(name = "first_name")
    private String firstName;

    @JsonAlias("last_name")
    @Column(name = "last_name")
    private String lastName;

    @Column
    private String patronymic;

    @JsonAlias("mobile_phone")
    @Schema(description = "Номер телефона")
    @Column(name = "mobile_phone")
    private String mobilePhone;

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "consent")
    @Builder.Default
    private boolean consent = false;
}
