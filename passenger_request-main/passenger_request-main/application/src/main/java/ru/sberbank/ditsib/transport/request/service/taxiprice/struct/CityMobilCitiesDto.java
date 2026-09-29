package ru.sberbank.ditsib.transport.request.service.taxiprice.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/*
{
  "isSuccess": true,
  "cities": [
    {
      "id": 2,
      "name": "Москва"
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
public class CityMobilCitiesDto {
    
    public boolean isSuccess;
    public List<City> cities = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class City {
        public String id;
        public String name;
    }
}

