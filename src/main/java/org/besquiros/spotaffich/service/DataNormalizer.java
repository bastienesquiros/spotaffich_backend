package org.besquiros.spotaffich.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.besquiros.spotaffich.entity.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class DataNormalizer {
    private DataNormalizer() {
    }


    public static List<GeoPoint> normalizeData(String cityName, JsonNode data) {
        List<GeoPoint> dataNormalized = new ArrayList<>();
        JsonNode apiResultNode;
        switch (cityName.toUpperCase()) {
            case "BORDEAUX":
                apiResultNode = data.get("results");
                for (JsonNode result : apiResultNode) {
                    dataNormalized.add(new GeoPoint(result.get("geo_point_2d").get("lat").asDouble(), result.get("geo_point_2d").get("lon").asDouble()));
                }
                break;
            case "LE HAILAN":
                apiResultNode = data.get("results");
                for (JsonNode result : apiResultNode) {
                    dataNormalized.add(new GeoPoint(result.get("geo_point").get("lat").asDouble(), result.get("geo_point").get("lon").asDouble()));
                }
                break;
        }

        return dataNormalized;
    }
}
