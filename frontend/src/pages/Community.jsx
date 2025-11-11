import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import {
  getPosts,
  getPostsByCategory,
  getPopularPosts,
  getPopularPostsByCategory,
  createPost,
  deletePost,
  likePost,
  dislikePost
} from '../api/community';
import { getUserData, isLoggedIn } from '../api/api';
import '../styles/Community.css';

function Community() {
  const navigate = useNavigate();
  
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState('전체');
  const [activeTab, setActiveTab] = useState('all');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [showWriteModal, setShowWriteModal] = useState(false);
  const [newPost, setNewPost] = useState({ 
    title: '', 
    content: '',
    categoryName: '축구'
  });

  const categories = ['전체', '축구', '야구', '농구', '롤', 'UFC', '자유게시판'];
  const writableCategories = ['축구', '야구', '농구', '롤', 'UFC', '자유게시판'];

  const currentUser = getUserData();

  const fetchPosts = async (page = 0) => {
    setLoading(true);
    try {
      let response;

      if (activeTab === 'popular') {
        if (selectedCategory === '전체') {
          response = await getPopularPosts(page, 20);
        } else {
          response = await getPopularPostsByCategory(selectedCategory, page, 20);
        }
      } else {
        if (selectedCategory === '전체') {
          response = await getPosts(page, 20, searchKeyword);
        } else {
          response = await getPostsByCategory(selectedCategory, page, 20);
        }
      }

      setPosts(response.content || []);
      setTotalPages(response.totalPages || 0);
      setCurrentPage(page);
    } catch (error) {
      console.error('게시글 조회 오류:', error);
      alert('게시글을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts(0);
  }, [selectedCategory, activeTab]);

  const handleSearch = () => {
    if (activeTab === 'popular') {
      alert('인기글 탭에서는 검색이 지원되지 않습니다.');
      return;
    }
    fetchPosts(0);
  };

  const handleCreatePost = async () => {
  if (!isLoggedIn()) {
    alert('로그인이 필요합니다.');
    navigate('/login');
    return;
  }

  if (!newPost.title.trim() || !newPost.content.trim()) {
    alert('제목과 내용을 입력해주세요.');
    return;
  }

  if (!newPost.categoryName) {
    alert('카테고리를 선택해주세요.');
    return;
  }

  try {
    await createPost(newPost.categoryName, newPost.title, newPost.content);
    alert('게시글이 작성되었습니다.');
    setShowWriteModal(false);
    setNewPost({ title: '', content: '', categoryName: '축구' });
    setSelectedCategory(newPost.categoryName);
    setActiveTab('all');
  } catch (error) {
    console.error('게시글 작성 오류:', error);
    alert(error.message || '게시글 작성에 실패했습니다.');
  }
};

  const handleDeletePost = async (postId) => {
    if (!window.confirm('정말 삭제하시겠습니까?')) {
      return;
    }

    try {
      await deletePost(postId);
      alert('게시글이 삭제되었습니다.');
      fetchPosts(currentPage);
    } catch (error) {
      console.error('게시글 삭제 오류:', error);
      alert(error.message || '게시글 삭제에 실패했습니다.');
    }
  };

  const goToPostDetail = (postId) => {
    navigate(`/community/post/${postId}`);
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      fetchPosts(newPage);
    }
  };

  return (
    <div>
      <Navbar />
      <div className="community-container">
        
        {/* 헤더 */}
        <div className="community-header">
          <h1 className="community-title">💬 커뮤니티</h1>
          
          {isLoggedIn() && activeTab === 'all' && (
            <button className="write-button" onClick={() => setShowWriteModal(true)}>
              ✏️ 글쓰기
            </button>
          )}
        </div>

        {/* 탭 */}
        <div className="tabs-container">
          <button
            className={`tab-button ${activeTab === 'all' ? 'active' : ''}`}
            onClick={() => setActiveTab('all')}
          >
            전체글
          </button>
          <button
            className={`tab-button ${activeTab === 'popular' ? 'active' : ''}`}
            onClick={() => setActiveTab('popular')}
          >
            인기글
          </button>
        </div>

        {/* 카테고리 버튼 */}
        <div className="category-buttons">
          {categories.map((cat) => (
            <button
              key={cat}
              className={`category-button ${selectedCategory === cat ? 'active' : ''}`}
              onClick={() => setSelectedCategory(cat)}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* 검색창 */}
        {activeTab === 'all' && (
          <div className="search-container">
            <input
              type="text"
              className="search-input"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              placeholder="제목, 내용, 작성자로 검색"
            />
            <button className="search-button" onClick={handleSearch}>
              🔍 검색
            </button>
          </div>
        )}

        {/* 게시글 목록 */}
        {loading ? (
          <div className="loading-state">로딩 중...</div>
        ) : posts.length === 0 ? (
          <div className="empty-state">게시글이 없습니다.</div>
        ) : (
          <div>
            {posts.map((post) => (
              <div key={post.postId} className="post-card" onClick={() => goToPostDetail(post.postId)}>
                <div className="post-card-header">
                  <div className="post-card-content">
                    <span className="badge badge-category">{post.categoryName}</span>
                    {post.isNotice && <span className="badge badge-notice">공지</span>}
                    {post.isPopular && <span className="badge badge-popular">인기</span>}
                    
                    <span className="post-title">{post.title}</span>
                    
                    {post.commentCount > 0 && (
                      <span className="comment-count">[{post.commentCount}]</span>
                    )}
                  </div>

                  {currentUser && currentUser.username === post.username && (
                    <button
                      className="delete-button"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDeletePost(post.postId);
                      }}
                    >
                      삭제
                    </button>
                  )}
                </div>

                <div className="post-meta">
                  <span>{post.nickname}</span>
                  <span>|</span>
                  <span>조회 {post.viewCount}</span>
                  <span>|</span>
                  <span>👍 {post.likeCount}</span>
                  <span>|</span>
                  <span>{new Date(post.createdAt).toLocaleString('ko-KR')}</span>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 페이지네이션 */}
        {totalPages > 1 && (
          <div className="pagination">
            <button
              className="pagination-button"
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 0}
            >
              이전
            </button>
            
            <span className="pagination-info">
              {currentPage + 1} / {totalPages}
            </span>
            
            <button
              className="pagination-button"
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages - 1}
            >
              다음
            </button>
          </div>
        )}

        {/* 글쓰기 모달 */}
        {showWriteModal && (
          <div className="modal-overlay">
            <div className="modal-content">
              <h2 className="modal-title">✏️ 글쓰기</h2>
              
              <select
                className="form-select"
                value={newPost.categoryName}
                onChange={(e) => setNewPost({...newPost, categoryName: e.target.value})}
              >
                {writableCategories.map(cat => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>

              <input
                type="text"
                className="form-input"
                placeholder="제목"
                value={newPost.title}
                onChange={(e) => setNewPost({...newPost, title: e.target.value})}
              />
              
              <textarea
                className="form-textarea"
                placeholder="내용"
                value={newPost.content}
                onChange={(e) => setNewPost({...newPost, content: e.target.value})}
              />
              
              <div className="modal-actions">
                <button className="cancel-button" onClick={() => setShowWriteModal(false)}>
                  취소
                </button>
                <button className="submit-button" onClick={handleCreatePost}>
                  작성
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Community;