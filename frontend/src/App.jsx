import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Fixtures from './pages/Fixtures';
import Dashboard from './pages/Dashboard';
import Navbar from './components/Navbar';
import './App.css';

/**
 * 메인 App 컴포넌트
 * React Router를 사용하여 페이지 라우팅 설정
 */
function App() {
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

          {/* ✅ 경기 일정 페이지 (새로 추가) */}
          <Route path="/fixtures" element={<Fixtures />} />

          {/* 커뮤니티 (추후 구현) */}
          <Route path="/community" element={
            <div style={{ 
              textAlign: 'center', 
              marginTop: '100px', 
              fontSize: '24px',
              color: '#888'
            }}>
              <h1>💬 커뮤니티</h1>
              <p style={{ fontSize: '18px', marginTop: '20px' }}>
                커뮤니티 기능은 추후 구현 예정입니다.
              </p>
              <a 
                href="/dashboard" 
                style={{ 
                  color: '#646cff', 
                  textDecoration: 'underline',
                  fontSize: '16px'
                }}
              >
                대시보드로 돌아가기
              </a>
            </div>
          } />

          {/* 승부예측 (추후 구현) */}
          <Route path="/predictions" element={
            <div style={{ 
              textAlign: 'center', 
              marginTop: '100px', 
              fontSize: '24px',
              color: '#888'
            }}>
              <h1>🎯 승부예측</h1>
              <p style={{ fontSize: '18px', marginTop: '20px' }}>
                승부예측 기능은 추후 구현 예정입니다.
              </p>
              <a 
                href="/dashboard" 
                style={{ 
                  color: '#646cff', 
                  textDecoration: 'underline',
                  fontSize: '16px'
                }}
              >
                대시보드로 돌아가기
              </a>
            </div>
          } />

          {/* 실시간 (추후 구현) */}
          <Route path="/live" element={
            <div style={{ 
              textAlign: 'center', 
              marginTop: '100px', 
              fontSize: '24px',
              color: '#888'
            }}>
              <h1>🔴 실시간</h1>
              <p style={{ fontSize: '18px', marginTop: '20px' }}>
                실시간 기능은 추후 구현 예정입니다.
              </p>
              <a 
                href="/dashboard" 
                style={{ 
                  color: '#646cff', 
                  textDecoration: 'underline',
                  fontSize: '16px'
                }}
              >
                대시보드로 돌아가기
              </a>
            </div>
          } />

          {/* 뉴스 (추후 구현) */}
          <Route path="/news" element={
            <div style={{ 
              textAlign: 'center', 
              marginTop: '100px', 
              fontSize: '24px',
              color: '#888'
            }}>
              <h1>📰 뉴스</h1>
              <p style={{ fontSize: '18px', marginTop: '20px' }}>
                뉴스 기능은 추후 구현 예정입니다.
              </p>
              <a 
                href="/dashboard" 
                style={{ 
                  color: '#646cff', 
                  textDecoration: 'underline',
                  fontSize: '16px'
                }}
              >
                대시보드로 돌아가기
              </a>
            </div>
          } />

          {/* 마이페이지 (추후 구현) */}
          <Route path="/mypage" element={
            <div style={{ 
              textAlign: 'center', 
              marginTop: '100px', 
              fontSize: '24px',
              color: '#888'
            }}>
              <h1>👤 마이페이지</h1>
              <p style={{ fontSize: '18px', marginTop: '20px' }}>
                마이페이지 기능은 추후 구현 예정입니다.
              </p>
              <a 
                href="/dashboard" 
                style={{ 
                  color: '#646cff', 
                  textDecoration: 'underline',
                  fontSize: '16px'
                }}
              >
                대시보드로 돌아가기
              </a>
            </div>
          } />

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