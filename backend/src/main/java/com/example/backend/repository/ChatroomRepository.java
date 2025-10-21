package com.example.backend.repository;

import com.example.backend.entity.Chatroom;
import com.example.backend.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
    Optional<Chatroom> findByMatch(Match match);
    List<Chatroom> findByIsActiveTrueOrderByViewerCountDesc();
}
