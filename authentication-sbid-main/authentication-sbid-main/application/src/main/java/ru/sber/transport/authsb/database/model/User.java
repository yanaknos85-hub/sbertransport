package ru.sber.transport.authsb.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "authorization_sbid", indexes = {
        @Index(name = "idx_users_sub", columnList = "sub", unique = true),
        @Index(name = "idx_users_inn", columnList = "inn"),
        @Index(name = "idx_users_email", columnList = "email")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "sub", nullable = false, unique = true, length = 255)
    private String sub;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "inn", length = 12)
    private String inn;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // Связь с organization
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_inn", referencedColumnName = "inn", foreignKey = @ForeignKey(name = "fk_user_organization"))
    private Organization organization;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Role> roles;
}