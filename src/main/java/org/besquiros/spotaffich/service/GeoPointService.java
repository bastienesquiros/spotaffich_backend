package org.besquiros.spotaffich.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.besquiros.spotaffich.entity.GeoPoint;
import org.besquiros.spotaffich.repository.GeoPointRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
        RestTemplate restTemplate = new RestTemplate();
        JsonNode callResult;
        String cityName = "";
        try {
            cityName = "BORDEAUX";
            url = "https://opendata.bordeaux-metropole.fr/api/explore/v2.1/catalog/datasets/bor_sigpanneaux/records";
            callResult = restTemplate.getForObject(url, JsonNode.class);
            if (callResult != null && !callResult.isEmpty()) {
                geoPointRepository.saveAll(DataNormalizer.normalizeData(cityName, callResult));
            } else {
                logger.warn("No data found for city: " + cityName);
            }
        } catch (Exception e) {
            logger.error("Error happened during API call for city: " + cityName + e);
        }
    }


}
