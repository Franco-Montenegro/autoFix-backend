package com.mingeso.backend.dto;

import com.mingeso.backend.entity.Bonus;
import com.mingeso.backend.entity.RepairOrder;
import com.mingeso.backend.entity.RepairOrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record RepairOrderResponse(
        Long id,
        String licensePlate,
        LocalDateTime entryDateTime,
        Integer mileage,
        LocalDateTime readyDateTime,
        LocalDateTime pickupDateTime,
        RepairOrderStatus status,
        List<Item> items,
        BonusInfo bonus,
        Cost cost
) {
    public record Item(Integer repairTypeId, String name, BigDecimal price) {

        public static Item from(RepairOrderItem item) {
            return new Item(item.getRepairType().getId(), item.getRepairType().getName(), item.getPrice());
        }
    }

    /** Bono asignado al ingreso; amount es el valor nominal del bono. */
    public record BonusInfo(Long id, String brand, BigDecimal amount) {

        public static BonusInfo from(Bonus bonus) {
            return bonus == null ? null : new BonusInfo(bonus.getId(), bonus.getBrand(), bonus.getAmount());
        }
    }

    /** Desglose del costo; null mientras no se haya calculado. */
    public record Cost(
            BigDecimal repairsSubtotal,
            BigDecimal mileageSurcharge,
            BigDecimal ageSurcharge,
            BigDecimal delaySurcharge,
            BigDecimal repairCountDiscount,
            BigDecimal dayDiscount,
            BigDecimal bonusDiscount,
            BigDecimal taxAmount,
            BigDecimal totalAmount
    ) {
        public static Cost from(RepairOrder order) {
            if (order.getTotalAmount() == null) {
                return null;
            }
            return new Cost(
                    order.getRepairsSubtotal(),
                    order.getMileageSurcharge(),
                    order.getAgeSurcharge(),
                    order.getDelaySurcharge(),
                    order.getRepairCountDiscount(),
                    order.getDayDiscount(),
                    order.getBonusDiscount(),
                    order.getTaxAmount(),
                    order.getTotalAmount()
            );
        }
    }

    public static RepairOrderResponse from(RepairOrder order, RepairOrderStatus status) {
        return new RepairOrderResponse(
                order.getId(),
                order.getVehicle().getLicensePlate(),
                order.getEntryDateTime(),
                order.getMileage(),
                order.getReadyDateTime(),
                order.getPickupDateTime(),
                status,
                order.getItems().stream()
                        .map(Item::from)
                        .sorted(Comparator.comparing(Item::repairTypeId))
                        .toList(),
                BonusInfo.from(order.getBonus()),
                Cost.from(order)
        );
    }
}
