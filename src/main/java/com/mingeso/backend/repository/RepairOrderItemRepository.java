package com.mingeso.backend.repository;

import com.mingeso.backend.entity.RepairOrderItem;
import com.mingeso.backend.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface RepairOrderItemRepository extends JpaRepository<RepairOrderItem, Long> {

    /** Cantidad de lineas y suma de precios por reparacion y tipo de vehiculo. */
    interface RepairTypeByVehicleType {
        Integer getRepairTypeId();

        VehicleType getVehicleType();

        long getCount();

        BigDecimal getAmount();
    }

    /**
     * Agrega las lineas de los ingresos entregados (con retiro) cuya fecha de ingreso esta en [from, to).
     */
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
}
