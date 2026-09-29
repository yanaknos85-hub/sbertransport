package ru.sber.transport.cargo.exchange.request.dto;

public record ErrorResponseDto(
        boolean isSuccess,
        String orderSbertransportId,
        ErrorDetails error
) {
    public ErrorResponseDto(String type, String orderSbertransportId, String message) {
        this(false, orderSbertransportId, new ErrorDetails(type, message));
    }

    public ErrorResponseDto(String type, String message) {
        this(false, null, new ErrorDetails(type, message));
    }

    public record ErrorDetails(
            String type,
            String message
    ) {
    }
}