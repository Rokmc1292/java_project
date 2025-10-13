import { useNavigate } from 'react-router-dom';

/**
 * 상단 네비게이션 바 컴포넌트
 * 로고, 메뉴, 로그인 사용자 정보 표시
 */
function Navbar({ user }) {
  const navigate = useNavigate();

  // 로그아웃 처리
  const handleLogout = () => {
    if (window.confirm('로그아웃 하시겠습니까?')) {
      localStorage.removeItem('user');
      navigate('/login');
    }
  };

  return (
    <nav style={{
      backgroundColor: '#1a1a1a',
      padding: '15px 20px',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      borderBottom: '2px solid #646cff'
    }}>
      {/* 로고 */}
      <div 
        style={{ 
          fontSize: '24px', 
          fontWeight: 'bold', 
          color: '#646cff',
          cursor: 'pointer'
        }}
        onClick={() => navigate('/')}
      >
        ⚽ Sports Community
      </div>

      {/* 사용자 정보 */}
      {user && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            {/* 프로필 이미지 */}
            <img 
              src={user.profileImage || '/images/default-profile.png'} 
              alt="profile"
              style={{
                width: '35px',
                height: '35px',
                borderRadius: '50%',
                border: '2px solid #646cff'
              }}
            />
            {/* 닉네임 및 티어 */}
            <div>
              <div style={{ fontSize: '14px', fontWeight: 'bold' }}>
                {user.nickname}
              </div>
              <div style={{ fontSize: '12px', color: '#888' }}>
                {user.tier} ({user.tierScore}점)
              </div>
            </div>
          </div>
          
          {/* 마이페이지 버튼 */}
          <button
            onClick={() => navigate('/mypage')}
            style={{
              padding: '8px 15px',
              backgroundColor: '#646cff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '14px'
            }}
          >
            마이페이지
          </button>

          {/* 로그아웃 버튼 */}
          <button
            onClick={handleLogout}
            style={{
              padding: '8px 15px',
              backgroundColor: '#333',
              color: 'white',
              border: '1px solid #666',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '14px'
            }}
          >
            로그아웃
          </button>
        </div>
      )}
    </nav>
  );
}

export default Navbar;