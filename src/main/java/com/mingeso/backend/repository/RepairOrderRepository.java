package com.mingeso.backend.repository;

import com.mingeso.backend.entity.Bonus;
import com.mingeso.backend.entity.RepairOrder;
import com.mingeso.backend.entity.Vehicle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
