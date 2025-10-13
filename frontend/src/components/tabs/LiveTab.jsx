import { useState, useEffect } from 'react';
import { getAllLiveMatches } from '../../api/sports';

/**
 * 실시간 탭 컴포넌트
 * 진행중인 경기 목록 (축구, 농구, 야구, e스포츠, UFC)
 */
function LiveTab() {
  const [selectedSport, setSelectedSport] = useState('all');
  const [liveMatches, setLiveMatches] = useState({ 
    football: [], 
    basketball: [], 
    baseball: [], 
    esports: [], 
    mma: [] 
  });
  const [loading, setLoading] = useState(false);

  // 실시간 경기 불러오기
  const loadLiveMatches = async () => {
    setLoading(true);
    try {
      const data = await getAllLiveMatches();
      setLiveMatches(data);
    } catch (error) {
      console.error('실시간 경기 조회 오류:', error);
    } finally {
      setLoading(false);
    }
  };

  // 30초마다 자동 새로고침
  useEffect(() => {
    loadLiveMatches();
    const interval = setInterval(loadLiveMatches, 30000);
    return () => clearInterval(interval);
  }, []);

  // 경기 카드 렌더링
  const renderLiveMatchCard = (match) => (
    <div
      key={match.matchId}
      style={{
        backgroundColor: '#2a2a2a',
        padding: '20px',
        borderRadius: '10px',
        border: '2px solid #ff4444',
        cursor: 'pointer',
        transition: 'transform 0.2s'
      }}
      onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.02)'}
      onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
    >
      {/* LIVE 태그 */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        marginBottom: '15px'
      }}>
        <span style={{
          backgroundColor: '#ff4444',
          color: 'white',
          padding: '4px 12px',
          borderRadius: '4px',
          fontSize: '12px',
          fontWeight: 'bold'
        }}>
          ● LIVE
        </span>
        <span style={{ fontSize: '14px', color: '#aaa' }}>
          {match.league?.name || match.sport}
        </span>
      </div>

      {/* 팀 정보 및 스코어 */}
      <div style={{ textAlign: 'center', marginBottom: '15px' }}>
        <div style={{ fontSize: '16px', marginBottom: '10px' }}>
          {match.teams?.home?.name || 'TBD'}
        </div>
        <div style={{ 
          fontSize: '28px', 
          fontWeight: 'bold', 
          color: '#646cff',
          margin: '10px 0'
        }}>
          {match.goals?.home ?? 0} - {match.goals?.away ?? 0}
        </div>
        <div style={{ fontSize: '16px' }}>
          {match.teams?.away?.name || 'TBD'}
        </div>
      </div>

      {/* 입장 버튼 */}
      <button style={{
        width: '100%',
        padding: '10px',
        backgroundColor: '#646cff',
        color: 'white',
        border: 'none',
        borderRadius: '6px',
        cursor: 'pointer',
        fontSize: '14px',
        fontWeight: 'bold'
      }}>
        채팅방 입장
      </button>
    </div>
  );

  const allLiveMatches = [
    ...liveMatches.football,
    ...liveMatches.basketball,
    ...liveMatches.baseball,
    ...liveMatches.esports,
    ...liveMatches.mma
  ];

  const filteredMatches = selectedSport === 'all' 
    ? allLiveMatches 
    : liveMatches[selectedSport] || [];

  return (
    <div style={{ padding: '20px' }}>
      <h1 style={{ fontSize: '28px', marginBottom: '20px' }}>
        🔴 실시간 경기
      </h1>

      {/* 종목 필터 */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px', flexWrap: 'wrap' }}>
        {[
          { id: 'all', label: '전체' },
          { id: 'football', label: '⚽ 축구' },
          { id: 'basketball', label: '🏀 농구' },
          { id: 'baseball', label: '⚾ 야구' },
          { id: 'esports', label: '🎮 e스포츠' },
          { id: 'mma', label: '🥊 UFC' }].map(sport => (
          <button
            key={sport.id}
            onClick={() => setSelectedSport(sport.id)}
            style={{
              padding: '8px 16px',
              backgroundColor: selectedSport === sport.id ? '#646cff' : '#2a2a2a',
              color: 'white',
              border: '1px solid #444',
              borderRadius: '6px',
              cursor: 'pointer',
              fontSize: '14px'
            }}
          >
            {sport.label}
          </button>
        ))}
      </div>

      {/* 자동 새로고침 안내 */}
      <div style={{
        backgroundColor: '#2a2a2a',
        padding: '10px',
        borderRadius: '6px',
        marginBottom: '20px',
        textAlign: 'center',
        fontSize: '14px',
        color: '#888'
      }}>
        🔄 30초마다 자동으로 업데이트됩니다
      </div>

      {/* 로딩 상태 */}
      {loading && filteredMatches.length === 0 && (
        <div style={{
          backgroundColor: '#2a2a2a',
          padding: '40px',
          borderRadius: '10px',
          textAlign: 'center',
          color: '#888'
        }}>
          실시간 경기를 불러오는 중...
        </div>
      )}

      {/* 실시간 경기 카드 목록 */}
      {!loading && filteredMatches.length > 0 && (
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', 
          gap: '20px' 
        }}>
          {filteredMatches.map(match => renderLiveMatchCard(match))}
        </div>
      )}

      {/* 경기 없을 때 메시지 */}
      {!loading && filteredMatches.length === 0 && (
        <div style={{
          backgroundColor: '#2a2a2a',
          padding: '60px',
          borderRadius: '10px',
          textAlign: 'center',
          color: '#888'
        }}>
          현재 진행중인 경기가 없습니다.
        </div>
      )}
    </div>
  );
}

export default LiveTab;