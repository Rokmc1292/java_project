package com.example.backend.service;

import com.example.backend.entity.*;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserSettingsRepository userSettingsRepository;

    /**
     * 댓글 알림 생성
     */
    @Transactional
    public void createCommentNotification(Post post, Comment comment) {
        User postAuthor = post.getUser();
        User commentAuthor = comment.getUser();

        // 본인이 작성한 글에 본인이 댓글 단 경우 알림 X
        if (postAuthor.getUserId().equals(commentAuthor.getUserId())) {
            return;
        }

        // 사용자 알림 설정 확인
        UserSettings setting = userSettingsRepository.findByUser(postAuthor).orElse(null);
        if (setting == null || !setting.getCommentNotification()) {
            return;
        }

        String content = String.format("%s님이 회원님의 게시글에 댓글을 남겼습니다: \"%s\"",
                commentAuthor.getNickname(),
                comment.getContent().length() > 30 ? comment.getContent().substring(0, 30) + "..." : comment.getContent());

        Notification notification = Notification.builder()
                .user(postAuthor)
                .notificationType("COMMENT")
                .content(content)
                .relatedType("POST")
                .relatedId(post.getPostId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * 대댓글 알림 생성
     */
    @Transactional
    public void createReplyNotification(Comment parentComment, Comment reply) {
        User commentAuthor = parentComment.getUser();
        User replyAuthor = reply.getUser();

        // 본인이 작성한 댓글에 본인이 대댓글 단 경우 알림 X
        if (commentAuthor.getUserId().equals(replyAuthor.getUserId())) {
            return;
        }

        // 사용자 알림 설정 확인
        UserSettings setting = userSettingsRepository.findByUser(commentAuthor).orElse(null);
        if (setting == null || !setting.getReplyNotification()) {
            return;
        }

        String content = String.format("%s님이 회원님의 댓글에 답글을 남겼습니다: \"%s\"",
                replyAuthor.getNickname(),
                reply.getContent().length() > 30 ? reply.getContent().substring(0, 30) + "..." : reply.getContent());

        Notification notification = Notification.builder()
                .user(commentAuthor)
                .notificationType("REPLY")
                .content(content)
                .relatedType("COMMENT")
                .relatedId(parentComment.getCommentId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * 인기글 진입 알림 생성
     */
    @Transactional
    public void createPopularPostNotification(Post post) {
        User postAuthor = post.getUser();

        // 사용자 알림 설정 확인
        UserSettings setting = userSettingsRepository.findByUser(postAuthor).orElse(null);
        if (setting == null || !setting.getPopularPostNotification()) {
            return;
        }

        String content = String.format("회원님의 게시글 \"%s\"이(가) 인기글에 등록되었습니다!",
                post.getTitle().length() > 30 ? post.getTitle().substring(0, 30) + "..." : post.getTitle());

        Notification notification = Notification.builder()
                .user(postAuthor)
                .notificationType("POPULAR_POST")
                .content(content)
                .relatedType("POST")
                .relatedId(post.getPostId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * 예측 결과 알림 생성
     */
    @Transactional
    public void createPredictionResultNotification(Prediction prediction, boolean isCorrect) {
        User user = prediction.getUser();

        // 사용자 알림 설정 확인
        UserSettings setting = userSettingsRepository.findByUser(user).orElse(null);
        if (setting == null || !setting.getPredictionResultNotification()) {
            return;
        }

        Match match = prediction.getMatch();
        String result = isCorrect ? "적중" : "실패";
        String content = String.format("예측하신 경기 \"%s vs %s\"의 결과가 %s했습니다!",
                match.getHomeTeam().getTeamName(),
                match.getAwayTeam().getTeamName(),
                result);

        Notification notification = Notification.builder()
                .user(user)
                .notificationType("PREDICTION_RESULT")
                .content(content)
                .relatedType("PREDICTION")
                .relatedId(prediction.getPredictionId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }
}