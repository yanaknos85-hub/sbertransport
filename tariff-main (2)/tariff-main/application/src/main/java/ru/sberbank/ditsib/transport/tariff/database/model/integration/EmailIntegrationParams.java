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
@DiscriminatorValue(value = TaxiExternalIntegrationType.Constants.EMAIL_XML_API_STRING)
public class EmailIntegrationParams extends IntegrationParams {
    @Column(name = "outbound_email")
    private String outboundEmail;
}
