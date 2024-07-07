package org.besquiros.spotaffich.controller;

import jakarta.validation.constraints.NotBlank;
import org.besquiros.spotaffich.entity.GeoPoint;
import org.besquiros.spotaffich.service.GeoPointService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/geopoint")
public class GeoPointController {

    GeoPointService geoPointService;


    public GeoPointController(GeoPointService geoPointService) {
        this.geoPointService = geoPointService;
    }

    @GetMapping("/populateDatabase") // TODO: TO DELETE AFTER TESTING OR MAKE SURE ITS NOT EXPOSED
    public void populateDatabase() {
        geoPointService.fetchAllGeoPoint();
    }

    @PostMapping("/findUserProximityGeoPoint")
    public ResponseEntity<List<GeoPoint>> findUserProximityGeoPoint(@RequestBody @NotBlank Double userLatitude, @NotBlank Double userLongitude) {
        List<GeoPoint> nearbyGeoPoints = geoPointService.findUserProximityGeoPoint(userLatitude, userLongitude);
        return ResponseEntity.ok(nearbyGeoPoints);
    }

}
