/**
 * 뉴스 관련 API 함수들
 */

import { apiGet } from './api';

/**
 * 전체 뉴스 조회
 */
export const getNews = async () => {
    return await apiGet('/api/news');
};

/**
 * 종목별 뉴스 조회
 */
export const getNewsBySport = async (sportName) => {
    return await apiGet(`/api/news/sport/${encodeURIComponent(sportName)}`);
};

/**
 * 인기 뉴스 조회 (TOP 10)
 */
export const getPopularNews = async (limit = 10) => {
    return await apiGet(`/api/news/popular?limit=${limit}`);
};