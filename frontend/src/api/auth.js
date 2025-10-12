// 백엔드 API 기본 URL
const API_BASE_URL = 'http://localhost:8080/api';

/**
 * 회원가입 API 호출
 * @param {Object} signupData - 회원가입 데이터 (username, password, passwordConfirm, nickname, email)
 * @returns {Promise} - 회원가입 결과
 */
export const signup = async (signupData) => {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/signup`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(signupData),
    });

    const data = await response.json();

    if (!response.ok) {
      // 에러 응답 처리
      throw new Error(data.message || '회원가입에 실패했습니다.');
    }

    return data;
  } catch (error) {
    throw error;
  }
};

/**
 * 로그인 API 호출
 * @param {Object} loginData - 로그인 데이터 (username, password)
 * @returns {Promise} - 로그인 결과
 */
export const login = async (loginData) => {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(loginData),
    });

    const data = await response.json();

    if (!response.ok) {
      // 에러 응답 처리
      throw new Error(data.message || '로그인에 실패했습니다.');
    }

    return data;
  } catch (error) {
    throw error;
  }
};

/**
 * 아이디 중복 체크 API 호출
 * @param {string} username - 체크할 아이디
 * @returns {Promise<boolean>} - 중복 여부
 */
export const checkUsernameDuplicate = async (username) => {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/check-username?username=${username}`);
    const data = await response.json();
    return data.isDuplicate;
  } catch (error) {
    console.error('아이디 중복 체크 오류:', error);
    return false;
  }
};

/**
 * 이메일 중복 체크 API 호출
 * @param {string} email - 체크할 이메일
 * @returns {Promise<boolean>} - 중복 여부
 */
export const checkEmailDuplicate = async (email) => {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/check-email?email=${email}`);
    const data = await response.json();
    return data.isDuplicate;
  } catch (error) {
    console.error('이메일 중복 체크 오류:', error);
    return false;
  }
};

/**
 * 닉네임 중복 체크 API 호출
 * @param {string} nickname - 체크할 닉네임
 * @returns {Promise<boolean>} - 중복 여부
 */
export const checkNicknameDuplicate = async (nickname) => {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/check-nickname?nickname=${nickname}`);
    const data = await response.json();
    return data.isDuplicate;
  } catch (error) {
    console.error('닉네임 중복 체크 오류:', error);
    return false;
  }
};