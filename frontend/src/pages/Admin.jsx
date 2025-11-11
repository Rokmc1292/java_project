import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import '../styles/Admin.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 관리자 페이지
 * - 대시보드
 * - 신고 관리
 * - 사용자 관리
 */
function Admin() {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('dashboard');
    const [loading, setLoading] = useState(false);

    // 대시보드 데이터
    const [dashboardStats, setDashboardStats] = useState(null);

    // 신고 데이터
    const [reports, setReports] = useState([]);
    const [reportsPage, setReportsPage] = useState(0);
    const [reportsTotalPages, setReportsTotalPages] = useState(0);
    const [reportStatus, setReportStatus] = useState('PENDING');

    // 사용자 데이터
    const [users, setUsers] = useState([]);
    const [usersPage, setUsersPage] = useState(0);
    const [usersTotalPages, setUsersTotalPages] = useState(0);
    const [userSearch, setUserSearch] = useState('');

    // 관리자 권한 확인
    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (!userData) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }

        try {
            const user = JSON.parse(userData);
            if (!user.isAdmin) {
                alert('관리자 권한이 필요합니다.');
                navigate('/');
                return;
            }
        } catch (e) {
            alert('로그인 정보가 올바르지 않습니다.');
            navigate('/login');
            return;
        }

        loadTabData();
    }, [activeTab]);

    // 탭 데이터 로드
    const loadTabData = () => {
        if (activeTab === 'dashboard') {
            loadDashboard();
        } else if (activeTab === 'reports') {
            loadReports();
        } else if (activeTab === 'users') {
            loadUsers();
        }
    };

    // 대시보드 로드
    const loadDashboard = async () => {
        setLoading(true);
        try {
            const response = await fetch(`${API_BASE_URL}/api/admin-page/dashboard/stats`, {
                credentials: 'include'
            });

            if (!response.ok) throw new Error('대시보드 로드 실패');

            const data = await response.json();
            setDashboardStats(data);
        } catch (error) {
            console.error('대시보드 로드 실패:', error);
            alert('대시보드를 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 신고 목록 로드
    const loadReports = async () => {
        setLoading(true);
        try {
            const response = await fetch(
                `${API_BASE_URL}/api/admin-page/reports?page=${reportsPage}&size=20&status=${reportStatus}`,
                { credentials: 'include' }
            );

            if (!response.ok) throw new Error('신고 목록 로드 실패');

            const data = await response.json();
            setReports(data.content || []);
            setReportsTotalPages(data.totalPages || 0);
        } catch (error) {
            console.error('신고 목록 로드 실패:', error);
            alert('신고 목록을 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 사용자 목록 로드
    const loadUsers = async () => {
        setLoading(true);
        try {
            const searchParam = userSearch ? `&search=${encodeURIComponent(userSearch)}` : '';
            const response = await fetch(
                `${API_BASE_URL}/api/admin-page/users?page=${usersPage}&size=20${searchParam}`,
                { credentials: 'include' }
            );

            if (!response.ok) throw new Error('사용자 목록 로드 실패');

            const data = await response.json();
            setUsers(data.content || []);
            setUsersTotalPages(data.totalPages || 0);
        } catch (error) {
            console.error('사용자 목록 로드 실패:', error);
            alert('사용자 목록을 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 신고 처리
    const handleProcessReport = async (reportId, action) => {
        if (!window.confirm(`이 신고를 ${action === 'ACCEPT' ? '승인' : '거부'}하시겠습니까?`)) {
            return;
        }

        try {
            const response = await fetch(
                `${API_BASE_URL}/api/admin-page/reports/${reportId}/process`,
                {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify({ action })
                }
            );

            if (!response.ok) throw new Error('신고 처리 실패');

            alert('신고가 처리되었습니다.');
            loadReports();
        } catch (error) {
            console.error('신고 처리 실패:', error);
            alert('신고 처리에 실패했습니다.');
        }
    };

    // 사용자 활성화/비활성화
    const handleToggleUserActive = async (userId) => {
        try {
            const response = await fetch(
                `${API_BASE_URL}/api/admin-page/users/${userId}/toggle-active`,
                {
                    method: 'PUT',
                    credentials: 'include'
                }
            );

            if (!response.ok) throw new Error('상태 변경 실패');

            const data = await response.json();
            alert(data.message);
            loadUsers();
        } catch (error) {
            console.error('사용자 상태 변경 실패:', error);
            alert('사용자 상태 변경에 실패했습니다.');
        }
    };

    // 사용자 관리자 권한 토글
    const handleToggleUserAdmin = async (userId) => {
        if (!window.confirm('정말 관리자 권한을 변경하시겠습니까?')) {
            return;
        }

        try {
            const response = await fetch(
                `${API_BASE_URL}/api/admin-page/users/${userId}/toggle-admin`,
                {
                    method: 'PUT',
                    credentials: 'include'
                }
            );

            if (!response.ok) throw new Error('권한 변경 실패');

            const data = await response.json();
            alert(data.message);
            loadUsers();
        } catch (error) {
            console.error('관리자 권한 변경 실패:', error);
            alert('관리자 권한 변경에 실패했습니다.');
        }
    };

    return (
        <div>
            <Navbar />
            <div className="admin-container">
                {/* 헤더 */}
                <div className="admin-header">
                    <h1>🔧 관리자 페이지</h1>
                    <p>시스템 관리 및 모니터링</p>
                </div>

                {/* 탭 네비게이션 */}
                <div className="admin-tabs">
                    <button
                        className={`admin-tab ${activeTab === 'dashboard' ? 'active' : ''}`}
                        onClick={() => setActiveTab('dashboard')}
                    >
                        📊 대시보드
                    </button>
                    <button
                        className={`admin-tab ${activeTab === 'reports' ? 'active' : ''}`}
                        onClick={() => setActiveTab('reports')}
                    >
                        🚨 신고 관리
                    </button>
                    <button
                        className={`admin-tab ${activeTab === 'users' ? 'active' : ''}`}
                        onClick={() => setActiveTab('users')}
                    >
                        👥 사용자 관리
                    </button>
                </div>

                {/* 탭 컨텐츠 */}
                {loading ? (
                    <div className="admin-loading">
                        <div className="spinner"></div>
                        <p>로딩 중...</p>
                    </div>
                ) : (
                    <div className="admin-content">
                        {/* 대시보드 */}
                        {activeTab === 'dashboard' && dashboardStats && (
                            <div className="dashboard-section">
                                {/* 통계 카드 */}
                                <div className="stats-grid">
                                    <div className="stat-card">
                                        <div className="stat-icon">👥</div>
                                        <div className="stat-info">
                                            <h3>{dashboardStats.totalUsers}</h3>
                                            <p>총 사용자</p>
                                            <span className="stat-change">+{dashboardStats.todayUsers} 오늘</span>
                                        </div>
                                    </div>

                                    <div className="stat-card">
                                        <div className="stat-icon">📝</div>
                                        <div className="stat-info">
                                            <h3>{dashboardStats.totalPosts}</h3>
                                            <p>총 게시글</p>
                                            <span className="stat-change">+{dashboardStats.todayPosts} 오늘</span>
                                        </div>
                                    </div>

                                    <div className="stat-card">
                                        <div className="stat-icon">💬</div>
                                        <div className="stat-info">
                                            <h3>{dashboardStats.totalComments}</h3>
                                            <p>총 댓글</p>
                                        </div>
                                    </div>

                                    <div className="stat-card">
                                        <div className="stat-icon">🎯</div>
                                        <div className="stat-info">
                                            <h3>{dashboardStats.totalPredictions}</h3>
                                            <p>총 예측</p>
                                        </div>
                                    </div>

                                    <div className="stat-card">
                                        <div className="stat-icon">⚽</div>
                                        <div className="stat-info">
                                            <h3>{dashboardStats.totalMatches}</h3>
                                            <p>총 경기</p>
                                        </div>
                                    </div>

                                    <div className="stat-card alert">
                                        <div className="stat-icon">🚨</div>
                                        <div className="stat-info">
                                            <h3>{dashboardStats.pendingReports}</h3>
                                            <p>대기중 신고</p>
                                        </div>
                                    </div>
                                </div>

                                {/* 가입자 추이 차트 */}
                                <div className="chart-card">
                                    <h2>📈 최근 7일 가입자 추이</h2>
                                    <div className="user-growth-chart">
                                        {dashboardStats.userGrowth.map((data, index) => (
                                            <div key={index} className="chart-bar-container">
                                                <div className="chart-bar" style={{ height: `${data.count * 20}px` }}>
                                                    <span className="bar-value">{data.count}</span>
                                                </div>
                                                <div className="chart-label">{data.date.slice(5)}</div>
                                            </div>
                                        ))}
                                    </div>
                                </div>

                                {/* 최근 활동 */}
                                <div className="activity-card">
                                    <h2>🔔 최근 활동</h2>
                                    <div className="activity-list">
                                        {dashboardStats.recentActivities.map((activity, index) => (
                                            <div key={index} className="activity-item">
                                                <span className="activity-type">{activity.type === 'POST' ? '📝' : '💬'}</span>
                                                <div className="activity-content">
                                                    <strong>{activity.user}</strong>님이{' '}
                                                    {activity.type === 'POST' ? '게시글을 작성' : '댓글을 작성'}했습니다
                                                    <p className="activity-detail">{activity.content}</p>
                                                </div>
                                                <span className="activity-time">
                          {new Date(activity.createdAt).toLocaleString('ko-KR')}
                        </span>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* 신고 관리 */}
                        {activeTab === 'reports' && (
                            <div className="reports-section">
                                {/* 필터 */}
                                <div className="reports-filter">
                                    <button
                                        className={`filter-btn ${reportStatus === 'PENDING' ? 'active' : ''}`}
                                        onClick={() => { setReportStatus('PENDING'); setReportsPage(0); }}
                                    >
                                        대기중
                                    </button>
                                    <button
                                        className={`filter-btn ${reportStatus === 'PROCESSED' ? 'active' : ''}`}
                                        onClick={() => { setReportStatus('PROCESSED'); setReportsPage(0); }}
                                    >
                                        처리완료
                                    </button>
                                    <button
                                        className={`filter-btn ${reportStatus === 'REJECTED' ? 'active' : ''}`}
                                        onClick={() => { setReportStatus('REJECTED'); setReportsPage(0); }}
                                    >
                                        거부
                                    </button>
                                </div>

                                {/* 신고 목록 */}
                                <div className="reports-list">
                                    {reports.length === 0 ? (
                                        <div className="empty-state">신고가 없습니다.</div>
                                    ) : (
                                        reports.map(report => (
                                            <div key={report.reportId} className="report-card">
                                                <div className="report-header">
                          <span className={`report-type ${report.targetType.toLowerCase()}`}>
                            {report.targetType === 'POST' ? '📝 게시글' : '💬 댓글'}
                          </span>
                                                    <span className="report-status">{report.status}</span>
                                                </div>
                                                <div className="report-body">
                                                    <p><strong>신고자:</strong> {report.reporterNickname}</p>
                                                    <p><strong>신고 사유:</strong> {report.reason}</p>
                                                    {report.description && (
                                                        <p><strong>상세 내용:</strong> {report.description}</p>
                                                    )}
                                                    <p className="report-time">
                                                        {new Date(report.createdAt).toLocaleString('ko-KR')}
                                                    </p>
                                                </div>
                                                {report.status === 'PENDING' && (
                                                    <div className="report-actions">
                                                        <button
                                                            className="btn-accept"
                                                            onClick={() => handleProcessReport(report.reportId, 'ACCEPT')}
                                                        >
                                                            승인
                                                        </button>
                                                        <button
                                                            className="btn-reject"
                                                            onClick={() => handleProcessReport(report.reportId, 'REJECT')}
                                                        >
                                                            거부
                                                        </button>
                                                    </div>
                                                )}
                                            </div>
                                        ))
                                    )}
                                </div>

                                {/* 페이지네이션 */}
                                {reportsTotalPages > 1 && (
                                    <div className="pagination">
                                        <button
                                            onClick={() => setReportsPage(Math.max(0, reportsPage - 1))}
                                            disabled={reportsPage === 0}
                                        >
                                            이전
                                        </button>
                                        <span>{reportsPage + 1} / {reportsTotalPages}</span>
                                        <button
                                            onClick={() => setReportsPage(Math.min(reportsTotalPages - 1, reportsPage + 1))}
                                            disabled={reportsPage >= reportsTotalPages - 1}
                                        >
                                            다음
                                        </button>
                                    </div>
                                )}
                            </div>
                        )}

                        {/* 사용자 관리 */}
                        {activeTab === 'users' && (
                            <div className="users-section">
                                {/* 검색 */}
                                <div className="users-search">
                                    <input
                                        type="text"
                                        placeholder="아이디, 닉네임, 이메일 검색..."
                                        value={userSearch}
                                        onChange={(e) => setUserSearch(e.target.value)}
                                        onKeyPress={(e) => e.key === 'Enter' && loadUsers()}
                                    />
                                    <button onClick={loadUsers}>검색</button>
                                </div>

                                {/* 사용자 테이블 */}
                                <div className="users-table">
                                    <table>
                                        <thead>
                                        <tr>
                                            <th>아이디</th>
                                            <th>닉네임</th>
                                            <th>이메일</th>
                                            <th>티어</th>
                                            <th>활동</th>
                                            <th>가입일</th>
                                            <th>상태</th>
                                            <th>관리</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {users.map(user => (
                                            <tr key={user.userId}>
                                                <td>{user.username}</td>
                                                <td>{user.nickname}</td>
                                                <td>{user.email}</td>
                                                <td>
                            <span className={`tier-badge ${user.tier.toLowerCase()}`}>
                              {user.tier}
                            </span>
                                                </td>
                                                <td className="user-activity">
                                                    <span>게시글: {user.postCount}</span>
                                                    <span>댓글: {user.commentCount}</span>
                                                    <span>예측: {user.predictionCount}</span>
                                                </td>
                                                <td>{new Date(user.createdAt).toLocaleDateString('ko-KR')}</td>
                                                <td>
                            <span className={`status-badge ${user.isActive ? 'active' : 'inactive'}`}>
                              {user.isActive ? '활성' : '비활성'}
                            </span>
                                                    {user.isAdmin && <span className="admin-badge">관리자</span>}
                                                </td>
                                                <td className="user-actions">
                                                    <button
                                                        className="btn-toggle"
                                                        onClick={() => handleToggleUserActive(user.userId)}
                                                    >
                                                        {user.isActive ? '비활성화' : '활성화'}
                                                    </button>
                                                    <button
                                                        className="btn-admin"
                                                        onClick={() => handleToggleUserAdmin(user.userId)}
                                                    >
                                                        {user.isAdmin ? '관리자 해제' : '관리자 지정'}
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>

                                {/* 페이지네이션 */}
                                {usersTotalPages > 1 && (
                                    <div className="pagination">
                                        <button
                                            onClick={() => setUsersPage(Math.max(0, usersPage - 1))}
                                            disabled={usersPage === 0}
                                        >
                                            이전
                                        </button>
                                        <span>{usersPage + 1} / {usersTotalPages}</span>
                                        <button
                                            onClick={() => setUsersPage(Math.min(usersTotalPages - 1, usersPage + 1))}
                                            disabled={usersPage >= usersTotalPages - 1}
                                        >
                                            다음
                                        </button>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}

export default Admin;