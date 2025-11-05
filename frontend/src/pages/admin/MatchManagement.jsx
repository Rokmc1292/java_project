/**
 * 경기 관리 페이지
 * 경기 추가, 수정, 삭제, 점수 업데이트
 *
 * 파일 위치: frontend/src/pages/admin/MatchManagement.jsx
 */

import { useState, useEffect } from 'react';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function MatchManagement() {
    const [matches, setMatches] = useState([]);
    const [selectedSport, setSelectedSport] = useState('ALL');
    const [loading, setLoading] = useState(false);
    const [showModal, setShowModal] = useState(false);
    const [editingMatch, setEditingMatch] = useState(null);

    const sports = ['ALL', 'FOOTBALL', 'BASKETBALL', 'BASEBALL', 'LOL', 'MMA'];

    useEffect(() => {
        loadMatches();
    }, [selectedSport]);

    const loadMatches = async () => {
        setLoading(true);
        try {
            const sportParam = selectedSport === 'ALL' ? '' : `?sport=${selectedSport}`;
            const response = await fetch(`${API_BASE_URL}/api/admin/matches${sportParam}`, {
                credentials: 'include'
            });
            if (response.ok) {
                const data = await response.json();
                setMatches(data);
            }
        } catch (error) {
            console.error('경기 로드 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleUpdateScore = async (matchId) => {
        const homeScore = prompt('홈팀 점수 입력:');
        const awayScore = prompt('원정팀 점수 입력:');

        if (homeScore === null || awayScore === null) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/admin/matches/${matchId}/score`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({
                    homeScore: parseInt(homeScore),
                    awayScore: parseInt(awayScore),
                    status: 'LIVE'
                })
            });

            if (response.ok) {
                alert('점수가 업데이트되었습니다.');
                loadMatches();
            } else {
                alert('점수 업데이트에 실패했습니다.');
            }
        } catch (error) {
            console.error('점수 업데이트 실패:', error);
            alert('점수 업데이트 중 오류가 발생했습니다.');
        }
    };

    const handleFinishMatch = async (matchId) => {
        if (!window.confirm('경기를 종료하시겠습니까?')) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/admin/matches/${matchId}/finish`, {
                method: 'PUT',
                credentials: 'include'
            });

            if (response.ok) {
                alert('경기가 종료되었습니다.');
                loadMatches();
            } else {
                alert('경기 종료에 실패했습니다.');
            }
        } catch (error) {
            console.error('경기 종료 실패:', error);
            alert('경기 종료 중 오류가 발생했습니다.');
        }
    };

    const handleDeleteMatch = async (matchId) => {
        if (!window.confirm('정말 이 경기를 삭제하시겠습니까?')) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/admin/matches/${matchId}`, {
                method: 'DELETE',
                credentials: 'include'
            });

            if (response.ok) {
                alert('경기가 삭제되었습니다.');
                loadMatches();
            } else {
                alert('경기 삭제에 실패했습니다.');
            }
        } catch (error) {
            console.error('경기 삭제 실패:', error);
            alert('경기 삭제 중 오류가 발생했습니다.');
        }
    };

    const getStatusBadge = (status) => {
        const badges = {
            SCHEDULED: { text: '예정', class: 'scheduled' },
            LIVE: { text: '진행중', class: 'live' },
            FINISHED: { text: '종료', class: 'finished' },
            POSTPONED: { text: '연기', class: 'postponed' }
        };
        return badges[status] || badges.SCHEDULED;
    };

    return (
        <div className="match-management">
            <div className="page-header">
                <h2>⚽ 경기 관리</h2>
                <button className="btn-primary" onClick={() => setShowModal(true)}>
                    + 경기 추가
                </button>
            </div>

            {/* 종목 필터 */}
            <div className="filter-tabs">
                {sports.map(sport => (
                    <button
                        key={sport}
                        className={`filter-tab ${selectedSport === sport ? 'active' : ''}`}
                        onClick={() => setSelectedSport(sport)}
                    >
                        {sport}
                    </button>
                ))}
            </div>

            {/* 경기 목록 */}
            {loading ? (
                <div className="loading">로딩 중...</div>
            ) : matches.length === 0 ? (
                <div className="no-data">등록된 경기가 없습니다.</div>
            ) : (
                <div className="match-table">
                    <table>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>종목</th>
                            <th>리그</th>
                            <th>홈팀</th>
                            <th>점수</th>
                            <th>원정팀</th>
                            <th>경기 일시</th>
                            <th>상태</th>
                            <th>관리</th>
                        </tr>
                        </thead>
                        <tbody>
                        {matches.map(match => {
                            const statusBadge = getStatusBadge(match.status);
                            return (
                                <tr key={match.matchId}>
                                    <td>{match.matchId}</td>
                                    <td>{match.sportName}</td>
                                    <td>{match.leagueName}</td>
                                    <td>{match.homeTeam}</td>
                                    <td className="score-cell">
                                        {match.status === 'SCHEDULED' ?
                                            'VS' :
                                            `${match.homeScore || 0} - ${match.awayScore || 0}`
                                        }
                                    </td>
                                    <td>{match.awayTeam}</td>
                                    <td>{match.matchDate}</td>
                                    <td>
                      <span className={`status-badge ${statusBadge.class}`}>
                        {statusBadge.text}
                      </span>
                                    </td>
                                    <td className="action-cell">
                                        <button
                                            className="btn-sm btn-info"
                                            onClick={() => handleUpdateScore(match.matchId)}
                                            disabled={match.status === 'FINISHED'}
                                        >
                                            점수
                                        </button>
                                        <button
                                            className="btn-sm btn-success"
                                            onClick={() => handleFinishMatch(match.matchId)}
                                            disabled={match.status === 'FINISHED'}
                                        >
                                            종료
                                        </button>
                                        <button
                                            className="btn-sm btn-danger"
                                            onClick={() => handleDeleteMatch(match.matchId)}
                                        >
                                            삭제
                                        </button>
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export default MatchManagement;