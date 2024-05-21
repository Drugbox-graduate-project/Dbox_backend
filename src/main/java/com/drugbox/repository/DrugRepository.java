package com.drugbox.repository;

import com.drugbox.domain.Drug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DrugRepository extends JpaRepository<Drug, Long> {
    List<Drug> findAllByDrugboxId(Long drugboxId);

    @Query(value="SELECT * FROM drug d WHERE d.date < :date", nativeQuery = true)
    List<Drug> findAllExpired(@Param("date") LocalDate date);

    @Query(value="SELECT * FROM drug d WHERE d.date = DATE_ADD(:date,INTERVAL 7 DAY)", nativeQuery = true)
    List<Drug> findAllNearExpired(@Param("date") LocalDate date);
}
