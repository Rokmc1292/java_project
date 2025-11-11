package com.example.backend.repository;

import com.example.backend.entity.Team;
import com.example.backend.entity.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByLeague(League league);
}
