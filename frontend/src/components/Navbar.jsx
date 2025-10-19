import { Link, useLocation } from 'react-router-dom';
import '../styles/Navbar.css';

/**
 * 네비게이션 바 컴포넌트
 * 모든 페이지 상단에 표시되는 메뉴
 */
function Navbar() {
  const location = useLocation();

  // 현재 경로가 활성화된 메뉴인지 확인
  const isActive = (path) => {
    return location.pathname === path ? 'active' : '';
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        {/* 로고 */}
        <Link to="/" className="navbar-logo">
          ⚽ Sports Hub
        </Link>

        {/* 메뉴 */}
        <ul className="navbar-menu">
          <li className="navbar-item">
            <Link to="/dashboard" className={`navbar-link ${isActive('/dashboard')}`}>
              🏠 홈
            </Link>
          </li>
          <li className="navbar-item">
            <Link to="/fixtures" className={`navbar-link ${isActive('/fixtures')}`}>
              📅 경기일정
            </Link>
          </li>
          <li className="navbar-item">
            <Link to="/community" className={`navbar-link ${isActive('/community')}`}>
              💬 커뮤니티
            </Link>
          </li>
          <li className="navbar-item">
            <Link to="/predictions" className={`navbar-link ${isActive('/predictions')}`}>
              🎯 승부예측
            </Link>
          </li>
          <li className="navbar-item">
            <Link to="/live" className={`navbar-link ${isActive('/live')}`}>
              🔴 실시간
            </Link>
          </li>
          <li className="navbar-item">
            <Link to="/news" className={`navbar-link ${isActive('/news')}`}>
              📰 뉴스
            </Link>
          </li>
        </ul>

        {/* 사용자 메뉴 */}
        <div className="navbar-user">
          <Link to="/mypage" className="navbar-link">
            👤 마이페이지
          </Link>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;