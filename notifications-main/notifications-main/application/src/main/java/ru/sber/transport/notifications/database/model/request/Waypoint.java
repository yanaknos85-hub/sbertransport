package ru.sber.transport.notifications.database.model.request;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
@Builder
public class Waypoint {
    
    private final Address address;
    
    private final Duration waitTime;
    
    private final Boolean checkinAutomatic;
    
    private final Boolean checkinManual;
}
