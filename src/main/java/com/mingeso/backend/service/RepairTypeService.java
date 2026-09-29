package com.mingeso.backend.service;

import com.mingeso.backend.dto.RepairTypeResponse;
import com.mingeso.backend.repository.RepairTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairTypeService {

    private final RepairTypeRepository repairTypeRepository;

    @Transactional(readOnly = true)
    public List<RepairTypeResponse> findAll() {
        return repairTypeRepository.findAll(Sort.by("id")).stream()
                .map(RepairTypeResponse::from)
                .toList();
    }
}
