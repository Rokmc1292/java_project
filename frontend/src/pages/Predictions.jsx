import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { getPredictableMatches } from '../api/prediction';
import { isLoggedIn } from '../api/api';
import '../styles/Predictions.css';

// 환경변수에서 API Base URL 가져오기
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 승부예측 메인 페이지
 * - 예측 가능한 경기 목록 (일주일 이내 경기)
 * - 종목별 필터링
 * - 마감 시간 카운트다운
 *
 * 파일 위치: frontend/src/pages/Predictions.jsx
 */
function Predictions() {
  const navigate = useNavigate();
  const [matches, setMatches] = useState([]); // 경기 목록
  const [selectedSport, setSelectedSport] = useState('ALL'); // 선택된 종목
  const [loading, setLoading] = useState(true); // 로딩 상태
  const [error, setError] = useState(null); // 에러 메시지
  const [page, setPage] = useState(0); // 현재 페이지
  const [totalPages, setTotalPages] = useState(0); // 전체 페이지 수

  // 종목 목록
  const sports = [
    { value: 'ALL', label: '전체', icon: '⚽🏀⚾🎮🥊' },
    { value: 'FOOTBALL', label: '축구', icon: '⚽' },
    { value: 'BASKETBALL', label: '농구', icon: '🏀' },
    { value: 'BASEBALL', label: '야구', icon: '⚾' },
    { value: 'LOL', label: '롤', icon: '🎮' },
    { value: 'MMA', label: 'UFC', icon: '🥊' }
  ];

  // 경기 목록 로드
  useEffect(() => {
    loadMatches();
  }, [selectedSport, page]);

  const loadMatches = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await getPredictableMatches(selectedSport, page, 20);
      setMatches(response.content || []);
      setTotalPages(response.totalPages || 0);
    } catch (err) {
      console.error('경기 목록 로드 실패:', err);
      setError('경기 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 경기 클릭 - 예측 페이지로 이동
  const handleMatchClick = (matchId) => {
    if (!isLoggedIn()) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }
    navigate(`/predictions/match/${matchId}`);
  };

  // 경기 시작까지 남은 시간 계산
  const getTimeUntilMatch = (matchDate) => {
    const now = new Date();
    const match = new Date(matchDate);
    const diff = match - now;

    if (diff < 0) return '경기 시작';

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

    return `${days}일 ${hours}시간 ${minutes}분`;
  };

  // 날짜 포맷팅 - ISO 형식을 일반 형식으로 변환
  const formatDate = (dateString) => {
    // "2025-11-08T09:00:00" → "2025-11-08 09:00:00"
    if (!dateString) return '';
    return dateString.replace('T', ' ');
  };

  return (
    <div>
      <Navbar />
      <div className="predictions-container">
        {/* 페이지 헤더 */}
        <div className="predictions-header">
          <h1>⚽ 승부예측</h1>
          <p>일주일 이내 경기를 예측할 수 있습니다. 코멘트와 함께 예측해보세요!</p>
        </div>

        {/* 안내 메시지 */}
        <div className="info-box">
          <p className="info-title">💡 승부예측 안내</p>
          <ul className="info-list">
            <li>현재부터 일주일 이내 경기를 예측할 수 있습니다</li>
            <li>점수는 선택 비율과 참여 인원에 따라 배당률처럼 차등 지급됩니다</li>
            <li>참여자가 많을수록, 적게 선택된 결과를 맞출수록 더 많은 점수를 획득합니다</li>
            <li>코멘트 작성은 필수입니다 (최소 2자)</li>
            <li>제출 후 수정할 수 없으니 신중하게 선택하세요</li>
          </ul>
        </div>

        {/* 종목 필터 탭 */}
        <div className="sport-tabs">
          {sports.map((sport) => (
            <button
              key={sport.value}
              className={`sport-tab ${selectedSport === sport.value ? 'active' : ''}`}
              onClick={() => {
                setSelectedSport(sport.value);
                setPage(0);
              }}
            >
              <span className="sport-icon">{sport.icon}</span>
              <span className="sport-label">{sport.label}</span>
            </button>
          ))}
        </div>

        {/* 로딩 상태 */}
        {loading && (
          <div className="loading">
            <div className="spinner"></div>
            <p>로딩 중...</p>
          </div>
        )}

        {/* 에러 메시지 */}
        {error && (
          <div className="error">
            ❌ {error}
          </div>
        )}

        {/* 경기 목록 */}
        {!loading && !error && matches.length === 0 && (
          <div className="no-matches">
            📭 예측 가능한 경기가 없습니다.
          </div>
        )}

        {!loading && !error && matches.length > 0 && (
          <div className="matches-list">
            {matches.map((match) => (
              <div
                key={match.matchId}
                className="match-card"
                onClick={() => handleMatchClick(match.matchId)}
              >
                {/* 경기 정보 헤더 */}
                <div className="match-header">
                  <div className="league-info">
                    {match.league?.logo && (
                      <img
                        src={`${API_BASE_URL}/${match.league.logo}`}
                        alt={match.league.name}
                        className="league-logo"
                      />
                    )}
                    <span className="league-name">
                      {match.league?.name || '리그'}
                    </span>
                  </div>
                  <span className="match-date">
                    {formatDate(match.detail?.matchDate)}
                  </span>
                </div>

                {/* 팀 대진 정보 */}
                <div className="match-teams">
                  {/* 홈팀 */}
                  <div className="team home-team">
                    {match.teams?.home?.logo ? (
                      <img
                        src={`${API_BASE_URL}/${match.teams.home.logo}`}
                        alt={match.teams.home.name}
                        className="team-logo"
                      />
                    ) : (
                      <div className="team-logo-placeholder">🏠</div>
                    )}
                    <div className="team-name">
                      {match.teams?.home?.name || '홈팀'}
                    </div>
                  </div>

                  {/* VS */}
                  <div className="vs">VS</div>

                  {/* 원정팀 */}
                  <div className="team away-team">
                    <div className="team-name">
                      {match.teams?.away?.name || '원정팀'}
                    </div>
                    {match.teams?.away?.logo ? (
                      <img
                        src={`${API_BASE_URL}/${match.teams.away.logo}`}
                        alt={match.teams.away.name}
                        className="team-logo"
                      />
                    ) : (
                      <div className="team-logo-placeholder">✈️</div>
                    )}
                  </div>
                </div>

                {/* 경기 상세 정보 */}
                <div className="match-info">
                  <span className="venue">
                    📍 {match.detail?.venue || '경기장 정보 없음'}
                  </span>
                  <span className="countdown">
                    ⏰ 마감까지 {getTimeUntilMatch(match.detail?.matchDate)}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 페이지네이션 */}
        {!loading && !error && totalPages > 1 && (
          <div className="pagination">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
              className="pagination-btn"
            >
              이전
            </button>

            <span className="pagination-info">
              {page + 1} / {totalPages}
            </span>

            <button
              onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
              disabled={page >= totalPages - 1}
              className="pagination-btn"
            >
              다음
            </button>
          </div>
        )}

        {/* 랭킹 바로가기 버튼 */}
        <div className="ranking-link">
          <button
            onClick={() => navigate('/predictions/ranking')}
            className="ranking-btn"
          >
            🏆 예측 랭킹 보기
          </button>
        </div>
      </div>
    </div>
  );
}

export default Predictions;