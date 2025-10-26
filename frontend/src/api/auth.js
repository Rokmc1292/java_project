/**
 * 인증 관련 API 함수들
 * 회원가입, 로그인, 중복 체크
 * 
 * 파일 위치: frontend/src/api/auth.js
 */

import { apiPost, apiGet, saveUserData } from './api';

/**
 * 회원가입
 * @param {object} signupData - 회원가입 데이터 (username, password, passwordConfirm, nickname, email)
 * @returns {Promise} - 생성된 사용자 정보
 */
export const signup = async (signupData) => {
  try {
    const response = await apiPost('/api/auth/signup', signupData);
    return response;
  } catch (error) {
    throw error;
  }
};

/**
 * 로그인 (JWT 토큰 발급)
 * @param {object} loginData - 로그인 데이터 (username, password)
 * @returns {Promise} - 사용자 정보 + JWT 토큰
 */
export const login = async (loginData) => {
  try {
    const response = await apiPost('/api/auth/login', loginData);
    
    // 응답에서 user와 token을 합쳐서 저장
    const userData = {
      ...response.user,
      token: response.token
    };
    
    // 로컬 스토리지에 저장
    saveUserData(userData);
    
    return userData;
  } catch (error) {
    throw error;
  }
};

/**
 * 아이디 중복 체크
 * @param {string} username - 확인할 아이디
 * @returns {Promise<boolean>} - 중복 여부 (true: 중복, false: 사용 가능)
 */
export const checkUsernameDuplicate = async (username) => {
  try {
    const response = await apiGet(`/api/auth/check-username?username=${username}`);
    return response.isDuplicate;
  } catch (error) {
    console.error('아이디 중복 체크 오류:', error);
    return false;
  }
};

/**
 * 이메일 중복 체크
 * @param {string} email - 확인할 이메일
 * @returns {Promise<boolean>} - 중복 여부 (true: 중복, false: 사용 가능)
 */
export const checkEmailDuplicate = async (email) => {
  try {
    const response = await apiGet(`/api/auth/check-email?email=${email}`);
    return response.isDuplicate;
  } catch (error) {
    console.error('이메일 중복 체크 오류:', error);
    return false;
  }
};

/**
 * 닉네임 중복 체크
 * @param {string} nickname - 확인할 닉네임
 * @returns {Promise<boolean>} - 중복 여부 (true: 중복, false: 사용 가능)
 */
export const checkNicknameDuplicate = async (nickname) => {
  try {
    const response = await apiGet(`/api/auth/check-nickname?nickname=${nickname}`);
    return response.isDuplicate;
  } catch (error) {
    console.error('닉네임 중복 체크 오류:', error);
    return false;
  }
};

/**
 * 현재 로그인한 사용자 정보 조회
 * @returns {Promise} - 사용자 정보
 */
export const getCurrentUser = async () => {
  try {
    const response = await apiGet('/api/auth/me');
    return response;
  } catch (error) {
    throw error;
  }
};