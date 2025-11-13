import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import '../styles/AdminPage.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 관리자 페이지
 * - 대시보드: 전체 통계, 최근 가입자
 * - 사용자 관리: 활성화/비활성화, 관리자 권한 부여
 * - 신고 관리: 신고 승인/거부
 * - 게시글 관리: 블라인드 처리
 */
function AdminPage() {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('dashboard');
    const [loading, setLoading] = useState(false);

    // 대시보드 데이터
    const [dashboardStats, setDashboardStats] = useState(null);

    // 사용자 관리
    const [users, setUsers] = useState([]);
    const [usersPage, setUsersPage] = useState(0);
    const [usersTotalPages, setUsersTotalPages] = useState(0);

    // 신고 관리
    const [reports, setReports] = useState([]);
    const [reportsPage, setReportsPage] = useState(0);
    const [reportsTotalPages, setReportsTotalPages] = useState(0);
    const [reportStatusFilter, setReportStatusFilter] = useState('PENDING');

    // 게시글 관리
    const [posts, setPosts] = useState([]);
    const [postsPage, setPostsPage] = useState(0);
    const [postsTotalPages, setPostsTotalPages] = useState(0);

    // 크롤링 관리
    const [crawlLoading, setCrawlLoading] = useState({});

    // 관리자 권한 체크
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
            alert('사용자 정보 오류');
            navigate('/login');
            return;
        }
    }, [navigate]);

    // 탭 변경 시 데이터 로드
    useEffect(() => {
        loadTabData();
    }, [activeTab, usersPage, reportsPage, postsPage, reportStatusFilter]);

    const loadTabData = async () => {
        setLoading(true);
        try {
            if (activeTab === 'dashboard') {
                await loadDashboard();
            } else if (activeTab === 'users') {
                await loadUsers();
            } else if (activeTab === 'reports') {
                await loadReports();
            } else if (activeTab === 'posts') {
                await loadPosts();
            }
        } catch (error) {
            console.error('데이터 로드 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    // 대시보드 로드
    const loadDashboard = async () => {
        const response = await fetch(`${API_BASE_URL}/api/admin/page/dashboard`);
        const data = await response.json();
        setDashboardStats(data);
    };

    // 사용자 목록 로드
    const loadUsers = async () => {
        const response = await fetch(
            `${API_BASE_URL}/api/admin/page/users?page=${usersPage}&size=20`
        );
        const data = await response.json();
        setUsers(data.content || []);
        setUsersTotalPages(data.totalPages || 0);
    };

    // 신고 목록 로드
    const loadReports = async () => {
        const response = await fetch(
            `${API_BASE_URL}/api/admin/page/reports?page=${reportsPage}&size=20&status=${reportStatusFilter}`
        );
        const data = await response.json();
        setReports(data.content || []);
        setReportsTotalPages(data.totalPages || 0);
    };

    // 게시글 목록 로드
    const loadPosts = async () => {
        const response = await fetch(
            `${API_BASE_URL}/api/admin/page/posts?page=${postsPage}&size=20`
        );
        const data = await response.json();
        setPosts(data.content || []);
        setPostsTotalPages(data.totalPages || 0);
    };

    // 관리자 권한 부여/해제
    const toggleAdminRole = async (userId) => {
        if (!window.confirm('관리자 권한을 변경하시겠습니까?')) return;

        try {
            const response = await fetch(
                `${API_BASE_URL}/api/admin/page/users/${userId}/admin`,
                { method: 'PUT' }
            );
            const data = await response.json();
            alert(data.message);
            loadUsers();
        } catch (error) {
            alert('권한 변경 실패');
        }
    };

    // 사용자 상태 변경
    const toggleUserStatus = async (userId) => {
        if (!window.confirm('사용자 상태를 변경하시겠습니까?')) return;

        try {
            const response = await fetch(
                `${API_BASE_URL}/api/admin/page/users/${userId}/status`,
                { method: 'PUT' }
            );
            const data = await response.json();
            alert(data.message);
            loadUsers();
        } catch (error) {
            alert('상태 변경 실패');
        }
    };

    // 신고 처리
    const processReport = async (reportId, action) => {
        if (!window.confirm(`신고를 ${action === 'PROCESSED' ? '승인' : '거부'}하시겠습니까?`)) return;

        try {
            const response = await fetch(
                `${API_BASE_URL}/api/admin/page/reports/${reportId}/process`,
                {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ action })
                }
            );
            const data = await response.json();
            alert(data.message);
            loadReports();
        } catch (error) {
            alert('신고 처리 실패');
        }
    };

    // 게시글 블라인드 처리
    const blindPost = async (postId) => {
        if (!window.confirm('게시글 블라인드 상태를 변경하시겠습니까?')) return;

        try {
            const response = await fetch(
                `${API_BASE_URL}/api/admin/page/posts/${postId}/blind`,
                { method: 'PUT' }
            );
            const data = await response.json();
            alert(data.message);
            loadPosts();
        } catch (error) {
            alert('블라인드 처리 실패');
        }
    };

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    };

    // 크롤링 실행
    const handleCrawl = async (league) => {
        if (!window.confirm(`${league} 크롤링을 시작하시겠습니까?`)) return;

        setCrawlLoading(prev => ({ ...prev, [league]: true }));

        try {
            const endpoint = league === 'all-leagues'
                ? '/api/admin/crawl/all-leagues'
                : `/api/admin/crawl/${league}`;

            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                method: 'POST'
            });
            const data = await response.json();

            if (data.success) {
                alert(data.message);
            } else {
                alert(`크롤링 실패: ${data.message}`);
            }
        } catch (error) {
            console.error('크롤링 실행 실패:', error);
            alert('크롤링 실행 중 오류가 발생했습니다.');
        } finally {
            setCrawlLoading(prev => ({ ...prev, [league]: false }));
        }
    };

    // 실시간 점수 업데이트
    const handleLiveUpdate = async (league) => {
        if (!window.confirm(`${league} 실시간 점수 업데이트를 시작하시겠습니까?`)) return;

        setCrawlLoading(prev => ({ ...prev, [`live-${league}`]: true }));

        try {
            const response = await fetch(`${API_BASE_URL}/api/admin/live/${league}`, {
                method: 'POST'
            });
            const data = await response.json();

            if (data.success) {
                alert(data.message);
            } else {
                alert(`업데이트 실패: ${data.message}`);
            }
        } catch (error) {
            console.error('실시간 업데이트 실패:', error);
            alert('실시간 업데이트 중 오류가 발생했습니다.');
        } finally {
            setCrawlLoading(prev => ({ ...prev, [`live-${league}`]: false }));
        }
    };

    return (
        <div>
            <Navbar />
            <div className="admin-container">
                <h1 className="admin-title">🛠️ 관리자 페이지</h1>

                {/* 탭 네비게이션 */}
                <div className="admin-tabs">
                    <button
                        className={`admin-tab ${activeTab === 'dashboard' ? 'active' : ''}`}
                        onClick={() => setActiveTab('dashboard')}
                    >
                        📊 대시보드
                    </button>
                    <button
                        className={`admin-tab ${activeTab === 'users' ? 'active' : ''}`}
                        onClick={() => setActiveTab('users')}
                    >
                        👥 사용자 관리
                    </button>
                    <button
                        className={`admin-tab ${activeTab === 'reports' ? 'active' : ''}`}
                        onClick={() => setActiveTab('reports')}
                    >
                        🚨 신고 관리
                    </button>
                    <button
                        className={`admin-tab ${activeTab === 'posts' ? 'active' : ''}`}
                        onClick={() => setActiveTab('posts')}
                    >
                        📝 게시글 관리
                    </button>
                    <button
                        className={`admin-tab ${activeTab === 'crawl' ? 'active' : ''}`}
                        onClick={() => setActiveTab('crawl')}
                    >
                        🔄 크롤링 관리
                    </button>
                </div>

                {loading && (
                    <div className="admin-loading">
                        <div className="spinner"></div>
                        <p>로딩 중...</p>
                    </div>
                )}

                {/* 대시보드 */}
                {!loading && activeTab === 'dashboard' && dashboardStats && (
                    <div className="dashboard-content">
                        <div className="stats-grid">
                            <div className="stat-card">
                                <div className="stat-icon">👥</div>
                                <div className="stat-info">
                                    <div className="stat-value">{dashboardStats.totalUsers}</div>
                                    <div className="stat-label">전체 사용자</div>
                                    <div className="stat-sub">오늘 +{dashboardStats.todayUsers}</div>
                                </div>
                            </div>

                            <div className="stat-card">
                                <div className="stat-icon">📝</div>
                                <div className="stat-info">
                                    <div className="stat-value">{dashboardStats.totalPosts}</div>
                                    <div className="stat-label">전체 게시글</div>
                                    <div className="stat-sub">오늘 +{dashboardStats.todayPosts}</div>
                                </div>
                            </div>

                            <div className="stat-card">
                                <div className="stat-icon">💬</div>
                                <div className="stat-info">
                                    <div className="stat-value">{dashboardStats.totalComments}</div>
                                    <div className="stat-label">전체 댓글</div>
                                    <div className="stat-sub">오늘 +{dashboardStats.todayComments}</div>
                                </div>
                            </div>

                            <div className="stat-card">
                                <div className="stat-icon">🎯</div>
                                <div className="stat-info">
                                    <div className="stat-value">{dashboardStats.totalPredictions}</div>
                                    <div className="stat-label">전체 예측</div>
                                    <div className="stat-sub">오늘 +{dashboardStats.todayPredictions}</div>
                                </div>
                            </div>

                            <div className="stat-card">
                                <div className="stat-icon">⚽</div>
                                <div className="stat-info">
                                    <div className="stat-value">{dashboardStats.totalMatches}</div>
                                    <div className="stat-label">전체 경기</div>
                                </div>
                            </div>

                            <div className="stat-card alert">
                                <div className="stat-icon">🚨</div>
                                <div className="stat-info">
                                    <div className="stat-value">{dashboardStats.pendingReports}</div>
                                    <div className="stat-label">처리 대기 신고</div>
                                </div>
                            </div>
                        </div>

                        <div className="recent-users-section">
                            <h2>📋 최근 가입자</h2>
                            <table className="admin-table">
                                <thead>
                                <tr>
                                    <th>아이디</th>
                                    <th>닉네임</th>
                                    <th>이메일</th>
                                    <th>티어</th>
                                    <th>가입일</th>
                                </tr>
                                </thead>
                                <tbody>
                                {dashboardStats.recentUsers.map(user => (
                                    <tr key={user.userId}>
                                        <td>{user.username}</td>
                                        <td>{user.nickname}</td>
                                        <td>{user.email}</td>
                                        <td><span className="tier-badge">{user.tier}</span></td>
                                        <td>{formatDate(user.createdAt)}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}

                {/* 사용자 관리 */}
                {!loading && activeTab === 'users' && (
                    <div className="users-content">
                        <table className="admin-table">
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>아이디</th>
                                <th>닉네임</th>
                                <th>이메일</th>
                                <th>티어</th>
                                <th>게시글</th>
                                <th>댓글</th>
                                <th>예측</th>
                                <th>관리자</th>
                                <th>상태</th>
                                <th>가입일</th>
                                <th>관리</th>
                            </tr>
                            </thead>
                            <tbody>
                            {users.map(user => (
                                <tr key={user.userId}>
                                    <td>{user.userId}</td>
                                    <td>{user.username}</td>
                                    <td>{user.nickname}</td>
                                    <td>{user.email}</td>
                                    <td><span className="tier-badge">{user.tier}</span></td>
                                    <td>{user.postCount}</td>
                                    <td>{user.commentCount}</td>
                                    <td>{user.predictionCount}</td>
                                    <td>
                      <span className={`status-badge ${user.isAdmin ? 'admin' : 'user'}`}>
                        {user.isAdmin ? '관리자' : '일반'}
                      </span>
                                    </td>
                                    <td>
                      <span className={`status-badge ${user.isActive ? 'active' : 'inactive'}`}>
                        {user.isActive ? '활성' : '비활성'}
                      </span>
                                    </td>
                                    <td>{formatDate(user.createdAt)}</td>
                                    <td>
                                        <div className="action-buttons">
                                            <button
                                                onClick={() => toggleAdminRole(user.userId)}
                                                className="admin-action-btn"
                                            >
                                                {user.isAdmin ? '권한 해제' : '관리자 지정'}
                                            </button>
                                            <button
                                                onClick={() => toggleUserStatus(user.userId)}
                                                className="admin-action-btn"
                                            >
                                                {user.isActive ? '비활성화' : '활성화'}
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>

                        {usersTotalPages > 1 && (
                            <div className="admin-pagination">
                                <button
                                    disabled={usersPage === 0}
                                    onClick={() => setUsersPage(usersPage - 1)}
                                >
                                    이전
                                </button>
                                <span>{usersPage + 1} / {usersTotalPages}</span>
                                <button
                                    disabled={usersPage >= usersTotalPages - 1}
                                    onClick={() => setUsersPage(usersPage + 1)}
                                >
                                    다음
                                </button>
                            </div>
                        )}
                    </div>
                )}

                {/* 신고 관리 */}
                {!loading && activeTab === 'reports' && (
                    <div className="reports-content">
                        <div className="filter-section">
                            <label>상태 필터:</label>
                            <select
                                value={reportStatusFilter}
                                onChange={(e) => {
                                    setReportStatusFilter(e.target.value);
                                    setReportsPage(0);
                                }}
                            >
                                <option value="">전체</option>
                                <option value="PENDING">처리 대기</option>
                                <option value="PROCESSED">처리 완료</option>
                                <option value="REJECTED">거부됨</option>
                            </select>
                        </div>

                        <table className="admin-table">
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>유형</th>
                                <th>신고 대상 내용</th>
                                <th>작성자</th>
                                <th>신고자</th>
                                <th>사유</th>
                                <th>설명</th>
                                <th>상태</th>
                                <th>신고일</th>
                                <th>관리</th>
                            </tr>
                            </thead>
                            <tbody>
                            {reports.map(report => (
                                <tr key={report.reportId}>
                                    <td>{report.reportId}</td>
                                    <td><span className="type-badge">{report.targetType}</span></td>
                                    <td className="description-cell">
                                        {report.targetDeleted ? (
                                            <span style={{color: '#999', fontStyle: 'italic'}}>
                                                [삭제된 {report.targetType === 'COMMENT' ? '댓글' : '게시글'}]
                                            </span>
                                        ) : (
                                            <div>
                                                <strong>{report.targetType === 'COMMENT' ? '댓글' : '게시글'} #{report.targetId}:</strong>
                                                <div style={{marginTop: '4px', color: '#555'}}>
                                                    {report.targetContent || '(내용 없음)'}
                                                </div>
                                            </div>
                                        )}
                                    </td>
                                    <td>{report.targetAuthor || '-'}</td>
                                    <td>{report.reporter.nickname}</td>
                                    <td>{report.reason}</td>
                                    <td className="description-cell">{report.description}</td>
                                    <td>
                      <span className={`status-badge ${report.status.toLowerCase()}`}>
                        {report.status === 'PENDING' ? '대기' :
                            report.status === 'PROCESSED' ? '완료' : '거부'}
                      </span>
                                    </td>
                                    <td>{formatDate(report.createdAt)}</td>
                                    <td>
                                        {report.status === 'PENDING' && !report.targetDeleted && (
                                            <div className="action-buttons">
                                                <button
                                                    onClick={() => processReport(report.reportId, 'PROCESSED')}
                                                    className="admin-action-btn approve"
                                                    title={report.targetType === 'COMMENT' ? '댓글 삭제' : '게시글 블라인드'}
                                                >
                                                    {report.targetType === 'COMMENT' ? '댓글 삭제' : '승인'}
                                                </button>
                                                <button
                                                    onClick={() => processReport(report.reportId, 'REJECTED')}
                                                    className="admin-action-btn reject"
                                                >
                                                    거부
                                                </button>
                                            </div>
                                        )}
                                        {report.targetDeleted && (
                                            <span style={{color: '#999', fontSize: '12px'}}>이미 삭제됨</span>
                                        )}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>

                        {reportsTotalPages > 1 && (
                            <div className="admin-pagination">
                                <button
                                    disabled={reportsPage === 0}
                                    onClick={() => setReportsPage(reportsPage - 1)}
                                >
                                    이전
                                </button>
                                <span>{reportsPage + 1} / {reportsTotalPages}</span>
                                <button
                                    disabled={reportsPage >= reportsTotalPages - 1}
                                    onClick={() => setReportsPage(reportsPage + 1)}
                                >
                                    다음
                                </button>
                            </div>
                        )}
                    </div>
                )}

                {/* 게시글 관리 */}
                {!loading && activeTab === 'posts' && (
                    <div className="posts-content">
                        <table className="admin-table">
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>제목</th>
                                <th>카테고리</th>
                                <th>작성자</th>
                                <th>조회</th>
                                <th>추천</th>
                                <th>댓글</th>
                                <th>상태</th>
                                <th>작성일</th>
                                <th>관리</th>
                            </tr>
                            </thead>
                            <tbody>
                            {posts.map(post => (
                                <tr key={post.postId}>
                                    <td>{post.postId}</td>
                                    <td className="title-cell">{post.title}</td>
                                    <td>{post.categoryName}</td>
                                    <td>{post.nickname}</td>
                                    <td>{post.viewCount}</td>
                                    <td>{post.likeCount}</td>
                                    <td>{post.commentCount}</td>
                                    <td>
                      <span className={`status-badge ${post.isBlinded ? 'blinded' : 'normal'}`}>
                        {post.isBlinded ? '블라인드' : '정상'}
                      </span>
                                    </td>
                                    <td>{formatDate(post.createdAt)}</td>
                                    <td>
                                        <button
                                            onClick={() => blindPost(post.postId)}
                                            className="admin-action-btn"
                                        >
                                            {post.isBlinded ? '해제' : '블라인드'}
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>

                        {postsTotalPages > 1 && (
                            <div className="admin-pagination">
                                <button
                                    disabled={postsPage === 0}
                                    onClick={() => setPostsPage(postsPage - 1)}
                                >
                                    이전
                                </button>
                                <span>{postsPage + 1} / {postsTotalPages}</span>
                                <button
                                    disabled={postsPage >= postsTotalPages - 1}
                                    onClick={() => setPostsPage(postsPage + 1)}
                                >
                                    다음
                                </button>
                            </div>
                        )}
                    </div>
                )}

                {/* 크롤링 관리 */}
                {!loading && activeTab === 'crawl' && (
                    <div className="crawl-content">
                        {/* 전체 리그 크롤링 */}
                        <div className="crawl-section-highlight">
                            <h2>전체 리그 크롤링</h2>
                            <div className="crawl-card-all">
                                <div className="crawl-info">
                                    <h3>모든 리그 일괄 크롤링</h3>
                                    <p>EPL, NBA, Bundesliga, La Liga, Serie A, Ligue 1, KBL 전체 리그의 일정을 순차적으로 크롤링합니다.</p>
                                    <p className="warning-text">완료까지 상당한 시간이 소요될 수 있습니다.</p>
                                </div>
                                <button
                                    className="crawl-btn-all"
                                    onClick={() => handleCrawl('all-leagues')}
                                    disabled={crawlLoading['all-leagues']}
                                >
                                    {crawlLoading['all-leagues'] ? '크롤링 중...' : '전체 리그 크롤링 시작'}
                                </button>
                            </div>
                        </div>

                        {/* 개별 리그 크롤링 */}
                        <div className="crawl-section">
                            <h2>개별 리그 크롤링</h2>
                            <div className="crawl-grid">
                                {/* EPL */}
                                <div className="crawl-card">
                                    <h3>EPL (프리미어리그)</h3>
                                    <p>잉글랜드 프리미어리그 일정 크롤링</p>
                                    <div className="crawl-actions">
                                        <button
                                            className="crawl-btn"
                                            onClick={() => handleCrawl('epl')}
                                            disabled={crawlLoading['epl']}
                                        >
                                            {crawlLoading['epl'] ? '크롤링 중...' : '일정 크롤링'}
                                        </button>
                                        <button
                                            className="live-btn"
                                            onClick={() => handleLiveUpdate('epl')}
                                            disabled={crawlLoading['live-epl']}
                                        >
                                            {crawlLoading['live-epl'] ? '업데이트 중...' : '실시간 업데이트'}
                                        </button>
                                    </div>
                                </div>

                                {/* NBA */}
                                <div className="crawl-card">
                                    <h3>NBA</h3>
                                    <p>미국 프로농구 일정 크롤링</p>
                                    <div className="crawl-actions">
                                        <button
                                            className="crawl-btn"
                                            onClick={() => handleCrawl('nba')}
                                            disabled={crawlLoading['nba']}
                                        >
                                            {crawlLoading['nba'] ? '크롤링 중...' : '일정 크롤링'}
                                        </button>
                                        <button
                                            className="live-btn"
                                            onClick={() => handleLiveUpdate('nba')}
                                            disabled={crawlLoading['live-nba']}
                                        >
                                            {crawlLoading['live-nba'] ? '업데이트 중...' : '실시간 업데이트'}
                                        </button>
                                    </div>
                                </div>

                                {/* Bundesliga */}
                                <div className="crawl-card">
                                    <h3>Bundesliga (분데스리가)</h3>
                                    <p>독일 분데스리가 일정 크롤링</p>
                                    <div className="crawl-actions">
                                        <button
                                            className="crawl-btn"
                                            onClick={() => handleCrawl('bundesliga')}
                                            disabled={crawlLoading['bundesliga']}
                                        >
                                            {crawlLoading['bundesliga'] ? '크롤링 중...' : '일정 크롤링'}
                                        </button>
                                        <button
                                            className="live-btn"
                                            onClick={() => handleLiveUpdate('bundesliga')}
                                            disabled={crawlLoading['live-bundesliga']}
                                        >
                                            {crawlLoading['live-bundesliga'] ? '업데이트 중...' : '실시간 업데이트'}
                                        </button>
                                    </div>
                                </div>

                                {/* La Liga */}
                                <div className="crawl-card">
                                    <h3>La Liga (라리가)</h3>
                                    <p>스페인 라리가 일정 크롤링</p>
                                    <div className="crawl-actions">
                                        <button
                                            className="crawl-btn"
                                            onClick={() => handleCrawl('laliga')}
                                            disabled={crawlLoading['laliga']}
                                        >
                                            {crawlLoading['laliga'] ? '크롤링 중...' : '일정 크롤링'}
                                        </button>
                                        <button
                                            className="live-btn"
                                            onClick={() => handleLiveUpdate('laliga')}
                                            disabled={crawlLoading['live-laliga']}
                                        >
                                            {crawlLoading['live-laliga'] ? '업데이트 중...' : '실시간 업데이트'}
                                        </button>
                                    </div>
                                </div>

                                {/* Serie A */}
                                <div className="crawl-card">
                                    <h3>Serie A (세리에 A)</h3>
                                    <p>이탈리아 세리에 A 일정 크롤링</p>
                                    <div className="crawl-actions">
                                        <button
                                            className="crawl-btn"
                                            onClick={() => handleCrawl('seriea')}
                                            disabled={crawlLoading['seriea']}
                                        >
                                            {crawlLoading['seriea'] ? '크롤링 중...' : '일정 크롤링'}
                                        </button>
                                        <button
                                            className="live-btn"
                                            onClick={() => handleLiveUpdate('seriea')}
                                            disabled={crawlLoading['live-seriea']}
                                        >
                                            {crawlLoading['live-seriea'] ? '업데이트 중...' : '실시간 업데이트'}
                                        </button>
                                    </div>
                                </div>

                                {/* Ligue 1 */}
                                <div className="crawl-card">
                                    <h3>Ligue 1 (리그 1)</h3>
                                    <p>프랑스 리그 1 일정 크롤링</p>
                                    <div className="crawl-actions">
                                        <button
                                            className="crawl-btn"
                                            onClick={() => handleCrawl('ligue1')}
                                            disabled={crawlLoading['ligue1']}
                                        >
                                            {crawlLoading['ligue1'] ? '크롤링 중...' : '일정 크롤링'}
                                        </button>
                                        <button
                                            className="live-btn"
                                            onClick={() => handleLiveUpdate('ligue1')}
                                            disabled={crawlLoading['live-ligue1']}
                                        >
                                            {crawlLoading['live-ligue1'] ? '업데이트 중...' : '실시간 업데이트'}
                                        </button>
                                    </div>
                                </div>

                                {/* KBL */}
                                <div className="crawl-card">
                                    <h3>KBL (한국프로농구)</h3>
                                    <p>한국 프로농구 일정 크롤링</p>
                                    <div className="crawl-actions">
                                        <button
                                            className="crawl-btn"
                                            onClick={() => handleCrawl('kbl')}
                                            disabled={crawlLoading['kbl']}
                                        >
                                            {crawlLoading['kbl'] ? '크롤링 중...' : '일정 크롤링'}
                                        </button>
                                        <button
                                            className="live-btn"
                                            onClick={() => handleLiveUpdate('kbl')}
                                            disabled={crawlLoading['live-kbl']}
                                        >
                                            {crawlLoading['live-kbl'] ? '업데이트 중...' : '실시간 업데이트'}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* 안내 메시지 */}
                        <div className="crawl-notice">
                            <h3>크롤링 안내</h3>
                            <ul>
                                <li>크롤링은 백그라운드에서 실행되며, 완료까지 수 분이 소요될 수 있습니다.</li>
                                <li>전체 리그 크롤링은 모든 리그를 순차적으로 실행하므로 상당한 시간이 소요됩니다.</li>
                                <li>실시간 업데이트는 현재 진행 중인 경기의 점수를 업데이트합니다.</li>
                                <li>크롤링 중 브라우저를 닫아도 서버에서 계속 실행됩니다.</li>
                            </ul>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default AdminPage;