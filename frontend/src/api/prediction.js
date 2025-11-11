/**
 * 승부예측 관련 API 함수들
 * - 예측 경기 목록, 예측 참여, 코멘트 추천/비추천, 랭킹, 통계 등
 * 
 * 파일 위치: frontend/src/api/prediction.js
 */

import { apiGet, apiPost } from './api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// ========== 예측 경기 목록 ==========

/**
 * 예측 가능한 경기 목록 조회 (D-2 경기)
 */
export const getPredictableMatches = async (sport = 'ALL', page = 0, size = 20) => {
  try {
    return await apiGet(`/api/predictions/matches?sport=${sport}&page=${page}&size=${size}`);
  } catch (error) {
    console.error('예측 가능한 경기 목록 조회 실패:', error);
    throw error;
  }
};

/**
 * 사용자가 이미 예측했는지 확인
 */
export const checkUserPrediction = async (matchId) => {
  try {
    return await apiGet(`/api/predictions/match/${matchId}/check`);
  } catch (error) {
    console.error('예측 여부 확인 실패:', error);
    throw error;
  }
};

// ========== 예측 참여 ==========

/**
 * 승부예측 생성
 */
export const createPrediction = async (matchId, predictedResult, comment) => {
  try {
    return await apiPost('/api/predictions', {
      matchId,
      predictedResult,
      comment
    });
  } catch (error) {
    console.error('예측 생성 실패:', error);
    throw error;
  }
};

// ========== 예측 조회 ==========

/**
 * 특정 경기의 모든 예측 조회
 */
export const getPredictionsByMatch = async (matchId, page = 0, size = 20) => {
  try {
    return await apiGet(`/api/predictions/match/${matchId}?page=${page}&size=${size}`);
  } catch (error) {
    console.error('경기별 예측 조회 실패:', error);
    throw error;
  }
};

/**
 * 특정 경기의 예측 통계 조회
 */
export const getPredictionStatistics = async (matchId) => {
  try {
    return await apiGet(`/api/predictions/match/${matchId}/statistics`);
  } catch (error) {
    console.error('예측 통계 조회 실패:', error);
    throw error;
  }
};

/**
 * 사용자의 예측 내역 조회
 */
export const getUserPredictions = async (username, status = 'all', page = 0, size = 20) => {
  try {
    return await apiGet(`/api/predictions/user/${username}?status=${status}&page=${page}&size=${size}`);
  } catch (error) {
    console.error('사용자 예측 내역 조회 실패:', error);
    throw error;
  }
};

// ========== 코멘트 추천/비추천 ==========

/**
 * 예측 코멘트 추천
 */
export const likePrediction = async (predictionId) => {
  try {
    return await apiPost(`/api/predictions/${predictionId}/like`);
  } catch (error) {
    console.error('예측 추천 실패:', error);
    throw error;
  }
};

/**
 * 예측 코멘트 비추천
 */
export const dislikePrediction = async (predictionId) => {
  try {
    return await apiPost(`/api/predictions/${predictionId}/dislike`);
  } catch (error) {
    console.error('예측 비추천 실패:', error);
    throw error;
  }
};

// ========== 랭킹 시스템 ==========

/**
 * 전체 랭킹 조회
 */
export const getOverallRanking = async (limit = 100) => {
  try {
    return await apiGet(`/api/predictions/ranking?limit=${limit}`);
  } catch (error) {
    console.error('전체 랭킹 조회 실패:', error);
    throw error;
  }
};

/**
 * 종목별 랭킹 조회
 */
export const getSportRanking = async (sportName, limit = 100) => {
  try {
    return await apiGet(`/api/predictions/ranking/${sportName}?limit=${limit}`);
  } catch (error) {
    console.error('종목별 랭킹 조회 실패:', error);
    throw error;
  }
};

// ========== 통계/분석 ==========

/**
 * 사용자 전체 통계 조회
 */
export const getUserStatistics = async (username) => {
  try {
    return await apiGet(`/api/predictions/statistics/${username}`);
  } catch (error) {
    console.error('사용자 통계 조회 실패:', error);
    throw error;
  }
};

/**
 * 주간 통계 조회
 */
export const getWeeklyStatistics = async (username) => {
  try {
    return await apiGet(`/api/predictions/statistics/${username}/weekly`);
  } catch (error) {
    console.error('주간 통계 조회 실패:', error);
    throw error;
  }
};

/**
 * 월간 통계 조회
 */
export const getMonthlyStatistics = async (username) => {
  try {
    return await apiGet(`/api/predictions/statistics/${username}/monthly`);
  } catch (error) {
    console.error('월간 통계 조회 실패:', error);
    throw error;
  }
};