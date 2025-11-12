import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import '../styles/Navbar.css';

/**
 * 네비게이션 바 컴포넌트
 * 모든 페이지 상단에 표시되는 메뉴
 */
function Navbar() {
    const location = useLocation();
    const navigate = useNavigate();
    const [user, setUser] = useState(null);

    // 로그인 상태 확인
    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (userData) {
            try {
                const parsedUser = JSON.parse(userData);
                console.log('현재 사용자:', parsedUser); // 디버깅용
                setUser(parsedUser);
            } catch (e) {
                console.error('사용자 정보 파싱 오류:', e);
                localStorage.removeItem('user');
            }
        }
    }, [location]); // location 변경 시마다 체크

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

                    {/* 관리자 메뉴 - 관리자만 표시 */}
                    {user && user.isAdmin && (
                        <li className="navbar-item">
                            <Link to="/admin" className={`navbar-link ${isActive('/admin')}`}>
                                관리자
                            </Link>
                        </li>
                    )}
                </ul>

                {/* 사용자 메뉴 */}
                <div className="navbar-user">
                    {user ? (
                        <>
              <span className="navbar-username">
                {user.nickname}님
                  {user.isAdmin && <span className="admin-badge">👑</span>}
              </span>
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