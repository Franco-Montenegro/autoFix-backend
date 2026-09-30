package com.mingeso.backend.controller;

import com.mingeso.backend.dto.R1ReportResponse;
import com.mingeso.backend.dto.R2ReportResponse;
import com.mingeso.backend.dto.R3ReportResponse;
import com.mingeso.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** R1: ingresos entre from y to (ambos incluidos) con el desglose del costo y totales. */
    @GetMapping("/r1")
    public R1ReportResponse r1(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.r1(from, to);
    }

    /** R2: las 11 reparaciones vs tipos de vehiculo (ingresos entregados), con totales. */
    @GetMapping("/r2")
    public R2ReportResponse r2(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.r2(from, to);
    }

    /** R3: estadisticas de tiempos de reparacion (horas) por tipo de vehiculo. */
    @GetMapping("/r3")
    public R3ReportResponse r3(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.r3(from, to);
    }
}
