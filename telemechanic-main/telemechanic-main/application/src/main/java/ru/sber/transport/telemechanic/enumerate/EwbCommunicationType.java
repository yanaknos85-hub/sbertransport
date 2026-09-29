package ru.sber.transport.telemechanic.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EwbCommunicationType {
    URBAN("Г – городское"),
    SUBURBAN("П – пригородное"),
    INTERCITY("М – междугородное");
    
    private final String title;
}
