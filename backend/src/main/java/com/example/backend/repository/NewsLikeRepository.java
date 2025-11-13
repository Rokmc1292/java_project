package com.example.backend.repository;

import com.example.backend.entity.News;
import com.example.backend.entity.NewsLike;
import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsLikeRepository extends JpaRepository<NewsLike, Long> {

    // 사용자가 특정 뉴스에 좋아요를 눌렀는지 확인
    boolean existsByNewsAndUser(News news, User user);

    // 특정 뉴스와 사용자의 좋아요 찾기
    Optional<NewsLike> findByNewsAndUser(News news, User user);

    // 사용자가 좋아요한 뉴스 목록
    Page<NewsLike> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 뉴스의 좋아요 개수
    long countByNews(News news);
}
