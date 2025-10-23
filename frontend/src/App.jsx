import { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Fixtures from './pages/Fixtures';
import Dashboard from './pages/Dashboard';
import Community from './pages/Community';
import Predictions from './pages/Predictions';
import Live from './pages/Live';
import News from './pages/News';
import MyPage from './pages/MyPage';
import Navbar from './components/Navbar';
import SplashScreen from './components/SplashScreen';
import './App.css';

/**
 * 메인 App 컴포넌트
 * React Router를 사용하여 페이지 라우팅 설정
 */
function App() {
  const [showSplash, setShowSplash] = useState(true);

  // 처음 방문 시에만 스플래시 화면 표시
  useEffect(() => {
    const hasVisited = sessionStorage.getItem('hasVisited');
    if (hasVisited) {
      setShowSplash(false);
    }
  }, []);

  const handleSplashFinish = () => {
    sessionStorage.setItem('hasVisited', 'true');
    setShowSplash(false);
  };

  if (showSplash) {
    return <SplashScreen onFinish={handleSplashFinish} />;
  }

  return (
    <BrowserRouter>
      <div className="App">
        <Routes>
          {/* 기본 경로 - 대시보드로 이동 */}
          <Route path="/" element={<Dashboard />} />

          {/* 로그인 페이지 */}
          <Route path="/login" element={<Login />} />

          {/* 회원가입 페이지 */}
          <Route path="/signup" element={<Signup />} />

          {/* 대시보드 (메인 페이지) */}
          <Route path="/dashboard" element={<Dashboard />} />

          {/* 경기 일정 페이지 */}
          <Route path="/fixtures" element={<Fixtures />} />

          {/* 커뮤니티 */}
          <Route path="/community" element={<Community />} />

          {/* 승부예측 */}
          <Route path="/predictions" element={<Predictions />} />

          {/* 실시간 */}
          <Route path="/live" element={<Live />} />

          {/* 뉴스 */}
          <Route path="/news" element={<News />} />

          {/* 마이페이지 */}
          <Route path="/mypage" element={<MyPage />} />

          {/* 404 페이지 */}
          <Route path="*" element={
            <div style={{ 
              textAlign: 'center', 
              marginTop: '100px',
              fontSize: '24px',
              color: '#888'
            }}>
              <h1>404 - 페이지를 찾을 수 없습니다</h1>
              <p style={{ fontSize: '18px', marginTop: '20px', color: '#666' }}>
                요청하신 페이지를 찾을 수 없습니다.
              </p>
              <a 
                href="/" 
                style={{ 
                  color: '#646cff', 
                  textDecoration: 'underline',
                  fontSize: '16px'
                }}
              >
                홈으로 돌아가기
              </a>
            </div>
          } />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;