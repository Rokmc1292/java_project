/**
 * 커뮤니티 페이지 (완전판)
 * - 게시글 목록 (전체글/인기글 탭)
 * - 카테고리별 필터링
 * - 검색 기능
 * - 게시글 작성/수정/삭제
 * - 페이지네이션
 * 
 * 파일 위치: frontend/src/pages/Community.jsx
 */

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

function Community() {
  const navigate = useNavigate();
  
  // 상태 관리
  const [posts, setPosts] = useState([]); // 게시글 목록
  const [loading, setLoading] = useState(false); // 로딩 상태
  const [selectedCategory, setSelectedCategory] = useState('전체'); // 선택된 카테고리
  const [activeTab, setActiveTab] = useState('all'); // 전체글/인기글 탭
  const [searchKeyword, setSearchKeyword] = useState(''); // 검색어
  const [currentPage, setCurrentPage] = useState(0); // 현재 페이지
  const [totalPages, setTotalPages] = useState(0); // 전체 페이지 수
  const [showWriteModal, setShowWriteModal] = useState(false); // 글쓰기 모달
  const [newPost, setNewPost] = useState({ 
    title: '', 
    content: '',
    categoryName: '축구' // 기본 카테고리
  }); // 새 게시글

  // 카테고리 목록
  const categories = ['전체', '축구', '야구', '농구', '롤', 'UFC', '자유게시판'];
  
  // 글쓰기용 카테고리 (전체 제외)
  const writableCategories = ['축구', '야구', '농구', '롤', 'UFC', '자유게시판'];

  // 현재 로그인한 사용자
  const currentUser = getUserData();

  /**
   * 게시글 목록 조회
   */
  const fetchPosts = async (page = 0) => {
    setLoading(true);
    try {
      let response;

      // 탭과 카테고리에 따라 다른 API 호출
      if (activeTab === 'popular') {
        // 인기글 탭
        if (selectedCategory === '전체') {
          response = await getPopularPosts(page, 20);
        } else {
          response = await getPopularPostsByCategory(selectedCategory, page, 20);
        }
      } else {
        // 전체글 탭
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

  // 초기 로드 및 카테고리/탭 변경 시 게시글 조회
  useEffect(() => {
    fetchPosts(0);
  }, [selectedCategory, activeTab]);

  /**
   * 검색 실행
   */
  const handleSearch = () => {
    if (activeTab === 'popular') {
      alert('인기글 탭에서는 검색이 지원되지 않습니다.');
      return;
    }
    fetchPosts(0);
  };

  /**
   * 게시글 작성
   */
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

    if (selectedCategory === '전체') {
      alert('카테고리를 선택해주세요.');
      return;
    }

    try {
      await createPost(selectedCategory, newPost.title, newPost.content);
      alert('게시글이 작성되었습니다.');
      setShowWriteModal(false);
      setNewPost({ title: '', content: '' });
      fetchPosts(0); // 목록 새로고침
    } catch (error) {
      console.error('게시글 작성 오류:', error);
      alert(error.message || '게시글 작성에 실패했습니다.');
    }
  };

  /**
   * 게시글 삭제
   */
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

  /**
   * 게시글 추천
   */
  const handleLikePost = async (postId) => {
    if (!isLoggedIn()) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    try {
      await likePost(postId);
      alert('추천했습니다.');
      fetchPosts(currentPage);
    } catch (error) {
      console.error('추천 오류:', error);
      alert(error.message || '추천에 실패했습니다.');
    }
  };

  /**
   * 게시글 상세 페이지로 이동
   */
  const goToPostDetail = (postId) => {
    navigate(`/community/post/${postId}`);
  };

  /**
   * 페이지 변경
   */
  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      fetchPosts(newPage);
    }
  };

  return (
    <div>
      <Navbar />
      <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '20px' }}>
        
        {/* 헤더 */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h1 style={{ fontSize: '28px', fontWeight: 'bold' }}>
            💬 커뮤니티
          </h1>
          
          {/* 글쓰기 버튼 */}
          {isLoggedIn() && (
            <button
              onClick={() => setShowWriteModal(true)}
              style={{
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
              ✏️ 글쓰기
            </button>
          )}
        </div>

        {/* 탭 (전체글/인기글) */}
        <div style={{ display: 'flex', gap: '10px', marginBottom: '20px', borderBottom: '2px solid #e0e0e0' }}>
          <button
            onClick={() => setActiveTab('all')}
            style={{
              padding: '10px 20px',
              backgroundColor: 'transparent',
              color: activeTab === 'all' ? '#646cff' : '#888',
              border: 'none',
              borderBottom: activeTab === 'all' ? '3px solid #646cff' : 'none',
              cursor: 'pointer',
              fontSize: '16px',
              fontWeight: activeTab === 'all' ? 'bold' : 'normal'
            }}
          >
            전체글
          </button>
          <button
            onClick={() => setActiveTab('popular')}
            style={{
              padding: '10px 20px',
              backgroundColor: 'transparent',
              color: activeTab === 'popular' ? '#646cff' : '#888',
              border: 'none',
              borderBottom: activeTab === 'popular' ? '3px solid #646cff' : 'none',
              cursor: 'pointer',
              fontSize: '16px',
              fontWeight: activeTab === 'popular' ? 'bold' : 'normal'
            }}
          >
            인기글
          </button>
        </div>

        {/* 카테고리 버튼 */}
        <div style={{ display: 'flex', gap: '10px', marginBottom: '20px', flexWrap: 'wrap' }}>
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              style={{
                padding: '8px 16px',
                backgroundColor: selectedCategory === cat ? '#646cff' : '#f0f0f0',
                color: selectedCategory === cat ? 'white' : '#333',
                border: 'none',
                borderRadius: '20px',
                cursor: 'pointer',
                fontSize: '14px',
                fontWeight: selectedCategory === cat ? 'bold' : 'normal',
                transition: 'all 0.3s'
              }}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* 검색창 (전체글 탭에서만) */}
        {activeTab === 'all' && (
          <div style={{ marginBottom: '20px', display: 'flex', gap: '10px' }}>
            <input
              type="text"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              placeholder="제목, 내용, 작성자로 검색"
              style={{
                flex: 1,
                padding: '10px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px'
              }}
            />
            <button
              onClick={handleSearch}
              style={{
                padding: '10px 20px',
                backgroundColor: '#646cff',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '14px'
              }}
            >
              🔍 검색
            </button>
          </div>
        )}

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
                onClick={() => goToPostDetail(post.postId)}
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
                  <div style={{ flex: 1 }}>
                    {/* 배지 (공지, 인기글) */}
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
                    
                    {/* 제목 */}
                    <span style={{ fontWeight: 'bold', fontSize: '18px' }}>
                      {post.title}
                    </span>
                    
                    {/* 댓글 수 */}
                    {post.commentCount > 0 && (
                      <span style={{ color: '#646cff', marginLeft: '10px', fontSize: '14px' }}>
                        [{post.commentCount}]
                      </span>
                    )}
                  </div>

                  {/* 삭제 버튼 (본인 글만) */}
                  {currentUser && currentUser.username === post.username && (
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDeletePost(post.postId);
                      }}
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

                {/* 작성자, 조회수, 추천수 */}
                <div style={{ marginTop: '10px', fontSize: '14px', color: '#666' }}>
                  <span>{post.nickname}</span>
                  <span style={{ margin: '0 10px' }}>|</span>
                  <span>조회 {post.viewCount}</span>
                  <span style={{ margin: '0 10px' }}>|</span>
                  <span>👍 {post.likeCount}</span>
                  <span style={{ margin: '0 10px' }}>|</span>
                  <span>{new Date(post.createdAt).toLocaleString('ko-KR')}</span>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 페이지네이션 */}
        {totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'center', gap: '10px', marginTop: '30px' }}>
            <button
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 0}
              style={{
                padding: '8px 16px',
                backgroundColor: currentPage === 0 ? '#ddd' : '#646cff',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: currentPage === 0 ? 'not-allowed' : 'pointer'
              }}
            >
              이전
            </button>
            
            <span style={{ padding: '8px 16px', fontSize: '16px' }}>
              {currentPage + 1} / {totalPages}
            </span>
            
            <button
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages - 1}
              style={{
                padding: '8px 16px',
                backgroundColor: currentPage === totalPages - 1 ? '#ddd' : '#646cff',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: currentPage === totalPages - 1 ? 'not-allowed' : 'pointer'
              }}
            >
              다음
            </button>
          </div>
        )}

        {/* 글쓰기 모달 */}
        {showWriteModal && (
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
              maxWidth: '600px'
            }}>
              <h2 style={{ marginBottom: '20px' }}>✏️ 글쓰기</h2>
              
              <input
                type="text"
                placeholder="제목"
                value={newPost.title}
                onChange={(e) => setNewPost({...newPost, title: e.target.value})}
                style={{
                  width: '100%',
                  padding: '10px',
                  marginBottom: '15px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '16px',
                  boxSizing: 'border-box'
                }}
              />
              
              <textarea
                placeholder="내용"
                value={newPost.content}
                onChange={(e) => setNewPost({...newPost, content: e.target.value})}
                style={{
                  width: '100%',
                  padding: '10px',
                  marginBottom: '15px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '16px',
                  minHeight: '200px',
                  resize: 'vertical',
                  boxSizing: 'border-box'
                }}
              />
              
              <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                <button
                  onClick={() => setShowWriteModal(false)}
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
                  onClick={handleCreatePost}
                  style={{
                    padding: '10px 20px',
                    backgroundColor: '#646cff',
                    color: 'white',
                    border: 'none',
                    borderRadius: '5px',
                    cursor: 'pointer'
                  }}
                >
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