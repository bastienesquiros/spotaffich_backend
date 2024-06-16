package org.besquiros.spotaffich.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
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
    private Date creationDate = new Date();
    private Date updateDate;

    public GeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
