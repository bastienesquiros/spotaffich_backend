package org.besquiros.spotaffich.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        NoGeoPointInArea that = (NoGeoPointInArea) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
