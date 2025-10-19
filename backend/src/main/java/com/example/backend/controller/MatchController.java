package com.example.backend.controller;

import com.example.backend.dto.MatchDto;
import com.example.backend.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 경기 일정 Controller
 * 경기 조회 API 제공
 */
@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")  // React 개발 서버 허용
public class MatchController {

    private final MatchService matchService;

    /**
     * 특정 날짜의 경기 조회
     * GET /api/matches?date=2025-10-16&sport=FOOTBALL
     */
    @GetMapping
    public ResponseEntity<List<MatchDto>> getMatches(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "ALL") String sport) {

        System.out.println("=== 경기 조회 요청 ===");
        System.out.println("날짜: " + date);
        System.out.println("종목: " + sport);

        List<MatchDto> matches = matchService.getMatchesByDate(date, sport);

        System.out.println("조회된 경기 수: " + matches.size());

        return ResponseEntity.ok(matches);
    }

    /**
     * 날짜 범위로 경기 조회
     * GET /api/matches/range?startDate=2025-10-16&endDate=2025-10-20&sport=ALL
     */
    @GetMapping("/range")
    public ResponseEntity<List<MatchDto>> getMatchesByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "ALL") String sport) {

        System.out.println("=== 경기 범위 조회 요청 ===");
        System.out.println("시작 날짜: " + startDate);
        System.out.println("종료 날짜: " + endDate);
        System.out.println("종목: " + sport);

        List<MatchDto> matches = matchService.getMatchesByDateRange(startDate, endDate, sport);

        System.out.println("조회된 경기 수: " + matches.size());

        return ResponseEntity.ok(matches);
    }
}