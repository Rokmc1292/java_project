import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import TabMenu from '../components/TabMenu';
import HomeTab from '../components/tabs/HomeTab';
import CommunityTab from '../components/tabs/CommunityTab';
import LiveTab from '../components/tabs/LiveTab';
import PredictionTab from '../components/tabs/PredictionTab';
import ScheduleTab from '../components/tabs/ScheduleTab';
import NewsTab from '../components/tabs/NewsTab';

/**
 * 메인 대시보드 페이지
 * 로그인 후 보이는 메인 화면
 * 탭 네비게이션으로 각 기능 페이지 전환
 */
function Dashboard() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [activeTab, setActiveTab] = useState('home');

  // 컴포넌트 마운트 시 로그인 확인
  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (!storedUser) {
      // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }
    setUser(JSON.parse(storedUser));
  }, [navigate]);

  // 사용자 정보 로딩 중
  if (!user) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        fontSize: '20px'
      }}>
        로딩중...
      </div>
    );
  }

  // 활성화된 탭에 따라 컴포넌트 렌더링
  const renderTabContent = () => {
    switch (activeTab) {
      case 'home':
        return <HomeTab />;
      case 'community':
        return <CommunityTab />;
      case 'live':
        return <LiveTab />;
      case 'prediction':
        return <PredictionTab />;
      case 'schedule':
        return <ScheduleTab />;
      case 'news':
        return <NewsTab />;
      default:
        return <HomeTab />;
    }
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#1a1a1a' }}>
      {/* 상단 네비게이션 바 */}
      <Navbar user={user} />
      
      {/* 탭 메뉴 */}
      <TabMenu activeTab={activeTab} setActiveTab={setActiveTab} />
      
      {/* 메인 콘텐츠 영역 */}
      <main style={{ 
        maxWidth: '1400px', 
        margin: '0 auto',
        minHeight: 'calc(100vh - 130px)'
      }}>
        {renderTabContent()}
      </main>

      {/* 푸터 */}
      <footer style={{
        backgroundColor: '#0a0a0a',
        padding: '20px',
        textAlign: 'center',
        color: '#666',
        borderTop: '1px solid #333',
        marginTop: '40px'
      }}>
        <p>© 2025 Sports Community Platform. All rights reserved.</p>
      </footer>
    </div>
  );
}

export default Dashboard;