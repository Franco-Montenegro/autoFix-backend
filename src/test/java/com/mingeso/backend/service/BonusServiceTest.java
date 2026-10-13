package com.mingeso.backend.service;

import com.mingeso.backend.dto.BonusRequest;
import com.mingeso.backend.dto.BonusResponse;
import com.mingeso.backend.entity.Bonus;
import com.mingeso.backend.exception.ConflictException;
import com.mingeso.backend.repository.BonusRepository;
import com.mingeso.backend.repository.RepairOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BonusServiceTest {

    @Mock
    private BonusRepository bonusRepository;

    @Mock
    private RepairOrderRepository repairOrderRepository;

    @InjectMocks
    private BonusService bonusService;

    @Test
    void createSavesMonthlyQuotaWithNothingUsed() {
        BonusRequest request = new BonusRequest(" toyota ", 2026, 9, 5, new BigDecimal("70000"));
        when(bonusRepository.existsByBrandAndPeriodYearAndPeriodMonth("TOYOTA", 2026, 9)).thenReturn(false);
        when(bonusRepository.save(any(Bonus.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BonusResponse response = bonusService.create(request);

        ArgumentCaptor<Bonus> captor = ArgumentCaptor.forClass(Bonus.class);
        verify(bonusRepository).save(captor.capture());
        Bonus saved = captor.getValue();
        assertThat(saved.getBrand()).isEqualTo("TOYOTA");
        assertThat(saved.getPeriodYear()).isEqualTo(2026);
        assertThat(saved.getPeriodMonth()).isEqualTo(9);
        assertThat(saved.getQuantity()).isEqualTo(5);
        assertThat(saved.getAmount()).isEqualByComparingTo("70000");

        assertThat(response.used()).isZero();
        assertThat(response.available()).isEqualTo(5);
    }

    @Test
    void createRejectsSecondQuotaForSameBrandAndMonth() {
        BonusRequest request = new BonusRequest("FORD", 2026, 9, 2, new BigDecimal("50000"));
        when(bonusRepository.existsByBrandAndPeriodYearAndPeriodMonth("FORD", 2026, 9)).thenReturn(true);

        assertThatThrownBy(() -> bonusService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Ya existen bonos registrados para FORD en 9/2026");
        verify(bonusRepository, never()).save(any());
    }

    @Test
    void findByPeriodComputesUsedAndAvailable() {
        Bonus ford = TestData.bonus(1, "FORD", 2026, 9, 2, 50_000);
        Bonus toyota = TestData.bonus(2, "TOYOTA", 2026, 9, 5, 70_000);
        when(bonusRepository.findByPeriodYearAndPeriodMonthOrderByBrand(2026, 9)).thenReturn(List.of(ford, toyota));
        when(repairOrderRepository.countByBonus(ford)).thenReturn(2L);
        when(repairOrderRepository.countByBonus(toyota)).thenReturn(1L);

        List<BonusResponse> responses = bonusService.findByPeriod(2026, 9);

        assertThat(responses).extracting(BonusResponse::brand).containsExactly("FORD", "TOYOTA");
        assertThat(responses).extracting(BonusResponse::used).containsExactly(2L, 1L);
        assertThat(responses).extracting(BonusResponse::available).containsExactly(0L, 4L);
    }

    @Test
    void findByPeriodReturnsEmptyListWhenNoQuotaWasLoaded() {
        when(bonusRepository.findByPeriodYearAndPeriodMonthOrderByBrand(2026, 10)).thenReturn(List.of());

        assertThat(bonusService.findByPeriod(2026, 10)).isEmpty();
    }
}
