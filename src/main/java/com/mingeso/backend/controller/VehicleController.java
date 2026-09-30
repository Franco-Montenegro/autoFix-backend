package com.mingeso.backend.controller;

import com.mingeso.backend.dto.RepairOrderResponse;
import com.mingeso.backend.dto.VehicleRequest;
import com.mingeso.backend.dto.VehicleResponse;
import com.mingeso.backend.service.RepairOrderService;
import com.mingeso.backend.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final RepairOrderService repairOrderService;

    @PostMapping
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
        VehicleResponse created = vehicleService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{licensePlate}")
                .buildAndExpand(created.licensePlate())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<VehicleResponse> findAll() {
        return vehicleService.findAll();
    }

    @GetMapping("/{licensePlate}")
    public VehicleResponse findByLicensePlate(@PathVariable String licensePlate) {
        return vehicleService.findByLicensePlate(licensePlate);
    }

    /** Historial de ingresos del vehiculo, del mas reciente al mas antiguo. */
    @GetMapping("/{licensePlate}/repair-orders")
    public List<RepairOrderResponse> findRepairOrders(@PathVariable String licensePlate) {
        return repairOrderService.findByVehicle(licensePlate);
    }
}
