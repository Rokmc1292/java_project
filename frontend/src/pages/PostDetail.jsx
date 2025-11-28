import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    getPost,
    likePost,
    dislikePost,
    scrapPost,
    createComment,
    deleteComment,
    likeComment,
    reportPost
} from '../api/community';
import { getUserData, isLoggedIn, apiGet } from '../api/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function PostDetail() {
    const { postId } = useParams();
    const navigate = useNavigate();

    const [post, setPost] = useState(null);
    const [comments, setComments] = useState([]);
    const [pageLoading, setPageLoading] = useState(false);
    const [commentLoading, setCommentLoading] = useState(false);
    const [newComment, setNewComment] = useState('');
    const [replyTo, setReplyTo] = useState(null);
    const [showReportModal, setShowReportModal] = useState(false);
    const [reportReason, setReportReason] = useState('');

    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalComments, setTotalComments] = useState(0);
    const commentSectionRef = useRef(null);

    const currentUser = getUserData();

    const [showCommentReportModal, setShowCommentReportModal] = useState(false);

    // 티어별 그라데이션 색상
    const getTierGradient = (tier) => {
        const gradients = {
            BRONZE: 'from-amber-700 to-amber-900',
            SILVER: 'from-gray-400 to-gray-600',
            GOLD: 'from-yellow-400 to-orange-500',
            PLATINUM: 'from-cyan-400 to-blue-500',
            DIAMOND: 'from-blue-300 to-blue-500'
        };
        return gradients[tier] || 'from-gray-500 to-gray-700';
    };
    const [reportingCommentId, setReportingCommentId] = useState(null);
    const [commentReportReason, setCommentReportReason] = useState('');

    /**
     * 게시글 조회
     */
    const fetchPost = async () => {
        try {
            const postData = await getPost(postId);
            setPost(postData);
        } catch (error) {
            console.error('게시글 조회 오류:', error);
            alert('게시글을 불러오는데 실패했습니다.');
            navigate('/community');
        }
    };

    /**
     * 댓글 조회 (페이지네이션)
     */
    const [bestComments, setBestComments] = useState([]);

    const fetchComments = async (page = 0) => {
        try {
            const data = await apiGet(`/api/community/posts/${postId}/comments?page=${page}&size=30`);

            setComments(data.comments);
            setBestComments(data.bestComments);
            setCurrentPage(data.currentPage);
            setTotalPages(data.totalPages);
            setTotalComments(data.totalComments);

            if (commentSectionRef.current && page > 0) {
                commentSectionRef.current.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        } catch (error) {
            console.error('댓글 조회 오류:', error);
        }
    };

    /**
     * 게시글 및 댓글 조회
     */
    const fetchPostAndComments = async () => {
        setPageLoading(true);
        try {
            await fetchPost();
            await fetchComments(currentPage);
        } finally {
            setPageLoading(false);
        }
    };

    useEffect(() => {
        fetchPostAndComments();
    }, [postId]);

    /**
     * 페이지 변경
     */
    const handlePageChange = (newPage) => {
        if (newPage < 0 || newPage >= totalPages) {
            alert('해당 페이지가 존재하지 않습니다.');
            return;
        }
        setCurrentPage(newPage);
        fetchComments(newPage);
    };

    /**
     * 게시글 추천
     */
    const handleLike = async () => {
        if (!isLoggedIn()) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }

        try {
            await likePost(postId);
            alert('추천했습니다.');
            await fetchPost();
        } catch (error) {
            alert(error.message || '추천에 실패했습니다.');
        }
    };

    /**
     * 게시글 비추천
     */
    const handleDislike = async () => {
        if (!isLoggedIn()) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }

        try {
            await dislikePost(postId);
            alert('비추천했습니다.');
            await fetchPost();
        } catch (error) {
            alert(error.message || '비추천에 실패했습니다.');
        }
    };

    /**
     * 스크랩
     */
    const handleScrap = async () => {
        if (!isLoggedIn()) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }

        try {
            await scrapPost(postId);
            alert('스크랩했습니다.');
        } catch (error) {
            alert(error.message || '스크랩에 실패했습니다.');
        }
    };

    /**
     * 댓글 작성
     */
    const handleCreateComment = async () => {
        if (!isLoggedIn()) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }

        if (!newComment.trim()) {
            alert('댓글 내용을 입력해주세요.');
            return;
        }

        if (commentLoading) {
            return;
        }

        setCommentLoading(true);

        try {
            await createComment(postId, newComment, replyTo);
            alert(replyTo ? '답글이 작성되었습니다.' : '댓글이 작성되었습니다.');
            setNewComment('');
            setReplyTo(null);

            await fetchPost();
            await fetchComments(currentPage);
        } catch (error) {
            console.error('댓글 작성 오류:', error);

            if (error.response && error.response.data) {
                const errors = error.response.data;

                if (typeof errors === 'object' && !errors.message) {
                    const errorMessages = Object.values(errors).join('\n');
                    alert(errorMessages);
                } else {
                    alert(errors.message || '댓글 작성에 실패했습니다.');
                }
            } else {
                alert(error.message || '댓글 작성에 실패했습니다.');
            }
        } finally {
            setCommentLoading(false);
        }
    };

    /**
     * 댓글 삭제
     */
    const handleDeleteComment = async (commentId) => {
        if (!window.confirm('댓글을 삭제하시겠습니까?')) {
            return;
        }

        try {
            await deleteComment(commentId);
            alert('댓글이 삭제되었습니다.');
            await fetchPost();
            await fetchComments(currentPage);
        } catch (error) {
            alert(error.message || '댓글 삭제에 실패했습니다.');
        }
    };

    /**
     * 댓글 추천
     */
    const handleLikeComment = async (commentId) => {
        if (!isLoggedIn()) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }

        try {
            await likeComment(commentId);
            alert('추천했습니다.');
            await fetchComments(currentPage);
        } catch (error) {
            alert(error.message || '추천에 실패했습니다.');
        }
    };

    /**
     * 댓글 비추천
     */
    const handleDislikeComment = async (commentId) => {
        try {
            const response = await fetch(`${API_BASE_URL}/api/community/comments/${commentId}/dislike`, {
                method: 'POST',
                credentials: 'include'
            });

            if (response.ok) {
                alert('비추천했습니다.');
                fetchComments(currentPage);
            } else {
                const error = await response.json();
                alert(error.message || '비추천에 실패했습니다.');
            }
        } catch (error) {
            console.error('댓글 비추천 오류:', error);
            alert('비추천 처리 중 오류가 발생했습니다.');
        }
    };

    /**
     * 게시글 신고
     */
    const handleReportPost = async () => {
        if (!reportReason.trim()) {
            alert('신고 사유를 입력해주세요.');
            return;
        }

        try {
            await reportPost(postId, '기타', reportReason);
            alert('신고가 접수되었습니다.');
            setShowReportModal(false);
            setReportReason('');
        } catch (error) {
            alert(error.message || '신고에 실패했습니다.');
        }
    };

    /**
     * 댓글 신고
     */
    const handleReportComment = async () => {
        if (!commentReportReason.trim()) {
            alert('신고 사유를 입력해주세요.');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/community/comments/${reportingCommentId}/report`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    reason: '기타',
                    description: commentReportReason
                })
            });

            if (response.ok) {
                alert('댓글 신고가 접수되었습니다.');
                setShowCommentReportModal(false);
                setCommentReportReason('');
                setReportingCommentId(null);
            } else {
                const error = await response.json();
                alert(error.message || '신고 처리에 실패했습니다.');
            }
        } catch (error) {
            console.error('댓글 신고 오류:', error);
            alert('신고 처리 중 오류가 발생했습니다.');
        }
    };

    /**
     * 댓글 렌더링 (대댓글 포함)
     */
    const renderComment = (comment, isReply = false) => {
        const isMyComment = currentUser && currentUser.username === comment.username;

        return (
            <div
                key={comment.commentId}
                className={`p-4 mb-3 rounded-lg ${
                    isReply
                        ? 'ml-10 bg-gray-700/30'
                        : 'bg-gray-700/50'
                } border border-gray-600/50`}
            >
                <div className="flex justify-between items-center mb-3">
                    <div className="flex items-center space-x-2">
                        {comment.isBest && (
                            <span className="bg-blue-500 text-white px-2 py-1 rounded-md text-xs font-bold">
                                ★ BEST
                            </span>
                        )}
                        <span className="font-bold text-white">{comment.nickname}</span>
                        {comment.userTier && (
                            <span className={`px-3 py-1 bg-gradient-to-r ${getTierGradient(comment.userTier)} text-white text-xs font-bold rounded shadow-lg`}>
                                {comment.userTier}
                            </span>
                        )}
                        <span className="text-xs text-gray-400">
                            {new Date(comment.createdAt).toLocaleString('ko-KR')}
                        </span>
                    </div>

                    {isMyComment && !comment.isDeleted && (
                        <button
                            onClick={() => handleDeleteComment(comment.commentId)}
                            className="px-3 py-1 bg-red-500 hover:bg-red-600 text-white rounded-md text-xs font-semibold transition"
                        >
                            삭제
                        </button>
                    )}
                </div>

                <div className="mb-3 whitespace-pre-wrap text-white">
                    {comment.isDeleted ? (
                        <span className="text-gray-500 italic">삭제된 댓글입니다.</span>
                    ) : (
                        comment.content
                    )}
                </div>

                {!comment.isDeleted && (
                    <div className="flex gap-2 text-sm">
                        <button
                            onClick={() => handleLikeComment(comment.commentId)}
                            className="px-3 py-1 bg-gray-600 hover:bg-gray-500 rounded-md transition"
                        >
                            👍 {comment.likeCount}
                        </button>

                        <button
                            onClick={() => handleDislikeComment(comment.commentId)}
                            className="px-3 py-1 bg-gray-600 hover:bg-gray-500 rounded-md transition"
                        >
                            👎 {comment.dislikeCount}
                        </button>

                        {!isReply && (
                            <button
                                onClick={() => setReplyTo(comment.commentId)}
                                className="px-3 py-1 bg-gray-600 hover:bg-gray-500 rounded-md transition"
                            >
                                💬 답글
                            </button>
                        )}

                        {!isMyComment && (
                            <button
                                onClick={() => {
                                    setReportingCommentId(comment.commentId);
                                    setShowCommentReportModal(true);
                                }}
                                className="px-3 py-1 bg-red-500 hover:bg-red-600 text-white rounded-md transition"
                            >
                                🚨 신고
                            </button>
                        )}
                    </div>
                )}

                {comment.replies && comment.replies.length > 0 && (
                    <div className="mt-4">
                        {comment.replies.map(reply => renderComment(reply, true))}
                    </div>
                )}
            </div>
        );
    };

    if (pageLoading || !post) {
        return (
            <div className="bg-gray-900 min-h-screen flex items-center justify-center">
                <div className="text-center">
                    <div className="inline-block w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
                    <p className="mt-4 text-white text-lg">로딩 중...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="bg-gray-900 min-h-screen text-white">
            <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-8 max-w-4xl">

                <button
                    onClick={() => navigate('/board')}
                    className="px-6 py-3 bg-gray-800/80 backdrop-blur-sm hover:bg-gray-700 rounded-lg font-semibold transition mb-6"
                >
                    ← 목록으로
                </button>

                {/* 게시글 본문 */}
                <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg shadow-xl p-8 mb-6">
                    <div className="mb-6 pb-6 border-b-2 border-gray-700">
                        <div className="flex items-center gap-2 mb-3">
                            {post.isNotice && (
                                <span className="bg-red-500 text-white px-3 py-1 rounded-md text-xs font-bold">
                                    공지
                                </span>
                            )}
                            {post.isPopular && (
                                <span className="bg-blue-500 text-white px-3 py-1 rounded-md text-xs font-bold">
                                    인기
                                </span>
                            )}
                        </div>
                        <h1 className="text-3xl font-bold">{post.title}</h1>
                    </div>

                    <div className="flex justify-between items-center mb-6">
                        <div className="text-sm text-gray-400 space-x-4">
                            <span className="font-bold text-white">{post.nickname}</span>
                            <span>{new Date(post.createdAt).toLocaleString('ko-KR')}</span>
                            <span>|</span>
                            <span>조회 {post.viewCount}</span>
                        </div>

                        {currentUser && currentUser.username !== post.username && (
                            <button
                                onClick={() => setShowReportModal(true)}
                                className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-md text-xs font-semibold transition"
                            >
                                🚨 신고
                            </button>
                        )}
                    </div>

                    <div className="text-base leading-relaxed mb-8 whitespace-pre-wrap">
                        {post.content}
                    </div>

                    <div className="flex justify-center gap-4 pt-6 border-t border-gray-700">
                        <button
                            onClick={handleLike}
                            className="px-8 py-3 bg-blue-500 hover:bg-blue-600 text-white rounded-lg font-bold transition shadow-lg"
                        >
                            👍 추천 {post.likeCount}
                        </button>

                        <button
                            onClick={handleDislike}
                            className="px-8 py-3 bg-gray-600 hover:bg-gray-500 text-white rounded-lg font-bold transition shadow-lg"
                        >
                            👎 비추천 {post.dislikeCount}
                        </button>

                        <button
                            onClick={handleScrap}
                            className="px-8 py-3 bg-green-500 hover:bg-green-600 text-white rounded-lg font-bold transition shadow-lg"
                        >
                            ⭐ 스크랩
                        </button>
                    </div>
                </div>

                {/* 댓글 섹션 */}
                <div
                    ref={commentSectionRef}
                    className="bg-gray-800/80 backdrop-blur-sm rounded-lg shadow-xl p-8"
                >
                    <h2 className="text-2xl font-bold mb-6">
                        💬 댓글 {totalComments}
                    </h2>

                    {isLoggedIn() && (
                        <div className="mb-8">
                            {replyTo && (
                                <div className="flex justify-between items-center p-3 bg-gray-700/50 rounded-lg mb-3">
                                    <span className="text-sm">답글 작성 중...</span>
                                    <button
                                        onClick={() => setReplyTo(null)}
                                        className="px-3 py-1 bg-gray-600 hover:bg-gray-500 rounded-md text-xs transition"
                                    >
                                        취소
                                    </button>
                                </div>
                            )}

                            <textarea
                                value={newComment}
                                onChange={(e) => setNewComment(e.target.value)}
                                placeholder={replyTo ? "답글을 입력하세요..." : "댓글을 입력하세요..."}
                                className="w-full p-4 bg-gray-700/50 border border-gray-600 rounded-lg text-white placeholder-gray-400 resize-vertical min-h-[100px] focus:outline-none focus:border-blue-500"
                            />

                            <button
                                onClick={handleCreateComment}
                                disabled={commentLoading || !newComment.trim()}
                                className={`mt-3 px-6 py-3 rounded-lg font-bold transition ${
                                    commentLoading || !newComment.trim()
                                        ? 'bg-gray-600 cursor-not-allowed'
                                        : 'bg-blue-500 hover:bg-blue-600'
                                } text-white`}
                            >
                                {commentLoading ? '작성 중...' : (replyTo ? '답글 작성' : '댓글 작성')}
                            </button>
                        </div>
                    )}

                    {/* 댓글 목록 */}
                    <div>
                        {comments.length === 0 ? (
                            <div className="text-center py-12 text-gray-400">
                                첫 댓글을 작성해보세요!
                            </div>
                        ) : (
                            <>
                                {/* 베스트 댓글 섹션 */}
                                {bestComments && bestComments.length > 0 && (
                                    <div className="mb-8">
                                        <h3 className="text-lg font-bold mb-4 text-blue-400">
                                            ★ 베스트 댓글
                                        </h3>
                                        <div className="border-t-2 border-blue-500 pt-4">
                                            {bestComments.map((comment, index) => (
                                                <div
                                                    key={`best-${comment.commentId}-${index}`}
                                                    className="p-4 mb-3 bg-blue-900/20 border border-blue-500/50 rounded-lg"
                                                >
                                                    <div className="flex justify-between items-center mb-3">
                                                        <div className="flex items-center space-x-2">
                                                            <span className="bg-blue-500 text-white px-2 py-1 rounded-md text-xs font-bold">
                                                                ★ BEST
                                                            </span>
                                                            <span className="font-bold text-white">{comment.nickname}</span>
                                                            {comment.userTier && (
                                                                <span className={`px-3 py-1 bg-gradient-to-r ${getTierGradient(comment.userTier)} text-white text-xs font-bold rounded shadow-lg`}>
                                                                    {comment.userTier}
                                                                </span>
                                                            )}
                                                            <span className="text-xs text-gray-400">
                                                                {new Date(comment.createdAt).toLocaleString('ko-KR')}
                                                            </span>
                                                        </div>
                                                    </div>
                                                    <div className="mb-3 whitespace-pre-wrap text-white">
                                                        {comment.content}
                                                    </div>
                                                    <div className="flex gap-2 text-sm">
                                                        <button
                                                            onClick={() => handleLikeComment(comment.commentId)}
                                                            className="px-3 py-1 bg-gray-600 hover:bg-gray-500 rounded-md transition"
                                                        >
                                                            👍 {comment.likeCount}
                                                        </button>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}

                                {/* 일반 댓글 섹션 */}
                                <div>
                                    <h3 className="text-lg font-bold mb-4">
                                        💬 댓글 {totalComments}
                                    </h3>
                                    {comments.map(comment => renderComment(comment))}
                                </div>
                            </>
                        )}
                    </div>

                    {/* 댓글 페이지네이션 */}
                    {totalComments > 30 && (
                        <div className="flex justify-center items-center gap-2 mt-8 pt-6 border-t border-gray-700">
                            <button
                                onClick={() => handlePageChange(currentPage - 10)}
                                disabled={currentPage < 10}
                                className={`px-4 py-2 bg-gray-700 rounded-lg transition ${
                                    currentPage < 10
                                        ? 'text-gray-500 cursor-not-allowed'
                                        : 'hover:bg-gray-600 text-white'
                                }`}
                            >
                                &lt;&lt;
                            </button>

                            <button
                                onClick={() => handlePageChange(currentPage - 1)}
                                disabled={currentPage === 0}
                                className={`px-4 py-2 bg-gray-700 rounded-lg transition min-w-[60px] ${
                                    currentPage === 0
                                        ? 'text-gray-500 cursor-not-allowed'
                                        : 'hover:bg-gray-600 text-white'
                                }`}
                            >
                                Prev
                            </button>

                            <div className="flex gap-2">
                                {(() => {
                                    const startPage = Math.floor(currentPage / 10) * 10;
                                    const endPage = Math.min(startPage + 10, totalPages);
                                    const pages = [];

                                    for (let i = startPage; i < endPage; i++) {
                                        pages.push(
                                            <button
                                                key={i}
                                                onClick={() => handlePageChange(i)}
                                                className={`min-w-[40px] px-3 py-2 rounded-lg font-semibold transition ${
                                                    currentPage === i
                                                        ? 'bg-blue-500 text-white'
                                                        : 'bg-gray-700 hover:bg-gray-600 text-white'
                                                }`}
                                            >
                                                {i + 1}
                                            </button>
                                        );
                                    }

                                    return pages;
                                })()}
                            </div>

                            <button
                                onClick={() => handlePageChange(currentPage + 1)}
                                disabled={currentPage >= totalPages - 1}
                                className={`px-4 py-2 bg-gray-700 rounded-lg transition min-w-[60px] ${
                                    currentPage >= totalPages - 1
                                        ? 'text-gray-500 cursor-not-allowed'
                                        : 'hover:bg-gray-600 text-white'
                                }`}
                            >
                                Next
                            </button>

                            <button
                                onClick={() => handlePageChange(currentPage + 10)}
                                disabled={currentPage >= totalPages - 10}
                                className={`px-4 py-2 bg-gray-700 rounded-lg transition ${
                                    currentPage >= totalPages - 10
                                        ? 'text-gray-500 cursor-not-allowed'
                                        : 'hover:bg-gray-600 text-white'
                                }`}
                            >
                                &gt;&gt;
                            </button>
                        </div>
                    )}
                </div>

                {/* 게시글 신고 모달 */}
                {showReportModal && (
                    <div className="fixed inset-0 bg-black/50 flex justify-center items-center z-50">
                        <div className="bg-gray-800 rounded-lg p-8 w-[90%] max-w-md">
                            <h2 className="text-2xl font-bold mb-6">🚨 게시글 신고</h2>

                            <textarea
                                placeholder="신고 사유를 입력해주세요"
                                value={reportReason}
                                onChange={(e) => setReportReason(e.target.value)}
                                className="w-full p-4 bg-gray-700/50 border border-gray-600 rounded-lg text-white placeholder-gray-400 resize-vertical min-h-[150px] focus:outline-none focus:border-blue-500 mb-4"
                            />

                            <div className="flex gap-3 justify-end">
                                <button
                                    onClick={() => setShowReportModal(false)}
                                    className="px-6 py-3 bg-gray-600 hover:bg-gray-500 rounded-lg font-semibold transition"
                                >
                                    취소
                                </button>
                                <button
                                    onClick={handleReportPost}
                                    className="px-6 py-3 bg-red-500 hover:bg-red-600 text-white rounded-lg font-semibold transition"
                                >
                                    신고하기
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                {/* 댓글 신고 모달 */}
                {showCommentReportModal && (
                    <div className="fixed inset-0 bg-black/50 flex justify-center items-center z-50">
                        <div className="bg-gray-800 rounded-lg p-8 w-[90%] max-w-md">
                            <h2 className="text-2xl font-bold mb-6">🚨 댓글 신고</h2>

                            <textarea
                                placeholder="신고 사유를 입력해주세요"
                                value={commentReportReason}
                                onChange={(e) => setCommentReportReason(e.target.value)}
                                className="w-full p-4 bg-gray-700/50 border border-gray-600 rounded-lg text-white placeholder-gray-400 resize-vertical min-h-[150px] focus:outline-none focus:border-blue-500 mb-4"
                            />

                            <div className="flex gap-3 justify-end">
                                <button
                                    onClick={() => {
                                        setShowCommentReportModal(false);
                                        setCommentReportReason('');
                                        setReportingCommentId(null);
                                    }}
                                    className="px-6 py-3 bg-gray-600 hover:bg-gray-500 rounded-lg font-semibold transition"
                                >
                                    취소
                                </button>
                                <button
                                    onClick={handleReportComment}
                                    className="px-6 py-3 bg-red-500 hover:bg-red-600 text-white rounded-lg font-semibold transition"
                                >
                                    신고하기
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default PostDetail;
