package com.example.backend.repository;

import com.example.backend.entity.PostReport;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    Optional<PostReport> findByPostAndReporter(Post post, User reporter);
    boolean existsByPostAndReporter(Post post, User reporter);
    Page<PostReport> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    long countByPostAndStatus(Post post, String status);
}
