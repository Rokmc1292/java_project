import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import {
  getPredictionsByMatch,
  getPredictionStatistics,
  createPrediction,
  likePrediction,
  dislikePrediction,
  checkUserPrediction
} from '../api/prediction';
import { getMatch } from '../api/match';
import { getUserData, isLoggedIn } from '../api/api';
import '../styles/Predictions.css';

// 환경변수에서 API Base URL 가져오기
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 승부예측 상세 페이지
 * - 경기 정보 표시
 * - 홈승/무승부/원정승 선택
 * - 실시간 투표 비율
 * - 코멘트 작성 및 목록
 * - 추천/비추천 기능
 * 
 * 파일 위치: frontend/src/pages/PredictionDetail.jsx
 */
function PredictionDetail() {
  const { matchId } = useParams(); // URL에서 경기 ID 가져오기
  const navigate = useNavigate();
  const [match, setMatch] = useState(null); // 경기 정보
  const [statistics, setStatistics] = useState(null); // 예측 통계
  const [predictions, setPredictions] = useState([]); // 예측 목록
  const [loading, setLoading] = useState(true); // 로딩 상태
  const [error, setError] = useState(null); // 에러 메시지

  // 예측 입력 상태
  const [selectedResult, setSelectedResult] = useState(''); // 선택된 결과 (HOME, DRAW, AWAY)
  const [comment, setComment] = useState(''); // 코멘트
  const [submitting, setSubmitting] = useState(false); // 제출 중 상태
  const [hasPredicted, setHasPredicted] = useState(false); // 이미 예측했는지 여부

  // 페이지네이션
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // 데이터 로드
  useEffect(() => {
    loadData();
  }, [matchId, page]);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      console.log('경기 정보 로드 시작 - matchId:', matchId);

      // 경기 정보 로드
      const matchData = await getMatch(matchId);
      console.log('경기 정보 로드 성공:', matchData);
      setMatch(matchData);

      // 예측 통계 로드
      console.log('예측 통계 로드 시작');
      const statsData = await getPredictionStatistics(matchId);
      console.log('예측 통계 로드 성공:', statsData);
      setStatistics(statsData);

      // 예측 목록 로드
      console.log('예측 목록 로드 시작');
      const predictionsData = await getPredictionsByMatch(matchId, page, 20);
      console.log('예측 목록 로드 성공:', predictionsData);
      setPredictions(predictionsData.content || []);
      setTotalPages(predictionsData.totalPages || 0);

      // 이미 예측했는지 확인
      if (isLoggedIn()) {
        console.log('예측 여부 확인 시작');
        try {
          const checkData = await checkUserPrediction(matchId);
          console.log('예측 여부 확인 성공:', checkData);
          setHasPredicted(checkData.hasPredicted);
        } catch (checkErr) {
          console.error('예측 여부 확인 실패 (무시):', checkErr);
          // 예측 여부 확인 실패는 무시 (로그인 문제일 수 있음)
          setHasPredicted(false);
        }
      }
    } catch (err) {
      console.error('데이터 로드 실패:', err);
      setError(`데이터를 불러오는데 실패했습니다. ${err.message || ''}`);
    } finally {
      setLoading(false);
    }
  };

  // 예측 제출
  const handleSubmit = async () => {
    if (!isLoggedIn()) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    if (!selectedResult) {
      alert('예측 결과를 선택해주세요.');
      return;
    }

    if (!comment.trim()) {
      alert('코멘트를 작성해주세요.');
      return;
    }

    if (comment.trim().length < 2) {
      alert('코멘트는 최소 2자 이상 작성해주세요.');
      return;
    }

    setSubmitting(true);
    try {
      await createPrediction(matchId, selectedResult, comment);
      alert('예측이 등록되었습니다!');
      loadData(); // 데이터 새로고침
      setSelectedResult('');
      setComment('');
      setHasPredicted(true);
    } catch (err) {
      console.error('예측 제출 실패:', err);
      alert(err.message || '예측 제출에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  // 코멘트 추천
  const handleLike = async (predictionId) => {
    if (!isLoggedIn()) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    try {
      await likePrediction(predictionId);
      alert('추천했습니다.');
      loadData();
    } catch (err) {
      alert(err.message || '추천에 실패했습니다.');
    }
  };

  // 코멘트 비추천
  const handleDislike = async (predictionId) => {
    if (!isLoggedIn()) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    try {
      await dislikePrediction(predictionId);
      alert('비추천했습니다.');
      loadData();
    } catch (err) {
      alert(err.message || '비추천에 실패했습니다.');
    }
  };

  // 날짜 포맷팅
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일 ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`;
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

  if (loading) {
    return (
      <div>
        <Navbar />
        <div className="loading">
          <div className="spinner"></div>
          <p>로딩 중...</p>
        </div>
      </div>
    );
  }

  if (error || !match) {
    return (
      <div>
        <Navbar />
        <div className="predictions-container">
          <div className="error">
            {error || '경기 정보를 찾을 수 없습니다.'}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <Navbar />
      <div className="predictions-container">
        {/* 뒤로가기 버튼 */}
        <button onClick={() => navigate('/predictions')} className="back-btn">
          ← 목록으로
        </button>

        {/* 경기 정보 카드 */}
        <div className="match-detail-card">
          {/* 경기 헤더 */}
          <div className="match-detail-header">
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

          {/* 팀 대진 */}
          <div className="match-detail-teams">
            {/* 홈팀 */}
            <div className="team-detail">
              {match.teams?.home?.logo ? (
                <img
                  src={`${API_BASE_URL}/${match.teams.home.logo}`}
                  alt={match.teams.home.name}
                  className="team-logo-large"
                />
              ) : (
                <div className="team-logo-large-placeholder">🏠</div>
              )}
              <div className="team-name-large">
                {match.teams?.home?.name || '홈팀'}
              </div>
            </div>

            {/* VS */}
            <div className="vs-large">VS</div>

            {/* 원정팀 */}
            <div className="team-detail">
              {match.teams?.away?.logo ? (
                <img
                  src={`${API_BASE_URL}/${match.teams.away.logo}`}
                  alt={match.teams.away.name}
                  className="team-logo-large"
                />
              ) : (
                <div className="team-logo-large-placeholder">✈️</div>
              )}
              <div className="team-name-large">
                {match.teams?.away?.name || '원정팀'}
              </div>
            </div>
          </div>

          {/* 경기장 정보 */}
          <div className="match-detail-venue">
            📍 {match.detail?.venue || '경기장 정보 없음'}
          </div>
        </div>

        {/* 예측 통계 */}
        {statistics && (
          <div className="statistics-card">
            <h2 className="section-title">📊 실시간 예측 현황</h2>

            <div className="total-votes">
              총 {statistics.totalVotes}명 참여
            </div>

            {/* 홈승 비율 */}
            <div className="vote-bar-container">
              <div className="vote-bar-header">
                <span className="vote-label">홈팀 승</span>
                <span className="vote-percentage home">
                  {statistics.homePercentage.toFixed(1)}% ({statistics.homeVotes}명)
                </span>
              </div>
              <div className="vote-bar">
                <div
                  className="vote-bar-fill home"
                  style={{ width: `${statistics.homePercentage}%` }}
                ></div>
              </div>
              <div className="expected-points">
                적중 시: <span className="points-win">+{statistics.homeWinPoints}점</span> /
                실패 시: <span className="points-lose">{statistics.homeLosePoints}점</span>
              </div>
            </div>

            {/* 무승부 비율 */}
            <div className="vote-bar-container">
              <div className="vote-bar-header">
                <span className="vote-label">무승부</span>
                <span className="vote-percentage draw">
                  {statistics.drawPercentage.toFixed(1)}% ({statistics.drawVotes}명)
                </span>
              </div>
              <div className="vote-bar">
                <div
                  className="vote-bar-fill draw"
                  style={{ width: `${statistics.drawPercentage}%` }}
                ></div>
              </div>
              <div className="expected-points">
                적중 시: <span className="points-win">+{statistics.drawWinPoints}점</span> /
                실패 시: <span className="points-lose">{statistics.drawLosePoints}점</span>
              </div>
            </div>

            {/* 원정승 비율 */}
            <div className="vote-bar-container">
              <div className="vote-bar-header">
                <span className="vote-label">원정팀 승</span>
                <span className="vote-percentage away">
                  {statistics.awayPercentage.toFixed(1)}% ({statistics.awayVotes}명)
                </span>
              </div>
              <div className="vote-bar">
                <div
                  className="vote-bar-fill away"
                  style={{ width: `${statistics.awayPercentage}%` }}
                ></div>
              </div>
              <div className="expected-points">
                적중 시: <span className="points-win">+{statistics.awayWinPoints}점</span> /
                실패 시: <span className="points-lose">{statistics.awayLosePoints}점</span>
              </div>
            </div>
          </div>
        )}

        {/* 예측 참여 폼 */}
        {!hasPredicted && (
          <div className="prediction-form-card">
            <h2 className="section-title">🎯 나의 예측</h2>

            {/* 결과 선택 버튼 */}
            <div className="result-buttons">
              <button
                onClick={() => setSelectedResult('HOME')}
                className={`result-btn home ${selectedResult === 'HOME' ? 'active' : ''}`}
              >
                홈팀 승
              </button>

              <button
                onClick={() => setSelectedResult('DRAW')}
                className={`result-btn draw ${selectedResult === 'DRAW' ? 'active' : ''}`}
              >
                무승부
              </button>

              <button
                onClick={() => setSelectedResult('AWAY')}
                className={`result-btn away ${selectedResult === 'AWAY' ? 'active' : ''}`}
              >
                원정팀 승
              </button>
            </div>

            {/* 코멘트 입력 */}
            <div className="comment-input-section">
              <label className="comment-label">
                코멘트 작성 (필수, 최소 2자)
              </label>
              <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder="예측 이유를 작성해주세요..."
                className="comment-textarea"
              />
              <div className={`character-count ${comment.length < 2 ? 'invalid' : 'valid'}`}>
                {comment.length} / 최소 2자
              </div>
            </div>

            {/* 제출 버튼 */}
            <button
              onClick={handleSubmit}
              disabled={submitting || !selectedResult || comment.trim().length < 2}
              className="submit-btn"
            >
              {submitting ? '제출 중...' : '예측 제출하기'}
            </button>

            <div className="warning-box">
              ⚠️ 예측은 제출 후 수정할 수 없습니다. 신중하게 선택해주세요!
            </div>
          </div>
        )}

        {/* 이미 예측한 경우 */}
        {hasPredicted && (
          <div className="already-predicted">
            <div className="check-icon">✅</div>
            <div className="already-predicted-title">
              이미 예측을 완료했습니다!
            </div>
            <div className="already-predicted-desc">
              경기 종료 후 결과를 확인하세요.
            </div>
          </div>
        )}

        {/* 코멘트 목록 */}
        <div className="comments-card">
          <h2 className="section-title">
            💬 예측 코멘트 ({statistics?.totalVotes || 0})
          </h2>

          {predictions.length === 0 ? (
            <div className="no-comments">
              아직 예측이 없습니다. 첫 번째로 예측해보세요!
            </div>
          ) : (
            <div className="comments-list">
              {predictions.map((prediction) => (
                <div key={prediction.predictionId} className="comment-item">
                  {/* 코멘트 헤더 */}
                  <div className="comment-header">
                    <div className="comment-user-info">
                      <span className="comment-nickname">
                        {getTierIcon(prediction.userTier)} {prediction.nickname}
                      </span>
                      <span className={`prediction-badge ${prediction.predictedResult.toLowerCase()}`}>
                        {prediction.predictedResult === 'HOME' ? '홈팀 승' :
                         prediction.predictedResult === 'DRAW' ? '무승부' : '원정팀 승'}
                      </span>
                      {prediction.isCorrect !== null && (
                        <span className={`result-badge ${prediction.isCorrect ? 'correct' : 'wrong'}`}>
                          {prediction.isCorrect ? '✅ 적중' : '❌ 빗나감'}
                        </span>
                      )}
                    </div>
                    <div className="comment-time">
                      {new Date(prediction.createdAt).toLocaleString('ko-KR')}
                    </div>
                  </div>

                  {/* 코멘트 내용 */}
                  <div className="comment-content">
                    {prediction.comment}
                  </div>

                  {/* 추천/비추천 버튼 */}
                  <div className="comment-actions">
                    <button
                      onClick={() => handleLike(prediction.predictionId)}
                      className="action-btn like"
                    >
                      👍 추천 {prediction.likeCount}
                    </button>
                    <button
                      onClick={() => handleDislike(prediction.predictionId)}
                      className="action-btn dislike"
                    >
                      👎 비추천 {prediction.dislikeCount}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* 페이지네이션 */}
          {totalPages > 1 && (
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
        </div>
      </div>
    </div>
  );
}

export default PredictionDetail;