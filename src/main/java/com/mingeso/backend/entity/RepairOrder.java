package com.mingeso.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ingreso de un vehiculo al taller (HU2); cada ingreso es una entrada del historial.
 * El estado no se guarda: se deduce de readyDateTime y pickupDateTime.
 * El desglose del costo se guarda (HU3) porque R1 muestra cada componente.
 */
@Entity
@Table(name = "repair_order", indexes = {
        @Index(name = "idx_repair_order_vehicle_entry", columnList = "vehicle_id, entry_date_time"),
        @Index(name = "idx_repair_order_entry", columnList = "entry_date_time")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "entry_date_time", nullable = false)
    private LocalDateTime entryDateTime;

    @Column(nullable = false)
    private Integer mileage;

    /** Fecha/hora de salida de la reparacion. */
    @Column(name = "ready_date_time")
    private LocalDateTime readyDateTime;

    /** Fecha/hora en que el cliente se llevo el vehiculo. */
    @Column(name = "pickup_date_time")
    private LocalDateTime pickupDateTime;

    /** Como maximo un bono por ingreso. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_id")
    private Bonus bonus;

    @Builder.Default
    @OneToMany(mappedBy = "repairOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepairOrderItem> items = new ArrayList<>();

    // ---- Desglose del costo (HU3): null hasta que se calcula al registrar el retiro ----

    @Column(name = "repairs_subtotal", precision = 12, scale = 0)
    private BigDecimal repairsSubtotal;

    @Column(name = "mileage_surcharge", precision = 12, scale = 0)
    private BigDecimal mileageSurcharge;

    @Column(name = "age_surcharge", precision = 12, scale = 0)
    private BigDecimal ageSurcharge;

    @Column(name = "delay_surcharge", precision = 12, scale = 0)
    private BigDecimal delaySurcharge;

    @Column(name = "repair_count_discount", precision = 12, scale = 0)
    private BigDecimal repairCountDiscount;

    @Column(name = "day_discount", precision = 12, scale = 0)
    private BigDecimal dayDiscount;

    /** Monto del bono efectivamente aplicado (con tope para que la base no baje de 0). */
    @Column(name = "bonus_discount", precision = 12, scale = 0)
    private BigDecimal bonusDiscount;

    @Column(name = "tax_amount", precision = 12, scale = 0)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", precision = 12, scale = 0)
    private BigDecimal totalAmount;

    public void addItem(RepairOrderItem item) {
        item.setRepairOrder(this);
        items.add(item);
    }
}
