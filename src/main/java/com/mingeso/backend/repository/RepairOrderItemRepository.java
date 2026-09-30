package com.mingeso.backend.repository;

import com.mingeso.backend.entity.EngineType;
import com.mingeso.backend.entity.RepairOrderItem;
import com.mingeso.backend.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Agregaciones de lineas de reparacion para R2 y R4.
 * Ambas consideran solo ingresos entregados (con retiro) cuya fecha de ingreso esta en [from, to).
 */
public interface RepairOrderItemRepository extends JpaRepository<RepairOrderItem, Long> {

    /** Cantidad de lineas y suma de precios por reparacion y tipo de vehiculo. */
    interface RepairTypeByVehicleType {
        Integer getRepairTypeId();

        VehicleType getVehicleType();

        long getCount();

        BigDecimal getAmount();
    }

    /** Cantidad de lineas y suma de precios por reparacion y tipo de motor. */
    interface RepairTypeByEngineType {
        Integer getRepairTypeId();

        EngineType getEngineType();

        long getCount();

        BigDecimal getAmount();
    }

    @Query("""
            select i.repairType.id as repairTypeId,
                   v.vehicleType as vehicleType,
                   count(i) as count,
                   sum(i.price) as amount
            from RepairOrderItem i
                 join i.repairOrder o
                 join o.vehicle v
            where o.entryDateTime >= :from
              and o.entryDateTime < :to
              and o.pickupDateTime is not null
            group by i.repairType.id, v.vehicleType
            """)
    List<RepairTypeByVehicleType> sumDeliveredByRepairTypeAndVehicleType(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            select i.repairType.id as repairTypeId,
                   v.engineType as engineType,
                   count(i) as count,
                   sum(i.price) as amount
            from RepairOrderItem i
                 join i.repairOrder o
                 join o.vehicle v
            where o.entryDateTime >= :from
              and o.entryDateTime < :to
              and o.pickupDateTime is not null
            group by i.repairType.id, v.engineType
            """)
    List<RepairTypeByEngineType> sumDeliveredByRepairTypeAndEngineType(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
