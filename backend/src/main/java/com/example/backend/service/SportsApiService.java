package com.example.backend.service;

import com.example.backend.config.*;
import com.example.backend.dto.MatchDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 외부 스포츠 API 호출 서비스
 * 승인 불필요한 무료 API 사용
 * - 축구: football-data.org
 * - 농구: balldontlie.io (API 키 불필요)
 * - 야구: MLB Stats API (API 키 불필요)
 * - e스포츠: PandaScore
 * - UFC: The Odds API
 */
@Service
@RequiredArgsConstructor
public class SportsApiService {

    private final FootballDataConfig footballDataConfig;
    private final BasketballConfig basketballConfig;
    private final BaseballConfig baseballConfig;
    private final PandaScoreConfig pandaScoreConfig;
    private final TheOddsConfig theOddsConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ==================== 축구 API (football-data.org) ====================

    /**
     * Football-Data.org HTTP 헤더 생성
     */
    private HttpHeaders createFootballDataHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", footballDataConfig.getKey());
        return headers;
    }

    /**
     * 축구 경기 일정 조회
     */
    public List<MatchDto> getFootballFixtures(String date) {
        try {
            // Football-Data.org는 dateFrom과 dateTo 사용
            String url = String.format("%s/matches?dateFrom=%s&dateTo=%s",
                    footballDataConfig.getUrl(), date, date);

            HttpEntity<String> entity = new HttpEntity<>(createFootballDataHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return parseFootballDataResponse(response.getBody());
        } catch (Exception e) {
            System.err.println("축구 일정 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 실시간 축구 경기 조회
     */
    public List<MatchDto> getLiveFootballMatches() {
        try {
            String url = String.format("%s/matches?status=LIVE",
                    footballDataConfig.getUrl());

            HttpEntity<String> entity = new HttpEntity<>(createFootballDataHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return parseFootballDataResponse(response.getBody());
        } catch (Exception e) {
            System.err.println("실시간 축구 경기 조회 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Football-Data.org API 응답 파싱
     */
    private List<MatchDto> parseFootballDataResponse(String responseBody) {
        List<MatchDto> matches = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode matchesArray = root.get("matches");

            if (matchesArray != null && matchesArray.isArray()) {
                for (JsonNode matchNode : matchesArray) {
                    // 리그 정보
                    MatchDto.LeagueInfo league = MatchDto.LeagueInfo.builder()
                            .name(getTextValue(matchNode, "competition", "name"))
                            .country(getTextValue(matchNode, "area", "name"))
                            .logo(getTextValue(matchNode, "competition", "emblem"))
                            .build();

                    // 팀 정보
                    MatchDto.Team homeTeam = MatchDto.Team.builder()
                            .name(getTextValue(matchNode, "homeTeam", "name"))
                            .logo(getTextValue(matchNode, "homeTeam", "crest"))
                            .build();

                    MatchDto.Team awayTeam = MatchDto.Team.builder()
                            .name(getTextValue(matchNode, "awayTeam", "name"))
                            .logo(getTextValue(matchNode, "awayTeam", "crest"))
                            .build();

                    MatchDto.TeamInfo teams = MatchDto.TeamInfo.builder()
                            .home(homeTeam)
                            .away(awayTeam)
                            .build();

                    // 경기 상태
                    String statusStr = getTextValue(matchNode, "status");
                    MatchDto.Status status = MatchDto.Status.builder()
                            .shortStatus(statusStr)
                            .longStatus(statusStr)
                            .build();

                    // 경기 시간
                    String dateStr = getTextValue(matchNode, "utcDate");
                    LocalDateTime matchDate = null;
                    if (dateStr != null && !dateStr.isEmpty()) {
                        try {
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr);
                            matchDate = zonedDateTime.toLocalDateTime();
                        } catch (Exception e) {
                            System.err.println("날짜 파싱 오류: " + e.getMessage());
                        }
                    }

                    // 경기장 정보
                    MatchDto.Venue venue = MatchDto.Venue.builder()
                            .name(getTextValue(matchNode, "venue"))
                            .city(null)
                            .build();

                    MatchDto.MatchStatus fixture = MatchDto.MatchStatus.builder()
                            .id(getLongValue(matchNode, "id"))
                            .date(matchDate)
                            .status(status)
                            .venue(venue)
                            .build();

                    // 점수 정보
                    Integer homeScore = getIntValue(matchNode, "score", "fullTime", "home");
                    Integer awayScore = getIntValue(matchNode, "score", "fullTime", "away");

                    MatchDto.Score goals = MatchDto.Score.builder()
                            .home(homeScore)
                            .away(awayScore)
                            .build();

                    MatchDto match = MatchDto.builder()
                            .matchId(getLongValue(matchNode, "id"))
                            .sport("FOOTBALL")
                            .league(league)
                            .teams(teams)
                            .fixture(fixture)
                            .goals(goals)
                            .build();

                    matches.add(match);
                }
            }
        } catch (Exception e) {
            System.err.println("축구 응답 파싱 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return matches;
    }

    // ==================== 농구 API (balldontlie.io) ====================

    /**
     * 농구 경기 일정 조회
     * API 키 불필요!
     */
    public List<MatchDto> getBasketballGames(String date) {
        try {
            String url = String.format("%s/games?start_date=%s&end_date=%s",
                    basketballConfig.getUrl(), date, date);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            return parseBasketballResponse(response.getBody());
        } catch (Exception e) {
            System.err.println("농구 일정 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 실시간 농구 경기 조회
     */
    public List<MatchDto> getLiveBasketballGames() {
        try {
            // 오늘 날짜의 모든 경기 조회 (실시간 필터링은 클라이언트에서)
            String today = LocalDateTime.now().toLocalDate().toString();
            String url = String.format("%s/games?start_date=%s&end_date=%s",
                    basketballConfig.getUrl(), today, today);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            return parseBasketballResponse(response.getBody());
        } catch (Exception e) {
            System.err.println("실시간 농구 경기 조회 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Ball Don't Lie API 응답 파싱
     */
    private List<MatchDto> parseBasketballResponse(String responseBody) {
        List<MatchDto> matches = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode dataArray = root.get("data");

            if (dataArray != null && dataArray.isArray()) {
                for (JsonNode gameNode : dataArray) {
                    // 리그 정보
                    MatchDto.LeagueInfo league = MatchDto.LeagueInfo.builder()
                            .name("NBA")
                            .country("USA")
                            .build();

                    // 팀 정보
                    MatchDto.Team homeTeam = MatchDto.Team.builder()
                            .name(getTextValue(gameNode, "home_team", "full_name"))
                            .build();

                    MatchDto.Team awayTeam = MatchDto.Team.builder()
                            .name(getTextValue(gameNode, "visitor_team", "full_name"))
                            .build();

                    MatchDto.TeamInfo teams = MatchDto.TeamInfo.builder()
                            .home(homeTeam)
                            .away(awayTeam)
                            .build();

                    // 경기 상태
                    String statusStr = getTextValue(gameNode, "status");
                    MatchDto.Status status = MatchDto.Status.builder()
                            .shortStatus(statusStr)
                            .longStatus(statusStr)
                            .build();

                    // 경기 시간
                    String dateStr = getTextValue(gameNode, "date");
                    LocalDateTime matchDate = null;
                    if (dateStr != null && !dateStr.isEmpty()) {
                        try {
                            matchDate = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
                        } catch (Exception e) {
                            System.err.println("날짜 파싱 오류: " + e.getMessage());
                        }
                    }

                    MatchDto.MatchStatus fixture = MatchDto.MatchStatus.builder()
                            .id(getLongValue(gameNode, "id"))
                            .date(matchDate)
                            .status(status)
                            .build();

                    // 점수 정보
                    Integer homeScore = getIntValue(gameNode, "home_team_score");
                    Integer awayScore = getIntValue(gameNode, "visitor_team_score");

                    MatchDto.Score goals = MatchDto.Score.builder()
                            .home(homeScore)
                            .away(awayScore)
                            .build();

                    MatchDto match = MatchDto.builder()
                            .matchId(getLongValue(gameNode, "id"))
                            .sport("BASKETBALL")
                            .league(league)
                            .teams(teams)
                            .fixture(fixture)
                            .goals(goals)
                            .build();

                    matches.add(match);
                }
            }
        } catch (Exception e) {
            System.err.println("농구 응답 파싱 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return matches;
    }

    // ==================== 야구 API (MLB Stats API) ====================

    /**
     * 야구 경기 일정 조회
     * API 키 불필요!
     */
    public List<MatchDto> getBaseballGames(String date) {
        try {
            String url = String.format("%s/schedule?sportId=1&date=%s",
                    baseballConfig.getUrl(), date);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            return parseBaseballResponse(response.getBody());
        } catch (Exception e) {
            System.err.println("야구 일정 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 실시간 야구 경기 조회
     */
    public List<MatchDto> getLiveBaseballGames() {
        try {
            String today = LocalDateTime.now().toLocalDate().toString();
            String url = String.format("%s/schedule?sportId=1&date=%s",
                    baseballConfig.getUrl(), today);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            return parseBaseballResponse(response.getBody());
        } catch (Exception e) {
            System.err.println("실시간 야구 경기 조회 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * MLB Stats API 응답 파싱
     */
    private List<MatchDto> parseBaseballResponse(String responseBody) {
        List<MatchDto> matches = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode datesArray = root.get("dates");

            if (datesArray != null && datesArray.isArray()) {
                for (JsonNode dateNode : datesArray) {
                    JsonNode gamesArray = dateNode.get("games");

                    if (gamesArray != null && gamesArray.isArray()) {
                        for (JsonNode gameNode : gamesArray) {
                            // 리그 정보
                            MatchDto.LeagueInfo league = MatchDto.LeagueInfo.builder()
                                    .name("MLB")
                                    .country("USA")
                                    .build();

                            // 팀 정보
                            MatchDto.Team homeTeam = MatchDto.Team.builder()
                                    .name(getTextValue(gameNode, "teams", "home", "team", "name"))
                                    .build();

                            MatchDto.Team awayTeam = MatchDto.Team.builder()
                                    .name(getTextValue(gameNode, "teams", "away", "team", "name"))
                                    .build();

                            MatchDto.TeamInfo teams = MatchDto.TeamInfo.builder()
                                    .home(homeTeam)
                                    .away(awayTeam)
                                    .build();

                            // 경기 상태
                            String statusCode = getTextValue(gameNode, "status", "statusCode");
                            String statusStr = getTextValue(gameNode, "status", "detailedState");
                            MatchDto.Status status = MatchDto.Status.builder()
                                    .shortStatus(statusCode)
                                    .longStatus(statusStr)
                                    .build();

                            // 경기 시간
                            String dateStr = getTextValue(gameNode, "gameDate");
                            LocalDateTime matchDate = null;
                            if (dateStr != null && !dateStr.isEmpty()) {
                                try {
                                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr);
                                    matchDate = zonedDateTime.toLocalDateTime();
                                } catch (Exception e) {
                                    System.err.println("날짜 파싱 오류: " + e.getMessage());
                                }
                            }

                            // 경기장 정보
                            MatchDto.Venue venue = MatchDto.Venue.builder()
                                    .name(getTextValue(gameNode, "venue", "name"))
                                    .city(null)
                                    .build();

                            MatchDto.MatchStatus fixture = MatchDto.MatchStatus.builder()
                                    .id(getLongValue(gameNode, "gamePk"))
                                    .date(matchDate)
                                    .status(status)
                                    .venue(venue)
                                    .build();

                            // 점수 정보
                            Integer homeScore = getIntValue(gameNode, "teams", "home", "score");
                            Integer awayScore = getIntValue(gameNode, "teams", "away", "score");

                            MatchDto.Score goals = MatchDto.Score.builder()
                                    .home(homeScore)
                                    .away(awayScore)
                                    .build();

                            MatchDto match = MatchDto.builder()
                                    .matchId(getLongValue(gameNode, "gamePk"))
                                    .sport("BASEBALL")
                                    .league(league)
                                    .teams(teams)
                                    .fixture(fixture)
                                    .goals(goals)
                                    .build();

                            matches.add(match);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("야구 응답 파싱 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return matches;
    }

    // ==================== e스포츠 API (PandaScore) ====================

    /**
     * PandaScore HTTP 헤더 생성
     */
    private HttpHeaders createPandaScoreHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + pandaScoreConfig.getToken());
        return headers;
    }

    /**
     * e스포츠 경기 일정 조회
     */
    public List<MatchDto> getEsportsMatches(String date) {
        try {
            String url = String.format("%s/matches?filter[begin_at]=%sT00:00:00Z..%sT23:59:59Z&sort=begin_at",
                    pandaScoreConfig.getUrl(), date, date);

            HttpEntity<String> entity = new HttpEntity<>(createPandaScoreHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return parseEsportsResponse(response.getBody());
        } catch (Exception e) {
            System.err.println("e스포츠 일정 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 실시간 e스포츠 경기 조회
     */
    public List<MatchDto> getLiveEsportsMatches() {
        try {
            String url = String.format("%s/matches/running", pandaScoreConfig.getUrl());

            HttpEntity<String> entity = new HttpEntity<>(createPandaScoreHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return parseEsportsResponse(response.getBody());
        } catch (Exception e) {
            System.err.println("실시간 e스포츠 경기 조회 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * PandaScore API 응답 파싱
     */
    private List<MatchDto> parseEsportsResponse(String responseBody) {
        List<MatchDto> matches = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            if (root.isArray()) {
                for (JsonNode matchNode : root) {
                    // 리그 정보
                    MatchDto.LeagueInfo league = MatchDto.LeagueInfo.builder()
                            .name(getTextValue(matchNode, "league", "name"))
                            .logo(getTextValue(matchNode, "league", "image_url"))
                            .build();

                    // 팀 정보
                    JsonNode opponentsNode = matchNode.get("opponents");
                    if (opponentsNode != null && opponentsNode.isArray() && opponentsNode.size() >= 2) {
                        MatchDto.Team homeTeam = MatchDto.Team.builder()
                                .name(getTextValue(opponentsNode.get(0), "opponent", "name"))
                                .logo(getTextValue(opponentsNode.get(0), "opponent", "image_url"))
                                .build();

                        MatchDto.Team awayTeam = MatchDto.Team.builder()
                                .name(getTextValue(opponentsNode.get(1), "opponent", "name"))
                                .logo(getTextValue(opponentsNode.get(1), "opponent", "image_url"))
                                .build();

                        MatchDto.TeamInfo teams = MatchDto.TeamInfo.builder()
                                .home(homeTeam)
                                .away(awayTeam)
                                .build();

                        // 경기 상태
                        String statusStr = getTextValue(matchNode, "status");
                        MatchDto.Status status = MatchDto.Status.builder()
                                .shortStatus(statusStr)
                                .longStatus(statusStr)
                                .build();

                        // 경기 시간
                        String dateStr = getTextValue(matchNode, "begin_at");
                        LocalDateTime matchDate = null;
                        if (dateStr != null && !dateStr.isEmpty()) {
                            try {
                                matchDate = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
                            } catch (Exception e) {
                                System.err.println("날짜 파싱 오류: " + e.getMessage());
                            }
                        }

                        MatchDto.MatchStatus fixture = MatchDto.MatchStatus.builder()
                                .id(getLongValue(matchNode, "id"))
                                .date(matchDate)
                                .status(status)
                                .build();

                        // 점수 정보
                        JsonNode results = matchNode.get("results");
                        Integer homeScore = null;
                        Integer awayScore = null;

                        if (results != null && results.isArray() && results.size() >= 2) {
                            homeScore = getIntValue(results.get(0), "score");
                            awayScore = getIntValue(results.get(1), "score");
                        }

                        MatchDto.Score goals = MatchDto.Score.builder()
                                .home(homeScore)
                                .away(awayScore)
                                .build();

                        // 게임 정보 추가
                        String gameName = getTextValue(matchNode, "videogame", "name");
                        if (league.getName() == null && gameName != null) {
                            league.setName(gameName);
                        }

                        MatchDto match = MatchDto.builder()
                                .matchId(getLongValue(matchNode, "id"))
                                .sport("ESPORTS")
                                .league(league)
                                .teams(teams)
                                .fixture(fixture)
                                .goals(goals)
                                .build();

                        matches.add(match);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("e스포츠 응답 파싱 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return matches;
    }

    // ==================== UFC API (The Odds API) ====================

    /**
     * UFC 이벤트 조회
     */
    public List<MatchDto> getUfcEvents() {
        try {
            String url = String.format("%s/sports/mma_ufc/events?apiKey=%s",
                    theOddsConfig.getUrl(), theOddsConfig.getKey());

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            return parseUfcResponse(response.getBody());
        } catch (Exception e) {
            System.err.println("UFC 이벤트 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 날짜 필터링된 UFC 이벤트 조회
     */
    public List<MatchDto> getUfcEventsByDate(String date) {
        List<MatchDto> allEvents = getUfcEvents();

        return allEvents.stream()
                .filter(match -> {
                    if (match.getFixture() == null || match.getFixture().getDate() == null) {
                        return false;
                    }
                    LocalDateTime matchDate = match.getFixture().getDate();
                    String matchDateStr = matchDate.toLocalDate().toString();
                    return matchDateStr.equals(date);
                })
                .toList();
    }

    /**
     * UFC API 응답 파싱
     */
    private List<MatchDto> parseUfcResponse(String responseBody) {
        List<MatchDto> matches = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            if (root.isArray()) {
                for (JsonNode eventNode : root) {
                    String eventId = getTextValue(eventNode, "id");
                    String commenceTimeStr = getTextValue(eventNode, "commence_time");

                    JsonNode teamsNode = eventNode.get("home_team");
                    String homeTeam = teamsNode != null ? teamsNode.asText() : null;

                    teamsNode = eventNode.get("away_team");
                    String awayTeam = teamsNode != null ? teamsNode.asText() : null;

                    MatchDto.LeagueInfo league = MatchDto.LeagueInfo.builder()
                            .name("UFC")
                            .country("International")
                            .build();

                    MatchDto.Team home = MatchDto.Team.builder()
                            .name(homeTeam != null ? homeTeam : "TBD")
                            .build();

                    MatchDto.Team away = MatchDto.Team.builder()
                            .name(awayTeam != null ? awayTeam : "TBD")
                            .build();

                    MatchDto.TeamInfo teams = MatchDto.TeamInfo.builder()
                            .home(home)
                            .away(away)
                            .build();

                    LocalDateTime matchDate = null;
                    if (commenceTimeStr != null && !commenceTimeStr.isEmpty()) {
                        try {
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(commenceTimeStr);
                            matchDate = zonedDateTime.toLocalDateTime();
                        } catch (Exception e) {
                            System.err.println("날짜 파싱 오류: " + e.getMessage());
                        }
                    }

                    MatchDto.Status status = MatchDto.Status.builder()
                            .shortStatus("SCH")
                            .longStatus("Scheduled")
                            .build();

                    MatchDto.MatchStatus fixture = MatchDto.MatchStatus.builder()
                            .id(eventId != null ? (long) eventId.hashCode() : null)
                            .date(matchDate)
                            .status(status)
                            .build();

                    MatchDto.Score goals = MatchDto.Score.builder()
                            .home(null)
                            .away(null)
                            .build();

                    MatchDto match = MatchDto.builder()
                            .matchId(eventId != null ? (long) eventId.hashCode() : null)
                            .sport("UFC")
                            .league(league)
                            .teams(teams)
                            .fixture(fixture)
                            .goals(goals)
                            .build();

                    matches.add(match);
                }
            }
        } catch (Exception e) {
            System.err.println("UFC 응답 파싱 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return matches;
    }

    // ==================== 통합 함수 ====================

    /**
     * 모든 종목의 경기 일정 조회 (5개 종목)
     */
    public List<MatchDto> getAllFixtures(String date, String sport) {
        List<MatchDto> allMatches = new ArrayList<>();

        if ("all".equalsIgnoreCase(sport) || "football".equalsIgnoreCase(sport)) {
            allMatches.addAll(getFootballFixtures(date));
        }

        if ("all".equalsIgnoreCase(sport) || "basketball".equalsIgnoreCase(sport)) {
            allMatches.addAll(getBasketballGames(date));
        }

        if ("all".equalsIgnoreCase(sport) || "baseball".equalsIgnoreCase(sport)) {
            allMatches.addAll(getBaseballGames(date));
        }

        if ("all".equalsIgnoreCase(sport) || "esports".equalsIgnoreCase(sport)) {
            allMatches.addAll(getEsportsMatches(date));
        }

        if ("all".equalsIgnoreCase(sport) || "ufc".equalsIgnoreCase(sport) || "mma".equalsIgnoreCase(sport)) {
            allMatches.addAll(getUfcEventsByDate(date));
        }

        return allMatches;
    }

    /**
     * 모든 종목의 실시간 경기 조회
     */
    public List<MatchDto> getAllLiveMatches() {
        List<MatchDto> allMatches = new ArrayList<>();

        allMatches.addAll(getLiveFootballMatches());
        allMatches.addAll(getLiveBasketballGames());
        allMatches.addAll(getLiveBaseballGames());
        allMatches.addAll(getLiveEsportsMatches());

        return allMatches;
    }

    // ==================== 유틸리티 메서드 ====================

    private String getTextValue(JsonNode node, String... paths) {
        JsonNode current = node;
        for (String path : paths) {
            if (current == null || current.isNull()) {
                return null;
            }
            current = current.get(path);
        }
        return (current != null && !current.isNull()) ? current.asText() : null;
    }

    private Long getLongValue(JsonNode node, String... paths) {
        JsonNode current = node;
        for (String path : paths) {
            if (current == null || current.isNull()) {
                return null;
            }
            current = current.get(path);
        }
        return (current != null && !current.isNull()) ? current.asLong() : null;
    }

    private Integer getIntValue(JsonNode node, String... paths) {
        JsonNode current = node;
        for (String path : paths) {
            if (current == null || current.isNull()) {
                return null;
            }
            current = current.get(path);
        }
        return (current != null && !current.isNull()) ? current.asInt() : null;
    }
}