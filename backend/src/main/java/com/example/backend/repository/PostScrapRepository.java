package com.example.backend.repository;

import com.example.backend.entity.PostScrap;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {
    Optional<PostScrap> findByPostAndUser(Post post, User user);
    boolean existsByPostAndUser(Post post, User user);
    Page<PostScrap> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    void deleteByPostAndUser(Post post, User user);
}
