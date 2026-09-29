package com.mingeso.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Catalogo de las 11 reparaciones con su precio por tipo de motor.
 * El id es asignado (1..11) y coincide con el numero del enunciado.
 * Un precio 0 significa que la reparacion no aplica para ese motor.
 */
@Entity
@Table(name = "repair_type")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairType {

    @Id
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "price_gasoline", nullable = false, precision = 12, scale = 0)
    private BigDecimal priceGasoline;

    @Column(name = "price_diesel", nullable = false, precision = 12, scale = 0)
    private BigDecimal priceDiesel;

    @Column(name = "price_hybrid", nullable = false, precision = 12, scale = 0)
    private BigDecimal priceHybrid;

    @Column(name = "price_electric", nullable = false, precision = 12, scale = 0)
    private BigDecimal priceElectric;
}
