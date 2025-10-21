package com.example.backend.repository;

import com.example.backend.entity.News;
import com.example.backend.entity.Sport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    Page<News> findAllByOrderByPublishedAtDesc(Pageable pageable);
    Page<News> findBySportOrderByPublishedAtDesc(Sport sport, Pageable pageable);
    List<News> findTop10ByOrderByViewCountDescPublishedAtDesc();
}
