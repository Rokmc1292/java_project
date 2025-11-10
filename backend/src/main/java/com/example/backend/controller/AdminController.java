package com.example.backend.controller;

import com.example.backend.dto.admin.*;
import com.example.backend.scheduler.EplScheduleCrawler;
import com.example.backend.scheduler.NbaScheduleCrawler;
import com.example.backend.repository.LeagueRepository;
import com.example.backend.repository.TeamRepository;
import com.example.backend.service.AdminService;
import com.example.backend.entity.League;
import com.example.backend.entity.Team;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 관리자 전용 컨트롤러
 * - 크롤링 관리
 * - 시스템 통계
 * - 경기 관리
 * - 사용자 관리
 * - 게시글 관리
 * - 신고 관리
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('ADMIN')") // 관리자만 접근 가능
public class AdminController {

    // 기존 의존성
    private final EplScheduleCrawler eplScheduleCrawler;
    private final NbaScheduleCrawler nbaScheduleCrawler;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;

    // 새로운 의존성
    private final AdminService adminService;

    // ==================== 크롤링 관리 (기존) ====================

    /**
     * EPL 전체 시즌 크롤링 수동 실행
     */
    @PostMapping("/crawl/epl")
    public ResponseEntity<Map<String, Object>> crawlEplSchedule() {
        log.info("=== EPL 크롤링 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 크롤링 실행 (별도 스레드)
            new Thread(() -> {
                try {
                    log.info("EPL 크롤링 시작...");
                    eplScheduleCrawler.crawlFullSeason();
                    log.info("EPL 크롤링 완료!");
                } catch (Exception e) {
                    log.error("EPL 크롤링 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "EPL 크롤링이 시작되었습니다. 완료까지 수 분이 소요될 수 있습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("크롤링 실행 실패", e);
            response.put("success", false);
            response.put("message", "크롤링 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * NBA 전체 시즌 크롤링 수동 실행
     */
    @PostMapping("/crawl/nba")
    public ResponseEntity<Map<String, Object>> crawlNbaSchedule() {
        log.info("=== NBA 크롤링 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 크롤링 실행 (별도 스레드)
            new Thread(() -> {
                try {
                    log.info("NBA 크롤링 시작...");
                    nbaScheduleCrawler.crawlFullSeason();
                    log.info("NBA 크롤링 완료!");
                } catch (Exception e) {
                    log.error("NBA 크롤링 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "NBA 크롤링이 시작되었습니다. 완료까지 수 분이 소요될 수 있습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("크롤링 실행 실패", e);
            response.put("success", false);
            response.put("message", "크롤링 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * DB 데이터 확인 (리그 및 팀)
     */
    @GetMapping("/check-db")
    public ResponseEntity<Map<String, Object>> checkDatabaseData() {
        log.info("=== DB 데이터 확인 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            List<League> allLeagues = leagueRepository.findAll();

            League eplLeague = leagueRepository.findById(1L).orElse(null);
            League nbaLeague = leagueRepository.findById(2L).orElse(null);

            long eplTeamCount = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 1L)
                    .count();

            long nbaTeamCount = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 2L)
                    .count();

            List<Map<String, Object>> nbaTeams = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 2L)
                    .map(t -> {
                        Map<String, Object> teamInfo = new HashMap<>();
                        teamInfo.put("teamId", t.getTeamId());
                        teamInfo.put("teamName", t.getTeamName());
                        return teamInfo;
                    })
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("totalLeagues", allLeagues.size());
            response.put("eplLeague", eplLeague != null ? Map.of(
                    "leagueId", eplLeague.getLeagueId(),
                    "leagueName", eplLeague.getLeagueName()
            ) : "NOT FOUND");
            response.put("nbaLeague", nbaLeague != null ? Map.of(
                    "leagueId", nbaLeague.getLeagueId(),
                    "leagueName", nbaLeague.getLeagueName()
            ) : "NOT FOUND");
            response.put("eplTeamCount", eplTeamCount);
            response.put("nbaTeamCount", nbaTeamCount);
            response.put("nbaTeams", nbaTeams);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("DB 데이터 확인 실패", e);
            response.put("success", false);
            response.put("message", "DB 데이터 확인 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 시스템 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "시스템이 정상 작동 중입니다.");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    // ==================== 통계 (신규) ====================

    /**
     * 빠른 통계 조회 (사이드바용)
     */
    @GetMapping("/stats")
    public ResponseEntity<QuickStatsDTO> getQuickStats() {
        QuickStatsDTO stats = adminService.getQuickStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 상세 통계 조회 (대시보드용)
     */
    @GetMapping("/stats/detail")
    public ResponseEntity<DetailedStatsDTO> getDetailedStats() {
        DetailedStatsDTO stats = adminService.getDetailedStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 최근 활동 조회
     */
    @GetMapping("/activities/recent")
    public ResponseEntity<List<ActivityDTO>> getRecentActivities() {
        List<ActivityDTO> activities = adminService.getRecentActivities();
        return ResponseEntity.ok(activities);
    }

    // ==================== 경기 관리 (신규) ====================

    /**
     * 전체 경기 조회 (관리자용)
     */
    @GetMapping("/matches")
    public ResponseEntity<List<AdminMatchDTO>> getAllMatches(
            @RequestParam(required = false) String sport) {
        List<AdminMatchDTO> matches = adminService.getAllMatches(sport);
        return ResponseEntity.ok(matches);
    }

    /**
     * 경기 점수 업데이트
     */
    @PutMapping("/matches/{matchId}/score")
    public ResponseEntity<Void> updateMatchScore(
            @PathVariable Long matchId,
            @RequestBody UpdateScoreRequest request) {
        adminService.updateMatchScore(matchId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 경기 종료 처리
     */
    @PutMapping("/matches/{matchId}/finish")
    public ResponseEntity<Void> finishMatch(@PathVariable Long matchId) {
        adminService.finishMatch(matchId);
        return ResponseEntity.ok().build();
    }

    /**
     * 경기 삭제
     */
    @DeleteMapping("/matches/{matchId}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long matchId) {
        adminService.deleteMatch(matchId);
        return ResponseEntity.ok().build();
    }

    // ==================== 사용자 관리 (신규) ====================

    /**
     * 사용자 목록 조회
     */
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDTO>> getUsers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<AdminUserDTO> users = adminService.getUsers(status, search, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * 사용자 정지
     */
    @PostMapping("/users/{username}/ban")
    public ResponseEntity<Void> banUser(
            @PathVariable String username,
            @RequestBody BanUserRequest request) {
        adminService.banUser(username, request.getReason());
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 정지 해제
     */
    @PostMapping("/users/{username}/unban")
    public ResponseEntity<Void> unbanUser(@PathVariable String username) {
        adminService.unbanUser(username);
        return ResponseEntity.ok().build();
    }

    // ==================== 게시글 관리 (신규) ====================

    /**
     * 전체 게시글 조회 (관리자용)
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<AdminPostDTO>> getAllPosts(Pageable pageable) {
        Page<AdminPostDTO> posts = adminService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 게시글 강제 삭제
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        adminService.deletePost(postId);
        return ResponseEntity.ok().build();
    }

    // ==================== 신고 관리 (신규) ====================

    /**
     * 미처리 신고 목록 조회
     */
    @GetMapping("/reports")
    public ResponseEntity<List<AdminReportDTO>> getPendingReports() {
        List<AdminReportDTO> reports = adminService.getPendingReports();
        return ResponseEntity.ok(reports);
    }

    /**
     * 신고 처리
     */
    @PostMapping("/reports/{reportId}/resolve")
    public ResponseEntity<Void> resolveReport(@PathVariable Long reportId) {
        adminService.resolveReport(reportId);
        return ResponseEntity.ok().build();
    }
}