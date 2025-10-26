package com.example.backend.repository;

import com.example.backend.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 신고 Repository
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    // 처리 대기중인 신고 조회
    List<Report> findByStatus(String status);

    // 특정 대상에 대한 신고 조회
    List<Report> findByTargetTypeAndTargetId(String targetType, Long targetId);

    // 전체 신고 목록 조회 (페이징)
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);
}