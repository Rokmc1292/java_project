import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
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