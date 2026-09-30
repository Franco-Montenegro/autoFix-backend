package com.mingeso.backend.service;

import com.mingeso.backend.dto.R1ReportResponse;
import com.mingeso.backend.exception.BadRequestException;
import com.mingeso.backend.repository.RepairOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Reportes R1-R4. Todos filtran los ingresos por fecha de ingreso, con ambos dias del rango incluidos.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final RepairOrderRepository repairOrderRepository;

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

    private void validateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BadRequestException("La fecha 'from' (" + from + ") no puede ser posterior a 'to' (" + to + ")");
        }
    }
}
