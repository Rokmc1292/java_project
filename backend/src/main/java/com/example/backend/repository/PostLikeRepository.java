package com.example.backend.repository;

import com.example.backend.entity.PostLike;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(Post post, User user);
    boolean existsByPostAndUser(Post post, User user);
    long countByPostAndLikeType(Post post, String likeType);
    void deleteByPostAndUser(Post post, User user);
}
