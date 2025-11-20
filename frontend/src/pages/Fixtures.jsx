import { useState, useEffect } from 'react';

// 환경변수에서 API Base URL 가져오기
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 경기 일정 페이지
 * 종목별 경기 일정을 날짜별로 조회
 */
function Fixtures() {
  // 상태 관리
  const [selectedSport, setSelectedSport] = useState('ALL');  // 선택된 종목
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);  // 선택된 날짜
  const [matches, setMatches] = useState([]);  // 경기 목록
  const [loading, setLoading] = useState(false);  // 로딩 상태
  const [error, setError] = useState(null);  // 에러 상태

  // 종목 목록
  const sports = [
    { value: 'ALL', label: '전체', icon: '⚽🏀⚾🎮🥊' },
    { value: 'FOOTBALL', label: '축구', icon: '⚽' },
    { value: 'BASKETBALL', label: '농구', icon: '🏀' },
    { value: 'BASEBALL', label: '야구', icon: '⚾' },
    { value: 'LOL', label: '롤', icon: '🎮' },
    { value: 'MMA', label: 'UFC', icon: '🥊' }
  ];

  // 경기 조회
  const fetchMatches = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/matches?date=${selectedDate}&sport=${selectedSport}`
      );

      if (!response.ok) {
        throw new Error('경기 데이터를 불러오는데 실패했습니다.');
      }

      const data = await response.json();
      setMatches(data);
    } catch (err) {
      console.error('경기 조회 에러:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // 종목 또는 날짜 변경 시 경기 조회
  useEffect(() => {
    fetchMatches();
  }, [selectedSport, selectedDate]);

  // 경기 상태 한글 변환
  const getStatusText = (status) => {
    switch (status) {
      case 'SCHEDULED':
        return '예정';
      case 'LIVE':
        return '진행중';
      case 'FINISHED':
        return '종료';
      case 'POSTPONED':
        return '연기';
      default:
        return status;
    }
  };

  // 경기 상태별 스타일 클래스
  const getStatusBadgeStyle = (status) => {
    switch (status) {
      case 'SCHEDULED':
        return 'bg-blue-500 text-white';
      case 'LIVE':
        return 'bg-red-500 text-white animate-pulse';
      case 'FINISHED':
        return 'bg-gray-600 text-white';
      case 'POSTPONED':
        return 'bg-yellow-500 text-white';
      default:
        return 'bg-gray-500 text-white';
    }
  };

  // 날짜 포맷팅
  const formatDateTime = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const dateStr = date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
    const timeStr = date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
    return `${dateStr} ${timeStr}`;
  };

  // 날짜 변경 (이전/다음 날)
  const changeDate = (days) => {
    const currentDate = new Date(selectedDate);
    currentDate.setDate(currentDate.getDate() + days);
    setSelectedDate(currentDate.toISOString().split('T')[0]);
  };

  // 승/패 팀 스타일 결정
  const getTeamStyle = (match, isHome) => {
    if (match.detail.status !== 'FINISHED') {
      return {};
    }

    // MMA 경기인 경우 winner 필드로 승자 판단
    if (match.sportType === 'MMA' && match.detail.winner) {
      const fighterName = isHome ? match.teams.home.name : match.teams.away.name;
      const isWinner = match.detail.winner === fighterName;

      return {
        color: isWinner ? '#10b981' : '#ef4444',
        fontWeight: 'bold'
      };
    }

    // 일반 경기인 경우 점수로 승자 판단
    if (!match.score) {
      return {};
    }

    const homeScore = match.score.home;
    const awayScore = match.score.away;

    if (homeScore == null || awayScore == null) {
      return {};
    }

    const isWinner = isHome
      ? homeScore > awayScore
      : awayScore > homeScore;

    return {
      color: isWinner ? '#10b981' : '#ef4444',
      fontWeight: 'bold'
    };
  };

  return (
    <div className="bg-gray-900 text-white min-h-screen">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 헤더 */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold mb-2">📅 경기 일정</h1>
          <p className="text-gray-400">오늘의 스포츠 경기를 확인하세요</p>
        </div>

        {/* 필터 섹션 */}
        <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6 mb-8">
          {/* 종목 선택 */}
          <div className="flex flex-wrap gap-3 mb-6">
            {sports.map((sport) => (
              <button
                key={sport.value}
                className={`px-6 py-3 rounded-lg font-semibold transition-all ${
                  selectedSport === sport.value
                    ? 'bg-blue-500 text-white shadow-lg transform scale-105'
                    : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
                }`}
                onClick={() => setSelectedSport(sport.value)}
              >
                <span className="mr-2">{sport.icon}</span>
                <span>{sport.label}</span>
              </button>
            ))}
          </div>

          {/* 날짜 선택 */}
          <div className="flex items-center justify-center gap-4">
            <button
              className="px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded-lg transition"
              onClick={() => changeDate(-1)}
            >
              ◀ 이전
            </button>
            <input
              type="date"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              className="px-4 py-2 bg-gray-700 text-white rounded-lg border-none focus:ring-2 focus:ring-blue-500"
            />
            <button
              className="px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded-lg transition"
              onClick={() => changeDate(1)}
            >
              다음 ▶
            </button>
          </div>
        </div>

        {/* 경기 목록 */}
        <div>
          {loading && (
            <div className="text-center py-16">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
              <p className="mt-4 text-gray-400">경기 정보를 불러오는 중...</p>
            </div>
          )}

          {error && (
            <div className="bg-red-500/20 border border-red-500 rounded-lg p-6 text-center">
              <p className="text-red-400">❌ {error}</p>
              <button
                onClick={fetchMatches}
                className="mt-4 px-6 py-2 bg-red-500 hover:bg-red-600 rounded-lg transition"
              >
                다시 시도
              </button>
            </div>
          )}

          {!loading && !error && matches.length === 0 && (
            <div className="bg-gray-800/50 rounded-lg p-16 text-center">
              <p className="text-gray-400 text-lg">📅 해당 날짜에 예정된 경기가 없습니다.</p>
            </div>
          )}

          {!loading && !error && matches.length > 0 && (
            <div className="space-y-6">
              {matches.map((match) => (
                <div key={match.matchId} className="bg-white rounded-lg p-6 shadow-xl">
                  {/* 리그 정보 */}
                  <div className="flex items-center justify-between mb-4 pb-3 border-b border-gray-200">
                    <div className="flex items-center gap-3">
                      {match.league.logo && (
                        <img
                          src={`${API_BASE_URL}/${match.league.logo}`}
                          alt={match.league.name}
                          className="w-6 h-6 object-contain"
                        />
                      )}
                      <span className="font-semibold text-gray-700">{match.league.name}</span>
                    </div>
                    <span className={`px-3 py-1 rounded-full text-sm font-bold ${getStatusBadgeStyle(match.detail.status)}`}>
                      {getStatusText(match.detail.status)}
                    </span>
                  </div>

                  {/* 경기 정보 */}
                  <div className="flex items-center justify-between">
                    {/* 홈팀/파이터1 */}
                    <div className="flex items-center gap-4 flex-1">
                      {match.teams.home.logo && (
                        <img
                          src={`${API_BASE_URL}/${match.teams.home.logo}`}
                          alt={match.teams.home.name}
                          className={`w-16 h-16 object-cover ${match.sportType === 'MMA' ? 'rounded-full' : 'rounded-lg'}`}
                        />
                      )}
                      <div>
                        <span className="text-lg font-bold text-gray-900" style={getTeamStyle(match, true)}>
                          {match.teams.home.name}
                        </span>
                        {match.teams.home.record && (
                          <span className="block text-sm text-gray-500">({match.teams.home.record})</span>
                        )}
                        {match.teams.home.weightClass && (
                          <span className="block text-xs text-gray-400">{match.teams.home.weightClass}</span>
                        )}
                      </div>
                    </div>

                    {/* 점수 또는 VS */}
                    <div className="text-center px-8">
                      {match.detail.status === 'FINISHED' && match.score ? (
                        <div className="text-3xl font-bold text-gray-900">
                          <span>{match.score.home}</span>
                          <span className="mx-2 text-gray-400">-</span>
                          <span>{match.score.away}</span>
                        </div>
                      ) : match.detail.status === 'LIVE' && match.score ? (
                        <div>
                          <div className="text-3xl font-bold text-gray-900">
                            <span>{match.score.home}</span>
                            <span className="mx-2 text-gray-400">-</span>
                            <span>{match.score.away}</span>
                          </div>
                          <span className="inline-block mt-2 px-3 py-1 bg-red-500 text-white text-xs font-bold rounded-full animate-pulse">
                            LIVE
                          </span>
                        </div>
                      ) : (
                        <div className="text-3xl font-bold text-gray-400">VS</div>
                      )}
                    </div>

                    {/* 원정팀/파이터2 */}
                    <div className="flex items-center gap-4 flex-1 justify-end">
                      <div className="text-right">
                        <span className="text-lg font-bold text-gray-900" style={getTeamStyle(match, false)}>
                          {match.teams.away.name}
                        </span>
                        {match.teams.away.record && (
                          <span className="block text-sm text-gray-500">({match.teams.away.record})</span>
                        )}
                        {match.teams.away.weightClass && (
                          <span className="block text-xs text-gray-400">{match.teams.away.weightClass}</span>
                        )}
                      </div>
                      {match.teams.away.logo && (
                        <img
                          src={`${API_BASE_URL}/${match.teams.away.logo}`}
                          alt={match.teams.away.name}
                          className={`w-16 h-16 object-cover ${match.sportType === 'MMA' ? 'rounded-full' : 'rounded-lg'}`}
                        />
                      )}
                    </div>
                  </div>

                  {/* 추가 정보 */}
                  <div className="mt-4 pt-3 border-t border-gray-200 flex flex-wrap gap-4 text-sm text-gray-600">
                    <span>🕐 {formatDateTime(match.detail.matchDate)}</span>
                    {match.detail.venue && (
                      <span>📍 {match.detail.venue}</span>
                    )}
                    {match.detail.eventName && (
                      <span>🎫 {match.detail.eventName}</span>
                    )}
                    {match.detail.winner && (
                      <span className="text-green-600 font-semibold">
                        🏆 승자: {match.detail.winner} ({match.detail.method}, R{match.detail.round})
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Fixtures;
