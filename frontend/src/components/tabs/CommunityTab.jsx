/**
 * 커뮤니티 탭 컴포넌트
 * 게시판 목록 (추후 구현)
 */
function CommunityTab() {
  return (
    <div style={{ padding: '20px' }}>
      <div style={{
        backgroundColor: '#2a2a2a',
        padding: '40px',
        borderRadius: '10px',
        textAlign: 'center'
      }}>
        <h1 style={{ fontSize: '32px', marginBottom: '20px' }}>💬 커뮤니티</h1>
        <p style={{ color: '#aaa', fontSize: '18px', marginBottom: '30px' }}>
          스포츠 팬들과 자유롭게 소통하세요
        </p>
        
        {/* 카테고리 버튼들 */}
        <div style={{ display: 'flex', gap: '10px', justifyContent: 'center', flexWrap: 'wrap' }}>
          {['⚽ 축구', '⚾ 야구', '🏀 농구', '🎮 e스포츠', '🥊 UFC', '💭 자유게시판'].map(category => (
            <button
              key={category}
              style={{
                padding: '12px 24px',
                backgroundColor: '#646cff',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                fontSize: '16px',
                cursor: 'pointer'
              }}
            >
              {category}
            </button>
          ))}
        </div>

        <div style={{ marginTop: '40px', color: '#888' }}>
          게시판 기능은 추후 구현 예정입니다.
        </div>
      </div>
    </div>
  );
}

export default CommunityTab;