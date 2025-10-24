import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import CommentSection from '../components/CommentSection';

/**
 * 게시글 상세 페이지
 */
function PostDetail() {
  const { postId } = useParams();
  const navigate = useNavigate();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);

  // 로그인 상태 확인
  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) {
      try {
        setUser(JSON.parse(userData));
      } catch (e) {
        console.error('사용자 정보 파싱 오류:', e);
      }
    }
  }, []);

  // 게시글 조회
  useEffect(() => {
    const fetchPost = async () => {
      setLoading(true);
      try {
        const username = user ? user.username : '';
        const response = await fetch(
          `http://localhost:8080/api/community/posts/${postId}?username=${username}`
        );
        if (!response.ok) throw new Error('게시글을 찾을 수 없습니다.');
        const data = await response.json();
        setPost(data);
      } catch (error) {
        console.error('게시글 조회 실패:', error);
        alert('게시글을 불러올 수 없습니다.');
        navigate('/community');
      } finally {
        setLoading(false);
      }
    };

    fetchPost();
  }, [postId, user, navigate]);

  // 추천
  const handleLike = async () => {
    if (!user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/api/community/posts/${postId}/like`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username: user.username })
        }
      );

      if (response.ok) {
        // 새로고침
        window.location.reload();
      }
    } catch (error) {
      console.error('추천 실패:', error);
      alert('추천에 실패했습니다.');
    }
  };

  // 비추천
  const handleDislike = async () => {
    if (!user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/api/community/posts/${postId}/dislike`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username: user.username })
        }
      );

      if (response.ok) {
        window.location.reload();
      }
    } catch (error) {
      console.error('비추천 실패:', error);
      alert('비추천에 실패했습니다.');
    }
  };

  // 스크랩
  const handleScrap = async () => {
    if (!user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/api/community/posts/${postId}/scrap`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username: user.username })
        }
      );

      if (response.ok) {
        window.location.reload();
      }
    } catch (error) {
      console.error('스크랩 실패:', error);
      alert('스크랩에 실패했습니다.');
    }
  };

  // 게시글 삭제
  const handleDelete = async () => {
    if (!window.confirm('정말 삭제하시겠습니까?')) return;

    try {
      const response = await fetch(
        `http://localhost:8080/api/community/posts/${postId}?username=${user.username}`,
        { method: 'DELETE' }
      );

      if (response.ok) {
        alert('게시글이 삭제되었습니다.');
        navigate('/community');
      } else {
        alert('게시글 삭제에 실패했습니다.');
      }
    } catch (error) {
      console.error('삭제 실패:', error);
      alert('게시글 삭제에 실패했습니다.');
    }
  };

  // 신고
  const handleReport = async () => {
    if (!user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    const reason = prompt('신고 사유를 선택하세요:\n1. 욕설\n2. 음란물\n3. 도배\n4. 기타');
    if (!reason) return;

    const description = prompt('상세 설명을 입력하세요 (선택):');

    try {
      const response = await fetch(
        `http://localhost:8080/api/community/posts/${postId}/report`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            username: user.username,
            reason,
            description: description || ''
          })
        }
      );

      if (response.ok) {
        alert('신고가 접수되었습니다.');
      } else {
        alert('신고에 실패했습니다.');
      }
    } catch (error) {
      console.error('신고 실패:', error);
      alert('신고에 실패했습니다.');
    }
  };

  if (loading) {
    return (
      <div>
        <Navbar />
        <div style={{ maxWidth: '900px', margin: '0 auto', padding: '20px', textAlign: 'center' }}>
          로딩 중...
        </div>
      </div>
    );
  }

  if (!post) return null;

  const isAuthor = user && user.username === post.username;

  return (
    <div>
      <Navbar />
      <div style={{ maxWidth: '900px', margin: '0 auto', padding: '20px' }}>
        {/* 상단 버튼 */}
        <div style={{ marginBottom: '20px' }}>
          <button
            onClick={() => navigate('/community')}
            style={{
              padding: '10px 20px',
              backgroundColor: '#f5f5f5',
              border: '1px solid #ddd',
              borderRadius: '5px',
              cursor: 'pointer'
            }}
          >
            ← 목록으로
          </button>
        </div>

        {/* 게시글 헤더 */}
        <div style={{
          backgroundColor: '#fff',
          padding: '30px',
          borderRadius: '8px',
          border: '1px solid #e0e0e0',
          marginBottom: '20px'
        }}>
          {/* 배지 */}
          <div style={{ marginBottom: '15px' }}>
            {post.isNotice && (
              <span style={{
                backgroundColor: '#ff4444',
                color: 'white',
                padding: '4px 12px',
                borderRadius: '4px',
                fontSize: '13px',
                marginRight: '8px',
                fontWeight: 'bold'
              }}>
                📌 공지
              </span>
            )}
            {post.isBest && (
              <span style={{
                backgroundColor: '#ffd700',
                color: '#333',
                padding: '4px 12px',
                borderRadius: '4px',
                fontSize: '13px',
                marginRight: '8px',
                fontWeight: 'bold'
              }}>
                ⭐ 베스트
              </span>
            )}
            {post.isPopular && !post.isBest && (
              <span style={{
                backgroundColor: '#646cff',
                color: 'white',
                padding: '4px 12px',
                borderRadius: '4px',
                fontSize: '13px',
                marginRight: '8px',
                fontWeight: 'bold'
              }}>
                🔥 인기
              </span>
            )}
            <span style={{
              backgroundColor: '#f0f0f0',
              color: '#666',
              padding: '4px 12px',
              borderRadius: '4px',
              fontSize: '13px'
            }}>
              {post.categoryName}
            </span>
          </div>

          {/* 제목 */}
          <h1 style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '15px' }}>
            {post.title}
          </h1>

          {/* 작성자 정보 */}
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            paddingBottom: '15px',
            borderBottom: '1px solid #e0e0e0'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '14px', color: '#666' }}>
              <span style={{ fontWeight: 'bold', color: '#333' }}>{post.nickname}</span>
              <span>|</span>
              <span>{new Date(post.createdAt).toLocaleString()}</span>
              <span>|</span>
              <span>조회 {post.viewCount}</span>
            </div>

            {/* 작성자 액션 버튼 */}
            {isAuthor && (
              <div style={{ display: 'flex', gap: '10px' }}>
                <button
                  onClick={() => navigate(`/community/edit/${postId}`)}
                  style={{
                    padding: '6px 15px',
                    backgroundColor: '#646cff',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '13px'
                  }}
                >
                  수정
                </button>
                <button
                  onClick={handleDelete}
                  style={{
                    padding: '6px 15px',
                    backgroundColor: '#ff4444',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontSize: '13px'
                  }}
                >
                  삭제
                </button>
              </div>
            )}
          </div>
        </div>

        {/* 게시글 내용 */}
        <div style={{
          backgroundColor: '#fff',
          padding: '30px',
          borderRadius: '8px',
          border: '1px solid #e0e0e0',
          marginBottom: '20px',
          minHeight: '300px',
          lineHeight: '1.8',
          fontSize: '16px',
          whiteSpace: 'pre-wrap'
        }}>
          {post.content}

          {/* 첨부파일 */}
          {post.attachments && post.attachments.length > 0 && (
            <div style={{ marginTop: '30px', paddingTop: '20px', borderTop: '1px solid #e0e0e0' }}>
              <h3 style={{ fontSize: '16px', marginBottom: '15px', fontWeight: 'bold' }}>첨부파일</h3>
              {post.attachments.map((attachment) => (
                <div key={attachment.attachmentId} style={{ marginBottom: '10px' }}>
                  {attachment.fileType === 'IMAGE' && (
                    <img
                      src={attachment.fileUrl}
                      alt={attachment.fileName}
                      style={{ maxWidth: '100%', borderRadius: '8px' }}
                    />
                  )}
                  {attachment.fileType === 'LINK' && (
                    <a
                      href={attachment.fileUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      style={{ color: '#646cff', textDecoration: 'underline' }}
                    >
                      {attachment.fileName || attachment.fileUrl}
                    </a>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* 추천/비추천 버튼 */}
        <div style={{
          backgroundColor: '#fff',
          padding: '20px',
          borderRadius: '8px',
          border: '1px solid #e0e0e0',
          marginBottom: '20px',
          display: 'flex',
          justifyContent: 'center',
          gap: '20px'
        }}>
          <button
            onClick={handleLike}
            style={{
              padding: '15px 40px',
              backgroundColor: post.userLikeStatus === 'LIKE' ? '#646cff' : '#f5f5f5',
              color: post.userLikeStatus === 'LIKE' ? 'white' : '#333',
              border: '1px solid #ddd',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '16px',
              fontWeight: 'bold',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '5px'
            }}
          >
            <span style={{ fontSize: '24px' }}>👍</span>
            <span>추천 {post.likeCount}</span>
          </button>

          <button
            onClick={handleDislike}
            style={{
              padding: '15px 40px',
              backgroundColor: post.userLikeStatus === 'DISLIKE' ? '#ff4444' : '#f5f5f5',
              color: post.userLikeStatus === 'DISLIKE' ? 'white' : '#333',
              border: '1px solid #ddd',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '16px',
              fontWeight: 'bold',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '5px'
            }}
          >
            <span style={{ fontSize: '24px' }}>👎</span>
            <span>비추천 {post.dislikeCount}</span>
          </button>
        </div>

        {/* 하단 액션 버튼 */}
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          marginBottom: '30px'
        }}>
          <div style={{ display: 'flex', gap: '10px' }}>
            <button
              onClick={handleScrap}
              style={{
                padding: '10px 20px',
                backgroundColor: post.isScraped ? '#646cff' : '#f5f5f5',
                color: post.isScraped ? 'white' : '#333',
                border: '1px solid #ddd',
                borderRadius: '5px',
                cursor: 'pointer'
              }}
            >
              {post.isScraped ? '📌 스크랩 취소' : '📌 스크랩'}
            </button>
          </div>

          <div>
            <button
              onClick={handleReport}
              style={{
                padding: '10px 20px',
                backgroundColor: '#f5f5f5',
                color: '#666',
                border: '1px solid #ddd',
                borderRadius: '5px',
                cursor: 'pointer'
              }}
            >
              🚨 신고
            </button>
          </div>
        </div>

        {/* 댓글 섹션 */}
        <CommentSection postId={postId} currentUser={user} />
      </div>
    </div>
  );
}

export default PostDetail;
