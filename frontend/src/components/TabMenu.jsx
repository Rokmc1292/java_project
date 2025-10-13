/**
 * 탭 메뉴 컴포넌트
 * 홈, 커뮤니티, 실시간, 승부예측, 경기일정, 뉴스 탭
 */
function TabMenu({ activeTab, setActiveTab }) {
  const tabs = [
    { id: 'home', label: '🏠 홈', icon: '🏠' },
    { id: 'community', label: '💬 커뮤니티', icon: '💬' },
    { id: 'live', label: '🔴 실시간', icon: '🔴' },
    { id: 'prediction', label: '🎯 승부예측', icon: '🎯' },
    { id: 'schedule', label: '📅 경기일정', icon: '📅' },
    { id: 'news', label: '📰 뉴스', icon: '📰' }
  ];

  return (
    <div style={{
      backgroundColor: '#2a2a2a',
      padding: '0',
      display: 'flex',
      justifyContent: 'center',
      borderBottom: '1px solid #444'
    }}>
      {tabs.map(tab => (
        <button
          key={tab.id}
          onClick={() => setActiveTab(tab.id)}
          style={{
            padding: '15px 30px',
            backgroundColor: activeTab === tab.id ? '#646cff' : 'transparent',
            color: activeTab === tab.id ? 'white' : '#aaa',
            border: 'none',
            cursor: 'pointer',
            fontSize: '16px',
            fontWeight: activeTab === tab.id ? 'bold' : 'normal',
            transition: 'all 0.3s',
            borderBottom: activeTab === tab.id ? '3px solid #646cff' : '3px solid transparent'
          }}
          onMouseEnter={(e) => {
            if (activeTab !== tab.id) {
              e.target.style.backgroundColor = '#333';
              e.target.style.color = 'white';
            }
          }}
          onMouseLeave={(e) => {
            if (activeTab !== tab.id) {
              e.target.style.backgroundColor = 'transparent';
              e.target.style.color = '#aaa';
            }
          }}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}

export default TabMenu;