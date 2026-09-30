package com.mingeso.backend.service;

import com.mingeso.backend.dto.R1ReportResponse;
import com.mingeso.backend.dto.R2ReportResponse;
import com.mingeso.backend.dto.R2ReportResponse.Cell;
import com.mingeso.backend.dto.R3ReportResponse;
import com.mingeso.backend.entity.RepairType;
import com.mingeso.backend.entity.VehicleType;
import com.mingeso.backend.exception.BadRequestException;
import com.mingeso.backend.repository.RepairOrderItemRepository;
import com.mingeso.backend.repository.RepairOrderItemRepository.RepairTypeByVehicleType;
import com.mingeso.backend.repository.RepairOrderRepository;
import com.mingeso.backend.repository.RepairOrderRepository.RepairTime;
import com.mingeso.backend.repository.RepairTypeRepository;
import com.mingeso.backend.service.report.DescriptiveStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reportes R1-R4. Todos filtran los ingresos por fecha de ingreso, con ambos dias del rango incluidos.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final double SECONDS_PER_HOUR = 3600.0;

    private final RepairOrderRepository repairOrderRepository;
    private final RepairOrderItemRepository repairOrderItemRepository;
    private final RepairTypeRepository repairTypeRepository;

    @Transactional(readOnly = true)
    public R1ReportResponse r1(LocalDate from, LocalDate to) {
        validateRange(from, to);
        List<R1ReportResponse.Row> rows = repairOrderRepository
                .findByEntryDateTimeGreaterThanEqualAndEntryDateTimeLessThanOrderByEntryDateTimeAscIdAsc(
                        from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .stream()
                .map(order -> R1ReportResponse.Row.from(order, RepairOrderService.statusOf(order)))
                .toList();
        return R1ReportResponse.of(from, to, rows);
    }

    /** R2: reparaciones vs tipos de vehiculo, solo ingresos entregados; filas por monto total descendente. */
    @Transactional(readOnly = true)
    public R2ReportResponse r2(LocalDate from, LocalDate to) {
        validateRange(from, to);

        // repairTypeId -> (vehicleType -> celda) con lo que devuelve la BD; lo que falta es 0.
        Map<Integer, Map<VehicleType, Cell>> found = new HashMap<>();
        for (RepairTypeByVehicleType agg : repairOrderItemRepository.sumDeliveredByRepairTypeAndVehicleType(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay())) {
            found.computeIfAbsent(agg.getRepairTypeId(), id -> new EnumMap<>(VehicleType.class))
                    .put(agg.getVehicleType(), new Cell(agg.getCount(), agg.getAmount()));
        }

        Map<VehicleType, Cell> columnTotals = emptyCellsByVehicleType();
        Cell grandTotal = Cell.EMPTY;
        List<R2ReportResponse.Row> rows = new ArrayList<>();

        for (RepairType repairType : repairTypeRepository.findAll(Sort.by("id"))) {
            Map<VehicleType, Cell> cells = emptyCellsByVehicleType();
            cells.putAll(found.getOrDefault(repairType.getId(), Map.of()));

            Cell rowTotal = cells.values().stream().reduce(Cell.EMPTY, Cell::plus);
            cells.forEach((type, cell) -> columnTotals.merge(type, cell, Cell::plus));
            grandTotal = grandTotal.plus(rowTotal);

            rows.add(new R2ReportResponse.Row(repairType.getId(), repairType.getName(), cells, rowTotal));
        }

        rows.sort(Comparator
                .comparing((R2ReportResponse.Row row) -> row.total().amount()).reversed()
                .thenComparing(R2ReportResponse.Row::repairTypeId));

        return new R2ReportResponse(from, to, Arrays.asList(VehicleType.values()), rows, columnTotals, grandTotal);
    }

    /** R3: estadisticas del tiempo salida - ingreso (horas) por tipo de vehiculo; ingresos con salida registrada. */
    @Transactional(readOnly = true)
    public R3ReportResponse r3(LocalDate from, LocalDate to) {
        validateRange(from, to);

        Map<VehicleType, List<Double>> hoursByType = new EnumMap<>(VehicleType.class);
        for (VehicleType type : VehicleType.values()) {
            hoursByType.put(type, new ArrayList<>());
        }
        for (RepairTime time : repairOrderRepository.findRepairTimes(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay())) {
            long seconds = Duration.between(time.getEntryDateTime(), time.getReadyDateTime()).toSeconds();
            hoursByType.get(time.getVehicleType()).add(seconds / SECONDS_PER_HOUR);
        }

        List<R3ReportResponse.Row> rows = hoursByType.entrySet().stream()
                .map(entry -> {
                    DescriptiveStatistics.Result stats = DescriptiveStatistics.of(entry.getValue());
                    return new R3ReportResponse.Row(entry.getKey(), stats.count(), stats.average(),
                            stats.standardDeviation(), stats.min(), stats.max(), stats.p90());
                })
                .toList();
        return new R3ReportResponse(from, to, "HOURS", rows);
    }

    private static Map<VehicleType, Cell> emptyCellsByVehicleType() {
        Map<VehicleType, Cell> cells = new EnumMap<>(VehicleType.class);
        for (VehicleType type : VehicleType.values()) {
            cells.put(type, Cell.EMPTY);
        }
        return cells;
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BadRequestException("La fecha 'from' (" + from + ") no puede ser posterior a 'to' (" + to + ")");
        }
    }
}
