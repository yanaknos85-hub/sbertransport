package ru.sberbank.ditsib.transport.tariff.dto.contractor.transport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Field {
    
    ID("ID"),
    
    BRAND("BRAND"),
    
    MODEL("MODEL"),
    
    STATE_NUMBER("STATE_NUMBER");
    
    private String value;
    
    Field(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
    
    @JsonCreator
    public static Field fromValue(String value) {
        for (Field b : Field.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
