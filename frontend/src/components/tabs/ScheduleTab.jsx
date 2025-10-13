import { useState, useEffect } from 'react';
import { getAllFixtures, formatDate, getTodayDate } from '../../api/sports';

/**
 * 경기일정 탭 컴포넌트
 * 종목별 경기 일정 표시 (축구, 농구, 야구, e스포츠, UFC)
 */
function ScheduleTab() {
  // 상태 관리
  const [selectedSport, setSelectedSport] = useState('all');
  const [selectedDate, setSelectedDate] = useState(getTodayDate());
  const [fixtures, setFixtures] = useState({ 
    football: [], 
    basketball: [], 
    baseball: [], 
    esports: [], 
    ufc: [] 
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // 경기 일정 불러오기
  const loadFixtures = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await getAllFixtures(selectedDate, selectedSport);
      setFixtures(data);
    } catch (err) {
      setError('경기 일정을 불러오는데 실패했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // 날짜/종목 변경 시 경기 일정 로드
  useEffect(() => {
    loadFixtures();
  }, [selectedDate, selectedSport]);

  // 날짜 변경 핸들러
  const handleDateChange = (days) => {
    const currentDate = new Date(selectedDate);
    currentDate.setDate(currentDate.getDate() + days);
    setSelectedDate(formatDate(currentDate));
  };

  // 경기 카드 렌더링 함수
  const renderMatchCard = (match) => {
    const isLive = match.fixture?.status?.shortStatus === 'LIVE' || 
                   match.fixture?.status?.shortStatus === 'IN PLAY';
    
    return (
      <div
        key={match.matchId}
        style={{
          backgroundColor: '#2a2a2a',
          padding: '20px',
          borderRadius: '8px',
          marginBottom: '15px',
          border: isLive ? '2px solid #ff4444' : '1px solid #444'
        }}
      >
        {/* 경기 상태 및 리그 정보 */}
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
          <span style={{ fontSize: '12px', color: '#888' }}>
            {match.league?.name} {match.league?.country && `- ${match.league.country}`}
          </span>
          <span style={{
            fontSize: '12px',
            fontWeight: 'bold',
            color: isLive ? '#ff4444' : '#888'
          }}>
            {isLive ? '● LIVE' : match.fixture?.status?.longStatus || 'Scheduled'}
          </span>
        </div>

        {/* 팀 정보 및 스코어 */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
          {/* 홈팀 */}
          <div style={{ flex: 1, textAlign: 'right' }}>
            <div style={{ fontSize: '16px', marginBottom: '5px' }}>
              {match.teams?.home?.name || 'TBD'}
            </div>
            {match.teams?.home?.logo && (
              <img 
                src={match.teams.home.logo} 
                alt={match.teams.home.name}
                style={{ width: '30px', height: '30px', objectFit: 'contain' }}
                onError={(e) => e.target.style.display = 'none'}
              />
            )}
          </div>

          {/* 스코어 또는 시간 */}
          <div style={{ textAlign: 'center', minWidth: '80px' }}>
            {match.goals?.home !== null && match.goals?.home !== undefined ? (
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#646cff' }}>
                {match.goals.home} - {match.goals.away}
              </div>
            ) : (
              <div style={{ fontSize: '14px', color: '#888' }}>
                {match.fixture?.date ? 
                  new Date(match.fixture.date).toLocaleTimeString('ko-KR', { 
                    hour: '2-digit', 
                    minute: '2-digit' 
                  }) : 'TBD'}
              </div>
            )}
          </div>

          {/* 원정팀 */}
          <div style={{ flex: 1, textAlign: 'left' }}>
            <div style={{ fontSize: '16px', marginBottom: '5px' }}>
              {match.teams?.away?.name || 'TBD'}
            </div>
            {match.teams?.away?.logo && (
              <img 
                src={match.teams.away.logo} 
                alt={match.teams.away.name}
                style={{ width: '30px', height: '30px', objectFit: 'contain' }}
                onError={(e) => e.target.style.display = 'none'}
              />
            )}
          </div>
        </div>

        {/* 경기장 정보 (있는 경우) */}
        {match.fixture?.venue?.name && (
          <div style={{ marginTop: '10px', fontSize: '12px', color: '#666', textAlign: 'center' }}>
            📍 {match.fixture.venue.name}
            {match.fixture.venue.city && `, ${match.fixture.venue.city}`}
          </div>
        )}
      </div>
    );
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1 style={{ fontSize: '28px', marginBottom: '20px' }}>
        📅 경기 일정
      </h1>

      {/* 종목 필터 */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px', flexWrap: 'wrap' }}>
        {[
          { id: 'all', label: '전체' },
          { id: 'football', label: '⚽ 축구' },
          { id: 'basketball', label: '🏀 농구' },
          { id: 'baseball', label: '⚾ 야구' },
          { id: 'esports', label: '🎮 e스포츠' },
          { id: 'ufc', label: '🥊 UFC' }
        ].map(sport => (
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

      {/* 날짜 선택 */}
      <div style={{ 
        backgroundColor: '#2a2a2a', 
        padding: '20px', 
        borderRadius: '10px',
        marginBottom: '20px',
        display: 'flex',
        gap: '10px',
        alignItems: 'center',
        justifyContent: 'center'
      }}>
        <button 
          onClick={() => handleDateChange(-1)}
          style={{
            padding: '8px 16px',
            backgroundColor: '#333',
            color: 'white',
            border: 'none',
            borderRadius: '6px',
            cursor: 'pointer'
          }}
        >
          ◀ 이전
        </button>
        <input 
          type="date" 
          value={selectedDate}
          onChange={(e) => setSelectedDate(e.target.value)}
          style={{
            padding: '8px',
            backgroundColor: '#1a1a1a',
            color: 'white',
            border: '1px solid #444',
            borderRadius: '6px',
            fontSize: '14px'
          }}
        />
        <button 
          onClick={() => handleDateChange(1)}
          style={{
            padding: '8px 16px',
            backgroundColor: '#333',
            color: 'white',
            border: 'none',
            borderRadius: '6px',
            cursor: 'pointer'
          }}
        >
          다음 ▶
        </button>
        <button 
          onClick={() => setSelectedDate(getTodayDate())}
          style={{
            padding: '8px 16px',
            backgroundColor: '#646cff',
            color: 'white',
            border: 'none',
            borderRadius: '6px',
            cursor: 'pointer'
          }}
        >
          오늘
        </button>
      </div>

      {/* 로딩 상태 */}
      {loading && (
        <div style={{
          backgroundColor: '#2a2a2a',
          padding: '40px',
          borderRadius: '10px',
          textAlign: 'center',
          color: '#888'
        }}>
          경기 일정을 불러오는 중...
        </div>
      )}

      {/* 에러 상태 */}
      {error && (
        <div style={{
          backgroundColor: '#2a2a2a',
          padding: '40px',
          borderRadius: '10px',
          textAlign: 'center',
          color: '#ff4444'
        }}>
          {error}
          <br />
          <button 
            onClick={loadFixtures}
            style={{
              marginTop: '20px',
              padding: '10px 20px',
              backgroundColor: '#646cff',
              color: 'white',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer'
            }}
          >
            다시 시도
          </button>
        </div>
      )}

      {/* 경기 일정 목록 */}
      {!loading && !error && (
        <div>
          {/* 축구 경기 */}
          {(selectedSport === 'all' || selectedSport === 'football') && fixtures.football.length > 0 && (
            <div style={{ marginBottom: '30px' }}>
              <h2 style={{ fontSize: '20px', marginBottom: '15px', color: '#646cff' }}>
                ⚽ 축구 ({fixtures.football.length}경기)
              </h2>
              {fixtures.football.map(match => renderMatchCard(match))}
            </div>
          )}

          {/* 농구 경기 */}
          {(selectedSport === 'all' || selectedSport === 'basketball') && fixtures.basketball.length > 0 && (
            <div style={{ marginBottom: '30px' }}>
              <h2 style={{ fontSize: '20px', marginBottom: '15px', color: '#646cff' }}>
                🏀 농구 ({fixtures.basketball.length}경기)
              </h2>
              {fixtures.basketball.map(match => renderMatchCard(match))}
            </div>
          )}

          {/* 야구 경기 */}
          {(selectedSport === 'all' || selectedSport === 'baseball') && fixtures.baseball.length > 0 && (
            <div style={{ marginBottom: '30px' }}>
              <h2 style={{ fontSize: '20px', marginBottom: '15px', color: '#646cff' }}>
                ⚾ 야구 ({fixtures.baseball.length}경기)
              </h2>
              {fixtures.baseball.map(match => renderMatchCard(match))}
            </div>
          )}

          {/* e스포츠 경기 */}
          {(selectedSport === 'all' || selectedSport === 'esports') && fixtures.esports.length > 0 && (
            <div style={{ marginBottom: '30px' }}>
              <h2 style={{ fontSize: '20px', marginBottom: '15px', color: '#646cff' }}>
                🎮 e스포츠 ({fixtures.esports.length}경기)
              </h2>
              {fixtures.esports.map(match => renderMatchCard(match))}
            </div>
          )}

          {/* UFC/MMA 경기 */}
          {(selectedSport === 'all' || selectedSport === 'ufc') && fixtures.ufc.length > 0 && (
            <div style={{ marginBottom: '30px' }}>
              <h2 style={{ fontSize: '20px', marginBottom: '15px', color: '#646cff' }}>
                🥊 UFC ({fixtures.ufc.length}경기)
              </h2>
              {fixtures.ufc.map(match => renderMatchCard(match))}
            </div>
          )}

          {/* 경기 없을 때 */}
          {fixtures.football.length === 0 && 
           fixtures.basketball.length === 0 && 
           fixtures.baseball.length === 0 && 
           fixtures.esports.length === 0 && 
           fixtures.ufc.length === 0 && (
            <div style={{
              backgroundColor: '#2a2a2a',
              padding: '40px',
              borderRadius: '10px',
              textAlign: 'center',
              color: '#888'
            }}>
              {selectedDate}에 예정된 경기가 없습니다.
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default ScheduleTab;