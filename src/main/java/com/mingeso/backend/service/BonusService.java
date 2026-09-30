package com.mingeso.backend.service;

import com.mingeso.backend.dto.BonusRequest;
import com.mingeso.backend.dto.BonusResponse;
import com.mingeso.backend.entity.Bonus;
import com.mingeso.backend.exception.ConflictException;
import com.mingeso.backend.repository.BonusRepository;
import com.mingeso.backend.repository.RepairOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BonusService {

    private final BonusRepository bonusRepository;
    private final RepairOrderRepository repairOrderRepository;

    @Transactional
    public BonusResponse create(BonusRequest request) {
        if (bonusRepository.existsByBrandAndPeriodYearAndPeriodMonth(request.brand(), request.year(), request.month())) {
            throw new ConflictException("Ya existen bonos registrados para " + request.brand()
                    + " en " + request.month() + "/" + request.year());
        }
        Bonus bonus = Bonus.builder()
                .brand(request.brand())
                .periodYear(request.year())
                .periodMonth(request.month())
                .quantity(request.quantity())
                .amount(request.amount())
                .build();
        return BonusResponse.from(bonusRepository.save(bonus), 0);
    }

    @Transactional(readOnly = true)
    public List<BonusResponse> findByPeriod(int year, int month) {
        return bonusRepository.findByPeriodYearAndPeriodMonthOrderByBrand(year, month).stream()
                .map(bonus -> BonusResponse.from(bonus, repairOrderRepository.countByBonus(bonus)))
                .toList();
    }
}
