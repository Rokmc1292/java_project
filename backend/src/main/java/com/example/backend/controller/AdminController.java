package com.example.backend.controller;

import com.example.backend.scheduler.EplScheduleCrawler;
import com.example.backend.scheduler.NbaScheduleCrawler;
import com.example.backend.scheduler.BundesligaScheduleCrawler;
import com.example.backend.scheduler.LaLigaScheduleCrawler;
import com.example.backend.scheduler.SerieAScheduleCrawler;
import com.example.backend.scheduler.Ligue1ScheduleCrawler;
import com.example.backend.scheduler.KblScheduleCrawler;
import com.example.backend.scheduler.EplLiveScoreUpdater;
import com.example.backend.scheduler.NbaLiveScoreUpdater;
import com.example.backend.scheduler.BundesligaLiveScoreUpdater;
import com.example.backend.scheduler.LaLigaLiveScoreUpdater;
import com.example.backend.scheduler.SerieALiveScoreUpdater;
import com.example.backend.scheduler.Ligue1LiveScoreUpdater;
import com.example.backend.scheduler.KblLiveScoreUpdater;
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
public class AdminController {

    private final EplScheduleCrawler eplScheduleCrawler;
    private final NbaScheduleCrawler nbaScheduleCrawler;
    private final BundesligaScheduleCrawler bundesligaScheduleCrawler;
    private final LaLigaScheduleCrawler laLigaScheduleCrawler;
    private final SerieAScheduleCrawler serieAScheduleCrawler;
    private final Ligue1ScheduleCrawler ligue1ScheduleCrawler;
    private final KblScheduleCrawler kblScheduleCrawler;
    private final EplLiveScoreUpdater eplLiveScoreUpdater;
    private final NbaLiveScoreUpdater nbaLiveScoreUpdater;
    private final BundesligaLiveScoreUpdater bundesligaLiveScoreUpdater;
    private final LaLigaLiveScoreUpdater laLigaLiveScoreUpdater;
    private final SerieALiveScoreUpdater serieALiveScoreUpdater;
    private final Ligue1LiveScoreUpdater ligue1LiveScoreUpdater;
    private final KblLiveScoreUpdater kblLiveScoreUpdater;
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
     * 세리에 A 전체 시즌 크롤링 수동 실행
     * POST /api/admin/crawl/seriea
     */
    @PostMapping("/crawl/seriea")
    public ResponseEntity<Map<String, Object>> crawlSerieASchedule() {
        log.info("=== 세리에 A 크롤링 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 크롤링 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
            new Thread(() -> {
                try {
                    log.info("세리에 A 크롤링 시작...");
                    serieAScheduleCrawler.crawlFullSeason();
                    log.info("세리에 A 크롤링 완료!");
                } catch (Exception e) {
                    log.error("세리에 A 크롤링 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "세리에 A 크롤링이 시작되었습니다. 완료까지 수 분이 소요될 수 있습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("크롤링 실행 실패", e);
            response.put("success", false);
            response.put("message", "크롤링 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 리그 1 전체 시즌 크롤링 수동 실행
     * POST /api/admin/crawl/ligue1
     */
    @PostMapping("/crawl/ligue1")
    public ResponseEntity<Map<String, Object>> crawlLigue1Schedule() {
        log.info("=== 리그 1 크롤링 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 크롤링 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
            new Thread(() -> {
                try {
                    log.info("리그 1 크롤링 시작...");
                    ligue1ScheduleCrawler.crawlFullSeason();
                    log.info("리그 1 크롤링 완료!");
                } catch (Exception e) {
                    log.error("리그 1 크롤링 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "리그 1 크롤링이 시작되었습니다. 완료까지 수 분이 소요될 수 있습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("크롤링 실행 실패", e);
            response.put("success", false);
            response.put("message", "크롤링 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * KBL 전체 시즌 크롤링 수동 실행
     * POST /api/admin/crawl/kbl
     */
    @PostMapping("/crawl/kbl")
    public ResponseEntity<Map<String, Object>> crawlKblSchedule() {
        log.info("=== KBL 크롤링 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 크롤링 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
            new Thread(() -> {
                try {
                    log.info("KBL 크롤링 시작...");
                    kblScheduleCrawler.crawlFullSeason();
                    log.info("KBL 크롤링 완료!");
                } catch (Exception e) {
                    log.error("KBL 크롤링 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "KBL 크롤링이 시작되었습니다. 완료까지 수 분이 소요될 수 있습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("크롤링 실행 실패", e);
            response.put("success", false);
            response.put("message", "크롤링 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 모든 리그 일정 크롤링 (순차 실행)
     * POST /api/admin/crawl/all-leagues
     * League ID 순서: EPL(1) → NBA(2) → Bundesliga(6) → La Liga(7) → Serie A(8) → Ligue 1(9) → KBL(10)
     */
    @PostMapping("/crawl/all-leagues")
    public ResponseEntity<Map<String, Object>> crawlAllLeagues() {
        log.info("=== 전체 리그 크롤링 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 크롤링 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
            new Thread(() -> {
                try {
                    log.info("🌍 ========== 전체 리그 크롤링 시작 ==========");

                    // League ID 순서대로 실행: 1 → 2 → 6 → 7 → 8 → 9 → 10

                    log.info("1️⃣ EPL 크롤링 시작 (league_id=1)");
                    eplScheduleCrawler.crawlFullSeason();
                    log.info("✅ EPL 크롤링 완료");

                    log.info("2️⃣ NBA 크롤링 시작 (league_id=2)");
                    nbaScheduleCrawler.crawlFullSeason();
                    log.info("✅ NBA 크롤링 완료");

                    log.info("6️⃣ Bundesliga 크롤링 시작 (league_id=6)");
                    bundesligaScheduleCrawler.crawlFullSeason();
                    log.info("✅ Bundesliga 크롤링 완료");

                    log.info("7️⃣ La Liga 크롤링 시작 (league_id=7)");
                    laLigaScheduleCrawler.crawlFullSeason();
                    log.info("✅ La Liga 크롤링 완료");

                    log.info("8️⃣ Serie A 크롤링 시작 (league_id=8)");
                    serieAScheduleCrawler.crawlFullSeason();
                    log.info("✅ Serie A 크롤링 완료");

                    log.info("9️⃣ Ligue 1 크롤링 시작 (league_id=9)");
                    ligue1ScheduleCrawler.crawlFullSeason();
                    log.info("✅ Ligue 1 크롤링 완료");

                    log.info("🔟 KBL 크롤링 시작 (league_id=10)");
                    kblScheduleCrawler.crawlFullSeason();
                    log.info("✅ KBL 크롤링 완료");

                    log.info("🎉 ========== 전체 리그 크롤링 완료 ==========");

                } catch (Exception e) {
                    log.error("❌ 전체 리그 크롤링 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "전체 리그 크롤링이 시작되었습니다. League ID 순서대로 순차 실행됩니다 (EPL→NBA→Bundesliga→La Liga→Serie A→Ligue 1→KBL). 완료까지 상당한 시간이 소요될 수 있습니다.");
            response.put("leagues", List.of("EPL(1)", "NBA(2)", "Bundesliga(6)", "La Liga(7)", "Serie A(8)", "Ligue 1(9)", "KBL(10)"));

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

            // 세리에 A 리그 확인
            League serieALeague = leagueRepository.findById(8L).orElse(null);

            // 리그 1 리그 확인
            League ligue1League = leagueRepository.findById(9L).orElse(null);

            // KBL 리그 확인
            League kblLeague = leagueRepository.findById(10L).orElse(null);

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

            // 세리에 A 팀 수 확인
            long serieATeamCount = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 8L)
                    .count();

            // 리그 1 팀 수 확인
            long ligue1TeamCount = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 9L)
                    .count();

            // KBL 팀 수 확인
            long kblTeamCount = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 10L)
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

            // 세리에 A 팀 목록
            List<Map<String, Object>> serieATeams = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 8L)
                    .map(t -> {
                        Map<String, Object> teamInfo = new HashMap<>();
                        teamInfo.put("teamId", t.getTeamId());
                        teamInfo.put("teamName", t.getTeamName());
                        return teamInfo;
                    })
                    .collect(Collectors.toList());

            // 리그 1 팀 목록
            List<Map<String, Object>> ligue1Teams = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 9L)
                    .map(t -> {
                        Map<String, Object> teamInfo = new HashMap<>();
                        teamInfo.put("teamId", t.getTeamId());
                        teamInfo.put("teamName", t.getTeamName());
                        return teamInfo;
                    })
                    .collect(Collectors.toList());

            // KBL 팀 목록
            List<Map<String, Object>> kblTeams = teamRepository.findAll().stream()
                    .filter(t -> t.getLeague() != null && t.getLeague().getLeagueId() == 10L)
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
            response.put("serieALeague", serieALeague != null ? Map.of(
                "leagueId", serieALeague.getLeagueId(),
                "leagueName", serieALeague.getLeagueName()
            ) : "NOT FOUND");
            response.put("ligue1League", ligue1League != null ? Map.of(
                "leagueId", ligue1League.getLeagueId(),
                "leagueName", ligue1League.getLeagueName()
            ) : "NOT FOUND");
            response.put("kblLeague", kblLeague != null ? Map.of(
                "leagueId", kblLeague.getLeagueId(),
                "leagueName", kblLeague.getLeagueName()
            ) : "NOT FOUND");
            response.put("eplTeamCount", eplTeamCount);
            response.put("nbaTeamCount", nbaTeamCount);
            response.put("bundesligaTeamCount", bundesligaTeamCount);
            response.put("laLigaTeamCount", laLigaTeamCount);
            response.put("serieATeamCount", serieATeamCount);
            response.put("ligue1TeamCount", ligue1TeamCount);
            response.put("kblTeamCount", kblTeamCount);
            response.put("nbaTeams", nbaTeams);
            response.put("bundesligaTeams", bundesligaTeams);
            response.put("laLigaTeams", laLigaTeams);
            response.put("serieATeams", serieATeams);
            response.put("ligue1Teams", ligue1Teams);
            response.put("kblTeams", kblTeams);

            log.info("EPL 리그: {}, 팀 수: {}", eplLeague != null ? "존재" : "없음", eplTeamCount);
            log.info("NBA 리그: {}, 팀 수: {}", nbaLeague != null ? "존재" : "없음", nbaTeamCount);
            log.info("분데스리가 리그: {}, 팀 수: {}", bundesligaLeague != null ? "존재" : "없음", bundesligaTeamCount);
            log.info("라리가 리그: {}, 팀 수: {}", laLigaLeague != null ? "존재" : "없음", laLigaTeamCount);
            log.info("세리에 A 리그: {}, 팀 수: {}", serieALeague != null ? "존재" : "없음", serieATeamCount);
            log.info("리그 1 리그: {}, 팀 수: {}", ligue1League != null ? "존재" : "없음", ligue1TeamCount);
            log.info("KBL 리그: {}, 팀 수: {}", kblLeague != null ? "존재" : "없음", kblTeamCount);

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
     * 세리에 A 실시간 점수 업데이트 수동 실행
     * POST /api/admin/live/seriea
     */
    @PostMapping("/live/seriea")
    public ResponseEntity<Map<String, Object>> updateSerieALiveScores() {
        log.info("=== 세리에 A 실시간 점수 업데이트 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 실시간 업데이트 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
            new Thread(() -> {
                try {
                    log.info("세리에 A 실시간 업데이트 시작...");
                    serieALiveScoreUpdater.updateLiveScores();
                    log.info("세리에 A 실시간 업데이트 완료!");
                } catch (Exception e) {
                    log.error("세리에 A 실시간 업데이트 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "세리에 A 실시간 점수 업데이트가 시작되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("실시간 업데이트 실행 실패", e);
            response.put("success", false);
            response.put("message", "실시간 업데이트 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 리그 1 실시간 점수 업데이트 수동 실행
     * POST /api/admin/live/ligue1
     */
    @PostMapping("/live/ligue1")
    public ResponseEntity<Map<String, Object>> updateLigue1LiveScores() {
        log.info("=== 리그 1 실시간 점수 업데이트 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 실시간 업데이트 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
            new Thread(() -> {
                try {
                    log.info("리그 1 실시간 업데이트 시작...");
                    ligue1LiveScoreUpdater.updateLiveScores();
                    log.info("리그 1 실시간 업데이트 완료!");
                } catch (Exception e) {
                    log.error("리그 1 실시간 업데이트 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "리그 1 실시간 점수 업데이트가 시작되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("실시간 업데이트 실행 실패", e);
            response.put("success", false);
            response.put("message", "실시간 업데이트 실행 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * KBL 실시간 점수 업데이트 수동 실행
     * POST /api/admin/live/kbl
     */
    @PostMapping("/live/kbl")
    public ResponseEntity<Map<String, Object>> updateKblLiveScores() {
        log.info("=== KBL 실시간 점수 업데이트 수동 실행 요청 ===");

        Map<String, Object> response = new HashMap<>();

        try {
            // 실시간 업데이트 실행 (별도 스레드에서 실행하여 API 응답 지연 방지)
            new Thread(() -> {
                try {
                    log.info("KBL 실시간 업데이트 시작...");
                    kblLiveScoreUpdater.updateLiveScores();
                    log.info("KBL 실시간 업데이트 완료!");
                } catch (Exception e) {
                    log.error("KBL 실시간 업데이트 실행 중 오류 발생", e);
                }
            }).start();

            response.put("success", true);
            response.put("message", "KBL 실시간 점수 업데이트가 시작되었습니다.");

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
