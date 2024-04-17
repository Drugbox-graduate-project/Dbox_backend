package com.drugbox.repository;

import com.drugbox.domain.Drug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DrugRepository extends JpaRepository<Drug, Long> {
    List<Drug> findAllByDrugboxId(Long drugboxId);
}
