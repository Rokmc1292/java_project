import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import mypageApi from '../api/mypageApi';
import ProfileSection from '../components/ProfileSection';
import StatsSection from '../components/StatsSection';
import ActivitySection from '../components/ActivitySection';
import SettingsSection from '../components/SettingsSection';
import '../styles/MyPage.css';

/**
 * 마이페이지 메인 컴포넌트
 * 프로필, 통계, 활동 내역, 설정 탭으로 구성
 */
const MyPage = () => {
  const navigate = useNavigate();

  // 현재 활성화된 탭 (profile, stats, activity, settings)
  const [activeTab, setActiveTab] = useState('profile');
  
  // 프로필 정보
  const [profile, setProfile] = useState(null);
  
  // 예측 통계
  const [predictionStats, setPredictionStats] = useState(null);
  
  // 최근 10경기 결과
  const [recentResults, setRecentResults] = useState([]);
  
  // 로딩 상태
  const [loading, setLoading] = useState(true);

  // 컴포넌트 마운트 시 인증 체크 및 데이터 로드
  useEffect(() => {
    // 로그인 체크
    const userData = localStorage.getItem('user');
    if (!userData) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    loadMyPageData();
  }, []);

  /**
   * 마이페이지 기본 데이터 로드
   * 프로필, 통계, 최근 결과 한번에 로드
   */
  const loadMyPageData = async () => {
    try {
      setLoading(true);
      
      // 병렬로 데이터 로드
      const [profileData, statsData, resultsData] = await Promise.all([
        mypageApi.getUserProfile(),
        mypageApi.getPredictionStats(),
        mypageApi.getRecentPredictionResults()
      ]);

      setProfile(profileData);
      setPredictionStats(statsData);
      setRecentResults(resultsData);
    } catch (error) {
      console.error('마이페이지 데이터 로드 실패:', error);
      alert('데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 로그아웃 처리
   */
  const handleLogout = () => {
    // 로컬 스토리지에서 사용자 정보 삭제
    localStorage.removeItem('user');
    // 로그인 페이지로 이동
    navigate('/login');
  };

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="mypage-loading">
          <div className="spinner"></div>
          <p>로딩 중...</p>
        </div>
      </>
    );
  }

  return (
    <>
      <Navbar />
      <div className="mypage-container">
      {/* 상단 프로필 헤더 - 모든 탭에서 보임 */}
      <div className="mypage-header">
        {profile && (
          <>
            <img 
              src={profile.profileImage} 
              alt="프로필" 
              className="profile-image-large"
            />
            <div className="profile-header-info">
              <h2>{profile.nickname}</h2>
              <div className="tier-badge-large">{profile.tier}</div>
            </div>
          </>
        )}
      </div>

      {/* 탭 네비게이션 */}
      <div className="mypage-tabs">
        <button
          className={`tab-button ${activeTab === 'profile' ? 'active' : ''}`}
          onClick={() => setActiveTab('profile')}
        >
          프로필
        </button>
        <button
          className={`tab-button ${activeTab === 'stats' ? 'active' : ''}`}
          onClick={() => setActiveTab('stats')}
        >
          통계
        </button>
        <button
          className={`tab-button ${activeTab === 'activity' ? 'active' : ''}`}
          onClick={() => setActiveTab('activity')}
        >
          활동 내역
        </button>
        <button
          className={`tab-button ${activeTab === 'settings' ? 'active' : ''}`}
          onClick={() => setActiveTab('settings')}
        >
          설정
        </button>
      </div>

      {/* 탭 컨텐츠 */}
      <div className="mypage-content">
        {activeTab === 'profile' && profile && (
          <ProfileSection profile={profile} />
        )}

        {activeTab === 'stats' && predictionStats && (
          <StatsSection 
            stats={predictionStats} 
            recentResults={recentResults} 
          />
        )}

        {activeTab === 'activity' && (
          <ActivitySection />
        )}

        {activeTab === 'settings' && (
          <SettingsSection onLogout={handleLogout} />
        )}
      </div>
    </div>
    </>
  );
};

export default MyPage;