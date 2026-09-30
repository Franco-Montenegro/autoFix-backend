package com.mingeso.backend.dto;

import com.mingeso.backend.entity.EngineType;
import com.mingeso.backend.entity.RepairOrder;
import com.mingeso.backend.entity.Vehicle;
import com.mingeso.backend.entity.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

/**
 * Reporte R1: ingresos de un rango de fechas con cada valor de la formula y una fila de totales.
 */
public record R1ReportResponse(
        LocalDate from,
        LocalDate to,
        List<Row> rows,
        Totals totals
) {
    /** Un ingreso. Los montos son null si el costo aun no se ha calculado. */
    public record Row(
            Long repairOrderId,
            String licensePlate,
            String brand,
            String model,
            VehicleType vehicleType,
            EngineType engineType,
            LocalDateTime entryDateTime,
            LocalDateTime readyDateTime,
            LocalDateTime pickupDateTime,
            RepairOrderStatus status,
            BigDecimal repairsSubtotal,
            BigDecimal mileageSurcharge,
            BigDecimal ageSurcharge,
            BigDecimal delaySurcharge,
            BigDecimal repairCountDiscount,
            BigDecimal dayDiscount,
            BigDecimal bonusDiscount,
            BigDecimal taxAmount,
            BigDecimal totalAmount
    ) {
        public static Row from(RepairOrder order, RepairOrderStatus status) {
            Vehicle vehicle = order.getVehicle();
            return new Row(
                    order.getId(),
                    vehicle.getLicensePlate(),
                    vehicle.getBrand(),
                    vehicle.getModel(),
                    vehicle.getVehicleType(),
                    vehicle.getEngineType(),
                    order.getEntryDateTime(),
                    order.getReadyDateTime(),
                    order.getPickupDateTime(),
                    status,
                    order.getRepairsSubtotal(),
                    order.getMileageSurcharge(),
                    order.getAgeSurcharge(),
                    order.getDelaySurcharge(),
                    order.getRepairCountDiscount(),
                    order.getDayDiscount(),
                    order.getBonusDiscount(),
                    order.getTaxAmount(),
                    order.getTotalAmount()
            );
        }

        boolean isCalculated() {
            return totalAmount != null;
        }
    }

    /** Suma de cada columna, considerando solo los ingresos con costo calculado. */
    public record Totals(
            int orderCount,
            int calculatedCount,
            BigDecimal repairsSubtotal,
            BigDecimal mileageSurcharge,
            BigDecimal ageSurcharge,
            BigDecimal delaySurcharge,
            BigDecimal repairCountDiscount,
            BigDecimal dayDiscount,
            BigDecimal bonusDiscount,
            BigDecimal taxAmount,
            BigDecimal totalAmount
    ) {
        public static Totals from(List<Row> rows) {
            List<Row> calculated = rows.stream().filter(Row::isCalculated).toList();
            return new Totals(
                    rows.size(),
                    calculated.size(),
                    sum(calculated, Row::repairsSubtotal),
                    sum(calculated, Row::mileageSurcharge),
                    sum(calculated, Row::ageSurcharge),
                    sum(calculated, Row::delaySurcharge),
                    sum(calculated, Row::repairCountDiscount),
                    sum(calculated, Row::dayDiscount),
                    sum(calculated, Row::bonusDiscount),
                    sum(calculated, Row::taxAmount),
                    sum(calculated, Row::totalAmount)
            );
        }

        private static BigDecimal sum(List<Row> rows, Function<Row, BigDecimal> column) {
            return rows.stream().map(column).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public static R1ReportResponse of(LocalDate from, LocalDate to, List<Row> rows) {
        return new R1ReportResponse(from, to, rows, Totals.from(rows));
    }
}
