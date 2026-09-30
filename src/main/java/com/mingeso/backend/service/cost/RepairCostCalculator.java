package com.mingeso.backend.service.cost;

import com.mingeso.backend.config.CostProperties;
import com.mingeso.backend.entity.RepairOrder;
import com.mingeso.backend.entity.RepairOrderItem;
import com.mingeso.backend.entity.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Costo Total = [Suma(Reparaciones) + Recargos - Descuentos] + IVA.
 * Los porcentajes se aplican sobre Suma(Reparaciones) y cada componente se redondea a pesos (HALF_UP).
 * El bono se descuenta como monto fijo, con tope para que la base antes de IVA no baje de 0.
 */
@Component
@RequiredArgsConstructor
public class RepairCostCalculator {

    private final RepairCountDiscountPolicy repairCountDiscountPolicy;
    private final AttentionDayDiscountPolicy attentionDayDiscountPolicy;
    private final MileageSurchargePolicy mileageSurchargePolicy;
    private final AgeSurchargePolicy ageSurchargePolicy;
    private final DelaySurchargePolicy delaySurchargePolicy;
    private final CostProperties costProperties;

    /**
     * @param order           ingreso con las fechas de ingreso, salida y retiro registradas
     * @param previousRepairs ingresos previos del vehiculo en los 12 meses anteriores
     */
    public CostBreakdown calculate(RepairOrder order, long previousRepairs) {
        Vehicle vehicle = order.getVehicle();

        BigDecimal subtotal = order.getItems().stream()
                .map(RepairOrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int age = order.getEntryDateTime().getYear() - vehicle.getManufactureYear();

        BigDecimal mileageSurcharge = percentOf(subtotal,
                mileageSurchargePolicy.rate(order.getMileage(), vehicle.getVehicleType()));
        BigDecimal ageSurcharge = percentOf(subtotal,
                ageSurchargePolicy.rate(age, vehicle.getVehicleType()));
        BigDecimal delaySurcharge = percentOf(subtotal,
                delaySurchargePolicy.rate(order.getReadyDateTime(), order.getPickupDateTime()));
        BigDecimal repairCountDiscount = percentOf(subtotal,
                repairCountDiscountPolicy.rate(previousRepairs, vehicle.getEngineType()));
        BigDecimal dayDiscount = percentOf(subtotal,
                attentionDayDiscountPolicy.rate(order.getEntryDateTime()));

        BigDecimal beforeBonus = subtotal
                .add(mileageSurcharge).add(ageSurcharge).add(delaySurcharge)
                .subtract(repairCountDiscount).subtract(dayDiscount);

        BigDecimal bonusDiscount = order.getBonus() == null
                ? BigDecimal.ZERO
                : order.getBonus().getAmount().min(beforeBonus.max(BigDecimal.ZERO));

        BigDecimal base = beforeBonus.subtract(bonusDiscount);
        BigDecimal tax = percentOf(base, costProperties.taxRate());

        return new CostBreakdown(
                subtotal,
                mileageSurcharge,
                ageSurcharge,
                delaySurcharge,
                repairCountDiscount,
                dayDiscount,
                bonusDiscount,
                tax,
                base.add(tax)
        );
    }

    private static BigDecimal percentOf(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate).setScale(0, RoundingMode.HALF_UP);
    }
}
