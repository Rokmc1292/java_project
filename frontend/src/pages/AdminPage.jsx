import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

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
        <div className="bg-gray-900 text-white min-h-screen">
            <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <h1 className="text-4xl font-bold mb-8">🛠️ 관리자 페이지</h1>

                {/* 탭 네비게이션 */}
                <div className="flex flex-wrap gap-3 mb-8">
                    <button
                        className={`px-6 py-3 rounded-lg font-semibold transition ${
                            activeTab === 'dashboard'
                                ? 'bg-blue-500 text-white shadow-lg'
                                : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
                        }`}
                        onClick={() => setActiveTab('dashboard')}
                    >
                        📊 대시보드
                    </button>
                    <button
                        className={`px-6 py-3 rounded-lg font-semibold transition ${
                            activeTab === 'users'
                                ? 'bg-blue-500 text-white shadow-lg'
                                : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
                        }`}
                        onClick={() => setActiveTab('users')}
                    >
                        👥 사용자 관리
                    </button>
                    <button
                        className={`px-6 py-3 rounded-lg font-semibold transition ${
                            activeTab === 'reports'
                                ? 'bg-blue-500 text-white shadow-lg'
                                : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
                        }`}
                        onClick={() => setActiveTab('reports')}
                    >
                        🚨 신고 관리
                    </button>
                    <button
                        className={`px-6 py-3 rounded-lg font-semibold transition ${
                            activeTab === 'posts'
                                ? 'bg-blue-500 text-white shadow-lg'
                                : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
                        }`}
                        onClick={() => setActiveTab('posts')}
                    >
                        📝 게시글 관리
                    </button>
                    <button
                        className={`px-6 py-3 rounded-lg font-semibold transition ${
                            activeTab === 'crawl'
                                ? 'bg-blue-500 text-white shadow-lg'
                                : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
                        }`}
                        onClick={() => setActiveTab('crawl')}
                    >
                        🔄 크롤링 관리
                    </button>
                </div>

                {loading && (
                    <div className="text-center py-16">
                        <div className="inline-block animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
                        <p className="mt-4 text-gray-400">로딩 중...</p>
                    </div>
                )}

                {/* 대시보드 */}
                {!loading && activeTab === 'dashboard' && dashboardStats && (
                    <div>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                            <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6">
                                <div className="text-4xl mb-3">👥</div>
                                <div className="text-3xl font-bold mb-2">{dashboardStats.totalUsers}</div>
                                <div className="text-gray-400 mb-2">전체 사용자</div>
                                <div className="text-green-400 text-sm">오늘 +{dashboardStats.todayUsers}</div>
                            </div>

                            <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6">
                                <div className="text-4xl mb-3">📝</div>
                                <div className="text-3xl font-bold mb-2">{dashboardStats.totalPosts}</div>
                                <div className="text-gray-400 mb-2">전체 게시글</div>
                                <div className="text-green-400 text-sm">오늘 +{dashboardStats.todayPosts}</div>
                            </div>

                            <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6">
                                <div className="text-4xl mb-3">💬</div>
                                <div className="text-3xl font-bold mb-2">{dashboardStats.totalComments}</div>
                                <div className="text-gray-400 mb-2">전체 댓글</div>
                                <div className="text-green-400 text-sm">오늘 +{dashboardStats.todayComments}</div>
                            </div>

                            <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6">
                                <div className="text-4xl mb-3">🎯</div>
                                <div className="text-3xl font-bold mb-2">{dashboardStats.totalPredictions}</div>
                                <div className="text-gray-400 mb-2">전체 예측</div>
                                <div className="text-green-400 text-sm">오늘 +{dashboardStats.todayPredictions}</div>
                            </div>

                            <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6">
                                <div className="text-4xl mb-3">⚽</div>
                                <div className="text-3xl font-bold mb-2">{dashboardStats.totalMatches}</div>
                                <div className="text-gray-400 mb-2">전체 경기</div>
                            </div>

                            <div className="bg-red-500/20 border border-red-500 rounded-lg p-6">
                                <div className="text-4xl mb-3">🚨</div>
                                <div className="text-3xl font-bold mb-2">{dashboardStats.pendingReports}</div>
                                <div className="text-red-400 mb-2">처리 대기 신고</div>
                            </div>
                        </div>

                        <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6">
                            <h2 className="text-2xl font-bold mb-4">📋 최근 가입자</h2>
                            <div className="overflow-x-auto">
                                <table className="w-full">
                                    <thead>
                                    <tr className="border-b border-gray-700">
                                        <th className="text-left py-3 px-4">아이디</th>
                                        <th className="text-left py-3 px-4">닉네임</th>
                                        <th className="text-left py-3 px-4">이메일</th>
                                        <th className="text-left py-3 px-4">티어</th>
                                        <th className="text-left py-3 px-4">가입일</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {dashboardStats.recentUsers.map(user => (
                                        <tr key={user.userId} className="border-b border-gray-700 hover:bg-gray-700/50">
                                            <td className="py-3 px-4">{user.username}</td>
                                            <td className="py-3 px-4">{user.nickname}</td>
                                            <td className="py-3 px-4 text-sm text-gray-400">{user.email}</td>
                                            <td className="py-3 px-4">
                                                <span className="px-3 py-1 bg-blue-500 text-white text-xs font-bold rounded-full">
                                                    {user.tier}
                                                </span>
                                            </td>
                                            <td className="py-3 px-4 text-sm text-gray-400">{formatDate(user.createdAt)}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                )}

                {/* 사용자 관리 */}
                {!loading && activeTab === 'users' && (
                    <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6">
                        <div className="overflow-x-auto">
                            <table className="w-full text-sm">
                                <thead>
                                <tr className="border-b border-gray-700">
                                    <th className="text-left py-3 px-2">ID</th>
                                    <th className="text-left py-3 px-2">아이디</th>
                                    <th className="text-left py-3 px-2">닉네임</th>
                                    <th className="text-left py-3 px-2">이메일</th>
                                    <th className="text-left py-3 px-2">티어</th>
                                    <th className="text-left py-3 px-2">게시글</th>
                                    <th className="text-left py-3 px-2">댓글</th>
                                    <th className="text-left py-3 px-2">예측</th>
                                    <th className="text-left py-3 px-2">관리자</th>
                                    <th className="text-left py-3 px-2">상태</th>
                                    <th className="text-left py-3 px-2">가입일</th>
                                    <th className="text-left py-3 px-2">관리</th>
                                </tr>
                                </thead>
                                <tbody>
                                {users.map(user => (
                                    <tr key={user.userId} className="border-b border-gray-700 hover:bg-gray-700/50">
                                        <td className="py-3 px-2">{user.userId}</td>
                                        <td className="py-3 px-2">{user.username}</td>
                                        <td className="py-3 px-2">{user.nickname}</td>
                                        <td className="py-3 px-2 text-xs text-gray-400">{user.email}</td>
                                        <td className="py-3 px-2">
                                            <span className="px-2 py-1 bg-blue-500 text-white text-xs font-bold rounded-full">
                                                {user.tier}
                                            </span>
                                        </td>
                                        <td className="py-3 px-2">{user.postCount}</td>
                                        <td className="py-3 px-2">{user.commentCount}</td>
                                        <td className="py-3 px-2">{user.predictionCount}</td>
                                        <td className="py-3 px-2">
                                            <span className={`px-2 py-1 text-xs font-bold rounded-full ${
                                                user.isAdmin ? 'bg-red-500 text-white' : 'bg-gray-600 text-gray-300'
                                            }`}>
                                                {user.isAdmin ? '관리자' : '일반'}
                                            </span>
                                        </td>
                                        <td className="py-3 px-2">
                                            <span className={`px-2 py-1 text-xs font-bold rounded-full ${
                                                user.isActive ? 'bg-green-500 text-white' : 'bg-gray-600 text-gray-300'
                                            }`}>
                                                {user.isActive ? '활성' : '비활성'}
                                            </span>
                                        </td>
                                        <td className="py-3 px-2 text-xs text-gray-400">{formatDate(user.createdAt)}</td>
                                        <td className="py-3 px-2">
                                            <div className="flex flex-col gap-1">
                                                <button
                                                    onClick={() => toggleAdminRole(user.userId)}
                                                    className="px-2 py-1 bg-blue-500 hover:bg-blue-600 text-white text-xs rounded"
                                                >
                                                    {user.isAdmin ? '권한 해제' : '관리자 지정'}
                                                </button>
                                                <button
                                                    onClick={() => toggleUserStatus(user.userId)}
                                                    className="px-2 py-1 bg-gray-600 hover:bg-gray-500 text-white text-xs rounded"
                                                >
                                                    {user.isActive ? '비활성화' : '활성화'}
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        {usersTotalPages > 1 && (
                            <div className="flex justify-center items-center gap-4 mt-6">
                                <button
                                    disabled={usersPage === 0}
                                    onClick={() => setUsersPage(usersPage - 1)}
                                    className={`px-4 py-2 rounded-lg font-semibold transition ${
                                        usersPage === 0
                                            ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                                            : 'bg-gray-700 text-white hover:bg-gray-600'
                                    }`}
                                >
                                    이전
                                </button>
                                <span className="text-gray-400">{usersPage + 1} / {usersTotalPages}</span>
                                <button
                                    disabled={usersPage >= usersTotalPages - 1}
                                    onClick={() => setUsersPage(usersPage + 1)}
                                    className={`px-4 py-2 rounded-lg font-semibold transition ${
                                        usersPage >= usersTotalPages - 1
                                            ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                                            : 'bg-gray-700 text-white hover:bg-gray-600'
                                    }`}
                                >
                                    다음
                                </button>
                            </div>
                        )}
                    </div>
                )}

                {/* 신고 관리 */}
                {!loading && activeTab === 'reports' && (
                    <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6">
                        <div className="mb-4">
                            <label className="text-gray-400 mr-3">상태 필터:</label>
                            <select
                                value={reportStatusFilter}
                                onChange={(e) => {
                                    setReportStatusFilter(e.target.value);
                                    setReportsPage(0);
                                }}
                                className="px-4 py-2 bg-gray-700 text-white rounded-lg border-none focus:ring-2 focus:ring-blue-500"
                            >
                                <option value="">전체</option>
                                <option value="PENDING">처리 대기</option>
                                <option value="PROCESSED">처리 완료</option>
                                <option value="REJECTED">거부됨</option>
                            </select>
                        </div>

                        <div className="overflow-x-auto">
                            <table className="w-full text-sm">
                                <thead>
                                <tr className="border-b border-gray-700">
                                    <th className="text-left py-3 px-2">ID</th>
                                    <th className="text-left py-3 px-2">유형</th>
                                    <th className="text-left py-3 px-2">신고 대상 내용</th>
                                    <th className="text-left py-3 px-2">작성자</th>
                                    <th className="text-left py-3 px-2">신고자</th>
                                    <th className="text-left py-3 px-2">사유</th>
                                    <th className="text-left py-3 px-2">설명</th>
                                    <th className="text-left py-3 px-2">상태</th>
                                    <th className="text-left py-3 px-2">신고일</th>
                                    <th className="text-left py-3 px-2">관리</th>
                                </tr>
                                </thead>
                                <tbody>
                                {reports.map(report => (
                                    <tr key={report.reportId} className="border-b border-gray-700 hover:bg-gray-700/50">
                                        <td className="py-3 px-2">{report.reportId}</td>
                                        <td className="py-3 px-2">
                                            <span className="px-2 py-1 bg-purple-500 text-white text-xs rounded-full">
                                                {report.targetType}
                                            </span>
                                        </td>
                                        <td className="py-3 px-2 max-w-xs">
                                            {report.targetDeleted ? (
                                                <span className="text-gray-500 italic">
                                                    [삭제된 {report.targetType === 'COMMENT' ? '댓글' : '게시글'}]
                                                </span>
                                            ) : (
                                                <div>
                                                    <strong className="text-xs">
                                                        {report.targetType === 'COMMENT' ? '댓글' : '게시글'} #{report.targetId}:
                                                    </strong>
                                                    <div className="mt-1 text-xs text-gray-400 truncate">
                                                        {report.targetContent || '(내용 없음)'}
                                                    </div>
                                                </div>
                                            )}
                                        </td>
                                        <td className="py-3 px-2">{report.targetAuthor || '-'}</td>
                                        <td className="py-3 px-2">{report.reporter.nickname}</td>
                                        <td className="py-3 px-2 text-xs">{report.reason}</td>
                                        <td className="py-3 px-2 text-xs max-w-xs truncate">{report.description}</td>
                                        <td className="py-3 px-2">
                                            <span className={`px-2 py-1 text-xs font-bold rounded-full ${
                                                report.status === 'PENDING' ? 'bg-yellow-500 text-black' :
                                                report.status === 'PROCESSED' ? 'bg-green-500 text-white' : 'bg-red-500 text-white'
                                            }`}>
                                                {report.status === 'PENDING' ? '대기' :
                                                 report.status === 'PROCESSED' ? '완료' : '거부'}
                                            </span>
                                        </td>
                                        <td className="py-3 px-2 text-xs text-gray-400">{formatDate(report.createdAt)}</td>
                                        <td className="py-3 px-2">
                                            {report.status === 'PENDING' && !report.targetDeleted && (
                                                <div className="flex flex-col gap-1">
                                                    <button
                                                        onClick={() => processReport(report.reportId, 'PROCESSED')}
                                                        className="px-2 py-1 bg-green-500 hover:bg-green-600 text-white text-xs rounded"
                                                        title={report.targetType === 'COMMENT' ? '댓글 삭제' : '게시글 블라인드'}
                                                    >
                                                        {report.targetType === 'COMMENT' ? '댓글 삭제' : '승인'}
                                                    </button>
                                                    <button
                                                        onClick={() => processReport(report.reportId, 'REJECTED')}
                                                        className="px-2 py-1 bg-red-500 hover:bg-red-600 text-white text-xs rounded"
                                                    >
                                                        거부
                                                    </button>
                                                </div>
                                            )}
                                            {report.targetDeleted && (
                                                <span className="text-gray-500 text-xs">이미 삭제됨</span>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        {reportsTotalPages > 1 && (
                            <div className="flex justify-center items-center gap-4 mt-6">
                                <button
                                    disabled={reportsPage === 0}
                                    onClick={() => setReportsPage(reportsPage - 1)}
                                    className={`px-4 py-2 rounded-lg font-semibold transition ${
                                        reportsPage === 0
                                            ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                                            : 'bg-gray-700 text-white hover:bg-gray-600'
                                    }`}
                                >
                                    이전
                                </button>
                                <span className="text-gray-400">{reportsPage + 1} / {reportsTotalPages}</span>
                                <button
                                    disabled={reportsPage >= reportsTotalPages - 1}
                                    onClick={() => setReportsPage(reportsPage + 1)}
                                    className={`px-4 py-2 rounded-lg font-semibold transition ${
                                        reportsPage >= reportsTotalPages - 1
                                            ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                                            : 'bg-gray-700 text-white hover:bg-gray-600'
                                    }`}
                                >
                                    다음
                                </button>
                            </div>
                        )}
                    </div>
                )}

                {/* 게시글 관리 */}
                {!loading && activeTab === 'posts' && (
                    <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6">
                        <div className="overflow-x-auto">
                            <table className="w-full text-sm">
                                <thead>
                                <tr className="border-b border-gray-700">
                                    <th className="text-left py-3 px-2">ID</th>
                                    <th className="text-left py-3 px-2">제목</th>
                                    <th className="text-left py-3 px-2">카테고리</th>
                                    <th className="text-left py-3 px-2">작성자</th>
                                    <th className="text-left py-3 px-2">조회</th>
                                    <th className="text-left py-3 px-2">추천</th>
                                    <th className="text-left py-3 px-2">댓글</th>
                                    <th className="text-left py-3 px-2">상태</th>
                                    <th className="text-left py-3 px-2">작성일</th>
                                    <th className="text-left py-3 px-2">관리</th>
                                </tr>
                                </thead>
                                <tbody>
                                {posts.map(post => (
                                    <tr key={post.postId} className="border-b border-gray-700 hover:bg-gray-700/50">
                                        <td className="py-3 px-2">{post.postId}</td>
                                        <td className="py-3 px-2 max-w-xs truncate font-semibold">{post.title}</td>
                                        <td className="py-3 px-2">{post.categoryName}</td>
                                        <td className="py-3 px-2">{post.nickname}</td>
                                        <td className="py-3 px-2">{post.viewCount}</td>
                                        <td className="py-3 px-2">{post.likeCount}</td>
                                        <td className="py-3 px-2">{post.commentCount}</td>
                                        <td className="py-3 px-2">
                                            <span className={`px-2 py-1 text-xs font-bold rounded-full ${
                                                post.isBlinded ? 'bg-red-500 text-white' : 'bg-green-500 text-white'
                                            }`}>
                                                {post.isBlinded ? '블라인드' : '정상'}
                                            </span>
                                        </td>
                                        <td className="py-3 px-2 text-xs text-gray-400">{formatDate(post.createdAt)}</td>
                                        <td className="py-3 px-2">
                                            <button
                                                onClick={() => blindPost(post.postId)}
                                                className="px-3 py-1 bg-gray-600 hover:bg-gray-500 text-white text-xs rounded"
                                            >
                                                {post.isBlinded ? '해제' : '블라인드'}
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        {postsTotalPages > 1 && (
                            <div className="flex justify-center items-center gap-4 mt-6">
                                <button
                                    disabled={postsPage === 0}
                                    onClick={() => setPostsPage(postsPage - 1)}
                                    className={`px-4 py-2 rounded-lg font-semibold transition ${
                                        postsPage === 0
                                            ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                                            : 'bg-gray-700 text-white hover:bg-gray-600'
                                    }`}
                                >
                                    이전
                                </button>
                                <span className="text-gray-400">{postsPage + 1} / {postsTotalPages}</span>
                                <button
                                    disabled={postsPage >= postsTotalPages - 1}
                                    onClick={() => setPostsPage(postsPage + 1)}
                                    className={`px-4 py-2 rounded-lg font-semibold transition ${
                                        postsPage >= postsTotalPages - 1
                                            ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                                            : 'bg-gray-700 text-white hover:bg-gray-600'
                                    }`}
                                >
                                    다음
                                </button>
                            </div>
                        )}
                    </div>
                )}

                {/* 크롤링 관리 */}
                {!loading && activeTab === 'crawl' && (
                    <div>
                        {/* 전체 리그 크롤링 */}
                        <div className="bg-gradient-to-r from-blue-500 to-purple-500 rounded-lg p-6 mb-8">
                            <h2 className="text-2xl font-bold mb-3">전체 리그 크롤링</h2>
                            <p className="mb-2">EPL, NBA, Bundesliga, La Liga, Serie A, Ligue 1, KBL 전체 리그의 일정을 순차적으로 크롤링합니다.</p>
                            <p className="text-yellow-300 text-sm mb-4">완료까지 상당한 시간이 소요될 수 있습니다.</p>
                            <button
                                onClick={() => handleCrawl('all-leagues')}
                                disabled={crawlLoading['all-leagues']}
                                className="px-8 py-3 bg-white text-purple-600 font-bold rounded-lg hover:bg-gray-100 transition disabled:opacity-50"
                            >
                                {crawlLoading['all-leagues'] ? '크롤링 중...' : '전체 리그 크롤링 시작'}
                            </button>
                        </div>

                        {/* 개별 리그 크롤링 */}
                        <h2 className="text-2xl font-bold mb-4">개별 리그 크롤링</h2>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                            {[
                                { id: 'epl', name: 'EPL', desc: '잉글랜드 프리미어리그' },
                                { id: 'nba', name: 'NBA', desc: '미국 프로농구' },
                                { id: 'bundesliga', name: 'Bundesliga', desc: '독일 분데스리가' },
                                { id: 'laliga', name: 'La Liga', desc: '스페인 라리가' },
                                { id: 'seriea', name: 'Serie A', desc: '이탈리아 세리에 A' },
                                { id: 'ligue1', name: 'Ligue 1', desc: '프랑스 리그 1' },
                                { id: 'kbl', name: 'KBL', desc: '한국 프로농구' }
                            ].map(league => (
                                <div key={league.id} className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6">
                                    <h3 className="text-xl font-bold mb-2">{league.name}</h3>
                                    <p className="text-gray-400 text-sm mb-4">{league.desc}</p>
                                    <div className="flex flex-col gap-2">
                                        <button
                                            onClick={() => handleCrawl(league.id)}
                                            disabled={crawlLoading[league.id]}
                                            className="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white font-semibold rounded-lg transition disabled:opacity-50"
                                        >
                                            {crawlLoading[league.id] ? '크롤링 중...' : '일정 크롤링'}
                                        </button>
                                        <button
                                            onClick={() => handleLiveUpdate(league.id)}
                                            disabled={crawlLoading[`live-${league.id}`]}
                                            className="px-4 py-2 bg-green-500 hover:bg-green-600 text-white font-semibold rounded-lg transition disabled:opacity-50"
                                        >
                                            {crawlLoading[`live-${league.id}`] ? '업데이트 중...' : '실시간 업데이트'}
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>

                        {/* 안내 메시지 */}
                        <div className="bg-blue-500/20 border border-blue-500 rounded-lg p-6">
                            <h3 className="text-xl font-bold mb-3">크롤링 안내</h3>
                            <ul className="space-y-2 text-sm text-gray-300">
                                <li>• 크롤링은 백그라운드에서 실행되며, 완료까지 수 분이 소요될 수 있습니다.</li>
                                <li>• 전체 리그 크롤링은 모든 리그를 순차적으로 실행하므로 상당한 시간이 소요됩니다.</li>
                                <li>• 실시간 업데이트는 현재 진행 중인 경기의 점수를 업데이트합니다.</li>
                                <li>• 크롤링 중 브라우저를 닫아도 서버에서 계속 실행됩니다.</li>
                            </ul>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default AdminPage;
