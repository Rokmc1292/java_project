/**
 * 신고 관리 페이지
 * 파일 위치: frontend/src/pages/admin/ReportManagement.jsx
 */

export function ReportManagement() {
    const [reports, setReports] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        loadReports();
    }, []);

    const loadReports = async () => {
        setLoading(true);
        try {
            const response = await fetch(`${API_BASE_URL}/api/admin/reports`, {
                credentials: 'include'
            });
            if (response.ok) {
                const data = await response.json();
                setReports(data);
            }
        } catch (error) {
            console.error('신고 로드 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleResolveReport = async (reportId) => {
        try {
            const response = await fetch(`${API_BASE_URL}/api/admin/reports/${reportId}/resolve`, {
                method: 'POST',
                credentials: 'include'
            });

            if (response.ok) {
                alert('신고가 처리되었습니다.');
                loadReports();
            }
        } catch (error) {
            console.error('신고 처리 실패:', error);
        }
    };

    return (
        <div className="report-management">
            <div className="page-header">
                <h2>🚨 신고 관리</h2>
            </div>

            {loading ? (
                <div className="loading">로딩 중...</div>
            ) : reports.length === 0 ? (
                <div className="no-data">신고가 없습니다.</div>
            ) : (
                <div className="report-table">
                    <table>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>유형</th>
                            <th>대상</th>
                            <th>신고자</th>
                            <th>사유</th>
                            <th>신고일</th>
                            <th>상태</th>
                            <th>관리</th>
                        </tr>
                        </thead>
                        <tbody>
                        {reports.map(report => (
                            <tr key={report.reportId}>
                                <td>{report.reportId}</td>
                                <td>{report.type}</td>
                                <td>{report.targetId}</td>
                                <td>{report.reporterNickname}</td>
                                <td>{report.reason}</td>
                                <td>{new Date(report.createdAt).toLocaleDateString()}</td>
                                <td>
                    <span className={`status-badge ${report.status.toLowerCase()}`}>
                      {report.status === 'PENDING' ? '미처리' : '처리완료'}
                    </span>
                                </td>
                                <td className="action-cell">
                                    {report.status === 'PENDING' && (
                                        <button
                                            className="btn-sm btn-success"
                                            onClick={() => handleResolveReport(report.reportId)}
                                        >
                                            처리
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export default ReportManagement;