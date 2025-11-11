package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPageService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PredictionRepository predictionRepository;
    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public boolean isAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return Boolean.TRUE.equals(user.getIsAdmin());
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboardStats() {
        AdminDashboardDto dashboard = new AdminDashboardDto();

        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime weekAgo = today.minusDays(7);

        dashboard.setTotalUsers((int) userRepository.count());
        dashboard.setActiveUsers((int) userRepository.findAll().stream()
                .filter(User::getIsActive).count());
        dashboard.setTodayNewUsers((int) userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isAfter(today)).count());

        dashboard.setTotalPosts((int) postRepository.count());
        dashboard.setTodayNewPosts((int) postRepository.findAll().stream()
                .filter(p -> p.getCreatedAt().isAfter(today)).count());

        dashboard.setTotalPredictions((int) predictionRepository.count());
        dashboard.setTodayNewPredictions((int) predictionRepository.findAll().stream()
                .filter(p -> p.getCreatedAt().isAfter(today)).count());

        dashboard.setPendingReports((int) reportRepository.findByStatus("PENDING").size());

        List<AdminDashboardDto.DailyStats> weeklyStats = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = today.minusDays(i);
            LocalDateTime dayEnd = dayStart.plusDays(1);

            AdminDashboardDto.DailyStats stats = new AdminDashboardDto.DailyStats();
            stats.setDate(dayStart.format(DateTimeFormatter.ISO_LOCAL_DATE));
            stats.setNewUsers((int) userRepository.findAll().stream()
                    .filter(u -> u.getCreatedAt().isAfter(dayStart) && u.getCreatedAt().isBefore(dayEnd)).count());
            stats.setNewPosts((int) postRepository.findAll().stream()
                    .filter(p -> p.getCreatedAt().isAfter(dayStart) && p.getCreatedAt().isBefore(dayEnd)).count());
            stats.setNewPredictions((int) predictionRepository.findAll().stream()
                    .filter(p -> p.getCreatedAt().isAfter(dayStart) && p.getCreatedAt().isBefore(dayEnd)).count());
            weeklyStats.add(stats);
        }
        dashboard.setWeeklyStats(weeklyStats);

        List<Post> topPosts = postRepository.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsBlinded()))
                .sorted((p1, p2) -> Integer.compare(p2.getLikeCount(), p1.getLikeCount()))
                .limit(5).collect(Collectors.toList());

        List<AdminDashboardDto.PopularPost> popularPosts = topPosts.stream()
                .map(p -> {
                    AdminDashboardDto.PopularPost pp = new AdminDashboardDto.PopularPost();
                    pp.setPostId(p.getPostId());
                    pp.setTitle(p.getTitle());
                    pp.setCategoryName(p.getCategory().getCategoryName());
                    pp.setViewCount(p.getViewCount());
                    pp.setLikeCount(p.getLikeCount());
                    pp.setCreatedAt(p.getCreatedAt());
                    return pp;
                }).collect(Collectors.toList());
        dashboard.setPopularPosts(popularPosts);

        List<User> allUsers = userRepository.findAll();
        List<AdminDashboardDto.ActiveUser> activeUsers = allUsers.stream()
                .map(u -> {
                    AdminDashboardDto.ActiveUser au = new AdminDashboardDto.ActiveUser();
                    au.setUsername(u.getUsername());
                    au.setNickname(u.getNickname());
                    au.setTier(u.getTier());
                    au.setPostCount((int) postRepository.countByUserAndCreatedAtAfter(u, weekAgo));
                    au.setCommentCount((int) commentRepository.countByUserAndCreatedAtAfter(u, weekAgo));
                    return au;
                })
                .sorted((u1, u2) -> Integer.compare(
                        u2.getPostCount() + u2.getCommentCount(),
                        u1.getPostCount() + u1.getCommentCount()))
                .limit(5).collect(Collectors.toList());
        dashboard.setActiveUserList(activeUsers);

        return dashboard;
    }

    @Transactional(readOnly = true)
    public Page<AdminActivityLogDto> getRecentActivities(Pageable pageable) {
        List<AdminActivityLogDto> activities = new ArrayList<>();

        List<Post> recentPosts = postRepository.findAll().stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .limit(100).collect(Collectors.toList());

        for (Post post : recentPosts) {
            AdminActivityLogDto activity = new AdminActivityLogDto();
            activity.setActivityType("POST_CREATE");
            activity.setDescription(post.getUser().getNickname() + "님이 게시글을 작성했습니다: " + post.getTitle());
            activity.setUsername(post.getUser().getUsername());
            activity.setTargetType("POST");
            activity.setTargetId(post.getPostId());
            activity.setCreatedAt(post.getCreatedAt());
            activities.add(activity);
        }

        activities.sort((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), activities.size());

        return new PageImpl<>(activities.subList(start, end), pageable, activities.size());
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDto> getUsers(String search, String tier, String status, String sort, Pageable pageable) {
        List<User> users = userRepository.findAll();

        if (search != null && !search.isEmpty()) {
            users = users.stream()
                    .filter(u -> u.getUsername().contains(search) ||
                            u.getNickname().contains(search) ||
                            u.getEmail().contains(search))
                    .collect(Collectors.toList());
        }

        if (tier != null && !tier.isEmpty()) {
            users = users.stream().filter(u -> u.getTier().equals(tier)).collect(Collectors.toList());
        }

        if (status != null && !status.isEmpty()) {
            if ("active".equals(status)) {
                users = users.stream().filter(User::getIsActive).collect(Collectors.toList());
            } else if ("inactive".equals(status)) {
                users = users.stream().filter(u -> !u.getIsActive()).collect(Collectors.toList());
            }
        }

        if ("tierScore".equals(sort)) {
            users.sort((u1, u2) -> Integer.compare(u2.getTierScore(), u1.getTierScore()));
        } else {
            users.sort((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()));
        }

        List<AdminUserDto> userDtos = users.stream().map(this::convertToAdminUserDto).collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), userDtos.size());

        return new PageImpl<>(userDtos.subList(start, end), pageable, userDtos.size());
    }

    @Transactional(readOnly = true)
    public AdminUserDetailDto getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        AdminUserDetailDto dto = new AdminUserDetailDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setProfileImage(user.getProfileImage());
        dto.setTier(user.getTier());
        dto.setTierScore(user.getTierScore());
        dto.setIsActive(user.getIsActive());
        dto.setIsAdmin(user.getIsAdmin());
        dto.setCreatedAt(user.getCreatedAt());

        AdminUserDetailDto.UserActivityStats stats = new AdminUserDetailDto.UserActivityStats();
        stats.setTotalPosts((int) postRepository.countByUser(user));
        // FIX: commentRepository에서 전체 조회 후 필터링
        stats.setTotalComments((int) commentRepository.findAll().stream()
                .filter(c -> c.getUser().equals(user))
                .count());

        long totalPredictions = predictionRepository.countByUser(user);
        long correctPredictions = predictionRepository.countByUserAndIsCorrect(user, true);
        stats.setTotalPredictions((int) totalPredictions);
        stats.setCorrectPredictions((int) correctPredictions);

        if (totalPredictions > 0) {
            stats.setPredictionAccuracy((double) correctPredictions / totalPredictions * 100);
        } else {
            stats.setPredictionAccuracy(0.0);
        }

        // FIX: postRepository의 countByUser 사용 또는 findAll로 필터링
        int receivedLikes = postRepository.findAll().stream()
                .filter(p -> p.getUser().equals(user))
                .mapToInt(Post::getLikeCount)
                .sum();
        stats.setReceivedLikes(receivedLikes);
        stats.setReceivedReports(0);

        dto.setActivityStats(stats);
        dto.setRecentActivities(new ArrayList<>());
        dto.setReports(new ArrayList<>());

        return dto;
    }

    @Transactional
    public void updateUserStatus(Long userId, Boolean isActive, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.setIsActive(isActive);
        userRepository.save(user);
        log.info("관리자 {}가 사용자 {}의 계정을 {}했습니다.", adminUsername, user.getUsername(), isActive ? "활성화" : "비활성화");
    }

    @Transactional
    public void updateUserTierScore(Long userId, Integer tierScore, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        int oldScore = user.getTierScore();
        user.setTierScore(tierScore);
        updateUserTier(user);
        userRepository.save(user);
        log.info("관리자 {}가 사용자 {}의 티어 점수를 {} -> {}로 수정했습니다.", adminUsername, user.getUsername(), oldScore, tierScore);
    }

    @Transactional
    public void updateAdminStatus(Long userId, Boolean isAdmin, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.setIsAdmin(isAdmin);
        userRepository.save(user);
        log.info("관리자 {}가 사용자 {}에게 관리자 권한을 {}했습니다.", adminUsername, user.getUsername(), isAdmin ? "부여" : "회수");
    }

    @Transactional(readOnly = true)
    public Page<AdminReportDto> getReports(String status, String type, Pageable pageable) {
        List<Report> reports = reportRepository.findAll();

        if (status != null && !status.isEmpty()) {
            reports = reports.stream().filter(r -> r.getStatus().equals(status.toUpperCase())).collect(Collectors.toList());
        }

        if (type != null && !type.isEmpty()) {
            reports = reports.stream().filter(r -> r.getTargetType().equals(type.toUpperCase())).collect(Collectors.toList());
        }

        reports.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));

        List<AdminReportDto> reportDtos = reports.stream().map(this::convertToAdminReportDto).collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), reportDtos.size());

        return new PageImpl<>(reportDtos.subList(start, end), pageable, reportDtos.size());
    }

    @Transactional(readOnly = true)
    public AdminReportDetailDto getReportDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("신고를 찾을 수 없습니다."));

        AdminReportDetailDto dto = new AdminReportDetailDto();
        dto.setReportId(report.getReportId());
        dto.setReason(report.getReason());
        dto.setDescription(report.getDescription());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());

        AdminReportDetailDto.ReporterInfo reporterInfo = new AdminReportDetailDto.ReporterInfo();
        User reporter = report.getReporter();
        reporterInfo.setUserId(reporter.getUserId());
        reporterInfo.setUsername(reporter.getUsername());
        reporterInfo.setNickname(reporter.getNickname());
        reporterInfo.setTier(reporter.getTier());
        reporterInfo.setReportCount((int) reportRepository.findAll().stream()
                .filter(r -> r.getReporter().equals(reporter)).count());
        dto.setReporter(reporterInfo);

        AdminReportDetailDto.TargetInfo targetInfo = new AdminReportDetailDto.TargetInfo();
        targetInfo.setTargetType(report.getTargetType());
        targetInfo.setTargetId(report.getTargetId());

        if ("POST".equals(report.getTargetType())) {
            postRepository.findById(report.getTargetId()).ifPresent(post -> {
                targetInfo.setContent(post.getContent());
                targetInfo.setAuthorUsername(post.getUser().getUsername());
                targetInfo.setAuthorNickname(post.getUser().getNickname());
                targetInfo.setCreatedAt(post.getCreatedAt());
                targetInfo.setAuthorReportCount((int) reportRepository.findAll().stream()
                        .filter(r -> "POST".equals(r.getTargetType()))
                        .filter(r -> postRepository.findById(r.getTargetId())
                                .map(p -> p.getUser().equals(post.getUser())).orElse(false))
                        .count());
            });
        }

        dto.setTarget(targetInfo);
        return dto;
    }

    @Transactional
    public void processReport(Long reportId, String action, String adminNote, String adminUsername) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("신고를 찾을 수 없습니다."));

        if ("APPROVE".equals(action)) {
            report.setStatus("PROCESSED");
            if ("POST".equals(report.getTargetType())) {
                postRepository.findById(report.getTargetId()).ifPresent(post -> {
                    post.setIsBlinded(true);
                    postRepository.save(post);
                });
            }
        } else if ("REJECT".equals(action)) {
            report.setStatus("REJECTED");
        }

        reportRepository.save(report);
        log.info("관리자 {}가 신고 {}를 {}했습니다. 메모: {}", adminUsername, reportId, action, adminNote);
    }

    @Transactional(readOnly = true)
    public Page<AdminPostDto> getPosts(String search, String category, Boolean isBlinded, Pageable pageable) {
        List<Post> posts = postRepository.findAll();

        if (search != null && !search.isEmpty()) {
            posts = posts.stream().filter(p -> p.getTitle().contains(search) || p.getContent().contains(search)).collect(Collectors.toList());
        }

        if (category != null && !category.isEmpty()) {
            posts = posts.stream().filter(p -> p.getCategory().getCategoryName().equals(category)).collect(Collectors.toList());
        }

        if (isBlinded != null) {
            posts = posts.stream().filter(p -> p.getIsBlinded().equals(isBlinded)).collect(Collectors.toList());
        }

        posts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));

        List<AdminPostDto> postDtos = posts.stream().map(this::convertToAdminPostDto).collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), postDtos.size());

        return new PageImpl<>(postDtos.subList(start, end), pageable, postDtos.size());
    }

    @Transactional
    public void blindPost(Long postId, String reason, String adminUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        post.setIsBlinded(true);
        postRepository.save(post);
        log.info("관리자 {}가 게시글 {}를 블라인드 처리했습니다. 사유: {}", adminUsername, postId, reason);
    }

    @Transactional
    public void unblindPost(Long postId, String adminUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        post.setIsBlinded(false);
        postRepository.save(post);
        log.info("관리자 {}가 게시글 {}의 블라인드를 해제했습니다.", adminUsername, postId);
    }

    @Transactional
    public void deletePost(Long postId, String adminUsername) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        postRepository.delete(post);
        log.info("관리자 {}가 게시글 {}를 삭제했습니다.", adminUsername, postId);
    }

    @Transactional(readOnly = true)
    public AdminUserStatisticsDto getUserStatistics(String period) {
        AdminUserStatisticsDto dto = new AdminUserStatisticsDto();
        dto.setPeriod(period);
        List<AdminUserStatisticsDto.UserStatData> data = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int days = "daily".equals(period) ? 7 : ("weekly".equals(period) ? 30 : 90);

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            AdminUserStatisticsDto.UserStatData statData = new AdminUserStatisticsDto.UserStatData();
            statData.setDate(dayStart.format(DateTimeFormatter.ISO_LOCAL_DATE));
            statData.setNewUsers((int) userRepository.findAll().stream()
                    .filter(u -> u.getCreatedAt().isAfter(dayStart) && u.getCreatedAt().isBefore(dayEnd)).count());
            statData.setTotalUsers((int) userRepository.findAll().stream()
                    .filter(u -> u.getCreatedAt().isBefore(dayEnd)).count());
            data.add(statData);
        }
        dto.setData(data);
        return dto;
    }

    @Transactional(readOnly = true)
    public AdminContentStatisticsDto getContentStatistics(String period) {
        AdminContentStatisticsDto dto = new AdminContentStatisticsDto();
        dto.setPeriod(period);
        List<AdminContentStatisticsDto.ContentStatData> data = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int days = "daily".equals(period) ? 7 : ("weekly".equals(period) ? 30 : 90);

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            AdminContentStatisticsDto.ContentStatData statData = new AdminContentStatisticsDto.ContentStatData();
            statData.setDate(dayStart.format(DateTimeFormatter.ISO_LOCAL_DATE));
            statData.setNewPosts((int) postRepository.findAll().stream()
                    .filter(p -> p.getCreatedAt().isAfter(dayStart) && p.getCreatedAt().isBefore(dayEnd)).count());
            statData.setNewComments((int) commentRepository.findAll().stream()
                    .filter(c -> c.getCreatedAt().isAfter(dayStart) && c.getCreatedAt().isBefore(dayEnd)).count());
            data.add(statData);
        }
        dto.setData(data);
        return dto;
    }

    @Transactional(readOnly = true)
    public AdminPredictionStatisticsDto getPredictionStatistics(String period) {
        AdminPredictionStatisticsDto dto = new AdminPredictionStatisticsDto();
        dto.setPeriod(period);
        List<AdminPredictionStatisticsDto.PredictionStatData> data = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int days = "daily".equals(period) ? 7 : ("weekly".equals(period) ? 30 : 90);

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            AdminPredictionStatisticsDto.PredictionStatData statData = new AdminPredictionStatisticsDto.PredictionStatData();
            statData.setDate(dayStart.format(DateTimeFormatter.ISO_LOCAL_DATE));
            List<Prediction> dayPredictions = predictionRepository.findAll().stream()
                    .filter(p -> p.getCreatedAt().isAfter(dayStart) && p.getCreatedAt().isBefore(dayEnd)).collect(Collectors.toList());
            statData.setNewPredictions(dayPredictions.size());
            long correct = dayPredictions.stream().filter(p -> Boolean.TRUE.equals(p.getIsCorrect())).count();
            statData.setCorrectPredictions((int) correct);
            if (!dayPredictions.isEmpty()) {
                statData.setAccuracy((double) correct / dayPredictions.size() * 100);
            } else {
                statData.setAccuracy(0.0);
            }
            data.add(statData);
        }
        dto.setData(data);
        return dto;
    }

    private void updateUserTier(User user) {
        int score = user.getTierScore();
        if (score >= 1000) user.setTier("DIAMOND");
        else if (score >= 600) user.setTier("PLATINUM");
        else if (score >= 300) user.setTier("GOLD");
        else if (score >= 100) user.setTier("SILVER");
        else user.setTier("BRONZE");
    }

    private AdminUserDto convertToAdminUserDto(User user) {
        AdminUserDto dto = new AdminUserDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setTier(user.getTier());
        dto.setTierScore(user.getTierScore());
        dto.setIsActive(user.getIsActive());
        dto.setIsAdmin(user.getIsAdmin());
        dto.setPostCount((int) postRepository.countByUser(user));
        // FIX: commentRepository.countByUser() 사용
        // 수정 후
        dto.setCommentCount((int) commentRepository.findAll().stream()
                .filter(c -> c.getUser().equals(user))
                .count());
        dto.setPredictionCount((int) predictionRepository.countByUser(user));
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    private AdminReportDto convertToAdminReportDto(Report report) {
        AdminReportDto dto = new AdminReportDto();
        dto.setReportId(report.getReportId());
        dto.setReporterUsername(report.getReporter().getUsername());
        dto.setReporterNickname(report.getReporter().getNickname());
        dto.setTargetType(report.getTargetType());
        dto.setTargetId(report.getTargetId());
        dto.setReason(report.getReason());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());

        if ("POST".equals(report.getTargetType())) {
            postRepository.findById(report.getTargetId()).ifPresent(post -> {
                dto.setTargetTitle(post.getTitle());
                dto.setTargetAuthor(post.getUser().getNickname());
            });
        } else if ("COMMENT".equals(report.getTargetType())) {
            commentRepository.findById(report.getTargetId()).ifPresent(comment -> {
                String preview = comment.getContent();
                if (preview.length() > 50) preview = preview.substring(0, 50) + "...";
                dto.setTargetTitle(preview);
                dto.setTargetAuthor(comment.getUser().getNickname());
            });
        }
        return dto;
    }

    private AdminPostDto convertToAdminPostDto(Post post) {
        AdminPostDto dto = new AdminPostDto();
        dto.setPostId(post.getPostId());
        dto.setCategoryName(post.getCategory().getCategoryName());
        dto.setTitle(post.getTitle());
        dto.setAuthorUsername(post.getUser().getUsername());
        dto.setAuthorNickname(post.getUser().getNickname());
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setIsBlinded(post.getIsBlinded());
        dto.setIsNotice(post.getIsNotice());
        dto.setCreatedAt(post.getCreatedAt());
        int reportCount = (int) reportRepository.findByTargetTypeAndTargetId("POST", post.getPostId()).size();
        dto.setReportCount(reportCount);
        return dto;
    }
}