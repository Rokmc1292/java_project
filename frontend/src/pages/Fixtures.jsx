import { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import '../styles/Fixtures.css';

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
  const getStatusClass = (status) => {
    switch (status) {
      case 'SCHEDULED':
        return 'status-scheduled';
      case 'LIVE':
        return 'status-live';
      case 'FINISHED':
        return 'status-finished';
      case 'POSTPONED':
        return 'status-postponed';
      default:
        return '';
    }
  };

  // 날짜 포맷팅 - ISO 형식을 일반 형식으로 변환
  const formatDate = (dateString) => {
    // "2025-11-08T09:00:00" → "2025-11-08 09:00:00"
    if (!dateString) return '';
    return dateString.replace('T', ' ');
  };

  // 날짜 변경 (이전/다음 날)
  const changeDate = (days) => {
    const currentDate = new Date(selectedDate);
    currentDate.setDate(currentDate.getDate() + days);
    setSelectedDate(currentDate.toISOString().split('T')[0]);
  };

  return (
    <div>
      <Navbar />
      <div className="fixtures-container">
        {/* 헤더 */}
        <div className="fixtures-header">
          <h1>경기 일정</h1>
          <p>오늘의 스포츠 경기를 확인하세요</p>
        </div>

      {/* 필터 섹션 */}
      <div className="fixtures-filters">
        {/* 종목 선택 */}
        <div className="sport-tabs">
          {sports.map((sport) => (
            <button
              key={sport.value}
              className={`sport-tab ${selectedSport === sport.value ? 'active' : ''}`}
              onClick={() => setSelectedSport(sport.value)}
            >
              <span className="sport-icon">{sport.icon}</span>
              <span className="sport-label">{sport.label}</span>
            </button>
          ))}
        </div>

        {/* 날짜 선택 */}
        <div className="date-selector">
          <button className="date-nav-btn" onClick={() => changeDate(-1)}>
            ◀ 이전
          </button>
          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="date-input"
          />
          <button className="date-nav-btn" onClick={() => changeDate(1)}>
            다음 ▶
          </button>
        </div>
      </div>

      {/* 경기 목록 */}
      <div className="fixtures-content">
        {loading && (
          <div className="loading">
            <div className="spinner"></div>
            <p>경기 정보를 불러오는 중...</p>
          </div>
        )}

        {error && (
          <div className="error">
            <p>❌ {error}</p>
            <button onClick={fetchMatches}>다시 시도</button>
          </div>
        )}

        {!loading && !error && matches.length === 0 && (
          <div className="no-matches">
            <p>📅 해당 날짜에 예정된 경기가 없습니다.</p>
          </div>
        )}

        {!loading && !error && matches.length > 0 && (
          <div className="matches-list">
            {matches.map((match) => (
              <div key={match.matchId} className="match-card">
                {/* 리그 정보 */}
                <div className="match-league">
                  {match.league.logo && (
                    <img
                      src={`${API_BASE_URL}/${match.league.logo}`}
                      alt={match.league.name}
                      className="league-logo"
                    />
                  )}
                  <span className="league-name">{match.league.name}</span>
                  <span className={`match-status ${getStatusClass(match.detail.status)}`}>
                    {getStatusText(match.detail.status)}
                  </span>
                </div>

                {/* 경기 정보 */}
                <div className="match-info">
                  {/* 홈팀/파이터1 */}
                  <div className="team home-team">
                    {match.teams.home.logo && (
                      <img
                        src={`${API_BASE_URL}/${match.teams.home.logo}`}
                        alt={match.teams.home.name}
                        className="team-logo"
                      />
                    )}
                    <div className="team-details">
                      <span className="team-name">{match.teams.home.name}</span>
                      {match.teams.home.record && (
                        <span className="fighter-record">({match.teams.home.record})</span>
                      )}
                      {match.teams.home.weightClass && (
                        <span className="weight-class">{match.teams.home.weightClass}</span>
                      )}
                    </div>
                  </div>

                  {/* 점수 또는 VS */}
                  <div className="match-score">
                    {match.detail.status === 'FINISHED' && match.score ? (
                      <div className="score">
                        <span className="score-home">{match.score.home}</span>
                        <span className="score-separator">-</span>
                        <span className="score-away">{match.score.away}</span>
                      </div>
                    ) : match.detail.status === 'LIVE' && match.score ? (
                      <div className="score live">
                        <span className="score-home">{match.score.home}</span>
                        <span className="score-separator">-</span>
                        <span className="score-away">{match.score.away}</span>
                        <span className="live-indicator">LIVE</span>
                      </div>
                    ) : (
                      <div className="vs">VS</div>
                    )}
                  </div>

                  {/* 원정팀/파이터2 */}
                  <div className="team away-team">
                    <div className="team-details">
                      <span className="team-name">{match.teams.away.name}</span>
                      {match.teams.away.record && (
                        <span className="fighter-record">({match.teams.away.record})</span>
                      )}
                      {match.teams.away.weightClass && (
                        <span className="weight-class">{match.teams.away.weightClass}</span>
                      )}
                    </div>
                    {match.teams.away.logo && (
                      <img
                        src={`${API_BASE_URL}/${match.teams.away.logo}`}
                        alt={match.teams.away.name}
                        className="team-logo"
                      />
                    )}
                  </div>
                </div>

                {/* 추가 정보 */}
                <div className="match-meta">
                  <span className="match-time">
                    🕐 {formatDate(match.detail.matchDate)}
                  </span>
                  {match.detail.venue && (
                    <span className="match-venue">📍 {match.detail.venue}</span>
                  )}
                  {match.detail.eventName && (
                    <span className="event-name">🎫 {match.detail.eventName}</span>
                  )}
                  {match.detail.winner && (
                    <span className="winner">
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