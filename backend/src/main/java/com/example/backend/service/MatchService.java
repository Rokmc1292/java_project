package com.example.backend.service;

import com.example.backend.dto.MatchDto;
import com.example.backend.entity.Match;
import com.example.backend.entity.MmaFight;
import com.example.backend.repository.MatchRepository;
import com.example.backend.repository.MmaFightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 경기 Service
 * 경기 일정 조회 및 DTO 변환
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

    private final MatchRepository matchRepository;
    private final MmaFightRepository mmaFightRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * 특정 날짜의 모든 경기 조회
     */
    public List<MatchDto> getMatchesByDate(LocalDate date, String sportType) {
        List<MatchDto> allMatches = new ArrayList<>();

        // UFC 경기 조회
        if ("ALL".equalsIgnoreCase(sportType) || "MMA".equalsIgnoreCase(sportType)) {
            List<MmaFight> mmaFights = mmaFightRepository.findByFightDate(date);
            allMatches.addAll(mmaFights.stream()
                    .map(this::convertMmaFightToDto)
                    .collect(Collectors.toList()));
        }

        // 일반 경기 조회 (축구, 농구, 야구, 롤)
        if ("ALL".equalsIgnoreCase(sportType)) {
            List<Match> matches = matchRepository.findByMatchDate(date);
            allMatches.addAll(matches.stream()
                    .map(this::convertMatchToDto)
                    .collect(Collectors.toList()));
        } else if (!"MMA".equalsIgnoreCase(sportType)) {
            List<Match> matches = matchRepository.findByMatchDateAndSport(date, sportType);
            allMatches.addAll(matches.stream()
                    .map(this::convertMatchToDto)
                    .collect(Collectors.toList()));
        }

        return allMatches;
    }

    /**
     * 날짜 범위로 경기 조회
     */
    public List<MatchDto> getMatchesByDateRange(LocalDate startDate, LocalDate endDate, String sportType) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<MatchDto> allMatches = new ArrayList<>();

        // UFC 경기 조회
        if ("ALL".equalsIgnoreCase(sportType) || "MMA".equalsIgnoreCase(sportType)) {
            List<MmaFight> mmaFights = mmaFightRepository.findByDateRange(startDateTime, endDateTime);
            allMatches.addAll(mmaFights.stream()
                    .map(this::convertMmaFightToDto)
                    .collect(Collectors.toList()));
        }

        // 일반 경기 조회
        if (!"MMA".equalsIgnoreCase(sportType)) {
            List<Match> matches = matchRepository.findByDateRange(startDateTime, endDateTime);
            allMatches.addAll(matches.stream()
                    .filter(m -> "ALL".equalsIgnoreCase(sportType) ||
                            m.getLeague().getSport().getSportName().equalsIgnoreCase(sportType))
                    .map(this::convertMatchToDto)
                    .collect(Collectors.toList()));
        }

        return allMatches;
    }

    /**
     * Match 엔티티를 MatchDto로 변환
     */
    private MatchDto convertMatchToDto(Match match) {
        return MatchDto.builder()
                .matchId(match.getMatchId())
                .sportType(match.getLeague().getSport().getSportName())
                .league(MatchDto.LeagueInfo.builder()
                        .leagueId(match.getLeague().getLeagueId())
                        .name(match.getLeague().getLeagueName())
                        .country(match.getLeague().getCountry())
                        .logo(buildImageUrl(match.getLeague().getLeagueLogo()))
                        .build())
                .teams(MatchDto.TeamInfo.builder()
                        .home(MatchDto.Team.builder()
                                .id(match.getHomeTeam().getTeamId())
                                .name(match.getHomeTeam().getTeamName())
                                .logo(buildImageUrl(match.getHomeTeam().getTeamLogo()))
                                .country(match.getHomeTeam().getCountry())
                                .build())
                        .away(MatchDto.Team.builder()
                                .id(match.getAwayTeam().getTeamId())
                                .name(match.getAwayTeam().getTeamName())
                                .logo(buildImageUrl(match.getAwayTeam().getTeamLogo()))
                                .country(match.getAwayTeam().getCountry())
                                .build())
                        .build())
                .detail(MatchDto.MatchDetail.builder()
                        .matchDate(match.getMatchDate())
                        .venue(match.getVenue())
                        .status(match.getStatus())
                        .build())
                .score(MatchDto.Score.builder()
                        .home(match.getHomeScore())
                        .away(match.getAwayScore())
                        .build())
                .build();
    }

    /**
     * MmaFight 엔티티를 MatchDto로 변환
     */
    private MatchDto convertMmaFightToDto(MmaFight fight) {
        return MatchDto.builder()
                .matchId(fight.getFightId())
                .sportType("MMA")
                .league(MatchDto.LeagueInfo.builder()
                        .leagueId(fight.getLeague().getLeagueId())
                        .name(fight.getLeague().getLeagueName())
                        .country(fight.getLeague().getCountry())
                        .logo(buildImageUrl(fight.getLeague().getLeagueLogo()))
                        .build())
                .teams(MatchDto.TeamInfo.builder()
                        .home(MatchDto.Team.builder()
                                .id(fight.getFighter1().getFighterId())
                                .name(fight.getFighter1().getFighterName())
                                .logo(buildImageUrl(fight.getFighter1().getFighterImage()))
                                .country(fight.getFighter1().getCountry())
                                .weightClass(fight.getFighter1().getWeightClass())
                                .record(fight.getFighter1().getRecord())
                                .build())
                        .away(MatchDto.Team.builder()
                                .id(fight.getFighter2().getFighterId())
                                .name(fight.getFighter2().getFighterName())
                                .logo(buildImageUrl(fight.getFighter2().getFighterImage()))
                                .country(fight.getFighter2().getCountry())
                                .weightClass(fight.getFighter2().getWeightClass())
                                .record(fight.getFighter2().getRecord())
                                .build())
                        .build())
                .detail(MatchDto.MatchDetail.builder()
                        .matchDate(fight.getFightDate())
                        .venue(fight.getVenue())
                        .status(fight.getStatus())
                        .eventName(fight.getEventName())
                        .winner(fight.getWinner() != null ? fight.getWinner().getFighterName() : null)
                        .method(fight.getMethod())
                        .round(fight.getRound())
                        .build())
                .score(null)  // UFC는 점수가 없음
                .build();
    }

    /**
     * 이미지 URL을 절대 경로로 변환
     * 상대 경로인 경우 baseUrl을 앞에 붙임
     */
    private String buildImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        // 이미 절대 URL인 경우 (http:// 또는 https://로 시작)
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }

        // 상대 경로인 경우 baseUrl 추가
        return baseUrl + imagePath;
    }
}