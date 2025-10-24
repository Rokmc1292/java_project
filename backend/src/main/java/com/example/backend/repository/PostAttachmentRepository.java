package com.example.backend.repository;

import com.example.backend.entity.PostAttachment;
import com.example.backend.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {
    List<PostAttachment> findByPostOrderByDisplayOrderAsc(Post post);
    void deleteByPost(Post post);
}
