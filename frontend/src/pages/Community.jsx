import { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';

/**
 * 커뮤니티 페이지
 * 게시글 목록 및 작성 기능
 */
function Community() {
  const [posts, setPosts] = useState([]);
  const [category, setCategory] = useState('전체글');
  const [loading, setLoading] = useState(false);

  const categories = ['전체글', '인기글', '축구', '야구', '농구', '롤', 'UFC', '자유게시판'];

  // 게시글 목록 조회
  const fetchPosts = async () => {
    setLoading(true);
    try {
      let url = 'http://localhost:8080/api/community/posts';

      if (category === '인기글') {
        url = 'http://localhost:8080/api/community/posts/popular';
      } else if (category !== '전체글') {
        url = `http://localhost:8080/api/community/posts/category/${category}`;
      }

      const response = await fetch(url);
      const data = await response.json();
      setPosts(data.content || []);
    } catch (error) {
      console.error('게시글 조회 실패:', error);
      setPosts([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts();
  }, [category]);

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
          marginBottom: '30px',
          borderBottom: '2px solid #e0e0e0',
          paddingBottom: '10px'
        }}>
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setCategory(cat)}
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
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = '#f9f9f9';
                  e.currentTarget.style.transform = 'translateY(0)';
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    {post.isNotice && (
                      <span style={{
                        backgroundColor: '#ff4444',
                        color: 'white',
                        padding: '2px 8px',
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
                        padding: '2px 8px',
                        borderRadius: '3px',
                        fontSize: '12px',
                        marginRight: '10px'
                      }}>
                        인기
                      </span>
                    )}
                    <span style={{ fontWeight: 'bold', fontSize: '18px' }}>
                      {post.title}
                    </span>
                    <span style={{ color: '#888', marginLeft: '10px', fontSize: '14px' }}>
                      [{post.commentCount}]
                    </span>
                  </div>
                  <div style={{ fontSize: '14px', color: '#666' }}>
                    {post.nickname} | 조회 {post.viewCount} | 추천 {post.likeCount}
                  </div>
                </div>
                <div style={{ marginTop: '10px', color: '#666', fontSize: '14px' }}>
                  {post.categoryName} | {new Date(post.createdAt).toLocaleDateString()}
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 글쓰기 버튼 */}
        <div style={{ marginTop: '30px', textAlign: 'right' }}>
          <button
            style={{
              padding: '12px 30px',
              backgroundColor: '#646cff',
              color: 'white',
              border: 'none',
              borderRadius: '5px',
              cursor: 'pointer',
              fontSize: '16px',
              fontWeight: 'bold'
            }}
            onClick={() => alert('글쓰기 기능은 로그인 후 사용 가능합니다.')}
          >
            ✏️ 글쓰기
          </button>
        </div>
      </div>
    </div>
  );
}

export default Community;
