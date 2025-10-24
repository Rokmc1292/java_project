package com.example.backend.service;

import com.example.backend.dto.PostStatisticsDto;
import com.example.backend.dto.UserActivityDto;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.CommentRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    /**
     * 주간 베스트 게시글 (최근 7일)
     */
    @Transactional(readOnly = true)
    public List<PostStatisticsDto> getWeeklyBestPosts(int limit) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        List<Post> allPosts = postRepository.findAll();

        List<Post> weeklyPosts = allPosts.stream()
                .filter(post -> post.getCreatedAt().isAfter(weekAgo))
                .filter(post -> !post.getIsBlinded())
                .sorted((p1, p2) -> {
                    int score1 = calculatePostScore(p1);
                    int score2 = calculatePostScore(p2);
                    return Integer.compare(score2, score1);
                })
                .limit(limit)
                .collect(Collectors.toList());

        AtomicInteger rank = new AtomicInteger(1);
        return weeklyPosts.stream()
                .map(post -> convertToStatisticsDto(post, "WEEKLY", rank.getAndIncrement()))
                .collect(Collectors.toList());
    }

    /**
     * 월간 베스트 게시글 (최근 30일)
     */
    @Transactional(readOnly = true)
    public List<PostStatisticsDto> getMonthlyBestPosts(int limit) {
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);

        List<Post> allPosts = postRepository.findAll();

        List<Post> monthlyPosts = allPosts.stream()
                .filter(post -> post.getCreatedAt().isAfter(monthAgo))
                .filter(post -> !post.getIsBlinded())
                .sorted((p1, p2) -> {
                    int score1 = calculatePostScore(p1);
                    int score2 = calculatePostScore(p2);
                    return Integer.compare(score2, score1);
                })
                .limit(limit)
                .collect(Collectors.toList());

        AtomicInteger rank = new AtomicInteger(1);
        return monthlyPosts.stream()
                .map(post -> convertToStatisticsDto(post, "MONTHLY", rank.getAndIncrement()))
                .collect(Collectors.toList());
    }

    /**
     * 활동 왕성 유저 랭킹 (최근 30일 기준)
     */
    @Transactional(readOnly = true)
    public List<UserActivityDto> getActiveUsersRanking(int limit) {
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);

        List<User> allUsers = userRepository.findAll();
        List<Post> allPosts = postRepository.findAll();

        List<UserActivityDto> userActivities = allUsers.stream()
                .filter(User::getIsActive)
                .map(user -> {
                    // 최근 30일 게시글 수
                    long postCount = allPosts.stream()
                            .filter(post -> post.getUser().getUserId().equals(user.getUserId()))
                            .filter(post -> post.getCreatedAt().isAfter(monthAgo))
                            .count();

                    // 최근 30일 댓글 수
                    long commentCount = commentRepository.findAll().stream()
                            .filter(comment -> comment.getUser().getUserId().equals(user.getUserId()))
                            .filter(comment -> comment.getCreatedAt().isAfter(monthAgo))
                            .count();

                    // 받은 총 추천수
                    int totalLikes = allPosts.stream()
                            .filter(post -> post.getUser().getUserId().equals(user.getUserId()))
                            .mapToInt(Post::getLikeCount)
                            .sum();

                    UserActivityDto dto = new UserActivityDto();
                    dto.setUserId(user.getUserId());
                    dto.setUsername(user.getUsername());
                    dto.setNickname(user.getNickname());
                    dto.setTier(user.getTier());
                    dto.setProfileImage(user.getProfileImage());
                    dto.setPostCount((int) postCount);
                    dto.setCommentCount((int) commentCount);
                    dto.setTotalLikes(totalLikes);

                    // 활동 점수 계산: 게시글 5점 + 댓글 2점 + 추천수 1점
                    int activityScore = (int) (postCount * 5 + commentCount * 2 + totalLikes);
                    dto.setActivityScore(activityScore);

                    return dto;
                })
                .filter(dto -> dto.getActivityScore() > 0) // 활동이 있는 유저만
                .sorted((u1, u2) -> Integer.compare(u2.getActivityScore(), u1.getActivityScore()))
                .limit(limit)
                .collect(Collectors.toList());

        // 랭킹 부여
        AtomicInteger rank = new AtomicInteger(1);
        userActivities.forEach(dto -> dto.setRank(rank.getAndIncrement()));

        return userActivities;
    }

    /**
     * 카테고리별 주간 베스트 게시글
     */
    @Transactional(readOnly = true)
    public List<PostStatisticsDto> getWeeklyBestPostsByCategory(String categoryName, int limit) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        List<Post> allPosts = postRepository.findAll();

        List<Post> categoryPosts = allPosts.stream()
                .filter(post -> post.getCategory().getCategoryName().equals(categoryName))
                .filter(post -> post.getCreatedAt().isAfter(weekAgo))
                .filter(post -> !post.getIsBlinded())
                .sorted((p1, p2) -> {
                    int score1 = calculatePostScore(p1);
                    int score2 = calculatePostScore(p2);
                    return Integer.compare(score2, score1);
                })
                .limit(limit)
                .collect(Collectors.toList());

        AtomicInteger rank = new AtomicInteger(1);
        return categoryPosts.stream()
                .map(post -> convertToStatisticsDto(post, "WEEKLY", rank.getAndIncrement()))
                .collect(Collectors.toList());
    }

    /**
     * 게시글 점수 계산 (추천수 3점 + 댓글수 2점 + 조회수 0.1점)
     */
    private int calculatePostScore(Post post) {
        return post.getLikeCount() * 3 +
               post.getCommentCount() * 2 +
               (int)(post.getViewCount() * 0.1);
    }

    /**
     * Entity를 StatisticsDto로 변환
     */
    private PostStatisticsDto convertToStatisticsDto(Post post, String period, int rank) {
        PostStatisticsDto dto = new PostStatisticsDto();
        dto.setPostId(post.getPostId());
        dto.setTitle(post.getTitle());
        dto.setCategoryName(post.getCategory().getCategoryName());
        dto.setAuthorNickname(post.getUser().getNickname());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setViewCount(post.getViewCount());
        dto.setPeriod(period);
        dto.setRank(rank);
        return dto;
    }
}
