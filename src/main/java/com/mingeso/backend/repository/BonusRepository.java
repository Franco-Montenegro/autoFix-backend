package com.mingeso.backend.repository;

import com.mingeso.backend.entity.Bonus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BonusRepository extends JpaRepository<Bonus, Long> {

    boolean existsByBrandAndPeriodYearAndPeriodMonth(String brand, Integer periodYear, Integer periodMonth);

    List<Bonus> findByPeriodYearAndPeriodMonthOrderByBrand(Integer periodYear, Integer periodMonth);

    /** Bloquea la fila del bono para revisar y consumir el cupo sin carreras. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bonus b where b.id = :id")
    Optional<Bonus> findByIdForUpdate(@Param("id") Long id);
}
