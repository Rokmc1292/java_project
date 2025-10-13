/**
 * 승부예측 탭 컴포넌트
 * 경기 결과 예측 (추후 구현)
 */
function PredictionTab() {
  return (
    <div style={{ padding: '20px' }}>
      <div style={{
        backgroundColor: '#2a2a2a',
        padding: '40px',
        borderRadius: '10px',
        textAlign: 'center'
      }}>
        <h1 style={{ fontSize: '32px', marginBottom: '20px' }}>🎯 승부예측</h1>
        <p style={{ color: '#aaa', fontSize: '18px', marginBottom: '20px' }}>
          경기 결과를 예측하고 티어 점수를 획득하세요!
        </p>
        <p style={{ color: '#888', fontSize: '14px', marginBottom: '30px' }}>
          ✅ 예측 성공 시 +10점 | ❌ 예측 실패 시 -10점
        </p>

        <div style={{ 
          display: 'inline-block',
          backgroundColor: '#1a1a1a',
          padding: '30px',
          borderRadius: '10px',
          marginBottom: '30px'
        }}>
          <div style={{ fontSize: '16px', color: '#aaa', marginBottom: '10px' }}>
            티어 시스템
          </div>
          <div style={{ display: 'flex', gap: '15px', justifyContent: 'center' }}>
            {['BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND'].map((tier, index) => (
              <div key={tier} style={{ textAlign: 'center' }}>
                <div style={{ 
                  fontSize: '24px',
                  color: index === 0 ? '#cd7f32' : 
                         index === 1 ? '#c0c0c0' : 
                         index === 2 ? '#ffd700' : 
                         index === 3 ? '#00ffff' : '#b9f2ff'
                }}>
                  {index === 0 ? '🥉' : index === 1 ? '🥈' : index === 2 ? '🥇' : index === 3 ? '💎' : '💠'}
                </div>
                <div style={{ fontSize: '12px', color: '#888', marginTop: '5px' }}>
                  {tier}
                </div>
              </div>
            ))}
          </div>
        </div>

        <div style={{ color: '#888' }}>
          승부예측 기능은 추후 구현 예정입니다.
        </div>
      </div>
    </div>
  );
}

export default PredictionTab;