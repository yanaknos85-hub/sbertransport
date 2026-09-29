package ru.sber.transport.cargo.exchange.request.service;

import ru.sber.transport.cargo.exchange.request.dto.RequestDto;
import ru.sber.transport.cargo.exchange.request.dto.RequestValidationResponse;

/**
 *
 */
public interface RequestValidationService {
    RequestValidationResponse validate(RequestDto requestDto);
}
