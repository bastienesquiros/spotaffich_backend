package org.besquiros.spotaffich.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "geo_point")
public class GeoPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigInteger id;
    @Column(nullable = false)
    private double latitude;
    @Column(nullable = false)
    private double longitude;
    private String address;
    private String picturePath;
    @Column(nullable = false, updatable = false)
    private LocalDate creationDate;
    private LocalDate updateDate;

    public GeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateDate = LocalDate.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoPoint geoPoint = (GeoPoint) o;
        return Double.compare(latitude, geoPoint.latitude) == 0 && Double.compare(longitude, geoPoint.longitude) == 0 && Objects.equals(id, geoPoint.id) && Objects.equals(address, geoPoint.address) && Objects.equals(picturePath, geoPoint.picturePath) && Objects.equals(creationDate, geoPoint.creationDate) && Objects.equals(updateDate, geoPoint.updateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, latitude, longitude, address, picturePath, creationDate, updateDate);
    }
}
