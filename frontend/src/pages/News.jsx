import { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';

/**
 * 스포츠 뉴스 페이지
 */
function News() {
  const [news, setNews] = useState([]);
  const [selectedSport, setSelectedSport] = useState('ALL');
  const [loading, setLoading] = useState(false);

  const sports = [
    { value: 'ALL', label: '전체' },
    { value: 'FOOTBALL', label: '축구' },
    { value: 'BASKETBALL', label: '농구' },
    { value: 'BASEBALL', label: '야구' },
    { value: 'LOL', label: '롤' },
    { value: 'MMA', label: 'UFC' }
  ];

  // 뉴스 조회
  const fetchNews = async () => {
    setLoading(true);
    try {
      let url = 'http://localhost:8080/api/news';
      if (selectedSport !== 'ALL') {
        url = `http://localhost:8080/api/news/sport/${selectedSport}`;
      }

      const response = await fetch(url);
      const data = await response.json();
      setNews(data.content || []);
    } catch (error) {
      console.error('뉴스 조회 실패:', error);
      setNews([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNews();
  }, [selectedSport]);

  return (
    <div>
      <Navbar />
      <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '20px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '20px' }}>
          📰 스포츠 뉴스
        </h1>

        {/* 종목 필터 */}
        <div style={{ display: 'flex', gap: '10px', marginBottom: '30px' }}>
          {sports.map((sport) => (
            <button
              key={sport.value}
              onClick={() => setSelectedSport(sport.value)}
              style={{
                padding: '10px 20px',
                backgroundColor: selectedSport === sport.value ? '#646cff' : '#f5f5f5',
                color: selectedSport === sport.value ? 'white' : '#333',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer',
                fontWeight: selectedSport === sport.value ? 'bold' : 'normal'
              }}
            >
              {sport.label}
            </button>
          ))}
        </div>

        {/* 뉴스 목록 */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: '50px', color: '#888' }}>
            로딩 중...
          </div>
        ) : news.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '50px', color: '#888' }}>
            뉴스가 없습니다.
          </div>
        ) : (
          <div style={{ display: 'grid', gap: '20px' }}>
            {news.map((item) => (
              <a
                key={item.newsId}
                href={item.sourceUrl}
                target="_blank"
                rel="noopener noreferrer"
                style={{
                  display: 'flex',
                  gap: '20px',
                  padding: '20px',
                  backgroundColor: 'white',
                  border: '2px solid #e0e0e0',
                  borderRadius: '10px',
                  textDecoration: 'none',
                  color: 'inherit',
                  transition: 'all 0.3s',
                  cursor: 'pointer'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.transform = 'translateY(-3px)';
                  e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.transform = 'translateY(0)';
                  e.currentTarget.style.boxShadow = 'none';
                }}
              >
                {/* 썸네일 */}
                {item.thumbnailUrl && (
                  <div style={{
                    width: '200px',
                    height: '140px',
                    backgroundColor: '#f0f0f0',
                    borderRadius: '8px',
                    overflow: 'hidden',
                    flexShrink: 0
                  }}>
                    <img
                      src={item.thumbnailUrl}
                      alt={item.title}
                      style={{
                        width: '100%',
                        height: '100%',
                        objectFit: 'cover'
                      }}
                    />
                  </div>
                )}

                {/* 내용 */}
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: '12px', color: '#888', marginBottom: '8px' }}>
                    {item.sportDisplayName} | {item.sourceName} | {new Date(item.publishedAt).toLocaleDateString()}
                  </div>

                  <h3 style={{ fontSize: '20px', fontWeight: 'bold', marginBottom: '10px', lineHeight: 1.4 }}>
                    {item.title}
                  </h3>

                  {item.content && (
                    <p style={{
                      color: '#666',
                      fontSize: '14px',
                      lineHeight: 1.6,
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      display: '-webkit-box',
                      WebkitLineClamp: 2,
                      WebkitBoxOrient: 'vertical'
                    }}>
                      {item.content}
                    </p>
                  )}

                  <div style={{ marginTop: '10px', fontSize: '12px', color: '#999' }}>
                    조회수: {item.viewCount}
                  </div>
                </div>
              </a>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default News;
