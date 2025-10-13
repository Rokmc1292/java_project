// 백엔드 API 기본 URL
const API_BASE_URL = 'http://localhost:8080/api/sports';

/**
 * 오늘 날짜를 YYYY-MM-DD 형식으로 반환
 */
export const getTodayDate = () => {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

/**
 * 날짜 포맷 변환 (YYYY-MM-DD)
 */
export const formatDate = (date) => {
  if (!date) return getTodayDate();
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

/**
 * 경기 일정 조회
 * @param {string} date - 날짜 (YYYY-MM-DD)
 * @param {string} sport - 종목 ('all', 'football', 'basketball', 'baseball', 'esports', 'mma')
 */
export const getAllFixtures = async (date = getTodayDate(), sport = 'all') => {
  try {
    const response = await fetch(
      `${API_BASE_URL}/fixtures?date=${date}&sport=${sport}`
    );

    if (!response.ok) {
      throw new Error('경기 일정 조회 실패');
    }

    const data = await response.json();
    
    // 종목별로 그룹화 (UFC 포함)
    const grouped = {
      football: data.filter(m => m.sport === 'FOOTBALL'),
      basketball: data.filter(m => m.sport === 'BASKETBALL'),
      baseball: data.filter(m => m.sport === 'BASEBALL'),
      esports: data.filter(m => m.sport === 'ESPORTS'),
      ufc: data.filter(m => m.sport === 'UFC')
    };
    
    return grouped;
  } catch (error) {
    console.error('경기 일정 조회 오류:', error);
    return { football: [], basketball: [], baseball: [], esports: [], ufc: [] };
  }
};

export const getAllLiveMatches = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/live`);

    if (!response.ok) {
      throw new Error('실시간 경기 조회 실패');
    }

    const data = await response.json();
    
    const grouped = {
      football: data.filter(m => m.sport === 'FOOTBALL'),
      basketball: data.filter(m => m.sport === 'BASKETBALL'),
      baseball: data.filter(m => m.sport === 'BASEBALL'),
      esports: data.filter(m => m.sport === 'ESPORTS'),
      ufc: data.filter(m => m.sport === 'UFC')
    };
    
    return grouped;
  } catch (error) {
    console.error('실시간 경기 조회 오류:', error);
    return { football: [], basketball: [], baseball: [], esports: [], ufc: [] };
  }
};