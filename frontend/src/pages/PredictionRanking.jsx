import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { getOverallRanking, getSportRanking } from '../api/prediction';
import '../styles/Predictions.css';

/**
 * 승부예측 랭킹 페이지
 * - 전체/종목별 랭킹
 * - 티어, 점수, 정확도, 연속 적중 표시
 * 
 * 파일 위치: frontend/src/pages/PredictionRanking.jsx
 */
function PredictionRanking() {
  const navigate = useNavigate();
  const [rankings, setRankings] = useState([]); // 랭킹 목록
  const [selectedSport, setSelectedSport] = useState('ALL'); // 선택된 종목
  const [loading, setLoading] = useState(true); // 로딩 상태
  const [error, setError] = useState(null); // 에러 메시지

  // 종목 목록
  const sports = [
    { value: 'ALL', label: '전체', icon: '⚽🏀⚾🎮🥊' },
    { value: 'FOOTBALL', label: '축구', icon: '⚽' },
    { value: 'BASKETBALL', label: '농구', icon: '🏀' },
    { value: 'BASEBALL', label: '야구', icon: '⚾' },
    { value: 'LOL', label: '롤', icon: '🎮' },
    { value: 'MMA', label: 'UFC', icon: '🥊' }
  ];

  // 랭킹 로드
  useEffect(() => {
    loadRankings();
  }, [selectedSport]);

  const loadRankings = async () => {
    setLoading(true);
    setError(null);
    try {
      let data;
      if (selectedSport === 'ALL') {
        data = await getOverallRanking(100);
      } else {
        data = await getSportRanking(selectedSport, 100);
      }
      // 순위 추가
      const rankedData = data.map((item, index) => ({
        ...item,
        rank: index + 1
      }));
      setRankings(rankedData);
    } catch (err) {
      console.error('랭킹 로드 실패:', err);
      setError('랭킹을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 티어 아이콘
  const getTierIcon = (tier) => {
    const icons = {
      BRONZE: '🥉',
      SILVER: '🥈',
      GOLD: '🥇',
      PLATINUM: '💎',
      DIAMOND: '👑'
    };
    return icons[tier] || '🥉';
  };

  // 순위 메달
  const getRankMedal = (rank) => {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return rank;
  };

  return (
    <div>
      <Navbar />
      <div className="predictions-container">
        {/* 페이지 헤더 */}
        <button onClick={() => navigate('/predictions')} className="back-btn">
          ← 예측 페이지로
        </button>

        <div className="predictions-header">
          <h1>🏆 예측 랭킹</h1>
          <p>티어 점수 기준 상위 랭커들을 확인하세요!</p>
        </div>

        {/* 종목 필터 탭 */}
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

        {/* 랭킹 테이블 */}
        {!loading && !error && (
          <div className="ranking-table">
            {/* 테이블 헤더 */}
            <div className="ranking-header">
              <div className="col-rank">순위</div>
              <div className="col-nickname">닉네임</div>
              <div className="col-tier">티어</div>
              <div className="col-score">점수</div>
              <div className="col-predictions">예측 횟수</div>
              <div className="col-accuracy">정확도</div>
              <div className="col-streak">연속 적중</div>
            </div>

            {/* 랭킹 리스트 */}
            {rankings.length === 0 ? (
              <div className="no-rankings">
                랭킹 데이터가 없습니다.
              </div>
            ) : (
              rankings.map((user) => (
                <div
                  key={user.username}
                  className={`ranking-row ${user.rank <= 3 ? 'top-three' : ''}`}
                >
                  {/* 순위 */}
                  <div className="col-rank rank-medal">
                    {getRankMedal(user.rank)}
                  </div>

                  {/* 닉네임 */}
                  <div className="col-nickname">
                    {user.nickname}
                  </div>

                  {/* 티어 */}
                  <div className="col-tier">
                    {getTierIcon(user.tier)}
                  </div>

                  {/* 점수 */}
                  <div className="col-score tier-score">
                    {user.tierScore}
                  </div>

                  {/* 예측 횟수 */}
                  <div className="col-predictions">
                    {user.totalPredictions}회
                  </div>

                  {/* 정확도 */}
                  <div className={`col-accuracy accuracy-${
                    user.accuracy >= 60 ? 'high' : user.accuracy >= 50 ? 'medium' : 'low'
                  }`}>
                    {user.accuracy.toFixed(1)}%
                  </div>

                  {/* 연속 적중 */}
                  <div className={`col-streak ${user.consecutiveCorrect >= 5 ? 'hot-streak' : ''}`}>
                    {user.consecutiveCorrect >= 5 && '🔥'} {user.consecutiveCorrect}연승
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* 티어 시스템 설명 */}
        <div className="tier-info-card">
          <h2 className="section-title">📊 티어 시스템</h2>

          <div className="tier-list">
            <div className="tier-item">
              <span className="tier-icon">🥉</span>
              <div className="tier-details">
                <div className="tier-name">BRONZE (브론즈)</div>
                <div className="tier-range">0 ~ 99점</div>
              </div>
            </div>

            <div className="tier-item">
              <span className="tier-icon">🥈</span>
              <div className="tier-details">
                <div className="tier-name">SILVER (실버)</div>
                <div className="tier-range">100 ~ 299점</div>
              </div>
            </div>

            <div className="tier-item">
              <span className="tier-icon">🥇</span>
              <div className="tier-details">
                <div className="tier-name">GOLD (골드)</div>
                <div className="tier-range">300 ~ 599점</div>
              </div>
            </div>

            <div className="tier-item">
              <span className="tier-icon">💎</span>
              <div className="tier-details">
                <div className="tier-name">PLATINUM (플래티넘)</div>
                <div className="tier-range">600 ~ 999점</div>
              </div>
            </div>

            <div className="tier-item">
              <span className="tier-icon">👑</span>
              <div className="tier-details">
                <div className="tier-name">DIAMOND (다이아몬드)</div>
                <div className="tier-range">1000점 이상</div>
              </div>
            </div>
          </div>

          <div className="tier-note">
            💡 예측 성공 시 +10점, 실패 시 -10점이 부여됩니다.
          </div>
        </div>
      </div>
    </div>
  );
}

export default PredictionRanking;