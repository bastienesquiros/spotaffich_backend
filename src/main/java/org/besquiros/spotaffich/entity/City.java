package org.besquiros.spotaffich.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigInteger id;
    @Column(nullable = false)
    private String cityName;
    @Column(nullable = false)
    private String dataDownloadLink;
    @Column(name = "targeted_lat_long_csv_column_position",nullable = false)
    private Integer targetedLatLongCSVColumnPosition;
    @Column(name = "targeted_csv_delimiter", nullable = false)
    private String targetedCSVDelimiter;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return Objects.equals(id, city.id) && Objects.equals(cityName, city.cityName) && Objects.equals(dataDownloadLink, city.dataDownloadLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cityName, dataDownloadLink);
    }
}
