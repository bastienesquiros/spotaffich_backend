package org.besquiros.spotaffich.repository;

import org.besquiros.spotaffich.entity.GeoPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface GeoPointRepository extends JpaRepository<GeoPoint, BigInteger> {
}
