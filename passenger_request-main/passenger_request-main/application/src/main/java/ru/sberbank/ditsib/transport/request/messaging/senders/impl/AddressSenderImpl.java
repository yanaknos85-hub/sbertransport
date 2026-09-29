package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.request.database.model.Address;
import ru.sberbank.ditsib.transport.request.messaging.message.AddressUsedMessage;
import ru.sberbank.ditsib.transport.request.messaging.senders.AddressSender;

import java.util.HashMap;
import java.util.UUID;

/**
 * Service for sending addresses.
 */
@RequiredArgsConstructor
@Component
class AddressSenderImpl implements AddressSender {

    @Qualifier("frequentlyAddressOutput")
    private final ObjectProvider<OutputBridge> frequentlyAddressOutput;

    @Override
    public void send(Address address, UUID employeeId, boolean first) {
        var message = AddressUsedMessage.builder()
                .first(first)
                .building(address.getBuilding())
                .city(address.getCity())
                .country(address.getCountry())
                .house(address.getHouse())
                .id(address.getId())
                .region(address.getRegion())
                .street(address.getStreet())
                .structure(address.getStructure())
                .employeeId(employeeId)
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
        var headers = new HashMap<String, Object>();
        headers.put(KafkaHeaders.KEY, message.getId());

        frequentlyAddressOutput.ifAvailable(outputBridge -> outputBridge.send(message, headers));
    }
}
