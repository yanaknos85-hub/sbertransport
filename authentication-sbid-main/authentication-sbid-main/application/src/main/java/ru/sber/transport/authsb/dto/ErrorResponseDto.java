package ru.sber.transport.authsb.dto;

public record ErrorResponseDto(
        int status,
        String message,
        String description)
{}

