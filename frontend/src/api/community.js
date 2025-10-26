/**
 * 커뮤니티 관련 API 함수들
 * 게시글 CRUD, 댓글, 추천, 스크랩, 신고 등
 * 
 * 파일 위치: frontend/src/api/community.js
 */

import { apiGet, apiPost, apiPut, apiDelete } from './api';

// ========== 통계 및 랭킹 ==========

/**
 * 주간 베스트 게시글
 */
export const getWeeklyBestPosts = async () => {
  return await apiGet('/api/community/posts/weekly-best');
};

/**
 * 월간 베스트 게시글
 */
export const getMonthlyBestPosts = async () => {
  return await apiGet('/api/community/posts/monthly-best');
};

/**
 * 활동 왕성 유저 조회
 */
export const getTopActiveUsers = async () => {
  return await apiGet('/api/community/users/top-active');
};

/**
 * 내가 작성한 게시글 목록
 */
export const getMyPosts = async (page = 0, size = 20) => {
  return await apiGet(`/api/community/my-posts?page=${page}&size=${size}`);
};

/**
 * 내가 작성한 댓글 목록
 */
export const getMyComments = async (page = 0, size = 20) => {
  return await apiGet(`/api/community/my-comments?page=${page}&size=${size}`);
};

// ========== 관리자 기능 ==========

/**
 * 게시글 블라인드 처리 (관리자만)
 */
export const blindPost = async (postId) => {
  return await apiPost(`/api/community/posts/${postId}/blind`);
};

/**
 * 게시글 블라인드 해제 (관리자만)
 */
export const unblindPost = async (postId) => {
  return await apiDelete(`/api/community/posts/${postId}/blind`);
};//게시글 조회 ==========

/**
 * 전체 게시글 조회 (페이징, 검색)
 */
export const getPosts = async (page = 0, size = 20, search = '') => {
  const searchParam = search ? `&search=${encodeURIComponent(search)}` : '';
  return await apiGet(`/api/community/posts?page=${page}&size=${size}${searchParam}`);
};

/**
 * 카테고리별 게시글 조회
 */
export const getPostsByCategory = async (categoryName, page = 0, size = 20) => {
  return await apiGet(`/api/community/posts/category/${encodeURIComponent(categoryName)}?page=${page}&size=${size}`);
};

/**
 * 인기 게시글 조회 (전체)
 */
export const getPopularPosts = async (page = 0, size = 20) => {
  return await apiGet(`/api/community/posts/popular?page=${page}&size=${size}`);
};

/**
 * 카테고리별 인기 게시글 조회
 */
export const getPopularPostsByCategory = async (categoryName, page = 0, size = 20) => {
  return await apiGet(`/api/community/posts/category/${encodeURIComponent(categoryName)}/popular?page=${page}&size=${size}`);
};

/**
 * 게시글 상세 조회
 */
export const getPost = async (postId) => {
  return await apiGet(`/api/community/posts/${postId}`);
};

// ========== 게시글 작성/수정/삭제 ==========

/**
 * 게시글 작성 (인증 필요)
 */
export const createPost = async (categoryName, title, content) => {
  return await apiPost('/api/community/posts', {
    categoryName,
    title,
    content
  });
};

/**
 * 게시글 수정 (본인만 가능)
 */
export const updatePost = async (postId, title, content) => {
  return await apiPut(`/api/community/posts/${postId}`, {
    title,
    content
  });
};

/**
 * 게시글 삭제 (본인만 가능)
 */
export const deletePost = async (postId) => {
  return await apiDelete(`/api/community/posts/${postId}`);
};

// ========== 게시글 추천/비추천 ==========

/**
 * 게시글 추천
 */
export const likePost = async (postId) => {
  return await apiPost(`/api/community/posts/${postId}/like`);
};

/**
 * 게시글 비추천
 */
export const dislikePost = async (postId) => {
  return await apiPost(`/api/community/posts/${postId}/dislike`);
};

/**
 * 게시글 추천/비추천 취소
 */
export const cancelPostVote = async (postId) => {
  return await apiDelete(`/api/community/posts/${postId}/vote`);
};

// ========== 댓글 기능 ==========

/**
 * 게시글의 댓글 목록 조회
 */
export const getComments = async (postId) => {
  return await apiGet(`/api/community/posts/${postId}/comments`);
};

/**
 * 댓글 작성
 * @param {number} postId - 게시글 ID
 * @param {string} content - 댓글 내용
 * @param {number|null} parentCommentId - 부모 댓글 ID (대댓글인 경우)
 */
export const createComment = async (postId, content, parentCommentId = null) => {
  return await apiPost(`/api/community/posts/${postId}/comments`, {
    content,
    parentCommentId
  });
};

/**
 * 댓글 수정
 */
export const updateComment = async (commentId, content) => {
  return await apiPut(`/api/community/comments/${commentId}`, {
    content
  });
};

/**
 * 댓글 삭제
 */
export const deleteComment = async (commentId) => {
  return await apiDelete(`/api/community/comments/${commentId}`);
};

/**
 * 댓글 추천
 */
export const likeComment = async (commentId) => {
  return await apiPost(`/api/community/comments/${commentId}/like`);
};

/**
 * 댓글 비추천
 */
export const dislikeComment = async (commentId) => {
  return await apiPost(`/api/community/comments/${commentId}/dislike`);
};

// ========== 스크랩 기능 ==========

/**
 * 게시글 스크랩
 */
export const scrapPost = async (postId) => {
  return await apiPost(`/api/community/posts/${postId}/scrap`);
};

/**
 * 게시글 스크랩 취소
 */
export const unscrapPost = async (postId) => {
  return await apiDelete(`/api/community/posts/${postId}/scrap`);
};

/**
 * 내 스크랩 목록 조회
 */
export const getMyScraps = async (page = 0, size = 20) => {
  return await apiGet(`/api/community/scraps?page=${page}&size=${size}`);
};

// ========== 신고 기능 ==========

/**
 * 게시글 신고
 */
export const reportPost = async (postId, reason, description) => {
  return await apiPost(`/api/community/posts/${postId}/report`, {
    reason,
    description
  });
};

/**
 * 댓글 신고
 */
export const reportComment = async (commentId, reason, description) => {
  return await apiPost(`/api/community/comments/${commentId}/report`, {
    reason,
    description
  });
};

// ========== 사용자 차단 ==========

/**
 * 사용자 차단
 */
export const blockUser = async (blockedUsername) => {
  return await apiPost('/api/community/block', {
    blockedUsername
  });
};

/**
 * 사용자 차단 해제
 */
export const unblockUser = async (blockedUsername) => {
  return await apiDelete(`/api/community/block/${blockedUsername}`);
};

// ==========