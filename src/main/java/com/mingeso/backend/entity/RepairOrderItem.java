package com.mingeso.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Reparacion incluida en un ingreso. El precio se congela al registrar,
 * segun el motor del vehiculo, para que no cambie si luego cambia el catalogo.
 */
@Entity
@Table(name = "repair_order_item", uniqueConstraints = @UniqueConstraint(
        name = "uk_repair_order_item_order_type",
        columnNames = {"repair_order_id", "repair_type_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_order_id", nullable = false)
    private RepairOrder repairOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repair_type_id", nullable = false)
    private RepairType repairType;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal price;
}
