/**
 * 게시글 관리 페이지
 * 파일 위치: frontend/src/pages/admin/PostManagement.jsx
 */

import { useState, useEffect } from 'react';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export function PostManagement() {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        loadPosts();
    }, []);

    const loadPosts = async () => {
        setLoading(true);
        try {
            const response = await fetch(`${API_BASE_URL}/api/admin/posts`, {
                credentials: 'include'
            });
            if (response.ok) {
                const data = await response.json();
                setPosts(data.content);
            }
        } catch (error) {
            console.error('게시글 로드 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleDeletePost = async (postId) => {
        if (!window.confirm('게시글을 삭제하시겠습니까?')) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/admin/posts/${postId}`, {
                method: 'DELETE',
                credentials: 'include'
            });

            if (response.ok) {
                alert('게시글이 삭제되었습니다.');
                loadPosts();
            }
        } catch (error) {
            console.error('게시글 삭제 실패:', error);
        }
    };

    return (
        <div className="post-management">
            <div className="page-header">
                <h2>📝 게시글 관리</h2>
            </div>

            {loading ? (
                <div className="loading">로딩 중...</div>
            ) : posts.length === 0 ? (
                <div className="no-data">게시글이 없습니다.</div>
            ) : (
                <div className="post-table">
                    <table>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>카테고리</th>
                            <th>제목</th>
                            <th>작성자</th>
                            <th>조회수</th>
                            <th>추천</th>
                            <th>작성일</th>
                            <th>관리</th>
                        </tr>
                        </thead>
                        <tbody>
                        {posts.map(post => (
                            <tr key={post.postId}>
                                <td>{post.postId}</td>
                                <td>{post.categoryName}</td>
                                <td>{post.title}</td>
                                <td>{post.nickname}</td>
                                <td>{post.viewCount}</td>
                                <td>{post.likeCount}</td>
                                <td>{new Date(post.createdAt).toLocaleDateString()}</td>
                                <td className="action-cell">
                                    <button
                                        className="btn-sm btn-danger"
                                        onClick={() => handleDeletePost(post.postId)}
                                    >
                                        삭제
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export default PostManagement;