package com.mingeso.backend.config;

import com.mingeso.backend.entity.RepairType;
import com.mingeso.backend.repository.RepairTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairTypeDataLoaderTest {

    @Mock
    private RepairTypeRepository repairTypeRepository;

    @InjectMocks
    private RepairTypeDataLoader loader;

    @Test
    @SuppressWarnings("unchecked")
    void loadsTheElevenRepairTypesWhenTableIsEmpty() {
        when(repairTypeRepository.count()).thenReturn(0L);

        loader.run(null);

        ArgumentCaptor<List<RepairType>> captor = ArgumentCaptor.forClass(List.class);
        verify(repairTypeRepository).saveAllAndFlush(captor.capture());
        assertThat(captor.getValue()).hasSize(11);
        assertThat(captor.getValue()).extracting(RepairType::getId)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
    }

    @Test
    void doesNothingWhenCatalogAlreadyExists() {
        when(repairTypeRepository.count()).thenReturn(11L);

        loader.run(null);

        verify(repairTypeRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void toleratesAnotherReplicaLoadingTheCatalogAtTheSameTime() {
        when(repairTypeRepository.count()).thenReturn(0L);
        when(repairTypeRepository.saveAllAndFlush(anyList()))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry '1' for key 'PRIMARY'"));

        assertThatCode(() -> loader.run(null)).doesNotThrowAnyException();
    }
}
