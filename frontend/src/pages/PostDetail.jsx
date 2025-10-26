/**
 * 게시글 상세 페이지
 * - 게시글 내용 표시
 * - 추천/비추천 기능
 * - 댓글 및 대댓글 작성/수정/삭제
 * - 스크랩 기능
 * 
 * 파일 위치: frontend/src/pages/PostDetail.jsx
 */

import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import {
  getPost,
  likePost,
  dislikePost,
  scrapPost,
  unscrapPost,
  getComments,
  createComment,
  deleteComment,
  likeComment,
  dislikeComment,
  reportPost,
  reportComment
} from '../api/community';
import { getUserData, isLoggedIn } from '../api/api';

function PostDetail() {
  const { postId } = useParams(); // URL 파라미터에서 postId 추출
  const navigate = useNavigate();
  
  // 상태 관리
  const [post, setPost] = useState(null); // 게시글 정보
  const [comments, setComments] = useState([]); // 댓글 목록
  const [loading, setLoading] = useState(false); // 로딩 상태
  const [newComment, setNewComment] = useState(''); // 새 댓글 내용
  const [replyTo, setReplyTo] = useState(null); // 답글 대상 댓글
  const [showReportModal, setShowReportModal] = useState(false); // 신고 모달
  const [reportReason, setReportReason] = useState(''); // 신고 사유

  // 현재 로그인한 사용자
  const currentUser = getUserData();

  /**
   * 게시글 및 댓글 조회
   */
  const fetchPostAndComments = async () => {
    setLoading(true);
    try {
      const [postData, commentsData] = await Promise.all([
        getPost(postId),
        getComments(postId)
      ]);
      
      setPost(postData);
      setComments(commentsData);
    } catch (error) {
      console.error('데이터 조회 오류:', error);
      alert('게시글을 불러오는데 실패했습니다.');
      navigate('/community');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPostAndComments();
  }, [postId]);

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
      fetchPostAndComments();
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
      fetchPostAndComments();
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

    try {
      await createComment(postId, newComment, replyTo);
      alert(replyTo ? '답글이 작성되었습니다.' : '댓글이 작성되었습니다.');
      setNewComment('');
      setReplyTo(null);
      fetchPostAndComments();
    } catch (error) {
      alert(error.message || '댓글 작성에 실패했습니다.');
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
      fetchPostAndComments();
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
      fetchPostAndComments();
    } catch (error) {
      alert(error.message || '추천에 실패했습니다.');
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
            <span style={{ fontWeight: 'bold', marginRight: '10px' }}>{comment.nickname}</span>
            <span style={{ fontSize: '12px', color: '#888' }}>
              {new Date(comment.createdAt).toLocaleString('ko-KR')}
            </span>
          </div>
          
          {isMyComment && (
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
        </div>

        {/* 대댓글 렌더링 */}
        {comment.replies && comment.replies.length > 0 && (
          <div style={{ marginTop: '15px' }}>
            {comment.replies.map(reply => renderComment(reply, true))}
          </div>
        )}
      </div>
    );
  };

  if (loading || !post) {
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
        
        {/* 뒤로가기 버튼 */}
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
          {/* 제목 */}
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

          {/* 작성자 정보 */}
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

          {/* 본문 내용 */}
          <div style={{ fontSize: '16px', lineHeight: '1.8', marginBottom: '30px', whiteSpace: 'pre-wrap' }}>
            {post.content}
          </div>

          {/* 추천/비추천/스크랩 버튼 */}
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

        {/* 댓글 섹션 */}
        <div style={{
          backgroundColor: 'white',
          border: '1px solid #e0e0e0',
          borderRadius: '10px',
          padding: '30px'
        }}>
          <h2 style={{ fontSize: '20px', fontWeight: 'bold', marginBottom: '20px' }}>
            💬 댓글 {post.commentCount}
          </h2>

          {/* 댓글 작성 */}
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
                style={{
                  marginTop: '10px',
                  padding: '10px 20px',
                  backgroundColor: '#646cff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '5px',
                  cursor: 'pointer',
                  fontSize: '16px',
                  fontWeight: 'bold'
                }}
              >
                {replyTo ? '답글 작성' : '댓글 작성'}
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
              comments.map(comment => renderComment(comment))
            )}
          </div>
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
      </div>
    </div>
  );
}

export default PostDetail;