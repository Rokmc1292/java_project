import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPredictableMatches } from '../api/prediction';
import { isLoggedIn } from '../api/api';

// 환경변수에서 API Base URL 가져오기
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 승부예측 메인 페이지
 * - 예측 가능한 경기 목록 (일주일 이내 경기)
 * - 종목별 필터링
 * - 마감 시간 카운트다운
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

  // 날짜 포맷팅
  const formatDateTime = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="bg-gray-900 text-white min-h-screen">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 페이지 헤더 */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold mb-2">🎯 승부예측</h1>
          <p className="text-gray-400">일주일 이내 경기를 예측할 수 있습니다. 코멘트와 함께 예측해보세요!</p>
        </div>

        {/* 안내 메시지 */}
        <div className="bg-blue-500/20 border border-blue-500 rounded-lg p-6 mb-8">
          <p className="font-bold text-blue-400 mb-3">💡 승부예측 안내</p>
          <ul className="space-y-2 text-sm text-gray-300">
            <li>• 현재부터 일주일 이내 경기를 예측할 수 있습니다</li>
            <li>• 점수는 선택 비율과 참여 인원에 따라 배당률처럼 차등 지급됩니다</li>
            <li>• 참여자가 많을수록, 적게 선택된 결과를 맞출수록 더 많은 점수를 획득합니다</li>
            <li>• 코멘트 작성은 필수입니다 (최소 2자)</li>
            <li>• 제출 후 수정할 수 없으니 신중하게 선택하세요</li>
          </ul>
        </div>

        {/* 종목 필터 탭 */}
        <div className="flex flex-wrap gap-3 mb-8">
          {sports.map((sport) => (
            <button
              key={sport.value}
              className={`px-6 py-3 rounded-lg font-semibold transition-all ${
                selectedSport === sport.value
                  ? 'bg-blue-500 text-white shadow-lg transform scale-105'
                  : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
              }`}
              onClick={() => {
                setSelectedSport(sport.value);
                setPage(0);
              }}
            >
              <span className="mr-2">{sport.icon}</span>
              <span>{sport.label}</span>
            </button>
          ))}
        </div>

        {/* 로딩 상태 */}
        {loading && (
          <div className="text-center py-16">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
            <p className="mt-4 text-gray-400">로딩 중...</p>
          </div>
        )}

        {/* 에러 메시지 */}
        {error && (
          <div className="bg-red-500/20 border border-red-500 rounded-lg p-6 text-center">
            <p className="text-red-400">❌ {error}</p>
          </div>
        )}

        {/* 경기 목록 */}
        {!loading && !error && matches.length === 0 && (
          <div className="bg-gray-800/50 rounded-lg p-16 text-center">
            <p className="text-gray-400 text-lg">📭 예측 가능한 경기가 없습니다.</p>
          </div>
        )}

        {!loading && !error && matches.length > 0 && (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {matches.map((match) => (
                <div
                  key={match.matchId}
                  className="bg-white rounded-lg p-6 shadow-xl cursor-pointer transform transition hover:scale-105 hover:shadow-2xl"
                  onClick={() => handleMatchClick(match.matchId)}
                >
                  {/* 경기 정보 헤더 */}
                  <div className="flex items-center justify-between mb-4 pb-3 border-b border-gray-200">
                    <div className="flex items-center gap-2">
                      {match.league?.logo && (
                        <img
                          src={`${API_BASE_URL}/${match.league.logo}`}
                          alt={match.league.name}
                          className="w-5 h-5 object-contain"
                        />
                      )}
                      <span className="text-xs font-semibold text-gray-600">
                        {match.league?.name || '리그'}
                      </span>
                    </div>
                  </div>

                  {/* 팀 대진 정보 */}
                  <div className="space-y-4 mb-4">
                    {/* 홈팀 */}
                    <div className="flex items-center gap-3">
                      {match.teams?.home?.logo ? (
                        <img
                          src={`${API_BASE_URL}/${match.teams.home.logo}`}
                          alt={match.teams.home.name}
                          className={`w-12 h-12 object-cover ${match.sportType === 'MMA' ? 'rounded-full' : 'rounded-lg'}`}
                        />
                      ) : (
                        <div className="w-12 h-12 rounded-lg bg-blue-100 flex items-center justify-center text-blue-600 font-bold">
                          H
                        </div>
                      )}
                      <span className="font-bold text-gray-900 flex-1">
                        {match.teams?.home?.name || '홈팀'}
                      </span>
                    </div>

                    {/* VS */}
                    <div className="text-center text-2xl font-bold text-gray-400">VS</div>

                    {/* 원정팀 */}
                    <div className="flex items-center gap-3">
                      {match.teams?.away?.logo ? (
                        <img
                          src={`${API_BASE_URL}/${match.teams.away.logo}`}
                          alt={match.teams.away.name}
                          className={`w-12 h-12 object-cover ${match.sportType === 'MMA' ? 'rounded-full' : 'rounded-lg'}`}
                        />
                      ) : (
                        <div className="w-12 h-12 rounded-lg bg-red-100 flex items-center justify-center text-red-600 font-bold">
                          A
                        </div>
                      )}
                      <span className="font-bold text-gray-900 flex-1">
                        {match.teams?.away?.name || '원정팀'}
                      </span>
                    </div>
                  </div>

                  {/* 경기 상세 정보 */}
                  <div className="space-y-2 text-sm text-gray-600 pt-3 border-t border-gray-200">
                    <div className="flex items-center gap-2">
                      <span>📍</span>
                      <span>{match.detail?.venue || '경기장 정보 없음'}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span>🕐</span>
                      <span>{formatDateTime(match.detail?.matchDate)}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span>⏰</span>
                      <span className="font-semibold text-red-500">
                        마감까지 {getTimeUntilMatch(match.detail?.matchDate)}
                      </span>
                    </div>
                  </div>

                  {/* 예측 버튼 */}
                  <button className="w-full mt-4 py-3 bg-yellow-500 hover:bg-yellow-600 text-black font-bold rounded-lg transition">
                    예측 참여하기 →
                  </button>
                </div>
              ))}
            </div>

            {/* 페이지네이션 */}
            {totalPages > 1 && (
              <div className="flex justify-center items-center gap-2 mt-8">
                <button
                  onClick={() => setPage(Math.max(0, page - 1))}
                  disabled={page === 0}
                  className={`px-4 py-2 rounded-lg font-semibold transition ${
                    page === 0
                      ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                      : 'bg-gray-700 text-white hover:bg-gray-600'
                  }`}
                >
                  이전
                </button>

                <span className="px-4 py-2 text-gray-400">
                  {page + 1} / {totalPages}
                </span>

                <button
                  onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                  disabled={page >= totalPages - 1}
                  className={`px-4 py-2 rounded-lg font-semibold transition ${
                    page >= totalPages - 1
                      ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                      : 'bg-gray-700 text-white hover:bg-gray-600'
                  }`}
                >
                  다음
                </button>
              </div>
            )}
          </>
        )}

        {/* 랭킹 바로가기 버튼 */}
        <div className="text-center mt-8">
          <button
            onClick={() => navigate('/predictions/ranking')}
            className="px-8 py-3 bg-gradient-to-r from-yellow-500 to-orange-500 hover:from-yellow-600 hover:to-orange-600 text-black font-bold rounded-lg transition shadow-lg"
          >
            🏆 예측 랭킹 보기
          </button>
        </div>
      </div>
    </div>
  );
}

export default Predictions;
