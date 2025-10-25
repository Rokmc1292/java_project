package com.example.backend.repository;

import com.example.backend.entity.BoardCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardCategoryRepository extends JpaRepository<BoardCategory, Long> {
    Optional<BoardCategory> findByCategoryName(String categoryName);
    List<BoardCategory> findByIsActiveTrueOrderByDisplayOrder();
    List<BoardCategory> findAllByIsActiveTrueOrderByDisplayOrderAsc();
}
