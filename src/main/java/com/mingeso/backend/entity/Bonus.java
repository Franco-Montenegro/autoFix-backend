package com.mingeso.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

/**
 * Cupo mensual de bonos de descuento por marca (convenio con TopCar).
 * Los bonos disponibles no se guardan: se calculan como quantity menos los ingresos que lo usan.
 */
@Entity
@Table(name = "bonus", uniqueConstraints = @UniqueConstraint(
        name = "uk_bonus_brand_period",
        columnNames = {"brand", "period_year", "period_month"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bonus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** En mayusculas, para compararla directamente con Vehicle.brand. */
    @Column(nullable = false, length = 50)
    private String brand;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(nullable = false)
    private Integer quantity;

    /** Monto de cada bono, en pesos. */
    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal amount;
}
