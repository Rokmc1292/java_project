import React, { useState } from 'react';
import {
    LayoutDashboard,
    Users,
    AlertTriangle,
    FileText,
    BarChart3,
    Menu,
    X,
    LogOut,
    Settings
} from 'lucide-react';

// 각 페이지 컴포넌트 임포트 (실제 프로젝트에서는 별도 파일로 분리)
const AdminDashboard = () => <div className="p-6">대시보드 컴포넌트</div>;
const AdminUsers = () => <div className="p-6">사용자 관리 컴포넌트</div>;
const AdminReports = () => <div className="p-6">신고 관리 컴포넌트</div>;
const AdminPosts = () => <div className="p-6">게시글 관리 컴포넌트</div>;
const AdminStatistics = () => <div className="p-6">통계 컴포넌트</div>;

const AdminLayout = () => {
    const [currentPage, setCurrentPage] = useState('dashboard');
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [adminInfo] = useState({
        username: 'admin',
        nickname: '관리자'
    });

    const menuItems = [
        {
            id: 'dashboard',
            label: '대시보드',
            icon: <LayoutDashboard className="w-5 h-5" />,
            component: AdminDashboard
        },
        {
            id: 'users',
            label: '사용자 관리',
            icon: <Users className="w-5 h-5" />,
            component: AdminUsers
        },
        {
            id: 'reports',
            label: '신고 관리',
            icon: <AlertTriangle className="w-5 h-5" />,
            badge: 12,
            component: AdminReports
        },
        {
            id: 'posts',
            label: '게시글 관리',
            icon: <FileText className="w-5 h-5" />,
            component: AdminPosts
        },
        {
            id: 'statistics',
            label: '통계 및 분석',
            icon: <BarChart3 className="w-5 h-5" />,
            component: AdminStatistics
        }
    ];

    const CurrentComponent = menuItems.find(item => item.id === currentPage)?.component || AdminDashboard;

    const handleLogout = async () => {
        if (confirm('로그아웃 하시겠습니까?')) {
            try {
                await fetch('http://localhost:8080/api/auth/logout', {
                    method: 'POST',
                    credentials: 'include'
                });
                window.location.href = '/';
            } catch (error) {
                console.error('로그아웃 실패:', error);
            }
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 flex">
            {/* 사이드바 */}
            <aside
                className={`
          fixed inset-y-0 left-0 z-50
          w-64 bg-gray-900 text-white
          transform transition-transform duration-300 ease-in-out
          ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
          lg:translate-x-0 lg:static
        `}
            >
                {/* 로고 & 타이틀 */}
                <div className="h-16 flex items-center justify-between px-6 border-b border-gray-800">
                    <div>
                        <h1 className="text-xl font-bold">관리자 페이지</h1>
                        <p className="text-xs text-gray-400">Admin Panel</p>
                    </div>
                    <button
                        onClick={() => setSidebarOpen(false)}
                        className="lg:hidden text-gray-400 hover:text-white"
                    >
                        <X className="w-6 h-6" />
                    </button>
                </div>

                {/* 관리자 정보 */}
                <div className="px-6 py-4 border-b border-gray-800">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center font-bold">
                            {adminInfo.nickname[0]}
                        </div>
                        <div>
                            <div className="text-sm font-medium">{adminInfo.nickname}</div>
                            <div className="text-xs text-gray-400">@{adminInfo.username}</div>
                        </div>
                    </div>
                </div>

                {/* 메뉴 */}
                <nav className="flex-1 px-3 py-4 space-y-1">
                    {menuItems.map((item) => (
                        <button
                            key={item.id}
                            onClick={() => {
                                setCurrentPage(item.id);
                                setSidebarOpen(false);
                            }}
                            className={`
                w-full flex items-center justify-between px-3 py-3 rounded-lg
                transition-colors duration-200
                ${currentPage === item.id
                                ? 'bg-blue-600 text-white'
                                : 'text-gray-300 hover:bg-gray-800 hover:text-white'
                            }
              `}
                        >
                            <div className="flex items-center gap-3">
                                {item.icon}
                                <span className="font-medium">{item.label}</span>
                            </div>
                            {item.badge && (
                                <span className="px-2 py-1 text-xs font-bold bg-red-500 text-white rounded-full">
                  {item.badge}
                </span>
                            )}
                        </button>
                    ))}
                </nav>

                {/* 하단 버튼 */}
                <div className="px-3 py-4 border-t border-gray-800 space-y-1">
                    <button
                        className="w-full flex items-center gap-3 px-3 py-3 rounded-lg text-gray-300 hover:bg-gray-800 hover:text-white transition-colors"
                    >
                        <Settings className="w-5 h-5" />
                        <span className="font-medium">설정</span>
                    </button>
                    <button
                        onClick={handleLogout}
                        className="w-full flex items-center gap-3 px-3 py-3 rounded-lg text-gray-300 hover:bg-red-600 hover:text-white transition-colors"
                    >
                        <LogOut className="w-5 h-5" />
                        <span className="font-medium">로그아웃</span>
                    </button>
                </div>
            </aside>

            {/* 메인 컨텐츠 */}
            <div className="flex-1 flex flex-col min-h-screen">
                {/* 상단 헤더 (모바일) */}
                <header className="lg:hidden h-16 bg-white border-b border-gray-200 flex items-center justify-between px-4">
                    <button
                        onClick={() => setSidebarOpen(true)}
                        className="p-2 text-gray-600 hover:text-gray-900"
                    >
                        <Menu className="w-6 h-6" />
                    </button>
                    <h1 className="text-lg font-bold text-gray-900">관리자 페이지</h1>
                    <div className="w-10"></div>
                </header>

                {/* 페이지 컨텐츠 */}
                <main className="flex-1 overflow-y-auto">
                    <CurrentComponent />
                </main>
            </div>

            {/* 사이드바 오버레이 (모바일) */}
            {sidebarOpen && (
                <div
                    onClick={() => setSidebarOpen(false)}
                    className="lg:hidden fixed inset-0 bg-black bg-opacity-50 z-40"
                />
            )}
        </div>
    );
};

export default AdminLayout;