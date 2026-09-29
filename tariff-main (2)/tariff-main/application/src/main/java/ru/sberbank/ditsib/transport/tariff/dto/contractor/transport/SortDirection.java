package ru.sberbank.ditsib.transport.tariff.dto.contractor.transport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Направление сортировки. *ASC* - по возрастанию *DESC* - по убыванию
 */
public enum SortDirection {
    
    ASC("ASC"),
    
    DESC("DESC");
    
    private final String value;
    
    SortDirection(String value) {
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
    public static SortDirection fromValue(String value) {
        for (SortDirection b : SortDirection.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}