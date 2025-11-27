package com.example.backend.scheduler;

import com.example.backend.entity.Match;
import com.example.backend.entity.MmaFight;
import com.example.backend.entity.Prediction;
import com.example.backend.repository.MatchRepository;
import com.example.backend.repository.MmaFightRepository;
import com.example.backend.repository.MmaPredictionRepository;
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
    private final MmaFightRepository mmaFightRepository;
    private final MmaPredictionRepository mmaPredictionRepository;
    private final PredictionService predictionService;

    /**
     * 5분마다 실행되는 자동 판정 스케줄러
     * - 상태가 'FINISHED'이고 아직 판정되지 않은 경기를 찾아서 판정
     * - 일반 경기(축구, 농구 등)와 MMA 경기 모두 판정
     */
    @Scheduled(fixedDelay = 300000) // 5분 (300,000ms)
    public void autoJudgePredictions() {
        log.info("=== 예측 자동 판정 스케줄러 시작 ===");

        try {
            // 1. 일반 경기 판정
            int regularJudgedCount = judgeRegularMatches();

            // 2. MMA 경기 판정
            int mmaJudgedCount = judgeMmaFights();

            int totalJudgedCount = regularJudgedCount + mmaJudgedCount;

            if (totalJudgedCount > 0) {
                log.info("=== 예측 자동 판정 완료: {}개 경기 판정 (일반: {}, MMA: {}) ===",
                        totalJudgedCount, regularJudgedCount, mmaJudgedCount);
            } else {
                log.info("판정할 예측이 없습니다. (모든 경기가 이미 판정됨)");
            }
        } catch (Exception e) {
            log.error("❌ 예측 자동 판정 스케줄러 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * 일반 경기(축구, 농구 등) 예측 판정
     */
    private int judgeRegularMatches() {
        try {
            // FINISHED 상태인 모든 경기 조회
            List<Match> finishedMatches = matchRepository.findByStatus("FINISHED");

            if (finishedMatches.isEmpty()) {
                log.info("판정할 FINISHED 일반 경기가 없습니다.");
                return 0;
            }

            log.info("FINISHED 일반 경기 총 개수: {}", finishedMatches.size());

            // 각 경기마다 판정되지 않은 예측이 있는지 확인
            int judgedCount = 0;
            for (Match match : finishedMatches) {
                // 해당 경기의 판정되지 않은 예측 개수 확인
                long unjudgedCount = predictionRepository.countByMatchAndIsCorrectIsNull(match);

                if (unjudgedCount > 0) {
                    try {
                        log.info("[일반 경기] 판정 시작: matchId={}, homeScore={}, awayScore={}, 판정할 예측 수={}",
                                match.getMatchId(), match.getHomeScore(), match.getAwayScore(), unjudgedCount);

                        predictionService.judgePredictions(match.getMatchId());

                        log.info("✅ [일반 경기] 판정 완료: matchId={}, 판정된 예측 수={}", match.getMatchId(), unjudgedCount);
                        judgedCount++;
                    } catch (Exception e) {
                        log.error("❌ [일반 경기] 판정 실패: matchId={}, error={}", match.getMatchId(), e.getMessage(), e);
                    }
                }
            }

            return judgedCount;
        } catch (Exception e) {
            log.error("❌ 일반 경기 판정 중 오류: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * MMA 경기 예측 판정
     */
    private int judgeMmaFights() {
        try {
            // FINISHED 상태인 모든 MMA 경기 조회
            List<MmaFight> finishedFights = mmaFightRepository.findByStatus("FINISHED");

            if (finishedFights.isEmpty()) {
                log.info("판정할 FINISHED MMA 경기가 없습니다.");
                return 0;
            }

            log.info("FINISHED MMA 경기 총 개수: {}", finishedFights.size());

            // 각 경기마다 판정되지 않은 예측이 있는지 확인
            int judgedCount = 0;
            for (MmaFight fight : finishedFights) {
                // 해당 경기의 판정되지 않은 예측 개수 확인
                long unjudgedCount = mmaPredictionRepository.countByFightAndIsCorrectIsNull(fight);

                if (unjudgedCount > 0) {
                    try {
                        String winnerName = fight.getWinner() != null ? fight.getWinner().getFighterName() : "없음";
                        log.info("[MMA 경기] 판정 시작: fightId={}, winner={}, 판정할 예측 수={}",
                                fight.getFightId(), winnerName, unjudgedCount);

                        predictionService.judgeMmaPredictions(fight.getFightId());

                        log.info("✅ [MMA 경기] 판정 완료: fightId={}, 판정된 예측 수={}", fight.getFightId(), unjudgedCount);
                        judgedCount++;
                    } catch (Exception e) {
                        log.error("❌ [MMA 경기] 판정 실패: fightId={}, error={}", fight.getFightId(), e.getMessage(), e);
                    }
                }
            }

            return judgedCount;
        } catch (Exception e) {
            log.error("❌ MMA 경기 판정 중 오류: {}", e.getMessage(), e);
            return 0;
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