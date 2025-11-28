import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { checkAuth } from '../api/auth';
import mypageApi from '../api/mypageApi';
import ProfileSection from '../components/ProfileSection';
import StatsSection from '../components/StatsSection';
import ActivitySection from '../components/ActivitySection';
import SettingsSection from '../components/SettingsSection';

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
    const verifyAndLoadData = async () => {
      try {
        // 서버 세션 확인
        await checkAuth();
        // 세션이 유효하면 데이터 로드
        loadMyPageData();
      } catch (err) {
        // 세션이 없거나 만료됨
        alert('로그인이 필요합니다.');
        navigate('/login');
      }
    };

    verifyAndLoadData();
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
      <div className="bg-gray-900 min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          <p className="mt-4 text-white text-lg">로딩 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-gray-900 min-h-screen text-white">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">

        {/* 상단 프로필 헤더 - 모든 탭에서 보임 */}
        <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg shadow-xl p-6 mb-8">
          {profile && (
            <div className="flex items-center space-x-6">
              <img
                src={profile.profileImage}
                alt="프로필"
                className="w-24 h-24 rounded-full object-cover border-4 border-blue-500"
              />
              <div>
                <h2 className="text-3xl font-bold mb-2">{profile.nickname}</h2>
                <div className="inline-block bg-gradient-to-r from-yellow-500 to-orange-500 text-white px-4 py-2 rounded-lg font-semibold shadow-lg">
                  {profile.tier}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* 탭 네비게이션 */}
        <div className="flex space-x-2 mb-8 overflow-x-auto">
          <button
            className={`px-6 py-3 rounded-lg font-semibold transition whitespace-nowrap ${
              activeTab === 'profile'
                ? 'bg-blue-500 text-white shadow-lg'
                : 'bg-gray-800/80 backdrop-blur-sm text-gray-300 hover:bg-gray-700'
            }`}
            onClick={() => setActiveTab('profile')}
          >
            프로필
          </button>
          <button
            className={`px-6 py-3 rounded-lg font-semibold transition whitespace-nowrap ${
              activeTab === 'stats'
                ? 'bg-blue-500 text-white shadow-lg'
                : 'bg-gray-800/80 backdrop-blur-sm text-gray-300 hover:bg-gray-700'
            }`}
            onClick={() => setActiveTab('stats')}
          >
            통계
          </button>
          <button
            className={`px-6 py-3 rounded-lg font-semibold transition whitespace-nowrap ${
              activeTab === 'activity'
                ? 'bg-blue-500 text-white shadow-lg'
                : 'bg-gray-800/80 backdrop-blur-sm text-gray-300 hover:bg-gray-700'
            }`}
            onClick={() => setActiveTab('activity')}
          >
            활동 내역
          </button>
          <button
            className={`px-6 py-3 rounded-lg font-semibold transition whitespace-nowrap ${
              activeTab === 'settings'
                ? 'bg-blue-500 text-white shadow-lg'
                : 'bg-gray-800/80 backdrop-blur-sm text-gray-300 hover:bg-gray-700'
            }`}
            onClick={() => setActiveTab('settings')}
          >
            설정
          </button>
        </div>

        {/* 탭 컨텐츠 */}
        <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg shadow-xl p-6">
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
    </div>
  );
};

export default MyPage;
