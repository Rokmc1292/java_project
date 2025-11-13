package com.example.backend.service;

import com.example.backend.dto.MatchDto;
import com.example.backend.dto.PredictionDto;
import com.example.backend.dto.PredictionRequest;
import com.example.backend.dto.PredictionStatisticsDto;
import com.example.backend.dto.PredictionRankingDto;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 승부예측 Service
 * - 예측 생성, 조회, 투표, 통계, 랭킹 등 모든 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final PredictionStatisticsRepository predictionStatisticsRepository;
    private final PredictionVoteRepository predictionVoteRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final MmaPredictionRepository mmaPredictionRepository;
    private final MmaPredictionStatisticsRepository mmaPredictionStatisticsRepository;
    private final MmaFightRepository mmaFightRepository;
    // NotificationService 제거

    // ========== 예측 경기 목록 (D-7 경기) ==========

    /**
     * 예측 가능한 경기 목록 조회 (7일 이내 경기)
     * 현재 시간부터 7일 후까지의 경기를 조회
     * MMA 경기 포함
     */
    @Transactional(readOnly = true)
    public Page<MatchDto> getPredictableMatches(String sportName, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);

        if (sportName != null && sportName.equalsIgnoreCase("MMA")) {
            // MMA 경기만 조회
            List<MmaFight> mmaFights = mmaFightRepository.findByDateRange(now, sevenDaysLater);
            List<MatchDto> mmaMatches = mmaFights.stream()
                    .filter(fight -> fight.getStatus().equals("SCHEDULED"))
                    .map(this::convertMmaFightToMatchDto)
                    .collect(Collectors.toList());

            // List를 Page로 변환
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), mmaMatches.size());
            List<MatchDto> pageContent = mmaMatches.subList(start, end);
            return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, mmaMatches.size());
        } else if (sportName == null || sportName.equals("ALL")) {
            // 전체 경기 조회 (Match + MmaFight)
            Page<Match> matches = matchRepository.findPredictableMatches(now, sevenDaysLater, pageable);
            List<MatchDto> allMatches = matches.stream()
                    .map(this::convertMatchToDto)
                    .collect(Collectors.toList());

            List<MmaFight> mmaFights = mmaFightRepository.findByDateRange(now, sevenDaysLater);
            List<MatchDto> mmaMatches = mmaFights.stream()
                    .filter(fight -> fight.getStatus().equals("SCHEDULED"))
                    .map(this::convertMmaFightToMatchDto)
                    .collect(Collectors.toList());
            allMatches.addAll(mmaMatches);

            // 날짜순 정렬
            allMatches.sort((a, b) -> a.getDetail().getMatchDate().compareTo(b.getDetail().getMatchDate()));

            return new org.springframework.data.domain.PageImpl<>(allMatches, pageable, allMatches.size());
        } else {
            // 다른 종목 경기만 조회
            Page<Match> matches = matchRepository.findPredictableMatchesBySport(sportName, now, sevenDaysLater, pageable);
            return matches.map(this::convertMatchToDto);
        }
    }

    /**
     * 사용자가 이미 예측한 경기인지 확인 (일반 경기 + MMA 경기 지원)
     */
    @Transactional(readOnly = true)
    public boolean hasUserPredicted(Long matchId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 먼저 Match 테이블 확인
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isPresent()) {
            return predictionRepository.findByMatchAndUser(matchOpt.get(), user).isPresent();
        }

        // Match에 없으면 MmaFight 테이블 확인
        Optional<MmaFight> fightOpt = mmaFightRepository.findById(matchId);
        if (fightOpt.isPresent()) {
            return mmaPredictionRepository.findByFightAndUser(fightOpt.get(), user).isPresent();
        }

        throw new RuntimeException("경기를 찾을 수 없습니다.");
    }

    /**
     * 사용자가 이미 예측한 MMA 경기인지 확인
     */
    @Transactional(readOnly = true)
    public boolean hasUserPredictedMma(Long fightId, String username) {
        MmaFight fight = mmaFightRepository.findById(fightId)
                .orElseThrow(() -> new RuntimeException("MMA 경기를 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return mmaPredictionRepository.findByFightAndUser(fight, user).isPresent();
    }

    // ========== 예측 참여 ==========

    /**
     * 승부예측 생성 (일반 경기 + MMA 경기 통합)
     */
    @Transactional
    public PredictionDto createPrediction(String username, PredictionRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 먼저 Match 테이블 확인
        Optional<Match> matchOpt = matchRepository.findById(request.getMatchId());
        if (matchOpt.isPresent()) {
            Match match = matchOpt.get();

            // 경기 시작 시간 확인
            if (match.getMatchDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("이미 시작된 경기는 예측할 수 없습니다.");
            }

            // 농구 경기는 무승부가 없음 (연장전으로 승부 결정)
            if (isBasketballMatch(match) && "DRAW".equals(request.getPredictedResult())) {
                throw new RuntimeException("농구 경기는 무승부가 없습니다. HOME 또는 AWAY를 선택해주세요.");
            }

            // 중복 예측 방지
            predictionRepository.findByMatchAndUser(match, user)
                    .ifPresent(p -> {
                        throw new RuntimeException("이미 이 경기에 대한 예측을 하셨습니다.");
                    });

            // 예측 생성
            Prediction prediction = new Prediction();
            prediction.setMatch(match);
            prediction.setUser(user);
            prediction.setPredictedResult(request.getPredictedResult());
            prediction.setComment(request.getComment());

            Prediction savedPrediction = predictionRepository.save(prediction);

            // 예측 통계 업데이트
            updatePredictionStatistics(match, request.getPredictedResult());

            return convertToDto(savedPrediction);
        }

        // Match에 없으면 MmaFight 테이블 확인
        Optional<MmaFight> fightOpt = mmaFightRepository.findById(request.getMatchId());
        if (fightOpt.isPresent()) {
            MmaFight fight = fightOpt.get();

            // 경기 시작 시간 확인
            if (fight.getFightDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("이미 시작된 경기는 예측할 수 없습니다.");
            }

            // 중복 예측 방지
            mmaPredictionRepository.findByFightAndUser(fight, user)
                    .ifPresent(p -> {
                        throw new RuntimeException("이미 이 경기에 대한 예측을 하셨습니다.");
                    });

            // MMA 예측 생성
            MmaPrediction prediction = new MmaPrediction();
            prediction.setFight(fight);
            prediction.setUser(user);
            prediction.setPredictedResult(request.getPredictedResult());
            prediction.setComment(request.getComment());

            MmaPrediction savedPrediction = mmaPredictionRepository.save(prediction);

            // 예측 통계 업데이트
            updateMmaPredictionStatistics(fight, request.getPredictedResult());

            return convertMmaPredictionToDto(savedPrediction);
        }

        throw new RuntimeException("경기를 찾을 수 없습니다.");
    }

    /**
     * 예측 통계 업데이트
     */
    private void updatePredictionStatistics(Match match, String predictedResult) {
        // 농구 경기는 무승부가 없음
        if (isBasketballMatch(match) && "DRAW".equals(predictedResult)) {
            throw new RuntimeException("농구 경기는 무승부 예측이 불가능합니다.");
        }

        PredictionStatistics stats = predictionStatisticsRepository.findByMatch(match)
                .orElseGet(() -> {
                    PredictionStatistics newStats = new PredictionStatistics();
                    newStats.setMatch(match);
                    return newStats;
                });

        switch (predictedResult) {
            case "HOME":
                stats.setHomeVotes(stats.getHomeVotes() + 1);
                break;
            case "DRAW":
                stats.setDrawVotes(stats.getDrawVotes() + 1);
                break;
            case "AWAY":
                stats.setAwayVotes(stats.getAwayVotes() + 1);
                break;
        }
        stats.setTotalVotes(stats.getTotalVotes() + 1);

        predictionStatisticsRepository.save(stats);
    }

    /**
     * MMA 승부예측 생성
     */
    @Transactional
    public PredictionDto createMmaPrediction(String username, PredictionRequest request) {
        MmaFight fight = mmaFightRepository.findById(request.getMatchId())
                .orElseThrow(() -> new RuntimeException("MMA 경기를 찾을 수 없습니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 경기 시작 시간 확인
        if (fight.getFightDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("이미 시작된 경기는 예측할 수 없습니다.");
        }

        // 중복 예측 방지
        mmaPredictionRepository.findByFightAndUser(fight, user)
                .ifPresent(p -> {
                    throw new RuntimeException("이미 이 경기에 대한 예측을 하셨습니다.");
                });

        // 예측 생성
        MmaPrediction prediction = new MmaPrediction();
        prediction.setFight(fight);
        prediction.setUser(user);
        prediction.setPredictedResult(request.getPredictedResult());
        prediction.setComment(request.getComment());

        MmaPrediction savedPrediction = mmaPredictionRepository.save(prediction);

        // 예측 통계 업데이트
        updateMmaPredictionStatistics(fight, request.getPredictedResult());

        return convertMmaPredictionToDto(savedPrediction);
    }

    /**
     * MMA 예측 통계 업데이트
     */
    private void updateMmaPredictionStatistics(MmaFight fight, String predictedResult) {
        MmaPredictionStatistics stats = mmaPredictionStatisticsRepository.findByFight(fight)
                .orElseGet(() -> {
                    MmaPredictionStatistics newStats = new MmaPredictionStatistics();
                    newStats.setFight(fight);
                    return newStats;
                });

        switch (predictedResult) {
            case "FIGHTER1":
                stats.setFighter1Votes(stats.getFighter1Votes() + 1);
                break;
            case "FIGHTER2":
                stats.setFighter2Votes(stats.getFighter2Votes() + 1);
                break;
        }
        stats.setTotalVotes(stats.getTotalVotes() + 1);

        mmaPredictionStatisticsRepository.save(stats);
    }

    // ========== 예측 조회 ==========

    /**
     * 특정 경기의 모든 예측 조회 (일반 경기 + MMA 경기 지원)
     */
    @Transactional(readOnly = true)
    public Page<PredictionDto> getPredictionsByMatch(Long matchId, Pageable pageable) {
        // 먼저 Match 테이블 확인
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isPresent()) {
            return predictionRepository.findByMatchOrderByLikeCountDescCreatedAtDesc(matchOpt.get(), pageable)
                    .map(this::convertToDto);
        }

        // Match에 없으면 MmaFight 테이블 확인
        Optional<MmaFight> fightOpt = mmaFightRepository.findById(matchId);
        if (fightOpt.isPresent()) {
            return mmaPredictionRepository.findByFightOrderByLikeCountDescCreatedAtDesc(fightOpt.get(), pageable)
                    .map(this::convertMmaPredictionToDto);
        }

        throw new RuntimeException("경기를 찾을 수 없습니다.");
    }

    /**
     * 특정 경기의 예측 통계 조회 (일반 경기 + MMA 경기 지원)
     */
    @Transactional(readOnly = true)
    public PredictionStatisticsDto getPredictionStatistics(Long matchId) {
        // 먼저 Match 테이블 확인
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isPresent()) {
            Match match = matchOpt.get();
            PredictionStatistics stats = predictionStatisticsRepository.findByMatch(match)
                    .orElseGet(() -> {
                        PredictionStatistics newStats = new PredictionStatistics();
                        newStats.setMatch(match);
                        newStats.setHomeVotes(0);
                        newStats.setDrawVotes(0);
                        newStats.setAwayVotes(0);
                        newStats.setTotalVotes(0);
                        return newStats;
                    });
            return convertStatisticsToDto(stats);
        }

        // Match에 없으면 MmaFight 테이블 확인
        Optional<MmaFight> fightOpt = mmaFightRepository.findById(matchId);
        if (fightOpt.isPresent()) {
            MmaFight fight = fightOpt.get();
            MmaPredictionStatistics stats = mmaPredictionStatisticsRepository.findByFight(fight)
                    .orElseGet(() -> {
                        MmaPredictionStatistics newStats = new MmaPredictionStatistics();
                        newStats.setFight(fight);
                        newStats.setFighter1Votes(0);
                        newStats.setFighter2Votes(0);
                        newStats.setTotalVotes(0);
                        return newStats;
                    });
            return convertMmaStatisticsToDto(stats);
        }

        throw new RuntimeException("경기를 찾을 수 없습니다.");
    }

    /**
     * 특정 MMA 경기의 모든 예측 조회
     */
    @Transactional(readOnly = true)
    public Page<PredictionDto> getMmaPredictionsByFight(Long fightId, Pageable pageable) {
        MmaFight fight = mmaFightRepository.findById(fightId)
                .orElseThrow(() -> new RuntimeException("MMA 경기를 찾을 수 없습니다."));

        return mmaPredictionRepository.findByFightOrderByLikeCountDescCreatedAtDesc(fight, pageable)
                .map(this::convertMmaPredictionToDto);
    }

    /**
     * 특정 MMA 경기의 예측 통계 조회
     */
    @Transactional(readOnly = true)
    public PredictionStatisticsDto getMmaPredictionStatistics(Long fightId) {
        MmaFight fight = mmaFightRepository.findById(fightId)
                .orElseThrow(() -> new RuntimeException("MMA 경기를 찾을 수 없습니다."));

        MmaPredictionStatistics stats = mmaPredictionStatisticsRepository.findByFight(fight)
                .orElseGet(() -> {
                    MmaPredictionStatistics newStats = new MmaPredictionStatistics();
                    newStats.setFight(fight);
                    newStats.setFighter1Votes(0);
                    newStats.setFighter2Votes(0);
                    newStats.setTotalVotes(0);
                    return newStats;
                });

        return convertMmaStatisticsToDto(stats);
    }

    /**
     * 사용자의 예측 내역 조회 (일반 경기 + MMA 경기 통합)
     */
    @Transactional(readOnly = true)
    public Page<PredictionDto> getUserPredictions(String username, String status, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<PredictionDto> allPredictions = new ArrayList<>();

        // 일반 경기 예측 조회
        Page<Prediction> predictions;
        if ("ongoing".equals(status)) {
            predictions = predictionRepository.findByUserAndIsCorrectIsNull(user, pageable);
        } else if ("completed".equals(status)) {
            predictions = predictionRepository.findByUserAndIsCorrectIsNotNull(user, pageable);
        } else {
            predictions = predictionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }
        allPredictions.addAll(predictions.stream().map(this::convertToDto).collect(Collectors.toList()));

        // MMA 경기 예측 조회
        Page<MmaPrediction> mmaPredictions;
        if ("ongoing".equals(status)) {
            mmaPredictions = mmaPredictionRepository.findByUserAndIsCorrectIsNull(user, pageable);
        } else if ("completed".equals(status)) {
            mmaPredictions = mmaPredictionRepository.findByUserAndIsCorrectIsNotNull(user, pageable);
        } else {
            mmaPredictions = mmaPredictionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }
        allPredictions.addAll(mmaPredictions.stream().map(this::convertMmaPredictionToDto).collect(Collectors.toList()));

        // 날짜순 정렬
        allPredictions.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return new org.springframework.data.domain.PageImpl<>(allPredictions, pageable, allPredictions.size());
    }

    // ========== 코멘트 추천/비추천 ==========

    /**
     * 예측 코멘트 추천 (일반 예측 + MMA 예측 통합)
     */
    @Transactional
    public void likePrediction(Long predictionId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 먼저 일반 Prediction 확인
        Optional<Prediction> predictionOpt = predictionRepository.findById(predictionId);
        if (predictionOpt.isPresent()) {
            Prediction prediction = predictionOpt.get();

            if (prediction.getUser().equals(user)) {
                throw new RuntimeException("자신의 예측은 추천할 수 없습니다.");
            }

            predictionVoteRepository.findByPredictionAndUser(prediction, user)
                    .ifPresent(vote -> {
                        throw new RuntimeException("이미 투표하셨습니다.");
                    });

            PredictionVote vote = new PredictionVote();
            vote.setPrediction(prediction);
            vote.setUser(user);
            vote.setVoteType("LIKE");
            predictionVoteRepository.save(vote);

            prediction.setLikeCount(prediction.getLikeCount() + 1);
            predictionRepository.save(prediction);
            return;
        }

        // MmaPrediction 확인
        Optional<MmaPrediction> mmaPredictionOpt = mmaPredictionRepository.findById(predictionId);
        if (mmaPredictionOpt.isPresent()) {
            MmaPrediction prediction = mmaPredictionOpt.get();

            if (prediction.getUser().equals(user)) {
                throw new RuntimeException("자신의 예측은 추천할 수 없습니다.");
            }

            // MMA 예측의 경우 직접 like count 증가 (별도 vote 테이블이 없다면)
            prediction.setLikeCount(prediction.getLikeCount() + 1);
            mmaPredictionRepository.save(prediction);
            return;
        }

        throw new RuntimeException("예측을 찾을 수 없습니다.");
    }

    /**
     * 예측 코멘트 비추천 (일반 예측 + MMA 예측 통합)
     */
    @Transactional
    public void dislikePrediction(Long predictionId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 먼저 일반 Prediction 확인
        Optional<Prediction> predictionOpt = predictionRepository.findById(predictionId);
        if (predictionOpt.isPresent()) {
            Prediction prediction = predictionOpt.get();

            if (prediction.getUser().equals(user)) {
                throw new RuntimeException("자신의 예측은 비추천할 수 없습니다.");
            }

            predictionVoteRepository.findByPredictionAndUser(prediction, user)
                    .ifPresent(vote -> {
                        throw new RuntimeException("이미 투표하셨습니다.");
                    });

            PredictionVote vote = new PredictionVote();
            vote.setPrediction(prediction);
            vote.setUser(user);
            vote.setVoteType("DISLIKE");
            predictionVoteRepository.save(vote);

            prediction.setDislikeCount(prediction.getDislikeCount() + 1);
            predictionRepository.save(prediction);
            return;
        }

        // MmaPrediction 확인
        Optional<MmaPrediction> mmaPredictionOpt = mmaPredictionRepository.findById(predictionId);
        if (mmaPredictionOpt.isPresent()) {
            MmaPrediction prediction = mmaPredictionOpt.get();

            if (prediction.getUser().equals(user)) {
                throw new RuntimeException("자신의 예측은 비추천할 수 없습니다.");
            }

            // MMA 예측의 경우 직접 dislike count 증가
            prediction.setDislikeCount(prediction.getDislikeCount() + 1);
            mmaPredictionRepository.save(prediction);
            return;
        }

        throw new RuntimeException("예측을 찾을 수 없습니다.");
    }

    // ========== 결과 처리 (경기 종료 후) ==========

    /**
     * 경기 종료 후 예측 결과 판정
     */
    @Transactional
    public void judgePredictions(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다."));

        if (!match.getStatus().equals("FINISHED")) {
            throw new RuntimeException("아직 종료되지 않은 경기입니다.");
        }

        String actualResult = determineMatchResult(match);
        List<Prediction> predictions = predictionRepository.findByMatchOrderByLikeCountDescCreatedAtDesc(match);

        // 예측 통계 가져오기 (배당률 계산용)
        PredictionStatistics stats = predictionStatisticsRepository.findByMatch(match)
                .orElseGet(() -> {
                    PredictionStatistics newStats = new PredictionStatistics();
                    newStats.setMatch(match);
                    newStats.setHomeVotes(0);
                    newStats.setDrawVotes(0);
                    newStats.setAwayVotes(0);
                    newStats.setTotalVotes(0);
                    return newStats;
                });

        // 각 선택지의 비율 계산
        double homeRatio = stats.getTotalVotes() > 0 ? (double) stats.getHomeVotes() / stats.getTotalVotes() : 0.33;
        double drawRatio = stats.getTotalVotes() > 0 ? (double) stats.getDrawVotes() / stats.getTotalVotes() : 0.33;
        double awayRatio = stats.getTotalVotes() > 0 ? (double) stats.getAwayVotes() / stats.getTotalVotes() : 0.33;

        for (Prediction prediction : predictions) {
            boolean isCorrect = prediction.getPredictedResult().equals(actualResult);
            prediction.setIsCorrect(isCorrect);
            predictionRepository.save(prediction);

            // 배당률 + 참여자 수 기반 점수 계산
            User user = prediction.getUser();
            int pointsChange = 0;
            int totalVotes = stats.getTotalVotes();

            if (isCorrect) {
                // 적중 시: 선택한 비율과 참여자 수에 따라 점수 차등 지급
                switch (prediction.getPredictedResult()) {
                    case "HOME":
                        pointsChange = calculateWinPoints(homeRatio, totalVotes);
                        break;
                    case "DRAW":
                        pointsChange = calculateWinPoints(drawRatio, totalVotes);
                        break;
                    case "AWAY":
                        pointsChange = calculateWinPoints(awayRatio, totalVotes);
                        break;
                }
            } else {
                // 실패 시: 선택한 비율과 참여자 수에 따라 점수 차등 감점
                switch (prediction.getPredictedResult()) {
                    case "HOME":
                        pointsChange = calculateLosePoints(homeRatio, totalVotes);
                        break;
                    case "DRAW":
                        pointsChange = calculateLosePoints(drawRatio, totalVotes);
                        break;
                    case "AWAY":
                        pointsChange = calculateLosePoints(awayRatio, totalVotes);
                        break;
                }
            }

            user.setTierScore(Math.max(0, user.getTierScore() + pointsChange));
            updateUserTier(user);
            userRepository.save(user);

            // 알림 기능은 나중에 구현 (현재는 주석 처리)
        }
    }

    /**
     * 경기 결과 판정
     */
    private String determineMatchResult(Match match) {
        Integer homeScore = match.getHomeScore();
        Integer awayScore = match.getAwayScore();

        if (homeScore == null || awayScore == null) {
            throw new RuntimeException("경기 점수가 없습니다. matchId=" + match.getMatchId());
        }

        if (homeScore > awayScore) {
            return "HOME";
        } else if (homeScore < awayScore) {
            return "AWAY";
        } else {
            return "DRAW";
        }
    }

    /**
     * 농구 경기 여부 확인
     * 농구는 연장전으로 무승부가 없는 종목
     */
    private boolean isBasketballMatch(Match match) {
        if (match.getLeague() == null || match.getLeague().getSport() == null) {
            return false;
        }

        // sport_id가 2이거나 sportName이 "BASKETBALL"인 경우
        Sport sport = match.getLeague().getSport();
        return sport.getSportId() == 2L || "BASKETBALL".equalsIgnoreCase(sport.getSportName());
    }

    /**
     * MMA 경기 종료 후 예측 결과 판정
     */
    @Transactional
    public void judgeMmaPredictions(Long fightId) {
        MmaFight fight = mmaFightRepository.findById(fightId)
                .orElseThrow(() -> new RuntimeException("MMA 경기를 찾을 수 없습니다."));

        if (!fight.getStatus().equals("FINISHED")) {
            throw new RuntimeException("아직 종료되지 않은 경기입니다.");
        }

        if (fight.getWinner() == null) {
            throw new RuntimeException("승자 정보가 없습니다. fightId=" + fightId);
        }

        String actualResult = determineMmaResult(fight);
        List<MmaPrediction> predictions = mmaPredictionRepository.findByFightOrderByLikeCountDescCreatedAtDesc(fight);

        // 예측 통계 가져오기 (배당률 계산용)
        MmaPredictionStatistics stats = mmaPredictionStatisticsRepository.findByFight(fight)
                .orElseGet(() -> {
                    MmaPredictionStatistics newStats = new MmaPredictionStatistics();
                    newStats.setFight(fight);
                    newStats.setFighter1Votes(0);
                    newStats.setFighter2Votes(0);
                    newStats.setTotalVotes(0);
                    return newStats;
                });

        // 각 선택지의 비율 계산
        double fighter1Ratio = stats.getTotalVotes() > 0 ? (double) stats.getFighter1Votes() / stats.getTotalVotes() : 0.5;
        double fighter2Ratio = stats.getTotalVotes() > 0 ? (double) stats.getFighter2Votes() / stats.getTotalVotes() : 0.5;

        for (MmaPrediction prediction : predictions) {
            boolean isCorrect = prediction.getPredictedResult().equals(actualResult);
            prediction.setIsCorrect(isCorrect);
            mmaPredictionRepository.save(prediction);

            // 배당률 + 참여자 수 기반 점수 계산
            User user = prediction.getUser();
            int pointsChange = 0;
            int totalVotes = stats.getTotalVotes();

            if (isCorrect) {
                // 적중 시
                switch (prediction.getPredictedResult()) {
                    case "FIGHTER1":
                        pointsChange = calculateWinPoints(fighter1Ratio, totalVotes);
                        break;
                    case "FIGHTER2":
                        pointsChange = calculateWinPoints(fighter2Ratio, totalVotes);
                        break;
                }
            } else {
                // 실패 시
                switch (prediction.getPredictedResult()) {
                    case "FIGHTER1":
                        pointsChange = calculateLosePoints(fighter1Ratio, totalVotes);
                        break;
                    case "FIGHTER2":
                        pointsChange = calculateLosePoints(fighter2Ratio, totalVotes);
                        break;
                }
            }

            user.setTierScore(Math.max(0, user.getTierScore() + pointsChange));
            updateUserTier(user);
            userRepository.save(user);
        }
    }

    /**
     * MMA 경기 결과 판정
     */
    private String determineMmaResult(MmaFight fight) {
        if (fight.getWinner() == null) {
            throw new RuntimeException("승자 정보가 없습니다. fightId=" + fight.getFightId());
        }

        if (fight.getWinner().getFighterId().equals(fight.getFighter1().getFighterId())) {
            return "FIGHTER1";
        } else {
            return "FIGHTER2";
        }
    }

    /**
     * 티어 점수에 따른 티어 업데이트
     */
    private void updateUserTier(User user) {
        int score = user.getTierScore();

        if (score >= 1000) {
            user.setTier("DIAMOND");
        } else if (score >= 600) {
            user.setTier("PLATINUM");
        } else if (score >= 300) {
            user.setTier("GOLD");
        } else if (score >= 100) {
            user.setTier("SILVER");
        } else {
            user.setTier("BRONZE");
        }
    }

    // ========== 랭킹 시스템 ==========

    @Transactional(readOnly = true)
    public List<PredictionRankingDto> getOverallRanking(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<User> topUsers = userRepository.findAllByOrderByTierScoreDescTierAsc(pageable).getContent();

        return topUsers.stream()
                .map((user) -> createRankingDto(user, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PredictionRankingDto> getSportRanking(String sportName, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<User> topUsers = userRepository.findAllByOrderByTierScoreDescTierAsc(pageable).getContent();

        return topUsers.stream()
                .map((user) -> createRankingDto(user, sportName))
                .collect(Collectors.toList());
    }

    private PredictionRankingDto createRankingDto(User user, String sportName) {
        PredictionRankingDto dto = new PredictionRankingDto();
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setTier(user.getTier());
        dto.setTierScore(user.getTierScore());

        if (sportName != null) {
            long totalPredictions = predictionRepository.countByUserAndSport(user, sportName);
            long correctPredictions = predictionRepository.countByUserAndSportAndIsCorrect(user, sportName);
            dto.setTotalPredictions((int) totalPredictions);
            dto.setCorrectPredictions((int) correctPredictions);
        } else {
            long totalPredictions = predictionRepository.countByUser(user);
            long correctPredictions = predictionRepository.countByUserAndIsCorrect(user, true);
            dto.setTotalPredictions((int) totalPredictions);
            dto.setCorrectPredictions((int) correctPredictions);
        }

        if (dto.getTotalPredictions() > 0) {
            dto.setAccuracy((double) dto.getCorrectPredictions() / dto.getTotalPredictions() * 100);
        } else {
            dto.setAccuracy(0.0);
        }

        dto.setConsecutiveCorrect(calculateConsecutiveCorrect(user));

        return dto;
    }

    private Integer calculateConsecutiveCorrect(User user) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prediction> recentPredictions = predictionRepository
                .findByUserAndIsCorrectIsNotNull(user, pageable);

        int consecutive = 0;
        for (Prediction prediction : recentPredictions) {
            if (Boolean.TRUE.equals(prediction.getIsCorrect())) {
                consecutive++;
            } else {
                break;
            }
        }
        return consecutive;
    }

    // ========== 통계/분석 ==========

    @Transactional(readOnly = true)
    public PredictionRankingDto getUserStatistics(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return createRankingDto(user, null);
    }

    @Transactional(readOnly = true)
    public PredictionRankingDto getWeeklyStatistics(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);

        PredictionRankingDto dto = new PredictionRankingDto();
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setTier(user.getTier());
        dto.setTierScore(user.getTierScore());

        long totalPredictions = predictionRepository.countByUserAndCreatedAtAfter(user, weekAgo);
        long correctPredictions = predictionRepository.countByUserAndCreatedAtAfterAndIsCorrect(user, weekAgo);

        dto.setTotalPredictions((int) totalPredictions);
        dto.setCorrectPredictions((int) correctPredictions);

        if (totalPredictions > 0) {
            dto.setAccuracy((double) correctPredictions / totalPredictions * 100);
        } else {
            dto.setAccuracy(0.0);
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public PredictionRankingDto getMonthlyStatistics(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);

        PredictionRankingDto dto = new PredictionRankingDto();
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setTier(user.getTier());
        dto.setTierScore(user.getTierScore());

        long totalPredictions = predictionRepository.countByUserAndCreatedAtAfter(user, monthAgo);
        long correctPredictions = predictionRepository.countByUserAndCreatedAtAfterAndIsCorrect(user, monthAgo);

        dto.setTotalPredictions((int) totalPredictions);
        dto.setCorrectPredictions((int) correctPredictions);

        if (totalPredictions > 0) {
            dto.setAccuracy((double) correctPredictions / totalPredictions * 100);
        } else {
            dto.setAccuracy(0.0);
        }

        return dto;
    }

    // ========== DTO 변환 ==========

    private PredictionDto convertToDto(Prediction prediction) {
        PredictionDto dto = new PredictionDto();
        dto.setPredictionId(prediction.getPredictionId());
        dto.setMatchId(prediction.getMatch().getMatchId());
        dto.setUsername(prediction.getUser().getUsername());
        dto.setNickname(prediction.getUser().getNickname());
        dto.setUserTier(prediction.getUser().getTier());
        dto.setPredictedResult(prediction.getPredictedResult());
        dto.setComment(prediction.getComment());
        dto.setIsCorrect(prediction.getIsCorrect());
        dto.setLikeCount(prediction.getLikeCount());
        dto.setDislikeCount(prediction.getDislikeCount());
        dto.setCreatedAt(prediction.getCreatedAt());
        return dto;
    }

    private PredictionStatisticsDto convertStatisticsToDto(PredictionStatistics stats) {
        PredictionStatisticsDto dto = new PredictionStatisticsDto();
        dto.setMatchId(stats.getMatch().getMatchId());

        // 농구 경기는 무승부가 없음
        boolean isBasketball = isBasketballMatch(stats.getMatch());

        dto.setHomeVotes(stats.getHomeVotes());
        dto.setDrawVotes(isBasketball ? 0 : stats.getDrawVotes());
        dto.setAwayVotes(stats.getAwayVotes());
        dto.setTotalVotes(stats.getTotalVotes());

        if (stats.getTotalVotes() > 0) {
            double homeRatio = (double) stats.getHomeVotes() / stats.getTotalVotes();
            double drawRatio = isBasketball ? 0.0 : (double) stats.getDrawVotes() / stats.getTotalVotes();
            double awayRatio = (double) stats.getAwayVotes() / stats.getTotalVotes();

            dto.setHomePercentage(homeRatio * 100);
            dto.setDrawPercentage(isBasketball ? 0.0 : drawRatio * 100);
            dto.setAwayPercentage(awayRatio * 100);

            // 예상 점수 계산 (배당률 + 참여자 수 고려)
            int totalVotes = stats.getTotalVotes();
            dto.setHomeWinPoints(calculateWinPoints(homeRatio, totalVotes));
            dto.setHomeLosePoints(calculateLosePoints(homeRatio, totalVotes));
            dto.setDrawWinPoints(isBasketball ? 0 : calculateWinPoints(drawRatio, totalVotes));
            dto.setDrawLosePoints(isBasketball ? 0 : calculateLosePoints(drawRatio, totalVotes));
            dto.setAwayWinPoints(calculateWinPoints(awayRatio, totalVotes));
            dto.setAwayLosePoints(calculateLosePoints(awayRatio, totalVotes));
        } else {
            dto.setHomePercentage(0.0);
            dto.setDrawPercentage(0.0);
            dto.setAwayPercentage(0.0);

            // 투표가 없을 때는 기본 점수
            dto.setHomeWinPoints(10);
            dto.setHomeLosePoints(-10);
            dto.setDrawWinPoints(isBasketball ? 0 : 10);
            dto.setDrawLosePoints(isBasketball ? 0 : -10);
            dto.setAwayWinPoints(10);
            dto.setAwayLosePoints(-10);
        }

        return dto;
    }

    /**
     * 배당률 기반 적중 점수 계산 (참여자 수 고려)
     * 공식: 10 + (90 * (1 - 선택 비율) * 참여자 보정 계수)
     * 참여자 보정 계수 = min(1.0, totalVotes / 10.0)
     * - 10명 이상: 최대 배당 효과
     * - 5명: 50% 배당 효과
     * - 1명: 10% 배당 효과
     */
    private Integer calculateWinPoints(double ratio, int totalVotes) {
        double participantFactor = Math.min(1.0, totalVotes / 10.0);
        return (int) Math.round(10 + (90 * (1 - ratio) * participantFactor));
    }

    /**
     * 배당률 기반 실패 점수 계산 (참여자 수 고려)
     * 공식: -(10 + (90 * 선택 비율 * 참여자 보정 계수))
     * 참여자 보정 계수 = min(1.0, totalVotes / 10.0)
     */
    private Integer calculateLosePoints(double ratio, int totalVotes) {
        double participantFactor = Math.min(1.0, totalVotes / 10.0);
        return -(int) Math.round(10 + (90 * ratio * participantFactor));
    }

    /**
     * Match 엔티티를 MatchDto로 변환
     */
    private MatchDto convertMatchToDto(Match match) {
        MatchDto dto = new MatchDto();
        dto.setMatchId(match.getMatchId());

        // 리그 정보
        if (match.getLeague() != null) {
            MatchDto.LeagueInfo leagueInfo = new MatchDto.LeagueInfo();
            leagueInfo.setLeagueId(match.getLeague().getLeagueId());
            leagueInfo.setName(match.getLeague().getLeagueName());
            leagueInfo.setCountry(match.getLeague().getCountry());
            leagueInfo.setLogo(match.getLeague().getLeagueLogo());
            dto.setLeague(leagueInfo);

            // 종목 정보
            if (match.getLeague().getSport() != null) {
                dto.setSportType(match.getLeague().getSport().getSportName());
            }
        }

        // 팀 정보
        MatchDto.TeamInfo teamInfo = new MatchDto.TeamInfo();

        if (match.getHomeTeam() != null) {
            MatchDto.Team homeTeam = new MatchDto.Team();
            homeTeam.setId(match.getHomeTeam().getTeamId());
            homeTeam.setName(match.getHomeTeam().getTeamName());
            homeTeam.setLogo(match.getHomeTeam().getTeamLogo());
            homeTeam.setCountry(match.getHomeTeam().getCountry());
            teamInfo.setHome(homeTeam);
        }

        if (match.getAwayTeam() != null) {
            MatchDto.Team awayTeam = new MatchDto.Team();
            awayTeam.setId(match.getAwayTeam().getTeamId());
            awayTeam.setName(match.getAwayTeam().getTeamName());
            awayTeam.setLogo(match.getAwayTeam().getTeamLogo());
            awayTeam.setCountry(match.getAwayTeam().getCountry());
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
     * MmaFight 엔티티를 MatchDto로 변환
     */
    private MatchDto convertMmaFightToMatchDto(MmaFight fight) {
        MatchDto dto = new MatchDto();
        dto.setMatchId(fight.getFightId());
        dto.setSportType("MMA");

        // 리그 정보
        if (fight.getLeague() != null) {
            MatchDto.LeagueInfo leagueInfo = new MatchDto.LeagueInfo();
            leagueInfo.setLeagueId(fight.getLeague().getLeagueId());
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
        if (fight.getWinner() != null) {
            detail.setWinner(fight.getWinner().getFighterName());
        }
        detail.setMethod(fight.getMethod());
        detail.setRound(fight.getRound());
        dto.setDetail(detail);

        return dto;
    }

    /**
     * MmaPrediction 엔티티를 PredictionDto로 변환
     */
    private PredictionDto convertMmaPredictionToDto(MmaPrediction prediction) {
        PredictionDto dto = new PredictionDto();
        dto.setPredictionId(prediction.getPredictionId());
        dto.setMatchId(prediction.getFight().getFightId());
        dto.setUsername(prediction.getUser().getUsername());
        dto.setNickname(prediction.getUser().getNickname());
        dto.setUserTier(prediction.getUser().getTier());
        dto.setPredictedResult(prediction.getPredictedResult());
        dto.setComment(prediction.getComment());
        dto.setIsCorrect(prediction.getIsCorrect());
        dto.setLikeCount(prediction.getLikeCount());
        dto.setDislikeCount(prediction.getDislikeCount());
        dto.setCreatedAt(prediction.getCreatedAt());
        return dto;
    }

    /**
     * MmaPredictionStatistics를 PredictionStatisticsDto로 변환
     */
    private PredictionStatisticsDto convertMmaStatisticsToDto(MmaPredictionStatistics stats) {
        PredictionStatisticsDto dto = new PredictionStatisticsDto();
        dto.setMatchId(stats.getFight().getFightId());
        dto.setHomeVotes(stats.getFighter1Votes());
        dto.setDrawVotes(0);  // MMA에는 무승부가 없음
        dto.setAwayVotes(stats.getFighter2Votes());
        dto.setTotalVotes(stats.getTotalVotes());

        if (stats.getTotalVotes() > 0) {
            double fighter1Ratio = (double) stats.getFighter1Votes() / stats.getTotalVotes();
            double fighter2Ratio = (double) stats.getFighter2Votes() / stats.getTotalVotes();

            dto.setHomePercentage(fighter1Ratio * 100);
            dto.setDrawPercentage(0.0);
            dto.setAwayPercentage(fighter2Ratio * 100);

            // 예상 점수 계산
            int totalVotes = stats.getTotalVotes();
            dto.setHomeWinPoints(calculateWinPoints(fighter1Ratio, totalVotes));
            dto.setHomeLosePoints(calculateLosePoints(fighter1Ratio, totalVotes));
            dto.setDrawWinPoints(0);
            dto.setDrawLosePoints(0);
            dto.setAwayWinPoints(calculateWinPoints(fighter2Ratio, totalVotes));
            dto.setAwayLosePoints(calculateLosePoints(fighter2Ratio, totalVotes));
        } else {
            dto.setHomePercentage(0.0);
            dto.setDrawPercentage(0.0);
            dto.setAwayPercentage(0.0);

            // 투표가 없을 때는 기본 점수
            dto.setHomeWinPoints(10);
            dto.setHomeLosePoints(-10);
            dto.setDrawWinPoints(0);
            dto.setDrawLosePoints(0);
            dto.setAwayWinPoints(10);
            dto.setAwayLosePoints(-10);
        }

        return dto;
    }
}