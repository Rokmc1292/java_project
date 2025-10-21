package com.example.backend.repository;

import com.example.backend.entity.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SportRepository extends JpaRepository<Sport, Long> {
    Optional<Sport> findBySportName(String sportName);
    List<Sport> findByIsActiveTrueOrderByDisplayOrder();
}
