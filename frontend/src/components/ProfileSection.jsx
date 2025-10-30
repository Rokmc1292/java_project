import React from 'react';
import '../styles/ProfileSection.css';

/**
 * 프로필 섹션 컴포넌트
 * 프로필 이미지, 닉네임, 티어, 점수 진행바, 가입일 표시
 */
const ProfileSection = ({ profile }) => {
  /**
   * 티어별 색상 반환
   */
  const getTierColor = (tier) => {
    const colors = {
      BRONZE: '#CD7F32',
      SILVER: '#C0C0C0',
      GOLD: '#FFD700',
      PLATINUM: '#00CED1',
      DIAMOND: '#B9F2FF'
    };
    return colors[tier] || '#CD7F32';
  };

  /**
   * 티어 진행률 계산 (퍼센트)
   */
  const calculateProgress = () => {
    if (!profile.nextTierScore) return 100; // 최고 티어는 100%

    const tierScoreRanges = {
      BRONZE: 0,
      SILVER: 100,
      GOLD: 300,
      PLATINUM: 600,
      DIAMOND: 1000
    };

    const currentTierMin = tierScoreRanges[profile.tier] || 0;
    const scoreInCurrentTier = profile.tierScore - currentTierMin;
    const tierRange = profile.nextTierScore - currentTierMin;

    if (tierRange === 0) return 100; // 범위가 0이면 100% 반환

    const progress = (scoreInCurrentTier / tierRange) * 100;
    return Math.max(0, Math.min(100, progress)); // 0-100 사이로 제한
  };

  /**
   * 날짜 포맷팅 (YYYY.MM.DD)
   */
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
  };

  const progress = calculateProgress();

  return (
    <div className="profile-section">
      {/* 프로필 카드 */}
      <div className="profile-card">
        <div className="profile-main">
          <img 
            src={profile.profileImage} 
            alt="프로필" 
            className="profile-image"
          />
          <div className="profile-info">
            <h3>{profile.nickname}</h3>
            <p className="join-date">가입일: {formatDate(profile.createdAt)}</p>
          </div>
        </div>

        {/* 티어 정보 */}
        <div className="tier-section">
          <div className="tier-header">
            <span 
              className="tier-badge"
              style={{ backgroundColor: getTierColor(profile.tier) }}
            >
              {profile.tier}
            </span>
            <span className="tier-score">
              {profile.tierScore} 점
            </span>
          </div>

          {/* 티어 진행 바 */}
          <div className="tier-progress-container">
            <div 
              className="tier-progress-bar"
              style={{ 
                width: `${progress}%`,
                backgroundColor: getTierColor(profile.tier)
              }}
            >
              <span className="tier-progress-text">
                {Math.round(progress)}%
              </span>
            </div>
          </div>

          {/* 다음 티어 정보 */}
          {profile.nextTierScore && (
            <p className="next-tier-info">
              다음 티어까지 {profile.nextTierScore - profile.tierScore}점 남음
            </p>
          )}
          {!profile.nextTierScore && (
            <p className="next-tier-info max-tier">
              최고 티어 달성! 🎉
            </p>
          )}
        </div>
      </div>

      {/* 티어 시스템 안내 */}
      <div className="tier-info-card">
        <h4>티어 시스템 안내</h4>
        <div className="tier-ranges">
          <div className="tier-range">
            <span className="tier-name" style={{ color: getTierColor('BRONZE') }}>
              브론즈
            </span>
            <span className="tier-score-range">0 - 99점</span>
          </div>
          <div className="tier-range">
            <span className="tier-name" style={{ color: getTierColor('SILVER') }}>
              실버
            </span>
            <span className="tier-score-range">100 - 299점</span>
          </div>
          <div className="tier-range">
            <span className="tier-name" style={{ color: getTierColor('GOLD') }}>
              골드
            </span>
            <span className="tier-score-range">300 - 599점</span>
          </div>
          <div className="tier-range">
            <span className="tier-name" style={{ color: getTierColor('PLATINUM') }}>
              플래티넘
            </span>
            <span className="tier-score-range">600 - 999점</span>
          </div>
          <div className="tier-range">
            <span className="tier-name" style={{ color: getTierColor('DIAMOND') }}>
              다이아
            </span>
            <span className="tier-score-range">1000점 이상</span>
          </div>
        </div>
        <p className="tier-guide">
          💡 승부예측 성공 시 +10점, 실패 시 -10점
        </p>
      </div>
    </div>
  );
};

export default ProfileSection;