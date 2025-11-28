import { useState, useEffect, useRef } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { checkAuth, logout as apiLogout } from '../api/auth';
import mypageApi from '../api/mypageApi';

const Header = () => {
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [user, setUser] = useState(null);
    const [isCheckingAuth, setIsCheckingAuth] = useState(true);
    const [unreadCount, setUnreadCount] = useState(0);
    const [showNotifications, setShowNotifications] = useState(false);
    const [notifications, setNotifications] = useState([]);
    const notificationRef = useRef(null);
    const location = useLocation();
    const navigate = useNavigate();

    // 초기 로드 시 세션 확인 (한 번만 실행)
    useEffect(() => {
        const verifySession = async () => {
            try {
                // 백엔드 세션 확인
                const userData = await checkAuth();
                setUser(userData);
            } catch (err) {
                // 세션이 없거나 만료됨
                setUser(null);
            } finally {
                setIsCheckingAuth(false);
            }
        };

        verifySession();
    }, []); // 마운트 시 한 번만 실행

    // location 변경 시 localStorage 동기화
    useEffect(() => {
        if (!isCheckingAuth) {
            const userData = localStorage.getItem('user');
            if (userData) {
                try {
                    const parsedUser = JSON.parse(userData);
                    setUser(parsedUser);
                } catch (e) {
                    console.error('사용자 정보 파싱 오류:', e);
                    localStorage.removeItem('user');
                    setUser(null);
                }
            }
        }
    }, [location, isCheckingAuth]);

    // 알림 개수 로드
    useEffect(() => {
        if (user) {
            loadUnreadCount();
            // 30초마다 알림 개수 업데이트
            const interval = setInterval(loadUnreadCount, 30000);
            return () => clearInterval(interval);
        }
    }, [user]);

    // 알림 드롭다운 외부 클릭 감지
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (notificationRef.current && !notificationRef.current.contains(event.target)) {
                setShowNotifications(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const loadUnreadCount = async () => {
        try {
            const count = await mypageApi.getUnreadNotificationCount();
            setUnreadCount(count);
        } catch (err) {
            console.error('알림 개수 조회 실패:', err);
        }
    };

    const loadNotifications = async () => {
        try {
            const data = await mypageApi.getUnreadNotifications();
            setNotifications(data.slice(0, 5)); // 최근 5개만 표시
        } catch (err) {
            console.error('알림 조회 실패:', err);
        }
    };

    const handleNotificationClick = async () => {
        if (!showNotifications) {
            await loadNotifications();
        }
        setShowNotifications(!showNotifications);
    };

    const handleNotificationItemClick = async (notification) => {
        try {
            // 읽음 처리
            await mypageApi.markNotificationAsRead(notification.notificationId);
            // 알림 개수 업데이트
            loadUnreadCount();
            // 관련 페이지로 이동
            if (notification.relatedType === 'POST' && notification.relatedId) {
                navigate(`/board/post/${notification.relatedId}`);
            }
            setShowNotifications(false);
        } catch (err) {
            console.error('알림 처리 실패:', err);
        }
    };

    // 로그아웃 처리
    const handleLogout = async () => {
        try {
            await apiLogout();
        } catch (err) {
            console.error('로그아웃 오류:', err);
        }
        setUser(null);
        navigate('/');
    };

    const isActive = (path) => {
        return location.pathname === path;
    };

    const navItems = [
        { path: '/fixtures', label: '경기 일정' },
        { path: '/board', label: '커뮤니티' },
        { path: '/predictions', label: '승부예측' },
        { path: '/live', label: '실시간' },
        { path: '/news', label: '뉴스' }
    ];

    return (
        <header className="bg-gray-800/80 backdrop-blur-sm sticky top-0 z-50">
            <nav className="container mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16">
                    {/* Logo */}
                    <div className="flex items-center">
                        <Link to="/" className="text-2xl font-bold text-white hover:text-blue-400 transition">
                            SportsHub
                        </Link>
                    </div>

                    {/* Desktop Navigation */}
                    <div className="hidden md:flex md:items-center md:space-x-4">
                        {navItems.map((item) => (
                            <Link
                                key={item.path}
                                to={item.path}
                                className={`px-3 py-2 rounded-md text-sm font-medium transition ${
                                    isActive(item.path)
                                        ? 'bg-blue-600 text-white'
                                        : 'text-gray-300 hover:bg-gray-700 hover:text-white'
                                }`}
                            >
                                {item.label}
                            </Link>
                        ))}
                    </div>

                    {/* Auth Buttons (Desktop) */}
                    <div className="hidden md:flex items-center space-x-2">
                        {user ? (
                            <>
                                {/* 알림 아이콘 */}
                                <div className="relative" ref={notificationRef}>
                                    <button
                                        onClick={handleNotificationClick}
                                        className="relative p-2 text-gray-300 hover:text-white hover:bg-gray-700 rounded-md transition"
                                    >
                                        <i className="fas fa-bell text-lg"></i>
                                        {unreadCount > 0 && (
                                            <span className="absolute top-0 right-0 inline-flex items-center justify-center w-5 h-5 text-xs font-bold text-white bg-red-500 rounded-full">
                                                {unreadCount > 9 ? '9+' : unreadCount}
                                            </span>
                                        )}
                                    </button>
                                    {/* 알림 드롭다운 */}
                                    {showNotifications && (
                                        <div className="absolute right-0 mt-2 w-80 bg-gray-800 rounded-lg shadow-xl border border-gray-700 z-50">
                                            <div className="p-4 border-b border-gray-700">
                                                <h3 className="text-sm font-bold text-white">알림</h3>
                                            </div>
                                            <div className="max-h-96 overflow-y-auto">
                                                {notifications.length === 0 ? (
                                                    <div className="p-4 text-center text-gray-400 text-sm">
                                                        새로운 알림이 없습니다
                                                    </div>
                                                ) : (
                                                    notifications.map((notif) => (
                                                        <div
                                                            key={notif.notificationId}
                                                            onClick={() => handleNotificationItemClick(notif)}
                                                            className="p-4 hover:bg-gray-700 cursor-pointer border-b border-gray-700 transition"
                                                        >
                                                            <p className="text-sm text-white">{notif.content}</p>
                                                            <p className="text-xs text-gray-400 mt-1">
                                                                {new Date(notif.createdAt).toLocaleString('ko-KR')}
                                                            </p>
                                                        </div>
                                                    ))
                                                )}
                                            </div>
                                        </div>
                                    )}
                                </div>
                                <span className="text-sm font-medium text-gray-300">
                                    {user.nickname}님
                                    {user.isAdmin && <span className="ml-1">👑</span>}
                                </span>
                                {/* 관리자 버튼 - 이름과 마이페이지 사이 */}
                                {user.isAdmin && (
                                    <Link
                                        to="/admin"
                                        className={`px-3 py-2 rounded-md text-sm font-medium transition ${
                                            isActive('/admin')
                                                ? 'bg-blue-600 text-white'
                                                : 'text-gray-300 hover:bg-gray-700 hover:text-white'
                                        }`}
                                    >
                                        관리자
                                    </Link>
                                )}
                                <Link
                                    to="/mypage"
                                    className="text-sm font-medium text-gray-300 hover:text-white px-3 py-2 rounded-md hover:bg-gray-700 transition"
                                >
                                    마이페이지
                                </Link>
                                <button
                                    onClick={handleLogout}
                                    className="bg-gray-700 hover:bg-gray-600 text-white px-3 py-2 rounded-md text-sm font-medium transition"
                                >
                                    로그아웃
                                </button>
                            </>
                        ) : (
                            <>
                                <Link to="/login" className="text-sm font-medium text-gray-300 hover:text-white transition">
                                    로그인
                                </Link>
                                <Link to="/signup" className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded-md text-sm font-medium transition">
                                    회원가입
                                </Link>
                            </>
                        )}
                    </div>

                    {/* Mobile Menu Button */}
                    <div className="md:hidden">
                        <button
                            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                            className="text-gray-300 hover:text-white focus:outline-none"
                        >
                            <i className={`fas ${isMobileMenuOpen ? 'fa-times' : 'fa-bars'} fa-lg`}></i>
                        </button>
                    </div>
                </div>

                {/* Mobile Menu */}
                {isMobileMenuOpen && (
                    <div className="md:hidden">
                        <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3">
                            {navItems.map((item) => (
                                <Link
                                    key={item.path}
                                    to={item.path}
                                    onClick={() => setIsMobileMenuOpen(false)}
                                    className={`block px-3 py-2 rounded-md text-base font-medium transition ${
                                        isActive(item.path)
                                            ? 'bg-blue-600 text-white'
                                            : 'text-gray-300 hover:bg-gray-700 hover:text-white'
                                    }`}
                                >
                                    {item.label}
                                </Link>
                            ))}
                            <div className="border-t border-gray-700 pt-2 mt-2">
                                {user ? (
                                    <>
                                        <div className="px-3 py-2 text-base font-medium text-gray-300">
                                            {user.nickname}님
                                            {user.isAdmin && <span className="ml-1">👑</span>}
                                        </div>
                                        <Link
                                            to="/mypage"
                                            onClick={() => setIsMobileMenuOpen(false)}
                                            className="block px-3 py-2 rounded-md text-base font-medium text-gray-300 hover:bg-gray-700 hover:text-white transition"
                                        >
                                            마이페이지
                                        </Link>
                                        <button
                                            onClick={() => {
                                                handleLogout();
                                                setIsMobileMenuOpen(false);
                                            }}
                                            className="block w-full text-left px-3 py-2 rounded-md text-base font-medium text-gray-300 hover:bg-gray-700 hover:text-white transition"
                                        >
                                            로그아웃
                                        </button>
                                    </>
                                ) : (
                                    <>
                                        <Link
                                            to="/login"
                                            onClick={() => setIsMobileMenuOpen(false)}
                                            className="block px-3 py-2 rounded-md text-base font-medium text-gray-300 hover:bg-gray-700 hover:text-white transition"
                                        >
                                            로그인
                                        </Link>
                                        <Link
                                            to="/signup"
                                            onClick={() => setIsMobileMenuOpen(false)}
                                            className="block px-3 py-2 rounded-md text-base font-medium text-gray-300 hover:bg-gray-700 hover:text-white transition"
                                        >
                                            회원가입
                                        </Link>
                                    </>
                                )}
                            </div>
                        </div>
                    </div>
                )}
            </nav>
        </header>
    );
};

export default Header;