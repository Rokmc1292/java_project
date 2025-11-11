package com.example.backend.scheduler;

import com.example.backend.entity.Match;
import com.example.backend.entity.Prediction;
import com.example.backend.repository.MatchRepository;
import com.example.backend.repository.PredictionRepository;
import com.example.backend.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 예측 판정 스케줄러
 * - 5분마다 종료된 경기의 예측을 자동 판정
 * - DB에서 경기 점수 업데이트 시 자동으로 감지하여 판정
 *
 * 파일 위치: backend/src/main/java/com/example/backend/scheduler/PredictionScheduler.java
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PredictionScheduler {

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final PredictionService predictionService;

    /**
     * 5분마다 실행되는 자동 판정 스케줄러
     * - 상태가 'FINISHED'이고 아직 판정되지 않은 경기를 찾아서 판정
     */
    @Scheduled(fixedDelay = 300000) // 5분 (300,000ms)
    public void autoJudgePredictions() {
        log.info("=== 예측 자동 판정 스케줄러 시작 ===");

        try {
            // FINISHED 상태인 모든 경기 조회
            List<Match> finishedMatches = matchRepository.findByStatus("FINISHED");

            if (finishedMatches.isEmpty()) {
                log.info("판정할 FINISHED 경기가 없습니다.");
                return;
            }

            log.info("FINISHED 경기 총 개수: {}", finishedMatches.size());

            // 각 경기마다 판정되지 않은 예측이 있는지 확인
            int judgedCount = 0;
            for (Match match : finishedMatches) {
                // 해당 경기의 판정되지 않은 예측 개수 확인
                long unjudgedCount = predictionRepository.countByMatchAndIsCorrectIsNull(match);

                if (unjudgedCount > 0) {
                    try {
                        log.info("경기 판정 시작: matchId={}, homeScore={}, awayScore={}, 판정할 예측 수={}",
                                match.getMatchId(), match.getHomeScore(), match.getAwayScore(), unjudgedCount);

                        predictionService.judgePredictions(match.getMatchId());

                        log.info("✅ 경기 판정 완료: matchId={}, 판정된 예측 수={}", match.getMatchId(), unjudgedCount);
                        judgedCount++;
                    } catch (Exception e) {
                        log.error("❌ 경기 판정 실패: matchId={}, error={}", match.getMatchId(), e.getMessage(), e);
                    }
                }
            }

            if (judgedCount > 0) {
                log.info("=== 예측 자동 판정 완료: {}개 경기 판정 ===", judgedCount);
            } else {
                log.info("판정할 예측이 없습니다. (모든 경기가 이미 판정됨)");
            }
        } catch (Exception e) {
            log.error("❌ 예측 자동 판정 스케줄러 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * 서버 시작 후 1분 뒤 한 번 실행 (테스트용)
     * - 서버 재시작 시 바로 판정 필요한 경기가 있는지 확인
     */
    @Scheduled(initialDelay = 60000, fixedDelay = Long.MAX_VALUE)
    public void initialJudgment() {
        log.info("=== 초기 예측 판정 실행 (서버 시작 후) ===");
        autoJudgePredictions();
    }
}