package ru.sberbank.ditsib.transport.request.exceptions;

public class CarLocationTaskNotFoundException extends BusinessException {

    public static final String ORDER_PARTNER_ID_NOT_FOUND = "Car location task with orderPartnerId=%s not found";
    public static final String REQUEST_ID_NOT_FOUND = "Car location task with requestId=%s not found";

    public CarLocationTaskNotFoundException(String message) {
        super(message);
    }
}
