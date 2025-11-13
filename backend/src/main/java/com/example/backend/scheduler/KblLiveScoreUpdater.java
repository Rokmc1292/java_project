package com.example.backend.scheduler;

import com.example.backend.entity.Match;
import com.example.backend.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * KBL 실시간 점수 업데이터
 * 10초마다 오늘의 SCHEDULED/LIVE 경기를 크롤링하여 상태와 점수 업데이트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KblLiveScoreUpdater {

    private final MatchRepository matchRepository;
    private final KblCrawlerService crawlerService;

    /**
     * 서버 시작 시 LIVE 상태로 남아있는 경기들을 즉시 크롤링하여 업데이트
     * 로컬 개발 환경과 24시간 서버 모두에서 정상 작동하도록 개선
     * 서버가 중단되었다가 다시 시작되면 실제 경기 상태를 확인하여 동기화
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void checkStuckLiveMatchesOnStartup() {
        log.info("🔍 [KBL] 서버 시작 - LIVE 상태 경기 즉시 크롤링 시작");

        try {
            // KBL 리그의 LIVE 상태 경기 조회
            List<Match> liveMatches = matchRepository.findByStatus("LIVE");
            List<Match> leagueMatches = liveMatches.stream()
                    .filter(m -> m.getLeague().getLeagueId().equals(10L))
                    .toList();

            if (leagueMatches.isEmpty()) {
                log.info("✅ [KBL] LIVE 상태 경기 없음");
                return;
            }

            log.info("⚠️ [KBL] LIVE 상태 경기 {}개 발견 - 즉시 크롤링 시작", leagueMatches.size());

            // 즉시 크롤링 수행하여 실제 상태 확인
            WebDriver driver = null;
            try {
                driver = crawlerService.setupDriver();
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

                String baseUrl = "https://m.sports.naver.com/basketball/schedule/index?category=kbl";
                driver.get(baseUrl);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body")));
                Thread.sleep(2000);

                List<WebElement> matchElements = driver.findElements(By.cssSelector(".MatchBox_match_item__WiPhj"));

                if (matchElements.isEmpty()) {
                    log.warn("⚠️ [KBL] 웹에서 경기 요소를 찾지 못함 - 시간 기반 처리");
                    // 웹에서 찾지 못한 경우, 모든 LIVE 경기를 시간 기반으로 처리
                    LocalDateTime now = LocalDateTime.now();
                    for (Match match : leagueMatches) {
                        if (match.getMatchDate().plusHours(4).isBefore(now)) {
                            match.setStatus("FINISHED");
                            match.setUpdatedAt(now);
                            matchRepository.save(match);
                            log.info("🏁 [KBL] 과거 경기 종료 처리: {} vs {}",
                                    match.getHomeTeam().getTeamName(),
                                    match.getAwayTeam().getTeamName());
                        }
                    }
                    return;
                }

                log.info("📋 [KBL] 웹에서 {}개 경기 요소 발견", matchElements.size());

                int updatedCount = 0;
                int finishedCount = 0;
                LocalDateTime now = LocalDateTime.now();

                for (Match match : leagueMatches) {
                    WebElement matchElement = findMatchElement(matchElements, match);

                    if (matchElement != null) {
                        // 웹에서 찾았으면 실제 상태로 업데이트
                        boolean updated = updateMatchScore(matchElement, match);
                        if (updated) {
                            updatedCount++;
                            if ("FINISHED".equals(match.getStatus())) {
                                finishedCount++;
                            }
                        }
                    } else {
                        // 웹에서 찾지 못한 경기는 시간 기반으로 처리
                        if (match.getMatchDate().plusHours(4).isBefore(now)) {
                            match.setStatus("FINISHED");
                            match.setUpdatedAt(now);
                            matchRepository.save(match);
                            finishedCount++;
                            log.info("🏁 [KBL] 과거 경기 종료 처리: {} vs {}",
                                    match.getHomeTeam().getTeamName(),
                                    match.getAwayTeam().getTeamName());
                        }
                    }
                }

                log.info("✅ [KBL] 서버 시작 크롤링 완료 - 업데이트: {}개, 종료: {}개",
                        updatedCount, finishedCount);

            } finally {
                if (driver != null) {
                    try {
                        driver.quit();
                    } catch (Exception e) {
                        log.warn("⚠️ WebDriver 종료 중 오류: {}", e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            log.error("❌ [KBL] LIVE 상태 경기 크롤링 실패", e);
        }
    }

    /**
     * 10초마다 실시간 점수 업데이트
     * fixedDelay: 이전 실행이 끝난 후 10초 대기
     * initialDelay: 서버 시작 후 35초 뒤 첫 실행 (다른 리그들과 시간 차이)
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 35000)
    @Transactional
    public void updateLiveScores() {
        // KBL의 LIVE 경기 조회 (날짜 관계없이 LIVE 상태만 추적)
        List<Match> liveMatches = matchRepository.findByStatus("LIVE");
        List<Match> kblLiveMatches = liveMatches.stream()
                .filter(m -> m.getLeague().getLeagueId().equals(10L))
                .toList();

        if (kblLiveMatches.isEmpty()) {
            // LIVE 경기가 없으면 로그 출력 안함 (너무 많은 로그 방지)
            return;
        }

        log.info("🏀 [실시간 업데이트] KBL LIVE 경기 {}개 발견, 크롤링 시작", kblLiveMatches.size());

        WebDriver driver = null;

        try {
            driver = crawlerService.setupDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 네이버 스포츠 KBL 일정 페이지 (오늘 날짜)
            String baseUrl = "https://sports.news.naver.com/basketball/schedule/index?category=kbl";

            // 페이지 로드 재시도 로직 (최대 3번)
            List<WebElement> matchElements = null;
            int maxRetries = 3;

            for (int retry = 0; retry < maxRetries; retry++) {
                try {
                    driver.get(baseUrl);
                    log.debug("🌐 페이지 로딩 중... (시도 {}/{})", retry + 1, maxRetries);

                    // 페이지가 완전히 로드될 때까지 대기 (최대 10초)
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body")));
                    Thread.sleep(2000);  // 동적 콘텐츠 로딩 대기 (증가)

                    // 경기 목록 찾기 - 명시적 대기 사용
                    try {
                        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                            By.cssSelector(".MatchBox_match_item__WiPhj")));
                        matchElements = driver.findElements(By.cssSelector(".MatchBox_match_item__WiPhj"));

                        if (matchElements != null && !matchElements.isEmpty()) {
                            log.info("📋 웹에서 {}개 경기 요소 발견", matchElements.size());
                            break;  // 성공적으로 찾았으면 종료
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ CSS 셀렉터로 경기를 찾지 못함 (시도 {}/{})", retry + 1, maxRetries);
                    }

                    // 요소를 찾지 못한 경우 디버깅 정보 출력
                    if (matchElements == null || matchElements.isEmpty()) {
                        log.warn("⚠️ 경기 요소를 찾지 못함. 현재 URL: {}", driver.getCurrentUrl());

                        // 페이지 소스의 일부를 로깅 (디버깅용)
                        String pageSource = driver.getPageSource();
                        if (pageSource.length() > 500) {
                            log.debug("📄 페이지 소스 샘플: {}", pageSource.substring(0, 500));
                        }

                        // MatchBox 관련 요소가 있는지 확인
                        List<WebElement> anyMatchBox = driver.findElements(By.cssSelector("[class*='MatchBox']"));
                        log.debug("🔍 MatchBox 관련 요소 수: {}", anyMatchBox.size());

                        if (retry < maxRetries - 1) {
                            log.info("🔄 페이지 재로딩 시도...");
                            Thread.sleep(2000);  // 재시도 전 대기
                        }
                    }

                } catch (Exception e) {
                    log.warn("⚠️ 페이지 로딩 중 오류 (시도 {}/{}): {}", retry + 1, maxRetries, e.getMessage());
                    if (retry < maxRetries - 1) {
                        Thread.sleep(2000);  // 재시도 전 대기
                    }
                }
            }

            // 재시도 후에도 요소를 찾지 못한 경우
            if (matchElements == null || matchElements.isEmpty()) {
                log.error("❌ {}번 시도 후에도 경기 요소를 찾지 못했습니다. 크롤링을 건너뜁니다.", maxRetries);
                return;
            }

            int updatedCount = 0;
            int finishedCount = 0;
            int liveStartedCount = 0;
            int notFoundCount = 0;

            for (Match match : kblLiveMatches) {
                try {
                    String beforeStatus = match.getStatus();
                    String homeTeam = match.getHomeTeam().getTeamName();
                    String awayTeam = match.getAwayTeam().getTeamName();

                    // 웹에서 해당 경기 찾기
                    WebElement matchElement = findMatchElement(matchElements, match);

                    if (matchElement != null) {
                        // 점수 및 상태 업데이트
                        boolean updated = updateMatchScore(matchElement, match);

                        if (updated) {
                            updatedCount++;

                            // SCHEDULED -> LIVE 전환 확인
                            if ("SCHEDULED".equals(beforeStatus) && "LIVE".equals(match.getStatus())) {
                                liveStartedCount++;
                                log.info("🟢 경기 시작: {} vs {}", homeTeam, awayTeam);
                            }

                            // 경기가 종료되었는지 확인
                            if ("FINISHED".equals(match.getStatus())) {
                                finishedCount++;
                                log.info("🏁 경기 종료: {} {} - {} {}", homeTeam,
                                        match.getHomeScore(), match.getAwayScore(), awayTeam);
                            }
                        }
                    } else {
                        // 매칭 실패 - 웹에서 경기를 찾지 못함
                        notFoundCount++;

                        // 경기 시작 시간 + 4시간이 지났으면 자동으로 FINISHED 처리
                        LocalDateTime matchEndTime = match.getMatchDate().plusHours(4);
                        LocalDateTime now = LocalDateTime.now();

                        if (matchEndTime.isBefore(now) && "LIVE".equals(beforeStatus)) {
                            match.setStatus("FINISHED");
                            match.setUpdatedAt(now);
                            matchRepository.save(match);
                            finishedCount++;
                            log.info("🏁 과거 경기 종료 처리: {} {} - {} {} (웹에서 경기 찾지 못함, 마지막 점수 유지)",
                                    homeTeam,
                                    match.getHomeScore() != null ? match.getHomeScore() : 0,
                                    match.getAwayScore() != null ? match.getAwayScore() : 0,
                                    awayTeam);
                        } else {
                            log.warn("❌ 웹에서 경기를 찾지 못함: {} vs {} (상태: {}, 점수: {}-{})",
                                    homeTeam, awayTeam, beforeStatus,
                                    match.getHomeScore(), match.getAwayScore());
                        }
                    }

                } catch (Exception e) {
                    log.warn("⚠️ 경기 업데이트 실패: {} vs {}",
                            match.getHomeTeam().getTeamName(),
                            match.getAwayTeam().getTeamName(), e);
                }
            }

            if (updatedCount > 0 || notFoundCount > 0) {
                log.info("✅ [실시간 업데이트] 업데이트: {}개, 시작: {}개, 종료: {}개, 미발견: {}개",
                        updatedCount, liveStartedCount, finishedCount, notFoundCount);
            }

        } catch (Exception e) {
            log.error("❌ [실시간 업데이트] 실패", e);
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                    log.debug("🔌 WebDriver 종료 완료");
                } catch (Exception e) {
                    log.warn("⚠️ WebDriver 종료 중 오류: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 웹 페이지에서 DB의 경기와 일치하는 요소 찾기
     * 홈팀과 원정팀 이름으로 매칭
     */
    private WebElement findMatchElement(List<WebElement> matchElements, Match dbMatch) {
        String homeTeamName = dbMatch.getHomeTeam().getTeamName();
        String awayTeamName = dbMatch.getAwayTeam().getTeamName();

        log.debug("🔍 매칭 시도: DB[{} vs {}]", homeTeamName, awayTeamName);

        for (WebElement matchElement : matchElements) {
            try {
                List<WebElement> teamItems = matchElement.findElements(By.cssSelector(".MatchBoxHeadToHeadArea_team_item__9ZknX"));

                if (teamItems.size() >= 2) {
                    String webHomeTeam = teamItems.get(0).findElement(By.cssSelector(".MatchBoxHeadToHeadArea_team__l2ZxP")).getText();
                    String webAwayTeam = teamItems.get(1).findElement(By.cssSelector(".MatchBoxHeadToHeadArea_team__l2ZxP")).getText();

                    log.debug("   웹[{} vs {}]", webHomeTeam, webAwayTeam);

                    // 팀 이름이 일치하면 해당 경기
                    if (homeTeamName.equals(webHomeTeam) && awayTeamName.equals(webAwayTeam)) {
                        log.debug("   ✅ 매칭 성공!");
                        return matchElement;
                    }
                }
            } catch (Exception e) {
                // 요소 찾기 실패시 다음 경기로
                continue;
            }
        }

        log.debug("   ❌ 매칭 실패: 웹에서 해당 경기를 찾지 못함");
        return null;
    }

    /**
     * 경기 점수 및 상태 업데이트
     * @return 업데이트 성공 여부
     */
    private boolean updateMatchScore(WebElement matchElement, Match match) {
        try {
            // 경기 상태 확인
            String statusText = matchElement.findElement(By.cssSelector(".MatchBox_status__xU6\\+d")).getText().strip();
            String newStatus = crawlerService.convertStatus(statusText);

            // ⚠️ 중요: 상태 전환 보호 로직
            String currentStatus = match.getStatus();

            // 1. FINISHED 경기는 상태를 변경하지 않음
            if ("FINISHED".equals(currentStatus) && !"FINISHED".equals(newStatus)) {
                log.warn("⚠️ FINISHED 경기 보호: {} vs {} (크롤링 상태: {} → 무시)",
                        match.getHomeTeam().getTeamName(),
                        match.getAwayTeam().getTeamName(),
                        newStatus);
                return false;
            }

            // 2. LIVE 경기는 SCHEDULED로 역전되지 않음 (FINISHED로만 전환 가능)
            if ("LIVE".equals(currentStatus) && "SCHEDULED".equals(newStatus)) {
                log.warn("⚠️ LIVE 경기 보호: {} vs {} (SCHEDULED로 역전 차단)",
                        match.getHomeTeam().getTeamName(),
                        match.getAwayTeam().getTeamName());
                return false;
            }

            // 점수 추출
            List<WebElement> scores = matchElement.findElements(By.cssSelector(".MatchBoxHeadToHeadArea_score__TChmp"));

            Integer newHomeScore = null;
            Integer newAwayScore = null;

            // 점수가 있으면 파싱 (LIVE 또는 FINISHED 경기)
            if (scores.size() >= 2) {
                try {
                    String homeScoreText = scores.get(0).getText().trim();
                    String awayScoreText = scores.get(1).getText().trim();

                    if (!homeScoreText.isEmpty() && !awayScoreText.isEmpty()) {
                        newHomeScore = Integer.parseInt(homeScoreText);
                        newAwayScore = Integer.parseInt(awayScoreText);
                    }
                } catch (NumberFormatException e) {
                    log.warn("⚠️ 점수 파싱 실패: {}", e.getMessage());
                }
            }

            // 점수나 상태가 변경되었는지 확인
            Integer currentHomeScore = match.getHomeScore();
            Integer currentAwayScore = match.getAwayScore();

            boolean scoreChanged = false;
            if (newHomeScore != null && newAwayScore != null) {
                scoreChanged = (currentHomeScore == null || !currentHomeScore.equals(newHomeScore))
                        || (currentAwayScore == null || !currentAwayScore.equals(newAwayScore));
            }

            boolean statusChanged = !newStatus.equals(match.getStatus());

            // 상태가 변경되거나 점수가 변경된 경우 업데이트
            if (scoreChanged || statusChanged) {
                // 상태 업데이트
                match.setStatus(newStatus);

                // 점수 업데이트 (있는 경우에만)
                if (newHomeScore != null && newAwayScore != null) {
                    match.setHomeScore(newHomeScore);
                    match.setAwayScore(newAwayScore);
                }

                match.setUpdatedAt(LocalDateTime.now());
                matchRepository.save(match);

                if (newHomeScore != null && newAwayScore != null) {
                    log.info("🔄 점수 업데이트: {} {} - {} {} (상태: {})",
                            match.getHomeTeam().getTeamName(),
                            newHomeScore,
                            newAwayScore,
                            match.getAwayTeam().getTeamName(),
                            newStatus);
                } else {
                    log.info("🔄 상태 업데이트: {} vs {} (상태: {} → {})",
                            match.getHomeTeam().getTeamName(),
                            match.getAwayTeam().getTeamName(),
                            currentStatus,
                            newStatus);
                }

                return true;
            }

        } catch (Exception e) {
            log.warn("⚠️ 점수 업데이트 실패", e);
        }

        return false;
    }

    /**
     * 경기 시작 시간 기준으로 SCHEDULED -> LIVE 상태 변경
     * 5분마다 실행하여 경기 시작 확인
     * KBL만 처리
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 135000)
    @Transactional
    public void checkMatchStartTime() {
        LocalDateTime now = LocalDateTime.now();

        // SCHEDULED 상태이면서 KBL(league_id = 10)인 경기 조회
        List<Match> scheduledMatches = matchRepository.findByStatus("SCHEDULED");

        int updatedCount = 0;

        for (Match match : scheduledMatches) {
            // KBL 경기만 처리
            if (match.getLeague().getLeagueId().equals(10L)) {
                // 경기 시작 시간이 현재 시간보다 이전이면 LIVE로 변경
                if (match.getMatchDate().isBefore(now)) {
                    match.setStatus("LIVE");
                    matchRepository.save(match);
                    updatedCount++;

                    log.info("🟢 경기 시작: {} vs {} ({})",
                            match.getHomeTeam().getTeamName(),
                            match.getAwayTeam().getTeamName(),
                            match.getMatchDate());
                }
            }
        }

        if (updatedCount > 0) {
            log.info("✅ {}개 KBL 경기가 LIVE 상태로 변경됨", updatedCount);
        }
    }
}
