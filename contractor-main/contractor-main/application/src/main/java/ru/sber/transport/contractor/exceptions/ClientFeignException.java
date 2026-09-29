package ru.sber.transport.contractor.exceptions;

import feign.FeignException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ClientFeignException extends RuntimeException {
    private final String message;
    private final FeignException.FeignClientException exception;
}
