package com.mingeso.backend.service;

import com.mingeso.backend.entity.Bonus;
import com.mingeso.backend.entity.EngineType;
import com.mingeso.backend.entity.RepairOrder;
import com.mingeso.backend.entity.RepairOrderItem;
import com.mingeso.backend.entity.RepairType;
import com.mingeso.backend.entity.Vehicle;
import com.mingeso.backend.entity.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Entidades de ejemplo para las pruebas unitarias de los servicios. */
public final class TestData {

    private TestData() {
    }

    public static Vehicle vehicle(String licensePlate, String brand, VehicleType vehicleType,
                                  EngineType engineType, int manufactureYear) {
        return Vehicle.builder()
                .id(1L)
                .licensePlate(licensePlate)
                .brand(brand)
                .model("COROLLA")
                .vehicleType(vehicleType)
                .engineType(engineType)
                .manufactureYear(manufactureYear)
                .seats(5)
                .build();
    }

    public static Vehicle toyotaSedan() {
        return vehicle("ABCD12", "TOYOTA", VehicleType.SEDAN, EngineType.GASOLINE, 2020);
    }

    public static RepairType repairType(int id, String name, long gasoline, long diesel, long hybrid, long electric) {
        return RepairType.builder()
                .id(id)
                .name(name)
                .priceGasoline(BigDecimal.valueOf(gasoline))
                .priceDiesel(BigDecimal.valueOf(diesel))
                .priceHybrid(BigDecimal.valueOf(hybrid))
                .priceElectric(BigDecimal.valueOf(electric))
                .build();
    }

    public static Bonus bonus(long id, String brand, int year, int month, int quantity, long amount) {
        return Bonus.builder()
                .id(id)
                .brand(brand)
                .periodYear(year)
                .periodMonth(month)
                .quantity(quantity)
                .amount(BigDecimal.valueOf(amount))
                .build();
    }

    /** Ingreso en reparacion con una linea por cada tipo, al precio de gasolina. */
    public static RepairOrder order(long id, Vehicle vehicle, LocalDateTime entry, RepairType... repairTypes) {
        RepairOrder order = RepairOrder.builder()
                .id(id)
                .vehicle(vehicle)
                .entryDateTime(entry)
                .mileage(10_000)
                .build();
        for (RepairType repairType : repairTypes) {
            order.addItem(RepairOrderItem.builder()
                    .repairType(repairType)
                    .price(repairType.getPriceGasoline())
                    .build());
        }
        return order;
    }
}
