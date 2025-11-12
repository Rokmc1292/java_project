import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
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
import { getUserData, isLoggedIn } from '../api/api';

function PostDetail() {
    const { postId } = useParams();
    const navigate = useNavigate();

    const [post, setPost] = useState(null);
    const [comments, setComments] = useState([]);
    const [pageLoading, setPageLoading] = useState(false);  // ⭐ 페이지 전체 로딩
    const [commentLoading, setCommentLoading] = useState(false);  // ⭐ 댓글 작성 로딩
    const [newComment, setNewComment] = useState('');
    const [replyTo, setReplyTo] = useState(null);
    const [showReportModal, setShowReportModal] = useState(false);
    const [reportReason, setReportReason] = useState('');

    // ⭐ 댓글 페이지네이션 상태 추가
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalComments, setTotalComments] = useState(0);
    const commentSectionRef = useRef(null);  // 댓글 섹션 참조

    const currentUser = getUserData();

    const [showCommentReportModal, setShowCommentReportModal] = useState(false);
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
            const response = await fetch(`http://localhost:8080/api/community/posts/${postId}/comments?page=${page}&size=30`);
            const data = await response.json();

            setComments(data.comments);
            setBestComments(data.bestComments);  // ⭐ 베스트 댓글 설정
            setCurrentPage(data.currentPage);
            setTotalPages(data.totalPages);
            setTotalComments(data.totalComments);

            // ⭐ 댓글 섹션으로 스크롤
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
        setPageLoading(true);  // ⭐ 페이지 로딩만
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

        // ⭐ 댓글 작성 중 상태 추가
        if (commentLoading) {
            return;
        }

        setCommentLoading(true);  // ⭐ 로딩 시작

        try {
            await createComment(postId, newComment, replyTo);
            alert(replyTo ? '답글이 작성되었습니다.' : '댓글이 작성되었습니다.');  // ⭐ alert 유지
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
                setCommentLoading(false);  // ⭐ 로딩 종료
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
            const response = await fetch(`http://localhost:8080/api/community/comments/${commentId}/dislike`, {
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
            const response = await fetch(`http://localhost:8080/api/community/comments/${reportingCommentId}/report`, {
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
                style={{
                    padding: '15px',
                    marginBottom: '10px',
                    marginLeft: isReply ? '40px' : '0',
                    backgroundColor: isReply ? '#f9f9f9' : 'white',
                    border: '1px solid #e0e0e0',
                    borderRadius: '5px'
                }}
            >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                    <div>
                        {comment.isBest && (
                            <span style={{
                                backgroundColor: '#4da6ff',
                                color: 'white',
                                padding: '2px 8px',
                                borderRadius: '3px',
                                fontSize: '11px',
                                fontWeight: 'bold',
                                marginRight: '8px'
                            }}>
                ★ BEST
              </span>
                        )}
                        <span style={{ fontWeight: 'bold', marginRight: '10px' }}>
              {comment.nickname}
            </span>
                        <span style={{ fontSize: '12px', color: '#888' }}>
              {new Date(comment.createdAt).toLocaleString('ko-KR')}
            </span>
                    </div>

                    {isMyComment && !comment.isDeleted && (
                        <button
                            onClick={() => handleDeleteComment(comment.commentId)}
                            style={{
                                padding: '5px 10px',
                                backgroundColor: '#ff4444',
                                color: 'white',
                                border: 'none',
                                borderRadius: '3px',
                                cursor: 'pointer',
                                fontSize: '12px'
                            }}
                        >
                            삭제
                        </button>
                    )}
                </div>

                <div style={{ marginBottom: '10px', whiteSpace: 'pre-wrap' }}>
                    {comment.content}
                </div>

                {!comment.isDeleted && (
                    <div style={{ display: 'flex', gap: '10px', fontSize: '14px' }}>
                        <button
                            onClick={() => handleLikeComment(comment.commentId)}
                            style={{
                                padding: '5px 10px',
                                backgroundColor: '#f0f0f0',
                                border: 'none',
                                borderRadius: '3px',
                                cursor: 'pointer'
                            }}
                        >
                            👍 {comment.likeCount}
                        </button>

                        <button
                            onClick={() => handleDislikeComment(comment.commentId)}
                            style={{
                                padding: '5px 10px',
                                backgroundColor: '#f0f0f0',
                                border: 'none',
                                borderRadius: '3px',
                                cursor: 'pointer'
                            }}
                        >
                            👎 {comment.dislikeCount}
                        </button>

                        {!isReply && (
                            <button
                                onClick={() => setReplyTo(comment.commentId)}
                                style={{
                                    padding: '5px 10px',
                                    backgroundColor: '#f0f0f0',
                                    border: 'none',
                                    borderRadius: '3px',
                                    cursor: 'pointer'
                                }}
                            >
                                💬 답글
                            </button>
                        )}
                        {/* 신고 버튼 추가 - 본인 댓글이 아닐 때만 */}
                        {!isMyComment && (
                            <button
                                onClick={() => {
                                    setReportingCommentId(comment.commentId);
                                    setShowCommentReportModal(true);
                                }}
                                style={{
                                    padding: '5px 10px',
                                    backgroundColor: '#ff4444',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '3px',
                                    cursor: 'pointer'
                                }}
                            >
                                🚨 신고
                            </button>
                        )}
                    </div>
                )}

                {comment.replies && comment.replies.length > 0 && (
                    <div style={{ marginTop: '15px' }}>
                        {comment.replies.map(reply => renderComment(reply, true))}
                    </div>
                )}
            </div>
        );
    };

    if (pageLoading || !post) {
        return (
            <div>
                <Navbar />
                <div style={{ textAlign: 'center', padding: '100px', color: '#888' }}>
                    로딩 중...
                </div>
            </div>
        );
    }

    return (
        <div>
            <Navbar />
            <div style={{ maxWidth: '900px', margin: '0 auto', padding: '20px' }}>

                <button
                    onClick={() => navigate('/community')}
                    style={{
                        padding: '8px 16px',
                        backgroundColor: '#f0f0f0',
                        border: 'none',
                        borderRadius: '5px',
                        cursor: 'pointer',
                        marginBottom: '20px'
                    }}
                >
                    ← 목록으로
                </button>

                {/* 게시글 본문 */}
                <div style={{
                    backgroundColor: 'white',
                    border: '1px solid #e0e0e0',
                    borderRadius: '10px',
                    padding: '30px',
                    marginBottom: '20px'
                }}>
                    <div style={{ marginBottom: '20px', borderBottom: '2px solid #e0e0e0', paddingBottom: '20px' }}>
                        {post.isNotice && (
                            <span style={{
                                backgroundColor: '#ff4444',
                                color: 'white',
                                padding: '3px 10px',
                                borderRadius: '3px',
                                fontSize: '12px',
                                marginRight: '10px'
                            }}>
                공지
              </span>
                        )}
                        {post.isPopular && (
                            <span style={{
                                backgroundColor: '#646cff',
                                color: 'white',
                                padding: '3px 10px',
                                borderRadius: '3px',
                                fontSize: '12px',
                                marginRight: '10px'
                            }}>
                인기
              </span>
                        )}
                        <h1 style={{ fontSize: '28px', fontWeight: 'bold', display: 'inline' }}>
                            {post.title}
                        </h1>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                        <div style={{ fontSize: '14px', color: '#666' }}>
                            <span style={{ fontWeight: 'bold', marginRight: '15px' }}>{post.nickname}</span>
                            <span>{new Date(post.createdAt).toLocaleString('ko-KR')}</span>
                            <span style={{ margin: '0 10px' }}>|</span>
                            <span>조회 {post.viewCount}</span>
                        </div>

                        <button
                            onClick={() => setShowReportModal(true)}
                            style={{
                                padding: '5px 10px',
                                backgroundColor: '#ff4444',
                                color: 'white',
                                border: 'none',
                                borderRadius: '3px',
                                cursor: 'pointer',
                                fontSize: '12px'
                            }}
                        >
                            🚨 신고
                        </button>
                    </div>

                    <div style={{ fontSize: '16px', lineHeight: '1.8', marginBottom: '30px', whiteSpace: 'pre-wrap' }}>
                        {post.content}
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'center', gap: '15px', paddingTop: '20px', borderTop: '1px solid #e0e0e0' }}>
                        <button
                            onClick={handleLike}
                            style={{
                                padding: '15px 30px',
                                backgroundColor: '#646cff',
                                color: 'white',
                                border: 'none',
                                borderRadius: '5px',
                                cursor: 'pointer',
                                fontSize: '16px',
                                fontWeight: 'bold'
                            }}
                        >
                            👍 추천 {post.likeCount}
                        </button>

                        <button
                            onClick={handleDislike}
                            style={{
                                padding: '15px 30px',
                                backgroundColor: '#888',
                                color: 'white',
                                border: 'none',
                                borderRadius: '5px',
                                cursor: 'pointer',
                                fontSize: '16px',
                                fontWeight: 'bold'
                            }}
                        >
                            👎 비추천 {post.dislikeCount}
                        </button>

                        <button
                            onClick={handleScrap}
                            style={{
                                padding: '15px 30px',
                                backgroundColor: '#4CAF50',
                                color: 'white',
                                border: 'none',
                                borderRadius: '5px',
                                cursor: 'pointer',
                                fontSize: '16px',
                                fontWeight: 'bold'
                            }}
                        >
                            ⭐ 스크랩
                        </button>
                    </div>
                </div>

                {/* ⭐ 댓글 섹션 - ref 추가 */}
                <div
                    ref={commentSectionRef}
                    style={{
                        backgroundColor: 'white',
                        border: '1px solid #e0e0e0',
                        borderRadius: '10px',
                        padding: '30px'
                    }}
                >
                    <h2 style={{ fontSize: '20px', fontWeight: 'bold', marginBottom: '20px' }}>
                        💬 댓글 {totalComments}
                    </h2>

                    {isLoggedIn() && (
                        <div style={{ marginBottom: '30px' }}>
                            {replyTo && (
                                <div style={{
                                    padding: '10px',
                                    backgroundColor: '#f0f0f0',
                                    marginBottom: '10px',
                                    borderRadius: '5px',
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center'
                                }}>
                                    <span>답글 작성 중...</span>
                                    <button
                                        onClick={() => setReplyTo(null)}
                                        style={{
                                            padding: '5px 10px',
                                            backgroundColor: '#ddd',
                                            border: 'none',
                                            borderRadius: '3px',
                                            cursor: 'pointer'
                                        }}
                                    >
                                        취소
                                    </button>
                                </div>
                            )}

                            <textarea
                                value={newComment}
                                onChange={(e) => setNewComment(e.target.value)}
                                placeholder={replyTo ? "답글을 입력하세요..." : "댓글을 입력하세요..."}
                                style={{
                                    width: '100%',
                                    padding: '15px',
                                    border: '1px solid #ddd',
                                    borderRadius: '5px',
                                    fontSize: '14px',
                                    minHeight: '100px',
                                    resize: 'vertical',
                                    boxSizing: 'border-box'
                                }}
                            />

                            <button
                                onClick={handleCreateComment}
                                disabled={commentLoading || !newComment.trim()}  // ⭐ 로딩 중일 때, 내용 없을 때 비활성화
                                style={{
                                    marginTop: '10px',
                                    padding: '10px 20px',
                                    backgroundColor: commentLoading || !newComment.trim() ? '#ccc' : '#646cff',  // ⭐ 로딩 중 색상 변경
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '5px',
                                    cursor: commentLoading || !newComment.trim() ? 'not-allowed' : 'pointer',  // ⭐ 로딩 중 커서 변경
                                    fontSize: '16px',
                                    fontWeight: 'bold'
                                }}
                            >
                                {commentLoading ? '작성 중...' : (replyTo ? '답글 작성' : '댓글 작성')}  {/* ⭐ 로딩 중 텍스트 */}
                            </button>
                        </div>
                    )}

                    {/* 댓글 목록 */}
                    <div>
                        {comments.length === 0 ? (
                            <div style={{ textAlign: 'center', padding: '50px', color: '#888' }}>
                                첫 댓글을 작성해보세요!
                            </div>
                        ) : (
                            <>
                                {/* ⭐ 베스트 댓글 섹션 - 백엔드에서 받은 데이터 사용 */}
                                {bestComments && bestComments.length > 0 && (
                                    <div style={{ marginBottom: '30px' }}>
                                        <h3 style={{
                                            fontSize: '16px',
                                            fontWeight: 'bold',
                                            marginBottom: '15px',
                                            color: '#4da6ff'
                                        }}>
                                            ★ 베스트 댓글
                                        </h3>
                                        <div style={{ borderTop: '2px solid #4da6ff', paddingTop: '15px' }}>
                                            {bestComments.map((comment, index) => (
                                                <div
                                                    key={`best-${comment.commentId}-${index}`}
                                                    style={{
                                                        padding: '15px',
                                                        marginBottom: '10px',
                                                        backgroundColor: '#f0f8ff',
                                                        border: '1px solid #4da6ff',
                                                        borderRadius: '5px'
                                                    }}
                                                >
                                                    <div style={{
                                                        display: 'flex',
                                                        justifyContent: 'space-between',
                                                        alignItems: 'center',
                                                        marginBottom: '10px'
                                                    }}>
                                                        <div>
                    <span style={{
                        backgroundColor: '#4da6ff',
                        color: 'white',
                        padding: '2px 8px',
                        borderRadius: '3px',
                        fontSize: '11px',
                        fontWeight: 'bold',
                        marginRight: '8px'
                    }}>
                      ★ BEST
                    </span>
                                                            <span style={{ fontWeight: 'bold', marginRight: '10px' }}>
                      {comment.nickname}
                    </span>
                                                            <span style={{ fontSize: '12px', color: '#888' }}>
                      {new Date(comment.createdAt).toLocaleString('ko-KR')}
                    </span>
                                                        </div>
                                                    </div>
                                                    <div style={{ marginBottom: '10px', whiteSpace: 'pre-wrap' }}>
                                                        {comment.content}
                                                    </div>
                                                    <div style={{ display: 'flex', gap: '10px', fontSize: '14px' }}>
                                                        <button
                                                            onClick={() => handleLikeComment(comment.commentId)}
                                                            style={{
                                                                padding: '5px 10px',
                                                                backgroundColor: '#f0f0f0',
                                                                border: 'none',
                                                                borderRadius: '3px',
                                                                cursor: 'pointer'
                                                            }}
                                                        >
                                                            👍 {comment.likeCount}
                                                        </button>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}

                                {/* ⭐ 일반 댓글 섹션 */}
                                <div>
                                    <h3 style={{ fontSize: '16px', fontWeight: 'bold', marginBottom: '15px' }}>
                                        💬 댓글 {totalComments}
                                    </h3>
                                    {comments.map(comment => renderComment(comment))}
                                </div>
                            </>
                        )}
                    </div>

                    {/* ⭐ 댓글 페이지네이션 - 30개 초과 시에만 표시 */}
                    {totalComments > 30 && (
                        <div style={{
                            display: 'flex',
                            justifyContent: 'center',
                            alignItems: 'center',
                            gap: '5px',
                            marginTop: '30px',
                            paddingTop: '20px',
                            borderTop: '1px solid #e0e0e0'
                        }}>
                            {/* 10페이지 이전 */}
                            <button
                                onClick={() => handlePageChange(currentPage - 10)}
                                disabled={currentPage < 10}
                                style={{
                                    padding: '8px 15px',
                                    backgroundColor: 'white',
                                    border: '1px solid #ddd',
                                    borderRadius: '5px',
                                    cursor: currentPage < 10 ? 'not-allowed' : 'pointer',
                                    fontSize: '14px',
                                    color: currentPage < 10 ? '#ccc' : '#333'
                                }}
                            >
                                &lt;&lt;
                            </button>

                            {/* 1페이지 이전 */}
                            <button
                                onClick={() => handlePageChange(currentPage - 1)}
                                disabled={currentPage === 0}
                                style={{
                                    padding: '8px 15px',
                                    backgroundColor: 'white',
                                    border: '1px solid #ddd',
                                    borderRadius: '5px',
                                    cursor: currentPage === 0 ? 'not-allowed' : 'pointer',
                                    fontSize: '14px',
                                    minWidth: '60px',
                                    color: currentPage === 0 ? '#ccc' : '#333'
                                }}
                            >
                                Prev
                            </button>

                            {/* 페이지 번호 버튼들 (1~10) */}
                            <div style={{ display: 'flex', gap: '5px' }}>
                                {(() => {
                                    const startPage = Math.floor(currentPage / 10) * 10;
                                    const endPage = Math.min(startPage + 10, totalPages);
                                    const pages = [];

                                    for (let i = startPage; i < endPage; i++) {
                                        pages.push(
                                            <button
                                                key={i}
                                                onClick={() => handlePageChange(i)}
                                                style={{
                                                    minWidth: '40px',
                                                    padding: '8px 12px',
                                                    backgroundColor: currentPage === i ? '#646cff' : 'white',
                                                    color: currentPage === i ? 'white' : '#333',
                                                    border: '1px solid #ddd',
                                                    borderRadius: '5px',
                                                    cursor: 'pointer',
                                                    fontSize: '14px',
                                                    fontWeight: currentPage === i ? 'bold' : 'normal'
                                                }}
                                            >
                                                {i + 1}
                                            </button>
                                        );
                                    }

                                    return pages;
                                })()}
                            </div>

                            {/* 1페이지 다음 */}
                            <button
                                onClick={() => handlePageChange(currentPage + 1)}
                                disabled={currentPage >= totalPages - 1}
                                style={{
                                    padding: '8px 15px',
                                    backgroundColor: 'white',
                                    border: '1px solid #ddd',
                                    borderRadius: '5px',
                                    cursor: currentPage >= totalPages - 1 ? 'not-allowed' : 'pointer',
                                    fontSize: '14px',
                                    minWidth: '60px',
                                    color: currentPage >= totalPages - 1 ? '#ccc' : '#333'
                                }}
                            >
                                Next
                            </button>

                            {/* 10페이지 다음 */}
                            <button
                                onClick={() => handlePageChange(currentPage + 10)}
                                disabled={currentPage >= totalPages - 10}
                                style={{
                                    padding: '8px 15px',
                                    backgroundColor: 'white',
                                    border: '1px solid #ddd',
                                    borderRadius: '5px',
                                    cursor: currentPage >= totalPages - 10 ? 'not-allowed' : 'pointer',
                                    fontSize: '14px',
                                    color: currentPage >= totalPages - 10 ? '#ccc' : '#333'
                                }}
                            >
                                &gt;&gt;
                            </button>
                        </div>
                    )}
                </div>

                {/* 신고 모달 */}
                {showReportModal && (
                    <div style={{
                        position: 'fixed',
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        backgroundColor: 'rgba(0,0,0,0.5)',
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        zIndex: 1000
                    }}>
                        <div style={{
                            backgroundColor: 'white',
                            padding: '30px',
                            borderRadius: '10px',
                            width: '90%',
                            maxWidth: '500px'
                        }}>
                            <h2 style={{ marginBottom: '20px' }}>🚨 게시글 신고</h2>

                            <textarea
                                placeholder="신고 사유를 입력해주세요"
                                value={reportReason}
                                onChange={(e) => setReportReason(e.target.value)}
                                style={{
                                    width: '100%',
                                    padding: '10px',
                                    marginBottom: '15px',
                                    border: '1px solid #ddd',
                                    borderRadius: '5px',
                                    fontSize: '14px',
                                    minHeight: '150px',
                                    resize: 'vertical',
                                    boxSizing: 'border-box'
                                }}
                            />

                            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                                <button
                                    onClick={() => setShowReportModal(false)}
                                    style={{
                                        padding: '10px 20px',
                                        backgroundColor: '#ddd',
                                        border: 'none',
                                        borderRadius: '5px',
                                        cursor: 'pointer'
                                    }}
                                >
                                    취소
                                </button>
                                <button
                                    onClick={handleReportPost}
                                    style={{
                                        padding: '10px 20px',
                                        backgroundColor: '#ff4444',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '5px',
                                        cursor: 'pointer'
                                    }}
                                >
                                    신고하기
                                </button>
                            </div>
                        </div>
                    </div>
                )}
                {/* 댓글 신고 모달 - 여기에 추가! */}
                {showCommentReportModal && (
                    <div style={{
                        position: 'fixed',
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        backgroundColor: 'rgba(0,0,0,0.5)',
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        zIndex: 1000
                    }}>
                        <div style={{
                            backgroundColor: 'white',
                            padding: '30px',
                            borderRadius: '10px',
                            width: '90%',
                            maxWidth: '500px'
                        }}>
                            <h2 style={{ marginBottom: '20px' }}>🚨 댓글 신고</h2>

                            <textarea
                                placeholder="신고 사유를 입력해주세요"
                                value={commentReportReason}
                                onChange={(e) => setCommentReportReason(e.target.value)}
                                style={{
                                    width: '100%',
                                    padding: '10px',
                                    marginBottom: '15px',
                                    border: '1px solid #ddd',
                                    borderRadius: '5px',
                                    fontSize: '14px',
                                    minHeight: '150px',
                                    resize: 'vertical',
                                    boxSizing: 'border-box'
                                }}
                            />

                            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                                <button
                                    onClick={() => {
                                        setShowCommentReportModal(false);
                                        setCommentReportReason('');
                                        setReportingCommentId(null);
                                    }}
                                    style={{
                                        padding: '10px 20px',
                                        backgroundColor: '#ddd',
                                        border: 'none',
                                        borderRadius: '5px',
                                        cursor: 'pointer'
                                    }}
                                >
                                    취소
                                </button>
                                <button
                                    onClick={handleReportComment}
                                    style={{
                                        padding: '10px 20px',
                                        backgroundColor: '#ff4444',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '5px',
                                        cursor: 'pointer'
                                    }}
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