package ru.sberbank.ditsib.transport.request.service.taxiprice.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/*
{
  "isSuccess": true,
  "tariffs": [
    {
      "id": 54321,
      "name": "Эконом",
      "options": [
        {
          "name": "animal",
          "title": "Перевозка животного"
        }
      ]
    }
  ]
}
 */

/**
 * DTO совместной поездки magenta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityMobilTariffDto {
    
    public boolean isSuccess;
    public List<Tariff> tariffs = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Tariff {
        public String id;
        public String name;
        public List<Option> options = new ArrayList<>();
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        public String name;
        public String title;
    }
}

