package ru.sber.transport.trips.cargo.providers.autopark;


import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import ru.sber.transport.dispatcher.messages.AutoparkMessage;
import ru.sber.transport.trips.cargo.providers.autopark.mapper.AutoparkMapper;
import ru.sber.transport.trips.cargo.providers.autopark.mapper.AutoparkMapperImpl;

import static org.assertj.core.api.Assertions.assertThat;

class AutoparkMapperTest {

    private AutoparkMapper mapper = new AutoparkMapperImpl();

    @Test
    void testToRecordUsingInstancio() {
        var message = Instancio.of(AutoparkMessage.class)
                .set(Select.field("deleted"), false)
                .create();

        var result = mapper.toRecord(message);

        assertThat(result.getId()).isEqualTo(message.getId());
        assertThat(result.getContractorId()).isEqualTo(message.contractorId());
        assertThat(result.getRoutingId()).isEqualTo(message.routingId());
        assertThat(result.getActive()).isEqualTo(!message.deleted());
    }

}