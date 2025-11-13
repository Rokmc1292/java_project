import { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';

// 환경변수에서 API Base URL 가져오기
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 실시간 페이지
 * 진행 중인 경기 및 실시간 채팅
 */
function Live() {
  const [liveMatches, setLiveMatches] = useState([]);
  const [selectedMatch, setSelectedMatch] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState(null);
  const [currentChatroomId, setCurrentChatroomId] = useState(null);

  // 로그인 상태 확인
  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) {
      try {
        const parsedUser = JSON.parse(userData);
        // username 필드 검증
        if (parsedUser && parsedUser.username) {
          setUser(parsedUser);
        } else {
          console.error('사용자 정보가 올바르지 않습니다.');
          localStorage.removeItem('user');
        }
      } catch (e) {
        console.error('사용자 정보 파싱 오류:', e);
        localStorage.removeItem('user');
      }
    }
  }, []);

  // 진행 중인 경기 조회
  const fetchLiveMatches = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/live/matches`, {
        credentials: 'include'  // 세션 쿠키 포함
      });
      const data = await response.json();
      setLiveMatches(data || []);
    } catch (error) {
      console.error('실시간 경기 조회 실패:', error);
      setLiveMatches([]);
    } finally {
      setLoading(false);
    }
  };

  // 경기 선택 시 채팅방 입장
  const enterChatroom = async (match) => {
    setSelectedMatch(match);
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/live/chatroom/match/${match.matchId}`,
        { credentials: 'include' }  // 세션 쿠키 포함
      );
      const data = await response.json();
      const chatroomId = data.chatroomId;
      setCurrentChatroomId(chatroomId);

      // 채팅 메시지 조회
      const messagesResponse = await fetch(
        `${API_BASE_URL}/api/live/chatroom/${chatroomId}/messages`,
        { credentials: 'include' }  // 세션 쿠키 포함
      );
      const messagesData = await messagesResponse.json();
      setMessages(messagesData || []);
    } catch (error) {
      console.error('채팅방 입장 실패:', error);
    }
  };

  // 채팅 메시지 전송
  const sendMessage = async () => {
    if (!user) {
      alert('로그인 후 채팅에 참여할 수 있습니다.');
      return;
    }

    if (!newMessage.trim()) {
      alert('메시지를 입력해주세요.');
      return;
    }

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/live/chatroom/${currentChatroomId}/messages`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include',  // 세션 쿠키 포함
          body: JSON.stringify({
            username: user.username,
            message: newMessage
          })
        }
      );

      if (response.ok) {
        // 메시지 전송 성공 후 목록 새로고침
        const messagesResponse = await fetch(
          `${API_BASE_URL}/api/live/chatroom/${currentChatroomId}/messages`,
          { credentials: 'include' }  // 세션 쿠키 포함
        );
        const messagesData = await messagesResponse.json();
        setMessages(messagesData || []);
        setNewMessage(''); // 입력창 초기화
      } else {
        const error = await response.json();
        alert(error.message || '메시지 전송에 실패했습니다.');
      }
    } catch (error) {
      console.error('메시지 전송 오류:', error);
      alert('메시지 전송 중 오류가 발생했습니다.');
    }
  };

  useEffect(() => {
    fetchLiveMatches();
    // 10초마다 점수 자동 업데이트
    const interval = setInterval(fetchLiveMatches, 10000);
    return () => clearInterval(interval);
  }, []);

  // 채팅방에 있을 때 liveMatches가 업데이트되면 selectedMatch도 업데이트
  useEffect(() => {
    if (selectedMatch && liveMatches.length > 0) {
      const updatedMatch = liveMatches.find(
        match => match.matchId === selectedMatch.matchId
      );
      if (updatedMatch) {
        setSelectedMatch(updatedMatch);
      }
    }
  }, [liveMatches]);

  return (
    <div>
      <Navbar />
      <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '20px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '20px' }}>
          🔴 실시간
        </h1>

        <div style={{
          backgroundColor: '#ffebee',
          padding: '15px',
          borderRadius: '8px',
          marginBottom: '20px',
          border: '1px solid #ffcdd2'
        }}>
          <p style={{ margin: 0, color: '#c62828', fontWeight: 'bold' }}>
            🔴 LIVE | 실시간 점수는 10초마다 자동 업데이트됩니다.
          </p>
          <p style={{ margin: '5px 0 0 0', fontSize: '14px', color: '#666' }}>
            경기를 클릭하면 실시간 채팅방에 참여할 수 있습니다.
          </p>
        </div>

        {!selectedMatch ? (
          /* 진행 중인 경기 목록 */
          loading ? (
            <div style={{ textAlign: 'center', padding: '50px', color: '#888' }}>
              로딩 중...
            </div>
          ) : liveMatches.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '50px', color: '#888' }}>
              현재 진행 중인 경기가 없습니다.
            </div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '20px' }}>
              {liveMatches.map((match) => (
                <div
                  key={match.matchId}
                  onClick={() => enterChatroom(match)}
                  style={{
                    padding: '20px',
                    backgroundColor: 'white',
                    border: '2px solid #ff4444',
                    borderRadius: '10px',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    boxShadow: '0 2px 8px rgba(255,68,68,0.2)'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-5px)';
                    e.currentTarget.style.boxShadow = '0 4px 12px rgba(255,68,68,0.3)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = '0 2px 8px rgba(255,68,68,0.2)';
                  }}
                >
                  <div style={{
                    backgroundColor: '#ff4444',
                    color: 'white',
                    padding: '5px 10px',
                    borderRadius: '5px',
                    display: 'inline-block',
                    marginBottom: '15px',
                    fontSize: '12px',
                    fontWeight: 'bold'
                  }}>
                    🔴 LIVE
                  </div>

                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px',
                    fontSize: '12px',
                    color: '#888',
                    marginBottom: '15px'
                  }}>
                    {match.league?.logo && (
                      <img
                        src={`${API_BASE_URL}/${match.league.logo}`}
                        alt={match.league?.name}
                        style={{ width: '24px', height: '24px', objectFit: 'contain' }}
                      />
                    )}
                    <span>{match.league?.name}</span>
                  </div>

                  <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '15px'
                  }}>
                    <div style={{ flex: 1, textAlign: 'center', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px' }}>
                      {match.teams?.home?.logo && (
                        <img
                          src={`${API_BASE_URL}/${match.teams.home.logo}`}
                          alt={match.teams?.home?.name}
                          style={{ width: '50px', height: '50px', objectFit: 'contain' }}
                        />
                      )}
                      <div style={{ fontWeight: 'bold', fontSize: '14px' }}>
                        {match.teams?.home?.name}
                      </div>
                      <div style={{ fontSize: '32px', fontWeight: 'bold', color: '#333' }}>
                        {match.score?.home || 0}
                      </div>
                    </div>

                    <div style={{ fontSize: '20px', color: '#888', padding: '0 20px' }}>
                      :
                    </div>

                    <div style={{ flex: 1, textAlign: 'center', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px' }}>
                      {match.teams?.away?.logo && (
                        <img
                          src={`${API_BASE_URL}/${match.teams.away.logo}`}
                          alt={match.teams?.away?.name}
                          style={{ width: '50px', height: '50px', objectFit: 'contain' }}
                        />
                      )}
                      <div style={{ fontWeight: 'bold', fontSize: '14px' }}>
                        {match.teams?.away?.name}
                      </div>
                      <div style={{ fontSize: '32px', fontWeight: 'bold', color: '#333' }}>
                        {match.score?.away || 0}
                      </div>
                    </div>
                  </div>

                  <div style={{ textAlign: 'center', color: '#666', fontSize: '14px' }}>
                    📍 {match.detail?.venue}
                  </div>
                </div>
              ))}
            </div>
          )
        ) : (
          /* 채팅방 화면 */
          <div style={{ display: 'flex', gap: '20px', height: '600px' }}>
            {/* 좌측: 스코어보드 */}
            <div style={{
              width: '30%',
              backgroundColor: 'white',
              border: '2px solid #e0e0e0',
              borderRadius: '10px',
              padding: '20px'
            }}>
              <button
                onClick={() => setSelectedMatch(null)}
                style={{
                  marginBottom: '20px',
                  padding: '10px',
                  backgroundColor: '#f5f5f5',
                  border: 'none',
                  borderRadius: '5px',
                  cursor: 'pointer',
                  width: '100%'
                }}
              >
                ← 뒤로 가기
              </button>

              <div style={{ textAlign: 'center' }}>
                <div style={{
                  backgroundColor: '#ff4444',
                  color: 'white',
                  padding: '10px',
                  borderRadius: '5px',
                  marginBottom: '20px',
                  fontWeight: 'bold'
                }}>
                  🔴 LIVE
                </div>

                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '10px',
                  marginBottom: '20px'
                }}>
                  {selectedMatch.league?.logo && (
                    <img
                      src={`${API_BASE_URL}/${selectedMatch.league.logo}`}
                      alt={selectedMatch.league?.name}
                      style={{ width: '30px', height: '30px', objectFit: 'contain' }}
                    />
                  )}
                  <h2 style={{ fontSize: '16px', margin: 0, color: '#888' }}>
                    {selectedMatch.league?.name}
                  </h2>
                </div>

                <div style={{
                  display: 'flex',
                  justifyContent: 'space-around',
                  alignItems: 'center',
                  marginBottom: '30px'
                }}>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px' }}>
                    {selectedMatch.teams?.home?.logo && (
                      <img
                        src={`${API_BASE_URL}/${selectedMatch.teams.home.logo}`}
                        alt={selectedMatch.teams?.home?.name}
                        style={{ width: '60px', height: '60px', objectFit: 'contain' }}
                      />
                    )}
                    <div style={{ fontSize: '16px', fontWeight: 'bold' }}>
                      {selectedMatch.teams?.home?.name}
                    </div>
                    <div style={{ fontSize: '48px', fontWeight: 'bold', color: '#333' }}>
                      {selectedMatch.score?.home || 0}
                    </div>
                  </div>

                  <div style={{ fontSize: '24px', color: '#888' }}>:</div>

                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px' }}>
                    {selectedMatch.teams?.away?.logo && (
                      <img
                        src={`${API_BASE_URL}/${selectedMatch.teams.away.logo}`}
                        alt={selectedMatch.teams?.away?.name}
                        style={{ width: '60px', height: '60px', objectFit: 'contain' }}
                      />
                    )}
                    <div style={{ fontSize: '16px', fontWeight: 'bold' }}>
                      {selectedMatch.teams?.away?.name}
                    </div>
                    <div style={{ fontSize: '48px', fontWeight: 'bold', color: '#333' }}>
                      {selectedMatch.score?.away || 0}
                    </div>
                  </div>
                </div>

                <div style={{ fontSize: '14px', color: '#666' }}>
                  📍 {selectedMatch.detail?.venue}
                </div>
              </div>
            </div>

            {/* 우측: 채팅창 */}
            <div style={{
              width: '70%',
              backgroundColor: 'white',
              border: '2px solid #e0e0e0',
              borderRadius: '10px',
              padding: '20px',
              display: 'flex',
              flexDirection: 'column'
            }}>
              <h3 style={{ marginBottom: '15px', fontSize: '18px', fontWeight: 'bold' }}>
                💬 실시간 채팅
              </h3>

              {/* 메시지 목록 */}
              <div style={{
                flex: 1,
                overflowY: 'auto',
                backgroundColor: '#f9f9f9',
                borderRadius: '5px',
                padding: '15px',
                marginBottom: '15px'
              }}>
                {messages.length === 0 ? (
                  <div style={{ textAlign: 'center', color: '#888', padding: '20px' }}>
                    채팅 메시지가 없습니다. 첫 메시지를 남겨보세요!
                  </div>
                ) : (
                  messages.map((msg) => (
                    <div key={msg.messageId} style={{ marginBottom: '10px' }}>
                      <div style={{ fontSize: '12px', color: '#888' }}>
                        <span style={{ fontWeight: 'bold', color: '#333' }}>
                          {msg.nickname}
                        </span>
                        <span style={{
                          marginLeft: '5px',
                          padding: '2px 6px',
                          backgroundColor: '#646cff',
                          color: 'white',
                          borderRadius: '3px',
                          fontSize: '10px'
                        }}>
                          {msg.userTier}
                        </span>
                      </div>
                      <div style={{ marginTop: '5px', color: '#333' }}>
                        {msg.message}
                      </div>
                    </div>
                  ))
                )}
              </div>

              {/* 메시지 입력 */}
              <div style={{ display: 'flex', gap: '10px' }}>
                <input
                  type="text"
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  placeholder="메시지를 입력하세요..."
                  style={{
                    flex: 1,
                    padding: '12px',
                    border: '1px solid #e0e0e0',
                    borderRadius: '5px',
                    fontSize: '14px'
                  }}
                />
                <button
                  onClick={sendMessage}
                  style={{
                    padding: '12px 25px',
                    backgroundColor: '#646cff',
                    color: 'white',
                    border: 'none',
                    borderRadius: '5px',
                    cursor: 'pointer',
                    fontWeight: 'bold'
                  }}
                >
                  전송
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Live;
