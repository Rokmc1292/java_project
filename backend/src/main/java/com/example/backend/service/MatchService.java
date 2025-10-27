package com.example.backend.service;

import com.example.backend.dto.MatchDto;
import com.example.backend.entity.*;
import com.example.backend.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    @Transactional(readOnly = true)
    public List<MatchDto> getMatchesByDate(LocalDate date, String sportType) {
        LocalDateTime dateTime = date.atStartOfDay();

        if (sportType == null || sportType.isEmpty() || sportType.equals("ALL")) {
            List<Match> matches = matchRepository.findByMatchDate(dateTime);
            return convertMatchesToDto(matches);
        } else {
            String sport = sportType.toUpperCase();
            List<Match> matches = matchRepository.findByMatchDateAndSport(dateTime, sport);
            return convertMatchesToDto(matches);
        }
    }

    @Transactional(readOnly = true)
    public List<MatchDto> getMatchesByDateRange(LocalDate startDate, LocalDate endDate, String sportType) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        if (sportType == null || sportType.isEmpty() || sportType.equals("ALL")) {
            List<Match> matches = matchRepository.findByDateRange(startDateTime, endDateTime);
            return convertMatchesToDto(matches);
        } else {
            String sport = sportType.toUpperCase();
            List<Match> matches = matchRepository.findByMatchDateBetweenAndSport(startDateTime, endDateTime, sport);
            return convertMatchesToDto(matches);
        }
    }

    private List<MatchDto> convertMatchesToDto(List<Match> matches) {
        return matches.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public MatchDto convertToDto(Match match) {
        MatchDto dto = new MatchDto();
        dto.setMatchId(match.getMatchId());

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
}