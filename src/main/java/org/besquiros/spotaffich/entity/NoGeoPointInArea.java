package org.besquiros.spotaffich.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "no_geo_point_in_area")
public class NoGeoPointInArea {

    LocalDate date;
    double minLatitude;
    double maxLatitude;
    double minLongitude;
    double maxLongitude;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigInteger id;


    public NoGeoPointInArea(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
        this.date = LocalDate.now();
        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
    }

    @PrePersist
    protected void onCreate() {
        this.date = LocalDate.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoGeoPointInArea that = (NoGeoPointInArea) o;
        return Double.compare(minLatitude, that.minLatitude) == 0 && Double.compare(maxLatitude, that.maxLatitude) == 0 && Double.compare(minLongitude, that.minLongitude) == 0 && Double.compare(maxLongitude, that.maxLongitude) == 0 && Objects.equals(date, that.date) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, minLatitude, maxLatitude, minLongitude, maxLongitude, id);
    }
}
