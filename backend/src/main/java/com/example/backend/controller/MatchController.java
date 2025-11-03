package com.example.backend.controller;

import com.example.backend.dto.MatchDto;
import com.example.backend.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 경기 일정 Controller
 * 경기 조회 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")  // React 개발 서버 허용
public class MatchController {

    private final MatchService matchService;

    /**
     * 특정 경기 상세 정보 조회
     * GET /api/matches/{matchId}
     */
    @GetMapping("/{matchId}")
    public ResponseEntity<MatchDto> getMatch(@PathVariable Long matchId) {
        log.debug("경기 상세 조회 요청 - matchId: {}", matchId);

        MatchDto match = matchService.getMatchById(matchId);

        log.debug("경기 상세 조회 완료 - matchId: {}", matchId);

        return ResponseEntity.ok(match);
    }

    /**
     * 특정 날짜의 경기 조회
     * GET /api/matches?date=2025-10-16&sport=FOOTBALL
     */
    @GetMapping(params = "date")
    public ResponseEntity<List<MatchDto>> getMatches(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "ALL") String sport) {

        log.debug("경기 조회 요청 - 날짜: {}, 종목: {}", date, sport);

        List<MatchDto> matches = matchService.getMatchesByDate(date, sport);

        log.debug("조회된 경기 수: {}", matches.size());

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

        log.debug("경기 범위 조회 요청 - 시작: {}, 종료: {}, 종목: {}", startDate, endDate, sport);

        List<MatchDto> matches = matchService.getMatchesByDateRange(startDate, endDate, sport);

        log.debug("조회된 경기 수: {}", matches.size());

        return ResponseEntity.ok(matches);
    }
}