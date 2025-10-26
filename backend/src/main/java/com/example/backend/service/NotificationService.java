package com.example.backend.service;

import com.example.backend.entity.*;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 서비스
 * - 댓글 알림
 * - 대댓글 알림
 * - 인기글 진입 알림
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserSettingRepository userSettingRepository;

    /**
     * 댓글 알림 전송 (게시글 작성자에게)
     */
    @Transactional
    public void sendCommentNotification(Post post, User commenter) {
        User postAuthor = post.getUser();

        // 본인이 작성한 글에 본인이 댓글 단 경우 알림 안 보냄
        if (postAuthor.getUserId().equals(commenter.getUserId())) {
            return;
        }

        // 사용자 설정 확인
        UserSetting setting = userSettingRepository.findByUser(postAuthor).orElse(null);
        if (setting != null && !setting.getCommentNotification()) {
            return; // 댓글 알림 비활성화 상태
        }

        // 알림 생성
        Notification notification = new Notification();
        notification.setUser(postAuthor);
        notification.setNotificationType("COMMENT");
        notification.setContent(commenter.getNickname() + "님이 회원님의 게시글에 댓글을 남겼습니다: " + post.getTitle());
        notification.setRelatedType("POST");
        notification.setRelatedId(post.getPostId());

        notificationRepository.save(notification);
    }

    /**
     * 대댓글 알림 전송 (댓글 작성자에게)
     */
    @Transactional
    public void sendReplyNotification(Comment parentComment, User replier) {
        User commentAuthor = parentComment.getUser();

        // 본인이 작성한 댓글에 본인이 대댓글 단 경우 알림 안 보냄
        if (commentAuthor.getUserId().equals(replier.getUserId())) {
            return;
        }

        // 사용자 설정 확인
        UserSetting setting = userSettingRepository.findByUser(commentAuthor).orElse(null);
        if (setting != null && !setting.getReplyNotification()) {
            return; // 대댓글 알림 비활성화 상태
        }

        // 알림 생성
        Notification notification = new Notification();
        notification.setUser(commentAuthor);
        notification.setNotificationType("REPLY");
        notification.setContent(replier.getNickname() + "님이 회원님의 댓글에 답글을 남겼습니다.");
        notification.setRelatedType("COMMENT");
        notification.setRelatedId(parentComment.getCommentId());

        notificationRepository.save(notification);
    }

    /**
     * 인기글 진입 알림 전송 (게시글 작성자에게)
     */
    @Transactional
    public void sendPopularPostNotification(Post post) {
        User postAuthor = post.getUser();

        // 사용자 설정 확인
        UserSetting setting = userSettingRepository.findByUser(postAuthor).orElse(null);
        if (setting != null && !setting.getPopularPostNotification()) {
            return; // 인기글 알림 비활성화 상태
        }

        // 알림 생성
        Notification notification = new Notification();
        notification.setUser(postAuthor);
        notification.setNotificationType("POPULAR_POST");
        notification.setContent("축하합니다! 회원님의 게시글이 인기글이 되었습니다: " + post.getTitle());
        notification.setRelatedType("POST");
        notification.setRelatedId(post.getPostId());

        notificationRepository.save(notification);
    }

    /**
     * 예측 결과 알림 전송 (예측 참여자에게)
     */
    @Transactional
    public void sendPredictionResultNotification(User user, boolean isCorrect, String matchInfo) {
        // 사용자 설정 확인
        UserSetting setting = userSettingRepository.findByUser(user).orElse(null);
        if (setting != null && !setting.getPredictionResultNotification()) {
            return; // 예측 결과 알림 비활성화 상태
        }

        // 알림 생성
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setNotificationType("PREDICTION_RESULT");

        if (isCorrect) {
            notification.setContent("예측 성공! " + matchInfo + " 경기 예측이 적중했습니다. +10점");
        } else {
            notification.setContent("예측 실패. " + matchInfo + " 경기 예측이 빗나갔습니다. -10점");
        }

        notificationRepository.save(notification);
    }
}