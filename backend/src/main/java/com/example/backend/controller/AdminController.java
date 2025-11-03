package com.example.backend.controller;

import com.example.backend.scheduler.EplScheduleCrawler;
import com.example.backend.scheduler.NbaScheduleCrawler;
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

            // EPL 팀 수 확인
            long eplTeamCount = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 1L)
                    .count();

            // NBA 팀 수 확인
            long nbaTeamCount = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 2L)
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

            log.info("EPL 리그: {}, 팀 수: {}", eplLeague != null ? "존재" : "없음", eplTeamCount);
            log.info("NBA 리그: {}, 팀 수: {}", nbaLeague != null ? "존재" : "없음", nbaTeamCount);

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
