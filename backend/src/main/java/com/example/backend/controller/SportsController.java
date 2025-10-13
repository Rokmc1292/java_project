package com.example.backend.controller;

import com.example.backend.dto.MatchDto;
import com.example.backend.service.SportsApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 스포츠 경기 정보 API Controller
 * 축구, 농구, 야구, e스포츠, UFC 경기 일정 및 실시간 점수 조회
 */
@RestController
@RequestMapping("/api/sports")
@RequiredArgsConstructor
public class SportsController {

    private final SportsApiService sportsApiService;

    /**
     * 경기 일정 조회
     * GET /api/sports/fixtures?date=2025-01-15&sport=football
     *
     * @param date 조회 날짜 (선택, 기본값: 오늘)
     * @param sport 종목 (선택, all/football/basketball/baseball/esports/mma, 기본값: all)
     * @return 경기 목록
     */
    @GetMapping("/fixtures")
    public ResponseEntity<List<MatchDto>> getFixtures(
            @RequestParam(required = false) String date,
            @RequestParam(required = false, defaultValue = "all") String sport
    ) {
        // 날짜가 없으면 오늘 날짜 사용
        if (date == null || date.isEmpty()) {
            date = LocalDate.now().toString();
        }

        List<MatchDto> matches = sportsApiService.getAllFixtures(date, sport);
        return ResponseEntity.ok(matches);
    }

    /**
     * 실시간 경기 조회
     * GET /api/sports/live
     *
     * @return 실시간 경기 목록
     */
    @GetMapping("/live")
    public ResponseEntity<List<MatchDto>> getLiveMatches() {
        List<MatchDto> liveMatches = sportsApiService.getAllLiveMatches();
        return ResponseEntity.ok(liveMatches);
    }

    /**
     * 종목별 경기 일정 조회
     * GET /api/sports/football?date=2025-01-15
     */
    @GetMapping("/football")
    public ResponseEntity<List<MatchDto>> getFootballFixtures(
            @RequestParam(required = false) String date
    ) {
        if (date == null || date.isEmpty()) {
            date = LocalDate.now().toString();
        }
        List<MatchDto> matches = sportsApiService.getFootballFixtures(date);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/basketball")
    public ResponseEntity<List<MatchDto>> getBasketballGames(
            @RequestParam(required = false) String date
    ) {
        if (date == null || date.isEmpty()) {
            date = LocalDate.now().toString();
        }
        List<MatchDto> matches = sportsApiService.getBasketballGames(date);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/baseball")
    public ResponseEntity<List<MatchDto>> getBaseballGames(
            @RequestParam(required = false) String date
    ) {
        if (date == null || date.isEmpty()) {
            date = LocalDate.now().toString();
        }
        List<MatchDto> matches = sportsApiService.getBaseballGames(date);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/esports")
    public ResponseEntity<List<MatchDto>> getEsportsMatches(
            @RequestParam(required = false) String date
    ) {
        if (date == null || date.isEmpty()) {
            date = LocalDate.now().toString();
        }
        List<MatchDto> matches = sportsApiService.getEsportsMatches(date);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/ufc")
    public ResponseEntity<List<MatchDto>> getUfcEvents(
            @RequestParam(required = false) String date
    ) {
        if (date == null || date.isEmpty()) {
            date = LocalDate.now().toString();
        }
        List<MatchDto> matches = sportsApiService.getUfcEventsByDate(date);
        return ResponseEntity.ok(matches);
    }
}