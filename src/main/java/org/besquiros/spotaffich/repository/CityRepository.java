package org.besquiros.spotaffich.repository;

import org.besquiros.spotaffich.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface CityRepository extends JpaRepository<City, BigInteger> {
}
