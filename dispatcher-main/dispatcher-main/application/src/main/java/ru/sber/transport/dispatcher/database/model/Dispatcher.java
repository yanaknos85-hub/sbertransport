package ru.sber.transport.dispatcher.database.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = "dispatcher", name = "dispatcher")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Dispatcher implements HasName {

        @Id
        @ToString.Include
        @GeneratedValue
        private UUID id;

        @ToString.Include
        @Column(name = "human_readable_id")
        private String humanReadableId;

        @Column(name = "last_name")
        private String lastName;

        @Column(name = "first_name")
        private String firstName;

        @Column(name = "patronymic")
        private String patronymic;

        @Column(name = "phone")
        private String phone;

        @Column(name = "email")
        private String email;

        @Column(name = "consent")
        private boolean consent;

        @JoinColumn(name = "contractor_id")
        @ManyToOne
        private Contractor contractor;

        @Column(name = "phone_confirmed")
        private boolean phoneConfirmed = false;

        @Column
        private boolean active = true;

        @Column(name = "oauth_id")
        private UUID oauthId;

        @JoinColumn(name = "autopark_id")
        @ManyToOne
        private Autopark autopark;

        @Column(name = "ewb_creation_possibility")
        private boolean ewbCreationPossibility = false;

        @Column(name = "personnel_number")
        private String personnelNumber;

        @Column(name = "attorney_number")
        private String attorneyNumber;

        @Column(name = "issue_date")
        private LocalDate issueDate;

        @Column(name = "expiry_date")
        private LocalDate expiryDate;

        @Column(name = "creation_system")
        private String creationSystem;
}
