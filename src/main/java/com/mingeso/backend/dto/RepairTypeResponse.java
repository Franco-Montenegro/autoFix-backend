package com.mingeso.backend.dto;

import com.mingeso.backend.entity.RepairType;

import java.math.BigDecimal;

public record RepairTypeResponse(
        Integer id,
        String name,
        BigDecimal priceGasoline,
        BigDecimal priceDiesel,
        BigDecimal priceHybrid,
        BigDecimal priceElectric
) {
    public static RepairTypeResponse from(RepairType repairType) {
        return new RepairTypeResponse(
                repairType.getId(),
                repairType.getName(),
                repairType.getPriceGasoline(),
                repairType.getPriceDiesel(),
                repairType.getPriceHybrid(),
                repairType.getPriceElectric()
        );
    }
}
