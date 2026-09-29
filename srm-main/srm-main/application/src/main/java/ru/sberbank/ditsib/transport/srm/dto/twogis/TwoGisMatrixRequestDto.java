package ru.sberbank.ditsib.transport.srm.dto.twogis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/*
    "points": [
        {
            "lat": 54.99770587584445,
            "lon": 82.79502868652345
        },
        {
            "lat": 54.99928130973027,
            "lon": 82.92137145996095
        },
        {
            "lat": 55.04533538802211,
            "lon": 82.98179626464844
        },
        {
            "lat": 55.072470687600536,
            "lon": 83.04634094238281
        }
    ],
    "sources": [0, 1],
    "targets": [2, 3]
 */

/**
 * DTO для матрицы расстояний 2гис - запрос.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoGisMatrixRequestDto {
    
    public List<Point> points = new ArrayList<>();
    public List<Integer> sources = new ArrayList<>();
    public List<Integer> targets = new ArrayList<>();
    public String mode;
    public String type;
    public List<String> filters = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {
        public Double lat;
        public Double lon;
    }
}

