package com.mingeso.backend.service;

import com.mingeso.backend.dto.RepairOrderCreateRequest;
import com.mingeso.backend.dto.RepairOrderResponse;
import com.mingeso.backend.dto.RepairOrderStatus;
import com.mingeso.backend.entity.Bonus;
import com.mingeso.backend.entity.EngineType;
import com.mingeso.backend.entity.RepairOrder;
import com.mingeso.backend.entity.RepairOrderItem;
import com.mingeso.backend.entity.RepairType;
import com.mingeso.backend.entity.Vehicle;
import com.mingeso.backend.entity.VehicleType;
import com.mingeso.backend.exception.BusinessRuleException;
import com.mingeso.backend.exception.ConflictException;
import com.mingeso.backend.exception.ResourceNotFoundException;
import com.mingeso.backend.repository.BonusRepository;
import com.mingeso.backend.repository.RepairOrderRepository;
import com.mingeso.backend.repository.RepairTypeRepository;
import com.mingeso.backend.repository.VehicleRepository;
import com.mingeso.backend.service.cost.CostBreakdown;
import com.mingeso.backend.service.cost.RepairCostCalculator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairOrderServiceTest {

    /** Martes 2026-09-08 10:00. */
    private static final LocalDateTime ENTRY = LocalDateTime.of(2026, 9, 8, 10, 0);

    private static final RepairType BRAKES = TestData.repairType(1, "Frenos", 100_000, 120_000, 180_000, 220_000);
    private static final RepairType ENGINE = TestData.repairType(2, "Motor", 350_000, 450_000, 700_000, 800_000);
    private static final RepairType EXHAUST = TestData.repairType(5, "Escape", 120_000, 140_000, 180_000, 0);
    private static final RepairType FUEL = TestData.repairType(6, "Combustible", 150_000, 140_000, 180_000, 0);

    @Mock
    private RepairOrderRepository repairOrderRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private RepairTypeRepository repairTypeRepository;

    @Mock
    private BonusRepository bonusRepository;

    @Mock
    private RepairCostCalculator repairCostCalculator;

    @InjectMocks
    private RepairOrderService repairOrderService;

    private void givenOrder(RepairOrder order) {
        when(repairOrderRepository.findWithDetailsById(order.getId())).thenReturn(Optional.of(order));
    }

    private static RepairOrder orderInRepair() {
        return TestData.order(10L, TestData.toyotaSedan(), ENTRY, BRAKES);
    }

    private static RepairOrder orderReady() {
        RepairOrder order = orderInRepair();
        order.setReadyDateTime(ENTRY.plusDays(1));
        return order;
    }

    private static RepairOrder orderDelivered() {
        RepairOrder order = orderReady();
        order.setPickupDateTime(ENTRY.plusDays(3));
        return order;
    }

    @Nested
    class Create {

        private static RepairOrderCreateRequest request(Integer... repairTypeIds) {
            return new RepairOrderCreateRequest("ABCD12", ENTRY, 25_000, List.of(repairTypeIds));
        }

        private void givenVehicle(Vehicle vehicle) {
            when(vehicleRepository.findByLicensePlate(vehicle.getLicensePlate())).thenReturn(Optional.of(vehicle));
        }

        @ParameterizedTest(name = "motor {0}: frenos {1}, motor {2}")
        @CsvSource({
                "GASOLINE, 100000, 350000",
                "DIESEL,   120000, 450000",
                "HYBRID,   180000, 700000",
                "ELECTRIC, 220000, 800000"
        })
        void copiesCatalogPriceForVehicleEngine(EngineType engineType, BigDecimal brakesPrice, BigDecimal enginePrice) {
            Vehicle vehicle = TestData.vehicle("ABCD12", "TOYOTA", VehicleType.SEDAN, engineType, 2020);
            givenVehicle(vehicle);
            when(repairOrderRepository.existsByVehicleAndPickupDateTimeIsNull(vehicle)).thenReturn(false);
            when(repairTypeRepository.findAllById(List.of(2, 1))).thenReturn(List.of(BRAKES, ENGINE));
            when(repairOrderRepository.save(any(RepairOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RepairOrderResponse response = repairOrderService.create(request(2, 1));

            ArgumentCaptor<RepairOrder> captor = ArgumentCaptor.forClass(RepairOrder.class);
            verify(repairOrderRepository).save(captor.capture());
            RepairOrder saved = captor.getValue();
            assertThat(saved.getVehicle()).isSameAs(vehicle);
            assertThat(saved.getEntryDateTime()).isEqualTo(ENTRY);
            assertThat(saved.getMileage()).isEqualTo(25_000);
            // Las lineas quedan en el orden pedido y enlazadas al ingreso
            assertThat(saved.getItems()).extracting(item -> item.getRepairType().getId()).containsExactly(2, 1);
            assertThat(saved.getItems()).allSatisfy(item -> assertThat(item.getRepairOrder()).isSameAs(saved));
            assertThat(saved.getItems()).extracting(RepairOrderItem::getPrice)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(enginePrice, brakesPrice);

            assertThat(response.status()).isEqualTo(RepairOrderStatus.IN_REPAIR);
            assertThat(response.licensePlate()).isEqualTo("ABCD12");
            assertThat(response.items()).extracting(RepairOrderResponse.Item::repairTypeId).containsExactly(1, 2);
            assertThat(response.bonus()).isNull();
            assertThat(response.cost()).isNull();
        }

        @Test
        void failsWhenVehicleDoesNotExist() {
            when(vehicleRepository.findByLicensePlate("ABCD12")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> repairOrderService.create(request(1)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No existe un vehículo con la patente ABCD12");
            verify(repairOrderRepository, never()).save(any());
        }

        @Test
        void rejectsVehicleWithAnOpenOrder() {
            Vehicle vehicle = TestData.toyotaSedan();
            givenVehicle(vehicle);
            when(repairOrderRepository.existsByVehicleAndPickupDateTimeIsNull(vehicle)).thenReturn(true);

            assertThatThrownBy(() -> repairOrderService.create(request(1)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("tiene un ingreso abierto");
            verify(repairOrderRepository, never()).save(any());
        }

        @Test
        void rejectsEntryMoreThanOneYearBeforeManufactureYear() {
            Vehicle vehicle = TestData.vehicle("ABCD12", "TOYOTA", VehicleType.SEDAN, EngineType.GASOLINE, 2028);
            givenVehicle(vehicle);

            assertThatThrownBy(() -> repairOrderService.create(request(1)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("(2026)")
                    .hasMessageContaining("(2028)");
            verifyNoInteractions(repairTypeRepository);
        }

        @Test
        void acceptsNextYearModel() {
            // Modelo 2027 ingresado en 2026: permitido (como maximo un año antes)
            Vehicle vehicle = TestData.vehicle("ABCD12", "TOYOTA", VehicleType.SEDAN, EngineType.GASOLINE, 2027);
            givenVehicle(vehicle);
            when(repairTypeRepository.findAllById(List.of(1))).thenReturn(List.of(BRAKES));
            when(repairOrderRepository.save(any(RepairOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

            assertThat(repairOrderService.create(request(1)).status()).isEqualTo(RepairOrderStatus.IN_REPAIR);
        }

        @Test
        void failsWhenSomeRepairTypeDoesNotExist() {
            givenVehicle(TestData.toyotaSedan());
            when(repairTypeRepository.findAllById(List.of(1, 98, 99))).thenReturn(List.of(BRAKES));

            assertThatThrownBy(() -> repairOrderService.create(request(1, 98, 99)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No existen reparaciones con los ids: [98, 99]");
        }

        @Test
        void rejectsRepairsThatDoNotApplyToTheEngine() {
            // Escape y combustible tienen precio 0 para motor electrico
            givenVehicle(TestData.vehicle("ABCD12", "TOYOTA", VehicleType.SEDAN, EngineType.ELECTRIC, 2022));
            when(repairTypeRepository.findAllById(List.of(1, 5, 6))).thenReturn(List.of(BRAKES, EXHAUST, FUEL));

            assertThatThrownBy(() -> repairOrderService.create(request(1, 5, 6)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Las siguientes reparaciones no aplican para motor ELECTRIC: Escape, Combustible");
            verify(repairOrderRepository, never()).save(any());
        }
    }

    @Nested
    class RegisterReady {

        @Test
        void setsReadyDateAndMovesToReady() {
            RepairOrder order = orderInRepair();
            givenOrder(order);

            RepairOrderResponse response = repairOrderService.registerReady(10L, ENTRY.plusHours(6));

            assertThat(order.getReadyDateTime()).isEqualTo(ENTRY.plusHours(6));
            assertThat(response.status()).isEqualTo(RepairOrderStatus.READY);
        }

        @Test
        void acceptsReadyDateEqualToEntryDate() {
            givenOrder(orderInRepair());

            assertThat(repairOrderService.registerReady(10L, ENTRY).status()).isEqualTo(RepairOrderStatus.READY);
        }

        @Test
        void failsWhenOrderDoesNotExist() {
            when(repairOrderRepository.findWithDetailsById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> repairOrderService.registerReady(99L, ENTRY))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No existe un ingreso con id 99");
        }

        @Test
        void rejectsRegisteringReadyTwice() {
            givenOrder(orderReady());

            assertThatThrownBy(() -> repairOrderService.registerReady(10L, ENTRY.plusDays(2)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("El ingreso 10 ya tiene registrada la fecha de salida");
        }

        @Test
        void rejectsReadyDateBeforeEntryDate() {
            RepairOrder order = orderInRepair();
            givenOrder(order);

            assertThatThrownBy(() -> repairOrderService.registerReady(10L, ENTRY.minusMinutes(1)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("no puede ser anterior a la fecha de ingreso");
            assertThat(order.getReadyDateTime()).isNull();
        }
    }

    @Nested
    class RegisterPickup {

        private static final CostBreakdown COST = new CostBreakdown(
                new BigDecimal("100000"), new BigDecimal("7000"), new BigDecimal("5000"), new BigDecimal("10000"),
                new BigDecimal("5000"), new BigDecimal("0"), new BigDecimal("0"),
                new BigDecimal("22230"), new BigDecimal("139230"));

        @Test
        void setsPickupDateAndStoresCalculatedCost() {
            RepairOrder order = orderReady();
            givenOrder(order);
            // Se cuentan los ingresos previos en los 12 meses anteriores al ingreso actual
            when(repairOrderRepository.countByVehicleAndEntryDateTimeGreaterThanEqualAndEntryDateTimeLessThan(
                    order.getVehicle(), ENTRY.minusMonths(12), ENTRY)).thenReturn(2L);
            when(repairCostCalculator.calculate(order, 2L)).thenReturn(COST);

            RepairOrderResponse response = repairOrderService.registerPickup(10L, ENTRY.plusDays(3));

            assertThat(order.getPickupDateTime()).isEqualTo(ENTRY.plusDays(3));
            assertThat(response.status()).isEqualTo(RepairOrderStatus.DELIVERED);
            assertThat(response.cost()).isEqualTo(new RepairOrderResponse.Cost(
                    COST.repairsSubtotal(), COST.mileageSurcharge(), COST.ageSurcharge(), COST.delaySurcharge(),
                    COST.repairCountDiscount(), COST.dayDiscount(), COST.bonusDiscount(),
                    COST.taxAmount(), COST.totalAmount()));
            assertThat(order.getTotalAmount()).isEqualByComparingTo("139230");
        }

        @Test
        void acceptsPickupDateEqualToReadyDate() {
            RepairOrder order = orderReady();
            givenOrder(order);
            when(repairCostCalculator.calculate(any(RepairOrder.class), anyLong())).thenReturn(COST);

            RepairOrderResponse response = repairOrderService.registerPickup(10L, order.getReadyDateTime());

            assertThat(response.status()).isEqualTo(RepairOrderStatus.DELIVERED);
        }

        @Test
        void rejectsPickupBeforeReadyIsRegistered() {
            givenOrder(orderInRepair());

            assertThatThrownBy(() -> repairOrderService.registerPickup(10L, ENTRY.plusDays(1)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("No se puede registrar el retiro del ingreso 10 antes de registrar su salida");
            verifyNoInteractions(repairCostCalculator);
        }

        @Test
        void rejectsRegisteringPickupTwice() {
            givenOrder(orderDelivered());

            assertThatThrownBy(() -> repairOrderService.registerPickup(10L, ENTRY.plusDays(5)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("El ingreso 10 ya tiene registrada la fecha de retiro");
            verifyNoInteractions(repairCostCalculator);
        }

        @Test
        void rejectsPickupDateBeforeReadyDate() {
            RepairOrder order = orderReady();
            givenOrder(order);

            assertThatThrownBy(() -> repairOrderService.registerPickup(10L, order.getReadyDateTime().minusHours(1)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("no puede ser anterior a la fecha de salida");
            assertThat(order.getPickupDateTime()).isNull();
            verifyNoInteractions(repairCostCalculator);
        }

        @Test
        void recalculateCostUpdatesDeliveredOrder() {
            RepairOrder order = orderDelivered();
            givenOrder(order);
            when(repairCostCalculator.calculate(order, 0L)).thenReturn(COST);

            RepairOrderResponse response = repairOrderService.recalculateCost(10L);

            assertThat(response.cost().totalAmount()).isEqualByComparingTo("139230");
        }

        @Test
        void recalculateCostRejectsOrderNotDelivered() {
            givenOrder(orderReady());

            assertThatThrownBy(() -> repairOrderService.recalculateCost(10L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("aún no registra su retiro");
            verifyNoInteractions(repairCostCalculator);
        }
    }

    @Nested
    class Bonuses {

        private void givenBonus(Bonus bonus) {
            when(bonusRepository.findByIdForUpdate(bonus.getId())).thenReturn(Optional.of(bonus));
        }

        @Test
        void assignsBonusOfSameBrandAndMonthWithQuota() {
            RepairOrder order = orderInRepair();
            Bonus bonus = TestData.bonus(3, "TOYOTA", 2026, 9, 5, 70_000);
            givenOrder(order);
            givenBonus(bonus);
            when(repairOrderRepository.countByBonus(bonus)).thenReturn(4L);

            RepairOrderResponse response = repairOrderService.assignBonus(10L, 3L);

            assertThat(order.getBonus()).isSameAs(bonus);
            assertThat(response.bonus()).isEqualTo(
                    new RepairOrderResponse.BonusInfo(3L, "TOYOTA", BigDecimal.valueOf(70_000)));
        }

        @Test
        void rejectsAssigningToDeliveredOrder() {
            givenOrder(orderDelivered());

            assertThatThrownBy(() -> repairOrderService.assignBonus(10L, 3L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("ya fue entregado");
            verifyNoInteractions(bonusRepository);
        }

        @Test
        void rejectsOrderThatAlreadyHasBonus() {
            RepairOrder order = orderInRepair();
            order.setBonus(TestData.bonus(1, "TOYOTA", 2026, 9, 5, 70_000));
            givenOrder(order);

            assertThatThrownBy(() -> repairOrderService.assignBonus(10L, 3L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("El ingreso 10 ya tiene un bono asignado");
            verifyNoInteractions(bonusRepository);
        }

        @Test
        void failsWhenBonusDoesNotExist() {
            givenOrder(orderInRepair());
            when(bonusRepository.findByIdForUpdate(3L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> repairOrderService.assignBonus(10L, 3L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No existe un bono con id 3");
        }

        @Test
        void rejectsBonusOfAnotherBrand() {
            givenOrder(orderInRepair());
            givenBonus(TestData.bonus(3, "FORD", 2026, 9, 5, 50_000));

            assertThatThrownBy(() -> repairOrderService.assignBonus(10L, 3L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("El bono es de la marca FORD y el vehículo es TOYOTA");
        }

        @ParameterizedTest(name = "bono de {1}/{0} para un ingreso de 9/2026")
        @CsvSource({"2026, 8", "2026, 10", "2025, 9"})
        void rejectsBonusOfAnotherPeriod(int year, int month) {
            givenOrder(orderInRepair());
            givenBonus(TestData.bonus(3, "TOYOTA", year, month, 5, 70_000));

            assertThatThrownBy(() -> repairOrderService.assignBonus(10L, 3L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("El bono corresponde a " + month + "/" + year + " y el ingreso es de 9/2026");
        }

        @Test
        void rejectsBonusWithoutQuota() {
            RepairOrder order = orderInRepair();
            Bonus bonus = TestData.bonus(3, "TOYOTA", 2026, 9, 5, 70_000);
            givenOrder(order);
            givenBonus(bonus);
            when(repairOrderRepository.countByBonus(bonus)).thenReturn(5L);

            assertThatThrownBy(() -> repairOrderService.assignBonus(10L, 3L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("No quedan bonos disponibles de TOYOTA para 9/2026");
            assertThat(order.getBonus()).isNull();
        }

        @Test
        void removesAssignedBonus() {
            RepairOrder order = orderReady();
            order.setBonus(TestData.bonus(3, "TOYOTA", 2026, 9, 5, 70_000));
            givenOrder(order);

            repairOrderService.removeBonus(10L);

            assertThat(order.getBonus()).isNull();
        }

        @Test
        void removeRejectsOrderWithoutBonus() {
            givenOrder(orderInRepair());

            assertThatThrownBy(() -> repairOrderService.removeBonus(10L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("El ingreso 10 no tiene un bono asignado");
        }

        @Test
        void removeRejectsDeliveredOrder() {
            RepairOrder order = orderDelivered();
            Bonus bonus = TestData.bonus(3, "TOYOTA", 2026, 9, 5, 70_000);
            order.setBonus(bonus);
            givenOrder(order);

            assertThatThrownBy(() -> repairOrderService.removeBonus(10L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("ya fue entregado");
            assertThat(order.getBonus()).isSameAs(bonus);
        }
    }

    @Nested
    class Queries {

        @Test
        void findByIdReturnsOrderWithItsStatus() {
            givenOrder(orderReady());

            RepairOrderResponse response = repairOrderService.findById(10L);

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.status()).isEqualTo(RepairOrderStatus.READY);
            assertThat(response.items()).extracting(RepairOrderResponse.Item::name).containsExactly("Frenos");
        }

        @Test
        void findByVehicleNormalizesPlateAndReturnsHistory() {
            Vehicle vehicle = TestData.toyotaSedan();
            RepairOrder older = TestData.order(1L, vehicle, ENTRY.minusMonths(2), BRAKES);
            older.setReadyDateTime(ENTRY.minusMonths(2).plusDays(1));
            older.setPickupDateTime(ENTRY.minusMonths(2).plusDays(2));
            RepairOrder current = TestData.order(2L, vehicle, ENTRY, ENGINE);
            when(vehicleRepository.findByLicensePlate("ABCD12")).thenReturn(Optional.of(vehicle));
            when(repairOrderRepository.findByVehicleOrderByEntryDateTimeDesc(vehicle)).thenReturn(List.of(current, older));

            List<RepairOrderResponse> history = repairOrderService.findByVehicle(" abcd12 ");

            assertThat(history).extracting(RepairOrderResponse::id).containsExactly(2L, 1L);
            assertThat(history).extracting(RepairOrderResponse::status)
                    .containsExactly(RepairOrderStatus.IN_REPAIR, RepairOrderStatus.DELIVERED);
        }

        @Test
        void findByVehicleFailsWhenVehicleDoesNotExist() {
            when(vehicleRepository.findByLicensePlate("ZZZZ99")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> repairOrderService.findByVehicle("zzzz99"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No existe un vehículo con la patente ZZZZ99");
        }
    }
}
