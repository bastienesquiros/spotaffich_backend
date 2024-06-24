package org.besquiros.spotaffich.repository;

import org.besquiros.spotaffich.entity.NoGeoPointInArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface NoGeoPointInAreaRepository extends JpaRepository<NoGeoPointInArea, BigInteger> {
}
