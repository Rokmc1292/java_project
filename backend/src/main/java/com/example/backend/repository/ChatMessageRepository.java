package com.example.backend.repository;

import com.example.backend.entity.ChatMessage;
import com.example.backend.entity.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatroomOrderByCreatedAtDesc(Chatroom chatroom);
    List<ChatMessage> findByChatroomOrderByCreatedAtAsc(Chatroom chatroom);
    List<ChatMessage> findByChatroomAndCreatedAtAfterOrderByCreatedAtAsc(Chatroom chatroom, LocalDateTime after);
}
