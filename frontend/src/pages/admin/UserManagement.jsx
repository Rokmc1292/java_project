/**
 * 사용자 관리 페이지
 * 사용자 목록, 검색, 정지/해제
 *
 * 파일 위치: frontend/src/pages/admin/UserManagement.jsx
 */

import { useState, useEffect } from 'react';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function UserManagement() {
    const [users, setUsers] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        loadUsers();
    }, [statusFilter, page]);

    const loadUsers = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page,
                size: 20,
                ...(statusFilter !== 'ALL' && { status: statusFilter }),
                ...(searchTerm && { search: searchTerm })
            });

            const response = await fetch(`${API_BASE_URL}/api/admin/users?${params}`, {
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                setUsers(data.content);
                setTotalPages(data.totalPages);
            }
        } catch (error) {
            console.error('사용자 로드 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = () => {
        setPage(0);
        loadUsers();
    };

    const handleBanUser = async (username) => {
        const reason = prompt('정지 사유를 입력하세요:');
        if (!reason) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/admin/users/${username}/ban`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ reason })
            });

            if (response.ok) {
                alert('사용자가 정지되었습니다.');
                loadUsers();
            } else {
                alert('정지 처리에 실패했습니다.');
            }
        } catch (error) {
            console.error('정지 처리 실패:', error);
        }
    };

    const handleUnbanUser = async (username) => {
        if (!window.confirm('정지를 해제하시겠습니까?')) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/admin/users/${username}/unban`, {
                method: 'POST',
                credentials: 'include'
            });

            if (response.ok) {
                alert('정지가 해제되었습니다.');
                loadUsers();
            } else {
                alert('정지 해제에 실패했습니다.');
            }
        } catch (error) {
            console.error('정지 해제 실패:', error);
        }
    };

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

    return (
        <div className="user-management">
            <div className="page-header">
                <h2>👥 사용자 관리</h2>
            </div>

            {/* 검색 및 필터 */}
            <div className="search-filter-section">
                <div className="search-box">
                    <input
                        type="text"
                        placeholder="아이디 또는 닉네임 검색"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                    />
                    <button onClick={handleSearch} className="btn-primary">
                        🔍 검색
                    </button>
                </div>

                <div className="filter-tabs">
                    <button
                        className={`filter-tab ${statusFilter === 'ALL' ? 'active' : ''}`}
                        onClick={() => setStatusFilter('ALL')}
                    >
                        전체
                    </button>
                    <button
                        className={`filter-tab ${statusFilter === 'ACTIVE' ? 'active' : ''}`}
                        onClick={() => setStatusFilter('ACTIVE')}
                    >
                        활성
                    </button>
                    <button
                        className={`filter-tab ${statusFilter === 'BANNED' ? 'active' : ''}`}
                        onClick={() => setStatusFilter('BANNED')}
                    >
                        정지
                    </button>
                </div>
            </div>

            {/* 사용자 목록 */}
            {loading ? (
                <div className="loading">로딩 중...</div>
            ) : users.length === 0 ? (
                <div className="no-data">사용자가 없습니다.</div>
            ) : (
                <>
                    <div className="user-table">
                        <table>
                            <thead>
                            <tr>
                                <th>아이디</th>
                                <th>닉네임</th>
                                <th>이메일</th>
                                <th>티어</th>
                                <th>점수</th>
                                <th>가입일</th>
                                <th>상태</th>
                                <th>관리</th>
                            </tr>
                            </thead>
                            <tbody>
                            {users.map(user => (
                                <tr key={user.username}>
                                    <td>{user.username}</td>
                                    <td>{user.nickname}</td>
                                    <td>{user.email}</td>
                                    <td>
                      <span
                          className="tier-badge"
                          style={{ backgroundColor: getTierColor(user.tier) }}
                      >
                        {user.tier}
                      </span>
                                    </td>
                                    <td>{user.tierScore}</td>
                                    <td>{new Date(user.createdAt).toLocaleDateString()}</td>
                                    <td>
                      <span className={`status-badge ${user.status.toLowerCase()}`}>
                        {user.status === 'ACTIVE' ? '활성' : '정지'}
                      </span>
                                    </td>
                                    <td className="action-cell">
                                        {user.status === 'ACTIVE' ? (
                                            <button
                                                className="btn-sm btn-danger"
                                                onClick={() => handleBanUser(user.username)}
                                            >
                                                정지
                                            </button>
                                        ) : (
                                            <button
                                                className="btn-sm btn-success"
                                                onClick={() => handleUnbanUser(user.username)}
                                            >
                                                해제
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>

                    {/* 페이지네이션 */}
                    {totalPages > 1 && (
                        <div className="pagination">
                            <button
                                onClick={() => setPage(Math.max(0, page - 1))}
                                disabled={page === 0}
                                className="btn-sm"
                            >
                                이전
                            </button>
                            <span className="page-info">
                {page + 1} / {totalPages}
              </span>
                            <button
                                onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                                disabled={page >= totalPages - 1}
                                className="btn-sm"
                            >
                                다음
                            </button>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}

export default UserManagement;