package com.mingeso.backend.repository;

import com.mingeso.backend.entity.Bonus;
import com.mingeso.backend.entity.RepairOrder;
import com.mingeso.backend.entity.Vehicle;
import com.mingeso.backend.entity.VehicleType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {

    @EntityGraph(attributePaths = {"vehicle", "bonus", "items", "items.repairType"})
    Optional<RepairOrder> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"vehicle", "bonus", "items", "items.repairType"})
    List<RepairOrder> findByVehicleOrderByEntryDateTimeDesc(Vehicle vehicle);

    /** Ingreso abierto: el vehiculo aun no ha sido retirado. */
    boolean existsByVehicleAndPickupDateTimeIsNull(Vehicle vehicle);

    /** Ingresos del vehiculo con fecha de ingreso en [from, to). */
    long countByVehicleAndEntryDateTimeGreaterThanEqualAndEntryDateTimeLessThan(
            Vehicle vehicle, LocalDateTime from, LocalDateTime to);

    long countByBonus(Bonus bonus);

    /** Datos minimos para medir el tiempo de reparacion de un ingreso (R3). */
    interface RepairTime {
        VehicleType getVehicleType();

        LocalDateTime getEntryDateTime();

        LocalDateTime getReadyDateTime();
    }

    /** Ingresos con salida registrada y fecha de ingreso en [from, to). */
    @Query("""
            select v.vehicleType as vehicleType,
                   o.entryDateTime as entryDateTime,
                   o.readyDateTime as readyDateTime
            from RepairOrder o
                 join o.vehicle v
            where o.entryDateTime >= :from
              and o.entryDateTime < :to
              and o.readyDateTime is not null
            """)
    List<RepairTime> findRepairTimes(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Ingresos con fecha de ingreso en [from, to), con su vehiculo, para los reportes. */
    @EntityGraph(attributePaths = {"vehicle"})
    List<RepairOrder> findByEntryDateTimeGreaterThanEqualAndEntryDateTimeLessThanOrderByEntryDateTimeAscIdAsc(
            LocalDateTime from, LocalDateTime to);
}
