package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final PredictionRepository predictionRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserSettingsRepository userSettingsRepository;

    public UserProfileDto getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        UserProfileDto dto = new UserProfileDto();
        dto.setUserId(user.getUserId());
        dto.setNickname(user.getNickname());
        dto.setProfileImage(user.getProfileImage());
        dto.setTier(user.getTier());
        dto.setTierScore(user.getTierScore());
        dto.setNextTierScore(calculateNextTierScore(user.getTier()));
        dto.setCreatedAt(user.getCreatedAt());

        return dto;
    }

    private Integer calculateNextTierScore(String tier) {
        switch (tier) {
            case "BRONZE": return 100;
            case "SILVER": return 300;
            case "GOLD": return 600;
            case "PLATINUM": return 1000;
            case "DIAMOND": return null;
            default: return 100;
        }
    }

    public PredictionStatsDto getPredictionStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        List<Prediction> predictions = predictionRepository.findByUser(user, Pageable.unpaged()).getContent();

        int total = predictions.size();
        int correct = (int) predictions.stream()
                .filter(p -> p.getIsCorrect() != null && p.getIsCorrect())
                .count();
        int incorrect = (int) predictions.stream()
                .filter(p -> p.getIsCorrect() != null && !p.getIsCorrect())
                .count();

        double winRate = total > 0 ? (correct * 100.0 / total) : 0.0;

        PredictionStatsDto dto = new PredictionStatsDto();
        dto.setTotalPredictions(total);
        dto.setCorrectPredictions(correct);
        dto.setIncorrectPredictions(incorrect);
        dto.setWinRate(Math.round(winRate * 10) / 10.0);

        return dto;
    }

    public List<RecentPredictionResultDto> getRecentPredictionResults(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        List<Prediction> predictions = predictionRepository
                .findByUserAndIsCorrectNotNull(user, pageable)
                .getContent(); // Page에서 List로 변환

        return predictions.stream()
                .map(p -> {
                    RecentPredictionResultDto dto = new RecentPredictionResultDto();
                    dto.setPredictionId(p.getPredictionId());
                    dto.setMatchId(p.getMatch().getMatchId());
                    dto.setHomeTeam(p.getMatch().getHomeTeam().getTeamName());
                    dto.setAwayTeam(p.getMatch().getAwayTeam().getTeamName());
                    dto.setPredictedResult(p.getPredictedResult());
                    dto.setIsCorrect(p.getIsCorrect());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Page<UserPredictionHistoryDto> getUserPredictionHistory(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        Page<Prediction> predictions = predictionRepository.findByUser(user, pageable);

        return predictions.map(p -> {
            Match match = p.getMatch();
            UserPredictionHistoryDto dto = new UserPredictionHistoryDto();
            dto.setPredictionId(p.getPredictionId());
            dto.setMatchId(match.getMatchId());
            dto.setSportName(match.getLeague().getSport().getDisplayName());
            dto.setHomeTeam(match.getHomeTeam().getTeamName());
            dto.setAwayTeam(match.getAwayTeam().getTeamName());
            dto.setHomeTeamLogo(match.getHomeTeam().getTeamLogo());
            dto.setAwayTeamLogo(match.getAwayTeam().getTeamLogo());
            dto.setPredictedResult(p.getPredictedResult());
            dto.setComment(p.getComment());
            dto.setMatchDate(match.getMatchDate());
            dto.setMatchStatus(match.getStatus());
            dto.setIsCorrect(p.getIsCorrect());
            dto.setHomeScore(match.getHomeScore());
            dto.setAwayScore(match.getAwayScore());
            dto.setCreatedAt(p.getCreatedAt());
            return dto;
        });
    }

    public Page<UserPostDto> getUserPosts(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        Page<Post> posts = postRepository.findByUser(user, pageable);

        return posts.map(post -> {
            UserPostDto dto = new UserPostDto();
            dto.setPostId(post.getPostId());
            dto.setCategoryName(post.getCategory().getCategoryName());
            dto.setTitle(post.getTitle());
            dto.setViewCount(post.getViewCount());
            dto.setLikeCount(post.getLikeCount());
            dto.setCommentCount(post.getCommentCount());
            dto.setIsPopular(post.getIsPopular());
            dto.setIsBest(post.getIsBest());
            dto.setCreatedAt(post.getCreatedAt());
            return dto;
        });
    }

    public Page<UserCommentDto> getUserComments(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        Page<Comment> comments = commentRepository.findByUser(user, pageable);

        return comments.map(comment -> {
            UserCommentDto dto = new UserCommentDto();
            dto.setCommentId(comment.getCommentId());
            dto.setPostId(comment.getPost().getPostId());
            dto.setPostTitle(comment.getPost().getTitle());
            dto.setContent(comment.getContent());
            dto.setLikeCount(comment.getLikeCount());
            dto.setIsDeleted(comment.getIsDeleted());
            dto.setCreatedAt(comment.getCreatedAt());
            return dto;
        });
    }

    @Transactional
    public void updateNickname(String username, String newNickname) {
        if (userRepository.existsByNickname(newNickname)) {
            throw new RuntimeException("이미 사용중인 닉네임입니다");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        user.setNickname(newNickname);
        userRepository.save(user);
    }

    @Transactional
    public void updatePassword(String username, UpdatePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        // BCrypt를 사용한 비밀번호 검증
        if (!BCrypt.checkpw(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("새 비밀번호가 일치하지 않습니다");
        }

        // BCrypt를 사용한 비밀번호 암호화
        user.setPassword(BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt()));
        userRepository.save(user);
    }

    public UpdateSettingsRequest getUserSettings(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        UserSettings settings = userSettingsRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettings(user));

        UpdateSettingsRequest response = new UpdateSettingsRequest();
        response.setCommentNotification(settings.getCommentNotification());
        response.setReplyNotification(settings.getReplyNotification());
        response.setPopularPostNotification(settings.getPopularPostNotification());
        response.setPredictionResultNotification(settings.getPredictionResultNotification());

        return response;
    }

    @Transactional
    public UserSettings createDefaultSettings(User user) {
        UserSettings settings = new UserSettings();
        settings.setUser(user);
        settings.setCommentNotification(true);
        settings.setReplyNotification(true);
        settings.setPopularPostNotification(true);
        settings.setPredictionResultNotification(true);

        return userSettingsRepository.save(settings);
    }

    @Transactional
    public void updateUserSettings(String username, UpdateSettingsRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        UserSettings settings = userSettingsRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettings(user));

        settings.setCommentNotification(request.getCommentNotification());
        settings.setReplyNotification(request.getReplyNotification());
        settings.setPopularPostNotification(request.getPopularPostNotification());
        settings.setPredictionResultNotification(request.getPredictionResultNotification());

        userSettingsRepository.save(settings);
    }
}