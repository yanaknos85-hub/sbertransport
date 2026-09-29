package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.corp.TechnicalUser;
import ru.sberbank.ditsib.transport.request.messaging.message.UpdateTripRequestStatusMessage;
import ru.sberbank.ditsib.transport.request.service.RequestService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;

import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class UpdateRequestStatusListenerImpl implements Consumer<Message<UpdateTripRequestStatusMessage>> {

    private final RequestService requestService;

    private final EmployeeService employeeService;

    public void accept(Message<UpdateTripRequestStatusMessage> message) {
        handle(message.getPayload());
    }

    private void handle(UpdateTripRequestStatusMessage message) {
        final UUID userId = message.getUserId() != null ? UUID.fromString(message.getUserId()) : null;
        requestService.get(message.getId())
                      .ifPresent(request -> {
                          var status = TripRequestStatus.getFromString(message.getStatus());
                          if (status.isPresent()) {
                              var employee = employeeService.getByUserId(userId).orElse(TechnicalUser.get());
                              requestService.changeState(request, status.get(), employee);
                          }
                      });
    }
}