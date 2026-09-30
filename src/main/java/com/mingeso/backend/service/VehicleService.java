package com.mingeso.backend.service;

import com.mingeso.backend.dto.VehicleRequest;
import com.mingeso.backend.dto.VehicleResponse;
import com.mingeso.backend.entity.Vehicle;
import com.mingeso.backend.exception.ConflictException;
import com.mingeso.backend.exception.ResourceNotFoundException;
import com.mingeso.backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        if (vehicleRepository.existsByLicensePlate(request.licensePlate())) {
            throw new ConflictException("Ya existe un vehículo registrado con la patente " + request.licensePlate());
        }
        Vehicle vehicle = Vehicle.builder()
                .licensePlate(request.licensePlate())
                .brand(request.brand())
                .model(request.model())
                .vehicleType(request.vehicleType())
                .manufactureYear(request.manufactureYear())
                .engineType(request.engineType())
                .seats(request.seats())
                .build();
        return VehicleResponse.from(vehicleRepository.save(vehicle));
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> findAll() {
        return vehicleRepository.findAll(Sort.by("licensePlate")).stream()
                .map(VehicleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse findByLicensePlate(String licensePlate) {
        String normalized = VehicleRequest.normalizeUpper(licensePlate);
        return vehicleRepository.findByLicensePlate(normalized)
                .map(VehicleResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un vehículo con la patente " + normalized));
    }
}
