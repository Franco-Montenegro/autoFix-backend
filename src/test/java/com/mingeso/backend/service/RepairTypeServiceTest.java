package com.mingeso.backend.service;

import com.mingeso.backend.dto.RepairTypeResponse;
import com.mingeso.backend.repository.RepairTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairTypeServiceTest {

    @Mock
    private RepairTypeRepository repairTypeRepository;

    @InjectMocks
    private RepairTypeService repairTypeService;

    @Test
    void findAllReturnsCatalogSortedByIdWithTheFourPrices() {
        when(repairTypeRepository.findAll(Sort.by("id"))).thenReturn(List.of(
                TestData.repairType(1, "Reparaciones del Sistema de Frenos", 120_000, 120_000, 180_000, 220_000),
                TestData.repairType(5, "Reparación del Sistema de Escape", 120_000, 140_000, 180_000, 0)));

        List<RepairTypeResponse> responses = repairTypeService.findAll();

        assertThat(responses).extracting(RepairTypeResponse::id).containsExactly(1, 5);
        RepairTypeResponse exhaust = responses.get(1);
        assertThat(exhaust.priceGasoline()).isEqualByComparingTo("120000");
        assertThat(exhaust.priceDiesel()).isEqualByComparingTo("140000");
        assertThat(exhaust.priceHybrid()).isEqualByComparingTo("180000");
        assertThat(exhaust.priceElectric()).isEqualByComparingTo("0");
    }
}
