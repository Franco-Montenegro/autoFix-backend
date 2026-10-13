package com.mingeso.backend.service.cost;

import com.mingeso.backend.config.CostProperties;
import com.mingeso.backend.entity.Bonus;
import com.mingeso.backend.entity.EngineType;
import com.mingeso.backend.entity.RepairOrder;
import com.mingeso.backend.entity.RepairOrderItem;
import com.mingeso.backend.entity.Vehicle;
import com.mingeso.backend.entity.VehicleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Las politicas son clases puras sin dependencias, por eso se usan las reales:
 * asi cada caso verifica la formula completa con montos calculados a mano.
 */
class RepairCostCalculatorTest {

    private final RepairCostCalculator calculator = new RepairCostCalculator(
            new RepairCountDiscountPolicy(),
            new AttentionDayDiscountPolicy(),
            new MileageSurchargePolicy(),
            new AgeSurchargePolicy(),
            new DelaySurchargePolicy(),
            new CostProperties(new BigDecimal("0.19")));

    /** Martes 2026-09-08 15:00: no aplica el descuento por dia de atencion. */
    private static final LocalDateTime TUESDAY_AFTERNOON = LocalDateTime.of(2026, 9, 8, 15, 0);

    @Test
    void appliesEverySurchargeDiscountBonusAndTax() {
        // Sedan gasolina 2016 (10 años -> 5%), 30.000 km (12%), 2 dias de retraso (10%),
        // 3 reparaciones previas (10%), bono de 70.000
        Vehicle vehicle = vehicle(VehicleType.SEDAN, EngineType.GASOLINE, 2016);
        RepairOrder order = order(vehicle, TUESDAY_AFTERNOON, 30_000,
                LocalDateTime.of(2026, 9, 10, 17, 0), LocalDateTime.of(2026, 9, 12, 9, 0),
                bonus("70000"), "100000", "50000");

        CostBreakdown cost = calculator.calculate(order, 3);

        assertThat(cost.repairsSubtotal()).isEqualByComparingTo("150000");
        assertThat(cost.mileageSurcharge()).isEqualByComparingTo("18000");
        assertThat(cost.ageSurcharge()).isEqualByComparingTo("7500");
        assertThat(cost.delaySurcharge()).isEqualByComparingTo("15000");
        assertThat(cost.repairCountDiscount()).isEqualByComparingTo("15000");
        assertThat(cost.dayDiscount()).isEqualByComparingTo("0");
        assertThat(cost.bonusDiscount()).isEqualByComparingTo("70000");
        // base = 150.000 + 18.000 + 7.500 + 15.000 - 15.000 - 70.000 = 105.500; IVA 19% = 20.045
        assertThat(cost.taxAmount()).isEqualByComparingTo("20045");
        assertThat(cost.totalAmount()).isEqualByComparingTo("125545");
    }

    @Test
    void appliesAttentionDayDiscountOnMondayMorning() {
        Vehicle vehicle = vehicle(VehicleType.SUV, EngineType.DIESEL, 2024);
        LocalDateTime mondayMorning = LocalDateTime.of(2026, 9, 7, 10, 0);
        RepairOrder order = order(vehicle, mondayMorning, 1_000,
                mondayMorning.plusHours(5), mondayMorning.plusHours(8), null, "200000");

        CostBreakdown cost = calculator.calculate(order, 0);

        assertThat(cost.dayDiscount()).isEqualByComparingTo("20000");
        assertThat(cost.mileageSurcharge()).isEqualByComparingTo("0");
        assertThat(cost.ageSurcharge()).isEqualByComparingTo("0");
        assertThat(cost.delaySurcharge()).isEqualByComparingTo("0");
        assertThat(cost.repairCountDiscount()).isEqualByComparingTo("0");
        assertThat(cost.bonusDiscount()).isEqualByComparingTo("0");
        // base = 200.000 - 20.000 = 180.000; IVA = 34.200
        assertThat(cost.taxAmount()).isEqualByComparingTo("34200");
        assertThat(cost.totalAmount()).isEqualByComparingTo("214200");
    }

    @Test
    void bonusIsCappedSoTotalBeforeTaxNeverGoesBelowZero() {
        // Hibrido con 10 reparaciones previas (25%) y lunes 09:00 (10%): base antes del bono = 6.500
        Vehicle vehicle = vehicle(VehicleType.HATCHBACK, EngineType.HYBRID, 2025);
        LocalDateTime mondayNine = LocalDateTime.of(2026, 9, 7, 9, 0);
        RepairOrder order = order(vehicle, mondayNine, 1_000,
                mondayNine.plusHours(2), mondayNine.plusHours(3), bonus("70000"), "10000");

        CostBreakdown cost = calculator.calculate(order, 10);

        assertThat(cost.repairCountDiscount()).isEqualByComparingTo("2500");
        assertThat(cost.dayDiscount()).isEqualByComparingTo("1000");
        assertThat(cost.bonusDiscount()).isEqualByComparingTo("6500");
        assertThat(cost.taxAmount()).isEqualByComparingTo("0");
        assertThat(cost.totalAmount()).isEqualByComparingTo("0");
    }

    @Test
    void roundsEachComponentToPesosHalfUp() {
        // 150 * 3% = 4,5 -> 5 (HALF_UP; con HALF_EVEN seria 4). IVA: 155 * 19% = 29,45 -> 29
        Vehicle vehicle = vehicle(VehicleType.SEDAN, EngineType.GASOLINE, 2026);
        RepairOrder order = order(vehicle, TUESDAY_AFTERNOON, 6_000,
                TUESDAY_AFTERNOON.plusHours(1), TUESDAY_AFTERNOON.plusHours(2), null, "150");

        CostBreakdown cost = calculator.calculate(order, 0);

        assertThat(cost.mileageSurcharge()).isEqualByComparingTo("5");
        assertThat(cost.taxAmount()).isEqualByComparingTo("29");
        assertThat(cost.totalAmount()).isEqualByComparingTo("184");
    }

    @Test
    void vehicleNewerThanEntryYearHasNoAgeSurcharge() {
        // Modelo 2027 ingresado en 2026: antiguedad -1, cuenta como 0
        Vehicle vehicle = vehicle(VehicleType.PICKUP, EngineType.ELECTRIC, 2027);
        RepairOrder order = order(vehicle, TUESDAY_AFTERNOON, 0,
                TUESDAY_AFTERNOON.plusHours(1), TUESDAY_AFTERNOON.plusHours(2), null, "100000");

        CostBreakdown cost = calculator.calculate(order, 0);

        assertThat(cost.ageSurcharge()).isEqualByComparingTo("0");
        assertThat(cost.totalAmount()).isEqualByComparingTo("119000");
    }

    private static Vehicle vehicle(VehicleType vehicleType, EngineType engineType, int manufactureYear) {
        return Vehicle.builder()
                .licensePlate("ABCD12")
                .brand("TOYOTA")
                .model("Test")
                .vehicleType(vehicleType)
                .engineType(engineType)
                .manufactureYear(manufactureYear)
                .seats(5)
                .build();
    }

    private static Bonus bonus(String amount) {
        return Bonus.builder()
                .brand("TOYOTA")
                .periodYear(2026)
                .periodMonth(9)
                .quantity(5)
                .amount(new BigDecimal(amount))
                .build();
    }

    private static RepairOrder order(Vehicle vehicle, LocalDateTime entry, int mileage,
                                     LocalDateTime ready, LocalDateTime pickup, Bonus bonus,
                                     String... itemPrices) {
        RepairOrder order = RepairOrder.builder()
                .vehicle(vehicle)
                .entryDateTime(entry)
                .mileage(mileage)
                .readyDateTime(ready)
                .pickupDateTime(pickup)
                .bonus(bonus)
                .build();
        Arrays.stream(itemPrices)
                .map(price -> RepairOrderItem.builder()
                        .repairOrder(order)
                        .price(new BigDecimal(price))
                        .build())
                .forEach(order.getItems()::add);
        return order;
    }
}
