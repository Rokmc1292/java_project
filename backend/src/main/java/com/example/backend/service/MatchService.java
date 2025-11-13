package com.example.backend.service;

import com.example.backend.dto.MatchDto;
import com.example.backend.entity.*;
import com.example.backend.repository.MatchRepository;
import com.example.backend.repository.MmaFightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MmaFightRepository mmaFightRepository;

    /**
     * 특정 경기 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public MatchDto getMatchById(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다."));
        return convertToDto(match);
    }

    @Transactional(readOnly = true)
    public List<MatchDto> getMatchesByDate(LocalDate date, String sportType) {
        LocalDateTime dateTime = date.atStartOfDay();
        List<MatchDto> result = new ArrayList<>();

        if (sportType == null || sportType.isEmpty() || sportType.equals("ALL")) {
            // 전체 종목 조회: Match + MmaFight
            List<Match> matches = matchRepository.findByMatchDate(dateTime);
            result.addAll(convertMatchesToDto(matches));

            List<MmaFight> mmaFights = mmaFightRepository.findByFightDate(date);
            result.addAll(convertMmaFightsToDto(mmaFights));
        } else if (sportType.equalsIgnoreCase("MMA")) {
            // MMA만 조회
            List<MmaFight> mmaFights = mmaFightRepository.findByFightDate(date);
            result.addAll(convertMmaFightsToDto(mmaFights));
        } else {
            // 다른 종목 조회 (축구, 농구 등)
            String sport = sportType.toUpperCase();
            List<Match> matches = matchRepository.findByMatchDateAndSport(dateTime, sport);
            result.addAll(convertMatchesToDto(matches));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<MatchDto> getMatchesByDateRange(LocalDate startDate, LocalDate endDate, String sportType) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        List<MatchDto> result = new ArrayList<>();

        if (sportType == null || sportType.isEmpty() || sportType.equals("ALL")) {
            // 전체 종목 조회: Match + MmaFight
            List<Match> matches = matchRepository.findByDateRange(startDateTime, endDateTime);
            result.addAll(convertMatchesToDto(matches));

            List<MmaFight> mmaFights = mmaFightRepository.findByDateRange(startDateTime, endDateTime);
            result.addAll(convertMmaFightsToDto(mmaFights));
        } else if (sportType.equalsIgnoreCase("MMA")) {
            // MMA만 조회
            List<MmaFight> mmaFights = mmaFightRepository.findByDateRange(startDateTime, endDateTime);
            result.addAll(convertMmaFightsToDto(mmaFights));
        } else {
            // 다른 종목 조회 (축구, 농구 등)
            String sport = sportType.toUpperCase();
            List<Match> matches = matchRepository.findByMatchDateBetweenAndSport(startDateTime, endDateTime, sport);
            result.addAll(convertMatchesToDto(matches));
        }

        return result;
    }

    private List<MatchDto> convertMatchesToDto(List<Match> matches) {
        return matches.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public MatchDto convertToDto(Match match) {
        MatchDto dto = new MatchDto();
        dto.setMatchId(match.getMatchId());
        dto.setSportType("MATCH");

        // 리그 정보
        if (match.getLeague() != null) {
            MatchDto.LeagueInfo leagueInfo = new MatchDto.LeagueInfo();
            leagueInfo.setName(match.getLeague().getLeagueName());
            leagueInfo.setLogo(match.getLeague().getLeagueLogo());


            dto.setLeague(leagueInfo);
        }

        // 팀 정보
        MatchDto.TeamInfo teamInfo = new MatchDto.TeamInfo();

        if (match.getHomeTeam() != null) {
            MatchDto.Team homeTeam = new MatchDto.Team();
            homeTeam.setName(match.getHomeTeam().getTeamName());
            homeTeam.setLogo(match.getHomeTeam().getTeamLogo());
            teamInfo.setHome(homeTeam);
        }

        if (match.getAwayTeam() != null) {
            MatchDto.Team awayTeam = new MatchDto.Team();
            awayTeam.setName(match.getAwayTeam().getTeamName());
            awayTeam.setLogo(match.getAwayTeam().getTeamLogo());
            teamInfo.setAway(awayTeam);
        }

        dto.setTeams(teamInfo);

        // 점수 정보
        MatchDto.Score score = new MatchDto.Score();
        score.setHome(match.getHomeScore());
        score.setAway(match.getAwayScore());
        dto.setScore(score);

        // 경기 상세 정보
        MatchDto.MatchDetail detail = new MatchDto.MatchDetail();
        detail.setMatchDate(match.getMatchDate());
        detail.setVenue(match.getVenue());
        detail.setStatus(match.getStatus());
        dto.setDetail(detail);

        return dto;
    }

    /**
     * MmaFight 리스트를 MatchDto 리스트로 변환
     */
    private List<MatchDto> convertMmaFightsToDto(List<MmaFight> mmaFights) {
        return mmaFights.stream()
                .map(this::convertMmaFightToDto)
                .collect(Collectors.toList());
    }

    /**
     * MmaFight를 MatchDto로 변환
     */
    private MatchDto convertMmaFightToDto(MmaFight fight) {
        MatchDto dto = new MatchDto();
        dto.setMatchId(fight.getFightId());
        dto.setSportType("MMA");

        // 리그 정보
        if (fight.getLeague() != null) {
            MatchDto.LeagueInfo leagueInfo = new MatchDto.LeagueInfo();
            leagueInfo.setName(fight.getLeague().getLeagueName());
            leagueInfo.setLogo(fight.getLeague().getLeagueLogo());
            dto.setLeague(leagueInfo);
        }

        // 파이터 정보 (팀으로 표시)
        MatchDto.TeamInfo teamInfo = new MatchDto.TeamInfo();

        if (fight.getFighter1() != null) {
            MatchDto.Team fighter1Team = new MatchDto.Team();
            fighter1Team.setId(fight.getFighter1().getFighterId());
            fighter1Team.setName(fight.getFighter1().getFighterName());
            fighter1Team.setLogo(fight.getFighter1().getFighterImage());
            fighter1Team.setCountry(fight.getFighter1().getCountry());
            fighter1Team.setWeightClass(fight.getFighter1().getWeightClass());
            fighter1Team.setRecord(fight.getFighter1().getRecord());
            teamInfo.setHome(fighter1Team);
        }

        if (fight.getFighter2() != null) {
            MatchDto.Team fighter2Team = new MatchDto.Team();
            fighter2Team.setId(fight.getFighter2().getFighterId());
            fighter2Team.setName(fight.getFighter2().getFighterName());
            fighter2Team.setLogo(fight.getFighter2().getFighterImage());
            fighter2Team.setCountry(fight.getFighter2().getCountry());
            fighter2Team.setWeightClass(fight.getFighter2().getWeightClass());
            fighter2Team.setRecord(fight.getFighter2().getRecord());
            teamInfo.setAway(fighter2Team);
        }

        dto.setTeams(teamInfo);

        // 점수 정보 (MMA는 점수가 없음)
        MatchDto.Score score = new MatchDto.Score();
        score.setHome(null);
        score.setAway(null);
        dto.setScore(score);

        // 경기 상세 정보
        MatchDto.MatchDetail detail = new MatchDto.MatchDetail();
        detail.setMatchDate(fight.getFightDate());
        detail.setVenue(fight.getVenue());
        detail.setStatus(fight.getStatus());
        detail.setEventName(fight.getEventName());

        // 승자 정보
        if (fight.getWinner() != null) {
            detail.setWinner(fight.getWinner().getFighterName());
        }
        detail.setMethod(fight.getMethod());
        detail.setRound(fight.getRound());

        dto.setDetail(detail);

        return dto;
    }
}