package ru.sber.transport.telemechanic.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.telemechanic.FirstTitleCreateRequestMessage;
import ru.sber.transport.telemechanic.FirstTitleCreateResponseMessage;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.dto.ewb.FirstTitleDto;
import ru.sber.transport.telemechanic.dto.ewb.first_title.FirstTitleRequest;
import ru.sber.transport.telemechanic.messaging.sender.FirstTitleCreateRequestSender;
import ru.sber.transport.telemechanic.provider.EwbProvider;
import ru.sber.transport.telemechanic.service.DriverService;
import ru.sber.transport.telemechanic.service.EwbService;

@RequiredArgsConstructor
@Component
@Slf4j
public class EwbProviderImpl implements EwbProvider {
    
    private final EwbService ewbService;
    private final DriverService driverService;
    private final FirstTitleCreateRequestSender firstTitleCreateRequestSender;
    
    @Override
    public void save(FirstTitleCreateRequestMessage message) {
        try {
            log.info("Start save first title, message id:{}", message.id());
            var driver = driverService.getByEmployeeId(message.driverEmployeeId());
            ewbService.sendAndSaveFirstTitle(new FirstTitleDto(
                    message.content(),
                    message.fileName(),
                    new FirstTitleRequest(
                            message.ewbUuid(),
                            message.startDate(),
                            message.finishDate(),
                            message.transportationType(),
                            message.communicationType(),
                            message.tariffDepartmentId(),
                            message.transportId(),
                            driver.getId()
                    ),
                    message.humanReadableId(),
                    message.creationTime(),
                    message.ewbUuid(),
                    EwbTitleType.FIRST,
                    message.signature()
            ), message.userId());
            var response = new FirstTitleCreateResponseMessage(message.id(), null);
            firstTitleCreateRequestSender.send(response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            var response = new FirstTitleCreateResponseMessage(message.id(), e.getMessage());
            firstTitleCreateRequestSender.send(response);
        } finally {
            log.info("Finish save first title, message id:{}", message.id());
        }
    }
}