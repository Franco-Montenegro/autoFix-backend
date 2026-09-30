package com.mingeso.backend.service;

import com.mingeso.backend.dto.RepairOrderCreateRequest;
import com.mingeso.backend.dto.RepairOrderResponse;
import com.mingeso.backend.dto.RepairOrderStatus;
import com.mingeso.backend.dto.VehicleRequest;
import com.mingeso.backend.entity.Bonus;
import com.mingeso.backend.entity.EngineType;
import com.mingeso.backend.entity.RepairOrder;
import com.mingeso.backend.entity.RepairOrderItem;
import com.mingeso.backend.entity.RepairType;
import com.mingeso.backend.entity.Vehicle;
import com.mingeso.backend.exception.BusinessRuleException;
import com.mingeso.backend.exception.ConflictException;
import com.mingeso.backend.exception.ResourceNotFoundException;
import com.mingeso.backend.repository.BonusRepository;
import com.mingeso.backend.repository.RepairOrderRepository;
import com.mingeso.backend.repository.RepairTypeRepository;
import com.mingeso.backend.repository.VehicleRepository;
import com.mingeso.backend.service.cost.CostBreakdown;
import com.mingeso.backend.service.cost.RepairCostCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepairOrderService {

    /** Un modelo del año siguiente puede ingresar como maximo un año antes de su año de fabricacion. */
    private static final int MAX_YEARS_BEFORE_MANUFACTURE = 1;

    private final RepairOrderRepository repairOrderRepository;
    private final VehicleRepository vehicleRepository;
    private final RepairTypeRepository repairTypeRepository;
    private final BonusRepository bonusRepository;
    private final RepairCostCalculator repairCostCalculator;

    @Transactional
    public RepairOrderResponse create(RepairOrderCreateRequest request) {
        Vehicle vehicle = findVehicle(request.licensePlate());

        if (repairOrderRepository.existsByVehicleAndPickupDateTimeIsNull(vehicle)) {
            throw new ConflictException("El vehículo " + vehicle.getLicensePlate()
                    + " tiene un ingreso abierto; debe ser retirado antes de registrar uno nuevo");
        }

        int entryYear = request.entryDateTime().getYear();
        if (entryYear < vehicle.getManufactureYear() - MAX_YEARS_BEFORE_MANUFACTURE) {
            throw new BusinessRuleException("El año de ingreso (" + entryYear
                    + ") no puede ser más de un año anterior al año de fabricación del vehículo ("
                    + vehicle.getManufactureYear() + ")");
        }

        List<RepairType> repairTypes = findRepairTypes(request.repairTypeIds());

        List<String> notApplicable = repairTypes.stream()
                .filter(rt -> priceFor(rt, vehicle.getEngineType()).signum() == 0)
                .map(RepairType::getName)
                .toList();
        if (!notApplicable.isEmpty()) {
            throw new BusinessRuleException("Las siguientes reparaciones no aplican para motor "
                    + vehicle.getEngineType() + ": " + String.join(", ", notApplicable));
        }

        RepairOrder order = RepairOrder.builder()
                .vehicle(vehicle)
                .entryDateTime(request.entryDateTime())
                .mileage(request.mileage())
                .build();
        repairTypes.forEach(rt -> order.addItem(RepairOrderItem.builder()
                .repairType(rt)
                .price(priceFor(rt, vehicle.getEngineType()))
                .build()));

        return toResponse(repairOrderRepository.save(order));
    }

    @Transactional
    public RepairOrderResponse registerReady(Long id, LocalDateTime readyDateTime) {
        RepairOrder order = findOrder(id);
        if (order.getReadyDateTime() != null) {
            throw new ConflictException("El ingreso " + id + " ya tiene registrada la fecha de salida");
        }
        if (readyDateTime.isBefore(order.getEntryDateTime())) {
            throw new BusinessRuleException("La fecha de salida no puede ser anterior a la fecha de ingreso ("
                    + order.getEntryDateTime() + ")");
        }
        order.setReadyDateTime(readyDateTime);
        return toResponse(order);
    }

    /** Registra el retiro y calcula el costo total del ingreso. */
    @Transactional
    public RepairOrderResponse registerPickup(Long id, LocalDateTime pickupDateTime) {
        RepairOrder order = findOrder(id);
        if (order.getReadyDateTime() == null) {
            throw new ConflictException("No se puede registrar el retiro del ingreso " + id
                    + " antes de registrar su salida");
        }
        if (order.getPickupDateTime() != null) {
            throw new ConflictException("El ingreso " + id + " ya tiene registrada la fecha de retiro");
        }
        if (pickupDateTime.isBefore(order.getReadyDateTime())) {
            throw new BusinessRuleException("La fecha de retiro no puede ser anterior a la fecha de salida ("
                    + order.getReadyDateTime() + ")");
        }
        order.setPickupDateTime(pickupDateTime);
        applyCost(order);
        return toResponse(order);
    }

    /** Vuelve a calcular el costo de un ingreso entregado (ej. tras corregir datos). */
    @Transactional
    public RepairOrderResponse recalculateCost(Long id) {
        RepairOrder order = findOrder(id);
        if (order.getPickupDateTime() == null) {
            throw new ConflictException("Solo se puede calcular el costo de un ingreso entregado; el ingreso "
                    + id + " aún no registra su retiro");
        }
        applyCost(order);
        return toResponse(order);
    }

    @Transactional
    public RepairOrderResponse assignBonus(Long id, Long bonusId) {
        RepairOrder order = findOrder(id);
        requireNotDelivered(order);
        if (order.getBonus() != null) {
            throw new ConflictException("El ingreso " + id + " ya tiene un bono asignado");
        }

        Bonus bonus = bonusRepository.findByIdForUpdate(bonusId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un bono con id " + bonusId));

        String vehicleBrand = order.getVehicle().getBrand();
        if (!bonus.getBrand().equals(vehicleBrand)) {
            throw new BusinessRuleException("El bono es de la marca " + bonus.getBrand()
                    + " y el vehículo es " + vehicleBrand);
        }
        LocalDateTime entry = order.getEntryDateTime();
        if (bonus.getPeriodYear() != entry.getYear() || bonus.getPeriodMonth() != entry.getMonthValue()) {
            throw new BusinessRuleException("El bono corresponde a " + bonus.getPeriodMonth() + "/"
                    + bonus.getPeriodYear() + " y el ingreso es de " + entry.getMonthValue() + "/" + entry.getYear());
        }
        if (repairOrderRepository.countByBonus(bonus) >= bonus.getQuantity()) {
            throw new ConflictException("No quedan bonos disponibles de " + bonus.getBrand()
                    + " para " + bonus.getPeriodMonth() + "/" + bonus.getPeriodYear());
        }

        order.setBonus(bonus);
        return toResponse(order);
    }

    @Transactional
    public void removeBonus(Long id) {
        RepairOrder order = findOrder(id);
        requireNotDelivered(order);
        if (order.getBonus() == null) {
            throw new ConflictException("El ingreso " + id + " no tiene un bono asignado");
        }
        order.setBonus(null);
    }

    @Transactional(readOnly = true)
    public RepairOrderResponse findById(Long id) {
        return toResponse(findOrder(id));
    }

    @Transactional(readOnly = true)
    public List<RepairOrderResponse> findByVehicle(String licensePlate) {
        Vehicle vehicle = findVehicle(VehicleRequest.normalizeUpper(licensePlate));
        return repairOrderRepository.findByVehicleOrderByEntryDateTimeDesc(vehicle).stream()
                .map(this::toResponse)
                .toList();
    }

    private void applyCost(RepairOrder order) {
        LocalDateTime entry = order.getEntryDateTime();
        long previousRepairs = repairOrderRepository
                .countByVehicleAndEntryDateTimeGreaterThanEqualAndEntryDateTimeLessThan(
                        order.getVehicle(), entry.minusMonths(12), entry);

        CostBreakdown cost = repairCostCalculator.calculate(order, previousRepairs);
        order.setRepairsSubtotal(cost.repairsSubtotal());
        order.setMileageSurcharge(cost.mileageSurcharge());
        order.setAgeSurcharge(cost.ageSurcharge());
        order.setDelaySurcharge(cost.delaySurcharge());
        order.setRepairCountDiscount(cost.repairCountDiscount());
        order.setDayDiscount(cost.dayDiscount());
        order.setBonusDiscount(cost.bonusDiscount());
        order.setTaxAmount(cost.taxAmount());
        order.setTotalAmount(cost.totalAmount());
    }

    /** El bono solo se cambia antes del retiro: despues el costo ya esta calculado. */
    private void requireNotDelivered(RepairOrder order) {
        if (order.getPickupDateTime() != null) {
            throw new ConflictException("No se puede modificar el bono del ingreso " + order.getId()
                    + " porque ya fue entregado y su costo está calculado");
        }
    }

    private Vehicle findVehicle(String licensePlate) {
        return vehicleRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un vehículo con la patente " + licensePlate));
    }

    private RepairOrder findOrder(Long id) {
        return repairOrderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un ingreso con id " + id));
    }

    /** Devuelve los tipos en el orden pedido; 404 si alguno no existe en el catalogo. */
    private List<RepairType> findRepairTypes(List<Integer> ids) {
        Map<Integer, RepairType> found = repairTypeRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(RepairType::getId, Function.identity()));
        List<Integer> missing = ids.stream().filter(id -> !found.containsKey(id)).toList();
        if (!missing.isEmpty()) {
            throw new ResourceNotFoundException("No existen reparaciones con los ids: " + missing);
        }
        return ids.stream().map(found::get).toList();
    }

    private BigDecimal priceFor(RepairType repairType, EngineType engineType) {
        return switch (engineType) {
            case GASOLINE -> repairType.getPriceGasoline();
            case DIESEL -> repairType.getPriceDiesel();
            case HYBRID -> repairType.getPriceHybrid();
            case ELECTRIC -> repairType.getPriceElectric();
        };
    }

    private RepairOrderStatus statusOf(RepairOrder order) {
        if (order.getPickupDateTime() != null) {
            return RepairOrderStatus.DELIVERED;
        }
        if (order.getReadyDateTime() != null) {
            return RepairOrderStatus.READY;
        }
        return RepairOrderStatus.IN_REPAIR;
    }

    private RepairOrderResponse toResponse(RepairOrder order) {
        return RepairOrderResponse.from(order, statusOf(order));
    }
}
