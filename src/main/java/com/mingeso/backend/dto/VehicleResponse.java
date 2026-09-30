package com.mingeso.backend.dto;

import com.mingeso.backend.entity.EngineType;
import com.mingeso.backend.entity.Vehicle;
import com.mingeso.backend.entity.VehicleType;

public record VehicleResponse(
        Long id,
        String licensePlate,
        String brand,
        String model,
        VehicleType vehicleType,
        Integer manufactureYear,
        EngineType engineType,
        Integer seats
) {
    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getVehicleType(),
                vehicle.getManufactureYear(),
                vehicle.getEngineType(),
                vehicle.getSeats()
        );
    }
}
