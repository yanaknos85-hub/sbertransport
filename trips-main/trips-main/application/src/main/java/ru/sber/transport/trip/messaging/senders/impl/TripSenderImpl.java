package ru.sber.transport.trip.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.trip.business.model.Shift;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.message.TripMessage;
import ru.sber.transport.trip.messaging.mapper.MessageTripMapper;
import ru.sber.transport.trip.messaging.providers.ContractorProvider;
import ru.sber.transport.trip.messaging.providers.ShiftProvider;
import ru.sber.transport.trip.messaging.senders.TripSender;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
class TripSenderImpl implements TripSender {

    @Qualifier("tripOutput")
    private final ObjectProvider<OutputBridge> tripOutput;

    @Qualifier("tripOutputSsl")
    private final ObjectProvider<OutputBridge> tripOutputSsl;

    private final MessageTripMapper tripMapper;

    private final ContractorProvider contractorProvider;

    private final ShiftProvider shiftProvider;

    @Override
    public void send(Trip trip) {
        log.debug("Sending trip");
        var message = tripMapper.toMessage(trip);
        message.setContractorDigitId(contractorProvider.getContractorDigitId(trip.getContractorId()));
        message.setType(TripMessage.TripType.PASSENGER);
        message.setVehicleId(getVehicleId(trip));
        if (trip.getPlannedShiftId() != null) {
            var shift = shiftProvider.get(trip.getPlannedShiftId());
            if (shift.isPresent()) {
                message.setPlannedShiftId(shift.get().getId());
                message.setDriverId(shift.get().getDriverId());
                message.setVehicleId(shift.get().getVehicleId());
            }
        }
        tripOutput.ifAvailable(ob -> ob.send(message));
        tripOutputSsl.ifAvailable(ob -> ob.send(message));
    }

    UUID getVehicleId(Trip trip){
        if(trip.getVehicleId() != null){
            return trip.getVehicleId();
        } else if(trip.getExpectedVehicleId()!= null){
            return trip.getExpectedVehicleId();
        } else return null;
    }
}
