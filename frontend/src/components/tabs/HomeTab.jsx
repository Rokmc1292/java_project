/**
 * 홈 탭 컴포넌트
 * 사이트 배너, 오늘의 경기 일정, 인기 게시글 등 요약 정보
 */
function HomeTab() {
  return (
    <div style={{ padding: '20px' }}>
      {/* 메인 배너 */}
      <div style={{
        backgroundColor: '#2a2a2a',
        padding: '60px 40px',
        borderRadius: '10px',
        marginBottom: '30px',
        textAlign: 'center',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      }}>
        <h1 style={{ fontSize: '36px', marginBottom: '15px' }}>
          스포츠 커뮤니티 플랫폼
        </h1>
        <p style={{ fontSize: '18px', color: '#eee' }}>
          실시간 경기 정보, 커뮤니티, 승부예측을 한곳에서!
        </p>
      </div>

      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', 
        gap: '20px',
        marginBottom: '30px'
      }}>
        {/* 오늘의 경기 */}
        <div style={{
          backgroundColor: '#2a2a2a',
          padding: '20px',
          borderRadius: '10px'
        }}>
          <h2 style={{ marginBottom: '15px', borderBottom: '2px solid #646cff', paddingBottom: '10px' }}>
            ⚽ 오늘의 경기
          </h2>
          <div style={{ color: '#aaa', textAlign: 'center', padding: '20px' }}>
            경기 일정을 불러오는 중...
          </div>
        </div>

        {/* 인기 게시글 */}
        <div style={{
          backgroundColor: '#2a2a2a',
          padding: '20px',
          borderRadius: '10px'
        }}>
          <h2 style={{ marginBottom: '15px', borderBottom: '2px solid #646cff', paddingBottom: '10px' }}>
            🔥 인기 게시글
          </h2>
          <div style={{ color: '#aaa', textAlign: 'center', padding: '20px' }}>
            인기 게시글을 불러오는 중...
          </div>
        </div>

        {/* 인기 승부예측 */}
        <div style={{
          backgroundColor: '#2a2a2a',
          padding: '20px',
          borderRadius: '10px'
        }}>
          <h2 style={{ marginBottom: '15px', borderBottom: '2px solid #646cff', paddingBottom: '10px' }}>
            🎯 인기 승부예측
          </h2>
          <div style={{ color: '#aaa', textAlign: 'center', padding: '20px' }}>
            승부예측을 불러오는 중...
          </div>
        </div>
      </div>

      {/* 최신 뉴스 */}
      <div style={{
        backgroundColor: '#2a2a2a',
        padding: '20px',
        borderRadius: '10px'
      }}>
        <h2 style={{ marginBottom: '15px', borderBottom: '2px solid #646cff', paddingBottom: '10px' }}>
          📰 최신 스포츠 뉴스
        </h2>
        <div style={{ color: '#aaa', textAlign: 'center', padding: '20px' }}>
          뉴스를 불러오는 중...
        </div>
      </div>
    </div>
  );
}

export default HomeTab;