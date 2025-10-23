import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';

/**
 * 마이페이지
 * 프로필, 통계, 활동 내역, 설정
 */
function MyPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [activeTab, setActiveTab] = useState('profile');

  /**
   * 로그아웃 처리 함수
   */
  const handleLogout = () => {
    if (window.confirm('정말 로그아웃 하시겠습니까?')) {
      // localStorage에서 사용자 정보 제거
      localStorage.removeItem('user');
      alert('로그아웃되었습니다.');
      // 로그인 페이지로 이동
      navigate('/login');
    }
  };

  // 임시 사용자 데이터
  const dummyUser = {
    username: 'user123',
    nickname: '스포츠팬',
    email: 'user@example.com',
    tier: 'BRONZE',
    tierScore: 0,
    profileImage: '/images/default-profile.png',
    createdAt: '2025-01-01',
    predictions: {
      total: 0,
      correct: 0,
      wrong: 0,
      winRate: 0
    },
    posts: [],
    comments: []
  };

  useEffect(() => {
    // 실제로는 API로 사용자 정보 조회
    setUser(dummyUser);
  }, []);

  const tabs = [
    { id: 'profile', label: '프로필', icon: '👤' },
    { id: 'predictions', label: '예측 내역', icon: '🎯' },
    { id: 'posts', label: '작성 글/댓글', icon: '✏️' },
    { id: 'settings', label: '설정', icon: '⚙️' }
  ];

  if (!user) {
    return (
      <div>
        <Navbar />
        <div style={{ textAlign: 'center', padding: '100px', color: '#888' }}>
          로그인이 필요합니다.
        </div>
      </div>
    );
  }

  return (
    <div>
      <Navbar />
      <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '20px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '20px' }}>
          👤 마이페이지
        </h1>

        {/* 탭 메뉴 */}
        <div style={{
          display: 'flex',
          gap: '10px',
          marginBottom: '30px',
          borderBottom: '2px solid #e0e0e0',
          paddingBottom: '10px'
        }}>
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              style={{
                padding: '12px 25px',
                backgroundColor: activeTab === tab.id ? '#646cff' : '#f5f5f5',
                color: activeTab === tab.id ? 'white' : '#333',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer',
                fontWeight: activeTab === tab.id ? 'bold' : 'normal',
                fontSize: '16px',
                transition: 'all 0.3s'
              }}
            >
              {tab.icon} {tab.label}
            </button>
          ))}
        </div>

        {/* 프로필 탭 */}
        {activeTab === 'profile' && (
          <div>
            <div style={{
              backgroundColor: 'white',
              border: '2px solid #e0e0e0',
              borderRadius: '10px',
              padding: '30px',
              marginBottom: '20px'
            }}>
              <h2 style={{ fontSize: '20px', fontWeight: 'bold', marginBottom: '20px' }}>
                프로필 정보
              </h2>

              <div style={{ display: 'flex', alignItems: 'center', gap: '30px' }}>
                <div style={{
                  width: '120px',
                  height: '120px',
                  borderRadius: '50%',
                  backgroundColor: '#f0f0f0',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '48px'
                }}>
                  👤
                </div>

                <div style={{ flex: 1 }}>
                  <div style={{ marginBottom: '15px' }}>
                    <div style={{ fontSize: '14px', color: '#888', marginBottom: '5px' }}>닉네임</div>
                    <div style={{ fontSize: '20px', fontWeight: 'bold' }}>{user.nickname}</div>
                  </div>

                  <div style={{ marginBottom: '15px' }}>
                    <div style={{ fontSize: '14px', color: '#888', marginBottom: '5px' }}>티어</div>
                    <div style={{
                      display: 'inline-block',
                      padding: '8px 20px',
                      backgroundColor: '#646cff',
                      color: 'white',
                      borderRadius: '20px',
                      fontWeight: 'bold'
                    }}>
                      {user.tier} ({user.tierScore} 점)
                    </div>
                  </div>

                  <div>
                    <div style={{ fontSize: '14px', color: '#888', marginBottom: '5px' }}>가입일</div>
                    <div style={{ fontSize: '16px' }}>{new Date(user.createdAt).toLocaleDateString()}</div>
                  </div>
                </div>
              </div>
            </div>

            {/* 통계 */}
            <div style={{
              backgroundColor: 'white',
              border: '2px solid #e0e0e0',
              borderRadius: '10px',
              padding: '30px'
            }}>
              <h2 style={{ fontSize: '20px', fontWeight: 'bold', marginBottom: '20px' }}>
                예측 통계
              </h2>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px' }}>
                <div style={{ textAlign: 'center', padding: '20px', backgroundColor: '#f9f9f9', borderRadius: '8px' }}>
                  <div style={{ fontSize: '14px', color: '#888', marginBottom: '10px' }}>총 예측</div>
                  <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#333' }}>
                    {user.predictions.total}
                  </div>
                </div>

                <div style={{ textAlign: 'center', padding: '20px', backgroundColor: '#f9f9f9', borderRadius: '8px' }}>
                  <div style={{ fontSize: '14px', color: '#888', marginBottom: '10px' }}>적중</div>
                  <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#4CAF50' }}>
                    {user.predictions.correct}
                  </div>
                </div>

                <div style={{ textAlign: 'center', padding: '20px', backgroundColor: '#f9f9f9', borderRadius: '8px' }}>
                  <div style={{ fontSize: '14px', color: '#888', marginBottom: '10px' }}>실패</div>
                  <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#f44336' }}>
                    {user.predictions.wrong}
                  </div>
                </div>

                <div style={{ textAlign: 'center', padding: '20px', backgroundColor: '#f9f9f9', borderRadius: '8px' }}>
                  <div style={{ fontSize: '14px', color: '#888', marginBottom: '10px' }}>승률</div>
                  <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#646cff' }}>
                    {user.predictions.winRate}%
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 예측 내역 탭 */}
        {activeTab === 'predictions' && (
          <div style={{
            backgroundColor: 'white',
            border: '2px solid #e0e0e0',
            borderRadius: '10px',
            padding: '30px',
            textAlign: 'center',
            color: '#888'
          }}>
            <div style={{ fontSize: '48px', marginBottom: '20px' }}>🎯</div>
            <p>예측 내역이 없습니다.</p>
            <p style={{ marginTop: '10px', fontSize: '14px' }}>
              경기 예측에 참여하고 티어 점수를 올려보세요!
            </p>
          </div>
        )}

        {/* 작성 글/댓글 탭 */}
        {activeTab === 'posts' && (
          <div style={{
            backgroundColor: 'white',
            border: '2px solid #e0e0e0',
            borderRadius: '10px',
            padding: '30px',
            textAlign: 'center',
            color: '#888'
          }}>
            <div style={{ fontSize: '48px', marginBottom: '20px' }}>✏️</div>
            <p>작성한 글과 댓글이 없습니다.</p>
            <p style={{ marginTop: '10px', fontSize: '14px' }}>
              커뮤니티에서 다른 팬들과 소통해보세요!
            </p>
          </div>
        )}

        {/* 설정 탭 */}
        {activeTab === 'settings' && (
          <div style={{
            backgroundColor: 'white',
            border: '2px solid #e0e0e0',
            borderRadius: '10px',
            padding: '30px'
          }}>
            <h2 style={{ fontSize: '20px', fontWeight: 'bold', marginBottom: '20px' }}>
              계정 설정
            </h2>

            <div style={{ marginBottom: '20px' }}>
              <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
                닉네임 변경
              </label>
              <input
                type="text"
                defaultValue={user.nickname}
                style={{
                  width: '100%',
                  padding: '12px',
                  border: '1px solid #e0e0e0',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              />
            </div>

            <div style={{ marginBottom: '20px' }}>
              <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
                비밀번호 변경
              </label>
              <input
                type="password"
                placeholder="새 비밀번호"
                style={{
                  width: '100%',
                  padding: '12px',
                  border: '1px solid #e0e0e0',
                  borderRadius: '5px',
                  fontSize: '14px',
                  marginBottom: '10px'
                }}
              />
              <input
                type="password"
                placeholder="비밀번호 확인"
                style={{
                  width: '100%',
                  padding: '12px',
                  border: '1px solid #e0e0e0',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              />
            </div>

            <button
              style={{
                padding: '12px 30px',
                backgroundColor: '#646cff',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '16px',
                fontWeight: 'bold',
                marginRight: '10px'
              }}
              onClick={() => alert('설정이 저장되었습니다.')}
            >
              저장하기
            </button>

            <button
              style={{
                padding: '12px 30px',
                backgroundColor: '#f44336',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '16px',
                fontWeight: 'bold'
              }}
              onClick={handleLogout}
            >
              로그아웃
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default MyPage;
