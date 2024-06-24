package org.besquiros.spotaffich.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.besquiros.spotaffich.entity.GeoPoint;
import org.besquiros.spotaffich.entity.NoGeoPointInArea;
import org.besquiros.spotaffich.repository.GeoPointRepository;
import org.besquiros.spotaffich.repository.NoGeoPointInAreaRepository;
import org.besquiros.spotaffich.util.GeoUtil;
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
    private final NoGeoPointInAreaRepository noGeoPointInAreaRepository;

    private final Environment env;

    public GeoPointService(GeoPointRepository geoPointRepository, Environment env, NoGeoPointInAreaRepository noGeoPointInAreaRepository) {
        this.geoPointRepository = geoPointRepository;
        this.noGeoPointInAreaRepository = noGeoPointInAreaRepository;
        this.env = env;
    }

    // TODO: Add the most possible APIs
    public void fetchAllGeoPoint() {
        Map<String, String> citiesApisMap = populateCitiesApisMap();
        List<List<GeoPoint>> dataToPersist = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        JsonNode callResult;
        boolean allCallsDone = true;

        for (Map.Entry<String, String> city : citiesApisMap.entrySet()) {
            try {
                callResult = restTemplate.getForObject(city.getValue(), JsonNode.class);
                if (callResult != null && !callResult.isEmpty()) {
                    dataToPersist.add(DataNormalizer.normalizeData(city.getKey(), callResult));
                } else {
                    logger.warn("No data found for city: " + city.getKey());
                    allCallsDone = false;
                }
            } catch (Exception e) {
                logger.error("Error happened data handling for city: " + city.getKey() + " " + e);
                e.printStackTrace();
                allCallsDone = false;

            }
        }
        if (allCallsDone) {
            persistGeoPoint(dataToPersist);
        }
    }

    private Map<String, String> populateCitiesApisMap() {
        Map<String, String> citiesApisMap = new HashMap<>();
        citiesApisMap.put("BORDEAUX", "https://opendata.bordeaux-metropole.fr/api/explore/v2.1/catalog/datasets/bor_sigpanneaux/records?limit=-1");
        citiesApisMap.put("LE HAILAN", "https://opendata.bordeaux-metropole.fr/api/explore/v2.1/catalog/datasets/leh_panneaux_affichage_libre/records?limit=-1");
        citiesApisMap.put("TALENCE", "https://opendata.bordeaux-metropole.fr/api/explore/v2.1/catalog/datasets/tal_panneaux_affichage_libre/records?limit=-1");
        return citiesApisMap;
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

    public List<GeoPoint> findUserProximityGeoPoint(double userLatitude, double userLongitude) {
        // For now, we define proximity is equivalent to a 10KM radius, this can be easily tweaked in the future
        GeoUtil.BoundingBox userCircularZone = GeoUtil.calculateBoundingBox(userLatitude, userLongitude, 10);

        List<GeoPoint> databaseGeoPointList = geoPointRepository.findAllLatitudeAndLongitude();
        List<GeoPoint> geoPointInRadiusList = new ArrayList<>();
        for (GeoPoint geoPoint : databaseGeoPointList) {
            if (GeoUtil.isPointInRadius(geoPoint.getLatitude(), geoPoint.getLongitude(), userCircularZone)) {
                geoPointInRadiusList.add(geoPoint);
            }
        }

        if (geoPointInRadiusList.isEmpty()) {
            noGeoPointFoundInArea(userCircularZone);
        }

        return geoPointInRadiusList;
    }

    private void noGeoPointFoundInArea(GeoUtil.BoundingBox noGeoPointZone) {
        noGeoPointInAreaRepository.save(new NoGeoPointInArea(noGeoPointZone.minLatitude(), noGeoPointZone.maxLatitude(), noGeoPointZone.minLongitude(), noGeoPointZone.maxLongitude()));
    }
}
