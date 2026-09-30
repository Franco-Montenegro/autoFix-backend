package com.mingeso.backend.controller;

import com.mingeso.backend.dto.BonusRequest;
import com.mingeso.backend.dto.BonusResponse;
import com.mingeso.backend.service.BonusService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bonuses")
@RequiredArgsConstructor
public class BonusController {

    private final BonusService bonusService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BonusResponse create(@Valid @RequestBody BonusRequest request) {
        return bonusService.create(request);
    }

    @GetMapping
    public List<BonusResponse> findByPeriod(
            @RequestParam int year,
            @RequestParam @Min(value = 1, message = "El mes debe estar entre 1 y 12")
            @Max(value = 12, message = "El mes debe estar entre 1 y 12") int month) {
        return bonusService.findByPeriod(year, month);
    }
}
