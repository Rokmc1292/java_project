/**
 * 메인 App 컴포넌트
 * React Router를 사용하여 페이지 라우팅 설정
 *
 * 파일 위치: frontend/src/App.jsx
 */

import { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Fixtures from './pages/Fixtures';
import Community from './pages/Community';
import PostDetail from './pages/PostDetail';
import Live from './pages/Live';
import News from './pages/News';
import MyPage from './pages/MyPage';
import AdminLayout from './pages/AdminLayout';
import SplashScreen from './components/SplashScreen';
import ProtectedRoute from './components/ProtectedRoute';
import Predictions from './pages/Predictions';
import PredictionDetail from './pages/PredictionDetail';
import PredictionRanking from './pages/PredictionRanking';
import './App.css';

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
                    {/* 기본 경로 - 경기 일정으로 리다이렉트 */}
                    <Route path="/" element={<Fixtures />} />

                    {/* 로그인 페이지 */}
                    <Route path="/login" element={<Login />} />

                    {/* 회원가입 페이지 */}
                    <Route path="/signup" element={<Signup />} />

                    {/* 대시보드 경로 - 경기 일정으로 리다이렉트 */}
                    <Route path="/dashboard" element={<Navigate to="/fixtures" replace />} />

                    {/* 경기 일정 페이지 */}
                    <Route path="/fixtures" element={<Fixtures />} />

                    {/* 커뮤니티 */}
                    <Route path="/community" element={<Community />} />

                    {/* 게시글 상세 페이지 */}
                    <Route path="/community/post/:postId" element={<PostDetail />} />

                    {/* 예측 */}
                    <Route path="/predictions" element={<Predictions />} />
                    <Route path="/predictions/match/:matchId" element={<PredictionDetail />} />
                    <Route path="/predictions/ranking" element={<PredictionRanking />} />

                    {/* 실시간 */}
                    <Route path="/live" element={<Live />} />

                    {/* 뉴스 */}
                    <Route path="/news" element={<News />} />

                    {/* 마이페이지 */}
                    <Route path="/mypage" element={<MyPage />} />

                    {/* 관리자 페이지 - 관리자 권한 필요 */}
                    <Route
                        path="/admin/*"
                        element={
                            <ProtectedRoute is_Admin={true}>
                                <AdminLayout />
                            </ProtectedRoute>
                        }
                    />

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