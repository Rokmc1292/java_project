import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getTodayMatches } from '../api/match';
import { getPopularPosts } from '../api/community';
import { getPopularNews } from '../api/news';
import { getPredictableMatches } from '../api/prediction';
import Navbar from '../components/Navbar';

const Home = () => {
    const [todayMatches, setTodayMatches] = useState([]);
    const [popularPosts, setPopularPosts] = useState([]);
    const [popularNews, setPopularNews] = useState([]);
    const [featuredPrediction, setFeaturedPrediction] = useState(null);
    const [loading, setLoading] = useState(true);

    // 오늘의 경기 데이터 로드
    useEffect(() => {
        const fetchTodayMatches = async () => {
            try {
                setLoading(true);
                const data = await getTodayMatches();

                if (Array.isArray(data)) {
                    setTodayMatches(data.slice(0, 3));
                } else {
                    setTodayMatches([]);
                }
            } catch (error) {
                console.error('경기 데이터 로드 실패:', error);
                setTodayMatches([]);
            } finally {
                setLoading(false);
            }
        };

        fetchTodayMatches();
    }, []);

    // 인기 게시글 데이터 로드
    useEffect(() => {
        const fetchPopularPosts = async () => {
            try {
                const data = await getPopularPosts(0, 3); // 3개만 가져오기
                console.log('인기 게시글 데이터:', data);

                if (data && data.content) {
                    setPopularPosts(data.content);
                } else if (Array.isArray(data)) {
                    setPopularPosts(data);
                } else {
                    setPopularPosts([]);
                }
            } catch (error) {
                console.error('인기 게시글 로드 실패:', error);
                setPopularPosts([]);
            }
        };

        fetchPopularPosts();
    }, []);

    // 인기 뉴스 데이터 로드
    useEffect(() => {
        const fetchPopularNews = async () => {
            try {
                const data = await getPopularNews(2);

                if (Array.isArray(data)) {
                    setPopularNews(data.slice(0, 2)); // 최대 2개로 확실히 제한
                } else {
                    setPopularNews([]);
                }
            } catch (error) {
                console.error('인기 뉴스 로드 실패:', error);
                setPopularNews([]);
            }
        };

        fetchPopularNews();
    }, []);

    // 인기 승부예측 데이터 로드
    useEffect(() => {
        const fetchFeaturedPrediction = async () => {
            try {
                const data = await getPredictableMatches('ALL', 0, 1); // 1개만
                console.log('승부예측 데이터:', data);

                if (data && data.content && data.content.length > 0) {
                    setFeaturedPrediction(data.content[0]);
                } else if (Array.isArray(data) && data.length > 0) {
                    setFeaturedPrediction(data[0]);
                } else {
                    setFeaturedPrediction(null);
                }
            } catch (error) {
                console.error('승부예측 로드 실패:', error);
                setFeaturedPrediction(null);
            }
        };

        fetchFeaturedPrediction();
    }, []);

    const scrollToTop = () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    return (
        <div className="bg-gray-900 text-white min-h-screen">

            {/* Hero Section */}
            <div className="relative overflow-hidden" style={{ height: '600px' }}>
                {/* Background Video */}
                <div className="absolute top-0 left-0 w-full h-full" style={{ zIndex: 0 }}>
                    <video
                        autoPlay
                        loop
                        muted
                        playsInline
                        className="absolute top-0 left-0 w-full h-full object-cover"
                    >
                        <source src="/videos/sports.mp4" type="video/mp4" />
                    </video>
                </div>
                <div className="absolute top-0 left-0 w-full h-full bg-black/55" style={{ zIndex: 1 }}></div>

                <section className="container mx-auto px-4 sm:px-6 lg:px-8 relative text-center h-full flex flex-col justify-center" style={{ zIndex: 10 }}>
                    <h1 className="text-4xl md:text-6xl font-bold tracking-tight">
                        <span className="block">All Sports,</span>
                        <span className="block text-blue-400">One Hub.</span>
                    </h1>
                    <p className="mt-4 text-lg md:text-xl text-gray-200 max-w-2xl mx-auto">
                        실시간 채팅, 승부예측, 커뮤니티까지. SportsHub에서 지금 바로 함께하세요!
                    </p>
                </section>
            </div>

            {/* Main Dashboard */}
            <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-8 -mt-16 relative z-20">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* Left Column */}
                    <div className="lg:col-span-2 space-y-8">
                        {/* Today's Matches */}
                        <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6">
                            <h2 className="text-xl font-bold mb-4">오늘의 주요 경기</h2>
                            <div className="space-y-4">
                                {loading ? (
                                    <div className="text-center py-8 text-gray-400">로딩 중...</div>
                                ) : todayMatches.length === 0 ? (
                                    <div className="text-center py-8 text-gray-400">오늘 예정된 경기가 없습니다.</div>
                                ) : (
                                    todayMatches.map((match) => (
                                        <div key={match.matchId} className="flex items-center p-3 bg-gray-700/50 rounded-md">
                                            <div className="flex items-center space-x-3 flex-1 justify-start">
                                                <img
                                                    src={`https://placehold.co/40x40/3B82F6/FFFFFF?text=${match.teams.home.name.slice(0, 2)}`}
                                                    alt={match.teams.home.name}
                                                    className="rounded-full"
                                                />
                                                <span className="font-medium">{match.teams.home.name}</span>
                                            </div>
                                            <div className="text-center w-[100px] flex-shrink-0">
                                                <span className="font-bold text-lg">vs</span>
                                                <p className="text-xs text-gray-400 whitespace-nowrap">
                                                    {new Date(match.detail.matchDate).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
                                                </p>
                                            </div>
                                            <div className="flex items-center space-x-3 flex-1 justify-end">
                                                <span className="font-medium">{match.teams.away.name}</span>
                                                <img
                                                    src={`https://placehold.co/40x40/FFFFFF/3B82F6?text=${match.teams.away.name.slice(0, 2)}`}
                                                    alt={match.teams.away.name}
                                                    className="rounded-full"
                                                />
                                            </div>
                                        </div>
                                    ))
                                )}

                                <Link
                                    to="/fixtures"
                                    onClick={scrollToTop}
                                    className="block text-center w-full bg-gray-700 hover:bg-gray-600 text-sm font-semibold py-2 rounded-md transition"
                                >
                                    전체 일정 보기 <i className="fa-solid fa-arrow-right ml-1"></i>
                                </Link>
                            </div>
                        </div>

                        {/* Popular Community Posts */}
                        <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6">
                            <h2 className="text-xl font-bold mb-4">🔥 커뮤니티 인기글</h2>
                            <ul className="space-y-3">
                                {popularPosts.length === 0 ? (
                                    <li className="text-center py-4 text-gray-400">인기 게시글이 없습니다.</li>
                                ) : (
                                    popularPosts.map((post) => (
                                        <li key={post.postId} className="flex items-center justify-between hover:bg-gray-700/50 p-2 rounded-md">
                                            <div className="flex items-center min-w-0 flex-1">
        <span className="text-xs bg-blue-500 text-white font-semibold px-2 py-1 rounded-full mr-3 flex-shrink-0">
            {post.categoryName || '전체'}
        </span>
                                                <Link to={`/board/${post.postId}`} className="hover:text-blue-400 truncate">
                                                    {post.title}
                                                </Link>
                                            </div>
                                            <span className="text-sm text-gray-400 font-mono ml-2 flex-shrink-0">[+{post.commentCount || 0}]</span>
                                        </li>
                                    ))
                                )}
                            </ul>
                            <Link
                                to="/board"
                                onClick={scrollToTop}
                                className="block text-center w-full mt-4 bg-gray-700 hover:bg-gray-600 text-sm font-semibold py-2 rounded-md transition"
                            >
                                커뮤니티 바로가기 <i className="fa-solid fa-arrow-right ml-1"></i>
                            </Link>
                        </div>
                    </div>

                    {/* Right Column */}
                    <div className="space-y-8">

                        {/* Prediction */}
                        <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6">
                            <h2 className="text-xl font-bold mb-4">🎯 주목할 만한 승부예측</h2>
                            {!featuredPrediction ? (
                                <div className="text-center py-8 text-gray-400">예측 가능한 경기가 없습니다.</div>
                            ) : (
                                <div className="bg-gray-700/50 rounded-lg p-4">
                                    <p className="text-xs text-blue-400 font-semibold mb-2">
                                        {featuredPrediction.league.name} / {new Date(featuredPrediction.detail.matchDate).toLocaleDateString('ko-KR')}
                                    </p>
                                    <div className="flex items-center justify-between">
                                        <div className="text-center">
                                            <img
                                                src={`https://placehold.co/48x48/3B82F6/FFFFFF?text=${featuredPrediction.teams.home.name.slice(0, 2)}`}
                                                alt={featuredPrediction.teams.home.name}
                                                className="rounded-full mx-auto"
                                            />
                                            <p className="text-sm mt-1 font-semibold">{featuredPrediction.teams.home.name}</p>
                                        </div>
                                        <span className="text-2xl font-bold text-gray-500">vs</span>
                                        <div className="text-center">
                                            <img
                                                src={`https://placehold.co/48x48/FFFFFF/3B82F6?text=${featuredPrediction.teams.away.name.slice(0, 2)}`}
                                                alt={featuredPrediction.teams.away.name}
                                                className="rounded-full mx-auto"
                                            />
                                            <p className="text-sm mt-1 font-semibold">{featuredPrediction.teams.away.name}</p>
                                        </div>
                                    </div>
                                    <Link
                                        to={`/prediction/${featuredPrediction.matchId}`}
                                        onClick={scrollToTop}
                                        className="block text-center w-full mt-4 bg-yellow-500 hover:bg-yellow-600 text-black text-sm font-bold py-2 rounded-md transition"
                                    >
                                        예측 참여하기
                                    </Link>
                                </div>
                            )}
                        </div>

                        {/* Popular News */}
                        <div className="bg-gray-800/80 backdrop-blur-sm rounded-lg p-6">
                            <h2 className="text-xl font-bold mb-4">📰 최신 인기 뉴스</h2>
                            <ul className="space-y-4">
                                {popularNews.length === 0 ? (
                                    <li className="text-center py-4 text-gray-400">뉴스가 없습니다.</li>
                                ) : (
                                    popularNews.map((news) => (
                                        <li key={news.newsId} className="hover:bg-gray-700/50 p-2 rounded-md -m-2">
                                            <a href={news.sourceUrl} target="_blank" rel="noopener noreferrer" className="flex items-start space-x-3">
                                                <img
                                                    src={news.thumbnailUrl || 'https://placehold.co/80x60/1F2937/FFFFFF?text=NEWS'}
                                                    alt={news.title}
                                                    className="w-20 h-auto rounded-md flex-shrink-0"
                                                />
                                                <div className="flex-1 min-w-0">
                                                    <h3 className="text-sm font-medium leading-tight hover:text-blue-400 transition truncate">
                                                        {news.title}
                                                    </h3>
                                                    <p className="text-xs text-gray-400 mt-1">
                                                        {news.sourceName} | {new Date(news.publishedAt).toLocaleDateString('ko-KR')}
                                                    </p>
                                                </div>
                                            </a>
                                        </li>
                                    ))
                                )}
                            </ul>
                            <Link
                                to="/news"
                                onClick={scrollToTop}
                                className="block text-center w-full mt-4 bg-gray-700 hover:bg-gray-600 text-sm font-semibold py-2 rounded-md transition"
                            >
                                뉴스 더보기 <i className="fa-solid fa-arrow-right ml-1"></i>
                            </Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Home;