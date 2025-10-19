import { Link } from 'react-router-dom';
import '../styles/Dashboard.css';

/**
 * 대시보드 (홈) 페이지
 * 메인 화면으로 주요 기능 소개 및 빠른 링크 제공
 */
function Dashboard() {
  return (
    <div className="dashboard-container">
      {/* 헤더 */}
      <header className="dashboard-header">
        <h1>⚽ Sports Hub</h1>
        <p>모든 스포츠 팬을 위한 종합 플랫폼</p>
      </header>

      {/* 메뉴 카드 */}
      <div className="menu-grid">
        {/* 경기 일정 */}
        <Link to="/fixtures" className="menu-card">
          <div className="card-icon">📅</div>
          <h2>경기 일정</h2>
          <p>오늘의 경기와 예정된 경기를 확인하세요</p>
        </Link>

        {/* 커뮤니티 */}
        <Link to="/community" className="menu-card disabled">
          <div className="card-icon">💬</div>
          <h2>커뮤니티</h2>
          <p>팬들과 함께 이야기를 나눠보세요</p>
          <span className="coming-soon">추후 구현</span>
        </Link>

        {/* 승부예측 */}
        <Link to="/predictions" className="menu-card disabled">
          <div className="card-icon">🎯</div>
          <h2>승부예측</h2>
          <p>경기 결과를 예측하고 포인트를 받으세요</p>
          <span className="coming-soon">추후 구현</span>
        </Link>

        {/* 실시간 */}
        <Link to="/live" className="menu-card disabled">
          <div className="card-icon">🔴</div>
          <h2>실시간</h2>
          <p>진행 중인 경기를 실시간으로 시청하세요</p>
          <span className="coming-soon">추후 구현</span>
        </Link>

        {/* 뉴스 */}
        <Link to="/news" className="menu-card disabled">
          <div className="card-icon">📰</div>
          <h2>뉴스</h2>
          <p>최신 스포츠 뉴스를 확인하세요</p>
          <span className="coming-soon">추후 구현</span>
        </Link>

        {/* 마이페이지 */}
        <Link to="/mypage" className="menu-card disabled">
          <div className="card-icon">👤</div>
          <h2>마이페이지</h2>
          <p>내 정보와 활동 내역을 확인하세요</p>
          <span className="coming-soon">추후 구현</span>
        </Link>
      </div>

      {/* 종목 섹션 */}
      <div className="sports-section">
        <h2>지원하는 종목</h2>
        <div className="sports-grid">
          <div className="sport-item">
            <span className="sport-emoji">⚽</span>
            <span>축구</span>
          </div>
          <div className="sport-item">
            <span className="sport-emoji">🏀</span>
            <span>농구</span>
          </div>
          <div className="sport-item">
            <span className="sport-emoji">⚾</span>
            <span>야구</span>
          </div>
          <div className="sport-item">
            <span className="sport-emoji">🎮</span>
            <span>롤</span>
          </div>
          <div className="sport-item">
            <span className="sport-emoji">🥊</span>
            <span>UFC</span>
          </div>
        </div>
      </div>

      {/* 푸터 */}
      <footer className="dashboard-footer">
        <p>© 2025 Sports Hub. All rights reserved.</p>
      </footer>
    </div>
  );
}

export default Dashboard;