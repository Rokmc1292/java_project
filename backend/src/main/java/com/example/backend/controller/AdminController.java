package com.example.backend.controller;

import com.example.backend.scheduler.EplScheduleCrawler;
import com.example.backend.scheduler.NbaScheduleCrawler;
import com.example.backend.scheduler.BundesligaScheduleCrawler;
import com.example.backend.scheduler.LaLigaScheduleCrawler;
import com.example.backend.scheduler.EplLiveScoreUpdater;
import com.example.backend.scheduler.NbaLiveScoreUpdater;
import com.example.backend.scheduler.BundesligaLiveScoreUpdater;
import com.example.backend.scheduler.LaLigaLiveScoreUpdater;
import com.example.backend.repository.LeagueRepository;
import com.example.backend.repository.TeamRepository;
import com.example.backend.entity.League;
import com.example.backend.entity.Team;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 관리자 API Controller
 * 크롤링 수동 실행 등 관리 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final EplScheduleCrawler eplScheduleCrawler;
    private final NbaScheduleCrawler nbaScheduleCrawler;
    private final BundesligaScheduleCrawler bundesligaScheduleCrawler;
    private final LaLigaScheduleCrawler laLigaScheduleCrawler;
    private final EplLiveScoreUpdater eplLiveScoreUpdater;
    private final NbaLiveScoreUpdater nbaLiveScoreUpdater;
    private final BundesligaLiveScoreUpdater bundesligaLiveScoreUpdater;
    private final LaLigaLiveScoreUpdater laLigaLiveScoreUpdater;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;

    /**
     * EPL 전체 시즌 크롤링 수동 실행
     * GET /api/admin/crawl/epl
     */
    @PostMapping("/crawl/epl")
    public ResponseEntity<Map<String, Object>> crawlEplSchedule() {
        log.info("=== EPL 크롤링 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 크롤링 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
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
     * POST /api/admin/crawl/nba
     */
    @PostMapping("/crawl/nba")
    public ResponseEntity<Map<String, Object>> crawlNbaSchedule() {
        log.info("=== NBA 크롤링 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 크롤링 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
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
     * 분데스리가 전체 시즌 크롤링 수동 실행
     * POST /api/admin/crawl/bundesliga
     */
    @PostMapping("/crawl/bundesliga")
    public ResponseEntity<Map<String, Object>> crawlBundesligaSchedule() {
        log.info("=== 분데스리가 크롤링 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 크롤링 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
            new Thread(() -> {
                try {
                    log.info("분데스리가 크롤링 시작...");
                    bundesligaScheduleCrawler.crawlFullSeason();
                    log.info("분데스리가 크롤링 완료!");
                } catch (Exception e) {
                    log.error("분데스리가 크롤링 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "분데스리가 크롤링이 시작되었습니다. 완료까지 수 분이 소요될 수 있습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("크롤링 실행 실패", e);
            response.put("success", false);
            response.put("message", "크롤링 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 라리가 전체 시즌 크롤링 수동 실행
     * POST /api/admin/crawl/laliga
     */
    @PostMapping("/crawl/laliga")
    public ResponseEntity<Map<String, Object>> crawlLaLigaSchedule() {
        log.info("=== 라리가 크롤링 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 크롤링 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
            new Thread(() -> {
                try {
                    log.info("라리가 크롤링 시작...");
                    laLigaScheduleCrawler.crawlFullSeason();
                    log.info("라리가 크롤링 완료!");
                } catch (Exception e) {
                    log.error("라리가 크롤링 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "라리가 크롤링이 시작되었습니다. 완료까지 수 분이 소요될 수 있습니다.");

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
     * GET /api/admin/check-db
     */
    @GetMapping("/check-db")
    public ResponseEntity<Map<String, Object>> checkDatabaseData() {
        log.info("=== DB 데이터 확인 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 전체 리그 조회
            List<League> allLeagues = leagueRepository.findAll();
            log.info("전체 리그 수: {}", allLeagues.size());

            // EPL 리그 확인
            League eplLeague = leagueRepository.findById(1L).orElse(null);

            // NBA 리그 확인
            League nbaLeague = leagueRepository.findById(2L).orElse(null);

            // 분데스리가 리그 확인
            League bundesligaLeague = leagueRepository.findById(6L).orElse(null);

            // 라리가 리그 확인
            League laLigaLeague = leagueRepository.findById(7L).orElse(null);

            // EPL 팀 수 확인
            long eplTeamCount = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 1L)
                    .count();

            // NBA 팀 수 확인
            long nbaTeamCount = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 2L)
                    .count();

            // 분데스리가 팀 수 확인
            long bundesligaTeamCount = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 6L)
                    .count();

            // 라리가 팀 수 확인
            long laLigaTeamCount = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 7L)
                    .count();

            // NBA 팀 목록
            List<Map<String, Object>> nbaTeams = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 2L)
                    .map(t -> {
                        Map<String, Object> teamInfo = new HashMap<>();
                        teamInfo.put("teamId", t.getTeamId());
                        teamInfo.put("teamName", t.getTeamName());
                        return teamInfo;
                    })
                    .collect(Collectors.toList());

            // 분데스리가 팀 목록
            List<Map<String, Object>> bundesligaTeams = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 6L)
                    .map(t -> {
                        Map<String, Object> teamInfo = new HashMap<>();
                        teamInfo.put("teamId", t.getTeamId());
                        teamInfo.put("teamName", t.getTeamName());
                        return teamInfo;
                    })
                    .collect(Collectors.toList());

            // 라리가 팀 목록
            List<Map<String, Object>> laLigaTeams = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 7L)
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
            response.put("bundesligaLeague", bundesligaLeague != null ? Map.of(
                "leagueId", bundesligaLeague.getLeagueId(),
                "leagueName", bundesligaLeague.getLeagueName()
            ) : "NOT FOUND");
            response.put("laLigaLeague", laLigaLeague != null ? Map.of(
                "leagueId", laLigaLeague.getLeagueId(),
                "leagueName", laLigaLeague.getLeagueName()
            ) : "NOT FOUND");
            response.put("eplTeamCount", eplTeamCount);
            response.put("nbaTeamCount", nbaTeamCount);
            response.put("bundesligaTeamCount", bundesligaTeamCount);
            response.put("laLigaTeamCount", laLigaTeamCount);
            response.put("nbaTeams", nbaTeams);
            response.put("bundesligaTeams", bundesligaTeams);
            response.put("laLigaTeams", laLigaTeams);

            log.info("EPL 리그: {}, 팀 수: {}", eplLeague != null ? "존재" : "없음", eplTeamCount);
            log.info("NBA 리그: {}, 팀 수: {}", nbaLeague != null ? "존재" : "없음", nbaTeamCount);
            log.info("분데스리가 리그: {}, 팀 수: {}", bundesligaLeague != null ? "존재" : "없음", bundesligaTeamCount);
            log.info("라리가 리그: {}, 팀 수: {}", laLigaLeague != null ? "존재" : "없음", laLigaTeamCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("DB 데이터 확인 실패", e);
            response.put("success", false);
            response.put("message", "DB 데이터 확인 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * EPL 실시간 점수 업데이트 수동 실행
     * POST /api/admin/live/epl
     */
    @PostMapping("/live/epl")
    public ResponseEntity<Map<String, Object>> updateEplLiveScores() {
        log.info("=== EPL 실시간 점수 업데이트 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 실시간 업데이트 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
            new Thread(() -> {
                try {
                    log.info("EPL 실시간 업데이트 시작...");
                    eplLiveScoreUpdater.updateLiveScores();
                    log.info("EPL 실시간 업데이트 완료!");
                } catch (Exception e) {
                    log.error("EPL 실시간 업데이트 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "EPL 실시간 점수 업데이트가 시작되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("실시간 업데이트 실행 실패", e);
            response.put("success", false);
            response.put("message", "실시간 업데이트 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * NBA 실시간 점수 업데이트 수동 실행
     * POST /api/admin/live/nba
     */
    @PostMapping("/live/nba")
    public ResponseEntity<Map<String, Object>> updateNbaLiveScores() {
        log.info("=== NBA 실시간 점수 업데이트 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 실시간 업데이트 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
            new Thread(() -> {
                try {
                    log.info("NBA 실시간 업데이트 시작...");
                    nbaLiveScoreUpdater.updateLiveScores();
                    log.info("NBA 실시간 업데이트 완료!");
                } catch (Exception e) {
                    log.error("NBA 실시간 업데이트 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "NBA 실시간 점수 업데이트가 시작되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("실시간 업데이트 실행 실패", e);
            response.put("success", false);
            response.put("message", "실시간 업데이트 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 분데스리가 실시간 점수 업데이트 수동 실행
     * POST /api/admin/live/bundesliga
     */
    @PostMapping("/live/bundesliga")
    public ResponseEntity<Map<String, Object>> updateBundesligaLiveScores() {
        log.info("=== 분데스리가 실시간 점수 업데이트 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 실시간 업데이트 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
            new Thread(() -> {
                try {
                    log.info("분데스리가 실시간 업데이트 시작...");
                    bundesligaLiveScoreUpdater.updateLiveScores();
                    log.info("분데스리가 실시간 업데이트 완료!");
                } catch (Exception e) {
                    log.error("분데스리가 실시간 업데이트 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "분데스리가 실시간 점수 업데이트가 시작되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("실시간 업데이트 실행 실패", e);
            response.put("success", false);
            response.put("message", "실시간 업데이트 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 라리가 실시간 점수 업데이트 수동 실행
     * POST /api/admin/live/laliga
     */
    @PostMapping("/live/laliga")
    public ResponseEntity<Map<String, Object>> updateLaLigaLiveScores() {
        log.info("=== 라리가 실시간 점수 업데이트 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 실시간 업데이트 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
            new Thread(() -> {
                try {
                    log.info("라리가 실시간 업데이트 시작...");
                    laLigaLiveScoreUpdater.updateLiveScores();
                    log.info("라리가 실시간 업데이트 완료!");
                } catch (Exception e) {
                    log.error("라리가 실시간 업데이트 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "라리가 실시간 점수 업데이트가 시작되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("실시간 업데이트 실행 실패", e);
            response.put("success", false);
            response.put("message", "실시간 업데이트 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 시스템 상태 확인
     * GET /api/admin/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "시스템이 정상 작동 중입니다.");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}
