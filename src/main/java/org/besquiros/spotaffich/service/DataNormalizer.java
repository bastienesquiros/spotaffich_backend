package org.besquiros.spotaffich.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.besquiros.spotaffich.entity.GeoPoint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DataNormalizer {

    public static List<GeoPoint> normalizeData(String cityName, JsonNode data) {
        List<GeoPoint> dataNormalized = new ArrayList<>();
        JsonNode APIResultNode;
        switch (cityName.toUpperCase()) {
            case "BORDEAUX":
                APIResultNode = data.get("results");
                for (JsonNode result : APIResultNode) {
                    dataNormalized.add(new GeoPoint(result.get("geo_point_2d").get("lat").asDouble(), result.get("geo_point_2d").get("lon").asDouble()));
                }
        }

        return dataNormalized;
    }
}
