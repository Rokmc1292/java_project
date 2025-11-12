import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import '../styles/AdminCrawlPage.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 크롤링 관리 페이지
 * - 각 리그별 일정 크롤링
 * - 전체 리그 일괄 크롤링
 * - 실시간 점수 업데이트
 */
function AdminCrawlPage() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState({});

    // 관리자 권한 체크
    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (!userData) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }

        try {
            const user = JSON.parse(userData);
            if (!user.isAdmin) {
                alert('관리자 권한이 필요합니다.');
                navigate('/');
                return;
            }
        } catch (e) {
            alert('사용자 정보 오류');
            navigate('/login');
            return;
        }
    }, [navigate]);

    const handleCrawl = async (league) => {
        if (!window.confirm(`${league} 크롤링을 시작하시겠습니까?`)) return;

        setLoading(prev => ({ ...prev, [league]: true }));

        try {
            const endpoint = league === 'all-leagues'
                ? '/api/admin/crawl/all-leagues'
                : `/api/admin/crawl/${league}`;

            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                method: 'POST'
            });
            const data = await response.json();

            if (data.success) {
                alert(data.message);
            } else {
                alert(`크롤링 실패: ${data.message}`);
            }
        } catch (error) {
            console.error('크롤링 실행 실패:', error);
            alert('크롤링 실행 중 오류가 발생했습니다.');
        } finally {
            setLoading(prev => ({ ...prev, [league]: false }));
        }
    };

    const handleLiveUpdate = async (league) => {
        if (!window.confirm(`${league} 실시간 점수 업데이트를 시작하시겠습니까?`)) return;

        setLoading(prev => ({ ...prev, [`live-${league}`]: true }));

        try {
            const response = await fetch(`${API_BASE_URL}/api/admin/live/${league}`, {
                method: 'POST'
            });
            const data = await response.json();

            if (data.success) {
                alert(data.message);
            } else {
                alert(`업데이트 실패: ${data.message}`);
            }
        } catch (error) {
            console.error('실시간 업데이트 실패:', error);
            alert('실시간 업데이트 중 오류가 발생했습니다.');
        } finally {
            setLoading(prev => ({ ...prev, [`live-${league}`]: false }));
        }
    };

    return (
        <div>
            <Navbar />
            <div className="admin-crawl-container">
                <div className="crawl-header">
                    <h1 className="crawl-title">크롤링 관리</h1>
                    <button
                        className="back-btn"
                        onClick={() => navigate('/admin')}
                    >
                        관리자 페이지로 돌아가기
                    </button>
                </div>

                {/* 전체 리그 크롤링 */}
                <div className="crawl-section highlight">
                    <h2>전체 리그 크롤링</h2>
                    <div className="crawl-card all-leagues">
                        <div className="crawl-info">
                            <h3>모든 리그 일괄 크롤링</h3>
                            <p>EPL, NBA, Bundesliga, La Liga, Serie A, Ligue 1, KBL 전체 리그의 일정을 순차적으로 크롤링합니다.</p>
                            <p className="warning-text">완료까지 상당한 시간이 소요될 수 있습니다.</p>
                        </div>
                        <button
                            className="crawl-btn all-leagues-btn"
                            onClick={() => handleCrawl('all-leagues')}
                            disabled={loading['all-leagues']}
                        >
                            {loading['all-leagues'] ? '크롤링 중...' : '전체 리그 크롤링 시작'}
                        </button>
                    </div>
                </div>

                {/* 개별 리그 크롤링 */}
                <div className="crawl-section">
                    <h2>개별 리그 크롤링</h2>
                    <div className="crawl-grid">
                        {/* EPL */}
                        <div className="crawl-card">
                            <div className="crawl-info">
                                <h3>EPL (프리미어리그)</h3>
                                <p>잉글랜드 프리미어리그 일정 크롤링</p>
                            </div>
                            <div className="crawl-actions">
                                <button
                                    className="crawl-btn"
                                    onClick={() => handleCrawl('epl')}
                                    disabled={loading['epl']}
                                >
                                    {loading['epl'] ? '크롤링 중...' : '일정 크롤링'}
                                </button>
                                <button
                                    className="live-btn"
                                    onClick={() => handleLiveUpdate('epl')}
                                    disabled={loading['live-epl']}
                                >
                                    {loading['live-epl'] ? '업데이트 중...' : '실시간 업데이트'}
                                </button>
                            </div>
                        </div>

                        {/* NBA */}
                        <div className="crawl-card">
                            <div className="crawl-info">
                                <h3>NBA</h3>
                                <p>미국 프로농구 일정 크롤링</p>
                            </div>
                            <div className="crawl-actions">
                                <button
                                    className="crawl-btn"
                                    onClick={() => handleCrawl('nba')}
                                    disabled={loading['nba']}
                                >
                                    {loading['nba'] ? '크롤링 중...' : '일정 크롤링'}
                                </button>
                                <button
                                    className="live-btn"
                                    onClick={() => handleLiveUpdate('nba')}
                                    disabled={loading['live-nba']}
                                >
                                    {loading['live-nba'] ? '업데이트 중...' : '실시간 업데이트'}
                                </button>
                            </div>
                        </div>

                        {/* Bundesliga */}
                        <div className="crawl-card">
                            <div className="crawl-info">
                                <h3>Bundesliga (분데스리가)</h3>
                                <p>독일 분데스리가 일정 크롤링</p>
                            </div>
                            <div className="crawl-actions">
                                <button
                                    className="crawl-btn"
                                    onClick={() => handleCrawl('bundesliga')}
                                    disabled={loading['bundesliga']}
                                >
                                    {loading['bundesliga'] ? '크롤링 중...' : '일정 크롤링'}
                                </button>
                                <button
                                    className="live-btn"
                                    onClick={() => handleLiveUpdate('bundesliga')}
                                    disabled={loading['live-bundesliga']}
                                >
                                    {loading['live-bundesliga'] ? '업데이트 중...' : '실시간 업데이트'}
                                </button>
                            </div>
                        </div>

                        {/* La Liga */}
                        <div className="crawl-card">
                            <div className="crawl-info">
                                <h3>La Liga (라리가)</h3>
                                <p>스페인 라리가 일정 크롤링</p>
                            </div>
                            <div className="crawl-actions">
                                <button
                                    className="crawl-btn"
                                    onClick={() => handleCrawl('laliga')}
                                    disabled={loading['laliga']}
                                >
                                    {loading['laliga'] ? '크롤링 중...' : '일정 크롤링'}
                                </button>
                                <button
                                    className="live-btn"
                                    onClick={() => handleLiveUpdate('laliga')}
                                    disabled={loading['live-laliga']}
                                >
                                    {loading['live-laliga'] ? '업데이트 중...' : '실시간 업데이트'}
                                </button>
                            </div>
                        </div>

                        {/* Serie A */}
                        <div className="crawl-card">
                            <div className="crawl-info">
                                <h3>Serie A (세리에 A)</h3>
                                <p>이탈리아 세리에 A 일정 크롤링</p>
                            </div>
                            <div className="crawl-actions">
                                <button
                                    className="crawl-btn"
                                    onClick={() => handleCrawl('seriea')}
                                    disabled={loading['seriea']}
                                >
                                    {loading['seriea'] ? '크롤링 중...' : '일정 크롤링'}
                                </button>
                                <button
                                    className="live-btn"
                                    onClick={() => handleLiveUpdate('seriea')}
                                    disabled={loading['live-seriea']}
                                >
                                    {loading['live-seriea'] ? '업데이트 중...' : '실시간 업데이트'}
                                </button>
                            </div>
                        </div>

                        {/* Ligue 1 */}
                        <div className="crawl-card">
                            <div className="crawl-info">
                                <h3>Ligue 1 (리그 1)</h3>
                                <p>프랑스 리그 1 일정 크롤링</p>
                            </div>
                            <div className="crawl-actions">
                                <button
                                    className="crawl-btn"
                                    onClick={() => handleCrawl('ligue1')}
                                    disabled={loading['ligue1']}
                                >
                                    {loading['ligue1'] ? '크롤링 중...' : '일정 크롤링'}
                                </button>
                                <button
                                    className="live-btn"
                                    onClick={() => handleLiveUpdate('ligue1')}
                                    disabled={loading['live-ligue1']}
                                >
                                    {loading['live-ligue1'] ? '업데이트 중...' : '실시간 업데이트'}
                                </button>
                            </div>
                        </div>

                        {/* KBL */}
                        <div className="crawl-card">
                            <div className="crawl-info">
                                <h3>KBL (한국프로농구)</h3>
                                <p>한국 프로농구 일정 크롤링</p>
                            </div>
                            <div className="crawl-actions">
                                <button
                                    className="crawl-btn"
                                    onClick={() => handleCrawl('kbl')}
                                    disabled={loading['kbl']}
                                >
                                    {loading['kbl'] ? '크롤링 중...' : '일정 크롤링'}
                                </button>
                                <button
                                    className="live-btn"
                                    onClick={() => handleLiveUpdate('kbl')}
                                    disabled={loading['live-kbl']}
                                >
                                    {loading['live-kbl'] ? '업데이트 중...' : '실시간 업데이트'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                {/* 안내 메시지 */}
                <div className="crawl-notice">
                    <h3>크롤링 안내</h3>
                    <ul>
                        <li>크롤링은 백그라운드에서 실행되며, 완료까지 수 분이 소요될 수 있습니다.</li>
                        <li>전체 리그 크롤링은 모든 리그를 순차적으로 실행하므로 상당한 시간이 소요됩니다.</li>
                        <li>실시간 업데이트는 현재 진행 중인 경기의 점수를 업데이트합니다.</li>
                        <li>크롤링 중 브라우저를 닫아도 서버에서 계속 실행됩니다.</li>
                    </ul>
                </div>
            </div>
        </div>
    );
}

export default AdminCrawlPage;
