import React, { useState, useEffect } from 'react';
import { Search, Filter, Shield, Ban, Edit, Eye, ChevronLeft, ChevronRight } from 'lucide-react';

const AdminUsers = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedUser, setSelectedUser] = useState(null);
    const [showDetail, setShowDetail] = useState(false);

    // 필터 상태
    const [search, setSearch] = useState('');
    const [tierFilter, setTierFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        fetchUsers();
    }, [search, tierFilter, statusFilter, page]);

    const fetchUsers = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: '20',
                ...(search && { search }),
                ...(tierFilter && { tier: tierFilter }),
                ...(statusFilter && { status: statusFilter })
            });

            const response = await fetch(`http://localhost:8080/api/admin-page/users?${params}`, {
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                setUsers(data.content);
                setTotalPages(data.totalPages);
            }
        } catch (error) {
            console.error('사용자 목록 로딩 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleUserClick = async (userId) => {
        try {
            const response = await fetch(`http://localhost:8080/api/admin-page/users/${userId}`, {
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                setSelectedUser(data);
                setShowDetail(true);
            }
        } catch (error) {
            console.error('사용자 상세 정보 로딩 실패:', error);
        }
    };

    const handleStatusToggle = async (userId, currentStatus) => {
        if (!confirm(`정말로 이 계정을 ${currentStatus ? '비활성화' : '활성화'}하시겠습니까?`)) {
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/admin-page/users/${userId}/status`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ isActive: !currentStatus })
            });

            if (response.ok) {
                alert('계정 상태가 변경되었습니다.');
                fetchUsers();
            }
        } catch (error) {
            console.error('계정 상태 변경 실패:', error);
        }
    };

    const handleAdminToggle = async (userId, currentStatus) => {
        if (!confirm(`정말로 관리자 권한을 ${currentStatus ? '회수' : '부여'}하시겠습니까?`)) {
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/admin-page/users/${userId}/admin`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ isAdmin: !currentStatus })
            });

            if (response.ok) {
                alert('관리자 권한이 변경되었습니다.');
                fetchUsers();
            }
        } catch (error) {
            console.error('관리자 권한 변경 실패:', error);
        }
    };

    const handleTierScoreUpdate = async (userId) => {
        const newScore = prompt('새로운 티어 점수를 입력하세요:');
        if (!newScore) return;

        try {
            const response = await fetch(`http://localhost:8080/api/admin-page/users/${userId}/tier-score`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ tierScore: parseInt(newScore) })
            });

            if (response.ok) {
                alert('티어 점수가 변경되었습니다.');
                fetchUsers();
            }
        } catch (error) {
            console.error('티어 점수 변경 실패:', error);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* 헤더 */}
                <div className="mb-6">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">사용자 관리</h1>
                    <p className="text-gray-600">전체 회원을 조회하고 관리합니다</p>
                </div>

                {/* 필터 및 검색 */}
                <div className="bg-white rounded-lg shadow p-6 mb-6">
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                        {/* 검색 */}
                        <div className="md:col-span-2">
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                <Search className="w-4 h-4 inline mr-1" />
                                검색
                            </label>
                            <input
                                type="text"
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                placeholder="아이디, 닉네임, 이메일 검색"
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                        </div>

                        {/* 티어 필터 */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                <Filter className="w-4 h-4 inline mr-1" />
                                티어
                            </label>
                            <select
                                value={tierFilter}
                                onChange={(e) => setTierFilter(e.target.value)}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            >
                                <option value="">전체</option>
                                <option value="BRONZE">BRONZE</option>
                                <option value="SILVER">SILVER</option>
                                <option value="GOLD">GOLD</option>
                                <option value="PLATINUM">PLATINUM</option>
                                <option value="DIAMOND">DIAMOND</option>
                            </select>
                        </div>

                        {/* 상태 필터 */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">상태</label>
                            <select
                                value={statusFilter}
                                onChange={(e) => setStatusFilter(e.target.value)}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            >
                                <option value="">전체</option>
                                <option value="active">활성</option>
                                <option value="inactive">비활성</option>
                            </select>
                        </div>
                    </div>
                </div>

                {/* 사용자 목록 테이블 */}
                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    사용자
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    티어
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    활동
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    상태
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    가입일
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    관리
                                </th>
                            </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                            {loading ? (
                                <tr>
                                    <td colSpan="6" className="px-6 py-4 text-center text-gray-500">
                                        로딩 중...
                                    </td>
                                </tr>
                            ) : users.length === 0 ? (
                                <tr>
                                    <td colSpan="6" className="px-6 py-4 text-center text-gray-500">
                                        사용자가 없습니다
                                    </td>
                                </tr>
                            ) : (
                                users.map((user) => (
                                    <tr key={user.userId} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div>
                                                <div className="flex items-center gap-2">
                                                    <div className="text-sm font-medium text-gray-900">
                                                        {user.nickname}
                                                    </div>
                                                    {user.isAdmin && (
                                                        <span className="px-2 py-1 text-xs font-semibold text-red-600 bg-red-100 rounded">
                                관리자
                              </span>
                                                    )}
                                                </div>
                                                <div className="text-sm text-gray-500">{user.username}</div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="flex items-center gap-2">
                          <span className={`px-2 py-1 text-xs font-semibold rounded ${getTierColor(user.tier)}`}>
                            {user.tier}
                          </span>
                                                <span className="text-sm text-gray-600">{user.tierScore}</span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm text-gray-900">
                                                게시글 {user.postCount} / 댓글 {user.commentCount}
                                            </div>
                                            <div className="text-sm text-gray-500">
                                                예측 {user.predictionCount}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            {user.isActive ? (
                                                <span className="px-2 py-1 text-xs font-semibold text-green-800 bg-green-100 rounded">
                            활성
                          </span>
                                            ) : (
                                                <span className="px-2 py-1 text-xs font-semibold text-red-800 bg-red-100 rounded">
                            비활성
                          </span>
                                            )}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {new Date(user.createdAt).toLocaleDateString('ko-KR')}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm">
                                            <div className="flex gap-2">
                                                <button
                                                    onClick={() => handleUserClick(user.userId)}
                                                    className="p-1 text-blue-600 hover:bg-blue-50 rounded"
                                                    title="상세보기"
                                                >
                                                    <Eye className="w-5 h-5" />
                                                </button>
                                                <button
                                                    onClick={() => handleTierScoreUpdate(user.userId)}
                                                    className="p-1 text-green-600 hover:bg-green-50 rounded"
                                                    title="티어 점수 수정"
                                                >
                                                    <Edit className="w-5 h-5" />
                                                </button>
                                                <button
                                                    onClick={() => handleStatusToggle(user.userId, user.isActive)}
                                                    className="p-1 text-orange-600 hover:bg-orange-50 rounded"
                                                    title={user.isActive ? '비활성화' : '활성화'}
                                                >
                                                    <Ban className="w-5 h-5" />
                                                </button>
                                                <button
                                                    onClick={() => handleAdminToggle(user.userId, user.isAdmin)}
                                                    className="p-1 text-red-600 hover:bg-red-50 rounded"
                                                    title={user.isAdmin ? '관리자 권한 회수' : '관리자 권한 부여'}
                                                >
                                                    <Shield className="w-5 h-5" />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>

                    {/* 페이지네이션 */}
                    <div className="bg-gray-50 px-6 py-4 flex items-center justify-between border-t">
                        <div className="text-sm text-gray-700">
                            페이지 {page + 1} / {totalPages}
                        </div>
                        <div className="flex gap-2">
                            <button
                                onClick={() => setPage(p => Math.max(0, p - 1))}
                                disabled={page === 0}
                                className="px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                <ChevronLeft className="w-5 h-5" />
                            </button>
                            <button
                                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                                disabled={page >= totalPages - 1}
                                className="px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                <ChevronRight className="w-5 h-5" />
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* 사용자 상세 모달 */}
            {showDetail && selectedUser && (
                <UserDetailModal
                    user={selectedUser}
                    onClose={() => {
                        setShowDetail(false);
                        setSelectedUser(null);
                    }}
                />
            )}
        </div>
    );
};

// 티어 색상
const getTierColor = (tier) => {
    const colors = {
        BRONZE: 'text-orange-800 bg-orange-100',
        SILVER: 'text-gray-800 bg-gray-100',
        GOLD: 'text-yellow-800 bg-yellow-100',
        PLATINUM: 'text-cyan-800 bg-cyan-100',
        DIAMOND: 'text-blue-800 bg-blue-100'
    };
    return colors[tier] || 'text-gray-800 bg-gray-100';
};

// 사용자 상세 모달
const UserDetailModal = ({ user, onClose }) => {
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                <div className="sticky top-0 bg-white border-b px-6 py-4 flex items-center justify-between">
                    <h2 className="text-xl font-bold">사용자 상세 정보</h2>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600"
                    >
                        ✕
                    </button>
                </div>

                <div className="p-6 space-y-6">
                    {/* 기본 정보 */}
                    <div>
                        <h3 className="text-lg font-semibold mb-3">기본 정보</h3>
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <div className="text-sm text-gray-500">아이디</div>
                                <div className="font-medium">{user.username}</div>
                            </div>
                            <div>
                                <div className="text-sm text-gray-500">닉네임</div>
                                <div className="font-medium">{user.nickname}</div>
                            </div>
                            <div>
                                <div className="text-sm text-gray-500">이메일</div>
                                <div className="font-medium">{user.email}</div>
                            </div>
                            <div>
                                <div className="text-sm text-gray-500">티어</div>
                                <div className="font-medium">{user.tier} ({user.tierScore}점)</div>
                            </div>
                            <div>
                                <div className="text-sm text-gray-500">가입일</div>
                                <div className="font-medium">
                                    {new Date(user.createdAt).toLocaleDateString('ko-KR')}
                                </div>
                            </div>
                            <div>
                                <div className="text-sm text-gray-500">상태</div>
                                <div className="font-medium">
                                    {user.isActive ? '활성' : '비활성'} / {user.isAdmin ? '관리자' : '일반 회원'}
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* 활동 통계 */}
                    {user.activityStats && (
                        <div>
                            <h3 className="text-lg font-semibold mb-3">활동 통계</h3>
                            <div className="grid grid-cols-2 gap-4">
                                <div className="bg-blue-50 p-4 rounded-lg">
                                    <div className="text-sm text-gray-600">게시글</div>
                                    <div className="text-2xl font-bold text-blue-600">
                                        {user.activityStats.totalPosts}
                                    </div>
                                </div>
                                <div className="bg-green-50 p-4 rounded-lg">
                                    <div className="text-sm text-gray-600">댓글</div>
                                    <div className="text-2xl font-bold text-green-600">
                                        {user.activityStats.totalComments}
                                    </div>
                                </div>
                                <div className="bg-purple-50 p-4 rounded-lg">
                                    <div className="text-sm text-gray-600">예측 참여</div>
                                    <div className="text-2xl font-bold text-purple-600">
                                        {user.activityStats.totalPredictions}
                                    </div>
                                </div>
                                <div className="bg-orange-50 p-4 rounded-lg">
                                    <div className="text-sm text-gray-600">예측 정확도</div>
                                    <div className="text-2xl font-bold text-orange-600">
                                        {user.activityStats.predictionAccuracy?.toFixed(1)}%
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AdminUsers;