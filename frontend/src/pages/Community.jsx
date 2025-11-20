import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  getPosts,
  getPostsByCategory,
  getPopularPosts,
  getPopularPostsByCategory,
  createPost,
  deletePost
} from '../api/community';
import { getUserData, isLoggedIn } from '../api/api';

function Community() {
  const navigate = useNavigate();

  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState('전체');
  const [activeTab, setActiveTab] = useState('all');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchType, setSearchType] = useState('all');
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
          response = await getPosts(page, 20, searchKeyword, searchType);
        } else {
          if (searchKeyword) {
            response = await getPosts(page, 20, searchKeyword, searchType);
          } else {
            response = await getPostsByCategory(selectedCategory, page, 20);
          }
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
    setSearchKeyword('');
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
      fetchPosts(0);
    } catch (error) {
      console.error('게시글 작성 오류:', error);
      if (error.response && error.response.data) {
        const errors = error.response.data;
        if (typeof errors === 'object' && !errors.message) {
          const errorMessages = Object.values(errors).join('\n');
          alert(errorMessages);
        } else {
          alert(errors.message || '게시글 작성에 실패했습니다.');
        }
      } else {
        alert(error.message || '게시글 작성에 실패했습니다.');
      }
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
    navigate(`/board/${postId}`);
  };

  const handlePageChange = (newPage) => {
    if (newPage < 0) {
      newPage = 0;
    }
    if (newPage >= totalPages) {
      newPage = totalPages - 1;
    }
    fetchPosts(newPage);
  };

  return (
    <div className="bg-gray-900 text-white min-h-screen">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 헤더 */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-4xl font-bold">💬 커뮤니티</h1>
            <p className="text-gray-400 mt-2">스포츠에 대한 이야기를 자유롭게 나눠보세요</p>
          </div>

          {isLoggedIn() && activeTab === 'all' && (
            <button
              onClick={() => setShowWriteModal(true)}
              className="px-6 py-3 bg-blue-500 hover:bg-blue-600 text-white font-bold rounded-lg transition shadow-lg"
            >
              ✏️ 글쓰기
            </button>
          )}
        </div>

        {/* 탭 */}
        <div className="flex gap-4 mb-6">
          <button
            className={`px-6 py-3 rounded-lg font-semibold transition ${
              activeTab === 'all'
                ? 'bg-blue-500 text-white shadow-lg'
                : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
            }`}
            onClick={() => setActiveTab('all')}
          >
            전체글
          </button>
          <button
            className={`px-6 py-3 rounded-lg font-semibold transition ${
              activeTab === 'popular'
                ? 'bg-blue-500 text-white shadow-lg'
                : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
            }`}
            onClick={() => setActiveTab('popular')}
          >
            🔥 인기글
          </button>
        </div>

        {/* 카테고리 버튼 */}
        <div className="flex flex-wrap gap-3 mb-6">
          {categories.map((cat) => (
            <button
              key={cat}
              className={`px-5 py-2 rounded-lg font-semibold transition ${
                selectedCategory === cat
                  ? 'bg-blue-500 text-white shadow-lg transform scale-105'
                  : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
              }`}
              onClick={() => setSelectedCategory(cat)}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* 검색창 */}
        {activeTab === 'all' && (
          <div className="flex gap-3 mb-8">
            <select
              value={searchType}
              onChange={(e) => setSearchType(e.target.value)}
              className="px-4 py-3 bg-gray-700 text-white rounded-lg border-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="all">제목+내용</option>
              <option value="title">제목</option>
              <option value="content">내용</option>
              <option value="author">작성자</option>
            </select>
            <input
              type="text"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              placeholder="검색어를 입력하세요"
              className="flex-1 px-4 py-3 bg-gray-700 text-white rounded-lg border-none focus:ring-2 focus:ring-blue-500"
            />
            <button
              onClick={handleSearch}
              className="px-6 py-3 bg-blue-500 hover:bg-blue-600 text-white font-bold rounded-lg transition"
            >
              🔍 검색
            </button>
          </div>
        )}

        {/* 게시글 목록 */}
        {loading ? (
          <div className="text-center py-16">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
            <p className="mt-4 text-gray-400">로딩 중...</p>
          </div>
        ) : posts.length === 0 ? (
          <div className="bg-gray-800/50 rounded-lg p-16 text-center">
            <p className="text-gray-400 text-lg">게시글이 없습니다.</p>
          </div>
        ) : (
          <>
            <div className="space-y-3 mb-8">
              {posts.map((post) => (
                <div
                  key={post.postId}
                  onClick={() => goToPostDetail(post.postId)}
                  className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-5 cursor-pointer hover:bg-gray-700/80 transition"
                >
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <span className="px-3 py-1 bg-blue-500 text-white text-xs font-bold rounded-full">
                        {post.categoryName}
                      </span>
                      {post.isNotice && (
                        <span className="px-3 py-1 bg-red-500 text-white text-xs font-bold rounded-full">공지</span>
                      )}
                      {post.isPopular && (
                        <span className="px-3 py-1 bg-yellow-500 text-black text-xs font-bold rounded-full">인기</span>
                      )}
                      <span className="font-bold text-lg">{post.title}</span>
                      {post.commentCount > 0 && (
                        <span className="text-blue-400 font-semibold">[{post.commentCount}]</span>
                      )}
                    </div>

                    {currentUser && currentUser.username === post.username && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDeletePost(post.postId);
                        }}
                        className="px-4 py-1 bg-red-500 hover:bg-red-600 text-white text-sm font-semibold rounded transition"
                      >
                        삭제
                      </button>
                    )}
                  </div>

                  <div className="flex items-center gap-3 text-sm text-gray-400">
                    <span>{post.nickname}</span>
                    <span>•</span>
                    <span>조회 {post.viewCount}</span>
                    <span>•</span>
                    <span>👍 {post.likeCount}</span>
                    <span>•</span>
                    <span>{new Date(post.createdAt).toLocaleString('ko-KR')}</span>
                  </div>
                </div>
              ))}
            </div>

            {/* 페이지네이션 */}
            <div className="flex justify-center items-center gap-2">
              <button
                onClick={() => handlePageChange(currentPage - 10)}
                disabled={currentPage < 10}
                className={`px-4 py-2 rounded-lg font-semibold transition ${
                  currentPage < 10
                    ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                    : 'bg-gray-700 text-white hover:bg-gray-600'
                }`}
              >
                &lt;&lt;
              </button>

              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className={`px-4 py-2 rounded-lg font-semibold transition ${
                  currentPage === 0
                    ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                    : 'bg-gray-700 text-white hover:bg-gray-600'
                }`}
              >
                이전
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
                        className={`px-4 py-2 rounded-lg font-semibold transition ${
                          currentPage === i
                            ? 'bg-blue-500 text-white shadow-lg'
                            : 'bg-gray-700 text-white hover:bg-gray-600'
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
                disabled={currentPage >= totalPages - 1 || totalPages === 0}
                className={`px-4 py-2 rounded-lg font-semibold transition ${
                  currentPage >= totalPages - 1 || totalPages === 0
                    ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                    : 'bg-gray-700 text-white hover:bg-gray-600'
                }`}
              >
                다음
              </button>

              <button
                onClick={() => handlePageChange(currentPage + 10)}
                disabled={currentPage >= totalPages - 10 || totalPages === 0}
                className={`px-4 py-2 rounded-lg font-semibold transition ${
                  currentPage >= totalPages - 10 || totalPages === 0
                    ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                    : 'bg-gray-700 text-white hover:bg-gray-600'
                }`}
              >
                &gt;&gt;
              </button>
            </div>
          </>
        )}

        {/* 글쓰기 모달 */}
        {showWriteModal && (
          <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
            <div className="bg-gray-800 rounded-lg p-8 max-w-2xl w-full mx-4">
              <h2 className="text-2xl font-bold mb-6">✏️ 글쓰기</h2>

              <select
                value={newPost.categoryName}
                onChange={(e) => setNewPost({...newPost, categoryName: e.target.value})}
                className="w-full px-4 py-3 bg-gray-700 text-white rounded-lg border-none focus:ring-2 focus:ring-blue-500 mb-4"
              >
                {writableCategories.map(cat => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>

              <input
                type="text"
                placeholder="제목"
                value={newPost.title}
                onChange={(e) => setNewPost({...newPost, title: e.target.value})}
                className="w-full px-4 py-3 bg-gray-700 text-white rounded-lg border-none focus:ring-2 focus:ring-blue-500 mb-4"
              />

              <textarea
                placeholder="내용"
                value={newPost.content}
                onChange={(e) => setNewPost({...newPost, content: e.target.value})}
                className="w-full px-4 py-3 bg-gray-700 text-white rounded-lg border-none focus:ring-2 focus:ring-blue-500 mb-4 h-64 resize-none"
              />

              <div className="flex gap-3">
                <button
                  onClick={() => setShowWriteModal(false)}
                  className="flex-1 px-6 py-3 bg-gray-700 hover:bg-gray-600 text-white font-bold rounded-lg transition"
                >
                  취소
                </button>
                <button
                  onClick={handleCreatePost}
                  className="flex-1 px-6 py-3 bg-blue-500 hover:bg-blue-600 text-white font-bold rounded-lg transition"
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
