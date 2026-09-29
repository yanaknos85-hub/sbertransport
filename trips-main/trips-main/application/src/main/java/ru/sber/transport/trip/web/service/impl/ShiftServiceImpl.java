package ru.sber.transport.trip.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.trip.business.dto.ShiftOperationType;
import ru.sber.transport.trip.business.model.Driver;
import ru.sber.transport.trip.business.model.Shift;
import ru.sber.transport.trip.messaging.ChannelType;
import ru.sber.transport.trip.messaging.providers.DriverProvider;
import ru.sber.transport.trip.messaging.providers.ShiftProvider;
import ru.sber.transport.trip.messaging.senders.dispatcher.DriverSender;
import ru.sber.transport.trip.messaging.senders.dispatcher.ShiftSender;
import ru.sber.transport.trip.web.service.ShiftService;
import ru.sber.transport.trip.web.service.VerificationService;

@Component
@RequiredArgsConstructor
@Transactional
public class ShiftServiceImpl implements ShiftService {

    private final VerificationService verificationService;

    private final ShiftProvider shiftProvider;

    private final DriverProvider driverProvider;

    private final DriverSender driverSender;

    private final ShiftSender shiftSender;

    @Override
    public void deactivate(Shift shift) {
        verificationService.checkShiftIsDeleted(shift, ShiftOperationType.DEACTIVATION);
        shift.setActive(false);
        shiftProvider.save(shift);
        shiftSender.send(shift);
        var driver = driverProvider.get(shift.getDriverId()).orElseThrow(() -> new EntityNotFoundException(Driver.class, shift.getDriverId()));
        driver.setShiftId(null);
        driver.setOnline(false);
        driverProvider.save(driver);
        driverSender.send(driver);
    }

    @Override
    public void activate(Shift shift) {
        verificationService.checkShiftIsDeleted(shift, ShiftOperationType.ACTIVATION);
        shift.setActive(true);
        shiftProvider.save(shift);
        shiftSender.send(shift);
        var shiftDriverId = shift.getDriverId();
        var driver = driverProvider.get(shiftDriverId).orElseThrow(() -> new EntityNotFoundException(Driver.class, shiftDriverId));
        driver.setShiftId(shift.getId());
        driver.setOnline(true);
        driverProvider.save(driver);
        driverSender.send(driver);
    }

}
