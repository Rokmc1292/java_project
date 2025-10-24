import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useState, useEffect, useRef } from 'react';
import '../styles/Navbar.css';

/**
 * 네비게이션 바 컴포넌트
 * 모든 페이지 상단에 표시되는 메뉴
 */
function Navbar() {
  const location = useLocation();
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [showNotifications, setShowNotifications] = useState(false);
  const notificationRef = useRef(null);

  // 로그인 상태 확인
  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) {
      try {
        setUser(JSON.parse(userData));
      } catch (e) {
        localStorage.removeItem('user');
      }
    }
  }, [location]); // location 변경 시마다 체크

  // 알림 조회
  useEffect(() => {
    if (!user) return;

    const fetchNotifications = async () => {
      try {
        // 읽지 않은 알림 개수 조회
        const countResponse = await fetch(
          `http://localhost:8080/api/notifications/unread/count?username=${user.username}`
        );
        const countData = await countResponse.json();
        setUnreadCount(countData.count || 0);

        // 읽지 않은 알림 목록 조회
        const listResponse = await fetch(
          `http://localhost:8080/api/notifications/unread?username=${user.username}`
        );
        const listData = await listResponse.json();
        setNotifications(listData.slice(0, 5)); // 최신 5개만
      } catch (error) {
        console.error('알림 조회 실패:', error);
      }
    };

    fetchNotifications();

    // 30초마다 알림 새로고침
    const interval = setInterval(fetchNotifications, 30000);
    return () => clearInterval(interval);
  }, [user]);

  // 외부 클릭 시 드롭다운 닫기
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (notificationRef.current && !notificationRef.current.contains(event.target)) {
        setShowNotifications(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // 현재 경로가 활성화된 메뉴인지 확인
  const isActive = (path) => {
    return location.pathname === path ? 'active' : '';
  };

  // 로그아웃 처리
  const handleLogout = () => {
    if (window.confirm('정말 로그아웃 하시겠습니까?')) {
      localStorage.removeItem('user');
      setUser(null);
      alert('로그아웃되었습니다.');
      navigate('/login');
    }
  };

  // 알림 클릭 처리
  const handleNotificationClick = async (notification) => {
    try {
      // 읽음 처리
      await fetch(`http://localhost:8080/api/notifications/${notification.notificationId}/read`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: user.username })
      });

      // 관련 게시글로 이동
      if (notification.relatedPostId) {
        navigate(`/community/post/${notification.relatedPostId}`);
      }

      setShowNotifications(false);

      // 알림 목록 새로고침
      setUnreadCount(prev => Math.max(0, prev - 1));
      setNotifications(prev => prev.filter(n => n.notificationId !== notification.notificationId));
    } catch (error) {
      console.error('알림 처리 실패:', error);
    }
  };

  // 모든 알림 읽음 처리
  const handleMarkAllAsRead = async () => {
    try {
      await fetch('http://localhost:8080/api/notifications/read-all', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: user.username })
      });

      setUnreadCount(0);
      setNotifications([]);
    } catch (error) {
      console.error('알림 읽음 처리 실패:', error);
    }
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        {/* 로고 */}
        <Link to="/" className="navbar-logo">
          Sports Hub
        </Link>

        {/* 메뉴 */}
        <ul className="navbar-menu">
          <li className="navbar-item">
            <Link to="/fixtures" className={`navbar-link ${isActive('/fixtures') || isActive('/')}`}>
              경기일정
            </Link>
          </li>
          <li className="navbar-item">
            <Link to="/community" className={`navbar-link ${isActive('/community')}`}>
              커뮤니티
            </Link>
          </li>
          <li className="navbar-item">
            <Link to="/predictions" className={`navbar-link ${isActive('/predictions')}`}>
              승부예측
            </Link>
          </li>
          <li className="navbar-item">
            <Link to="/live" className={`navbar-link ${isActive('/live')}`}>
              실시간
            </Link>
          </li>
          <li className="navbar-item">
            <Link to="/news" className={`navbar-link ${isActive('/news')}`}>
              뉴스
            </Link>
          </li>
        </ul>

        {/* 사용자 메뉴 */}
        <div className="navbar-user">
          {user ? (
            <>
              {/* 알림 아이콘 */}
              <div ref={notificationRef} style={{ position: 'relative', marginRight: '15px' }}>
                <button
                  onClick={() => setShowNotifications(!showNotifications)}
                  style={{
                    position: 'relative',
                    background: 'none',
                    border: 'none',
                    fontSize: '24px',
                    cursor: 'pointer',
                    padding: '5px'
                  }}
                >
                  🔔
                  {unreadCount > 0 && (
                    <span style={{
                      position: 'absolute',
                      top: '0',
                      right: '0',
                      backgroundColor: '#ff4444',
                      color: 'white',
                      fontSize: '11px',
                      fontWeight: 'bold',
                      borderRadius: '10px',
                      padding: '2px 6px',
                      minWidth: '18px',
                      textAlign: 'center'
                    }}>
                      {unreadCount > 99 ? '99+' : unreadCount}
                    </span>
                  )}
                </button>

                {/* 알림 드롭다운 */}
                {showNotifications && (
                  <div style={{
                    position: 'absolute',
                    top: '40px',
                    right: '0',
                    width: '350px',
                    maxHeight: '400px',
                    backgroundColor: 'white',
                    border: '1px solid #ddd',
                    borderRadius: '8px',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                    zIndex: 1000,
                    overflow: 'hidden'
                  }}>
                    {/* 헤더 */}
                    <div style={{
                      padding: '15px',
                      borderBottom: '1px solid #e0e0e0',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}>
                      <span style={{ fontWeight: 'bold', fontSize: '16px' }}>알림</span>
                      {unreadCount > 0 && (
                        <button
                          onClick={handleMarkAllAsRead}
                          style={{
                            fontSize: '12px',
                            color: '#646cff',
                            background: 'none',
                            border: 'none',
                            cursor: 'pointer',
                            textDecoration: 'underline'
                          }}
                        >
                          모두 읽음
                        </button>
                      )}
                    </div>

                    {/* 알림 목록 */}
                    <div style={{ maxHeight: '350px', overflowY: 'auto' }}>
                      {notifications.length === 0 ? (
                        <div style={{
                          padding: '30px',
                          textAlign: 'center',
                          color: '#888',
                          fontSize: '14px'
                        }}>
                          새로운 알림이 없습니다.
                        </div>
                      ) : (
                        notifications.map(notification => (
                          <div
                            key={notification.notificationId}
                            onClick={() => handleNotificationClick(notification)}
                            style={{
                              padding: '15px',
                              borderBottom: '1px solid #f0f0f0',
                              cursor: 'pointer',
                              backgroundColor: notification.isRead ? 'white' : '#f9f9ff',
                              transition: 'background-color 0.2s'
                            }}
                            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f0f0f0'}
                            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = notification.isRead ? 'white' : '#f9f9ff'}
                          >
                            <div style={{
                              fontSize: '13px',
                              lineHeight: '1.5',
                              color: '#333',
                              marginBottom: '5px'
                            }}>
                              {notification.content}
                            </div>
                            <div style={{ fontSize: '11px', color: '#888' }}>
                              {new Date(notification.createdAt).toLocaleString()}
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                )}
              </div>

              <span className="navbar-username">{user.nickname}님</span>
              <Link to="/mypage" className="navbar-link">
                마이페이지
              </Link>
              <button onClick={handleLogout} className="navbar-logout-btn">
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="navbar-link">
                로그인
              </Link>
              <Link to="/signup" className="navbar-link navbar-signup">
                회원가입
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar;