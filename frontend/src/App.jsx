import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useEffect } from 'react';
import Header from './components/Header';
import Footer from './components/Footer';
import Home from './pages/Home';
import Community from './pages/Community';
import PostDetail from './pages/PostDetail';
import News from './pages/News';
import Fixtures from './pages/Fixtures';
import Predictions from './pages/Predictions';
import Live from './pages/Live';
import Login from './pages/Login';
import Signup from './pages/Signup';
import MyPage from './pages/MyPage';
import AdminPage from './pages/AdminPage';

function App() {
    // 앱 초기 로드 시 로그인 상태 초기화
    useEffect(() => {
        localStorage.removeItem('user');
    }, []);

    return (
        <Router>
            <div className="min-h-screen bg-gray-900">
                <Header />
                <main>
                    <Routes>
                        <Route path="/" element={<Home />} />
                        <Route path="/fixtures" element={<Fixtures />} />
                        <Route path="/board" element={<Community />} />
                        <Route path="/board/:postId" element={<PostDetail />} />
                        <Route path="/predictions" element={<Predictions />} />
                        <Route path="/live" element={<Live />} />
                        <Route path="/news" element={<News />} />
                        <Route path="/login" element={<Login />} />
                        <Route path="/signup" element={<Signup />} />
                        <Route path="/mypage" element={<MyPage />} />
                        <Route path="/admin" element={<AdminPage />} />
                    </Routes>
                </main>
                <Footer />
            </div>
        </Router>
    );
}

export default App;