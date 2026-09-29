package com.mingeso.backend.config;

import com.mingeso.backend.entity.RepairType;
import com.mingeso.backend.repository.RepairTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Carga inicial idempotente del catalogo de reparaciones:
 * inserta las 11 filas solo si la tabla esta vacia.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RepairTypeDataLoader implements ApplicationRunner {

    private final RepairTypeRepository repairTypeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repairTypeRepository.count() > 0) {
            return;
        }
        List<RepairType> catalog = List.of(
                repairType(1, "Reparaciones del Sistema de Frenos", 120_000, 120_000, 180_000, 220_000),
                repairType(2, "Servicio del Sistema de Refrigeración", 130_000, 130_000, 190_000, 230_000),
                repairType(3, "Reparaciones del Motor", 350_000, 450_000, 700_000, 800_000),
                repairType(4, "Reparaciones de la Transmisión", 210_000, 210_000, 300_000, 300_000),
                repairType(5, "Reparación del Sistema Eléctrico", 150_000, 150_000, 200_000, 250_000),
                repairType(6, "Reparaciones del Sistema de Escape", 100_000, 120_000, 450_000, 0),
                repairType(7, "Reparación de Neumáticos y Ruedas", 100_000, 100_000, 100_000, 100_000),
                repairType(8, "Reparaciones de la Suspensión y la Dirección", 180_000, 180_000, 210_000, 250_000),
                repairType(9, "Reparación del Sistema de Aire Acondicionado y Calefacción", 150_000, 150_000, 180_000, 180_000),
                repairType(10, "Reparaciones del Sistema de Combustible", 130_000, 140_000, 220_000, 0),
                repairType(11, "Reparación y Reemplazo del Parabrisas y Cristales", 80_000, 80_000, 80_000, 80_000)
        );
        repairTypeRepository.saveAll(catalog);
        log.info("Catalogo de reparaciones cargado: {} registros", catalog.size());
    }

    private static RepairType repairType(int id, String name,
                                         long gasoline, long diesel, long hybrid, long electric) {
        return RepairType.builder()
                .id(id)
                .name(name)
                .priceGasoline(BigDecimal.valueOf(gasoline))
                .priceDiesel(BigDecimal.valueOf(diesel))
                .priceHybrid(BigDecimal.valueOf(hybrid))
                .priceElectric(BigDecimal.valueOf(electric))
                .build();
    }
}
