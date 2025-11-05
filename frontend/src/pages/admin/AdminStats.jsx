/**
 * 관리자 통계 페이지
 * 전체 시스템 통계 및 최근 활동 표시
 *
 * 파일 위치: frontend/src/pages/admin/AdminStats.jsx
 */

import { useState, useEffect } from 'react';

function AdminStats() {
    const [stats, setStats] = useState({
        users: { total: 0, newToday: 0, active: 0 },
        posts: { total: 0, today: 0, popular: 0 },
        predictions: { total: 0, today: 0, accuracy: 0 },
        reports: { pending: 0, resolved: 0, total: 0 }
    });

    const [recentActivities, setRecentActivities] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadStats();
        loadRecentActivities();
    }, []);

    const loadStats = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/admin/stats/detail', {
                credentials: 'include'
            });
            if (response.ok) {
                const data = await response.json();
                setStats(data);
            }
        } catch (error) {
            console.error('통계 로드 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const loadRecentActivities = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/admin/activities/recent', {
                credentials: 'include'
            });
            if (response.ok) {
                const data = await response.json();
                setRecentActivities(data);
            }
        } catch (error) {
            console.error('최근 활동 로드 실패:', error);
        }
    };

    if (loading) {
        return <div className="loading">로딩 중...</div>;
    }

    return (
        <div className="admin-stats-page">
            <h2>시스템 통계</h2>

            {/* 통계 카드 그리드 */}
            <div className="stats-grid">
                {/* 사용자 통계 */}
                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-icon">👥</span>
                        <h3>사용자</h3>
                    </div>
                    <div className="stat-body">
                        <div className="stat-main">
                            <span className="stat-number">{stats.users.total}</span>
                            <span className="stat-label">전체 사용자</span>
                        </div>
                        <div className="stat-details">
                            <div className="stat-detail-item">
                                <span className="detail-label">오늘 가입</span>
                                <span className="detail-value new">{stats.users.newToday}</span>
                            </div>
                            <div className="stat-detail-item">
                                <span className="detail-label">활성 사용자</span>
                                <span className="detail-value">{stats.users.active}</span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* 게시글 통계 */}
                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-icon">📝</span>
                        <h3>게시글</h3>
                    </div>
                    <div className="stat-body">
                        <div className="stat-main">
                            <span className="stat-number">{stats.posts.total}</span>
                            <span className="stat-label">전체 게시글</span>
                        </div>
                        <div className="stat-details">
                            <div className="stat-detail-item">
                                <span className="detail-label">오늘 작성</span>
                                <span className="detail-value new">{stats.posts.today}</span>
                            </div>
                            <div className="stat-detail-item">
                                <span className="detail-label">인기 게시글</span>
                                <span className="detail-value">{stats.posts.popular}</span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* 예측 통계 */}
                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-icon">🎯</span>
                        <h3>승부예측</h3>
                    </div>
                    <div className="stat-body">
                        <div className="stat-main">
                            <span className="stat-number">{stats.predictions.total}</span>
                            <span className="stat-label">전체 예측</span>
                        </div>
                        <div className="stat-details">
                            <div className="stat-detail-item">
                                <span className="detail-label">오늘 예측</span>
                                <span className="detail-value new">{stats.predictions.today}</span>
                            </div>
                            <div className="stat-detail-item">
                                <span className="detail-label">평균 정확도</span>
                                <span className="detail-value">{stats.predictions.accuracy}%</span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* 신고 통계 */}
                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-icon">🚨</span>
                        <h3>신고</h3>
                    </div>
                    <div className="stat-body">
                        <div className="stat-main">
                            <span className="stat-number">{stats.reports.total}</span>
                            <span className="stat-label">전체 신고</span>
                        </div>
                        <div className="stat-details">
                            <div className="stat-detail-item">
                                <span className="detail-label">미처리</span>
                                <span className="detail-value pending">{stats.reports.pending}</span>
                            </div>
                            <div className="stat-detail-item">
                                <span className="detail-label">처리 완료</span>
                                <span className="detail-value">{stats.reports.resolved}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* 최근 활동 */}
            <div className="recent-activities">
                <h3>최근 활동</h3>
                {recentActivities.length === 0 ? (
                    <div className="no-activities">최근 활동이 없습니다.</div>
                ) : (
                    <div className="activities-list">
                        {recentActivities.map((activity, index) => (
                            <div key={index} className="activity-item">
                                <span className="activity-icon">{activity.icon}</span>
                                <div className="activity-content">
                                    <p className="activity-text">{activity.text}</p>
                                    <span className="activity-time">{activity.time}</span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

export default AdminStats;