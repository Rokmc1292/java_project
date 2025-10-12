import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
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
          {/* 기본 페이지 (임시) */}
          <Route path="/" element={
            <div style={{ textAlign: 'center', marginTop: '100px' }}>
              <h1>스포츠 허브</h1>
              <div style={{ marginTop: '20px' }}>
                <a href="/login" style={{ margin: '0 10px' }}>로그인</a>
                <a href="/signup" style={{ margin: '0 10px' }}>회원가입</a>
              </div>
            </div>
          } />
          
          {/* 로그인 페이지 */}
          <Route path="/login" element={<Login />} />
          
          {/* 회원가입 페이지 */}
          <Route path="/signup" element={<Signup />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;