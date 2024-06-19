package org.besquiros.spotaffich.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.besquiros.spotaffich.entity.GeoPoint;
import org.besquiros.spotaffich.repository.GeoPointRepository;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;


@Service
public class GeoPointService {

    private static final Logger logger = LogManager.getLogger(GeoPointService.class);
    private final GeoPointRepository geoPointRepository;

    private final Environment env;

    public GeoPointService(GeoPointRepository geoPointRepository, Environment env) {
        this.geoPointRepository = geoPointRepository;
        this.env = env;
    }

    // TODO: Add the most possible APIs
    public void fetchAllGeoPoint() {
        String url;
        List<List<GeoPoint>> dataToPersist = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        JsonNode callResult;
        String cityName = "";
        boolean allCallsDone = true;
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
        populateGeoPointAddress();
        retrieveGeoPointStreetPicture();
    }

    public void populateGeoPointAddress() {
        List<GeoPoint> geoPointList = geoPointRepository.findAll();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", env.getProperty("RADAR_API_KEY"));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        for (GeoPoint geoPoint : geoPointList) {
            if (geoPoint.getAddress() == null || geoPoint.getAddress().isEmpty()) {
                String url = String.format("https://api.radar.io/v1/geocode/reverse?coordinates=%f,%f", geoPoint.getLatitude(), geoPoint.getLongitude());

                try {
                    ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

                    if (response.getBody() != null && !response.getBody().isEmpty()) {
                        geoPoint.setAddress(response.getBody().get("addresses").get(0).get("formattedAddress").asText());
                    }
                } catch (Exception e) {
                    logger.error("Error during RADAR.IO API call: " + e.getMessage());
                }
                geoPointRepository.save(geoPoint);
            }
        }
    }

    // TODO: Change save path IN PROD
    public void retrieveGeoPointStreetPicture() {
        List<GeoPoint> geoPointList = geoPointRepository.findAll();
        RestTemplate restTemplate = new RestTemplate();

        for (GeoPoint geoPoint : geoPointList) {
            if (geoPoint.getPicturePath() == null || geoPoint.getPicturePath().isEmpty()) {
                try {
                    String url = String.format("https://maps.googleapis.com/maps/api/streetview?location=%f,%f&return_error_code=true&size=600x400&key=%s", geoPoint.getLatitude(), geoPoint.getLongitude(), env.getProperty("GOOGLE_API_KEY"));
                    ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
                    if (response.getStatusCode().value() != 400) {
                        try {
                            byte[] imageBytes = response.getBody();
                            File pictureToSave = new File(env.getProperty("picture_folder") + geoPoint.getId() + ".jpg");
                            FileOutputStream fos = new FileOutputStream(pictureToSave);
                            fos.write(imageBytes);
                            fos.close();
                            geoPoint.setPicturePath(pictureToSave.getPath());
                            geoPointRepository.save(geoPoint);
                        } catch (Exception e) {
                            logger.error("Error while creating StreetView picture");
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error during Google Maps API call: " + e.getMessage());
                }
            }
        }
    }
}
