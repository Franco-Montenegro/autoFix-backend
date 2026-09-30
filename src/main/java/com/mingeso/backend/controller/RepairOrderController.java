package com.mingeso.backend.controller;

import com.mingeso.backend.dto.BonusAssignRequest;
import com.mingeso.backend.dto.RepairOrderCreateRequest;
import com.mingeso.backend.dto.RepairOrderDateRequest;
import com.mingeso.backend.dto.RepairOrderResponse;
import com.mingeso.backend.service.RepairOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/repair-orders")
@RequiredArgsConstructor
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    @PostMapping
    public ResponseEntity<RepairOrderResponse> create(@Valid @RequestBody RepairOrderCreateRequest request) {
        RepairOrderResponse created = repairOrderService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public RepairOrderResponse findById(@PathVariable Long id) {
        return repairOrderService.findById(id);
    }

    @PatchMapping("/{id}/ready")
    public RepairOrderResponse registerReady(@PathVariable Long id,
                                             @Valid @RequestBody RepairOrderDateRequest request) {
        return repairOrderService.registerReady(id, request.dateTime());
    }

    @PatchMapping("/{id}/pickup")
    public RepairOrderResponse registerPickup(@PathVariable Long id,
                                              @Valid @RequestBody RepairOrderDateRequest request) {
        return repairOrderService.registerPickup(id, request.dateTime());
    }

    @PutMapping("/{id}/bonus")
    public RepairOrderResponse assignBonus(@PathVariable Long id,
                                           @Valid @RequestBody BonusAssignRequest request) {
        return repairOrderService.assignBonus(id, request.bonusId());
    }

    @DeleteMapping("/{id}/bonus")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBonus(@PathVariable Long id) {
        repairOrderService.removeBonus(id);
    }

    /** Recalcula el costo de un ingreso entregado. */
    @PostMapping("/{id}/cost")
    public RepairOrderResponse recalculateCost(@PathVariable Long id) {
        return repairOrderService.recalculateCost(id);
    }
}
