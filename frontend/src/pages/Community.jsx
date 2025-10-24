import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';

/**
 * 커뮤니티 페이지 - 완전 구현
 * 게시글 목록, 카테고리별 조회, 인기글, 전체글 탭
 */
function Community() {
  const [posts, setPosts] = useState([]);
  const [category, setCategory] = useState('전체글');
  const [view, setView] = useState('all'); // 'all' or 'popular'
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const navigate = useNavigate();

  const categories = ['전체글', '축구', '야구', '농구', '기타'];

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

  // 게시글 목록 조회
  const fetchPosts = async () => {
    setLoading(true);
    try {
      let url = '';
      const username = user ? user.username : '';

      if (category === '전체글') {
        if (view === 'popular') {
          url = `http://localhost:8080/api/community/posts/popular?page=${page}&size=20&username=${username}`;
        } else {
          url = `http://localhost:8080/api/community/posts?page=${page}&size=20&username=${username}`;
        }
      } else {
        if (view === 'popular') {
          url = `http://localhost:8080/api/community/posts/popular/${category}?page=${page}&size=20&username=${username}`;
        } else {
          url = `http://localhost:8080/api/community/posts/category/${category}?page=${page}&size=20&username=${username}`;
        }
      }

      const response = await fetch(url);
      const data = await response.json();
      setPosts(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (error) {
      console.error('게시글 조회 실패:', error);
      setPosts([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts();
  }, [category, view, page, user]);

  // 게시글 클릭 핸들러
  const handlePostClick = (postId) => {
    navigate(`/community/post/${postId}`);
  };

  // 글쓰기 버튼 핸들러
  const handleWritePost = () => {
    if (!user) {
      alert('글쓰기는 로그인 후 사용 가능합니다.');
      navigate('/login');
    } else {
      navigate('/community/write');
    }
  };

  return (
    <div>
      <Navbar />
      <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '20px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '20px' }}>
          💬 커뮤니티
        </h1>

        {/* 카테고리 탭 */}
        <div style={{
          display: 'flex',
          gap: '10px',
          marginBottom: '10px',
          borderBottom: '2px solid #e0e0e0',
          paddingBottom: '10px'
        }}>
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => {
                setCategory(cat);
                setPage(0);
              }}
              style={{
                padding: '10px 20px',
                backgroundColor: category === cat ? '#646cff' : '#f5f5f5',
                color: category === cat ? 'white' : '#333',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer',
                fontWeight: category === cat ? 'bold' : 'normal',
                transition: 'all 0.3s'
              }}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* 전체글/인기글 탭 */}
        <div style={{
          display: 'flex',
          gap: '10px',
          marginBottom: '20px'
        }}>
          <button
            onClick={() => {
              setView('all');
              setPage(0);
            }}
            style={{
              padding: '8px 20px',
              backgroundColor: view === 'all' ? '#646cff' : '#f5f5f5',
              color: view === 'all' ? 'white' : '#333',
              border: 'none',
              borderRadius: '5px',
              cursor: 'pointer',
              fontSize: '14px'
            }}
          >
            전체글
          </button>
          <button
            onClick={() => {
              setView('popular');
              setPage(0);
            }}
            style={{
              padding: '8px 20px',
              backgroundColor: view === 'popular' ? '#646cff' : '#f5f5f5',
              color: view === 'popular' ? 'white' : '#333',
              border: 'none',
              borderRadius: '5px',
              cursor: 'pointer',
              fontSize: '14px'
            }}
          >
            인기글
          </button>
        </div>

        {/* 게시글 목록 */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: '50px', color: '#888' }}>
            로딩 중...
          </div>
        ) : posts.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '50px', color: '#888' }}>
            게시글이 없습니다.
          </div>
        ) : (
          <div>
            {posts.map((post) => (
              <div
                key={post.postId}
                onClick={() => handlePostClick(post.postId)}
                style={{
                  padding: '20px',
                  marginBottom: '15px',
                  backgroundColor: '#f9f9f9',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  cursor: 'pointer',
                  transition: 'all 0.3s'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = '#f0f0f0';
                  e.currentTarget.style.transform = 'translateY(-2px)';
                  e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = '#f9f9f9';
                  e.currentTarget.style.transform = 'translateY(0)';
                  e.currentTarget.style.boxShadow = 'none';
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div style={{ flex: 1 }}>
                    {/* 배지 */}
                    <div style={{ marginBottom: '8px' }}>
                      {post.isNotice && (
                        <span style={{
                          backgroundColor: '#ff4444',
                          color: 'white',
                          padding: '3px 8px',
                          borderRadius: '3px',
                          fontSize: '12px',
                          marginRight: '5px',
                          fontWeight: 'bold'
                        }}>
                          📌 공지
                        </span>
                      )}
                      {post.isBest && (
                        <span style={{
                          backgroundColor: '#ffd700',
                          color: '#333',
                          padding: '3px 8px',
                          borderRadius: '3px',
                          fontSize: '12px',
                          marginRight: '5px',
                          fontWeight: 'bold'
                        }}>
                          ⭐ 베스트
                        </span>
                      )}
                      {post.isPopular && !post.isBest && (
                        <span style={{
                          backgroundColor: '#646cff',
                          color: 'white',
                          padding: '3px 8px',
                          borderRadius: '3px',
                          fontSize: '12px',
                          marginRight: '5px',
                          fontWeight: 'bold'
                        }}>
                          🔥 인기
                        </span>
                      )}
                    </div>

                    {/* 제목 */}
                    <div style={{ marginBottom: '8px' }}>
                      <span style={{ fontWeight: 'bold', fontSize: '18px', marginRight: '8px' }}>
                        {post.title}
                      </span>
                      {post.commentCount > 0 && (
                        <span style={{ color: '#646cff', fontSize: '14px', fontWeight: 'bold' }}>
                          [{post.commentCount}]
                        </span>
                      )}
                    </div>

                    {/* 작성자 및 메타 정보 */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '13px', color: '#666' }}>
                      <span style={{ fontWeight: 'bold' }}>{post.nickname}</span>
                      <span>|</span>
                      <span>{post.categoryName}</span>
                      <span>|</span>
                      <span>조회 {post.viewCount}</span>
                      <span>|</span>
                      <span>👍 {post.likeCount}</span>
                      <span>👎 {post.dislikeCount}</span>
                      <span>|</span>
                      <span>{new Date(post.createdAt).toLocaleDateString()}</span>
                    </div>
                  </div>

                  {/* 첨부파일 표시 */}
                  {post.attachments && post.attachments.length > 0 && (
                    <div style={{ marginLeft: '15px', color: '#888', fontSize: '24px' }}>
                      📎
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 페이지네이션 */}
        {totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'center', gap: '5px', marginTop: '30px' }}>
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
              style={{
                padding: '8px 12px',
                backgroundColor: page === 0 ? '#e0e0e0' : '#646cff',
                color: page === 0 ? '#999' : 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: page === 0 ? 'not-allowed' : 'pointer'
              }}
            >
              이전
            </button>
            <span style={{ padding: '8px 15px', backgroundColor: '#f5f5f5', borderRadius: '5px' }}>
              {page + 1} / {totalPages}
            </span>
            <button
              onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
              disabled={page >= totalPages - 1}
              style={{
                padding: '8px 12px',
                backgroundColor: page >= totalPages - 1 ? '#e0e0e0' : '#646cff',
                color: page >= totalPages - 1 ? '#999' : 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: page >= totalPages - 1 ? 'not-allowed' : 'pointer'
              }}
            >
              다음
            </button>
          </div>
        )}

        {/* 글쓰기 버튼 */}
        <div style={{ marginTop: '30px', textAlign: 'right' }}>
          <button
            onClick={handleWritePost}
            style={{
              padding: '12px 30px',
              backgroundColor: '#646cff',
              color: 'white',
              border: 'none',
              borderRadius: '5px',
              cursor: 'pointer',
              fontSize: '16px',
              fontWeight: 'bold',
              transition: 'all 0.3s'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.backgroundColor = '#5558e3';
              e.currentTarget.style.transform = 'translateY(-2px)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.backgroundColor = '#646cff';
              e.currentTarget.style.transform = 'translateY(0)';
            }}
          >
            ✏️ 글쓰기
          </button>
        </div>
      </div>
    </div>
  );
}

export default Community;
