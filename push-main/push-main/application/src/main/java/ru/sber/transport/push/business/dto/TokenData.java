package ru.sber.transport.push.business.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TokenData {

    private UUID id;

    private PlatformType platformType;
    
    private String value;
    
}
