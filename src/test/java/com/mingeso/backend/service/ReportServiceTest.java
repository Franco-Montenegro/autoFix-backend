package com.mingeso.backend.service;

import com.mingeso.backend.dto.CountAmount;
import com.mingeso.backend.dto.R1ReportResponse;
import com.mingeso.backend.dto.R2ReportResponse;
import com.mingeso.backend.dto.R3ReportResponse;
import com.mingeso.backend.dto.R4ReportResponse;
import com.mingeso.backend.dto.RepairMatrixRow;
import com.mingeso.backend.dto.RepairOrderStatus;
import com.mingeso.backend.entity.EngineType;
import com.mingeso.backend.entity.RepairOrder;
import com.mingeso.backend.entity.RepairType;
import com.mingeso.backend.entity.Vehicle;
import com.mingeso.backend.entity.VehicleType;
import com.mingeso.backend.exception.BadRequestException;
import com.mingeso.backend.repository.RepairOrderItemRepository;
import com.mingeso.backend.repository.RepairOrderItemRepository.RepairTypeByEngineType;
import com.mingeso.backend.repository.RepairOrderItemRepository.RepairTypeByVehicleType;
import com.mingeso.backend.repository.RepairOrderRepository;
import com.mingeso.backend.repository.RepairOrderRepository.RepairTime;
import com.mingeso.backend.repository.RepairTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private static final LocalDate FROM = LocalDate.of(2026, 9, 1);
    private static final LocalDate TO = LocalDate.of(2026, 9, 30);
    /** El rango incluye ambos dias: se consulta [from 00:00, to + 1 dia 00:00). */
    private static final LocalDateTime FROM_START = LocalDateTime.of(2026, 9, 1, 0, 0);
    private static final LocalDateTime TO_EXCLUSIVE = LocalDateTime.of(2026, 10, 1, 0, 0);

    private static final List<RepairType> CATALOG = List.of(
            TestData.repairType(1, "Frenos", 100_000, 120_000, 180_000, 220_000),
            TestData.repairType(2, "Motor", 350_000, 450_000, 700_000, 800_000));

    @Mock
    private RepairOrderRepository repairOrderRepository;

    @Mock
    private RepairOrderItemRepository repairOrderItemRepository;

    @Mock
    private RepairTypeRepository repairTypeRepository;

    @InjectMocks
    private ReportService reportService;

    /** Implementaciones simples de las proyecciones que devuelven las consultas. */
    private record ByVehicleType(Integer repairTypeId, VehicleType vehicleType, long count, BigDecimal amount)
            implements RepairTypeByVehicleType {
        public Integer getRepairTypeId() { return repairTypeId; }
        public VehicleType getVehicleType() { return vehicleType; }
        public long getCount() { return count; }
        public BigDecimal getAmount() { return amount; }
    }

    private record ByEngineType(Integer repairTypeId, EngineType engineType, long count, BigDecimal amount)
            implements RepairTypeByEngineType {
        public Integer getRepairTypeId() { return repairTypeId; }
        public EngineType getEngineType() { return engineType; }
        public long getCount() { return count; }
        public BigDecimal getAmount() { return amount; }
    }

    private record Time(VehicleType vehicleType, LocalDateTime entryDateTime, LocalDateTime readyDateTime)
            implements RepairTime {
        public VehicleType getVehicleType() { return vehicleType; }
        public LocalDateTime getEntryDateTime() { return entryDateTime; }
        public LocalDateTime getReadyDateTime() { return readyDateTime; }
    }

    @Test
    void r1ListsOrdersAndTotalsOnlyCalculatedOnes() {
        Vehicle vehicle = TestData.toyotaSedan();
        RepairOrder delivered = TestData.order(1L, vehicle, LocalDateTime.of(2026, 9, 2, 10, 0));
        delivered.setReadyDateTime(LocalDateTime.of(2026, 9, 3, 10, 0));
        delivered.setPickupDateTime(LocalDateTime.of(2026, 9, 4, 10, 0));
        delivered.setRepairsSubtotal(new BigDecimal("100000"));
        delivered.setMileageSurcharge(new BigDecimal("7000"));
        delivered.setAgeSurcharge(new BigDecimal("5000"));
        delivered.setDelaySurcharge(new BigDecimal("5000"));
        delivered.setRepairCountDiscount(new BigDecimal("10000"));
        delivered.setDayDiscount(BigDecimal.ZERO);
        delivered.setBonusDiscount(new BigDecimal("20000"));
        delivered.setTaxAmount(new BigDecimal("16530"));
        delivered.setTotalAmount(new BigDecimal("103530"));
        RepairOrder inRepair = TestData.order(2L, vehicle, LocalDateTime.of(2026, 9, 20, 9, 0));
        when(repairOrderRepository.findByEntryDateTimeGreaterThanEqualAndEntryDateTimeLessThanOrderByEntryDateTimeAscIdAsc(
                FROM_START, TO_EXCLUSIVE)).thenReturn(List.of(delivered, inRepair));

        R1ReportResponse report = reportService.r1(FROM, TO);

        assertThat(report.from()).isEqualTo(FROM);
        assertThat(report.to()).isEqualTo(TO);
        assertThat(report.rows()).extracting(R1ReportResponse.Row::repairOrderId).containsExactly(1L, 2L);
        assertThat(report.rows()).extracting(R1ReportResponse.Row::status)
                .containsExactly(RepairOrderStatus.DELIVERED, RepairOrderStatus.IN_REPAIR);

        R1ReportResponse.Row first = report.rows().get(0);
        assertThat(first.licensePlate()).isEqualTo("ABCD12");
        assertThat(first.brand()).isEqualTo("TOYOTA");
        assertThat(first.vehicleType()).isEqualTo(VehicleType.SEDAN);
        assertThat(first.engineType()).isEqualTo(EngineType.GASOLINE);
        assertThat(first.totalAmount()).isEqualByComparingTo("103530");
        // Un ingreso sin costo calculado trae los montos en null
        assertThat(report.rows().get(1).totalAmount()).isNull();
        assertThat(report.rows().get(1).repairsSubtotal()).isNull();

        R1ReportResponse.Totals totals = report.totals();
        assertThat(totals.orderCount()).isEqualTo(2);
        assertThat(totals.calculatedCount()).isEqualTo(1);
        assertThat(totals.repairsSubtotal()).isEqualByComparingTo("100000");
        assertThat(totals.bonusDiscount()).isEqualByComparingTo("20000");
        assertThat(totals.taxAmount()).isEqualByComparingTo("16530");
        assertThat(totals.totalAmount()).isEqualByComparingTo("103530");
    }

    @Test
    void r1WithoutOrdersHasZeroTotals() {
        LocalDate day = LocalDate.of(2026, 9, 15);
        // Un rango de un solo dia consulta [dia 00:00, dia siguiente 00:00)
        when(repairOrderRepository.findByEntryDateTimeGreaterThanEqualAndEntryDateTimeLessThanOrderByEntryDateTimeAscIdAsc(
                day.atStartOfDay(), day.plusDays(1).atStartOfDay())).thenReturn(List.of());

        R1ReportResponse report = reportService.r1(day, day);

        assertThat(report.rows()).isEmpty();
        assertThat(report.totals().orderCount()).isZero();
        assertThat(report.totals().totalAmount()).isEqualByComparingTo("0");
    }

    @Test
    void r2CrossesRepairTypesWithVehicleTypes() {
        when(repairTypeRepository.findAll(Sort.by("id"))).thenReturn(CATALOG);
        when(repairOrderItemRepository.sumDeliveredByRepairTypeAndVehicleType(FROM_START, TO_EXCLUSIVE)).thenReturn(List.of(
                new ByVehicleType(1, VehicleType.SEDAN, 3, new BigDecimal("300000")),
                new ByVehicleType(2, VehicleType.SUV, 1, new BigDecimal("350000"))));

        R2ReportResponse report = reportService.r2(FROM, TO);

        assertThat(report.vehicleTypes()).containsExactly(VehicleType.values());
        assertThat(report.rows()).extracting(RepairMatrixRow::repairTypeId).containsExactly(2, 1);
        assertThat(report.rows().get(1).cells().get(VehicleType.SEDAN))
                .isEqualTo(new CountAmount(3, new BigDecimal("300000")));
        assertThat(report.columnTotals().get(VehicleType.SUV)).isEqualTo(new CountAmount(1, new BigDecimal("350000")));
        assertThat(report.grandTotal()).isEqualTo(new CountAmount(4, new BigDecimal("650000")));
    }

    @Test
    void r3ComputesHoursPerVehicleTypeAndIncludesTypesWithoutData() {
        LocalDateTime entry = LocalDateTime.of(2026, 9, 7, 9, 0);
        when(repairOrderRepository.findRepairTimes(FROM_START, TO_EXCLUSIVE)).thenReturn(List.of(
                new Time(VehicleType.SEDAN, entry, entry.plusHours(2).plusMinutes(30)),
                new Time(VehicleType.SEDAN, entry, entry.plusHours(4)),
                new Time(VehicleType.SUV, entry, entry.plusHours(1).plusMinutes(20))));

        R3ReportResponse report = reportService.r3(FROM, TO);

        assertThat(report.unit()).isEqualTo("HOURS");
        assertThat(report.rows()).extracting(R3ReportResponse.Row::vehicleType).containsExactly(VehicleType.values());

        R3ReportResponse.Row sedan = report.rows().get(VehicleType.SEDAN.ordinal());
        assertThat(sedan.count()).isEqualTo(2);
        assertThat(sedan.average()).isEqualByComparingTo("3.25");
        assertThat(sedan.standardDeviation()).isEqualByComparingTo("0.75");
        assertThat(sedan.min()).isEqualByComparingTo("2.50");
        assertThat(sedan.max()).isEqualByComparingTo("4.00");
        assertThat(sedan.p90()).isEqualByComparingTo("4.00");

        // 1 h 20 min = 1,333... h
        R3ReportResponse.Row suv = report.rows().get(VehicleType.SUV.ordinal());
        assertThat(suv.count()).isEqualTo(1);
        assertThat(suv.average()).isEqualByComparingTo("1.33");

        R3ReportResponse.Row van = report.rows().get(VehicleType.VAN.ordinal());
        assertThat(van.count()).isZero();
        assertThat(van.average()).isNull();
        assertThat(van.p90()).isNull();
    }

    @Test
    void r4CrossesRepairTypesWithEngineTypes() {
        when(repairTypeRepository.findAll(Sort.by("id"))).thenReturn(CATALOG);
        when(repairOrderItemRepository.sumDeliveredByRepairTypeAndEngineType(FROM_START, TO_EXCLUSIVE)).thenReturn(List.of(
                new ByEngineType(1, EngineType.ELECTRIC, 2, new BigDecimal("440000")),
                new ByEngineType(2, EngineType.DIESEL, 1, new BigDecimal("450000"))));

        R4ReportResponse report = reportService.r4(FROM, TO);

        assertThat(report.engineTypes()).containsExactly(EngineType.values());
        assertThat(report.rows()).extracting(RepairMatrixRow::repairTypeId).containsExactly(2, 1);
        assertThat(report.rows().get(1).cells()).containsOnlyKeys(EngineType.values());
        assertThat(report.columnTotals().get(EngineType.ELECTRIC)).isEqualTo(new CountAmount(2, new BigDecimal("440000")));
        assertThat(report.columnTotals().get(EngineType.GASOLINE)).isEqualTo(CountAmount.EMPTY);
        assertThat(report.grandTotal()).isEqualTo(new CountAmount(3, new BigDecimal("890000")));
    }

    @Test
    void everyReportRejectsFromAfterTo() {
        String message = "La fecha 'from' (2026-09-30) no puede ser posterior a 'to' (2026-09-01)";

        assertThatThrownBy(() -> reportService.r1(TO, FROM)).isInstanceOf(BadRequestException.class).hasMessage(message);
        assertThatThrownBy(() -> reportService.r2(TO, FROM)).isInstanceOf(BadRequestException.class).hasMessage(message);
        assertThatThrownBy(() -> reportService.r3(TO, FROM)).isInstanceOf(BadRequestException.class).hasMessage(message);
        assertThatThrownBy(() -> reportService.r4(TO, FROM)).isInstanceOf(BadRequestException.class).hasMessage(message);
        verifyNoInteractions(repairOrderRepository, repairOrderItemRepository, repairTypeRepository);
    }
}
