package org.besquiros.spotaffich.repository;

import org.besquiros.spotaffich.entity.GeoPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.util.List;

public interface GeoPointRepository extends JpaRepository<GeoPoint, BigInteger> {
   @Query("SELECT new GeoPoint(g.latitude, g.longitude) FROM GeoPoint g")
    public List<GeoPoint> findAllLatitudeAndLongitude();
}
