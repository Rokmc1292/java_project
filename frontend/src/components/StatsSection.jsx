import React from 'react';
import '../styles/StatsSection.css';

/**
 * 통계 섹션 컴포넌트
 * 예측 통계와 최근 10경기 결과 표시
 */
const StatsSection = ({ stats, recentResults }) => {
  /**
   * 승률에 따른 색상 반환
   */
  const getWinRateColor = (rate) => {
    if (rate >= 70) return '#4CAF50'; // 초록색
    if (rate >= 50) return '#FFC107'; // 노란색
    return '#F44336'; // 빨간색
  };

  return (
    <div className="stats-section">
      {/* 예측 통계 카드 */}
      <div className="stats-card">
        <h3>예측 통계</h3>
        <div className="stats-grid">
          {/* 총 예측 횟수 */}
          <div className="stat-item">
            <div className="stat-icon">📊</div>
            <div className="stat-content">
              <p className="stat-label">총 예측</p>
              <p className="stat-value">{stats.totalPredictions}회</p>
            </div>
          </div>

          {/* 성공한 예측 */}
          <div className="stat-item success">
            <div className="stat-icon">✅</div>
            <div className="stat-content">
              <p className="stat-label">성공</p>
              <p className="stat-value">{stats.correctPredictions}회</p>
            </div>
          </div>

          {/* 실패한 예측 */}
          <div className="stat-item fail">
            <div className="stat-icon">❌</div>
            <div className="stat-content">
              <p className="stat-label">실패</p>
              <p className="stat-value">{stats.incorrectPredictions}회</p>
            </div>
          </div>

          {/* 승률 */}
          <div className="stat-item winrate">
            <div className="stat-icon">🎯</div>
            <div className="stat-content">
              <p className="stat-label">승률</p>
              <p 
                className="stat-value"
                style={{ color: getWinRateColor(stats.winRate) }}
              >
                {stats.winRate}%
              </p>
            </div>
          </div>
        </div>

        {/* 승률 진행 바 */}
        <div className="winrate-bar-container">
          <div 
            className="winrate-bar"
            style={{ 
              width: `${stats.winRate}%`,
              backgroundColor: getWinRateColor(stats.winRate)
            }}
          />
        </div>
      </div>

      {/* 최근 10경기 결과 */}
      <div className="recent-results-card">
        <h3>최근 10경기 전적</h3>
        {recentResults.length === 0 ? (
          <p className="no-results">아직 완료된 예측이 없습니다.</p>
        ) : (
          <div className="results-grid">
            {recentResults.map((result) => (
              <div 
                key={result.predictionId}
                className={`result-badge ${result.isCorrect ? 'correct' : 'incorrect'}`}
                title={`${result.homeTeam} vs ${result.awayTeam}`}
              >
                {result.isCorrect ? 'O' : 'X'}
              </div>
            ))}
          </div>
        )}
        <p className="results-guide">
          최근 경기부터 순서대로 표시됩니다 (O: 성공, X: 실패)
        </p>
      </div>

      {/* 통계 분석 */}
      {stats.totalPredictions > 0 && (
        <div className="stats-analysis-card">
          <h4>📈 나의 예측 분석</h4>
          <ul className="analysis-list">
            <li>
              {stats.winRate >= 60 ? 
                '🎉 훌륭한 예측력을 보여주고 있습니다!' : 
                stats.winRate >= 50 ?
                '👍 평균 이상의 예측 실력입니다!' :
                '💪 더 신중한 분석이 필요합니다!'}
            </li>
            <li>
              총 {stats.totalPredictions}경기에 참여하여 {stats.correctPredictions}경기를 맞췄습니다.
            </li>
            {stats.winRate >= 70 && (
              <li>⭐ 승률 70% 이상 달성! 예측 고수입니다!</li>
            )}
          </ul>
        </div>
      )}
    </div>
  );
};

export default StatsSection;