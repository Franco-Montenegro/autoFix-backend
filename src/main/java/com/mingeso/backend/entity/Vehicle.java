package com.mingeso.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Vehiculo registrado la primera vez que llega al taller (HU1).
 * La patente es unica pero no es PK, para poder corregirla sin romper FKs.
 */
@Entity
@Table(name = "vehicle")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", nullable = false, unique = true, length = 6)
    private String licensePlate;

    /** Guardada en mayusculas para compararla con la marca de los bonos. */
    @Column(nullable = false, length = 50)
    private String brand;

    @Column(nullable = false, length = 50)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "manufacture_year", nullable = false)
    private Integer manufactureYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "engine_type", nullable = false, length = 20)
    private EngineType engineType;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private Integer seats;
}
