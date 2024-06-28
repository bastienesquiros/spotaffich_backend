package org.besquiros.spotaffich.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.besquiros.spotaffich.entity.City;
import org.besquiros.spotaffich.entity.GeoPoint;
import org.besquiros.spotaffich.entity.NoGeoPointInArea;
import org.besquiros.spotaffich.repository.CityRepository;
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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.*;


@Service
public class GeoPointService {

    private static final Logger logger = LogManager.getLogger(GeoPointService.class);
    private final GeoPointRepository geoPointRepository;
    private final NoGeoPointInAreaRepository noGeoPointInAreaRepository;
    private final CityRepository cityRepository;
    private final Environment env;

    public GeoPointService(GeoPointRepository geoPointRepository, Environment env, NoGeoPointInAreaRepository noGeoPointInAreaRepository, CityRepository cityRepository) {
        this.geoPointRepository = geoPointRepository;
        this.noGeoPointInAreaRepository = noGeoPointInAreaRepository;
        this.cityRepository = cityRepository;
        this.env = env;
    }

    // TODO: Add the most possible data
    public void fetchAllGeoPoint() {
        List<City> cityList = cityRepository.findAll();
        List<List<GeoPoint>> geoPointListToPersist = new ArrayList<>();
        boolean allDataRetrieved = true;
        for (City city : cityList) {
            try {
                URL downloadDataURL = new URI(city.getDataDownloadLink()).toURL();
                Reader dataReader = new InputStreamReader(downloadDataURL.openStream());
                CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setDelimiter(';').setTrim(true).build();
                CSVParser csvParser = new CSVParser(dataReader, csvFormat);
                geoPointListToPersist.add(DataNormalizer.normalizeData(city, csvParser.getRecords()));
                dataReader.close();
                csvParser.close();
            } catch (Exception e) {
                logger.error("Error while getting data from: " + city.getCityName() + " - " + e.getMessage());
                e.printStackTrace();
                allDataRetrieved = false;
            }
            if (allDataRetrieved) {
                persistGeoPoint(geoPointListToPersist);
            }
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
                if (!geoPointsInDatabaseMap.containsKey(key) && !pointsToSave.contains(geoPointFetched)) {
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
            for (GeoPoint geoPointToDelete : pointsToDelete) {
                File pictureToDelete = new File(env.getProperty("picture_folder") + geoPointToDelete.getId() + ".jpg");
                pictureToDelete.delete();
            }
            geoPointRepository.deleteAll(pointsToDelete);
        }
        findAndPersistGeoPointAddress();
        findAndPersistGeoPointStreetPicture();
    }

    public void findAndPersistGeoPointAddress() {
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
    public void findAndPersistGeoPointStreetPicture() {
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