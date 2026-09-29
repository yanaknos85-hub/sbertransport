package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Класс поездки
 */


public enum OrderClass {
  
  ECONOMY("ECONOMY"),
  
  COMFORT("COMFORT"),
  
  COMFORT_PLUS("COMFORT_PLUS"),
  
  BUSINESS("BUSINESS");

  private String value;

  OrderClass(String value) {
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
  public static OrderClass fromValue(String value) {
    for (OrderClass b : OrderClass.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

