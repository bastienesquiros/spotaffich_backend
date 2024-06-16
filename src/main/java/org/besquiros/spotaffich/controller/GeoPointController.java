package org.besquiros.spotaffich.controller;

import org.besquiros.spotaffich.service.GeoPointService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/geopoint")
public class GeoPointController {

    GeoPointService geoPointService;

    public GeoPointController(GeoPointService geoPointService) {
        this.geoPointService = geoPointService;
    }

    @GetMapping("/test") // TODO: REFACTO
    public void test() {
    geoPointService.fetchAllGeoPoint();
    }


}
