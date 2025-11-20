import { useState, useEffect } from 'react';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function News() {
  const [news, setNews] = useState([]);
  const [selectedSport, setSelectedSport] = useState('ALL');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [loading, setLoading] = useState(false);
  const [showLikedOnly, setShowLikedOnly] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [likeAnimations, setLikeAnimations] = useState({});
  const [showScrollTop, setShowScrollTop] = useState(false);
  const [toast, setToast] = useState({ show: false, message: '', type: '' });
  const [popularNews, setPopularNews] = useState([]);
  
  // 로그인한 사용자 정보 가져오기
  const [currentUser, setCurrentUser] = useState(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    // localStorage에서 로그인 정보 확인
    const user = JSON.parse(localStorage.getItem('user') || 'null');
    if (user && user.userId) {
      setCurrentUser(user);
      setIsLoggedIn(true);
    }
  }, []);

  const userId = currentUser?.userId || null;

  const sports = [
    { value: 'ALL', label: '🏆 전체', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
    { value: 'FOOTBALL', label: '⚽ 축구', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
    { value: 'BASKETBALL', label: '🏀 농구', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
    { value: 'BASEBALL', label: '⚾ 야구', gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' },
    { value: 'LOL', label: '🎮 e스포츠', gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' },
    { value: 'MMA', label: '🥊 UFC', gradient: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)' }
  ];

  // 토스트 메시지 표시
  const showToast = (message, type = 'success') => {
    setToast({ show: true, message, type });
    setTimeout(() => setToast({ show: false, message: '', type: '' }), 3000);
  };

  // 스크롤 이벤트
  useEffect(() => {
    const handleScroll = () => {
      setShowScrollTop(window.pageYOffset > 300);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // 맨 위로 스크롤
  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // 뉴스 조회
  const fetchNews = async () => {
    setLoading(true);
    try {
      let url = `${API_BASE_URL}/api/news?page=${currentPage}&size=10${userId ? `&userId=${userId}` : ''}`;
      
      if (showLikedOnly) {
        if (!userId) {
          setNews([]);
          setLoading(false);
          return;
        }
        url = `${API_BASE_URL}/api/news/liked?userId=${userId}&page=${currentPage}&size=10`;
      }
      else if (searchKeyword.trim()) {
        if (selectedSport === 'ALL') {
          url = `${API_BASE_URL}/api/news/search?keyword=${encodeURIComponent(searchKeyword)}&page=${currentPage}&size=10${userId ? `&userId=${userId}` : ''}`;
        } else {
          url = `${API_BASE_URL}/api/news/search/${selectedSport}?keyword=${encodeURIComponent(searchKeyword)}&page=${currentPage}&size=10${userId ? `&userId=${userId}` : ''}`;
        }
      } else {
        if (selectedSport !== 'ALL') {
          url = `${API_BASE_URL}/api/news/sport/${selectedSport}?page=${currentPage}&size=10${userId ? `&userId=${userId}` : ''}`;
        }
      }

      const response = await fetch(url);
      const data = await response.json();
      setNews(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (error) {
      console.error('뉴스 조회 실패:', error);
      setNews([]);
      showToast('뉴스를 불러오는데 실패했습니다.', 'error');
    } finally {
      setLoading(false);
    }
  };

  // 조회수 증가
  const increaseViewCount = async (newsId) => {
    try {
      await fetch(`${API_BASE_URL}/api/news/${newsId}/view`, {
        method: 'PUT',
      });
    } catch (error) {
      console.error('조회수 증가 실패:', error);
    }
  };

  // 인기 뉴스 조회
  const fetchPopularNews = async () => {
    try {
      const url = `${API_BASE_URL}/api/news/popular${userId ? `?userId=${userId}` : ''}`;
      const response = await fetch(url);
      const data = await response.json();
      setPopularNews(data || []);
    } catch (error) {
      console.error('인기 뉴스 조회 실패:', error);
    }
  };

  // 좋아요 토글
  const toggleLike = async (newsId, e) => {
    e.preventDefault();
    e.stopPropagation();
    
    // 로그인 체크
    if (!isLoggedIn || !userId) {
      showToast('로그인이 필요합니다!', 'error');
      setTimeout(() => {
        window.location.href = '/login';
      }, 1500);
      return;
    }
    
    setLikeAnimations(prev => ({ ...prev, [newsId]: true }));
    setTimeout(() => {
      setLikeAnimations(prev => ({ ...prev, [newsId]: false }));
    }, 600);
    
    try {
      const response = await fetch(`${API_BASE_URL}/api/news/${newsId}/like`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ userId }),
      });
      
      const result = await response.json();
      
      if (result.success) {
        setNews(prevNews => 
          prevNews.map(item => 
            item.newsId === newsId
              ? {
                  ...item,
                  isLiked: result.isLiked,
                  likeCount: result.isLiked ? item.likeCount + 1 : item.likeCount - 1
                }
              : item
          )
        );
        showToast(result.isLiked ? '❤️ 좋아요!' : '좋아요 취소');
      }
    } catch (error) {
      console.error('좋아요 실패:', error);
      showToast('좋아요에 실패했습니다.', 'error');
    }
  };

  useEffect(() => {
    setCurrentPage(0);
  }, [selectedSport, searchKeyword, showLikedOnly]);

  useEffect(() => {
    fetchNews();
  }, [currentPage, selectedSport, searchKeyword, showLikedOnly]);
  
  useEffect(() => {
    fetchPopularNews();
  }, [userId]);
  
  // 로딩 스켈레톤
  const LoadingSkeleton = () => (
    <div style={{ display: 'grid', gap: '20px' }}>
      {[1, 2, 3, 4, 5].map((i) => (
        <div
          key={i}
          style={{
            display: 'flex',
            gap: '20px',
            padding: '20px',
            backgroundColor: 'white',
            borderRadius: '16px',
            boxShadow: '0 4px 6px rgba(0,0,0,0.05)',
            animation: 'pulse 1.5s ease-in-out infinite'
          }}
        >
          <div style={{
            width: '200px',
            height: '140px',
            backgroundColor: '#e0e0e0',
            borderRadius: '12px'
          }} />
          <div style={{ flex: 1 }}>
            <div style={{
              width: '80%',
              height: '20px',
              backgroundColor: '#e0e0e0',
              borderRadius: '8px',
              marginBottom: '10px'
            }} />
            <div style={{
              width: '60%',
              height: '16px',
              backgroundColor: '#e0e0e0',
              borderRadius: '8px',
              marginBottom: '8px'
            }} />
            <div style={{
              width: '90%',
              height: '14px',
              backgroundColor: '#e0e0e0',
              borderRadius: '8px'
            }} />
          </div>
        </div>
      ))}
      <style>{`
        @keyframes pulse {
          0%, 100% { opacity: 1; }
          50% { opacity: 0.5; }
        }
      `}</style>
    </div>
  );

  return (
    <div style={{ 
      minHeight: '100vh',
      background: '#f8f9fa',
      paddingBottom: '40px'
    }}>
      
      {/* 헤더 섹션 */}
      <div style={{
        maxWidth: '1200px',
        margin: '0 auto',
        padding: '40px 20px 20px'
      }}>
        <div style={{
          textAlign: 'center',
          marginBottom: '40px'
        }}>
          <h1 style={{
            fontSize: '48px',
            fontWeight: '800',
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            marginBottom: '10px'
          }}>
            📰 스포츠 뉴스
          </h1>
          <p style={{
            color: '#666',
            fontSize: '18px',
            fontWeight: '500'
          }}>
            최신 스포츠 소식을 한눈에
          </p>
        </div>

        {/* 검색창 */}
        <div style={{
          marginBottom: '30px',
          borderRadius: '16px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
        }}>
          <input
            type="text"
            placeholder="🔍 뉴스 검색... (예: 손흥민, 토트넘)"
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            style={{
              width: '100%',
              padding: '16px 24px',
              fontSize: '16px',
              border: '2px solid #e0e0e0',
              borderRadius: '12px',
              outline: 'none',
              backgroundColor: 'white',
              transition: 'all 0.3s'
            }}
            onFocus={(e) => {
              e.target.style.borderColor = '#667eea';
              e.target.style.boxShadow = '0 4px 20px rgba(102,126,234,0.2)';
            }}
            onBlur={(e) => {
              e.target.style.borderColor = '#e0e0e0';
              e.target.style.boxShadow = 'none';
            }}
          />
        </div>

        {/* 종목 필터 */}
        <div style={{
          display: 'flex',
          gap: '12px',
          marginBottom: '30px',
          flexWrap: 'wrap',
          alignItems: 'center'
        }}>
          {sports.map((sport) => (
            <button
              key={sport.value}
              onClick={() => {
                setSelectedSport(sport.value);
                setShowLikedOnly(false);
              }}
              style={{
                padding: '12px 24px',
                background: selectedSport === sport.value && !showLikedOnly 
                  ? sport.gradient 
                  : 'white',
                color: selectedSport === sport.value && !showLikedOnly ? 'white' : '#333',
                border: selectedSport === sport.value && !showLikedOnly 
                  ? 'none' 
                  : '2px solid #e0e0e0',
                borderRadius: '12px',
                cursor: 'pointer',
                fontWeight: '700',
                fontSize: '15px',
                transition: 'all 0.3s',
                boxShadow: selectedSport === sport.value && !showLikedOnly 
                  ? '0 8px 20px rgba(0,0,0,0.2)' 
                  : 'none',
                transform: selectedSport === sport.value && !showLikedOnly 
                  ? 'translateY(-2px)' 
                  : 'none'
              }}
              onMouseEnter={(e) => {
                if (!(selectedSport === sport.value && !showLikedOnly)) {
                  e.currentTarget.style.background = '#f5f5f5';
                  e.currentTarget.style.transform = 'translateY(-2px)';
                }
              }}
              onMouseLeave={(e) => {
                if (!(selectedSport === sport.value && !showLikedOnly)) {
                  e.currentTarget.style.background = 'white';
                  e.currentTarget.style.transform = 'none';
                }
              }}
            >
              {sport.label}
            </button>
          ))}
          
          <button
            onClick={() => {
              if (!isLoggedIn) {
                showToast('로그인이 필요합니다!', 'error');
                setTimeout(() => {
                  window.location.href = '/login';
                }, 1500);
                return;
              }
              setShowLikedOnly(!showLikedOnly);
            }}
            disabled={!isLoggedIn}
            style={{
              padding: '12px 24px',
              background: showLikedOnly 
                ? 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' 
                : 'white',
              color: showLikedOnly ? 'white' : '#333',
              border: showLikedOnly ? 'none' : '2px solid #e0e0e0',
              borderRadius: '12px',
              cursor: !isLoggedIn ? 'not-allowed' : 'pointer',
              fontWeight: '700',
              fontSize: '15px',
              transition: 'all 0.3s',
              marginLeft: 'auto',
              boxShadow: showLikedOnly ? '0 8px 20px rgba(0,0,0,0.2)' : 'none',
              opacity: !isLoggedIn ? 0.5 : 1
            }}
            onMouseEnter={(e) => {
              if (isLoggedIn && !showLikedOnly) {
                e.currentTarget.style.background = '#f5f5f5';
                e.currentTarget.style.transform = 'translateY(-2px)';
              }
            }}
            onMouseLeave={(e) => {
              if (isLoggedIn && !showLikedOnly) {
                e.currentTarget.style.background = 'white';
                e.currentTarget.style.transform = 'none';
              }
            }}
            title={!isLoggedIn ? '로그인 후 이용 가능합니다' : ''}
          >
            {showLikedOnly ? '❤️ 전체 보기' : '🤍 좋아요한 뉴스'}
          </button>
        </div>
      </div>

      {/* 뉴스 목록 + 인기 뉴스 사이드바 */}
      <div style={{ 
        maxWidth: '1200px', 
        margin: '0 auto', 
        padding: '0 20px',
        display: 'flex',
        gap: '20px',
        alignItems: 'flex-start'
      }}>
        {/* 메인 뉴스 목록 */}
        <div style={{ flex: 1 }}>
          {searchKeyword && (
            <div style={{
              marginBottom: '20px',
              padding: '12px 20px',
              background: 'white',
              borderRadius: '12px',
              color: '#333',
              fontSize: '14px',
              fontWeight: '600',
              border: '2px solid #e0e0e0',
              boxShadow: '0 2px 8px rgba(0,0,0,0.05)'
            }}>
              "<strong style={{ fontSize: '16px' }}>{searchKeyword}</strong>" 검색 결과: {news.length}개
            </div>
          )}

          {loading ? (
            <LoadingSkeleton />
          ) : news.length === 0 ? (
            <div style={{
              textAlign: 'center',
              padding: '100px 20px',
              background: 'white',
              borderRadius: '20px',
              border: '2px solid #e0e0e0',
              boxShadow: '0 4px 12px rgba(0,0,0,0.05)'
            }}>
              <div style={{ fontSize: '64px', marginBottom: '20px' }}>📭</div>
              <p style={{ color: '#666', fontSize: '18px', fontWeight: '600' }}>
                {showLikedOnly ? '좋아요한 뉴스가 없습니다.' : searchKeyword ? '검색 결과가 없습니다.' : '뉴스가 없습니다.'}
              </p>
            </div>
          ) : (
            <>
              <div style={{ display: 'grid', gap: '24px' }}>
                {news.map((item, index) => (
                  < a
                    key={item.newsId}
                    href={item.sourceUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    onClick={() => increaseViewCount(item.newsId)}
                    style={{
                      display: 'flex',
                      gap: '24px',
                      padding: '24px',
                      background: 'white',
                      borderRadius: '20px',
                      textDecoration: 'none',
                      color: 'inherit',
                      transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
                      cursor: 'pointer',
                      position: 'relative',
                      boxShadow: '0 10px 40px rgba(0,0,0,0.1)',
                      animation: `fadeInUp 0.6s ease-out ${index * 0.1}s both`,
                      overflow: 'hidden'
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.transform = 'translateY(-8px) scale(1.01)';
                      e.currentTarget.style.boxShadow = '0 20px 60px rgba(0,0,0,0.15)';
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.transform = 'translateY(0) scale(1)';
                      e.currentTarget.style.boxShadow = '0 10px 40px rgba(0,0,0,0.1)';
                    }}
                  >
                    {/* 그라디언트 배경 */}
                    <div style={{
                      position: 'absolute',
                      top: 0,
                      left: 0,
                      right: 0,
                      height: '4px',
                      background: 'linear-gradient(90deg, #667eea 0%, #764ba2 50%, #f093fb 100%)',
                    }} />

                    {item.thumbnailUrl && (
                      <div style={{
                        width: '220px',
                        height: '160px',
                        backgroundColor: '#f0f0f0',
                        borderRadius: '16px',
                        overflow: 'hidden',
                        flexShrink: 0,
                        boxShadow: '0 4px 12px rgba(0,0,0,0.08)'
                      }}>
                        <img
                          src={item.thumbnailUrl}
                          alt={item.title}
                          loading="lazy"
                          style={{
                            width: '100%',
                            height: '100%',
                            objectFit: 'cover',
                            transition: 'transform 0.4s'
                          }}
                          onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.1)'}
                          onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
                        />
                      </div>
                    )}

                    <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                      <div style={{
                        fontSize: '13px',
                        color: '#8e8e8e',
                        marginBottom: '12px',
                        fontWeight: '600',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px'
                      }}>
                        <span style={{
                          padding: '4px 12px',
                          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                          color: 'white',
                          borderRadius: '6px',
                          fontSize: '12px'
                        }}>
                          {item.sportDisplayName}
                        </span>
                        <span>{item.sourceName}</span>
                        <span>•</span>
                        <span>{new Date(item.publishedAt).toLocaleDateString()}</span>
                      </div>

                      <h3 style={{
                        fontSize: '22px',
                        fontWeight: '800',
                        marginBottom: '12px',
                        lineHeight: 1.4,
                        color: '#1a1a1a'
                      }}>
                        {item.title}
                      </h3>

                      {item.content && (
                        <p style={{
                          color: '#666',
                          fontSize: '15px',
                          lineHeight: 1.7,
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          display: '-webkit-box',
                          WebkitLineClamp: 2,
                          WebkitBoxOrient: 'vertical',
                          marginBottom: '16px',
                          flex: 1
                        }}>
                          {item.content}
                        </p>
                      )}

                      <div style={{
                        fontSize: '15px',
                        color: '#8e8e8e',
                        display: 'flex',
                        gap: '24px',
                        alignItems: 'center',
                        fontWeight: '600'
                      }}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <span style={{ fontSize: '18px' }}>👁️</span>
                          {item.viewCount.toLocaleString()}
                        </span>
                        <span style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '6px',
                          color: item.isLiked ? '#ed4956' : '#8e8e8e'
                        }}>
                          <span style={{ fontSize: '18px' }}>{item.isLiked ? '❤️' : '🤍'}</span>
                          {(item.likeCount || 0).toLocaleString()}
                        </span>
                      </div>
                    </div>

                    {/* 좋아요 버튼 */}
                    <button
                      onClick={(e) => toggleLike(item.newsId, e)}
                      disabled={!isLoggedIn}
                      style={{
                        position: 'absolute',
                        top: '16px',
                        right: '16px',
                        width: '42px',
                        height: '42px',
                        borderRadius: '50%',
                        border: 'none',
                        background: !isLoggedIn
                          ? '#e0e0e0'
                          : item.isLiked 
                            ? 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' 
                            : 'white',
                        cursor: !isLoggedIn ? 'not-allowed' : 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                        boxShadow: !isLoggedIn
                          ? 'none'
                          : item.isLiked 
                            ? '0 8px 20px rgba(245,87,108,0.4)' 
                            : '0 4px 12px rgba(0,0,0,0.1)',
                        zIndex: 10,
                        transform: likeAnimations[item.newsId] ? 'scale(1.3)' : 'scale(1)',
                        opacity: !isLoggedIn ? 0.5 : 1
                      }}
                      onMouseEnter={(e) => {
                        if (isLoggedIn && !likeAnimations[item.newsId]) {
                          e.currentTarget.style.transform = 'scale(1.15) rotate(5deg)';
                          e.currentTarget.style.boxShadow = item.isLiked 
                            ? '0 12px 28px rgba(245,87,108,0.5)' 
                            : '0 8px 20px rgba(0,0,0,0.15)';
                        }
                      }}
                      onMouseLeave={(e) => {
                        if (isLoggedIn && !likeAnimations[item.newsId]) {
                          e.currentTarget.style.transform = 'scale(1) rotate(0deg)';
                          e.currentTarget.style.boxShadow = item.isLiked 
                            ? '0 8px 20px rgba(245,87,108,0.4)' 
                            : '0 4px 12px rgba(0,0,0,0.1)';
                        }
                      }}
                      title={!isLoggedIn ? '로그인 후 이용 가능합니다' : ''}
                    >
                      <span style={{
                        fontSize: '20px',
                        filter: !isLoggedIn ? 'grayscale(100%)' : item.isLiked ? 'none' : 'grayscale(100%)',
                        transition: 'filter 0.3s'
                      }}>
                        {item.isLiked ? '❤️' : '🤍'}
                      </span>
                    </button>
                  </a>
                ))}
              </div>

              {/* 페이지네이션 */}
              {totalPages > 1 && (() => {
                // 현재 페이지 주변 5개 페이지만 표시
                const maxVisible = 5;
                let startPage = Math.max(0, currentPage - Math.floor(maxVisible / 2));
                let endPage = Math.min(totalPages, startPage + maxVisible);
                
                // 끝에 가까우면 시작 페이지 조정
                if (endPage - startPage < maxVisible) {
                  startPage = Math.max(0, endPage - maxVisible);
                }
                
                return (
                  <div style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    gap: '8px',
                    marginTop: '50px',
                    flexWrap: 'wrap',
                    padding: '20px',
                    background: 'white',
                    borderRadius: '16px',
                    border: '2px solid #e0e0e0',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.05)'
                  }}>
                    {/* 맨 처음 */}
                    <button
                      onClick={() => setCurrentPage(0)}
                      disabled={currentPage === 0}
                      style={{
                        width: '40px',
                        height: '40px',
                        background: currentPage === 0 ? '#f5f5f5' : 'white',
                        color: currentPage === 0 ? '#ccc' : '#667eea',
                        border: '2px solid #e0e0e0',
                        borderRadius: '10px',
                        cursor: currentPage === 0 ? 'not-allowed' : 'pointer',
                        transition: 'all 0.3s',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        opacity: currentPage === 0 ? 0.4 : 1
                      }}
                      onMouseEnter={(e) => {
                        if (currentPage !== 0) {
                          e.currentTarget.style.transform = 'scale(1.1)';
                          e.currentTarget.style.borderColor = '#667eea';
                        }
                      }}
                      onMouseLeave={(e) => {
                        if (currentPage !== 0) {
                          e.currentTarget.style.transform = 'scale(1)';
                          e.currentTarget.style.borderColor = '#e0e0e0';
                        }
                      }}
                    >
                      <span style={{ fontSize: '18px', fontWeight: 'bold' }}>«</span>
                    </button>

                    {/* 이전 */}
                    <button
                      onClick={() => setCurrentPage(currentPage - 1)}
                      disabled={currentPage === 0}
                      style={{
                        width: '40px',
                        height: '40px',
                        background: currentPage === 0 ? '#f5f5f5' : 'white',
                        color: currentPage === 0 ? '#ccc' : '#667eea',
                        border: '2px solid #e0e0e0',
                        borderRadius: '10px',
                        cursor: currentPage === 0 ? 'not-allowed' : 'pointer',
                        transition: 'all 0.3s',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        opacity: currentPage === 0 ? 0.4 : 1
                      }}
                      onMouseEnter={(e) => {
                        if (currentPage !== 0) {
                          e.currentTarget.style.transform = 'scale(1.1)';
                          e.currentTarget.style.borderColor = '#667eea';
                        }
                      }}
                      onMouseLeave={(e) => {
                        if (currentPage !== 0) {
                          e.currentTarget.style.transform = 'scale(1)';
                          e.currentTarget.style.borderColor = '#e0e0e0';
                        }
                      }}
                    >
                     <span style={{ fontSize: '18px', fontWeight: 'bold' }}>‹</span>
                    </button>

                    {/* 페이지 번호들 */}
                    {[...Array(endPage - startPage)].map((_, index) => {
                      const pageNum = startPage + index;
                      return (
                        <button
                          key={pageNum}
                          onClick={() => setCurrentPage(pageNum)}
                          style={{
                            minWidth: '40px',
                            height: '40px',
                            padding: '0 12px',
                            background: currentPage === pageNum 
                              ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' 
                              : 'white',
                            color: currentPage === pageNum ? 'white' : '#333',
                            border: '2px solid #e0e0e0',
                            borderRadius: '10px',
                            cursor: 'pointer',
                            fontWeight: currentPage === pageNum ? '700' : '600',
                            fontSize: '15px',
                            transition: 'all 0.3s',
                            boxShadow: currentPage === pageNum 
                              ? '0 4px 12px rgba(102,126,234,0.4)' 
                              : 'none'
                          }}
                          onMouseEnter={(e) => {
                            if (currentPage !== pageNum) {
                              e.currentTarget.style.transform = 'scale(1.1)';
                              e.currentTarget.style.borderColor = '#667eea';
                            }
                          }}
                          onMouseLeave={(e) => {
                            if (currentPage !== pageNum) {
                              e.currentTarget.style.transform = 'scale(1)';
                              e.currentTarget.style.borderColor = '#e0e0e0';
                            }
                          }}
                        >
                          {pageNum + 1}
                        </button>
                      );
                    })}

                    {/* 다음 */}
                    <button
                      onClick={() => setCurrentPage(currentPage + 1)}
                      disabled={currentPage === totalPages - 1}
                      style={{
                        width: '40px',
                        height: '40px',
                        background: currentPage === totalPages - 1 ? '#f5f5f5' : 'white',
                        color: currentPage === totalPages - 1 ? '#ccc' : '#667eea',
                        border: '2px solid #e0e0e0',
                        borderRadius: '10px',
                        cursor: currentPage === totalPages - 1 ? 'not-allowed' : 'pointer',
                        transition: 'all 0.3s',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        opacity: currentPage === totalPages - 1 ? 0.4 : 1
                      }}
                      onMouseEnter={(e) => {
                        if (currentPage !== totalPages - 1) {
                          e.currentTarget.style.transform = 'scale(1.1)';
                          e.currentTarget.style.borderColor = '#667eea';
                        }
                      }}
                      onMouseLeave={(e) => {
                        if (currentPage !== totalPages - 1) {
                          e.currentTarget.style.transform = 'scale(1)';
                          e.currentTarget.style.borderColor = '#e0e0e0';
                        }
                      }}
                    >
                     <span style={{ fontSize: '18px', fontWeight: 'bold' }}>›</span>
                    </button>

                    {/* 맨 끝 */}
                    <button
                      onClick={() => setCurrentPage(totalPages - 1)}
                      disabled={currentPage === totalPages - 1}
                      style={{
                        width: '40px',
                        height: '40px',
                        background: currentPage === totalPages - 1 ? '#f5f5f5' : 'white',
                        color: currentPage === totalPages - 1 ? '#ccc' : '#667eea',
                        border: '2px solid #e0e0e0',
                        borderRadius: '10px',
                        cursor: currentPage === totalPages - 1 ? 'not-allowed' : 'pointer',
                        transition: 'all 0.3s',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        opacity: currentPage === totalPages - 1 ? 0.4 : 1
                      }}
                      onMouseEnter={(e) => {
                        if (currentPage !== totalPages - 1) {
                          e.currentTarget.style.transform = 'scale(1.1)';
                          e.currentTarget.style.borderColor = '#667eea';
                        }
                      }}
                      onMouseLeave={(e) => {
                        if (currentPage !== totalPages - 1) {
                          e.currentTarget.style.transform = 'scale(1)';
                          e.currentTarget.style.borderColor = '#e0e0e0';
                        }
                      }}
                    >
                      <span style={{ fontSize: '18px', fontWeight: 'bold' }}>»</span>
                    </button>
                  </div>
                );
              })()}
            </>
          )}
        </div>

        {/* 인기 뉴스 사이드바 */}
        {popularNews.length > 0 && (
          <div style={{
            width: '320px',
            position: 'sticky',
            top: '20px',
            flexShrink: 0
          }}>
            <div style={{
              background: 'white',
              borderRadius: '16px',
              border: '2px solid #e0e0e0',
              boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
              overflow: 'hidden'
            }}>
              {/* 헤더 */}
              <div style={{
                padding: '20px',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                color: 'white'
              }}>
                <h3 style={{
                  fontSize: '18px',
                  fontWeight: '800',
                  margin: 0,
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px'
                }}>
                  🔥 실시간 인기 뉴스
                </h3>
                <p style={{
                  fontSize: '12px',
                  margin: '4px 0 0 0',
                  opacity: 0.9
                }}>
                  조회수 기준 TOP 10
                </p>
              </div>

              {/* 뉴스 목록 */}
              <div style={{
                maxHeight: '600px',
                overflowY: 'auto'
              }}>
                {popularNews.slice(0, 10).map((item, index) => (
                  <a
                  key={item.newsId}
                  href={item.sourceUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  onClick={(e) => {
                    console.log('=== 인기 뉴스 클릭 ===');
                    console.log('뉴스 ID:', item.newsId);
                    console.log('뉴스 제목:', item.title);
                    console.log('sourceUrl:', item.sourceUrl);
                    console.log('전체 데이터:', item);
                    increaseViewCount(item.newsId);
                  }}
                    style={{
                      display: 'block',
                      padding: '16px',
                      textDecoration: 'none',
                      color: 'inherit',
                      borderBottom: index < 9 ? '1px solid #f0f0f0' : 'none',
                      transition: 'all 0.2s',
                      cursor: 'pointer',
                      position: 'relative'
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.background = '#f8f9fa';
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.background = 'white';
                    }}
                  >
                    {/* 순위 배지 */}
                    <div style={{
                      position: 'absolute',
                      top: '16px',
                      left: '16px',
                      width: '24px',
                      height: '24px',
                      borderRadius: '4px',
                      background: index < 3 
                        ? 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' 
                        : '#667eea',
                      color: 'white',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontWeight: '800',
                      fontSize: '12px',
                      boxShadow: '0 2px 8px rgba(0,0,0,0.15)'
                    }}>
                      {index + 1}
                    </div>

                    {/* 내용 */}
                    <div style={{ paddingLeft: '36px' }}>
                      {/* 종목 태그 */}
                      <span style={{
                        display: 'inline-block',
                        fontSize: '10px',
                        color: 'white',
                        padding: '2px 8px',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        borderRadius: '4px',
                        fontWeight: '700',
                        marginBottom: '6px'
                      }}>
                        {item.sportDisplayName}
                      </span>

                      {/* 제목 */}
                      <h4 style={{
                        fontSize: '13px',
                        fontWeight: '700',
                        margin: '0 0 8px 0',
                        lineHeight: 1.4,
                        color: '#1a1a1a',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        display: '-webkit-box',
                        WebkitLineClamp: 2,
                        WebkitBoxOrient: 'vertical'
                      }}>
                        {item.title}
                      </h4>

                      {/* 통계 */}
                      <div style={{
                        fontSize: '11px',
                        color: '#8e8e8e',
                        display: 'flex',
                        gap: '8px',
                        alignItems: 'center',
                        fontWeight: '600'
                      }}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: '3px' }}>
                          👁️ {item.viewCount.toLocaleString()}
                        </span>
                        <span style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '3px',
                          color: item.isLiked ? '#ed4956' : '#8e8e8e'
                        }}>
                          {item.isLiked ? '❤️' : '🤍'} {(item.likeCount || 0).toLocaleString()}
                        </span>
                      </div>
                    </div>
                  </a>
                ))}
              </div>

              {/* 새로고침 버튼 */}
              <div style={{
                padding: '12px',
                borderTop: '1px solid #f0f0f0',
                textAlign: 'center'
              }}>
                <button
                  onClick={(e) => {
                    e.preventDefault();
                    fetchPopularNews();
                    showToast('인기 뉴스를 새로고침했습니다!');
                  }}
                  style={{
                    padding: '8px 16px',
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    color: 'white',
                    border: 'none',
                    borderRadius: '8px',
                    cursor: 'pointer',
                    fontSize: '12px',
                    fontWeight: '700',
                    transition: 'all 0.3s'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'scale(1.05)';
                    e.currentTarget.style.boxShadow = '0 4px 12px rgba(102,126,234,0.4)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'scale(1)';
                    e.currentTarget.style.boxShadow = 'none';
                  }}
                >
                  🔄 새로고침
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

     {/* 스크롤 탑 버튼 */}
     {showScrollTop && (
        <button
          onClick={scrollToTop}
          style={{
            position: 'fixed',
            bottom: '30px',
            right: '30px',
            width: '56px',
            height: '56px',
            borderRadius: '50%',
            border: 'none',
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            cursor: 'pointer',
            boxShadow: '0 8px 24px rgba(102,126,234,0.5)',
            transition: 'all 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
            zIndex: 1000,
            animation: 'bounceIn 0.6s',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'translateY(-8px) scale(1.1)';
            e.currentTarget.style.boxShadow = '0 16px 40px rgba(102,126,234,0.6)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'translateY(0) scale(1)';
            e.currentTarget.style.boxShadow = '0 8px 24px rgba(102,126,234,0.5)';
          }}
        >
          {/* 화살표 SVG */}
          <svg 
            width="24" 
            height="24" 
            viewBox="0 0 24 24" 
            fill="none" 
            style={{
              transition: 'transform 0.3s'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = 'translateY(-3px)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = 'translateY(0)';
            }}
          >
            <path 
              d="M12 19V5M12 5L5 12M12 5L19 12" 
              stroke="white" 
              strokeWidth="3" 
              strokeLinecap="round" 
              strokeLinejoin="round"
            />
          </svg>
        </button>
      )}

      {/* 토스트 알림 */}
      {toast.show && (
        <div style={{
          position: 'fixed',
          top: '90px',
          left: '50%',
          transform: `translateX(-50%) translateY(${toast.show ? '0' : '-20px'})`,
          padding: '16px 32px',
          background: toast.type === 'error' 
            ? 'linear-gradient(135deg, #f5576c 0%, #f093fb 100%)' 
            : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          color: 'white',
          borderRadius: '12px',
          boxShadow: '0 8px 24px rgba(0,0,0,0.2)',
          zIndex: 2000,
          fontWeight: '600',
          fontSize: '15px',
          animation: 'slideDown 0.3s ease-out',
          backdropFilter: 'blur(10px)'
        }}>
          {toast.message}
        </div>
      )}

      {/* 애니메이션 CSS */}
      <style>{`
        @keyframes fadeInUp {
          from {
            opacity: 0;
            transform: translateY(30px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        
        @keyframes bounceIn {
          0% {
            opacity: 0;
            transform: scale(0.3);
          }
          50% {
            opacity: 1;
            transform: scale(1.05);
          }
          70% {
            transform: scale(0.9);
          }
          100% {
            transform: scale(1);
          }
        }
        
        @keyframes slideDown {
          from {
            opacity: 0;
            transform: translateX(-50%) translateY(-20px);
          }
          to {
            opacity: 1;
            transform: translateX(-50%) translateY(0);
          }
        }

        /* 반응형 디자인 */
        @media (max-width: 1024px) {
          .popular-sidebar {
            display: none !important;
          }
        }
      `}</style>
    </div> 
  );
}

export default News;