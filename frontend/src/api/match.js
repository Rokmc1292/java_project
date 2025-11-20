/**
 * 경기 관련 API 함수들
 * 
 * 파일 위치: frontend/src/api/match.js
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 특정 경기 상세 정보 조회
 */
export const getMatch = async (matchId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/matches/${matchId}`, {
      method: 'GET',
      credentials: 'include', // 세션 쿠키 전송
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error('경기 정보를 불러오는데 실패했습니다.');
    }

    return await response.json();
  } catch (error) {
    console.error('경기 상세 조회 실패:', error);
    throw error;
  }
};

/**
 * 날짜별 경기 조회
 */
export const getMatchesByDate = async (date, sport = null) => {
  try {
    const sportParam = sport ? `&sport=${sport}` : '';
    const response = await fetch(`${API_BASE_URL}/api/matches?date=${date}${sportParam}`);
    
    if (!response.ok) {
      throw new Error('경기 정보를 불러오는데 실패했습니다.');
    }
    
    return await response.json();
  } catch (error) {
    console.error('날짜별 경기 조회 실패:', error);
    throw error;
  }
};

/**
 * 진행 중인 경기 조회 (LIVE 상태)
 */
export const getLiveMatches = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/live/matches`);
    
    if (!response.ok) {
      throw new Error('실시간 경기 정보를 불러오는데 실패했습니다.');
    }
    
    return await response.json();
  } catch (error) {
    console.error('실시간 경기 조회 실패:', error);
    throw error;
  }
};
/**
 * 오늘의 경기 조회
 */
export const getTodayMatches = async (sport = null) => {
    try {
        const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD 형식
        const sportParam = sport ? `&sport=${sport}` : '';
        const response = await fetch(`${API_BASE_URL}/api/matches?date=${today}${sportParam}`);

        if (!response.ok) {
            throw new Error('오늘의 경기 정보를 불러오는데 실패했습니다.');
        }

        return await response.json();
    } catch (error) {
        console.error('오늘의 경기 조회 실패:', error);
        throw error;
    }
};