import React, { useState, useEffect } from 'react';
import { BarChart3, Users, FileText, AlertTriangle, TrendingUp, Eye, MessageSquare, Target } from 'lucide-react';

const AdminDashboard = () => {
    const [dashboardData, setDashboardData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/admin-page/dashboard', {
                credentials: 'include'
            });
            if (response.ok) {
                const data = await response.json();
                setDashboardData(data);
            }
        } catch (error) {
            console.error('대시보드 데이터 로딩 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="text-xl">로딩 중...</div>
            </div>
        );
    }

    if (!dashboardData) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="text-xl text-red-500">데이터를 불러올 수 없습니다</div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-7xl mx-auto">
                {/* 헤더 */}
                <div className="mb-8">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">관리자 대시보드</h1>
                    <p className="text-gray-600">시스템 전체 현황을 한눈에 확인하세요</p>
                </div>

                {/* 통계 카드 */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                    <StatCard
                        icon={<Users className="w-8 h-8" />}
                        title="전체 회원"
                        value={dashboardData.totalUsers}
                        subtitle={`오늘 +${dashboardData.todayNewUsers}명`}
                        color="blue"
                    />
                    <StatCard
                        icon={<FileText className="w-8 h-8" />}
                        title="전체 게시글"
                        value={dashboardData.totalPosts}
                        subtitle={`오늘 +${dashboardData.todayNewPosts}개`}
                        color="green"
                    />
                    <StatCard
                        icon={<Target className="w-8 h-8" />}
                        title="전체 예측"
                        value={dashboardData.totalPredictions}
                        subtitle={`오늘 +${dashboardData.todayNewPredictions}개`}
                        color="purple"
                    />
                    <StatCard
                        icon={<AlertTriangle className="w-8 h-8" />}
                        title="대기 중인 신고"
                        value={dashboardData.pendingReports}
                        subtitle="처리 필요"
                        color="red"
                    />
                </div>

                {/* 주간 통계 차트 */}
                <div className="bg-white rounded-lg shadow p-6 mb-8">
                    <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center gap-2">
                        <TrendingUp className="w-6 h-6" />
                        최근 7일 통계
                    </h2>
                    <WeeklyChart data={dashboardData.weeklyStats} />
                </div>

                {/* 인기 게시글 & 활동적인 유저 */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                    {/* 인기 게시글 TOP 5 */}
                    <div className="bg-white rounded-lg shadow p-6">
                        <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center gap-2">
                            <BarChart3 className="w-6 h-6" />
                            인기 게시글 TOP 5
                        </h2>
                        <div className="space-y-3">
                            {dashboardData.popularPosts.map((post, index) => (
                                <div key={post.postId} className="flex items-start gap-3 p-3 hover:bg-gray-50 rounded-lg cursor-pointer">
                                    <div className="flex-shrink-0 w-8 h-8 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center font-bold">
                                        {index + 1}
                                    </div>
                                    <div className="flex-1 min-w-0">
                                        <div className="text-sm font-medium text-gray-900 truncate">{post.title}</div>
                                        <div className="text-xs text-gray-500">{post.categoryName}</div>
                                    </div>
                                    <div className="flex items-center gap-2 text-sm text-gray-600">
                                        <Eye className="w-4 h-4" />
                                        {post.viewCount}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* 활동적인 유저 TOP 5 */}
                    <div className="bg-white rounded-lg shadow p-6">
                        <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center gap-2">
                            <Users className="w-6 h-6" />
                            활동적인 유저 TOP 5
                        </h2>
                        <div className="space-y-3">
                            {dashboardData.activeUserList.map((user, index) => (
                                <div key={user.username} className="flex items-center gap-3 p-3 hover:bg-gray-50 rounded-lg cursor-pointer">
                                    <div className="flex-shrink-0 w-8 h-8 bg-green-100 text-green-600 rounded-full flex items-center justify-center font-bold">
                                        {index + 1}
                                    </div>
                                    <div className="flex-1 min-w-0">
                                        <div className="text-sm font-medium text-gray-900">{user.nickname}</div>
                                        <div className="text-xs text-gray-500">{user.tier}</div>
                                    </div>
                                    <div className="flex gap-3 text-sm text-gray-600">
                                        <div className="flex items-center gap-1">
                                            <FileText className="w-4 h-4" />
                                            {user.postCount}
                                        </div>
                                        <div className="flex items-center gap-1">
                                            <MessageSquare className="w-4 h-4" />
                                            {user.commentCount}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

// 통계 카드 컴포넌트
const StatCard = ({ icon, title, value, subtitle, color }) => {
    const colorClasses = {
        blue: 'bg-blue-500',
        green: 'bg-green-500',
        purple: 'bg-purple-500',
        red: 'bg-red-500'
    };

    return (
        <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between mb-4">
                <div className={`${colorClasses[color]} text-white p-3 rounded-lg`}>
                    {icon}
                </div>
            </div>
            <div>
                <h3 className="text-sm font-medium text-gray-600 mb-1">{title}</h3>
                <p className="text-3xl font-bold text-gray-900">{value.toLocaleString()}</p>
                <p className="text-sm text-gray-500 mt-1">{subtitle}</p>
            </div>
        </div>
    );
};

// 주간 차트 컴포넌트
const WeeklyChart = ({ data }) => {
    const maxValue = Math.max(
        ...data.map(d => Math.max(d.newUsers, d.newPosts, d.newPredictions))
    );

    return (
        <div className="space-y-6">
            {/* 범례 */}
            <div className="flex gap-6 justify-center">
                <div className="flex items-center gap-2">
                    <div className="w-4 h-4 bg-blue-500 rounded"></div>
                    <span className="text-sm text-gray-600">신규 회원</span>
                </div>
                <div className="flex items-center gap-2">
                    <div className="w-4 h-4 bg-green-500 rounded"></div>
                    <span className="text-sm text-gray-600">신규 게시글</span>
                </div>
                <div className="flex items-center gap-2">
                    <div className="w-4 h-4 bg-purple-500 rounded"></div>
                    <span className="text-sm text-gray-600">신규 예측</span>
                </div>
            </div>

            {/* 차트 */}
            <div className="space-y-4">
                {data.map((day) => (
                    <div key={day.date} className="space-y-2">
                        <div className="text-sm font-medium text-gray-700">
                            {new Date(day.date).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })}
                        </div>
                        <div className="flex gap-2">
                            <div className="flex-1 space-y-1">
                                <div className="flex items-center gap-2">
                                    <div
                                        className="h-6 bg-blue-500 rounded transition-all"
                                        style={{ width: `${(day.newUsers / maxValue) * 100}%` }}
                                    ></div>
                                    <span className="text-sm text-gray-600">{day.newUsers}</span>
                                </div>
                                <div className="flex items-center gap-2">
                                    <div
                                        className="h-6 bg-green-500 rounded transition-all"
                                        style={{ width: `${(day.newPosts / maxValue) * 100}%` }}
                                    ></div>
                                    <span className="text-sm text-gray-600">{day.newPosts}</span>
                                </div>
                                <div className="flex items-center gap-2">
                                    <div
                                        className="h-6 bg-purple-500 rounded transition-all"
                                        style={{ width: `${(day.newPredictions / maxValue) * 100}%` }}
                                    ></div>
                                    <span className="text-sm text-gray-600">{day.newPredictions}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default AdminDashboard;