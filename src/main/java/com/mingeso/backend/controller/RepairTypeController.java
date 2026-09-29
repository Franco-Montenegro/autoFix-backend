package com.mingeso.backend.controller;

import com.mingeso.backend.dto.RepairTypeResponse;
import com.mingeso.backend.service.RepairTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/repair-types")
@RequiredArgsConstructor
public class RepairTypeController {

    private final RepairTypeService repairTypeService;

    @GetMapping
    public List<RepairTypeResponse> findAll() {
        return repairTypeService.findAll();
    }
}
