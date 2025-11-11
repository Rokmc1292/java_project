import React, { useState, useEffect } from 'react';
import { AlertTriangle, CheckCircle, XCircle, Eye, Filter, ChevronLeft, ChevronRight } from 'lucide-react';

const AdminReports = () => {
    const [reports, setReports] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedReport, setSelectedReport] = useState(null);
    const [showDetail, setShowDetail] = useState(false);

    // 필터 상태
    const [statusFilter, setStatusFilter] = useState('');
    const [typeFilter, setTypeFilter] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        fetchReports();
    }, [statusFilter, typeFilter, page]);

    const fetchReports = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams({
                page: page.toString(),
                size: '20',
                ...(statusFilter && { status: statusFilter }),
                ...(typeFilter && { type: typeFilter })
            });

            const response = await fetch(`http://localhost:8080/api/admin-page/reports?${params}`, {
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                setReports(data.content);
                setTotalPages(data.totalPages);
            }
        } catch (error) {
            console.error('신고 목록 로딩 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleReportClick = async (reportId) => {
        try {
            const response = await fetch(`http://localhost:8080/api/admin-page/reports/${reportId}`, {
                credentials: 'include'
            });

            if (response.ok) {
                const data = await response.json();
                setSelectedReport(data);
                setShowDetail(true);
            }
        } catch (error) {
            console.error('신고 상세 정보 로딩 실패:', error);
        }
    };

    const handleProcessReport = async (reportId, action) => {
        const actionText = action === 'APPROVE' ? '승인' : '반려';
        const adminNote = prompt(`신고를 ${actionText}합니다. 관리자 메모를 입력하세요:`);

        if (adminNote === null) return;

        try {
            const response = await fetch(`http://localhost:8080/api/admin-page/reports/${reportId}/process`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ action, adminNote })
            });

            if (response.ok) {
                alert(`신고가 ${actionText}되었습니다.`);
                setShowDetail(false);
                setSelectedReport(null);
                fetchReports();
            }
        } catch (error) {
            console.error('신고 처리 실패:', error);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* 헤더 */}
                <div className="mb-6">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">신고 관리</h1>
                    <p className="text-gray-600">사용자 신고 내역을 확인하고 처리합니다</p>
                </div>

                {/* 통계 카드 */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
                    <StatCard
                        icon={<AlertTriangle className="w-6 h-6" />}
                        title="대기 중"
                        count={reports.filter(r => r.status === 'PENDING').length}
                        color="yellow"
                    />
                    <StatCard
                        icon={<CheckCircle className="w-6 h-6" />}
                        title="처리 완료"
                        count={reports.filter(r => r.status === 'PROCESSED').length}
                        color="green"
                    />
                    <StatCard
                        icon={<XCircle className="w-6 h-6" />}
                        title="반려"
                        count={reports.filter(r => r.status === 'REJECTED').length}
                        color="red"
                    />
                </div>

                {/* 필터 */}
                <div className="bg-white rounded-lg shadow p-6 mb-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {/* 상태 필터 */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                <Filter className="w-4 h-4 inline mr-1" />
                                상태
                            </label>
                            <select
                                value={statusFilter}
                                onChange={(e) => setStatusFilter(e.target.value)}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            >
                                <option value="">전체</option>
                                <option value="PENDING">대기 중</option>
                                <option value="PROCESSED">처리 완료</option>
                                <option value="REJECTED">반려</option>
                            </select>
                        </div>

                        {/* 타입 필터 */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">타입</label>
                            <select
                                value={typeFilter}
                                onChange={(e) => setTypeFilter(e.target.value)}
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            >
                                <option value="">전체</option>
                                <option value="POST">게시글</option>
                                <option value="COMMENT">댓글</option>
                            </select>
                        </div>
                    </div>
                </div>

                {/* 신고 목록 */}
                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    신고자
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    대상
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    사유
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    상태
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    신고일
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    관리
                                </th>
                            </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                            {loading ? (
                                <tr>
                                    <td colSpan="6" className="px-6 py-4 text-center text-gray-500">
                                        로딩 중...
                                    </td>
                                </tr>
                            ) : reports.length === 0 ? (
                                <tr>
                                    <td colSpan="6" className="px-6 py-4 text-center text-gray-500">
                                        신고가 없습니다
                                    </td>
                                </tr>
                            ) : (
                                reports.map((report) => (
                                    <tr key={report.reportId} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm font-medium text-gray-900">
                                                {report.reporterNickname}
                                            </div>
                                            <div className="text-sm text-gray-500">
                                                @{report.reporterUsername}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-2 mb-1">
                          <span className={`px-2 py-1 text-xs font-semibold rounded ${
                              report.targetType === 'POST'
                                  ? 'text-blue-800 bg-blue-100'
                                  : 'text-green-800 bg-green-100'
                          }`}>
                            {report.targetType === 'POST' ? '게시글' : '댓글'}
                          </span>
                                                <span className="text-sm text-gray-500">
                            작성자: {report.targetAuthor}
                          </span>
                                            </div>
                                            <div className="text-sm text-gray-900 max-w-xs truncate">
                                                {report.targetTitle}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm text-gray-900">{report.reason}</div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            {getStatusBadge(report.status)}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {new Date(report.createdAt).toLocaleDateString('ko-KR')}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <button
                                                onClick={() => handleReportClick(report.reportId)}
                                                className="p-1 text-blue-600 hover:bg-blue-50 rounded"
                                                title="상세보기"
                                            >
                                                <Eye className="w-5 h-5" />
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>

                    {/* 페이지네이션 */}
                    <div className="bg-gray-50 px-6 py-4 flex items-center justify-between border-t">
                        <div className="text-sm text-gray-700">
                            페이지 {page + 1} / {totalPages}
                        </div>
                        <div className="flex gap-2">
                            <button
                                onClick={() => setPage(p => Math.max(0, p - 1))}
                                disabled={page === 0}
                                className="px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                <ChevronLeft className="w-5 h-5" />
                            </button>
                            <button
                                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                                disabled={page >= totalPages - 1}
                                className="px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                <ChevronRight className="w-5 h-5" />
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* 신고 상세 모달 */}
            {showDetail && selectedReport && (
                <ReportDetailModal
                    report={selectedReport}
                    onClose={() => {
                        setShowDetail(false);
                        setSelectedReport(null);
                    }}
                    onProcess={handleProcessReport}
                />
            )}
        </div>
    );
};

// 통계 카드
const StatCard = ({ icon, title, count, color }) => {
    const colorClasses = {
        yellow: 'bg-yellow-500 text-white',
        green: 'bg-green-500 text-white',
        red: 'bg-red-500 text-white'
    };

    return (
        <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center gap-4">
                <div className={`${colorClasses[color]} p-3 rounded-lg`}>
                    {icon}
                </div>
                <div>
                    <div className="text-sm text-gray-600">{title}</div>
                    <div className="text-2xl font-bold text-gray-900">{count}</div>
                </div>
            </div>
        </div>
    );
};

// 상태 뱃지
const getStatusBadge = (status) => {
    const badges = {
        PENDING: <span className="px-2 py-1 text-xs font-semibold text-yellow-800 bg-yellow-100 rounded">대기 중</span>,
        PROCESSED: <span className="px-2 py-1 text-xs font-semibold text-green-800 bg-green-100 rounded">처리 완료</span>,
        REJECTED: <span className="px-2 py-1 text-xs font-semibold text-red-800 bg-red-100 rounded">반려</span>
    };
    return badges[status] || null;
};

// 신고 상세 모달
const ReportDetailModal = ({ report, onClose, onProcess }) => {
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-lg max-w-3xl w-full max-h-[90vh] overflow-y-auto">
                <div className="sticky top-0 bg-white border-b px-6 py-4 flex items-center justify-between">
                    <h2 className="text-xl font-bold">신고 상세 정보</h2>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600"
                    >
                        ✕
                    </button>
                </div>

                <div className="p-6 space-y-6">
                    {/* 신고 정보 */}
                    <div>
                        <h3 className="text-lg font-semibold mb-3">신고 정보</h3>
                        <div className="bg-gray-50 p-4 rounded-lg space-y-2">
                            <div className="flex justify-between">
                                <span className="text-sm text-gray-600">신고 ID:</span>
                                <span className="text-sm font-medium">{report.reportId}</span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-sm text-gray-600">신고일:</span>
                                <span className="text-sm font-medium">
                  {new Date(report.createdAt).toLocaleString('ko-KR')}
                </span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-sm text-gray-600">상태:</span>
                                {getStatusBadge(report.status)}
                            </div>
                        </div>
                    </div>

                    {/* 신고자 정보 */}
                    {report.reporter && (
                        <div>
                            <h3 className="text-lg font-semibold mb-3">신고자 정보</h3>
                            <div className="bg-blue-50 p-4 rounded-lg space-y-2">
                                <div className="flex justify-between">
                                    <span className="text-sm text-gray-600">닉네임:</span>
                                    <span className="text-sm font-medium">{report.reporter.nickname}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-sm text-gray-600">아이디:</span>
                                    <span className="text-sm font-medium">@{report.reporter.username}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-sm text-gray-600">티어:</span>
                                    <span className="text-sm font-medium">{report.reporter.tier}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-sm text-gray-600">총 신고 횟수:</span>
                                    <span className="text-sm font-medium">{report.reporter.reportCount}회</span>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* 신고 대상 정보 */}
                    {report.target && (
                        <div>
                            <h3 className="text-lg font-semibold mb-3">신고 대상</h3>
                            <div className="bg-red-50 p-4 rounded-lg space-y-3">
                                <div className="flex items-center gap-2">
                  <span className={`px-2 py-1 text-xs font-semibold rounded ${
                      report.target.targetType === 'POST'
                          ? 'text-blue-800 bg-blue-100'
                          : 'text-green-800 bg-green-100'
                  }`}>
                    {report.target.targetType === 'POST' ? '게시글' : '댓글'}
                  </span>
                                    <span className="text-sm text-gray-600">
                    작성자: {report.target.authorNickname} (@{report.target.authorUsername})
                  </span>
                                </div>
                                <div className="bg-white p-3 rounded border border-red-200">
                                    <div className="text-sm text-gray-900 whitespace-pre-wrap">
                                        {report.target.content}
                                    </div>
                                </div>
                                <div className="flex justify-between text-sm">
                                    <span className="text-gray-600">작성자가 받은 총 신고:</span>
                                    <span className="font-medium text-red-600">
                    {report.target.authorReportCount}회
                  </span>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* 신고 사유 */}
                    <div>
                        <h3 className="text-lg font-semibold mb-3">신고 사유</h3>
                        <div className="bg-gray-50 p-4 rounded-lg space-y-2">
                            <div>
                                <div className="text-sm text-gray-600 mb-1">카테고리:</div>
                                <div className="text-sm font-medium">{report.reason}</div>
                            </div>
                            {report.description && (
                                <div>
                                    <div className="text-sm text-gray-600 mb-1">상세 설명:</div>
                                    <div className="text-sm text-gray-900 whitespace-pre-wrap">
                                        {report.description}
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* 처리 버튼 (대기 중인 경우만) */}
                    {report.status === 'PENDING' && (
                        <div className="flex gap-3">
                            <button
                                onClick={() => onProcess(report.reportId, 'APPROVE')}
                                className="flex-1 px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 font-medium"
                            >
                                <CheckCircle className="w-5 h-5 inline mr-2" />
                                신고 승인 (대상 블라인드)
                            </button>
                            <button
                                onClick={() => onProcess(report.reportId, 'REJECT')}
                                className="flex-1 px-6 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 font-medium"
                            >
                                <XCircle className="w-5 h-5 inline mr-2" />
                                신고 반려
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AdminReports;