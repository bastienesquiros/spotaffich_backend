package org.besquiros.spotaffich.service;

import org.apache.commons.csv.CSVRecord;
import org.besquiros.spotaffich.entity.City;
import org.besquiros.spotaffich.entity.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;

public class DataNormalizer {
    private DataNormalizer() {
    }


    public static List<GeoPoint> normalizeData(City city, List<CSVRecord> csvRecordList) {
        List<GeoPoint> dataNormalized = new ArrayList<>();
        for (CSVRecord row : csvRecordList) {
            if (!row.get(city.getTargetedLatLongCSVColumn()).isEmpty()) {
                String[] latandLongCoords = row.get(city.getTargetedLatLongCSVColumn()).split(",");
                dataNormalized.add(new GeoPoint(parseDouble(latandLongCoords[0]), parseDouble(latandLongCoords[1])));
            }
        }
        return dataNormalized;
    }
}
