package com.example.backend.repository;

import com.example.backend.entity.League;
import com.example.backend.entity.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {
    List<League> findBySportAndIsActiveTrueOrderByDisplayOrder(Sport sport);
}
