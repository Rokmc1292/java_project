import { useState, useEffect, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { checkAuth } from '../api/auth';

// 환경변수에서 API Base URL 가져오기
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 실시간 페이지
 * 진행 중인 경기 및 실시간 채팅
 */
function Live() {
  const [searchParams] = useSearchParams();
  const [liveMatches, setLiveMatches] = useState([]);
  const [selectedMatch, setSelectedMatch] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState(null);
  const [currentChatroomId, setCurrentChatroomId] = useState(null);
  const stompClientRef = useRef(null);
  const messagesEndRef = useRef(null);

  // 시간 포맷 함수 (HH:mm)
  const formatTime = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
  };

  // 티어별 그라데이션 색상
  const getTierGradient = (tier) => {
    const gradients = {
      BRONZE: 'from-amber-700 to-amber-900',
      SILVER: 'from-gray-400 to-gray-600',
      GOLD: 'from-yellow-400 to-orange-500',
      PLATINUM: 'from-cyan-400 to-blue-500',
      DIAMOND: 'from-blue-300 to-blue-500'
    };
    return gradients[tier] || 'from-gray-500 to-gray-700';
  };

  // 메시지 자동 스크롤
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // 로그인 상태 확인
  useEffect(() => {
    const verifySession = async () => {
      try {
        // 서버 세션 확인
        const userData = await checkAuth();
        if (userData && userData.username) {
          setUser(userData);
        }
      } catch (err) {
        // 세션이 없거나 만료됨
        setUser(null);
      }
    };

    verifySession();
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

  // 채팅 메시지 전송 (WebSocket)
  const sendMessage = () => {
    if (!user) {
      alert('로그인 후 채팅에 참여할 수 있습니다.');
      return;
    }

    if (!newMessage.trim()) {
      alert('메시지를 입력해주세요.');
      return;
    }

    const client = stompClientRef.current;

    if (client && client.connected) {
      // WebSocket을 통해 메시지 전송
      client.publish({
        destination: `/app/chat/${currentChatroomId}`,
        body: JSON.stringify({
          username: user.username,
          message: newMessage
        })
      });

      console.log('메시지 전송:', newMessage);
      setNewMessage(''); // 입력창 초기화
    } else {
      console.error('WebSocket 연결이 끊어졌습니다.');
      alert('채팅 연결이 끊어졌습니다. 페이지를 새로고침해주세요.');
    }
  };

  useEffect(() => {
    fetchLiveMatches();
    // 10초마다 점수 자동 업데이트
    const interval = setInterval(fetchLiveMatches, 10000);
    return () => clearInterval(interval);
  }, []);

  // URL 파라미터에 matchId가 있으면 자동으로 채팅방 입장
  useEffect(() => {
    const matchIdParam = searchParams.get('matchId');
    if (matchIdParam && liveMatches.length > 0 && !selectedMatch) {
      const match = liveMatches.find(m => m.matchId === parseInt(matchIdParam));
      if (match) {
        enterChatroom(match);
      }
    }
  }, [searchParams, liveMatches, selectedMatch]);

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

  // WebSocket 연결 및 채팅방 구독
  useEffect(() => {
    if (currentChatroomId && user) {
      // 기존 메시지 불러오기
      const fetchInitialMessages = async () => {
        try {
          const messagesResponse = await fetch(
            `${API_BASE_URL}/api/live/chatroom/${currentChatroomId}/messages`,
            { credentials: 'include' }
          );
          const messagesData = await messagesResponse.json();
          setMessages(messagesData || []);
        } catch (error) {
          console.error('초기 메시지 조회 실패:', error);
        }
      };

      fetchInitialMessages();

      // WebSocket 연결
      const client = new Client({
        webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        debug: (str) => {
          console.log('STOMP Debug:', str);
        },
        onConnect: () => {
          console.log('WebSocket 연결 성공!');

          // 채팅방 구독
          client.subscribe(`/topic/chatroom/${currentChatroomId}`, (message) => {
            const receivedMessage = JSON.parse(message.body);
            console.log('메시지 수신:', receivedMessage);

            // 새 메시지를 messages 배열에 추가
            setMessages((prevMessages) => [...prevMessages, receivedMessage]);
          });
        },
        onStompError: (frame) => {
          console.error('STOMP 에러:', frame);
        }
      });

      client.activate();
      stompClientRef.current = client;

      // 컴포넌트 언마운트 시 연결 해제
      return () => {
        if (client && client.connected) {
          client.deactivate();
          console.log('WebSocket 연결 해제');
        }
      };
    }
  }, [currentChatroomId, user]);

  return (
    <div className="bg-gray-900 text-white min-h-screen">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-4xl font-bold mb-4">🔴 실시간</h1>

        <div className="bg-red-500/20 border border-red-500 rounded-lg p-4 mb-8">
          <p className="text-red-400 font-bold">🔴 LIVE | 실시간 점수는 10초마다 자동 업데이트됩니다.</p>
          <p className="text-sm text-gray-400 mt-1">
            경기를 클릭하면 실시간 채팅방에 참여할 수 있습니다.
          </p>
        </div>

        {!selectedMatch ? (
          /* 진행 중인 경기 목록 */
          loading ? (
            <div className="text-center py-16">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-red-500"></div>
              <p className="mt-4 text-gray-400">로딩 중...</p>
            </div>
          ) : liveMatches.length === 0 ? (
            <div className="bg-gray-800/50 rounded-lg p-16 text-center">
              <p className="text-gray-400 text-lg">현재 진행 중인 경기가 없습니다.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {liveMatches.map((match) => (
                <div
                  key={match.matchId}
                  onClick={() => enterChatroom(match)}
                  className="bg-white rounded-lg p-6 cursor-pointer transform transition hover:scale-105 hover:shadow-2xl shadow-xl"
                >
                  <div className="inline-block bg-red-500 text-white px-3 py-1 rounded-full text-xs font-bold mb-4 animate-pulse">
                    🔴 LIVE
                  </div>

                  <div className="flex items-center gap-3 text-xs text-gray-600 mb-4">
                    {match.league?.logo && (
                      <img
                        src={`${API_BASE_URL}/${match.league.logo}`}
                        alt={match.league?.name}
                        className="w-6 h-6 object-contain"
                      />
                    )}
                    <span className="font-semibold">{match.league?.name}</span>
                  </div>

                  <div className="flex items-center justify-between mb-4">
                    <div className="flex flex-col items-center flex-1 gap-2">
                      {match.teams?.home?.logo && (
                        <img
                          src={`${API_BASE_URL}/${match.teams.home.logo}`}
                          alt={match.teams?.home?.name}
                          className="w-16 h-16 object-contain"
                        />
                      )}
                      <div className="font-bold text-sm text-gray-900 text-center">
                        {match.teams?.home?.name}
                      </div>
                      <div className="text-4xl font-bold text-gray-900">
                        {match.score?.home || 0}
                      </div>
                    </div>

                    <div className="text-2xl text-gray-400 px-4">:</div>

                    <div className="flex flex-col items-center flex-1 gap-2">
                      {match.teams?.away?.logo && (
                        <img
                          src={`${API_BASE_URL}/${match.teams.away.logo}`}
                          alt={match.teams?.away?.name}
                          className="w-16 h-16 object-contain"
                        />
                      )}
                      <div className="font-bold text-sm text-gray-900 text-center">
                        {match.teams?.away?.name}
                      </div>
                      <div className="text-4xl font-bold text-gray-900">
                        {match.score?.away || 0}
                      </div>
                    </div>
                  </div>

                  <div className="text-center text-gray-600 text-sm">
                    📍 {match.detail?.venue}
                  </div>
                </div>
              ))}
            </div>
          )
        ) : (
          /* 채팅방 화면 */
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* 좌측: 스코어보드 */}
            <div className="lg:col-span-1 bg-white rounded-lg p-6 shadow-xl">
              <button
                onClick={() => setSelectedMatch(null)}
                className="mb-6 w-full px-4 py-2 bg-gray-200 hover:bg-gray-300 text-gray-700 rounded-lg transition font-semibold"
              >
                ← 뒤로 가기
              </button>

              <div className="text-center">
                <div className="inline-block bg-red-500 text-white px-4 py-2 rounded-full font-bold mb-4 animate-pulse">
                  🔴 LIVE
                </div>

                <div className="flex items-center justify-center gap-3 mb-6">
                  {selectedMatch.league?.logo && (
                    <img
                      src={`${API_BASE_URL}/${selectedMatch.league.logo}`}
                      alt={selectedMatch.league?.name}
                      className="w-8 h-8 object-contain"
                    />
                  )}
                  <h2 className="text-lg font-semibold text-gray-600">
                    {selectedMatch.league?.name}
                  </h2>
                </div>

                <div className="flex items-center justify-between mb-6">
                  <div className="flex flex-col items-center flex-1 gap-3">
                    {selectedMatch.teams?.home?.logo && (
                      <img
                        src={`${API_BASE_URL}/${selectedMatch.teams.home.logo}`}
                        alt={selectedMatch.teams?.home?.name}
                        className="w-20 h-20 object-contain"
                      />
                    )}
                    <div className="text-base font-bold text-gray-900">
                      {selectedMatch.teams?.home?.name}
                    </div>
                    <div className="text-5xl font-bold text-gray-900">
                      {selectedMatch.score?.home || 0}
                    </div>
                  </div>

                  <div className="text-2xl text-gray-400 px-4">:</div>

                  <div className="flex flex-col items-center flex-1 gap-3">
                    {selectedMatch.teams?.away?.logo && (
                      <img
                        src={`${API_BASE_URL}/${selectedMatch.teams.away.logo}`}
                        alt={selectedMatch.teams?.away?.name}
                        className="w-20 h-20 object-contain"
                      />
                    )}
                    <div className="text-base font-bold text-gray-900">
                      {selectedMatch.teams?.away?.name}
                    </div>
                    <div className="text-5xl font-bold text-gray-900">
                      {selectedMatch.score?.away || 0}
                    </div>
                  </div>
                </div>

                <div className="text-sm text-gray-600">
                  📍 {selectedMatch.detail?.venue}
                </div>
              </div>
            </div>

            {/* 우측: 채팅창 */}
            <div className="lg:col-span-2 bg-gray-800/80 backdrop-blur-sm rounded-lg p-6 flex flex-col shadow-xl">
              <h3 className="text-xl font-bold mb-4">💬 실시간 채팅</h3>

              {/* 메시지 목록 */}
              <div className="flex-1 bg-gray-900/50 rounded-lg p-4 mb-4 overflow-y-auto" style={{ maxHeight: '500px' }}>
                {messages.length === 0 ? (
                  <div className="text-center text-gray-400 py-8">
                    채팅 메시지가 없습니다. 첫 메시지를 남겨보세요!
                  </div>
                ) : (
                  <>
                    {messages.map((msg) => (
                      <div
                        key={msg.messageId}
                        className={`mb-3 p-3 rounded-lg ${
                          msg.isAdmin
                            ? 'bg-yellow-500/20 border border-yellow-500'
                            : 'bg-gray-700/50'
                        }`}
                      >
                        <div className="flex items-center justify-between mb-2">
                          <div className="flex items-center gap-2">
                            {msg.isAdmin && (
                              <span className="px-2 py-1 bg-red-500 text-white text-xs font-bold rounded">
                                관리자
                              </span>
                            )}
                            <span className={`font-bold ${msg.isAdmin ? 'text-yellow-400' : 'text-blue-400'}`}>
                              {msg.nickname}
                            </span>
                            <span className={`px-3 py-2 bg-gradient-to-r ${getTierGradient(msg.userTier)} text-white text-sm font-bold rounded shadow-lg`}>
                              {msg.userTier}
                            </span>
                          </div>
                          <span className="text-xs text-gray-400">
                            {formatTime(msg.createdAt)}
                          </span>
                        </div>
                        <div className={`text-sm ${msg.isAdmin ? 'font-semibold text-white' : 'text-gray-200'}`}>
                          {msg.message}
                        </div>
                      </div>
                    ))}
                    <div ref={messagesEndRef} />
                  </>
                )}
              </div>

              {/* 메시지 입력 */}
              <div className="flex gap-3">
                <input
                  type="text"
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                  placeholder="메시지를 입력하세요..."
                  className="flex-1 px-4 py-3 bg-gray-700 text-white rounded-lg border-none focus:ring-2 focus:ring-blue-500"
                />
                <button
                  onClick={sendMessage}
                  className="px-6 py-3 bg-blue-500 hover:bg-blue-600 text-white font-bold rounded-lg transition"
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
