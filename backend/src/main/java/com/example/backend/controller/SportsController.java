package com.example.backend.controller;

import com.example.backend.dto.MatchDto;
import com.example.backend.service.SportsApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 스포츠 경기 정보 API 컨트롤러
 */
@RestController
@RequestMapping("/api/sports")
@RequiredArgsConstructor
public class SportsController {

    private final SportsApiService sportsApiService;

    /**
     * 경기 일정 조회
     * GET /api/sports/fixtures?date=2024-12-20&sport=all
     *
     * @param date 조회할 날짜 (YYYY-MM-DD)
     * @param sport 종목 (all, football, basketball, baseball, mma, esports)
     * @return 경기 목록
     */
    @GetMapping("/fixtures")
    public ResponseEntity<List<MatchDto>> getFixtures(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(defaultValue = "all") String sport) {

        String dateStr = (date != null)
                ? date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        System.out.println("경기 일정 요청 - 날짜: " + dateStr + ", 종목: " + sport);

        List<MatchDto> matches = sportsApiService.getAllFixtures(dateStr, sport);

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
        System.out.println("실시간 경기 요청");

        List<MatchDto> matches = sportsApiService.getAllLiveMatches();

        return ResponseEntity.ok(matches);
    }

    /**
     * 축구 경기 일정 조회
     * GET /api/sports/football?date=2024-12-20
     */
    @GetMapping("/football")
    public ResponseEntity<List<MatchDto>> getFootballFixtures(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        String dateStr = (date != null)
                ? date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<MatchDto> matches = sportsApiService.getFootballFixtures(dateStr);

        return ResponseEntity.ok(matches);
    }

    /**
     * 농구 경기 일정 조회
     * GET /api/sports/basketball?date=2024-12-20
     */
    @GetMapping("/basketball")
    public ResponseEntity<List<MatchDto>> getBasketballGames(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        String dateStr = (date != null)
                ? date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<MatchDto> matches = sportsApiService.getBasketballGames(dateStr);

        return ResponseEntity.ok(matches);
    }

    /**
     * 야구 경기 일정 조회
     * GET /api/sports/baseball?date=2024-12-20
     */
    @GetMapping("/baseball")
    public ResponseEntity<List<MatchDto>> getBaseballGames(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        String dateStr = (date != null)
                ? date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<MatchDto> matches = sportsApiService.getBaseballGames(dateStr);

        return ResponseEntity.ok(matches);
    }

    /**
     * MMA 경기 일정 조회 (UFC)
     * GET /api/sports/mma?date=2024-12-20
     */
    @GetMapping("/mma")
    public ResponseEntity<List<MatchDto>> getMmaFights(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        String dateStr = (date != null)
                ? date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<MatchDto> matches = sportsApiService.getMmaFights(dateStr);

        return ResponseEntity.ok(matches);
    }

    /**
     * e스포츠 경기 일정 조회
     * GET /api/sports/esports?date=2024-12-20
     */
    @GetMapping("/esports")
    public ResponseEntity<List<MatchDto>> getEsportsMatches(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        String dateStr = (date != null)
                ? date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<MatchDto> matches = sportsApiService.getEsportsMatches(dateStr);

        return ResponseEntity.ok(matches);
    }
}