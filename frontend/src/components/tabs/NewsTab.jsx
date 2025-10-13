/**
 * 뉴스 탭 컴포넌트
 * 스포츠 뉴스 목록 (추후 API 연동)
 */
function NewsTab() {
  return (
    <div style={{ padding: '20px' }}>
      <h1 style={{ fontSize: '28px', marginBottom: '20px' }}>
        📰 스포츠 뉴스
      </h1>

      {/* 카테고리 필터 */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px', flexWrap: 'wrap' }}>
        {['전체', '⚽ 축구', '⚾ 야구', '🏀 농구', '🎮 e스포츠', '🥊 UFC'].map(category => (
          <button
            key={category}
            style={{
              padding: '8px 16px',
              backgroundColor: category === '전체' ? '#646cff' : '#2a2a2a',
              color: 'white',
              border: '1px solid #444',
              borderRadius: '6px',
              cursor: 'pointer',
              fontSize: '14px'
            }}
          >
            {category}
          </button>
        ))}
      </div>

      {/* 뉴스 목록 */}
      <div style={{
        backgroundColor: '#2a2a2a',
        padding: '40px',
        borderRadius: '10px',
        textAlign: 'center',
        color: '#888'
      }}>
        스포츠 뉴스를 불러오는 중...<br/>
        (API 연동 후 실제 뉴스가 표시됩니다)
      </div>
    </div>
  );
}

export default NewsTab;