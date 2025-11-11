/**
 * 인증 관련 API 함수들
 * 회원가입, 로그인, 로그아웃, 중복 체크, 세션 확인
 *
 * 파일 위치: frontend/src/api/auth.js
 */

import { apiPost, apiGet, saveUserData } from './api';

/**
 * 회원가입
 * @param {object} signupData - { username, password, passwordConfirm, nickname, email }
 */
export const signup = async (signupData) => {
    try {
        const res = await apiPost('/api/auth/signup', signupData);
        return res;
    } catch (err) {
        throw err;
    }
};

/**
 * 로그인 (세션 기반)
 * 서버가 { user: {...} } 형태를 반환하므로 user만 저장
 * 세션 유지를 위해 credentials: 'include' 필수
 *  - apiPost가 credentials를 안 붙여준다면, 아래 fetch 분기 사용
 *  - 가능하면 apiPost 내부에서 credentials: 'include'를 기본값으로 넣어두는 것을 추천
 */
export const login = async (loginData) => {
    try {
        // 1) apiPost를 우선 사용 (프로젝트 공통 처리 활용)
        //    apiPost가 credentials: 'include'를 설정하고 있다고 가정
        try {
            const res = await apiPost('/api/auth/login', loginData);
            const user = res?.user ?? res; // { user: {...} } or {...}
            saveUserData(user);            // localStorage에 user 저장
            return user;
        } catch (e) {
            // 2) apiPost가 쿠키를 안 붙이면 세션이 유지되지 않으니, 안전장치로 직접 fetch 시도
            const r = await fetch('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include', // ★ 세션 쿠키 유지
                body: JSON.stringify(loginData),
            });

            if (!r.ok) {
                const err = await r.json().catch(() => ({}));
                throw new Error(err.message || '로그인 실패');
            }

            const data = await r.json();
            const user = data?.user ?? data;
            saveUserData(user);
            return user;
        }
    } catch (err) {
        throw err;
    }
};

/**
 * 로그아웃 (세션 종료)
 */
export const logout = async () => {
    // apiPost 선호, 실패 시 fetch 폴백
    try {
        await apiPost('/api/auth/logout', {});
    } catch {
        await fetch('http://localhost:8080/api/auth/logout', {
            method: 'POST',
            credentials: 'include',
        }).catch(() => {});
    }
    // 클라이언트 보관 정보 초기화
    try {
        saveUserData(null); // 프로젝트의 saveUserData가 null 처리 지원하지 않으면 아래로 대체
    } catch {
        localStorage.removeItem('user');
    }
};

/**
 * 현재 로그인한 사용자 정보 (세션 기준)
 * 서버의 /api/auth/me가 세션의 username으로 사용자 정보를 돌려줌
 */
export const getCurrentUser = async () => {
    // apiGet 선호, 실패 시 fetch 폴백
    try {
        return await apiGet('/api/auth/me');
    } catch {
        const r = await fetch('http://localhost:8080/api/auth/me', {
            method: 'GET',
            credentials: 'include',
        });
        if (!r.ok) throw new Error('인증 필요');
        return await r.json();
    }
};

/**
 * 로그인 상태 및 권한 확인 (백엔드의 /api/auth/check 사용 시)
 * 서버가 { username, nickname, isAdmin, ... } 형태로 반환하도록 구현되어 있어야 함
 * 성공 시 프론트 보관 정보 갱신
 */
export const checkAuth = async () => {
    try {
        // apiGet 사용
        try {
            const res = await apiGet('/api/auth/check');
            // 서버 응답이 사용자 객체 형태라면 그대로 저장
            saveUserData(res);
            return res;
        } catch {
            // fetch 폴백
            const r = await fetch('http://localhost:8080/api/auth/check', {
                method: 'GET',
                credentials: 'include',
            });
            if (!r.ok) throw new Error('UNAUTHORIZED');
            const data = await r.json();
            saveUserData(data);
            return data;
        }
    } catch (err) {
        // 인증 아님 → 클라이언트 보관 정보 제거
        try {
            saveUserData(null);
        } catch {
            localStorage.removeItem('user');
        }
        throw err;
    }
};

/**
 * 아이디 중복 체크
 */
export const checkUsernameDuplicate = async (username) => {
    try {
        const res = await apiGet(`/api/auth/check-username?username=${encodeURIComponent(username)}`);
        return !!res.isDuplicate;
    } catch (err) {
        console.error('아이디 중복 체크 오류:', err);
        return false;
    }
};

/**
 * 이메일 중복 체크
 */
export const checkEmailDuplicate = async (email) => {
    try {
        const res = await apiGet(`/api/auth/check-email?email=${encodeURIComponent(email)}`);
        return !!res.isDuplicate;
    } catch (err) {
        console.error('이메일 중복 체크 오류:', err);
        return false;
    }
};

/**
 * 닉네임 중복 체크
 */
export const checkNicknameDuplicate = async (nickname) => {
    try {
        const res = await apiGet(`/api/auth/check-nickname?nickname=${encodeURIComponent(nickname)}`);
        return !!res.isDuplicate;
    } catch (err) {
        console.error('닉네임 중복 체크 오류:', err);
        return false;
    }
};
