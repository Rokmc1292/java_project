/**
 * 관리자 대시보드 메인 페이지
 * - 통계 요약
 * - 경기 관리
 * - 사용자 관리
 * - 게시글 관리
 * - 신고 관리
 *
 * 파일 위치: frontend/src/pages/admin/AdminDashboard.jsx
 */

import { useState, useEffect } from 'react';
import { Routes, Route, Link, useLocation } from 'react-router-dom';
import Navbar from '../../components/Navbar';
import AdminStats from './AdminStats';
import MatchManagement from './MatchManagement';
import UserManagement from './UserManagement';
import PostManagement from './PostManagement';
import ReportManagement from './ReportManagement';
import '../../styles/AdminDashboard.css';

function AdminDashboard() {
    const location = useLocation();
    const [stats, setStats] = useState({
        totalUsers: 0,
        totalMatches: 0,
        totalPosts: 0,
        totalReports: 0
    });

    useEffect(() => {
        loadStats();
    }, []);

    const loadStats = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/admin/stats', {
                credentials: 'include'
            });
            if (response.ok) {
                const data = await response.json();
                setStats(data);
            }
        } catch (error) {
            console.error('통계 로드 실패:', error);
        }
    };

    const isActive = (path) => {
        return location.pathname === path ? 'active' : '';
    };

    return (
        <div>
            <Navbar />
            <div className="admin-container">
                <div className="admin-header">
                    <h1>🔧 관리자 페이지</h1>
                    <p>시스템 관리 및 콘텐츠 관리</p>
                </div>

                <div className="admin-layout">
                    {/* 사이드바 메뉴 */}
                    <aside className="admin-sidebar">
                        <nav className="admin-nav">
                            <Link
                                to="/admin"
                                className={`admin-nav-item ${isActive('/admin')}`}
                            >
                                📊 대시보드
                            </Link>
                            <Link
                                to="/admin/matches"
                                className={`admin-nav-item ${isActive('/admin/matches')}`}
                            >
                                ⚽ 경기 관리
                            </Link>
                            <Link
                                to="/admin/users"
                                className={`admin-nav-item ${isActive('/admin/users')}`}
                            >
                                👥 사용자 관리
                            </Link>
                            <Link
                                to="/admin/posts"
                                className={`admin-nav-item ${isActive('/admin/posts')}`}
                            >
                                📝 게시글 관리
                            </Link>
                            <Link
                                to="/admin/reports"
                                className={`admin-nav-item ${isActive('/admin/reports')}`}
                            >
                                🚨 신고 관리
                            </Link>
                        </nav>

                        {/* 빠른 통계 */}
                        <div className="quick-stats">
                            <h3>빠른 통계</h3>
                            <div className="stat-item">
                                <span className="stat-label">총 사용자</span>
                                <span className="stat-value">{stats.totalUsers}</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-label">총 경기</span>
                                <span className="stat-value">{stats.totalMatches}</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-label">총 게시글</span>
                                <span className="stat-value">{stats.totalPosts}</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-label">미처리 신고</span>
                                <span className="stat-value pending">{stats.totalReports}</span>
                            </div>
                        </div>
                    </aside>

                    {/* 메인 콘텐츠 영역 */}
                    <main className="admin-content">
                        <Routes>
                            <Route index element={<AdminStats />} />
                            <Route path="matches" element={<MatchManagement />} />
                            <Route path="users" element={<UserManagement />} />
                            <Route path="posts" element={<PostManagement />} />
                            <Route path="reports" element={<ReportManagement />} />
                        </Routes>
                    </main>
                </div>
            </div>
        </div>
    );
}

export default AdminDashboard;