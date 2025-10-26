/**
 * API 유틸리티
 * - 백엔드 API 호출 공통 함수
 * - JWT 토큰 자동 포함
 * - 에러 처리
 * 
 * 파일 위치: frontend/src/api/api.js
 */

// 환경변수에서 API Base URL 가져오기
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * API 요청 공통 함수
 * @param {string} endpoint - API 엔드포인트 (예: '/api/community/posts')
 * @param {object} options - fetch options (method, body 등)
 * @returns {Promise} - API 응답 데이터
 */
export const apiRequest = async (endpoint, options = {}) => {
  // 기본 헤더 설정
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  // JWT 토큰이 있으면 Authorization 헤더에 추가
  const token = getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // API 요청 실행
  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    // 응답이 JSON이 아닌 경우 처리
    const contentType = response.headers.get('content-type');
    if (!contentType || !contentType.includes('application/json')) {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: 서버 오류가 발생했습니다.`);
      }
      return null;
    }

    // JSON 파싱
    const data = await response.json();

    // HTTP 에러 처리
    if (!response.ok) {
      throw new Error(data.message || `HTTP ${response.status} 오류`);
    }

    return data;
  } catch (error) {
    console.error('API 요청 오류:', error);
    throw error;
  }
};

/**
 * GET 요청
 */
export const apiGet = (endpoint) => {
  return apiRequest(endpoint, { method: 'GET' });
};

/**
 * POST 요청
 */
export const apiPost = (endpoint, data) => {
  return apiRequest(endpoint, {
    method: 'POST',
    body: JSON.stringify(data),
  });
};

/**
 * PUT 요청
 */
export const apiPut = (endpoint, data) => {
  return apiRequest(endpoint, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
};

/**
 * DELETE 요청
 */
export const apiDelete = (endpoint) => {
  return apiRequest(endpoint, { method: 'DELETE' });
};

// ========== 토큰 관리 함수 ==========

/**
 * 로컬 스토리지에서 JWT 토큰 가져오기
 */
export const getToken = () => {
  const user = localStorage.getItem('user');
  if (user) {
    try {
      const userData = JSON.parse(user);
      return userData.token;
    } catch (e) {
      return null;
    }
  }
  return null;
};

/**
 * 로컬 스토리지에 사용자 정보 및 토큰 저장
 */
export const saveUserData = (userData) => {
  localStorage.setItem('user', JSON.stringify(userData));
};

/**
 * 로컬 스토리지에서 사용자 정보 가져오기
 */
export const getUserData = () => {
  const user = localStorage.getItem('user');
  if (user) {
    try {
      return JSON.parse(user);
    } catch (e) {
      return null;
    }
  }
  return null;
};

/**
 * 로컬 스토리지에서 사용자 정보 삭제 (로그아웃)
 */
export const clearUserData = () => {
  localStorage.removeItem('user');
};

/**
 * 로그인 여부 확인
 */
export const isLoggedIn = () => {
  return getToken() !== null;
};