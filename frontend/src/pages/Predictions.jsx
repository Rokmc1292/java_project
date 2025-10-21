import { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';

/**
 * 승부예측 페이지
 * 경기별 예측 참여 및 통계 확인
 */
function Predictions() {
  const [matches, setMatches] = useState([]);
  const [selectedSport, setSelectedSport] = useState('ALL');
  const [loading, setLoading] = useState(false);

  const sports = [
    { value: 'ALL', label: '전체' },
    { value: 'FOOTBALL', label: '축구' },
    { value: 'BASKETBALL', label: '농구' },
    { value: 'BASEBALL', label: '야구' },
    { value: 'LOL', label: '롤' },
    { value: 'MMA', label: 'UFC' }
  ];

  // 이틀 후 경기 조회
  const fetchMatches = async () => {
    setLoading(true);
    try {
      const twoDaysLater = new Date();
      twoDaysLater.setDate(twoDaysLater.getDate() + 2);
      const dateStr = twoDaysLater.toISOString().split('T')[0];

      const response = await fetch(
        `http://localhost:8080/api/matches?date=${dateStr}&sport=${selectedSport}`
      );
      const data = await response.json();
      setMatches(data || []);
    } catch (error) {
      console.error('경기 조회 실패:', error);
      setMatches([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMatches();
  }, [selectedSport]);

  return (
    <div>
      <Navbar />
      <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '20px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '20px' }}>
          🎯 승부예측
        </h1>

        <div style={{
          backgroundColor: '#f0f8ff',
          padding: '15px',
          borderRadius: '8px',
          marginBottom: '20px',
          border: '1px solid #b0d4f1'
        }}>
          <p style={{ margin: 0, color: '#0066cc', fontWeight: 'bold' }}>
            ℹ️ 경기 이틀 전(D-2)부터 예측에 참여할 수 있습니다!
          </p>
          <p style={{ margin: '5px 0 0 0', fontSize: '14px', color: '#666' }}>
            예측 성공 시 +10점, 실패 시 -10점이 반영됩니다.
          </p>
        </div>

        {/* 종목 필터 */}
        <div style={{ display: 'flex', gap: '10px', marginBottom: '30px' }}>
          {sports.map((sport) => (
            <button
              key={sport.value}
              onClick={() => setSelectedSport(sport.value)}
              style={{
                padding: '10px 20px',
                backgroundColor: selectedSport === sport.value ? '#646cff' : '#f5f5f5',
                color: selectedSport === sport.value ? 'white' : '#333',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer',
                fontWeight: selectedSport === sport.value ? 'bold' : 'normal'
              }}
            >
              {sport.label}
            </button>
          ))}
        </div>

        {/* 경기 목록 */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: '50px', color: '#888' }}>
            로딩 중...
          </div>
        ) : matches.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '50px', color: '#888' }}>
            예측 가능한 경기가 없습니다.
          </div>
        ) : (
          <div style={{ display: 'grid', gap: '20px' }}>
            {matches.map((match) => (
              <div
                key={match.matchId}
                style={{
                  padding: '25px',
                  backgroundColor: 'white',
                  border: '2px solid #e0e0e0',
                  borderRadius: '10px',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
                }}
              >
                <div style={{ fontSize: '14px', color: '#888', marginBottom: '10px' }}>
                  {match.league?.name} | {new Date(match.detail?.matchDate).toLocaleString()}
                </div>

                <div style={{
                  display: 'flex',
                  justifyContent: 'space-around',
                  alignItems: 'center',
                  marginBottom: '20px'
                }}>
                  <div style={{ textAlign: 'center', flex: 1 }}>
                    <div style={{ fontSize: '20px', fontWeight: 'bold' }}>
                      {match.teams?.home?.name}
                    </div>
                  </div>

                  <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#666', padding: '0 30px' }}>
                    VS
                  </div>

                  <div style={{ textAlign: 'center', flex: 1 }}>
                    <div style={{ fontSize: '20px', fontWeight: 'bold' }}>
                      {match.teams?.away?.name}
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
                  <button
                    style={{
                      padding: '12px 30px',
                      backgroundColor: '#4CAF50',
                      color: 'white',
                      border: 'none',
                      borderRadius: '5px',
                      cursor: 'pointer',
                      fontWeight: 'bold',
                      flex: 1
                    }}
                    onClick={() => alert('로그인 후 예측에 참여할 수 있습니다.')}
                  >
                    홈 승리
                  </button>
                  <button
                    style={{
                      padding: '12px 30px',
                      backgroundColor: '#FFC107',
                      color: 'white',
                      border: 'none',
                      borderRadius: '5px',
                      cursor: 'pointer',
                      fontWeight: 'bold',
                      flex: 1
                    }}
                    onClick={() => alert('로그인 후 예측에 참여할 수 있습니다.')}
                  >
                    무승부
                  </button>
                  <button
                    style={{
                      padding: '12px 30px',
                      backgroundColor: '#2196F3',
                      color: 'white',
                      border: 'none',
                      borderRadius: '5px',
                      cursor: 'pointer',
                      fontWeight: 'bold',
                      flex: 1
                    }}
                    onClick={() => alert('로그인 후 예측에 참여할 수 있습니다.')}
                  >
                    원정 승리
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default Predictions;
