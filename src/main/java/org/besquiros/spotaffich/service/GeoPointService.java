package org.besquiros.spotaffich.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.besquiros.spotaffich.entity.GeoPoint;
import org.besquiros.spotaffich.repository.GeoPointRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Service
public class GeoPointService {

    static final Logger logger = LogManager.getLogger(GeoPointService.class);
    private final GeoPointRepository geoPointRepository;

    public GeoPointService(GeoPointRepository geoPointRepository) {
        this.geoPointRepository = geoPointRepository;
    }

    // TODO: Add the most possible APIs
    public void fetchAllGeoPoint() {
        String url;
        List<List<GeoPoint>> dataToPersist = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        JsonNode callResult;
        String cityName = "";
        Boolean allCallsDone = true;
        try {
            // TODO: Verify dataset integrity for BORDEAUX
            cityName = "BORDEAUX";
            url = "https://opendata.bordeaux-metropole.fr/api/explore/v2.1/catalog/datasets/bor_sigpanneaux/records";
            callResult = restTemplate.getForObject(url, JsonNode.class);
            if (callResult != null && !callResult.isEmpty()) {
                dataToPersist.add(DataNormalizer.normalizeData(cityName, callResult));
            } else {
                logger.warn("No data found for city: " + cityName);
                allCallsDone = false;
            }
        } catch (Exception e) {
            logger.error("Error happened data handling for city: " + cityName + " " + e);
        }
        // TODO: Add a method that compares fetched and current bdd state to remove inactive GeoPoints and insert new ones, call it only when this whole call went good;

        if (allCallsDone) {
            persistGeoPoint(dataToPersist);
        }

    }


    // TODO: VERIFY IF THIS CAUSES PERFORMANCE ISSUES
    public void persistGeoPoint(List<List<GeoPoint>> geoPointFetchedData) {
        List<GeoPoint> geoPointsAlreadyInDatabase = geoPointRepository.findAll();

        Map<String, GeoPoint> geoPointsInDatabaseMap = new HashMap<>();
        for (GeoPoint geoPoint : geoPointsAlreadyInDatabase) {
            String key = geoPoint.getLongitude() + "_" + geoPoint.getLatitude();
            geoPointsInDatabaseMap.put(key, geoPoint);
        }

        Set<String> fetchedGeoPointsKeys = new HashSet<>();
        List<GeoPoint> pointsToSave = new ArrayList<>();

        for (List<GeoPoint> geoPointFetchedList : geoPointFetchedData) {
            for (GeoPoint geoPointFetched : geoPointFetchedList) {
                String key = geoPointFetched.getLongitude() + "_" + geoPointFetched.getLatitude();
                fetchedGeoPointsKeys.add(key);
                if (!geoPointsInDatabaseMap.containsKey(key)) {
                    pointsToSave.add(geoPointFetched);
                }
            }
        }

        List<GeoPoint> pointsToDelete = new ArrayList<>();
        for (Map.Entry<String, GeoPoint> entry : geoPointsInDatabaseMap.entrySet()) {
            if (!fetchedGeoPointsKeys.contains(entry.getKey())) {
                pointsToDelete.add(entry.getValue());
            }
        }

        if (!pointsToSave.isEmpty()) {
            geoPointRepository.saveAll(pointsToSave);
        }

        if (!pointsToDelete.isEmpty()) {
            geoPointRepository.deleteAll(pointsToDelete);
        }
    }

}