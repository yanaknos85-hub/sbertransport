package ru.sberbank.ditsib.transport.request.service.taxiprice.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/*
{
  "collectionTime": "2020-04-02T11:10:00+03:00",
  "routePoints": {
    "source": {
      "latitude": 55.731603,
      "longitude": 37.625821
    },
    "waypoints": [
      {
        "latitude": 55.731603,
        "longitude": 37.625821
      }
    ],
    "destination": {
      "latitude": 55.731603,
      "longitude": 37.625821
    }
  },
  "options": [
    "animal"
  ],
  "tariffGroups": [
    "comfort",
    "business"
  ],
  "tariff": 54321
}
 */

/**
 * DTO совместной поездки magenta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityMobilCalculateRequestDto {
    
    public String collectionTime;
    public RoutePoint routePoints = new RoutePoint();
    public List<String> options;
    public List<String> tariffGroups = new ArrayList<>();
    public String tariff;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoutePoint {
        public Point source;

        @Builder.Default
        public List<Point> waypoints = new ArrayList<>();
        public Point destination;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {
        public Double latitude;
        public Double longitude;
    }
}

