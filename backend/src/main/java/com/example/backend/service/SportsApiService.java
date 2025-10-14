package com.example.backend.service;

import com.example.backend.config.ApiSportsConfig;
import com.example.backend.dto.MatchDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 스포츠 API 통합 서비스
 * API-SPORTS.IO 직접 API 사용
 */
@Service
@RequiredArgsConstructor
public class SportsApiService {

    private final RestTemplate restTemplate;
    private final ApiSportsConfig config;
    private final ObjectMapper objectMapper;

    // ==================== 공통 유틸리티 메서드 ====================

    /**
     * API-FOOTBALL 헤더 생성
     */
    private HttpHeaders createFootballHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-apisports-key", config.getFootballApiKey());
        return headers;
    }

    /**
     * API-BASKETBALL 헤더 생성
     */
    private HttpHeaders createBasketballHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-apisports-key", config.getBasketballApiKey());
        return headers;
    }

    /**
     * API-BASEBALL 헤더 생성
     */
    private HttpHeaders createBaseballHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-apisports-key", config.getBaseballApiKey());
        return headers;
    }

    /**
     * API-MMA 헤더 생성
     */
    private HttpHeaders createMmaHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-apisports-key", config.getMmaApiKey());
        return headers;
    }

    /**
     * PandaScore 헤더 생성
     */
    private HttpHeaders createPandaScoreHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + config.getPandascoreToken());
        return headers;
    }

    /**
     * JSON에서 텍스트 값 안전하게 추출
     */
    private String getTextValue(JsonNode node, String... paths) {
        JsonNode current = node;
        for (String path : paths) {
            if (current == null || current.isNull()) return null;
            current = current.get(path);
        }
        return (current != null && !current.isNull()) ? current.asText() : null;
    }

    /**
     * JSON에서 정수 값 안전하게 추출
     */
    private Integer getIntValue(JsonNode node, String... paths) {
        JsonNode current = node;
        for (String path : paths) {
            if (current == null || current.isNull()) return null;
            current = current.get(path);
        }
        return (current != null && !current.isNull()) ? current.asInt() : null;
    }

    /**
     * JSON에서 Long 값 안전하게 추출
     */
    private Long getLongValue(JsonNode node, String... paths) {
        JsonNode current = node;
        for (String path : paths) {
            if (current == null || current.isNull()) return null;
            current = current.get(path);
        }
        return (current != null && !current.isNull()) ? current.asLong() : null;
    }

    // ==================== 축구 API (EPL, 분데스리가, 라리가, 세리에A만) ====================

    /**
     * 축구 경기 일정 조회
     * 주요 4개 리그만: EPL(39), 분데스리가(78), 라리가(140), 세리에A(135)
     * 무료/유료 플랜 자동 대응
     */
    public List<MatchDto> getFootballFixtures(String date) {
        System.out.println("=== 축구 API 호출 시작 ===");
        System.out.println("날짜: " + date);
        System.out.println("시즌: " + ApiSportsConfig.getFootballSeason() +
                (ApiSportsConfig.isPremiumPlan() ? " (현재 시즌)" : " (무료 플랜 제한)"));

        List<MatchDto> allMatches = new ArrayList<>();

        for (int leagueId : ApiSportsConfig.FOOTBALL_LEAGUE_IDS) {
            try {
                String url = String.format("%s/fixtures?date=%s&league=%d&season=%d",
                        config.getFootballUrl(), date, leagueId, ApiSportsConfig.getFootballSeason());

                System.out.println("축구 리그 " + leagueId + " 요청: " + url);

                HttpEntity<String> entity = new HttpEntity<>(createFootballHeaders());
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                System.out.println("응답 상태: " + response.getStatusCode());

                if (response.getStatusCode() == HttpStatus.OK) {
                    List<MatchDto> matches = parseFootballResponse(response.getBody());
                    allMatches.addAll(matches);
                    System.out.println("리그 " + leagueId + ": " + matches.size() + "개 경기");
                }
            } catch (Exception e) {
                System.err.println("❌ 축구 리그 " + leagueId + " 조회 오류: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("✅ 총 축구 경기: " + allMatches.size() + "개");
        return allMatches;
    }

    /**
     * 축구 API 응답 파싱
     */
    private List<MatchDto> parseFootballResponse(String responseBody) {
        List<MatchDto> matches = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode responseNode = root.get("response");

            if (responseNode != null && responseNode.isArray()) {
                for (JsonNode matchNode : responseNode) {
                    JsonNode fixture = matchNode.get("fixture");
                    JsonNode league = matchNode.get("league");
                    JsonNode teams = matchNode.get("teams");
                    JsonNode goals = matchNode.get("goals");

                    // 리그 정보
                    MatchDto.LeagueInfo leagueInfo = MatchDto.LeagueInfo.builder()
                            .name(getTextValue(league, "name"))
                            .country(getTextValue(league, "country"))
                            .logo(getTextValue(league, "logo"))
                            .build();

                    // 팀 정보
                    MatchDto.Team homeTeam = MatchDto.Team.builder()
                            .name(getTextValue(teams, "home", "name"))
                            .logo(getTextValue(teams, "home", "logo"))
                            .build();

                    MatchDto.Team awayTeam = MatchDto.Team.builder()
                            .name(getTextValue(teams, "away", "name"))
                            .logo(getTextValue(teams, "away", "logo"))
                            .build();

                    MatchDto.TeamInfo teamInfo = MatchDto.TeamInfo.builder()
                            .home(homeTeam)
                            .away(awayTeam)
                            .build();

                    // 경기 상태
                    MatchDto.Status status = MatchDto.Status.builder()
                            .shortStatus(getTextValue(fixture, "status", "short"))
                            .longStatus(getTextValue(fixture, "status", "long"))
                            .build();

                    // 경기 시간
                    String dateStr = getTextValue(fixture, "date");
                    LocalDateTime matchDate = null;
                    if (dateStr != null) {
                        try {
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr);
                            matchDate = zonedDateTime.toLocalDateTime();
                        } catch (Exception e) {
                            System.err.println("날짜 파싱 오류: " + e.getMessage());
                        }
                    }

                    // 경기장 정보
                    MatchDto.Venue venue = MatchDto.Venue.builder()
                            .name(getTextValue(fixture, "venue", "name"))
                            .city(getTextValue(fixture, "venue", "city"))
                            .build();

                    MatchDto.MatchStatus fixtureStatus = MatchDto.MatchStatus.builder()
                            .id(getLongValue(fixture, "id"))
                            .date(matchDate)
                            .status(status)
                            .venue(venue)
                            .build();

                    // 점수
                    MatchDto.Score score = MatchDto.Score.builder()
                            .home(getIntValue(goals, "home"))
                            .away(getIntValue(goals, "away"))
                            .build();

                    MatchDto match = MatchDto.builder()
                            .matchId(getLongValue(fixture, "id"))
                            .sport("FOOTBALL")
                            .league(leagueInfo)
                            .teams(teamInfo)
                            .fixture(fixtureStatus)
                            .goals(score)
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

    /**
     * 실시간 축구 경기 조회 (4개 주요 리그만)
     */
    public List<MatchDto> getLiveFootballMatches() {
        System.out.println("=== 실시간 축구 경기 조회 ===");
        System.out.println("시즌: " + ApiSportsConfig.getFootballSeason() +
                (ApiSportsConfig.isPremiumPlan() ? " (현재 시즌)" : " (무료 플랜 제한)"));

        List<MatchDto> allMatches = new ArrayList<>();

        for (int leagueId : ApiSportsConfig.FOOTBALL_LEAGUE_IDS) {
            try {
                String url = String.format("%s/fixtures?live=%d&season=%d",
                        config.getFootballUrl(), leagueId, ApiSportsConfig.getFootballSeason());

                HttpEntity<String> entity = new HttpEntity<>(createFootballHeaders());
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    List<MatchDto> matches = parseFootballResponse(response.getBody());
                    allMatches.addAll(matches);
                }
            } catch (Exception e) {
                System.err.println("❌ 실시간 축구 리그 " + leagueId + " 조회 오류: " + e.getMessage());
            }
        }

        System.out.println("✅ 실시간 축구 경기: " + allMatches.size() + "개");
        return allMatches;
    }

    // ==================== 농구 API (NBA만) ====================

    /**
     * 농구 경기 일정 조회 (NBA만)
     * 무료/유료 플랜 자동 대응
     */
    public List<MatchDto> getBasketballGames(String date) {
        System.out.println("=== 농구 API 호출 시작 (NBA만) ===");
        System.out.println("날짜: " + date);
        System.out.println("시즌: " + ApiSportsConfig.getBasketballSeason() +
                (ApiSportsConfig.isPremiumPlan() ? " (현재 시즌)" : " (무료 플랜 제한)"));

        try {
            String url = String.format("%s/games?date=%s&league=%d&season=%s",
                    config.getBasketballUrl(), date, ApiSportsConfig.NBA_LEAGUE_ID,
                    ApiSportsConfig.getBasketballSeason());

            System.out.println("요청 URL: " + url);

            HttpEntity<String> entity = new HttpEntity<>(createBasketballHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            System.out.println("응답 상태: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                List<MatchDto> matches = parseBasketballResponse(response.getBody());
                System.out.println("✅ NBA 경기: " + matches.size() + "개");
                return matches;
            }
        } catch (Exception e) {
            System.err.println("❌ 농구 일정 조회 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * 농구 API 응답 파싱
     */
    private List<MatchDto> parseBasketballResponse(String responseBody) {
        List<MatchDto> matches = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode responseNode = root.get("response");

            if (responseNode != null && responseNode.isArray()) {
                for (JsonNode matchNode : responseNode) {
                    // 리그 정보
                    MatchDto.LeagueInfo league = MatchDto.LeagueInfo.builder()
                            .name(getTextValue(matchNode, "league", "name"))
                            .country(getTextValue(matchNode, "country", "name"))
                            .logo(getTextValue(matchNode, "league", "logo"))
                            .build();

                    // 팀 정보
                    MatchDto.Team homeTeam = MatchDto.Team.builder()
                            .name(getTextValue(matchNode, "teams", "home", "name"))
                            .logo(getTextValue(matchNode, "teams", "home", "logo"))
                            .build();

                    MatchDto.Team awayTeam = MatchDto.Team.builder()
                            .name(getTextValue(matchNode, "teams", "away", "name"))
                            .logo(getTextValue(matchNode, "teams", "away", "logo"))
                            .build();

                    MatchDto.TeamInfo teams = MatchDto.TeamInfo.builder()
                            .home(homeTeam)
                            .away(awayTeam)
                            .build();

                    // 경기 상태
                    MatchDto.Status status = MatchDto.Status.builder()
                            .shortStatus(getTextValue(matchNode, "status", "short"))
                            .longStatus(getTextValue(matchNode, "status", "long"))
                            .build();

                    // 경기 시간
                    String dateStr = getTextValue(matchNode, "date");
                    LocalDateTime matchDate = null;
                    if (dateStr != null) {
                        try {
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr);
                            matchDate = zonedDateTime.toLocalDateTime();
                        } catch (Exception e) {
                            System.err.println("날짜 파싱 오류: " + e.getMessage());
                        }
                    }

                    MatchDto.MatchStatus fixture = MatchDto.MatchStatus.builder()
                            .id(getLongValue(matchNode, "id"))
                            .date(matchDate)
                            .status(status)
                            .build();

                    // 점수
                    MatchDto.Score goals = MatchDto.Score.builder()
                            .home(getIntValue(matchNode, "scores", "home", "total"))
                            .away(getIntValue(matchNode, "scores", "away", "total"))
                            .build();

                    MatchDto match = MatchDto.builder()
                            .matchId(getLongValue(matchNode, "id"))
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

    /**
     * 실시간 농구 경기 조회 (NBA만)
     */
    public List<MatchDto> getLiveBasketballGames() {
        System.out.println("=== 실시간 농구 경기 조회 (NBA만) ===");
        System.out.println("시즌: " + ApiSportsConfig.getBasketballSeason() +
                (ApiSportsConfig.isPremiumPlan() ? " (현재 시즌)" : " (무료 플랜 제한)"));

        try {
            String url = String.format("%s/games?live=%d&season=%s",
                    config.getBasketballUrl(), ApiSportsConfig.NBA_LEAGUE_ID,
                    ApiSportsConfig.getBasketballSeason());

            HttpEntity<String> entity = new HttpEntity<>(createBasketballHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                List<MatchDto> matches = parseBasketballResponse(response.getBody());
                System.out.println("✅ 실시간 NBA 경기: " + matches.size() + "개");
                return matches;
            }
        } catch (Exception e) {
            System.err.println("❌ 실시간 농구 조회 오류: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    // ==================== 야구 API (MLB만) ====================

    /**
     * 야구 경기 일정 조회 (MLB만)
     * 무료/유료 플랜 자동 대응
     */
    public List<MatchDto> getBaseballGames(String date) {
        System.out.println("=== 야구 API 호출 시작 (MLB만) ===");
        System.out.println("날짜: " + date);
        System.out.println("시즌: " + ApiSportsConfig.getBaseballSeason() +
                (ApiSportsConfig.isPremiumPlan() ? " (현재 시즌)" : " (무료 플랜 제한)"));

        try {
            String url = String.format("%s/games?date=%s&league=%d&season=%d",
                    config.getBaseballUrl(), date, ApiSportsConfig.MLB_LEAGUE_ID,
                    ApiSportsConfig.getBaseballSeason());

            System.out.println("요청 URL: " + url);

            HttpEntity<String> entity = new HttpEntity<>(createBaseballHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            System.out.println("응답 상태: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                List<MatchDto> matches = parseBaseballResponse(response.getBody());
                System.out.println("✅ MLB 경기: " + matches.size() + "개");
                return matches;
            }
        } catch (Exception e) {
            System.err.println("❌ 야구 일정 조회 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * 야구 API 응답 파싱
     */
    private List<MatchDto> parseBaseballResponse(String responseBody) {
        List<MatchDto> matches = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode responseNode = root.get("response");

            if (responseNode != null && responseNode.isArray()) {
                for (JsonNode matchNode : responseNode) {
                    // 리그 정보
                    MatchDto.LeagueInfo league = MatchDto.LeagueInfo.builder()
                            .name(getTextValue(matchNode, "league", "name"))
                            .country(getTextValue(matchNode, "country", "name"))
                            .logo(getTextValue(matchNode, "league", "logo"))
                            .build();

                    // 팀 정보
                    MatchDto.Team homeTeam = MatchDto.Team.builder()
                            .name(getTextValue(matchNode, "teams", "home", "name"))
                            .logo(getTextValue(matchNode, "teams", "home", "logo"))
                            .build();

                    MatchDto.Team awayTeam = MatchDto.Team.builder()
                            .name(getTextValue(matchNode, "teams", "away", "name"))
                            .logo(getTextValue(matchNode, "teams", "away", "logo"))
                            .build();

                    MatchDto.TeamInfo teams = MatchDto.TeamInfo.builder()
                            .home(homeTeam)
                            .away(awayTeam)
                            .build();

                    // 경기 상태
                    MatchDto.Status status = MatchDto.Status.builder()
                            .shortStatus(getTextValue(matchNode, "status", "short"))
                            .longStatus(getTextValue(matchNode, "status", "long"))
                            .build();

                    // 경기 시간
                    String dateStr = getTextValue(matchNode, "date");
                    LocalDateTime matchDate = null;
                    if (dateStr != null) {
                        try {
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr);
                            matchDate = zonedDateTime.toLocalDateTime();
                        } catch (Exception e) {
                            System.err.println("날짜 파싱 오류: " + e.getMessage());
                        }
                    }

                    MatchDto.MatchStatus fixture = MatchDto.MatchStatus.builder()
                            .id(getLongValue(matchNode, "id"))
                            .date(matchDate)
                            .status(status)
                            .build();

                    // 점수
                    MatchDto.Score goals = MatchDto.Score.builder()
                            .home(getIntValue(matchNode, "scores", "home", "total"))
                            .away(getIntValue(matchNode, "scores", "away", "total"))
                            .build();

                    MatchDto match = MatchDto.builder()
                            .matchId(getLongValue(matchNode, "id"))
                            .sport("BASEBALL")
                            .league(league)
                            .teams(teams)
                            .fixture(fixture)
                            .goals(goals)
                            .build();

                    matches.add(match);
                }
            }
        } catch (Exception e) {
            System.err.println("야구 응답 파싱 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return matches;
    }

    /**
     * 실시간 야구 경기 조회 (MLB만)
     */
    public List<MatchDto> getLiveBaseballGames() {
        System.out.println("=== 실시간 야구 경기 조회 (MLB만) ===");
        System.out.println("시즌: " + ApiSportsConfig.getBaseballSeason() +
                (ApiSportsConfig.isPremiumPlan() ? " (현재 시즌)" : " (무료 플랜 제한)"));

        try {
            String url = String.format("%s/games?live=%d&season=%d",
                    config.getBasketballUrl(), ApiSportsConfig.MLB_LEAGUE_ID,
                    ApiSportsConfig.getBaseballSeason());

            HttpEntity<String> entity = new HttpEntity<>(createBaseballHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                List<MatchDto> matches = parseBaseballResponse(response.getBody());
                System.out.println("✅ 실시간 MLB 경기: " + matches.size() + "개");
                return matches;
            }
        } catch (Exception e) {
            System.err.println("❌ 실시간 야구 조회 오류: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    // ==================== MMA API (UFC만) ====================

    /**
     * MMA 경기 일정 조회 (UFC만)
     */
    public List<MatchDto> getMmaFights(String date) {
        System.out.println("=== MMA API 호출 시작 (UFC만) ===");
        System.out.println("날짜: " + date);
        System.out.println("API URL: " + config.getMmaUrl());
        System.out.println("API KEY: " + (config.getMmaApiKey() != null ? "설정됨" : "NULL"));

        try {
            String url = String.format("%s/fights?date=%s&league=%d",
                    config.getMmaUrl(), date, ApiSportsConfig.UFC_LEAGUE_ID);

            System.out.println("요청 URL: " + url);

            HttpEntity<String> entity = new HttpEntity<>(createMmaHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            System.out.println("응답 상태: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                List<MatchDto> matches = parseMmaResponse(response.getBody());
                System.out.println("✅ UFC 경기: " + matches.size() + "개");
                return matches;
            }
        } catch (Exception e) {
            System.err.println("❌ MMA 일정 조회 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * MMA API 응답 파싱
     */
    private List<MatchDto> parseMmaResponse(String responseBody) {
        List<MatchDto> matches = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode responseNode = root.get("response");

            if (responseNode != null && responseNode.isArray()) {
                for (JsonNode matchNode : responseNode) {
                    // 리그 정보
                    MatchDto.LeagueInfo league = MatchDto.LeagueInfo.builder()
                            .name(getTextValue(matchNode, "league", "name"))
                            .country(getTextValue(matchNode, "country", "name"))
                            .logo(getTextValue(matchNode, "league", "logo"))
                            .build();

                    // 파이터 정보
                    MatchDto.Team homeFighter = MatchDto.Team.builder()
                            .name(getTextValue(matchNode, "fighters", "home", "name"))
                            .logo(getTextValue(matchNode, "fighters", "home", "image"))
                            .build();

                    MatchDto.Team awayFighter = MatchDto.Team.builder()
                            .name(getTextValue(matchNode, "fighters", "away", "name"))
                            .logo(getTextValue(matchNode, "fighters", "away", "image"))
                            .build();

                    MatchDto.TeamInfo teams = MatchDto.TeamInfo.builder()
                            .home(homeFighter)
                            .away(awayFighter)
                            .build();

                    // 경기 상태
                    MatchDto.Status status = MatchDto.Status.builder()
                            .shortStatus(getTextValue(matchNode, "status", "short"))
                            .longStatus(getTextValue(matchNode, "status", "long"))
                            .build();

                    // 경기 시간
                    String dateStr = getTextValue(matchNode, "date");
                    LocalDateTime matchDate = null;
                    if (dateStr != null) {
                        try {
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr);
                            matchDate = zonedDateTime.toLocalDateTime();
                        } catch (Exception e) {
                            System.err.println("날짜 파싱 오류: " + e.getMessage());
                        }
                    }

                    MatchDto.MatchStatus fixture = MatchDto.MatchStatus.builder()
                            .id(getLongValue(matchNode, "id"))
                            .date(matchDate)
                            .status(status)
                            .build();

                    // 점수 (MMA는 승자로 표시)
                    MatchDto.Score goals = MatchDto.Score.builder()
                            .home(null)
                            .away(null)
                            .build();

                    MatchDto match = MatchDto.builder()
                            .matchId(getLongValue(matchNode, "id"))
                            .sport("MMA")
                            .league(league)
                            .teams(teams)
                            .fixture(fixture)
                            .goals(goals)
                            .build();

                    matches.add(match);
                }
            }
        } catch (Exception e) {
            System.err.println("MMA 응답 파싱 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return matches;
    }

/**
 * 실시간 MMA 경기 조회 (UFC만)
 **/

    public List<MatchDto> getLiveMmaFights() {
        System.out.println("=== 실시간 MMA 경기 조회 (UFC만) ===");

        try {
            String url = String.format("%s/fights?live=%d",
                    config.getMmaUrl(), ApiSportsConfig.UFC_LEAGUE_ID);

            HttpEntity<String> entity = new HttpEntity<>(createMmaHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                List<MatchDto> matches = parseMmaResponse(response.getBody());
                System.out.println("✅ 실시간 UFC 경기: " + matches.size() + "개");
                return matches;
            }
        } catch (Exception e) {
            System.err.println("❌ 실시간 MMA 조회 오류: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    // ==================== e스포츠 API (PandaScore) ====================

    /**
     * e스포츠 경기 일정 조회
     * 무료 플랜: 제한 없음
     */
    public List<MatchDto> getEsportsMatches(String date) {
        System.out.println("=== e스포츠 API 호출 시작 ===");
        System.out.println("날짜: " + date);
        System.out.println("API URL: " + config.getPandascoreUrl());
        System.out.println("API TOKEN: " + (config.getPandascoreToken() != null ? "설정됨" : "NULL"));

        try {
            String url = String.format("%s/matches?filter[begin_at]=%sT00:00:00Z..%sT23:59:59Z&sort=begin_at",
                    config.getPandascoreUrl(), date, date);

            System.out.println("요청 URL: " + url);

            HttpEntity<String> entity = new HttpEntity<>(createPandaScoreHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            System.out.println("응답 상태: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                List<MatchDto> matches = parseEsportsResponse(response.getBody());
                System.out.println("✅ e스포츠 경기: " + matches.size() + "개");
                return matches;
            }
        } catch (Exception e) {
            System.err.println("❌ e스포츠 일정 조회 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * e스포츠 API 응답 파싱
     */
    private List<MatchDto> parseEsportsResponse(String responseBody) {
        List<MatchDto> matches = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            if (root.isArray()) {
                for (JsonNode matchNode : root) {
                    // 리그 정보
                    String leagueName = getTextValue(matchNode, "league", "name");
                    String videogameName = getTextValue(matchNode, "videogame", "name");

                    MatchDto.LeagueInfo league = MatchDto.LeagueInfo.builder()
                            .name(leagueName != null ? leagueName : videogameName)
                            .logo(getTextValue(matchNode, "league", "image_url"))
                            .build();

                    // 팀 정보
                    JsonNode opponents = matchNode.get("opponents");
                    MatchDto.Team homeTeam = null;
                    MatchDto.Team awayTeam = null;

                    if (opponents != null && opponents.isArray() && opponents.size() >= 2) {
                        homeTeam = MatchDto.Team.builder()
                                .name(getTextValue(opponents.get(0), "opponent", "name"))
                                .logo(getTextValue(opponents.get(0), "opponent", "image_url"))
                                .build();

                        awayTeam = MatchDto.Team.builder()
                                .name(getTextValue(opponents.get(1), "opponent", "name"))
                                .logo(getTextValue(opponents.get(1), "opponent", "image_url"))
                                .build();
                    }

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
                    if (dateStr != null) {
                        try {
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr);
                            matchDate = zonedDateTime.toLocalDateTime();
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
        } catch (Exception e) {
            System.err.println("e스포츠 응답 파싱 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return matches;
    }

    /**
     * 실시간 e스포츠 경기 조회
     */
    public List<MatchDto> getLiveEsportsMatches() {
        System.out.println("=== 실시간 e스포츠 경기 조회 ===");

        try {
            String url = String.format("%s/matches/running", config.getPandascoreUrl());

            HttpEntity<String> entity = new HttpEntity<>(createPandaScoreHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                List<MatchDto> matches = parseEsportsResponse(response.getBody());
                System.out.println("✅ 실시간 e스포츠 경기: " + matches.size() + "개");
                return matches;
            }
        } catch (Exception e) {
            System.err.println("❌ 실시간 e스포츠 조회 오류: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    // ==================== 통합 메서드 ====================

    /**
     * 모든 종목 경기 일정 조회
     */
    public List<MatchDto> getAllFixtures(String date, String sport) {
        List<MatchDto> allMatches = new ArrayList<>();

        System.out.println("=== 전체 경기 조회 시작 ===");
        System.out.println("날짜: " + date + ", 종목: " + sport);

        if ("all".equalsIgnoreCase(sport) || "football".equalsIgnoreCase(sport)) {
            allMatches.addAll(getFootballFixtures(date));
        }

        if ("all".equalsIgnoreCase(sport) || "basketball".equalsIgnoreCase(sport)) {
            allMatches.addAll(getBasketballGames(date));
        }

        if ("all".equalsIgnoreCase(sport) || "baseball".equalsIgnoreCase(sport)) {
            allMatches.addAll(getBaseballGames(date));
        }

        if ("all".equalsIgnoreCase(sport) || "mma".equalsIgnoreCase(sport)) {
            allMatches.addAll(getMmaFights(date));
        }

        if ("all".equalsIgnoreCase(sport) || "esports".equalsIgnoreCase(sport)) {
            allMatches.addAll(getEsportsMatches(date));
        }

        System.out.println("=== 전체 경기 조회 완료: " + allMatches.size() + "개 ===");
        return allMatches;
    }

    /**
     * 모든 종목 실시간 경기 조회
     */
    public List<MatchDto> getAllLiveMatches() {
        List<MatchDto> allMatches = new ArrayList<>();

        System.out.println("=== 전체 실시간 경기 조회 시작 ===");

        allMatches.addAll(getLiveFootballMatches());
        allMatches.addAll(getLiveBasketballGames());
        allMatches.addAll(getLiveBaseballGames());
        allMatches.addAll(getLiveMmaFights());
        allMatches.addAll(getLiveEsportsMatches());

        System.out.println("=== 전체 실시간 경기 조회 완료: " + allMatches.size() + "개 ===");
        return allMatches;
    }
}