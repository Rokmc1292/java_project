import { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { checkAuth, logout as apiLogout } from '../api/auth';

const Header = () => {
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [user, setUser] = useState(null);
    const [isCheckingAuth, setIsCheckingAuth] = useState(true);
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