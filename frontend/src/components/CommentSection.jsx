import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * 댓글 섹션 컴포넌트
 */
function CommentSection({ postId, currentUser }) {
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newComment, setNewComment] = useState('');
  const [replyTo, setReplyTo] = useState(null);
  const [replyContent, setReplyContent] = useState('');
  const navigate = useNavigate();

  // 댓글 목록 조회
  const fetchComments = async () => {
    setLoading(true);
    try {
      const response = await fetch(
        `http://localhost:8080/api/community/posts/${postId}/comments`
      );
      const data = await response.json();
      setComments(data);
    } catch (error) {
      console.error('댓글 조회 실패:', error);
      setComments([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchComments();
  }, [postId]);

  // 댓글 작성
  const handleSubmitComment = async (e) => {
    e.preventDefault();

    if (!currentUser) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    if (!newComment.trim()) {
      alert('댓글 내용을 입력하세요.');
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/api/community/posts/${postId}/comments`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            username: currentUser.username,
            content: newComment,
            parentCommentId: null
          })
        }
      );

      if (response.ok) {
        setNewComment('');
        fetchComments();
      } else {
        alert('댓글 작성에 실패했습니다.');
      }
    } catch (error) {
      console.error('댓글 작성 실패:', error);
      alert('댓글 작성에 실패했습니다.');
    }
  };

  // 대댓글 작성
  const handleSubmitReply = async (parentCommentId) => {
    if (!currentUser) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    if (!replyContent.trim()) {
      alert('댓글 내용을 입력하세요.');
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/api/community/posts/${postId}/comments`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            username: currentUser.username,
            content: replyContent,
            parentCommentId: parentCommentId
          })
        }
      );

      if (response.ok) {
        setReplyContent('');
        setReplyTo(null);
        fetchComments();
      } else {
        alert('댓글 작성에 실패했습니다.');
      }
    } catch (error) {
      console.error('댓글 작성 실패:', error);
      alert('댓글 작성에 실패했습니다.');
    }
  };

  // 댓글 삭제
  const handleDeleteComment = async (commentId) => {
    if (!window.confirm('정말 삭제하시겠습니까?')) return;

    try {
      const response = await fetch(
        `http://localhost:8080/api/community/comments/${commentId}?username=${currentUser.username}`,
        { method: 'DELETE' }
      );

      if (response.ok) {
        alert('댓글이 삭제되었습니다.');
        fetchComments();
      } else {
        alert('댓글 삭제에 실패했습니다.');
      }
    } catch (error) {
      console.error('댓글 삭제 실패:', error);
      alert('댓글 삭제에 실패했습니다.');
    }
  };

  // 댓글 구조화 (부모-자식 관계)
  const structureComments = () => {
    const parentComments = comments.filter(c => !c.parentCommentId);
    const childComments = comments.filter(c => c.parentCommentId);

    return parentComments.map(parent => ({
      ...parent,
      replies: childComments.filter(child => child.parentCommentId === parent.commentId)
    }));
  };

  const structuredComments = structureComments();

  // 티어 배지 색상
  const getTierColor = (tier) => {
    const colors = {
      BRONZE: '#CD7F32',
      SILVER: '#C0C0C0',
      GOLD: '#FFD700',
      PLATINUM: '#E5E4E2',
      DIAMOND: '#B9F2FF'
    };
    return colors[tier] || '#999';
  };

  return (
    <div style={{
      backgroundColor: '#fff',
      padding: '30px',
      borderRadius: '8px',
      border: '1px solid #e0e0e0'
    }}>
      <h2 style={{ fontSize: '20px', fontWeight: 'bold', marginBottom: '20px' }}>
        댓글 {comments.length}개
      </h2>

      {/* 댓글 작성 폼 */}
      <form onSubmit={handleSubmitComment} style={{ marginBottom: '30px' }}>
        <textarea
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          placeholder={currentUser ? '댓글을 입력하세요...' : '로그인이 필요합니다.'}
          disabled={!currentUser}
          style={{
            width: '100%',
            minHeight: '100px',
            padding: '15px',
            border: '1px solid #ddd',
            borderRadius: '5px',
            fontSize: '14px',
            resize: 'vertical',
            fontFamily: 'inherit'
          }}
        />
        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '10px' }}>
          <button
            type="submit"
            disabled={!currentUser}
            style={{
              padding: '10px 25px',
              backgroundColor: currentUser ? '#646cff' : '#ccc',
              color: 'white',
              border: 'none',
              borderRadius: '5px',
              cursor: currentUser ? 'pointer' : 'not-allowed',
              fontSize: '14px',
              fontWeight: 'bold'
            }}
          >
            댓글 작성
          </button>
        </div>
      </form>

      {/* 댓글 목록 */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '30px', color: '#888' }}>
          댓글을 불러오는 중...
        </div>
      ) : structuredComments.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '30px', color: '#888' }}>
          첫 댓글을 작성해보세요!
        </div>
      ) : (
        <div>
          {structuredComments.map((comment) => (
            <div key={comment.commentId} style={{ marginBottom: '20px' }}>
              {/* 부모 댓글 */}
              <div style={{
                padding: '15px',
                backgroundColor: '#f9f9f9',
                borderRadius: '5px',
                border: '1px solid #e0e0e0'
              }}>
                {comment.isDeleted ? (
                  <div style={{ color: '#999', fontStyle: 'italic' }}>
                    삭제된 댓글입니다.
                  </div>
                ) : (
                  <>
                    {/* 작성자 정보 */}
                    <div style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      marginBottom: '10px'
                    }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <span style={{ fontWeight: 'bold', color: '#333' }}>
                          {comment.nickname}
                        </span>
                        <span style={{
                          fontSize: '11px',
                          padding: '2px 6px',
                          borderRadius: '3px',
                          backgroundColor: getTierColor(comment.userTier),
                          color: 'white',
                          fontWeight: 'bold'
                        }}>
                          {comment.userTier}
                        </span>
                        <span style={{ fontSize: '13px', color: '#888' }}>
                          {new Date(comment.createdAt).toLocaleString()}
                        </span>
                      </div>

                      {/* 액션 버튼 */}
                      <div style={{ display: 'flex', gap: '10px' }}>
                        <button
                          onClick={() => setReplyTo(replyTo === comment.commentId ? null : comment.commentId)}
                          style={{
                            padding: '5px 12px',
                            backgroundColor: '#f5f5f5',
                            border: '1px solid #ddd',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '12px'
                          }}
                        >
                          답글
                        </button>
                        {currentUser && currentUser.username === comment.username && (
                          <button
                            onClick={() => handleDeleteComment(comment.commentId)}
                            style={{
                              padding: '5px 12px',
                              backgroundColor: '#ff4444',
                              color: 'white',
                              border: 'none',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '12px'
                            }}
                          >
                            삭제
                          </button>
                        )}
                      </div>
                    </div>

                    {/* 댓글 내용 */}
                    <div style={{
                      fontSize: '14px',
                      lineHeight: '1.6',
                      whiteSpace: 'pre-wrap',
                      color: '#333'
                    }}>
                      {comment.content}
                    </div>
                  </>
                )}
              </div>

              {/* 대댓글 작성 폼 */}
              {replyTo === comment.commentId && (
                <div style={{
                  marginLeft: '40px',
                  marginTop: '10px',
                  padding: '15px',
                  backgroundColor: '#f0f0f0',
                  borderRadius: '5px'
                }}>
                  <textarea
                    value={replyContent}
                    onChange={(e) => setReplyContent(e.target.value)}
                    placeholder="답글을 입력하세요..."
                    style={{
                      width: '100%',
                      minHeight: '80px',
                      padding: '10px',
                      border: '1px solid #ddd',
                      borderRadius: '5px',
                      fontSize: '14px',
                      resize: 'vertical',
                      fontFamily: 'inherit'
                    }}
                  />
                  <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
                    <button
                      onClick={() => {
                        setReplyTo(null);
                        setReplyContent('');
                      }}
                      style={{
                        padding: '8px 20px',
                        backgroundColor: '#f5f5f5',
                        border: '1px solid #ddd',
                        borderRadius: '5px',
                        cursor: 'pointer',
                        fontSize: '13px'
                      }}
                    >
                      취소
                    </button>
                    <button
                      onClick={() => handleSubmitReply(comment.commentId)}
                      style={{
                        padding: '8px 20px',
                        backgroundColor: '#646cff',
                        color: 'white',
                        border: 'none',
                        borderRadius: '5px',
                        cursor: 'pointer',
                        fontSize: '13px',
                        fontWeight: 'bold'
                      }}
                    >
                      답글 작성
                    </button>
                  </div>
                </div>
              )}

              {/* 대댓글 목록 */}
              {comment.replies && comment.replies.length > 0 && (
                <div style={{ marginLeft: '40px', marginTop: '10px' }}>
                  {comment.replies.map((reply) => (
                    <div
                      key={reply.commentId}
                      style={{
                        padding: '15px',
                        backgroundColor: '#fafafa',
                        borderRadius: '5px',
                        border: '1px solid #e0e0e0',
                        marginBottom: '10px'
                      }}
                    >
                      {reply.isDeleted ? (
                        <div style={{ color: '#999', fontStyle: 'italic' }}>
                          삭제된 댓글입니다.
                        </div>
                      ) : (
                        <>
                          {/* 작성자 정보 */}
                          <div style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            marginBottom: '10px'
                          }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                              <span style={{ fontSize: '16px' }}>↳</span>
                              <span style={{ fontWeight: 'bold', color: '#333' }}>
                                {reply.nickname}
                              </span>
                              <span style={{
                                fontSize: '11px',
                                padding: '2px 6px',
                                borderRadius: '3px',
                                backgroundColor: getTierColor(reply.userTier),
                                color: 'white',
                                fontWeight: 'bold'
                              }}>
                                {reply.userTier}
                              </span>
                              <span style={{ fontSize: '13px', color: '#888' }}>
                                {new Date(reply.createdAt).toLocaleString()}
                              </span>
                            </div>

                            {currentUser && currentUser.username === reply.username && (
                              <button
                                onClick={() => handleDeleteComment(reply.commentId)}
                                style={{
                                  padding: '5px 12px',
                                  backgroundColor: '#ff4444',
                                  color: 'white',
                                  border: 'none',
                                  borderRadius: '4px',
                                  cursor: 'pointer',
                                  fontSize: '12px'
                                }}
                              >
                                삭제
                              </button>
                            )}
                          </div>

                          {/* 댓글 내용 */}
                          <div style={{
                            fontSize: '14px',
                            lineHeight: '1.6',
                            whiteSpace: 'pre-wrap',
                            color: '#333'
                          }}>
                            {reply.content}
                          </div>
                        </>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default CommentSection;
