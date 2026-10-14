package com.mingeso.backend.config;

import com.mingeso.backend.dto.BonusRequest;
import com.mingeso.backend.dto.RepairOrderCreateRequest;
import com.mingeso.backend.dto.VehicleRequest;
import com.mingeso.backend.entity.EngineType;
import com.mingeso.backend.entity.VehicleType;
import com.mingeso.backend.repository.VehicleRepository;
import com.mingeso.backend.service.BonusService;
import com.mingeso.backend.service.RepairOrderService;
import com.mingeso.backend.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos de demostracion para visualizar el frontend (vehiculos, bonos e ingresos).
 * Solo se activa con SEED_DEMO_DATA=true y solo si no hay vehiculos registrados.
 * Usa los servicios, asi los costos se calculan con las mismas reglas que la API.
 */
@Slf4j
@Component
@Order(2)
@ConditionalOnProperty(name = "app.seed.demo-data", havingValue = "true")
@RequiredArgsConstructor
public class DemoDataLoader implements ApplicationRunner {

    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;
    private final BonusService bonusService;
    private final RepairOrderService repairOrderService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (vehicleRepository.count() > 0) {
            log.info("Datos de demo omitidos: ya existen vehiculos registrados");
            return;
        }

        vehicleService.create(new VehicleRequest("ABCD12", "Toyota", "Corolla", VehicleType.SEDAN, 2018, EngineType.GASOLINE, 5));
        vehicleService.create(new VehicleRequest("BCDF34", "Ford", "Ranger", VehicleType.PICKUP, 2015, EngineType.DIESEL, 5));
        vehicleService.create(new VehicleRequest("CDFG56", "Hyundai", "Tucson", VehicleType.SUV, 2021, EngineType.HYBRID, 5));
        vehicleService.create(new VehicleRequest("DFGH78", "Honda", "e", VehicleType.HATCHBACK, 2022, EngineType.ELECTRIC, 4));

        Long hyundaiSeptember = bonusService.create(bonus("HYUNDAI", 2026, 9, 3, 40_000)).id();
        bonusService.create(bonus("TOYOTA", 2026, 10, 5, 50_000));
        Long fordOctober = bonusService.create(bonus("FORD", 2026, 10, 3, 60_000)).id();
        Long hondaOctober = bonusService.create(bonus("HONDA", 2026, 10, 2, 80_000)).id();

        // Entregado: lunes en horario de descuento
        Long order = createOrder("ABCD12", LocalDateTime.of(2026, 8, 3, 10, 0), 45_000, List.of(1, 7));
        deliver(order, LocalDateTime.of(2026, 8, 5, 17, 0), LocalDateTime.of(2026, 8, 6, 10, 0));

        // Entregado: diesel con alto kilometraje y retiro con atraso
        order = createOrder("BCDF34", LocalDateTime.of(2026, 8, 19, 15, 30), 128_000, List.of(3, 10));
        deliver(order, LocalDateTime.of(2026, 8, 25, 18, 0), LocalDateTime.of(2026, 8, 29, 11, 0));

        // Entregado con bono
        order = createOrder("CDFG56", LocalDateTime.of(2026, 9, 10, 11, 0), 22_000, List.of(2, 5, 9));
        repairOrderService.assignBonus(order, hyundaiSeptember);
        deliver(order, LocalDateTime.of(2026, 9, 12, 13, 0), LocalDateTime.of(2026, 9, 12, 16, 30));

        // Entregado: segunda visita del mismo vehiculo (descuento por numero de reparaciones)
        order = createOrder("ABCD12", LocalDateTime.of(2026, 9, 21, 16, 0), 52_000, List.of(8));
        deliver(order, LocalDateTime.of(2026, 9, 23, 12, 0), LocalDateTime.of(2026, 9, 24, 9, 30));

        // Listo para retiro, con bono asignado
        order = createOrder("DFGH78", LocalDateTime.of(2026, 10, 5, 9, 30), 8_000, List.of(11, 7));
        repairOrderService.assignBonus(order, hondaOctober);
        repairOrderService.registerReady(order, LocalDateTime.of(2026, 10, 7, 15, 0));

        // En reparacion, con bono asignado
        order = createOrder("BCDF34", LocalDateTime.of(2026, 10, 8, 14, 0), 131_500, List.of(1));
        repairOrderService.assignBonus(order, fordOctober);

        log.info("Datos de demo cargados: 4 vehiculos, 4 bonos y 6 ingresos");
    }

    private Long createOrder(String licensePlate, LocalDateTime entry, int mileage, List<Integer> repairTypeIds) {
        return repairOrderService.create(new RepairOrderCreateRequest(
                licensePlate, entry, mileage, repairTypeIds)).id();
    }

    private void deliver(Long orderId, LocalDateTime ready, LocalDateTime pickup) {
        repairOrderService.registerReady(orderId, ready);
        repairOrderService.registerPickup(orderId, pickup);
    }

    private static BonusRequest bonus(String brand, int year, int month, int quantity, long amount) {
        return new BonusRequest(brand, year, month, quantity, BigDecimal.valueOf(amount));
    }
}
