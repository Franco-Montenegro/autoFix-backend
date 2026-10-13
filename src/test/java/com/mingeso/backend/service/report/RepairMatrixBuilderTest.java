package com.mingeso.backend.service.report;

import com.mingeso.backend.dto.CountAmount;
import com.mingeso.backend.dto.RepairMatrixRow;
import com.mingeso.backend.entity.RepairType;
import com.mingeso.backend.entity.VehicleType;
import com.mingeso.backend.service.TestData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RepairMatrixBuilderTest {

    private static final List<RepairType> CATALOG = List.of(
            TestData.repairType(1, "Frenos", 0, 0, 0, 0),
            TestData.repairType(2, "Motor", 0, 0, 0, 0),
            TestData.repairType(3, "Escape", 0, 0, 0, 0),
            TestData.repairType(4, "Combustible", 0, 0, 0, 0));

    private static RepairMatrixBuilder.Aggregate<VehicleType> agg(int repairTypeId, VehicleType column,
                                                                 long count, long amount) {
        return new RepairMatrixBuilder.Aggregate<>(repairTypeId, column, count, BigDecimal.valueOf(amount));
    }

    private static CountAmount cell(long count, long amount) {
        return new CountAmount(count, BigDecimal.valueOf(amount));
    }

    @Test
    void buildsFullMatrixWithTotalsSortedByAmountDescending() {
        RepairMatrixBuilder.Result<VehicleType> result = RepairMatrixBuilder.build(CATALOG, VehicleType.class, List.of(
                agg(1, VehicleType.SEDAN, 2, 200_000),
                agg(1, VehicleType.SUV, 1, 150_000),
                agg(2, VehicleType.VAN, 1, 500_000)));

        // Motor (500.000), Frenos (350.000) y luego las sin datos por id
        assertThat(result.rows()).extracting(RepairMatrixRow::repairTypeId).containsExactly(2, 1, 3, 4);

        RepairMatrixRow<VehicleType> brakes = result.rows().get(1);
        assertThat(brakes.name()).isEqualTo("Frenos");
        assertThat(brakes.cells()).containsOnlyKeys(VehicleType.values());
        assertThat(brakes.cells().get(VehicleType.SEDAN)).isEqualTo(cell(2, 200_000));
        assertThat(brakes.cells().get(VehicleType.SUV)).isEqualTo(cell(1, 150_000));
        assertThat(brakes.cells().get(VehicleType.VAN)).isEqualTo(CountAmount.EMPTY);
        assertThat(brakes.total()).isEqualTo(cell(3, 350_000));

        // Una reparacion sin datos aparece igual, con todas sus celdas en 0
        RepairMatrixRow<VehicleType> exhaust = result.rows().get(2);
        assertThat(exhaust.cells().values()).hasSize(5).containsOnly(CountAmount.EMPTY);
        assertThat(exhaust.total()).isEqualTo(CountAmount.EMPTY);

        assertThat(result.columnTotals()).containsOnlyKeys(VehicleType.values());
        assertThat(result.columnTotals().get(VehicleType.SEDAN)).isEqualTo(cell(2, 200_000));
        assertThat(result.columnTotals().get(VehicleType.SUV)).isEqualTo(cell(1, 150_000));
        assertThat(result.columnTotals().get(VehicleType.VAN)).isEqualTo(cell(1, 500_000));
        assertThat(result.columnTotals().get(VehicleType.HATCHBACK)).isEqualTo(CountAmount.EMPTY);
        assertThat(result.columnTotals().get(VehicleType.PICKUP)).isEqualTo(CountAmount.EMPTY);
        assertThat(result.grandTotal()).isEqualTo(cell(4, 850_000));
    }

    @Test
    void breaksAmountTiesByRepairTypeId() {
        RepairMatrixBuilder.Result<VehicleType> result = RepairMatrixBuilder.build(CATALOG, VehicleType.class, List.of(
                agg(3, VehicleType.PICKUP, 1, 100_000),
                agg(1, VehicleType.HATCHBACK, 2, 100_000)));

        assertThat(result.rows()).extracting(RepairMatrixRow::repairTypeId).containsExactly(1, 3, 2, 4);
    }

    @Test
    void withoutDataEveryRepairTypeAppearsWithZeros() {
        RepairMatrixBuilder.Result<VehicleType> result = RepairMatrixBuilder.build(CATALOG, VehicleType.class, List.of());

        assertThat(result.rows()).extracting(RepairMatrixRow::repairTypeId).containsExactly(1, 2, 3, 4);
        assertThat(result.rows()).extracting(RepairMatrixRow::total).containsOnly(CountAmount.EMPTY);
        assertThat(result.columnTotals().values()).containsOnly(CountAmount.EMPTY);
        assertThat(result.grandTotal()).isEqualTo(CountAmount.EMPTY);
    }

    @Test
    void ignoresAggregatesOfRepairTypesOutsideTheCatalog() {
        RepairMatrixBuilder.Result<VehicleType> result = RepairMatrixBuilder.build(CATALOG, VehicleType.class, List.of(
                agg(99, VehicleType.SEDAN, 1, 900_000)));

        assertThat(result.rows()).hasSize(4);
        assertThat(result.grandTotal()).isEqualTo(CountAmount.EMPTY);
    }
}
