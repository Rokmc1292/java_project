import React, { useState, useEffect } from 'react';
import { Search, Filter, Eye, EyeOff, Trash2, ChevronLeft, ChevronRight } from 'lucide-react';

const AdminPosts = () => {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);

    // 필터 상태
    const [search, setSearch] = useState('');
    const [categoryFilter, setCategoryFilter] = useState('');
    const [blindedFilter, setBlindedFilter] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const categories = ['자유게시판', '유머게시판', '축구게시판', '농구게시판', '예측게시판'];

    useEffect(() => {
        fetchPosts();
    }, [search, categoryFilter, blindedFilter, page]);

    const fetchPosts = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: '20',
                ...(search && { search }),
                ...(categoryFilter && { category: categoryFilter }),
                ...(blindedFilter && { isBlinded: blindedFilter })
            });

            const response = await fetch(`http://localhost:8080/api/admin-page/posts?${params}`, {
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                setPosts(data.content);
                setTotalPages(data.totalPages);
            }
        } catch (error) {
            console.error('게시글 목록 로딩 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleBlindPost = async (postId, isCurrentlyBlinded) => {
        if (isCurrentlyBlinded) {
            // 블라인드 해제
            if (!confirm('블라인드를 해제하시겠습니까?')) return;

            try {
                const response = await fetch(`http://localhost:8080/api/admin-page/posts/${postId}/unblind`, {
                    method: 'PUT',
                    credentials: 'include'
                });

                if (response.ok) {
                    alert('블라인드가 해제되었습니다.');
                    fetchPosts();
                }
            } catch (error) {
                console.error('블라인드 해제 실패:', error);
            }
        } else {
            // 블라인드 처리
            const reason = prompt('블라인드 사유를 입력하세요:');
            if (!reason) return;

            try {
                const response = await fetch(`http://localhost:8080/api/admin-page/posts/${postId}/blind`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify({ reason })
                });

                if (response.ok) {
                    alert('게시글이 블라인드 처리되었습니다.');
                    fetchPosts();
                }
            } catch (error) {
                console.error('블라인드 처리 실패:', error);
            }
        }
    };

    const handleDeletePost = async (postId) => {
        if (!confirm('정말로 이 게시글을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/admin-page/posts/${postId}`, {
                method: 'DELETE',
                credentials: 'include'
            });

            if (response.ok) {
                alert('게시글이 삭제되었습니다.');
                fetchPosts();
            }
        } catch (error) {
            console.error('게시글 삭제 실패:', error);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* 헤더 */}
                <div className="mb-6">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">게시글 관리</h1>
                    <p className="text-gray-600">전체 게시글을 조회하고 관리합니다</p>
                </div>

                {/* 통계 카드 */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
                    <div className="bg-white rounded-lg shadow p-6">
                        <div className="flex items-center gap-4">
                            <div className="bg-blue-500 text-white p-3 rounded-lg">
                                <Eye className="w-6 h-6" />
                            </div>
                            <div>
                                <div className="text-sm text-gray-600">전체 게시글</div>
                                <div className="text-2xl font-bold text-gray-900">
                                    {posts.filter(p => !p.isBlinded).length}
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="bg-white rounded-lg shadow p-6">
                        <div className="flex items-center gap-4">
                            <div className="bg-red-500 text-white p-3 rounded-lg">
                                <EyeOff className="w-6 h-6" />
                            </div>
                            <div>
                                <div className="text-sm text-gray-600">블라인드</div>
                                <div className="text-2xl font-bold text-gray-900">
                                    {posts.filter(p => p.isBlinded).length}
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="bg-white rounded-lg shadow p-6">
                        <div className="flex items-center gap-4">
                            <div className="bg-yellow-500 text-white p-3 rounded-lg">
                                <Filter className="w-6 h-6" />
                            </div>
                            <div>
                                <div className="text-sm text-gray-600">신고된 게시글</div>
                                <div className="text-2xl font-bold text-gray-900">
                                    {posts.filter(p => p.reportCount > 0).length}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* 필터 및 검색 */}
                <div className="bg-white rounded-lg shadow p-6 mb-6">
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        {/* 검색 */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                <Search className="w-4 h-4 inline mr-1" />
                                검색
                            </label>
                            <input
                                type="text"
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                placeholder="제목, 내용 검색"
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                        </div>

                        {/* 카테고리 필터 */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                <Filter className="w-4 h-4 inline mr-1" />
                                카테고리
                            </label>
                            <select
                                value={categoryFilter}
                                onChange={(e) => setCategoryFilter(e.target.value)}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            >
                                <option value="">전체</option>
                                {categories.map(cat => (
                                    <option key={cat} value={cat}>{cat}</option>
                                ))}
                            </select>
                        </div>

                        {/* 블라인드 필터 */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">상태</label>
                            <select
                                value={blindedFilter}
                                onChange={(e) => setBlindedFilter(e.target.value)}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            >
                                <option value="">전체</option>
                                <option value="false">일반</option>
                                <option value="true">블라인드</option>
                            </select>
                        </div>
                    </div>
                </div>

                {/* 게시글 목록 */}
                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    게시글
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    작성자
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    통계
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    신고
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    상태
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    작성일
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    관리
                                </th>
                            </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                            {loading ? (
                                <tr>
                                    <td colSpan="7" className="px-6 py-4 text-center text-gray-500">
                                        로딩 중...
                                    </td>
                                </tr>
                            ) : posts.length === 0 ? (
                                <tr>
                                    <td colSpan="7" className="px-6 py-4 text-center text-gray-500">
                                        게시글이 없습니다
                                    </td>
                                </tr>
                            ) : (
                                posts.map((post) => (
                                    <tr
                                        key={post.postId}
                                        className={`hover:bg-gray-50 ${post.isBlinded ? 'bg-red-50' : ''}`}
                                    >
                                        <td className="px-6 py-4">
                                            <div>
                                                <div className="flex items-center gap-2 mb-1">
                            <span className="px-2 py-1 text-xs font-semibold text-blue-800 bg-blue-100 rounded">
                              {post.categoryName}
                            </span>
                                                    {post.isNotice && (
                                                        <span className="px-2 py-1 text-xs font-semibold text-red-600 bg-red-100 rounded">
                                공지
                              </span>
                                                    )}
                                                </div>
                                                <div className="text-sm font-medium text-gray-900 max-w-md truncate">
                                                    {post.title}
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm font-medium text-gray-900">
                                                {post.authorNickname}
                                            </div>
                                            <div className="text-sm text-gray-500">
                                                @{post.authorUsername}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm text-gray-900">
                                                조회 {post.viewCount} / 추천 {post.likeCount}
                                            </div>
                                            <div className="text-sm text-gray-500">
                                                댓글 {post.commentCount}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            {post.reportCount > 0 ? (
                                                <span className="px-2 py-1 text-xs font-semibold text-red-800 bg-red-100 rounded">
                            {post.reportCount}건
                          </span>
                                            ) : (
                                                <span className="text-sm text-gray-500">-</span>
                                            )}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            {post.isBlinded ? (
                                                <span className="px-2 py-1 text-xs font-semibold text-red-800 bg-red-100 rounded">
                            블라인드
                          </span>
                                            ) : (
                                                <span className="px-2 py-1 text-xs font-semibold text-green-800 bg-green-100 rounded">
                            일반
                          </span>
                                            )}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {new Date(post.createdAt).toLocaleDateString('ko-KR')}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="flex gap-2">
                                                <button
                                                    onClick={() => handleBlindPost(post.postId, post.isBlinded)}
                                                    className={`p-1 rounded ${
                                                        post.isBlinded
                                                            ? 'text-green-600 hover:bg-green-50'
                                                            : 'text-orange-600 hover:bg-orange-50'
                                                    }`}
                                                    title={post.isBlinded ? '블라인드 해제' : '블라인드'}
                                                >
                                                    {post.isBlinded ? <Eye className="w-5 h-5" /> : <EyeOff className="w-5 h-5" />}
                                                </button>
                                                <button
                                                    onClick={() => handleDeletePost(post.postId)}
                                                    className="p-1 text-red-600 hover:bg-red-50 rounded"
                                                    title="삭제"
                                                >
                                                    <Trash2 className="w-5 h-5" />
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
        </div>
    );
};

export default AdminPosts;