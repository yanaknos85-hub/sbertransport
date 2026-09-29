package ru.sber.transport.contractor.database.model;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class JsonIntegrationParams {

    @Column
    private String url;

    @Column
    private String login;

    @Column
    private String password;
}
