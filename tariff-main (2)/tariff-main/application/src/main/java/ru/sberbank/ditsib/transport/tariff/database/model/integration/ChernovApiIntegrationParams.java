package ru.sberbank.ditsib.transport.tariff.database.model.integration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;

@Getter
//@Entity
@SuperBuilder
@RequiredArgsConstructor
@Jacksonized
@DiscriminatorValue(value = TaxiExternalIntegrationType.Constants.CHERNOV_JSON_API_STRING)
public class ChernovApiIntegrationParams extends IntegrationParams {
    @Column(name = "rest_address")
    private String restAddress;
    
    @Column(name = "inbound_controller_name")
    private String inboundControllerName;
    
    @Column(name = "port")
    private String port;
    
    @Column(name="login")
    private String login;
    
    @Column(name = "password")
    private String password;
}
