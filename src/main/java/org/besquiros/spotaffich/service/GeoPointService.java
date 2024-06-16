package org.besquiros.spotaffich.service;

import org.besquiros.spotaffich.repository.GeoPointRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;


@Service
public class GeoPointService {
    private final GeoPointRepository geoPointRepository;

    public GeoPointService(GeoPointRepository geoPointRepository) {
    this.geoPointRepository = geoPointRepository;
    }

    // TODO: Rajouter le plus d'APIS possible
    public void fetchAllGeoPoint() {
        try {

        } catch (Exception e) {

        }
    }


}
